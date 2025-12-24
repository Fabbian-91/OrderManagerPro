package com.ordermanager.dao;

import com.ordermanager.model.User;

import java.sql.SQLException;
import java.util.Optional;

/**
 * DAO específico para usuarios con métodos adicionales
 */
public interface UserDAO extends GenericDAO<User> {
    Optional<User> findByUsername(String username) throws SQLException;
    
    Optional<User> findByEmail(String email) throws SQLException;
    
    boolean authenticate(String username, String password) throws SQLException;
}
