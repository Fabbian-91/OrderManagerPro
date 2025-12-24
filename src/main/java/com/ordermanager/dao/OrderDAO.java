package com.ordermanager.dao;

import com.ordermanager.model.Order;

import java.sql.SQLException;
import java.util.List;

/**
 * DAO específico para pedidos
 */
public interface OrderDAO extends GenericDAO<Order> {
    List<Order> findByUserId(Long userId) throws SQLException;
    
    List<Order> findByStatus(Order.OrderStatus status) throws SQLException;
    
    List<Order> findPendingOrders() throws SQLException;
}
