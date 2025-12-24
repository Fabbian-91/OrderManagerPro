# OrderManagerPro

Backend en Java para gestión de usuarios, productos y pedidos con características avanzadas. Proyecto ideal como portafolio para Java intermedio/avanzado.

## 🚀 Características Principales

### POO Avanzada
- **Herencia**: Clase base `BaseEntity` con propiedades comunes
- **Polimorfismo**: Métodos abstractos y sobreescritura
- **Encapsulación**: Validación en setters, getters controlados
- **Interfaces**: DAOs genéricos con interfaces reutilizables

### Colecciones y Genéricos
- **Interfaces Genéricas**: `GenericDAO<T>` para operaciones CRUD
- **Collections Framework**: Uso de `List`, `ArrayList`, `Map`
- **Inmutabilidad**: `Collections.unmodifiableList()` para proteger datos
- **Streams API**: Operaciones funcionales con streams

### Concurrencia
- **ExecutorService**: Pool de threads para procesamiento paralelo
- **BlockingQueue**: Cola thread-safe para pedidos pendientes
- **Background Processing**: Procesamiento asíncrono de pedidos
- **Scheduler**: Worker periódico para detectar pedidos pendientes

### I/O de Archivos
- **CSV Export/Import**: Apache Commons CSV para backups
- **File Handling**: Manejo robusto de archivos con try-with-resources
- **Backup System**: Sistema completo de respaldo de datos

### JDBC y PostgreSQL
- **Transacciones**: Gestión de transacciones para operaciones complejas
- **PreparedStatements**: Prevención de SQL Injection
- **Connection Pooling**: Gestión eficiente de conexiones
- **Tipos Personalizados**: ENUM types en PostgreSQL

### Servlets y API REST
- **CRUD Completo**: Endpoints para todas las entidades
- **JSON Serialization**: Gson para manejo de JSON
- **CORS Support**: Filtro para permitir peticiones cross-origin
- **RESTful Design**: Diseño de API REST-like

## 📋 Arquitectura del Proyecto

```
OrderManagerPro/
├── src/main/java/com/ordermanager/
│   ├── model/              # Entidades del dominio (POO)
│   │   ├── BaseEntity.java
│   │   ├── User.java
│   │   ├── Product.java
│   │   ├── Order.java
│   │   └── OrderItem.java
│   ├── dao/                # Data Access Objects (Genéricos)
│   │   ├── GenericDAO.java
│   │   ├── UserDAO.java
│   │   ├── ProductDAO.java
│   │   ├── OrderDAO.java
│   │   └── *DAOImpl.java
│   ├── servlet/            # Servlets (API REST)
│   │   ├── UserServlet.java
│   │   ├── ProductServlet.java
│   │   ├── OrderServlet.java
│   │   ├── ExportServlet.java
│   │   └── CorsFilter.java
│   ├── concurrent/         # Procesamiento concurrente
│   │   └── OrderProcessor.java
│   └── util/              # Utilidades
│       ├── DatabaseConnection.java
│       └── CSVHandler.java
├── src/main/resources/
│   ├── database.properties # Configuración de BD
│   └── schema.sql         # Schema de PostgreSQL
└── src/main/webapp/
    ├── WEB-INF/web.xml    # Descriptor de web app
    └── index.html         # Página de documentación

```

## 🛠️ Tecnologías Utilizadas

- **Java 11**: Lenguaje base
- **Servlets 4.0**: API de servlets
- **JDBC**: Acceso a base de datos
- **PostgreSQL 42.6.0**: Base de datos
- **Gson 2.10.1**: Serialización JSON
- **Apache Commons CSV 1.10.0**: Manejo de CSV
- **SLF4J 2.0.9**: Logging
- **Maven**: Gestión de dependencias

## 📦 Instalación y Configuración

### Requisitos Previos
- JDK 11 o superior
- Maven 3.6+
- PostgreSQL 12+
- Servidor de aplicaciones (Tomcat 9+, Jetty, etc.)

### Paso 1: Clonar el Repositorio
```bash
git clone https://github.com/Fabbian-91/OrderManagerPro.git
cd OrderManagerPro
```

### Paso 2: Configurar PostgreSQL
```bash
# Crear base de datos
createdb ordermanager

# O desde psql:
psql -U postgres
CREATE DATABASE ordermanager;
\q
```

### Paso 3: Ejecutar Schema SQL
```bash
psql -U postgres -d ordermanager -f src/main/resources/schema.sql
```

### Paso 4: Configurar Credenciales
Editar `src/main/resources/database.properties`:
```properties
db.url=jdbc:postgresql://localhost:5432/ordermanager
db.username=postgres
db.password=tu_password
```

### Paso 5: Compilar el Proyecto
```bash
mvn clean package
```

### Paso 6: Desplegar en Tomcat
```bash
# Copiar el WAR generado a Tomcat
cp target/OrderManagerPro.war $CATALINA_HOME/webapps/

# Iniciar Tomcat
$CATALINA_HOME/bin/startup.sh
```

