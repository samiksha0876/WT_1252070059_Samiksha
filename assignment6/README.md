# BookNest - Online Book Store

Full-stack online bookstore built with **React + TypeScript + Vite**, **Spring Boot + Java**, and **MongoDB**.

## Features
- Attractive responsive Home page
- Login page
- Registration page with MongoDB persistence
- Catalogue page with search and category filtering
- Book details and Add to Cart
- Cart drawer with quantity controls
- User dashboard/profile
- REST API with Spring Boot

## Run in VS Code

### 1. Start MongoDB
Use local MongoDB on `mongodb://localhost:27017/booknest`, or update the URI in `backend/src/main/resources/application.properties`.

### 2. Backend
Open a terminal:
```bash
cd backend
mvn spring-boot:run
```
Backend: `http://localhost:8080`

### 3. Frontend
Open another terminal:
```bash
cd frontend
npm install
npm run dev
```
Frontend: `http://localhost:5173`

## Demo login
The app includes a demo login button on the Login page. Registration is stored in MongoDB.

> Java 17+ and Maven 3.9+ recommended. Node.js 18+ recommended.
