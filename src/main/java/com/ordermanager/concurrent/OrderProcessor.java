package com.ordermanager.concurrent;

import com.ordermanager.dao.OrderDAO;
import com.ordermanager.dao.OrderDAOImpl;
import com.ordermanager.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.*;

/**
 * Procesador de pedidos en segundo plano usando concurrencia
 * Implementa el patrón Producer-Consumer con ExecutorService
 */
public class OrderProcessor {
    private static final Logger logger = LoggerFactory.getLogger(OrderProcessor.class);
    private static OrderProcessor instance;
    
    private final ExecutorService executorService;
    private final BlockingQueue<Long> orderQueue;
    private final OrderDAO orderDAO;
    private volatile boolean running;

    private OrderProcessor() {
        this.executorService = Executors.newFixedThreadPool(3);
        this.orderQueue = new LinkedBlockingQueue<>(100);
        this.orderDAO = new OrderDAOImpl();
        this.running = false;
    }

    public static synchronized OrderProcessor getInstance() {
        if (instance == null) {
            instance = new OrderProcessor();
        }
        return instance;
    }

    /**
     * Inicia el procesamiento de pedidos en segundo plano
     */
    public void start() {
        if (running) {
            logger.warn("El procesador de pedidos ya está en ejecución");
            return;
        }
        
        running = true;
        logger.info("Iniciando procesador de pedidos...");
        
        // Iniciar workers
        for (int i = 0; i < 3; i++) {
            executorService.submit(new OrderWorker(i));
        }
        
        // Iniciar scheduler para buscar pedidos pendientes
        executorService.submit(new OrderScheduler());
    }

    /**
     * Detiene el procesamiento de pedidos
     */
    public void stop() {
        running = false;
        logger.info("Deteniendo procesador de pedidos...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Encola un pedido para procesamiento
     */
    public boolean enqueueOrder(Long orderId) {
        try {
            orderQueue.offer(orderId, 5, TimeUnit.SECONDS);
            logger.info("Pedido {} encolado para procesamiento", orderId);
            return true;
        } catch (InterruptedException e) {
            logger.error("Error encolando pedido {}", orderId, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Worker que procesa pedidos de la cola
     */
    private class OrderWorker implements Runnable {
        private final int workerId;

        public OrderWorker(int workerId) {
            this.workerId = workerId;
        }

        @Override
        public void run() {
            logger.info("Worker {} iniciado", workerId);
            
            while (running) {
                try {
                    Long orderId = orderQueue.poll(1, TimeUnit.SECONDS);
                    if (orderId != null) {
                        processOrder(orderId);
                    }
                } catch (InterruptedException e) {
                    logger.warn("Worker {} interrumpido", workerId);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            logger.info("Worker {} detenido", workerId);
        }

        private void processOrder(Long orderId) {
            logger.info("Worker {} procesando pedido {}", workerId, orderId);
            
            try {
                // Simular procesamiento (validación, pago, preparación)
                Thread.sleep(2000);
                
                // Obtener el pedido
                Order order = orderDAO.findById(orderId).orElse(null);
                if (order == null) {
                    logger.error("Pedido {} no encontrado", orderId);
                    return;
                }
                
                // Procesar según el estado
                if (order.getStatus() == Order.OrderStatus.PENDING) {
                    order.process();
                    orderDAO.update(order);
                    logger.info("Worker {} cambió pedido {} a PROCESSING", workerId, orderId);
                    
                    // Simular preparación
                    Thread.sleep(3000);
                    
                    // Completar pedido
                    order.complete();
                    orderDAO.update(order);
                    logger.info("Worker {} completó pedido {}", workerId, orderId);
                }
                
            } catch (InterruptedException e) {
                logger.error("Worker {} interrumpido procesando pedido {}", workerId, orderId);
                Thread.currentThread().interrupt();
            } catch (SQLException e) {
                logger.error("Worker {} error SQL procesando pedido {}", workerId, orderId, e);
            }
        }
    }

    /**
     * Scheduler que busca pedidos pendientes periódicamente
     */
    private class OrderScheduler implements Runnable {
        @Override
        public void run() {
            logger.info("Scheduler de pedidos iniciado");
            
            while (running) {
                try {
                    // Buscar pedidos pendientes cada 10 segundos
                    Thread.sleep(10000);
                    
                    List<Order> pendingOrders = orderDAO.findPendingOrders();
                    logger.info("Scheduler encontró {} pedidos pendientes", pendingOrders.size());
                    
                    for (Order order : pendingOrders) {
                        enqueueOrder(order.getId());
                    }
                    
                } catch (InterruptedException e) {
                    logger.warn("Scheduler interrumpido");
                    Thread.currentThread().interrupt();
                    break;
                } catch (SQLException e) {
                    logger.error("Error en scheduler buscando pedidos pendientes", e);
                }
            }
            
            logger.info("Scheduler de pedidos detenido");
        }
    }

    /**
     * Obtiene el estado del procesador
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Obtiene el tamaño de la cola
     */
    public int getQueueSize() {
        return orderQueue.size();
    }
}
