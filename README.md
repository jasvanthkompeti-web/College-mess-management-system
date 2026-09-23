# College Mess Management — 7 Files

A simple full-stack Java project designed for a fresher.

## Exactly 7 files

1. `pom.xml` — Maven dependencies
2. `application.properties` — MySQL configuration
3. `MessApplication.java` — Spring Boot backend, entities, repositories, authentication, controllers and security
4. `index.html` — frontend
5. `style.css` — frontend styling
6. `app.js` — frontend JavaScript
7. `README.md` — instructions

## Technologies

Java 17 + Spring Boot + Spring MVC + Spring Data JPA + Hibernate + Spring Security + MySQL + HTML + CSS + JavaScript + Maven.

## Setup

Install Java 17, Maven and MySQL.

Create the database:

```sql
CREATE DATABASE college_mess;
```

Open `application.properties` and change:

```properties
spring.datasource.username=root
spring.datasource.password=root
```

to your MySQL credentials.

Run:

```bash
mvn spring-boot:run
```

Open:

```text
http://localhost:8080
```

## Admin account

The first startup automatically creates:

```text
Email: admin@mess.com
Password: Admin@123
```

The admin can manage menu items through the REST API. Students can register, login and place orders.

## GitHub

```bash
git init
git add .
git commit -m "College mess full stack application"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/college-mess-7-files.git
git push -u origin main
```

Do not upload real passwords or secrets to GitHub.

## Important

This 7-file version is intentionally simplified for learning. Authentication tokens are kept in memory, so users are logged out when the server restarts. For a production application, use persistent authentication/JWT and externalized secrets.
