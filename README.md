# SpendSmart Expense Tracker Backend

SpendSmart is a Java Spring Boot microservices backend for a full-stack Expense Tracker with Data Visualization. It supports user authentication, expense and income tracking, category management, budgets, analytics, recurring transactions, notifications, payments, and administrator oversight.

This backend matches the case-study goal: help users track daily income and expenses, organize transactions into custom categories, monitor budgets, unlock recurring flows, and view financial summaries through the frontend dashboard.

## Backend Architecture

The project is organized as independent Spring Boot services connected through Eureka service discovery and an API Gateway.

| Service | Port | Purpose |
| --- | ---: | --- |
| `eureka-server` | `8761` | Service registry for the microservices |
| `api-gateway` | `8080` | Single entry point for the frontend and routes requests to services |
| `auth-service` | `8081` | Registration, login, OTP, JWT, profile preferences, user data |
| `expense-service` | `8082` | Expense CRUD and user expense history |
| `income-service` | `8083` | Income CRUD and user income history |
| `category-service` | `8084` | Expense and income categories |
| `budget-service` | `8085` | Monthly budget tracking |
| `analytics-service` | `8086` | Income, expense, and summary analytics |
| `recurring-service` | `8087` | Recurring income/expense rules and scheduled automation |
| `notification-service` | `8088` | In-app and email notifications |
| `payment-service` | `8090` | Razorpay payment records and recurring access purchase |
| `admin-service` | `8091` | Admin login, users, transactions, broadcast, audit logs |

## Tech Stack

- Java 17
- Spring Boot
- Spring Cloud Netflix Eureka
- Spring Cloud Gateway
- Spring Web / WebMVC / WebFlux
- Spring Data JPA
- MySQL
- Spring Security and JWT
- RabbitMQ for notification messages
- Redis for OTP/auth support
- Java Mail / SMTP
- Razorpay Java SDK
- Maven Wrapper
- JUnit and JaCoCo

## Prerequisites

Install and run these before starting the services:

- JDK 17
- MySQL Server
- RabbitMQ
- Redis
- Maven is optional because every service includes `mvnw.cmd`

Create the MySQL databases used by the services:

```sql
CREATE DATABASE spendsmart_auth_db;
CREATE DATABASE spendsmart_expense_db;
CREATE DATABASE spendsmart_income_db;
CREATE DATABASE spendsmart_category_db;
CREATE DATABASE spendsmart_budget_db;
CREATE DATABASE spendsmart_recurring_db;
CREATE DATABASE spendsmart_notification_db;
CREATE DATABASE spendsmart_payment_db;
```

Update each service's `src/main/resources/application.yml` if your local MySQL, RabbitMQ, Redis, SMTP, or Razorpay credentials differ from the committed development values.

## How To Run

Open one terminal per service and start them in this order:

```powershell
cd eureka-server
.\mvnw.cmd spring-boot:run
```

```powershell
cd api-gateway
.\mvnw.cmd spring-boot:run
```

Then start the business services:

```powershell
cd auth-service
.\mvnw.cmd spring-boot:run
```

```powershell
cd expense-service
.\mvnw.cmd spring-boot:run
```

```powershell
cd income-service
.\mvnw.cmd spring-boot:run
```

```powershell
cd category-service
.\mvnw.cmd spring-boot:run
```

```powershell
cd budget-service
.\mvnw.cmd spring-boot:run
```

```powershell
cd analytics-service
.\mvnw.cmd spring-boot:run
```

```powershell
cd recurring-service
.\mvnw.cmd spring-boot:run
```

```powershell
cd notification-service
.\mvnw.cmd spring-boot:run
```

```powershell
cd payment-service
.\mvnw.cmd spring-boot:run
```

```powershell
cd admin-service
.\mvnw.cmd spring-boot:run
```

After startup:

- Eureka dashboard: `http://localhost:8761`
- API Gateway: `http://localhost:8080`
- Frontend expected origin: `http://localhost:5173`

## API Gateway Routes

The frontend should call the gateway on `http://localhost:8080`. The gateway routes:

