# Vizsgaremek1

A real-time chat backend built with **Java Spring Boot**. 

## Features

- User registration and login
- Profile picture upload
- Real-time WebSocket messaging
- File uploads and notifications
- Friend/contact management

## Technologies

- Java 17
- Spring Boot, Spring WebSocket, Spring Security
- H2 Database (development)
- Liquibase
- Lombok

## Getting Started

1. Clone the repo:
   ```bash
   git clone https://github.com/LordMous/vizsgaremek1.git
   cd vizsgaremek1
   ```
2. Build and run:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

- WebSocket: `ws://localhost:8080/chat`
- REST APIs: `http://localhost:8080/api/...`
- H2 Console: `http://localhost:8080/h2-console`

## Main Endpoints

- `POST /api/users/register` - Register
- `POST /api/users/login` - Login
- `POST /api/users/{id}/profile-picture` - Upload profile picture
- `POST /api/messages/upload` - Upload file
- `POST /api/friends/request/{userId}` - Send friend request
- `GET /api/friends/list` - List friends
