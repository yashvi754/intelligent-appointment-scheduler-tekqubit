# Intelligent Appointment Scheduler (Tekqubit)

An intelligent service appointment scheduling system with a Spring Boot backend and a modern frontend.  
The project focuses on clean execution flow, proper data handling, and a simple setup process.

---

## Tech Stack

### Backend
- Java
- Spring Boot
- Maven (Maven Wrapper)

### Frontend
- Node.js
- npm
- Vite-based development server

---

## Prerequisites

Ensure the following are installed on your system:

- Java 17 or higher
- Node.js & npm
- Git

---

## Project Structure

```
intelligent-scheduler/
├── backend/
├── frontend/
└── README.md
```

---

## How to Run the Project

### 1. Backend Setup (Mandatory)

1. Navigate to the `backend` folder.
2. **Delete the `data` file/folder if it exists** inside the backend directory.  
   This avoids conflicts caused by previously stored data.

### 2. Run Backend (Spring Boot)

From the project root, run:

```bash
cd backend
.\mvnw.cmd spring-boot:run
```

### 3. Run Frontend

Open the new terminal window, then run:
```bash
cd frontend
npm run dev
```
## Access the Application
- Frontend will run on the URL shown in the terminal (commonly http://localhost:5173)
- Backend runs on the configured Spring Boot port.
