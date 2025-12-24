package com.ordermanager.util;

import com.ordermanager.model.Order;
import com.ordermanager.model.OrderItem;
import com.ordermanager.model.Product;
import com.ordermanager.model.User;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilidad para exportar e importar datos en formato CSV
 * Implementa I/O de archivos para backup y restauración
 */
public class CSVHandler {
    private static final Logger logger = LoggerFactory.getLogger(CSVHandler.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String BACKUP_DIR = "backups";

    static {
        // Crear directorio de backups si no existe
        try {
            Path backupPath = Paths.get(BACKUP_DIR);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
            }
        } catch (IOException e) {
            logger.error("Error creando directorio de backups", e);
        }
    }

    /**
     * Exporta usuarios a CSV
     */
    public static void exportUsers(List<User> users, String filename) throws IOException {
        String filepath = BACKUP_DIR + File.separator + filename;
        
        try (Writer writer = new FileWriter(filepath);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("ID", "Username", "Email", "FullName", "Role", "CreatedAt", "UpdatedAt"))) {
            
            for (User user : users) {
                printer.printRecord(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getRole().name(),
                    user.getCreatedAt().format(DATE_FORMATTER),
                    user.getUpdatedAt().format(DATE_FORMATTER)
                );
            }
            
            logger.info("Exportados {} usuarios a {}", users.size(), filepath);
        }
    }

    /**
     * Importa usuarios desde CSV
     */
    public static List<User> importUsers(String filename) throws IOException {
        String filepath = BACKUP_DIR + File.separator + filename;
        List<User> users = new ArrayList<>();
        
        try (Reader reader = new FileReader(filepath);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {
            
            for (CSVRecord record : parser) {
                User user = new User();
                user.setId(Long.parseLong(record.get("ID")));
                user.setUsername(record.get("Username"));
                user.setEmail(record.get("Email"));
                user.setFullName(record.get("FullName"));
                user.setRole(User.UserRole.valueOf(record.get("Role")));
                user.setCreatedAt(LocalDateTime.parse(record.get("CreatedAt"), DATE_FORMATTER));
                user.setUpdatedAt(LocalDateTime.parse(record.get("UpdatedAt"), DATE_FORMATTER));
                users.add(user);
            }
            
            logger.info("Importados {} usuarios desde {}", users.size(), filepath);
        }
        
        return users;
    }

    /**
     * Exporta productos a CSV
     */
    public static void exportProducts(List<Product> products, String filename) throws IOException {
        String filepath = BACKUP_DIR + File.separator + filename;
        
        try (Writer writer = new FileWriter(filepath);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("ID", "Name", "Description", "Price", "Stock", "Category", "Active", "CreatedAt", "UpdatedAt"))) {
            
            for (Product product : products) {
                printer.printRecord(
                    product.getId(),
                    product.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    product.getStock(),
                    product.getCategory(),
                    product.isActive(),
                    product.getCreatedAt().format(DATE_FORMATTER),
                    product.getUpdatedAt().format(DATE_FORMATTER)
                );
            }
            
            logger.info("Exportados {} productos a {}", products.size(), filepath);
        }
    }

    /**
     * Importa productos desde CSV
     */
    public static List<Product> importProducts(String filename) throws IOException {
        String filepath = BACKUP_DIR + File.separator + filename;
        List<Product> products = new ArrayList<>();
        
        try (Reader reader = new FileReader(filepath);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {
            
            for (CSVRecord record : parser) {
                Product product = new Product();
                product.setId(Long.parseLong(record.get("ID")));
                product.setName(record.get("Name"));
                product.setDescription(record.get("Description"));
                product.setPrice(new BigDecimal(record.get("Price")));
                product.setStock(Integer.parseInt(record.get("Stock")));
                product.setCategory(record.get("Category"));
                product.setActive(Boolean.parseBoolean(record.get("Active")));
                product.setCreatedAt(LocalDateTime.parse(record.get("CreatedAt"), DATE_FORMATTER));
                product.setUpdatedAt(LocalDateTime.parse(record.get("UpdatedAt"), DATE_FORMATTER));
                products.add(product);
            }
            
            logger.info("Importados {} productos desde {}", products.size(), filepath);
        }
        
        return products;
    }

    /**
     * Exporta pedidos a CSV
     */
    public static void exportOrders(List<Order> orders, String filename) throws IOException {
        String filepath = BACKUP_DIR + File.separator + filename;
        
        try (Writer writer = new FileWriter(filepath);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("ID", "UserID", "TotalAmount", "Status", "ShippingAddress", "ItemsCount", "CreatedAt", "UpdatedAt"))) {
            
            for (Order order : orders) {
                printer.printRecord(
                    order.getId(),
                    order.getUserId(),
                    order.getTotalAmount(),
                    order.getStatus().name(),
                    order.getShippingAddress(),
                    order.getItems().size(),
                    order.getCreatedAt().format(DATE_FORMATTER),
                    order.getUpdatedAt().format(DATE_FORMATTER)
                );
            }
            
            logger.info("Exportados {} pedidos a {}", orders.size(), filepath);
        }
    }

    /**
     * Exporta items de pedidos a CSV
     */
    public static void exportOrderItems(List<OrderItem> items, String filename) throws IOException {
        String filepath = BACKUP_DIR + File.separator + filename;
        
        try (Writer writer = new FileWriter(filepath);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("ID", "OrderID", "ProductID", "ProductName", "Quantity", "UnitPrice", "Subtotal", "CreatedAt", "UpdatedAt"))) {
            
            for (OrderItem item : items) {
                printer.printRecord(
                    item.getId(),
                    item.getOrderId(),
                    item.getProductId(),
                    item.getProductName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal(),
                    item.getCreatedAt().format(DATE_FORMATTER),
                    item.getUpdatedAt().format(DATE_FORMATTER)
                );
            }
            
            logger.info("Exportados {} items de pedidos a {}", items.size(), filepath);
        }
    }

    /**
     * Genera un backup completo del sistema
     */
    public static void generateFullBackup(List<User> users, List<Product> products, List<Order> orders) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            exportUsers(users, "users_" + timestamp + ".csv");
            exportProducts(products, "products_" + timestamp + ".csv");
            exportOrders(orders, "orders_" + timestamp + ".csv");
            logger.info("Backup completo generado con timestamp: {}", timestamp);
        } catch (IOException e) {
            logger.error("Error generando backup completo", e);
        }
    }
}
