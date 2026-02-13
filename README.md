# Order Processing Microservice

A production-ready Spring Boot REST API for managing orders with proper state transitions and validation.

## Architecture Overview

This service follows a clean layered architecture:
- **Controller Layer**: REST endpoints for order operations
- **Service Layer**: Business logic and state management
- **Repository Layer**: Data persistence with JPA
- **Domain Layer**: Core business entities and rules

## Technical Stack

- **Java 17+**
- **Spring Boot 3.2.2**
- **Spring Data JPA**
- **H2 Database** (in-memory, easily switchable to PostgreSQL)
- **Maven** for dependency management
- **JUnit 5 & Mockito** for testing

## Prerequisites

- Java 17 or higher
- Maven 3.6+

## Build Instructions

```bash
# Clone the repository
git clone <repository-url>
cd order-processing-microservice-quantiphi

# Build the project
mvn clean install

# Run tests
mvn test
```

## Run Instructions

```bash
# Start the application
mvn spring-boot:run

# Application will start on http://localhost:8080
```

Alternatively, run the JAR directly:
```bash
java -jar target/order-processing-service-1.0.0.jar
```

## API Endpoints

**Base URL:** `/api/v1`

### 1. Create Order
**POST** `/api/v1/orders`

Creates a new order with items and calculates the total price.

**Request:**
```json
{
  "customerId": "123",
  "items": [
    { "productId": "A1", "quantity": 2, "price": 10.0 },
    { "productId": "B2", "quantity": 1, "price": 20.0 }
  ]
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "customerId": "123",
  "items": [
    { "productId": "A1", "quantity": 2, "price": 10.0, "subtotal": 20.0 },
    { "productId": "B2", "quantity": 1, "price": 20.0, "subtotal": 20.0 }
  ],
  "totalPrice": 40.0,
  "status": "CREATED",
  "createdAt": "2026-02-13T10:30:00",
  "updatedAt": "2026-02-13T10:30:00"
}
```

### 2. Get Order
**GET** `/api/v1/orders/{id}`

Retrieves full order details by ID.

**Response:** `200 OK`
```json
{
  "id": 1,
  "customerId": "123",
  "items": [...],
  "totalPrice": 40.0,
  "status": "CREATED",
  "createdAt": "2026-02-13T10:30:00",
  "updatedAt": "2026-02-13T10:30:00"
}
```

### 3. Update Order Status
**PATCH** `/api/v1/orders/{id}/status`

Updates the order status with validation for allowed transitions.

**Request:**
```json
{
  "status": "PROCESSING"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "status": "PROCESSING",
  ...
}
```

## Order Status State Machine

Valid status transitions:
- `CREATED` → `PROCESSING` or `CANCELLED`
- `PROCESSING` → `COMPLETED` or `CANCELLED`
- `COMPLETED` → (final state, no transitions)
- `CANCELLED` → (final state, no transitions)

Invalid transitions return `400 Bad Request` with an error message.

## Validation Rules

- **Customer ID**: Required, cannot be blank
- **Items**: At least one item required
- **Product ID**: Required for each item
- **Quantity**: Minimum value of 1
- **Price**: Minimum value of 0.01

## Error Handling

The API returns structured error responses:

**404 Not Found:**
```json
{
  "timestamp": "2026-02-13T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Order with ID 999 not found",
  "path": "/api/v1/orders/999"
}
```

**400 Bad Request (Validation):**
```json
{
  "timestamp": "2026-02-13T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid request parameters",
  "path": "/api/v1/orders",
  "validationErrors": {
    "customerId": "Customer ID is required",
    "items": "Order must contain at least one item"
  }
}
```

## Testing

The project includes comprehensive test coverage:

```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report
```

**Test Coverage Includes:**
- Unit tests for domain logic (Order entity, status transitions)
- Service layer tests with mocked dependencies
- Controller layer tests with MockMvc
- Integration tests for complete order lifecycle
- Validation tests for all endpoints

## Docker Deployment

### Build and Run with Docker

```bash
# Build the JAR
mvn clean package -DskipTests

# Build Docker image
docker build -t order-processing-service:1.0.0 .

# Run container
docker run -p 8080:8080 order-processing-service:1.0.0
```

### Using Docker Compose

```bash
# Start the service
docker-compose up -d

# View logs
docker-compose logs -f

# Stop the service
docker-compose down
```

## Database

The application uses H2 in-memory database by default for easy testing.

**H2 Console:** http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:orderdb`
- Username: `sa`
- Password: (empty)

### Switching to PostgreSQL

Update `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/orderdb
    driver-class-name: org.postgresql.Driver
    username: your_username
    password: your_password
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
```

Add PostgreSQL dependency to `pom.xml`:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

## Design Decisions

1. **Immutability & Encapsulation**: Order entities enforce business rules internally, preventing invalid state changes.

2. **State Machine Pattern**: Order status transitions are validated through the enum's `canTransitionTo()` method, ensuring data integrity.

3. **DTO Pattern**: Clear separation between internal domain models and API contracts, allowing independent evolution.

4. **Bidirectional Relationship**: Order-OrderItem relationship uses proper JPA cascading for atomic operations.

5. **BigDecimal for Money**: Ensures precise financial calculations without floating-point errors.

6. **Constructor Injection**: Preferred over field injection for better testability and immutability.

7. **Exception Handling**: Global exception handler provides consistent error responses across all endpoints.

## Recent Enhancements

✅ **Comprehensive Javadoc** - All public APIs documented  
✅ **Structured Logging** - SLF4J logging throughout the application  
✅ **API Versioning** - Endpoints prefixed with `/api/v1`  
✅ **Integration Tests** - Full end-to-end test coverage  
✅ **Docker Support** - Dockerfile and docker-compose included  
✅ **Production Ready** - Clean architecture and best practices

## Production Considerations

- **Logging**: Add SLF4J with Logback for structured logging
- **Monitoring**: Integrate Spring Boot Actuator for health checks and metrics
- **Security**: Add Spring Security for authentication/authorization
- **API Documentation**: Consider Swagger/OpenAPI for interactive API docs
- **Database**: Switch to PostgreSQL or similar for production
- **Containerization**: Dockerfile included for Docker deployment
- **Configuration**: Externalize configuration for different environments

## Project Structure

```
src/
├── main/
│   ├── java/com/quantiphi/orderservice/
│   │   ├── controller/       # REST endpoints
│   │   ├── service/          # Business logic
│   │   ├── repository/       # Data access
│   │   ├── domain/           # Entities & enums
│   │   ├── dto/              # Request/Response objects
│   │   └── exception/        # Exception handling
│   └── resources/
│       └── application.yml   # Configuration
└── test/
    └── java/com/quantiphi/orderservice/
        ├── controller/       # Controller tests
        ├── service/          # Service tests
        └── domain/           # Domain logic tests
```

## Contact

For questions or clarifications, please contact the development team.