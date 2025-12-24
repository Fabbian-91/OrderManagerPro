package com.ordermanager.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ordermanager.concurrent.OrderProcessor;
import com.ordermanager.dao.OrderDAO;
import com.ordermanager.dao.OrderDAOImpl;
import com.ordermanager.model.Order;
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
 * Servlet para gestionar pedidos (CRUD) con procesamiento asíncrono
 */
@WebServlet("/api/orders/*")
public class OrderServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(OrderServlet.class);
    private OrderDAO orderDAO;
    private OrderProcessor orderProcessor;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        this.orderDAO = new OrderDAOImpl();
        this.orderProcessor = OrderProcessor.getInstance();
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();
        
        // Iniciar procesador de pedidos
        orderProcessor.start();
        logger.info("OrderServlet inicializado");
    }

    @Override
    public void destroy() {
        // Detener procesador de pedidos
        orderProcessor.stop();
        logger.info("OrderServlet destruido");
        super.destroy();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String userId = req.getParameter("userId");
        String status = req.getParameter("status");
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                List<Order> orders;
                
                if (userId != null) {
                    // GET /api/orders?userId=X
                    orders = orderDAO.findByUserId(Long.parseLong(userId));
                } else if (status != null) {
                    // GET /api/orders?status=X
                    orders = orderDAO.findByStatus(Order.OrderStatus.valueOf(status));
                } else {
                    // GET /api/orders - Listar todos
                    orders = orderDAO.findAll();
                }
                
                sendJsonResponse(resp, HttpServletResponse.SC_OK, orders);
            } else {
                // GET /api/orders/{id} - Obtener pedido por ID
                Long id = Long.parseLong(pathInfo.substring(1));
                Order order = orderDAO.findById(id).orElse(null);
                
                if (order != null) {
                    sendJsonResponse(resp, HttpServletResponse.SC_OK, order);
                } else {
                    sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Pedido no encontrado");
                }
            }
        } catch (SQLException e) {
            logger.error("Error en GET orders", e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error en la base de datos");
        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Parámetro inválido");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Order order = gson.fromJson(req.getReader(), Order.class);
            
            if (!order.validate()) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Datos de pedido inválidos");
                return;
            }
            
            Order created = orderDAO.create(order);
            
            // Encolar pedido para procesamiento asíncrono
            orderProcessor.enqueueOrder(created.getId());
            
            sendJsonResponse(resp, HttpServletResponse.SC_CREATED, created);
            
        } catch (SQLException e) {
            logger.error("Error en POST orders", e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creando pedido");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "ID de pedido requerido");
            return;
        }
        
        try {
            Long id = Long.parseLong(pathInfo.substring(1));
            Order order = gson.fromJson(req.getReader(), Order.class);
            order.setId(id);
            
            boolean updated = orderDAO.update(order);
            if (updated) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, order);
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Pedido no encontrado");
            }
            
        } catch (SQLException e) {
            logger.error("Error en PUT orders", e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error actualizando pedido");
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "ID inválido");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "ID de pedido requerido");
            return;
        }
        
        try {
            Long id = Long.parseLong(pathInfo.substring(1));
            boolean deleted = orderDAO.delete(id);
            
            if (deleted) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, "{\"message\": \"Pedido eliminado\"}");
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Pedido no encontrado");
            }
            
        } catch (SQLException e) {
            logger.error("Error en DELETE orders", e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error eliminando pedido");
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
