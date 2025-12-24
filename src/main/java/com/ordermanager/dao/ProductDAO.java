package com.ordermanager.dao;

import com.ordermanager.model.Product;

import java.sql.SQLException;
import java.util.List;

/**
 * DAO específico para productos
 */
public interface ProductDAO extends GenericDAO<Product> {
    List<Product> findByCategory(String category) throws SQLException;
    
    List<Product> findActive() throws SQLException;
    
    List<Product> findWithLowStock(int threshold) throws SQLException;
}
