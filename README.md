# phasezero-catalog-service

## Project Overview

`phasezero-catalog-service` is a small Spring Boot microservice that manages a product catalogue via REST APIs. The service demonstrates backend fundamentals (Java + Spring Boot), REST API design, data modelling, validation, and clean code structure.

### Main features

* Add product (with validations and business rules)
* List all products
* Search products by name (case-insensitive, contains)
* Filter products by category
* Sort products by price (ascending)
* Compute total inventory value (sum of price * stock)

---

## Technology Stack

* Java 17
* Spring Boot (Spring Web, Spring Validation)
* Build tool: Maven
* Storage: In-memory repository (Map). Optionally pluggable to H2 (instructions below).

---

## Design & Code Structure

The project follows a standard layered architecture:

* `controller` layer: REST controllers handling HTTP requests and mapping to services.
* `service` layer: Business logic, normalization, validations, and coordination.
* `repository` layer: Data storage access. Implemented as an in-memory repository (backed by a `ConcurrentHashMap`).
* `model` / `dto`: Domain model (`Product`) and request/response DTOs (if used).
* `exception` package: Custom exceptions and a global exception handler to produce consistent error responses.

**Main classes (examples)**

* `com.phasezero_catalog_service.demo.controller.ProductController`
* `com.phasezero_catalog_service.demo.service.ProductService`
* `com.phasezero_catalog_service.demo.repository.InMemoryProductRepository`
* `com.phasezero_catalog_service.demo.model.Product`
* `com.phasezero_catalog_service.demo.exception.GlobalExceptionHandler`

**Notes on additional fields**

* `id` (UUID): internal unique identifier (not the business key). Optional, useful for internal references.
* `createdAt` (ISO timestamp): for bookkeeping. Both are included but are not used as business keys — `partNumber` remains the unique business key.

---

## Business Rules & Validations

When creating or updating a product the service enforces:

* `partNumber` is the business key and **must be unique**. Attempting to create a product with an existing `partNumber` results in HTTP `409 Conflict`.
* `partName` is normalized to **lowercase** before saving (stored value is lowercased).
* `price` and `stock` **cannot be negative**. Negative values return HTTP `400 Bad Request`.
* Required fields (`partNumber`, `partName`, `category`, `price`, `stock`) must be present. Missing/empty required fields return HTTP `400 Bad Request`.

All error responses follow a consistent JSON structure:

```json
{
  "timestamp": "2025-12-11T02:36:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "price cannot be negative",
  "path": "/products"
}
```

---

## Endpoints

> Base path: `/products`

1. **Add new product**

   * **POST** `/products`
   * **Request body (JSON)**

     ```json
     {
       "partNumber": "PN-1001",
       "partName": "Hydraulic Filter",
       "category": "filters",
       "price": 49.99,
       "stock": 120
     }
     ```
   * **Success response:** `201 Created` with saved product JSON (including optional `id` and `createdAt`).
   * **Errors:** `400 Bad Request` (validation errors), `409 Conflict` (duplicate `partNumber`).

2. **List all products**

   * **GET** `/products`
   * **Response:** `200 OK`, JSON array of product objects.

3. **Search by name (contains, case-insensitive)**

   * **GET** `/products/search?name=<text>`
   * Example: `/products/search?name=filter` matches `hydraulic filter`, `FILTER`, etc.
   * **Response:** `200 OK`, JSON array of matched products (may be empty).

4. **Filter by category**

   * **GET** `/products/filter?category=<category>` or **GET** `/products/category/{category}` (either option may be implemented)
   * Example: `/products/filter?category=filters`
   * **Response:** `200 OK`, JSON array of products in that category.

5. **Sort products by price (ascending)**

   * **GET** `/products/sort?by=price&order=asc` or simpler: `/products/sort/price` (implementation detail)
   * **Response:** `200 OK`, JSON array sorted by price ascending.

6. **Total inventory value**

   * **GET** `/products/inventory/value`
   * Computes `sum(price * stock)` for all products.
   * **Response:** `200 OK`

     ```json
     { "inventoryValue": 12345.67 }
     ```

---

## Example Requests & Responses

### 1) Add product — success

**Request**

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{"partNumber":"PN-1001","partName":"Hydraulic Filter","category":"filters","price":49.99,"stock":120}'
```

**Response** `201 Created`

```json
{
  "id": "5f47a2e3-8a8c-4a2f-b2a1-1c9e9c1e8b3d",
  "partNumber": "PN-1001",
  "partName": "hydraulic filter",
  "category": "filters",
  "price": 49.99,
  "stock": 120,
  "createdAt": "2025-12-11T02:36:00Z"
}
```

### 2) Add product — duplicate partNumber (error)

**Request** (same `partNumber`)

**Response** `409 Conflict`

```json
{
  "timestamp": "2025-12-11T02:36:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Product with partNumber 'PN-1001' already exists",
  "path": "/products"
}
```

### 3) Search by name — example

**Request**

```
GET /products/search?name=filter
```

**Response** `200 OK`

```json
[
  {
    "partNumber": "PN-1001",
    "partName": "hydraulic filter",
    "category": "filters",
    "price": 49.99,
    "stock": 120
  },
  {
    "partNumber": "PN-1003",
    "partName": "oil filter",
    "category": "filters",
    "price": 19.99,
    "stock": 250
  }
]
```

### 4) Inventory value — example

**Request**

```
GET /products/inventory/value
```

**Response** `200 OK`

```json
{ "inventoryValue": 24998.5 }
```

---

## Running the application

### Prerequisites

* Java 17 installed and `JAVA_HOME` set.
* Maven installed (or use the included wrapper `mvnw`).

### Build & Run

```bash
# From project root
mvn clean package
java -jar target/phasezero-catalog-service-0.0.1-SNAPSHOT.jar
```

Or using Spring Boot plugin:

```bash
mvn spring-boot:run
```

The application starts on `http://localhost:8080` by default.

---

## Switching to H2 (optional)

If you prefer using an in-memory H2 DB instead of the in-memory `Map` repository, the project can be adapted by:

1. Add `spring-boot-starter-data-jpa` and `com.h2database:h2` to `pom.xml`.
2. Create a `Product` JPA entity and `JpaRepository<Product, UUID>`.
3. Configure `spring.datasource.url=jdbc:h2:mem:phasezero` and `spring.jpa.hibernate.ddl-auto=update` (or `create`).

*Why H2?* — H2 provides SQL-style persistence for quick iteration and lets you demonstrate JPA mappings. For this assignment an in-memory `Map` repository is enough and keeps the project simple.

---

## Testing

* Unit tests for the service layer (validation logic, business rules) and controller integration tests are recommended.
* Example: use `@WebMvcTest` for controller tests and `@SpringBootTest` for integration tests.

---

## Assumptions & Limitations

* `partNumber` is treated as the business key and must be unique.
* `partName` is stored in lowercase to satisfy normalization requirement.
* The service does not implement pagination for list/search results (can be added later).
* No authentication/authorization is included (out of scope for this assignment).

---

## Example README Checklist (for submission)

* [x] Source code (Git repository)
* [x] README with build/run instructions
* [x] Endpoints documented with examples
* [x] Notes on validations and business rules

---

## Contact / Author

Author: PhaseZero Candidate


