package com.ordermanager.dao;

import com.ordermanager.model.Order;
import com.ordermanager.model.OrderItem;
import com.ordermanager.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del DAO de pedidos con transacciones
 */
public class OrderDAOImpl implements OrderDAO {
    private static final Logger logger = LoggerFactory.getLogger(OrderDAOImpl.class);
    private final DatabaseConnection dbConnection;

    public OrderDAOImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public Order create(Order order) throws SQLException {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false); // Iniciar transacción
            
            // Insertar el pedido
            String orderSql = "INSERT INTO orders (user_id, total_amount, status, shipping_address, created_at, updated_at) " +
                             "VALUES (?, ?, ?::order_status, ?, ?, ?) RETURNING id";
            
            Long orderId;
            try (PreparedStatement stmt = conn.prepareStatement(orderSql)) {
                stmt.setLong(1, order.getUserId());
                stmt.setBigDecimal(2, order.getTotalAmount());
                stmt.setString(3, order.getStatus().name());
                stmt.setString(4, order.getShippingAddress());
                stmt.setTimestamp(5, Timestamp.valueOf(order.getCreatedAt()));
                stmt.setTimestamp(6, Timestamp.valueOf(order.getUpdatedAt()));
                
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    orderId = rs.getLong("id");
                    order.setId(orderId);
                } else {
                    throw new SQLException("No se pudo crear el pedido");
                }
            }
            
            // Insertar los items del pedido
            String itemSql = "INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price, subtotal, created_at, updated_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
            
            for (OrderItem item : order.getItems()) {
                try (PreparedStatement stmt = conn.prepareStatement(itemSql)) {
                    stmt.setLong(1, orderId);
                    stmt.setLong(2, item.getProductId());
                    stmt.setString(3, item.getProductName());
                    stmt.setInt(4, item.getQuantity());
                    stmt.setBigDecimal(5, item.getUnitPrice());
                    stmt.setBigDecimal(6, item.getSubtotal());
                    stmt.setTimestamp(7, Timestamp.valueOf(item.getCreatedAt()));
                    stmt.setTimestamp(8, Timestamp.valueOf(item.getUpdatedAt()));
                    
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        item.setId(rs.getLong("id"));
                        item.setOrderId(orderId);
                    }
                }
                
                // Actualizar stock del producto
                String updateStockSql = "UPDATE products SET stock = stock - ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateStockSql)) {
                    stmt.setInt(1, item.getQuantity());
                    stmt.setLong(2, item.getProductId());
                    stmt.executeUpdate();
                }
            }
            
            conn.commit(); // Confirmar transacción
            logger.info("Pedido creado con ID: {} (con {} items)", orderId, order.getItems().size());
            return order;
            
        } catch (SQLException e) {
            if (conn != null) {
                dbConnection.rollback(conn);
            }
            logger.error("Error creando pedido", e);
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                dbConnection.closeConnection(conn);
            }
        }
    }

    @Override
    public Optional<Order> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM orders WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                loadOrderItems(order, conn);
                return Optional.of(order);
            }
            return Optional.empty();
        }
    }

    @Override
    public List<Order> findAll() throws SQLException {
        String sql = "SELECT * FROM orders ORDER BY created_at DESC";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                loadOrderItems(order, conn);
                orders.add(order);
            }
        }
        return orders;
    }

    @Override
    public boolean update(Order order) throws SQLException {
        String sql = "UPDATE orders SET user_id = ?, total_amount = ?, status = ?::order_status, " +
                     "shipping_address = ?, updated_at = ? WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, order.getUserId());
            stmt.setBigDecimal(2, order.getTotalAmount());
            stmt.setString(3, order.getStatus().name());
            stmt.setString(4, order.getShippingAddress());
            stmt.setTimestamp(5, Timestamp.valueOf(order.getUpdatedAt()));
            stmt.setLong(6, order.getId());
            
            int rows = stmt.executeUpdate();
            logger.info("Pedido actualizado: {}", order.getId());
            return rows > 0;
        }
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Eliminar items primero
            String deleteItemsSql = "DELETE FROM order_items WHERE order_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteItemsSql)) {
                stmt.setLong(1, id);
                stmt.executeUpdate();
            }
            
            // Eliminar pedido
            String deleteOrderSql = "DELETE FROM orders WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteOrderSql)) {
                stmt.setLong(1, id);
                stmt.executeUpdate();
            }
            
            conn.commit();
            logger.info("Pedido eliminado: {}", id);
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                dbConnection.rollback(conn);
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                dbConnection.closeConnection(conn);
            }
        }
    }

    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        }
    }

    @Override
    public List<Order> findByUserId(Long userId) throws SQLException {
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                loadOrderItems(order, conn);
                orders.add(order);
            }
        }
        return orders;
    }

    @Override
    public List<Order> findByStatus(Order.OrderStatus status) throws SQLException {
        String sql = "SELECT * FROM orders WHERE status = ?::order_status ORDER BY created_at DESC";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                loadOrderItems(order, conn);
                orders.add(order);
            }
        }
        return orders;
    }

    @Override
    public List<Order> findPendingOrders() throws SQLException {
        return findByStatus(Order.OrderStatus.PENDING);
    }

    private void loadOrderItems(Order order, Connection conn) throws SQLException {
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, order.getId());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                OrderItem item = mapResultSetToOrderItem(rs);
                order.addItem(item);
            }
        }
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setUserId(rs.getLong("user_id"));
        order.setShippingAddress(rs.getString("shipping_address"));
        order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
        order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        order.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return order;
    }

    private OrderItem mapResultSetToOrderItem(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();
        item.setId(rs.getLong("id"));
        item.setOrderId(rs.getLong("order_id"));
        item.setProductId(rs.getLong("product_id"));
        item.setProductName(rs.getString("product_name"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        item.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return item;
    }
}
