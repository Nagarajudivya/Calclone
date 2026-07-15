# 📅 Cal Clone

A full-stack appointment scheduling application inspired by **Cal.com**, built using **Spring Boot**, **Javascript**, and **PostgreSQL**. The application enables users to schedule meetings, manage availability, and book appointments through an intuitive interface.

---

## 🚀 Features

### Authentication
- User Registration
- User Login
- JWT Authentication
- Secure Password Encryption
- Protected Routes

### User Features
- Create and Manage Event Types
- Set Availability Schedule
- Book Appointments
- View Upcoming Meetings
- Cancel Scheduled Meetings
- Profile Management

### Backend Features
- RESTful APIs
- JWT-based Authentication & Authorization
- Exception Handling
- Input Validation
- Layered Architecture
- Database Integration using JPA/Hibernate

---

## 🛠️ Tech Stack

### Frontend
- JavaScript
- HTML5
- CSS3

### Backend
- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- REST API

### Database
- PostgreSQL

### Tools
- Git
- GitHub
- Postman
- IntelliJ IDEA
- VS Code

---

## 🏗️ Project Architecture

```
React Frontend
        │
        ▼
REST APIs
        │
        ▼
Spring Boot Backend
        │
        ▼
Spring Data JPA / Hibernate
        │
        ▼
PostgreSQL Database
```

---

## 📁 Project Structure

```
cal-clone
│
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── ...
│
│   ├── src/resources
│   ├── /templates
│   └── ...
│
└── README.md
```

---

## ⚙️ Installation

### Clone the Repository

```bash
git clone https://github.com/your-username/cal-clone.git
```

### Backend Setup

```bash
cd backend

mvn clean install

mvn spring-boot:run
```

Backend runs on:

```
http://localhost:8080
```

---

### Frontend Setup

```bash
cd frontend

npm install

npm run dev
```

Frontend runs on:

```
http://localhost:5173
```

---

## 📡 API Endpoints

### Authentication

```
POST /api/auth/register

POST /api/auth/login
```

### Events

```
GET /api/events

POST /api/events

PUT /api/events/{id}

DELETE /api/events/{id}
```

### Bookings

```
POST /api/bookings

GET /api/bookings

DELETE /api/bookings/{id}
```

---

## 🔒 Security

- JWT Authentication
- Password Encryption
- Role-based Authorization (if implemented)

---

## 🌱 Future Enhancements

- Google Calendar Integration
- Email Notifications
- Meeting Reminders
- Video Meeting Integration

---

## 👩‍💻 Author

**Divya Nagaraju**

- Java Backend Developer
- GitHub: https://github.com/Nagarajudivya

---

## 📄 License

This project is developed for learning purposes and portfolio demonstration.
