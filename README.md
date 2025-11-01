# 🧩 Dynamic REST Endpoints (Spring Boot)

This project provides an infrastructure for **dynamic REST endpoints** in Spring Boot.  
The goal is to automatically handle CRUD operations without writing separate controllers for each entity.

---

## 🚀 Features

- 🧠 **Dynamic Endpoint Discovery**
    - On application startup, all `JpaRepository` beans in the Spring context are scanned.
    - A dynamic endpoint is created for each entity.
    - Example:
        - `Order Entity` → `/orders`
        - `Product Entity` → `/products`

- ⚙️ **Dynamic Handler Selection**
    - Different `IHandler` implementations are invoked based on the HTTP method.
    - Example:
        - `GET` → `GetHandler`
        - `POST` → `PostHandler`
        - `PUT` → `PutHandler`
        - `DELETE` → `DeleteHandler`

- 🧾 **DTO and Request Mapping**
    - Base packages for DTOs and Request classes are read from `application.properties`.
    - Classes like `GetOrderRequest`, `CreateOrderRequest`, `UpdateOrderRequest` are automatically loaded for each HTTP verb (`Class.forName`).
    - Reflection is used to map entity ↔ DTO dynamically.

- ✅ **Validation Support**
    - Annotations like `@NotNull` and `@Size` are automatically checked.
    - Meaningful error messages are returned for invalid requests.

---

## 🔗 Example Endpoints

| HTTP Method | URL               | Description                            |
|-------------|------------------|----------------------------------------|
| `GET`       | `/orders?page=0&pageSize=20` | Retrieves paginated records             |
| `GET`       | `/orders/1`       | Retrieves a specific record by ID       |
| `POST`      | `/orders`         | Creates a new record                    |
| `PUT`       | `/orders/1`       | Updates an existing record              |
| `DELETE`    | `/orders/1`       | Deletes a record                        |

---

## ⚙️ Configuration

Example `application.properties`:

```properties
spring.application.name=demo

spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true

dynamic.endpoint.request.base-package=com.example.demo.request
dynamic.endpoint.dto.base-package=com.example.demo.dto
```

## 💡 Usage
* **Configure** application.properties with the paths to your DTOs and request classes.
 ```properties
    dynamic.endpoint.request.base-package=com.example.demo.request
    dynamic.endpoint.dto.base-package=com.example.demo.dto
 ```
* **Create** the Request and DTO classes that will be used by the endpoints.
  Classes like `CreateOrderRequest`, `CreateOrderDto`, and `UpdateOrderRequest` are automatically loaded for their corresponding HTTP verbs (using Class.forName).


*  **Define** Entity and Repository:
```java
@Entity
public class Order { /* fields */ }

public interface OrderRepository extends JpaRepository<Order, Long> {}
```
* **Run:** Upon startup, the framework scans the Spring Context for all JpaRepository beans. It identifies the entity type and instantly registers the corresponding REST endpoints.