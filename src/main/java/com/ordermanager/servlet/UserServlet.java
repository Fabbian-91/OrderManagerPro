package com.ordermanager.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ordermanager.dao.UserDAO;
import com.ordermanager.dao.UserDAOImpl;
import com.ordermanager.model.User;
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
 * Servlet para gestionar usuarios (CRUD)
 */
@WebServlet("/api/users/*")
public class UserServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(UserServlet.class);
    private UserDAO userDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        this.userDAO = new UserDAOImpl();
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();
        logger.info("UserServlet inicializado");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/users - Listar todos los usuarios
                List<User> users = userDAO.findAll();
                sendJsonResponse(resp, HttpServletResponse.SC_OK, users);
            } else {
                // GET /api/users/{id} - Obtener usuario por ID
                Long id = Long.parseLong(pathInfo.substring(1));
                User user = userDAO.findById(id).orElse(null);
                
                if (user != null) {
                    sendJsonResponse(resp, HttpServletResponse.SC_OK, user);
                } else {
                    sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Usuario no encontrado");
                }
            }
        } catch (SQLException e) {
            logger.error("Error en GET users", e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error en la base de datos");
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "ID inválido");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            User user = gson.fromJson(req.getReader(), User.class);
            
            if (!user.validate()) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Datos de usuario inválidos");
                return;
            }
            
            User created = userDAO.create(user);
            sendJsonResponse(resp, HttpServletResponse.SC_CREATED, created);
            
        } catch (SQLException e) {
            logger.error("Error en POST users", e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creando usuario");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "ID de usuario requerido");
            return;
        }
        
        try {
            Long id = Long.parseLong(pathInfo.substring(1));
            User user = gson.fromJson(req.getReader(), User.class);
            user.setId(id);
            
            if (!user.validate()) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Datos de usuario inválidos");
                return;
            }
            
            boolean updated = userDAO.update(user);
            if (updated) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, user);
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Usuario no encontrado");
            }
            
        } catch (SQLException e) {
            logger.error("Error en PUT users", e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error actualizando usuario");
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "ID inválido");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "ID de usuario requerido");
            return;
        }
        
        try {
            Long id = Long.parseLong(pathInfo.substring(1));
            boolean deleted = userDAO.delete(id);
            
            if (deleted) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, "{\"message\": \"Usuario eliminado\"}");
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Usuario no encontrado");
            }
            
        } catch (SQLException e) {
            logger.error("Error en DELETE users", e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error eliminando usuario");
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
