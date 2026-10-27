# Java Projects Portfolio: 10 Production-Ready Systems

[![Java CI Multi-Module Build](https://github.com/pabitra/java-projects/actions/workflows/build.yml/badge.svg)](https://github.com/pabitra/java-projects/actions/workflows/build.yml)
[![Tests: 117 Passed](https://img.shields.io/badge/Tests-117%20Passed-10B981.svg)](PROGRESS.md)
[![Java Version](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Build Tool](https://img.shields.io/badge/Maven-3.9%2B-C71A36.svg)](https://maven.apache.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3%2B-6DB33F.svg)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 👋 About Me & This Repository

Welcome! I am a **3rd-year B.Tech student** passionate about software engineering, backend architecture, and problem-solving. This repository is my public portfolio showcasing **10 end-to-end Java projects** engineered to illustrate progressive mastery of:
- **Core Java & Object-Oriented Design**: Clean encapsulation, polymorphism, immutability, thread-safe concurrency, and design patterns.
- **Data Structures & Algorithms**: Frequency analysis, custom collections, backtracking, and constraint-satisfaction scheduling.
- **Desktop Engineering**: Modern, responsive Java Swing interfaces with event-driven architecture and real-time graphics.
- **Enterprise Persistence**: JDBC, PreparedStatement best practices, Spring Data JPA, schema migrations, and zero-configuration H2 databases with optional MySQL profiles.
- **Spring Boot Ecosystem**: RESTful APIs, Spring Security (Role-Based Access Control + BCrypt), Thymeleaf web UIs, and background task scheduling (`@Scheduled`).
- **Computer Vision**: Real-time Haar cascade facial detection using JavaCV / OpenCV.

Every project adheres to production coding standards: clean package separation (`model`, `repository`, `service`, `controller`, `util`), zero hardcoded secrets, input validation, defensive error handling, comprehensive JUnit 5 test suites, and strict sub-5-minute out-of-the-box runability.

---

## 🚀 Projects Overview

| # | Project Name | Tech Stack | Difficulty | Status | Folder Link | Preview |
|---|--------------|------------|:----------:|:------:|-------------|:-------:|
| 1 | **Password Generator** | Java Swing, `SecureRandom`, Clipboard API | Beginner | ✅ Done | [`01-password-generator`](file:///Users/pabitra/Downloads/JAVA_Projects/01-password-generator) | [SVG Preview](file:///Users/pabitra/Downloads/JAVA_Projects/docs/screenshots/01-password-generator-preview.svg) |
| 2 | **Word Count Tool** | CLI + Swing GUI, Java NIO, `HashMap`, Frequency Sorting | Beginner | ✅ Done | [`02-word-count-tool`](file:///Users/pabitra/Downloads/JAVA_Projects/02-word-count-tool) | [SVG Preview](file:///Users/pabitra/Downloads/JAVA_Projects/docs/screenshots/02-word-count-tool-preview.svg) |
| 3 | **Snake Game** | Java 2D Graphics, Swing `Timer`, Key Bindings, File I/O | Beginner | ✅ Done | [`03-snake-game`](file:///Users/pabitra/Downloads/JAVA_Projects/03-snake-game) | [SVG Preview](file:///Users/pabitra/Downloads/JAVA_Projects/docs/screenshots/03-snake-game-preview.svg) |
| 4 | **Electricity Bill System** | JDBC, H2/MySQL, Slab Tariff Logic, OpenPDF Generation | Intermediate | ✅ Done | [`04-electricity-bill-system`](file:///Users/pabitra/Downloads/JAVA_Projects/04-electricity-bill-system) | [SVG Preview](file:///Users/pabitra/Downloads/JAVA_Projects/docs/screenshots/04-electricity-bill-preview.svg) |
| 5 | **Exam Seating Arrangement** | Graph/Matrix DSA, Constraint Solver, CSV & PDF Export | Intermediate | ✅ Done | [`05-exam-seating-arrangement`](file:///Users/pabitra/Downloads/JAVA_Projects/05-exam-seating-arrangement) | [SVG Preview](file:///Users/pabitra/Downloads/JAVA_Projects/docs/screenshots/05-exam-seating-preview.svg) |
| 6 | **Online Survey System** | Spring Boot, Thymeleaf, Spring Data JPA, Chart.js Visuals | Intermediate | ✅ Done | [`06-online-survey-system`](file:///Users/pabitra/Downloads/JAVA_Projects/06-online-survey-system) | [SVG Preview](file:///Users/pabitra/Downloads/JAVA_Projects/docs/screenshots/06-online-survey-preview.svg) |
| 7 | **Online Resume Builder** | Spring Boot, Thymeleaf, Form Validation, OpenPDF Export | Intermediate | ✅ Done | [`07-online-resume-builder`](file:///Users/pabitra/Downloads/JAVA_Projects/07-online-resume-builder) | [SVG Preview](file:///Users/pabitra/Downloads/JAVA_Projects/docs/screenshots/07-online-resume-preview.svg) |
| 8 | **Medical Management System** | Spring Boot, Spring Security (RBAC), JPA, BCrypt, H2/MySQL | Advanced | ✅ Done | [`08-medical-management-system`](file:///Users/pabitra/Downloads/JAVA_Projects/08-medical-management-system) | [SVG Preview](file:///Users/pabitra/Downloads/JAVA_Projects/docs/screenshots/08-medical-management-preview.svg) |
| 9 | **ISP Automation System** | Spring Boot, JPA, `@Scheduled` Billing Invoices, Ticketing | Advanced | ✅ Done | [`09-isp-automation`](file:///Users/pabitra/Downloads/JAVA_Projects/09-isp-automation) | [SVG Preview](file:///Users/pabitra/Downloads/JAVA_Projects/docs/screenshots/09-isp-automation-preview.svg) |
| 10 | **Face Detection Application** | JavaCV / OpenCV, Haar Cascades, Ethical AI Framework | Advanced | ✅ Done | [`10-face-detection`](file:///Users/pabitra/Downloads/JAVA_Projects/10-face-detection) | [SVG Preview](file:///Users/pabitra/Downloads/JAVA_Projects/docs/screenshots/10-face-detection-preview.svg) |

---

## ⚡ How to Run Any Project (In Under 5 Minutes)

### Prerequisites
- **JDK 17 or higher** (`java -version`)
- **Maven 3.8+** (`mvn -version`)
- *Optional*: Docker & Docker Compose (if testing optional MySQL profiles)

### 1. Clone the Repository
```bash
git clone https://github.com/pabitra/java-projects.git
cd java-projects
```

### 2. Build and Test the Entire Portfolio
To verify and run all unit and integration tests across all modules:
```bash
mvn clean test
```

### 3. Run an Individual Project
Every module can be run directly using Maven without manual compilation:

#### Desktop / CLI Projects (Modules 1 - 5, 10):
Navigate to the module folder or run using Maven `exec`:
```bash
cd 01-password-generator
mvn clean compile exec:java
```

#### Spring Boot Web Applications (Modules 6 - 9):
All Spring Boot applications boot immediately with an embedded H2 database (zero installation needed):
```bash
cd 06-online-survey-system
mvn spring-boot:run
```
Then open your browser at `http://localhost:8080`.

#### Optional MySQL Setup:
For database-backed projects (Modules 4, 8, 9), an optional MySQL profile and `docker-compose.yml` are provided:
```bash
# Start MySQL container
docker compose up -d

# Run with the MySQL profile activated
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

---

## 🛠️ Repository Standards & Quality
- **Zero-Setup Default**: All modules run out-of-the-box using embedded engines or in-memory persistence.
- **Security First**: BCrypt password hashing, parameter binding against SQL injection, strict input sanitization.
- **Automated CI**: GitHub Actions runs on every push and PR against JDK 17 to ensure continuous build health.
- **Strict Testing**: Each module contains at least 5 meaningful JUnit 5 tests exercising real domain logic.

---

## 💼 Recruiter & Career Assets
- 📄 **[Resume Bullets (Google X-Y-Z Formula)](PORTFOLIO_GUIDE.md#1-resume-bullet-points-google-x-y-z-formula)**: 20 quantified bullet points for your software engineering resume.
- 🏷️ **[GitHub Topics & Description](PORTFOLIO_GUIDE.md#2-recommended-github-repository-topics--tags)**: Recommended SEO tags and repository description.
- 📌 **[Pinned Repository Strategy](PORTFOLIO_GUIDE.md#4-top-34-projects-to-pin-on-github-profile)**: Recommended top 3-4 projects to feature on your GitHub profile.
- 🧠 **[Interview Revision Master List](PORTFOLIO_GUIDE.md#5-comprehensive-interview-revision-list)**: Core Java, OOP, DSA, JPA, Spring Boot, Security, and Computer Vision interview concepts.

---

## 📜 License
This repository is licensed under the [MIT License](LICENSE).
