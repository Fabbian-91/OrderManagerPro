package com.ordermanager.servlet;

import com.ordermanager.dao.OrderDAO;
import com.ordermanager.dao.OrderDAOImpl;
import com.ordermanager.dao.ProductDAO;
import com.ordermanager.dao.ProductDAOImpl;
import com.ordermanager.dao.UserDAO;
import com.ordermanager.dao.UserDAOImpl;
import com.ordermanager.model.Order;
import com.ordermanager.model.Product;
import com.ordermanager.model.User;
import com.ordermanager.util.CSVHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Servlet para exportar datos a CSV (backup)
 */
@WebServlet("/api/export/*")
public class ExportServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ExportServlet.class);
    private UserDAO userDAO;
    private ProductDAO productDAO;
    private OrderDAO orderDAO;

    @Override
    public void init() throws ServletException {
        this.userDAO = new UserDAOImpl();
        this.productDAO = new ProductDAOImpl();
        this.orderDAO = new OrderDAOImpl();
        logger.info("ExportServlet inicializado");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Especifique tipo de exportación");
                return;
            }
            
            String exportType = pathInfo.substring(1);
            
            switch (exportType) {
                case "users":
                    exportUsers(resp);
                    break;
                case "products":
                    exportProducts(resp);
                    break;
                case "orders":
                    exportOrders(resp);
                    break;
                case "all":
                    exportAll(resp);
                    break;
                default:
                    sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Tipo de exportación inválido");
            }
            
        } catch (SQLException e) {
            logger.error("Error en exportación", e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error exportando datos");
        }
    }

    private void exportUsers(HttpServletResponse resp) throws SQLException, IOException {
        List<User> users = userDAO.findAll();
        String filename = "users_export.csv";
        CSVHandler.exportUsers(users, filename);
        sendJsonResponse(resp, "{\"message\": \"Usuarios exportados\", \"filename\": \"" + filename + "\", \"count\": " + users.size() + "}");
    }

    private void exportProducts(HttpServletResponse resp) throws SQLException, IOException {
        List<Product> products = productDAO.findAll();
        String filename = "products_export.csv";
        CSVHandler.exportProducts(products, filename);
        sendJsonResponse(resp, "{\"message\": \"Productos exportados\", \"filename\": \"" + filename + "\", \"count\": " + products.size() + "}");
    }

    private void exportOrders(HttpServletResponse resp) throws SQLException, IOException {
        List<Order> orders = orderDAO.findAll();
        String filename = "orders_export.csv";
        CSVHandler.exportOrders(orders, filename);
        sendJsonResponse(resp, "{\"message\": \"Pedidos exportados\", \"filename\": \"" + filename + "\", \"count\": " + orders.size() + "}");
    }

    private void exportAll(HttpServletResponse resp) throws SQLException, IOException {
        List<User> users = userDAO.findAll();
        List<Product> products = productDAO.findAll();
        List<Order> orders = orderDAO.findAll();
        
        CSVHandler.generateFullBackup(users, products, orders);
        
        int totalRecords = users.size() + products.size() + orders.size();
        sendJsonResponse(resp, "{\"message\": \"Backup completo generado\", \"totalRecords\": " + totalRecords + "}");
    }

    private void sendJsonResponse(HttpServletResponse resp, String json) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(json);
    }

    private void sendErrorResponse(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(status);
        resp.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}
