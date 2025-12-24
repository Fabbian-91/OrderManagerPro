package com.ordermanager.dao;

import com.ordermanager.model.Product;
import com.ordermanager.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del DAO de productos
 */
public class ProductDAOImpl implements ProductDAO {
    private static final Logger logger = LoggerFactory.getLogger(ProductDAOImpl.class);
    private final DatabaseConnection dbConnection;

    public ProductDAOImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public Product create(Product product) throws SQLException {
        String sql = "INSERT INTO products (name, description, price, stock, category, active, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.setInt(4, product.getStock());
            stmt.setString(5, product.getCategory());
            stmt.setBoolean(6, product.isActive());
            stmt.setTimestamp(7, Timestamp.valueOf(product.getCreatedAt()));
            stmt.setTimestamp(8, Timestamp.valueOf(product.getUpdatedAt()));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                product.setId(rs.getLong("id"));
                logger.info("Producto creado con ID: {}", product.getId());
                return product;
            }
            throw new SQLException("No se pudo crear el producto");
        }
    }

    @Override
    public Optional<Product> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM products WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToProduct(rs));
            }
            return Optional.empty();
        }
    }

    @Override
    public List<Product> findAll() throws SQLException {
        String sql = "SELECT * FROM products ORDER BY name";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    @Override
    public boolean update(Product product) throws SQLException {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, " +
                     "stock = ?, category = ?, active = ?, updated_at = ? WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.setInt(4, product.getStock());
            stmt.setString(5, product.getCategory());
            stmt.setBoolean(6, product.isActive());
            stmt.setTimestamp(7, Timestamp.valueOf(product.getUpdatedAt()));
            stmt.setLong(8, product.getId());
            
            int rows = stmt.executeUpdate();
            logger.info("Producto actualizado: {}", product.getId());
            return rows > 0;
        }
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            int rows = stmt.executeUpdate();
            logger.info("Producto eliminado: {}", id);
            return rows > 0;
        }
    }

    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products";
        
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
    public List<Product> findByCategory(String category) throws SQLException {
        String sql = "SELECT * FROM products WHERE category = ? ORDER BY name";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    @Override
    public List<Product> findActive() throws SQLException {
        String sql = "SELECT * FROM products WHERE active = true ORDER BY name";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    @Override
    public List<Product> findWithLowStock(int threshold) throws SQLException {
        String sql = "SELECT * FROM products WHERE stock <= ? AND active = true ORDER BY stock";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, threshold);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setStock(rs.getInt("stock"));
        product.setCategory(rs.getString("category"));
        product.setActive(rs.getBoolean("active"));
        product.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        product.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return product;
    }
}
