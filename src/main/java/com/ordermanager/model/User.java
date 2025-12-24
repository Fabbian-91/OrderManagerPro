package com.ordermanager.model;

import java.util.Objects;

/**
 * Modelo de Usuario con encapsulación y validación
 */
public class User extends BaseEntity {
    private String username;
    private String email;
    private String password;
    private String fullName;
    private UserRole role;

    public enum UserRole {
        ADMIN, CUSTOMER, MANAGER
    }

    public User() {
        super();
        this.role = UserRole.CUSTOMER;
    }

    public User(Long id, String username, String email, String password, String fullName, UserRole role) {
        super(id);
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    @Override
    public boolean validate() {
        return username != null && !username.trim().isEmpty()
                && email != null && email.contains("@")
                && password != null && password.length() >= 6
                && fullName != null && !fullName.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role=" + role +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        User user = (User) o;
        return Objects.equals(username, user.username) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), username, email);
    }
}
