package com.ordermanager.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ordermanager.dao.ProductDAO;
import com.ordermanager.dao.ProductDAOImpl;
import com.ordermanager.model.Product;
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
 * Servlet para gestionar productos (CRUD)
 */
@WebServlet("/api/products/*")
public class ProductServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ProductServlet.class);
    private ProductDAO productDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        this.productDAO = new ProductDAOImpl();
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();
        logger.info("ProductServlet inicializado");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String category = req.getParameter("category");
        String active = req.getParameter("active");
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                List<Product> products;
                
                if (category != null) {
                    // GET /api/products?category=X
                    products = productDAO.findByCategory(category);
                } else if ("true".equals(active)) {
                    // GET /api/products?active=true
                    products = productDAO.findActive();
                } else {
                    // GET /api/products - Listar todos
                    products = productDAO.findAll();
                }
                
                sendJsonResponse(resp, HttpServletResponse.SC_OK, products);
            } else {
                // GET /api/products/{id} - Obtener producto por ID
                Long id = Long.parseLong(pathInfo.substring(1));
                Product product = productDAO.findById(id).orElse(null);
                
                if (product != null) {
                    sendJsonResponse(resp, HttpServletResponse.SC_OK, product);
                } else {
                    sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Producto no encontrado");
                }
            }
        } catch (SQLException e) {
            logger.error("Error en GET products", e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error en la base de datos");
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "ID inválido");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Product product = gson.fromJson(req.getReader(), Product.class);
            
            if (!product.validate()) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Datos de producto inválidos");
                return;
            }
            
            Product created = productDAO.create(product);
            sendJsonResponse(resp, HttpServletResponse.SC_CREATED, created);
            
        } catch (SQLException e) {
            logger.error("Error en POST products", e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creando producto");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "ID de producto requerido");
            return;
        }
        
        try {
            Long id = Long.parseLong(pathInfo.substring(1));
            Product product = gson.fromJson(req.getReader(), Product.class);
            product.setId(id);
            
            if (!product.validate()) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Datos de producto inválidos");
                return;
            }
            
            boolean updated = productDAO.update(product);
            if (updated) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, product);
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Producto no encontrado");
            }
            
        } catch (SQLException e) {
            logger.error("Error en PUT products", e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error actualizando producto");
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "ID inválido");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "ID de producto requerido");
            return;
        }
        
        try {
            Long id = Long.parseLong(pathInfo.substring(1));
            boolean deleted = productDAO.delete(id);
            
            if (deleted) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, "{\"message\": \"Producto eliminado\"}");
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Producto no encontrado");
            }
            
        } catch (SQLException e) {
            logger.error("Error en DELETE products", e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error eliminando producto");
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "ID inválido");
        }
    }

    private void sendJsonResponse(HttpServletResponse resp, int status, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(status);
        resp.getWriter().write(gson.toJson(data));
    }

    private void sendErrorResponse(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(status);
        resp.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}