| Path | Service |
| --- | --- |
| `/auth/**` | `auth-service` |
| `/expenses/**` | `expense-service` |
| `/income/**` | `income-service` |
| `/categories/**` | `category-service` |
| `/budgets/**` | `budget-service` |
| `/analytics/**` | `analytics-service` |
| `/recurring/**` | `recurring-service` |
| `/payments/**` | `payment-service` |
| `/notifications/**` | `notification-service` |
| `/admin/**` | `admin-service` |

## Main API Modules

### Authentication

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/register/send-otp`
- `POST /auth/register/verify-otp`
- `POST /auth/password/forgot/send-otp`
- `POST /auth/password/forgot/verify-otp`
- `POST /auth/password/reset`
- `GET /auth/users/{userId}`
- `PUT /auth/users/{userId}/preferences`

### Expenses

- `POST /expenses`
- `GET /expenses/{id}`
- `GET /expenses/user/{userId}`
- `PUT /expenses/{id}`
- `DELETE /expenses/{id}`

### Income

- `POST /income`
- `GET /income/{id}`
- `GET /income/user/{userId}`
- `PUT /income/{id}`
- `DELETE /income/{id}`

### Categories

- `POST /categories`
- `GET /categories/{id}`
- `GET /categories/user/{userId}`
- `GET /categories/user/{userId}/{type}`
- `PUT /categories/{id}`
- `DELETE /categories/{id}`

### Budgets

- `POST /budgets`
- `GET /budgets/{id}`
- `GET /budgets/user/{userId}`
- `GET /budgets/user/{userId}/{month}/{year}`
- `PUT /budgets/{id}`
- `DELETE /budgets/{id}`

### Analytics

- `GET /analytics/expense/{userId}`
- `GET /analytics/income/{userId}`
- `GET /analytics/summary/{userId}`

### Recurring Transactions

- `POST /recurring`
- `GET /recurring`
- `GET /recurring/user/{userId}`
- `DELETE /recurring/{id}`

### Payments

- `POST /payments`
- `POST /payments/recurring-access/order`
- `POST /payments/recurring-access/verify`
- `GET /payments/recurring-access/user/{userId}/status`
- `GET /payments/{id}`
- `GET /payments/user/{userId}`
- `GET /payments/user/{userId}/summary`
- `PUT /payments/{id}`
- `DELETE /payments/{id}`

### Notifications

- `POST /notifications/send`
- `POST /notifications/budget-alert`
- `POST /notifications/bulk`
- `POST /notifications/email`
- `GET /notifications/recipient/{recipientId}`
- `GET /notifications/recipient/{recipientId}/unread`
- `GET /notifications/recipient/{recipientId}/unread/count`
- `PUT /notifications/{notificationId}/read`
- `PUT /notifications/recipient/{recipientId}/read-all`
- `DELETE /notifications/{notificationId}`

### Admin

- `POST /admin/login`
- `GET /admin/overview`
- `GET /admin/users`
- `PUT /admin/users/{userId}/status`
- `DELETE /admin/users/{userId}`
- `GET /admin/transactions`
- `POST /admin/broadcast`
- `GET /admin/audit-logs`

## Testing

Run tests for an individual service from that service folder:

```powershell
.\mvnw.cmd test
```

Example:

```powershell
cd expense-service
.\mvnw.cmd test
```

Several services include controller, service, application, and layer coverage tests. JaCoCo is configured in the Maven builds for coverage reporting.

## Frontend Connection

The frontend project uses Vite proxying. In development it calls `/api`, and Vite forwards that to:

```text
http://127.0.0.1:8080
```

Make sure the API Gateway is running before using the frontend.

## Case Study Features Covered

- User and admin roles
- Registration, login, OTP verification, password reset, and profile preferences
- Expense, income, category, and budget management
- Monthly summary and analytics endpoints
- Recurring transaction rules
- Budget and notification flows
- Razorpay-backed recurring access payment
- Admin overview, user management, transactions, broadcasts, and audit logs
