# **Library Management System – Design Document**

---

## **1. Introduction**

### **1.1 Purpose**

This document provides a comprehensive design overview of the *Library Management System* based on the provided project. It describes the system architecture, components, data flow, and implementation details to help developers, stakeholders, and reviewers understand the system clearly.

### **1.2 Scope**

The system is a full-stack web application designed to:

* Manage books and users
* Handle borrowing and returning of books
* Enforce role-based access (Admin, Librarian, User)
* Track due dates and fines
* Provide REST APIs for frontend integration

---

## **2. System Overview**

### **2.1 System Type**

* Web-based application
* Client-Server Architecture

### **2.2 Technology Stack**

| Layer      | Technology                |
| ---------- | ------------------------- |
| Backend    | Spring Boot               |
| Frontend   | React (JSX-based UI)      |
| Database   | MySQL / H2 (configurable) |
| ORM        | Spring Data JPA           |
| Security   | Spring Security           |
| Build Tool | Maven                     |

---

## **3. Architecture Design**

### **3.1 High-Level Architecture**

```
Frontend (React UI)
        ↓
REST API (Spring Boot Controllers)
        ↓
Service Layer (Business Logic)
        ↓
Repository Layer (JPA)
        ↓
Database (MySQL / H2)
```

### **3.2 Layers Description**

#### **1. Controller Layer**

* Handles HTTP requests
* Maps endpoints (`/api/v1/...`)
* Validates inputs
* Calls service methods

#### **2. Service Layer**

* Contains business logic
* Implements rules such as:

  * Borrow restrictions
  * Due date calculation
  * Fine calculation

#### **3. Repository Layer**

* Interface extending JPA repositories
* Handles CRUD operations
* Abstracts database access

#### **4. Entity Layer**

* Represents database tables
* Uses annotations like `@Entity`, `@ManyToOne`

---

## **4. Functional Requirements**

### **4.1 User Management**

* Register new users
* Login authentication
* Role-based access:

  * Admin
  * Librarian
  * User

### **4.2 Book Management**

* Add books
* Update books
* Delete books
* View available books

### **4.3 Borrowing System**

* Borrow book (if available)
* Return book
* Track borrowing history

### **4.4 Due Date Handling**

* Default due date = current date + 7 days
* Only Admin/Librarian can modify due date

### **4.5 Fine Management**

* Fine applied if book is returned late
* User must clear fine before borrowing again

---

## **5. Non-Functional Requirements**

* **Performance:** Fast API responses
* **Security:** Role-based authorization
* **Scalability:** Modular architecture
* **Maintainability:** Clean layered design
* **Usability:** Simple UI

---

## **6. Database Design**

### **6.1 Key Entities**

#### **User**

* id
* username
* password
* role

#### **Book**

* id
* title
* author
* isbn
* availableCopies

#### **BorrowRecord**

* id
* user_id (FK)
* book_id (FK)
* issueDate
* dueDate
* returnDate
* isReturned
* fineAmount

---

### **6.2 Relationships**

* One User → Many BorrowRecords
* One Book → Many BorrowRecords

**Reason:**
Each borrow record belongs to:

* One user
* One book

---

## **7. API Design**

### **7.1 Authentication APIs**

* `POST /api/auth/register`
* `POST /api/auth/login`

### **7.2 Book APIs**

* `GET /api/books`
* `POST /api/books`
* `PUT /api/books/{id}`
* `DELETE /api/books/{id}`

### **7.3 Borrow APIs**

* `POST /api/borrows/{bookId}`
* `GET /api/borrows`
* `PUT /api/borrows/return/{id}`

---

## **8. Business Logic Design**

### **8.1 Borrow Flow**

1. Check book availability
2. Reduce available copies
3. Create borrow record
4. Set due date = current date + 7 days

---

### **8.2 Return Flow**

1. Mark as returned
2. Update return date
3. Increase book copies
4. Calculate fine:

   * If returned after due date

---

### **8.3 Fine Calculation**

```
Fine = (Return Date - Due Date) × Daily Fine Rate
```

---

### **8.4 Borrow Restrictions**

* User cannot borrow:

  * If already holding a book
  * If unpaid fine exists

---

## **9. Security Design**

### **9.1 Authentication**

* Username + Password
* JWT-based authentication (if implemented)

### **9.2 Authorization**

* Role-based:

  * Admin → Full control
  * Librarian → Manage books & due dates
  * User → Borrow/Return only

---

## **10. UI Design (Frontend)**

### **10.1 Pages**

* Login/Register
* Dashboard
* Book List
* Borrow History
* Admin Panel

### **10.2 Features**

* Dynamic rendering using React
* API integration via Axios/Fetch

---

## **11. Error Handling**

* Validation errors (e.g., password length)
* Resource not found
* Unauthorized access
* Server errors (500)

---

## **12. Deployment Design**

### **12.1 Backend**

* Runs on configurable port (e.g., 8081)

### **12.2 Frontend**

* Runs on development server (React)

### **12.3 Database**

* Local or cloud-based DB

---

## **13. Future Enhancements**

* Email notifications
* Payment gateway for fines
* Book recommendation system
* Multi-book borrowing
* Analytics dashboard

---

## **14. Conclusion**

This Library Management System is designed using a clean, modular architecture with clear separation of concerns. It supports real-world use cases like borrowing, fine management, and role-based access control, making it scalable and production-ready with further enhancements.

---
