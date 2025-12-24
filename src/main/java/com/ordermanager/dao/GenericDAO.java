package com.ordermanager.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Interface genérica para operaciones CRUD
 * Demuestra uso de genéricos en Java
 * @param <T> Tipo de entidad
 */
public interface GenericDAO<T> {
    /**
     * Crea una nueva entidad
     */
    T create(T entity) throws SQLException;

    /**
     * Encuentra una entidad por ID
     */
    Optional<T> findById(Long id) throws SQLException;

    /**
     * Obtiene todas las entidades
     */
    List<T> findAll() throws SQLException;

    /**
     * Actualiza una entidad existente
     */
    boolean update(T entity) throws SQLException;

    /**
     * Elimina una entidad por ID
     */
    boolean delete(Long id) throws SQLException;

    /**
     * Cuenta el número total de entidades
     */
    long count() throws SQLException;
}
