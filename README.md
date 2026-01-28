# Inventory & Employee Management Backend System

A **Spring Boot–based backend system** designed for organizations that require secure inventory control, employee accountability, and transaction-safe stock operations — without the complexity of full-scale ERP systems.

This project focuses on **data integrity, security, concurrency handling, and clean backend architecture**, making it production-ready and recruiter-friendly.

---

## Project Overview

The Inventory & Employee Management System is a **RESTful backend application** built using **Spring Boot**, implementing transaction-driven stock updates, role-based access control (RBAC), audit logging, and operational analytics.

The system follows a **clean layered architecture** and is designed to handle real-world workflows involving multiple users, concurrent updates, and strict traceability requirements.

---

## Problem Statement

Most inventory systems fall into two extremes:

- **Too simple** → Basic CRUD systems with no transaction safety, audit trails, or access control  
- **Too complex** → ERP-level solutions that are heavy, rigid, and difficult to customize  

This project bridges that gap by offering:

- Transaction-safe inventory operations  
- Secure multi-user access  
- Full audit and traceability  
- Actionable analytics and reporting  

---

## Core Business Logic & Features

### Inventory Management
- Transaction-based stock IN / OUT handling  
- Prevention of negative stock  
- Stock adjustment with mandatory reasons (damaged, issued, returned, etc.)  
- Custom low-stock thresholds per item  
- Soft deletion to preserve historical data  

### Audit & Accountability
- Complete transaction and activity history  
- Tracks **who did what and when**  
- Stores user, timestamp, IP, and operation metadata  
- Immutable audit logs for traceability  

### Security & Access Control
- Stateless JWT authentication  
- Role-Based Access Control (RBAC)  
- Restricted API access based on role and responsibility  

#### Roles

**ADMIN**
- Full system access  
- User and role management  
- Audit logs and analytics access  

**MANAGER**
- Inventory oversight  
- Reports and dashboards  
- Low-stock monitoring  

**EMPLOYEE**
- Inventory viewing  
- Stock transactions  
- Limited data visibility  

### Concurrency & Data Integrity
- Optimistic locking to prevent lost updates  
- Transactional boundaries to maintain consistency  
- Safe handling of simultaneous stock updates  

### Reporting & Analytics
- Inventory valuation  
- Low-stock and slow-moving item detection  
- Transaction history per employee  
- Dashboard KPIs for operational insights  

### Import / Export
- Bulk inventory and employee import via CSV / Excel  
- Export inventory and transaction data for reporting  

---

## Tech Stack

| Layer | Technology |
|-----|-----------|
| Framework | Spring Boot |
| Security | Spring Security (JWT, RBAC) |
| ORM | Hibernate / JPA |
| Database | MySQL / PostgreSQL |
| Reporting | CSV / PDF |
| Build Tool | Maven |
| Tools | Git, Postman |

---

## Backend Architecture

### Controllers
- RESTful endpoint exposure  
- DTO-based request validation  
- Clear HTTP request/response handling  

### Services
- Core business logic  
- `@Transactional` boundaries for consistency  
- Validation and rule enforcement  

### Repositories
- JPA repositories  
- Custom queries for reporting and analytics  

### Security
- JWT-based stateless authentication  
- Method-level authorization  
- Fine-grained role enforcement  

---

## API Endpoints Overview

### Authentication

#### Register User  
**POST /api/auth/register**

```json
{
  "username": "mark",
  "email": "mark@staff.com",
  "password": "mark123",
  "role": "STAFF"
}
```
#### Supported Roles:Admin,Manager,Employee

#### Login
**POST /api/auth/login

```json
{
  "username": "staff",
  "password": "staff123"
}
```
#### Returns a JWT token required for all authenticated requests.

### Inventory
### Create Product (Admin / Manager Only)
#### POST /api/products

```json
{
  "sku": "009",
  "name": "Earbuds",
  "category": "Electronics",
  "costPrice": 900.00,
  "sellingPrice": 1200.00,
  "quantity": 50
}
```
#### Creates a new product with initial stock.

### Stock Transactions
### Adjust Stock
#### POST /api/stock/adjust

```json
{
  "productId": 1,
  "quantity": -5,
  "reason": "DAMAGED"
}
```
#### Adjusts inventory stock with a mandatory reason.

### Get Transaction History
### GET /api/stock/transactions
#### Returns complete stock transaction history with audit details.

### Low Stock Alerts
### GET /api/stock/low-alerts
#### Returns products that have reached their configured reorder threshold.

### Bulk Stock Update
### POST /api/stock/bulk-update
#### Bulk update stock quantities using CSV or structured payload.

## Analytics

### Dashboard KPIs
### GET /api/analytics/dashboard-kpis
#### Returns key dashboard metrics such as inventory value and recent activity.

### Total Inventory Value
### GET /api/analytics/stock-value
#### Calculates total inventory value using quantity and pricing.

### Slow Moving Items
### GET /api/analytics/slow-moving
#### Returns products with low sales velocity.


## Import / Export


### Import Products
### POST /api/import/products
#### Uploads products using CSV or Excel files.

### Import Employees
### POST /api/import/employees
#### Uploads employee data in bulk.

### Export Inventory
### GET /api/export/inventory
#### Exports inventory data as CSV.

### Export Transactions
### GET /api/export/transactions
#### Exports transaction history as CSV.

### Audit & Activity
### Get All Audit Logs (Admin Only)
### GET /api/audit/logs
#### Returns complete system audit logs.

### Entity Audit History
### GET /api/audit/entity/{id}
#### Returns audit history for a specific entity.

### User Activity
### GET /api/audit/user/{userId}
#### Returns actions performed by a specific user.

## Notifications

### Get Notifications
### GET /api/notifications
#### Fetch notifications for the authenticated user.

### Mark Notification as Read
### PUT /api/notifications/{id}/read
#### Marks a notification as read.

### Update Notification Preferences
### POST /api/notifications/settings
#### Updates notification preferences for the user.

