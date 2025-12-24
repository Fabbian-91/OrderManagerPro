-- OrderManagerPro Database Schema
-- PostgreSQL

-- Crear tipos enumerados
CREATE TYPE user_role AS ENUM ('ADMIN', 'CUSTOMER', 'MANAGER');
CREATE TYPE order_status AS ENUM ('PENDING', 'PROCESSING', 'COMPLETED', 'CANCELLED');

-- Tabla de usuarios
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role user_role NOT NULL DEFAULT 'CUSTOMER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de productos
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    category VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de pedidos
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL CHECK (total_amount >= 0),
    status order_status NOT NULL DEFAULT 'PENDING',
    shipping_address TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tabla de items de pedidos
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 2) NOT NULL CHECK (unit_price >= 0),
    subtotal DECIMAL(10, 2) NOT NULL CHECK (subtotal >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Índices para optimizar consultas
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_active ON products(active);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

-- Función para actualizar updated_at automáticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers para actualizar updated_at
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_order_items_updated_at
    BEFORE UPDATE ON order_items
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Datos de ejemplo

-- Usuarios de ejemplo
INSERT INTO users (username, email, password, full_name, role) VALUES
    ('admin', 'admin@ordermanager.com', 'admin123', 'Administrador Sistema', 'ADMIN'),
    ('juan.perez', 'juan.perez@example.com', 'password123', 'Juan Pérez', 'CUSTOMER'),
    ('maria.lopez', 'maria.lopez@example.com', 'password123', 'María López', 'CUSTOMER'),
    ('manager', 'manager@ordermanager.com', 'manager123', 'Manager Sistema', 'MANAGER');

-- Productos de ejemplo
INSERT INTO products (name, description, price, stock, category) VALUES
    ('Laptop Dell XPS 13', 'Laptop ultradelgada con procesador Intel i7', 1299.99, 10, 'Electrónica'),
    ('iPhone 14 Pro', 'Smartphone Apple con cámara de 48MP', 999.99, 25, 'Electrónica'),
    ('Monitor LG 27"', 'Monitor 4K UHD con HDR', 399.99, 15, 'Electrónica'),
    ('Teclado Mecánico Logitech', 'Teclado gaming RGB', 129.99, 30, 'Accesorios'),
    ('Mouse Inalámbrico', 'Mouse ergonómico con batería recargable', 49.99, 50, 'Accesorios'),
    ('Auriculares Sony WH-1000XM5', 'Auriculares con cancelación de ruido', 349.99, 20, 'Audio'),
    ('Webcam Logitech HD', 'Cámara web 1080p para videoconferencias', 79.99, 35, 'Accesorios'),
    ('Disco SSD Samsung 1TB', 'Unidad de estado sólido NVMe', 149.99, 40, 'Almacenamiento');

-- Pedidos de ejemplo (se crean sin items aquí, deben agregarse vía API)
INSERT INTO orders (user_id, total_amount, status, shipping_address) VALUES
    (2, 0, 'PENDING', 'Calle Principal 123, Madrid, España'),
    (3, 0, 'PENDING', 'Avenida Central 456, Barcelona, España');