### Paso 7: Acceder a la Aplicación
Abrir navegador en: `http://localhost:8080/OrderManagerPro/`

## 🔌 API Endpoints

### Usuarios
- `GET /api/users` - Listar todos los usuarios
- `GET /api/users/{id}` - Obtener usuario por ID
- `POST /api/users` - Crear usuario
- `PUT /api/users/{id}` - Actualizar usuario
- `DELETE /api/users/{id}` - Eliminar usuario

### Productos
- `GET /api/products` - Listar todos los productos
- `GET /api/products?category={cat}` - Filtrar por categoría
- `GET /api/products?active=true` - Solo productos activos
- `GET /api/products/{id}` - Obtener producto por ID
- `POST /api/products` - Crear producto
- `PUT /api/products/{id}` - Actualizar producto
- `DELETE /api/products/{id}` - Eliminar producto

### Pedidos
- `GET /api/orders` - Listar todos los pedidos
- `GET /api/orders?userId={id}` - Pedidos de un usuario
- `GET /api/orders?status={status}` - Filtrar por estado
- `GET /api/orders/{id}` - Obtener pedido por ID
- `POST /api/orders` - Crear pedido (procesamiento asíncrono)
- `PUT /api/orders/{id}` - Actualizar pedido
- `DELETE /api/orders/{id}` - Eliminar pedido

### Exportación
- `GET /api/export/users` - Exportar usuarios a CSV
- `GET /api/export/products` - Exportar productos a CSV
- `GET /api/export/orders` - Exportar pedidos a CSV
- `GET /api/export/all` - Backup completo

## 📝 Ejemplos de Uso

### Crear un Usuario
```bash
curl -X POST http://localhost:8080/OrderManagerPro/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test.user",
    "email": "test@example.com",
    "password": "password123",
    "fullName": "Test User",
    "role": "CUSTOMER"
  }'
```

### Crear un Producto
```bash
curl -X POST http://localhost:8080/OrderManagerPro/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop HP",
    "description": "Laptop para trabajo",
    "price": 899.99,
    "stock": 15,
    "category": "Electrónica"
  }'
```

### Crear un Pedido
```bash
curl -X POST http://localhost:8080/OrderManagerPro/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "shippingAddress": "Calle Principal 123",
    "items": [
      {
        "productId": 1,
        "productName": "Laptop Dell",
        "quantity": 1,
        "unitPrice": 1299.99
      }
    ]
  }'
```

## 🎯 Características Técnicas Destacadas

### 1. Transacciones ACID
Los pedidos se crean en una transacción que incluye:
- Inserción del pedido
- Inserción de items
- Actualización de stock de productos
- Rollback automático en caso de error

### 2. Procesamiento Asíncrono
Los pedidos nuevos se procesan en segundo plano:
- Worker threads procesan pedidos de una cola
- Cambios de estado: PENDING → PROCESSING → COMPLETED
- Scheduler busca pedidos pendientes cada 10 segundos

### 3. Validación de Datos
Todas las entidades implementan validación:
- Email válido con @
- Passwords mínimo 6 caracteres
- Precios no negativos
- Stock no negativo

### 4. Patrón DAO Genérico
Interface genérica reutilizable:
```java
public interface GenericDAO<T> {
    T create(T entity) throws SQLException;
    Optional<T> findById(Long id) throws SQLException;
    List<T> findAll() throws SQLException;
    boolean update(T entity) throws SQLException;
    boolean delete(Long id) throws SQLException;
}
```

### 5. Backup CSV Automático
Sistema de respaldo con:
- Export de todas las tablas
- Timestamps automáticos
- Formato CSV estándar

## 🔒 Seguridad

- PreparedStatements para prevenir SQL Injection
- Validación de entrada en todas las operaciones
- Transacciones para integridad de datos
- CORS configurado (puede restringirse en producción)

## 📊 Base de Datos

### Modelo de Datos
- **users**: Usuarios del sistema
- **products**: Catálogo de productos
- **orders**: Pedidos realizados
- **order_items**: Items de cada pedido

### Tipos Enumerados
- `user_role`: ADMIN, CUSTOMER, MANAGER
- `order_status`: PENDING, PROCESSING, COMPLETED, CANCELLED

## 🧪 Testing Manual

### Verificar Usuarios
```bash
curl http://localhost:8080/OrderManagerPro/api/users
```

### Verificar Productos
```bash
curl http://localhost:8080/OrderManagerPro/api/products
```

### Generar Backup
```bash
curl http://localhost:8080/OrderManagerPro/api/export/all
```

## 📈 Mejoras Futuras

- [ ] Autenticación JWT
- [ ] Tests unitarios con JUnit
- [ ] Paginación en endpoints
- [ ] Documentación OpenAPI/Swagger
- [ ] Caché con Redis
- [ ] Métricas y monitoreo

## 👨‍💻 Autor

Proyecto desarrollado como portafolio de Java intermedio/avanzado.

## 📄 Licencia

Este proyecto es de código abierto y está disponible para uso educativo.
