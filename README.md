# Library Management System 

A full-stack **Library Management System** built with **Java Spring Boot 3 + React (Vite + TypeScript)** and an **H2 file database**.

## What is upgraded

- New users can register themselves
- Old users (`admin`, `librarian`) can still log in
- Logged-in users can borrow books
- Users can borrow **multiple copies of the same book**
- Users can return borrowed books
- Admin and Librarian accounts remain available for management tasks
- Uses a dedicated H2 file database for this upgraded version:
  - `backend/data/librarydb_final`

## Tech stack

### Backend
- Java 17+
- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA
- H2 database

### Frontend
- React 18
- JavaScript
- Vite
- Axios
- React Router

## Default accounts

| Username | Password | Roles |
|---|---|---|
| admin | admin123 | ADMIN, LIBRARIAN |
| librarian | lib123 | LIBRARIAN |
| reader | reader123 | USER |

You can also create your own USER account from the Register page.

## H2 console

After starting the backend, open:

- http://localhost:8081/h2-console

Use:

- JDBC URL: `jdbc:h2:file:./data/librarydb_final;AUTO_SERVER=TRUE`
- Username: `sa`
- Password: leave blank

## Run the backend

```bash
cd backend
mvnw.cmd spring-boot:run
```

If you are on macOS/Linux:

```bash
cd backend
./mvnw spring-boot:run
```

Backend URL:
- http://localhost:8081

## Run the frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend URL:
- http://localhost:3000

## Important note about old DB files

This upgraded version uses a **new H2 file** named:

- `librarydb_final`

So it does not depend on older files like `moviesdb` or earlier library DB files.

## Role summary

### Admin
- Controls users and the whole system
- Has full access
- Can manage books, members, and see all borrowings

### Librarian
- Controls books and library operations
- Can manage books and members
- Can see all borrowings

### User
- Registers and signs in
- Can borrow books
- Can borrow multiple copies in one borrowing
- Can return their borrowed books

## Main APIs

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/books`
- `POST /api/borrowings/borrow`
- `POST /api/borrowings/{id}/return`
- `POST /api/borrowings/{id}/renew`
- `GET /api/borrowings`
- `GET /api/dashboard/stats`
