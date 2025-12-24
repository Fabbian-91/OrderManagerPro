package com.ordermanager.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Modelo de Producto con validación de datos
 */
public class Product extends BaseEntity {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private boolean active;

    public Product() {
        super();
        this.active = true;
        this.stock = 0;
    }

    public Product(Long id, String name, String description, BigDecimal price, Integer stock, String category) {
        super(id);
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.active = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Verifica si hay stock suficiente
     */
    public boolean hasStock(int quantity) {
        return this.stock >= quantity;
    }

    /**
     * Reduce el stock del producto
     */
    public void reduceStock(int quantity) throws IllegalArgumentException {
        if (!hasStock(quantity)) {
            throw new IllegalArgumentException("Stock insuficiente");
        }
        this.stock -= quantity;
    }

    /**
     * Incrementa el stock del producto
     */
    public void increaseStock(int quantity) {
        this.stock += quantity;
    }

    @Override
    public boolean validate() {
        return name != null && !name.trim().isEmpty()
                && price != null && price.compareTo(BigDecimal.ZERO) >= 0
                && stock != null && stock >= 0
                && category != null && !category.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", category='" + category + '\'' +
                ", active=" + active +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Product product = (Product) o;
        return Objects.equals(name, product.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }
}
