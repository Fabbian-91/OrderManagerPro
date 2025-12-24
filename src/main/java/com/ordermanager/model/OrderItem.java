package com.ordermanager.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Clase que representa un item dentro de un pedido
 */
public class OrderItem extends BaseEntity {
    private Long orderId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public OrderItem() {
        super();
        this.quantity = 0;
        this.unitPrice = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
    }

    public OrderItem(Long id, Long orderId, Long productId, String productName, Integer quantity, BigDecimal unitPrice) {
        super(id);
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        calculateSubtotal();
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        calculateSubtotal();
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    private void calculateSubtotal() {
        if (quantity != null && unitPrice != null) {
            this.subtotal = unitPrice.multiply(new BigDecimal(quantity));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }

    @Override
    public boolean validate() {
        return productId != null
                && quantity != null && quantity > 0
                && unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) >= 0;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", subtotal=" + subtotal +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(orderId, orderItem.orderId) && Objects.equals(productId, orderItem.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), orderId, productId);
    }
}
