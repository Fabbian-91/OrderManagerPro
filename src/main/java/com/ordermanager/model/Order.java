package com.ordermanager.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Modelo de Pedido con uso de colecciones y genéricos
 */
public class Order extends BaseEntity {
    private Long userId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String shippingAddress;

    public enum OrderStatus {
        PENDING, PROCESSING, COMPLETED, CANCELLED
    }

    public Order() {
        super();
        this.items = new ArrayList<>();
        this.totalAmount = BigDecimal.ZERO;
        this.status = OrderStatus.PENDING;
    }

    public Order(Long id, Long userId, String shippingAddress) {
        super(id);
        this.userId = userId;
        this.shippingAddress = shippingAddress;
        this.items = new ArrayList<>();
        this.totalAmount = BigDecimal.ZERO;
        this.status = OrderStatus.PENDING;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void setItems(List<OrderItem> items) {
        this.items = new ArrayList<>(items);
        calculateTotal();
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
        calculateTotal();
    }

    public void removeItem(OrderItem item) {
        this.items.remove(item);
        calculateTotal();
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    /**
     * Calcula el total del pedido basado en los items
     */
    private void calculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Procesa el pedido cambiando su estado
     */
    public void process() {
        if (this.status == OrderStatus.PENDING) {
            this.status = OrderStatus.PROCESSING;
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Completa el pedido
     */
    public void complete() {
        if (this.status == OrderStatus.PROCESSING) {
            this.status = OrderStatus.COMPLETED;
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Cancela el pedido
     */
    public void cancel() {
        if (this.status != OrderStatus.COMPLETED) {
            this.status = OrderStatus.CANCELLED;
            this.updatedAt = LocalDateTime.now();
        }
    }

    @Override
    public boolean validate() {
        return userId != null
                && items != null && !items.isEmpty()
                && shippingAddress != null && !shippingAddress.trim().isEmpty()
                && items.stream().allMatch(OrderItem::validate);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", userId=" + userId +
                ", itemsCount=" + items.size() +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", shippingAddress='" + shippingAddress + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Order order = (Order) o;
        return Objects.equals(userId, order.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userId);
    }
}
