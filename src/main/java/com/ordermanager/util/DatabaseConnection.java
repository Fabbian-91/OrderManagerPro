package com.ordermanager.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestión de conexiones a PostgreSQL con patrón Singleton
 */
public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static DatabaseConnection instance;
    private String url;
    private String username;
    private String password;

    private DatabaseConnection() {
        loadDatabaseProperties();
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private void loadDatabaseProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                logger.warn("database.properties no encontrado, usando valores por defecto");
                setDefaultProperties();
                return;
            }
            props.load(input);
            this.url = props.getProperty("db.url", "jdbc:postgresql://localhost:5432/ordermanager");
            this.username = props.getProperty("db.username", "postgres");
            this.password = props.getProperty("db.password", "postgres");
            
            // Cargar el driver de PostgreSQL
            Class.forName("org.postgresql.Driver");
            logger.info("Propiedades de base de datos cargadas correctamente");
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Error cargando propiedades de base de datos", e);
            setDefaultProperties();
        }
    }

    private void setDefaultProperties() {
        this.url = "jdbc:postgresql://localhost:5432/ordermanager";
        this.username = "postgres";
        this.password = "postgres";
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("Driver PostgreSQL no encontrado", e);
        }
    }

    /**
     * Obtiene una nueva conexión a la base de datos
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Cierra una conexión de forma segura
     */
    public void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error("Error cerrando conexión", e);
            }
        }
    }

    /**
     * Ejecuta rollback de forma segura
     */
    public void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                logger.error("Error en rollback", e);
            }
        }
    }
}
