# 🚀 How to Run All 10 Java Projects

This quick reference guide lists the exact commands to run and test every single project in this multi-module repository.

> [!NOTE]
> All commands below are designed to be executed directly from the **repository root folder** (`JAVA_Projects/`).
> You do **not** need to `cd` into module subdirectories.

---

## 📋 Quick Cheat Sheet Table

| # | Project Name | Type | Run Command (from Root) | Default Port / Window |
|---|--------------|:----:|-------------------------|:---------------------:|
| 1 | **Password Generator** | Desktop GUI | `mvn exec:java -pl 01-password-generator` | Swing Window |
| 2 | **Word Count Tool** | Desktop GUI / CLI | `mvn exec:java -pl 02-word-count-tool` | Swing Window |
| 3 | **Snake Game** | Desktop GUI | `mvn exec:java -pl 03-snake-game` | Swing Window |
| 4 | **Electricity Bill System** | Desktop GUI | `mvn exec:java -pl 04-electricity-bill-system` | Swing Window |
| 5 | **Exam Seating Arrangement** | Desktop GUI | `mvn exec:java -pl 05-exam-seating-arrangement` | Swing Window |
| 6 | **Online Survey System** | Spring Boot Web | `mvn spring-boot:run -pl 06-online-survey-system` | [http://localhost:8080](http://localhost:8080) |
| 7 | **Online Resume Builder** | Spring Boot Web | `mvn spring-boot:run -pl 07-online-resume-builder` | [http://localhost:8080](http://localhost:8080) |
| 8 | **Medical Management System** | Spring Boot Web | `mvn spring-boot:run -pl 08-medical-management-system` | [http://localhost:8080](http://localhost:8080) |
| 9 | **ISP Automation System** | Spring Boot Web | `mvn spring-boot:run -pl 09-isp-automation` | [http://localhost:8080](http://localhost:8080) |
| 10 | **Face Detection Application** | Desktop GUI / CLI | `mvn exec:java -pl 10-face-detection` | Swing Window |

---

## 🧪 Run Tests

### Run tests across ALL 10 projects at once:
```bash
mvn test
```

### Run tests for a specific project:
```bash
mvn test -pl 01-password-generator
mvn test -pl 02-word-count-tool
mvn test -pl 03-snake-game
mvn test -pl 04-electricity-bill-system
mvn test -pl 05-exam-seating-arrangement
mvn test -pl 06-online-survey-system
mvn test -pl 07-online-resume-builder
mvn test -pl 08-medical-management-system
mvn test -pl 09-isp-automation
mvn test -pl 10-face-detection
```

---

## 🛠️ Detailed Running Instructions Per Project

### 01. Password Generator
- **GUI Mode**:
  ```bash
  mvn exec:java -pl 01-password-generator
  ```
- **Run via Packaged JAR**:
  ```bash
  mvn package -pl 01-password-generator -DskipTests
  java -jar 01-password-generator/target/01-password-generator-1.0.0.jar
  ```

---

### 02. Word Count Tool
- **GUI Mode**:
  ```bash
  mvn exec:java -pl 02-word-count-tool
  ```
- **CLI Mode (Direct file analysis)**:
  ```bash
  mvn exec:java -pl 02-word-count-tool -Dexec.args="README.md"
  ```

---

### 03. Snake Game
- **GUI Mode**:
  ```bash
  mvn exec:java -pl 03-snake-game
  ```
- **Controls**: Arrow Keys or `W`, `A`, `S`, `D` to navigate, `SPACE` to pause/resume.

---

### 04. Electricity Billing System
- **GUI Mode (Runs with zero-setup embedded H2 database)**:
  ```bash
  mvn exec:java -pl 04-electricity-bill-system
  ```
- **Optional MySQL Mode**:
  ```bash
  mvn exec:java -pl 04-electricity-bill-system -Dexec.args="--mysql"
  ```

---

### 05. Exam Seating Arrangement System
- **GUI Mode**:
  ```bash
  mvn exec:java -pl 05-exam-seating-arrangement
  ```
- **CLI Mode (Headless solver)**:
  ```bash
  mvn exec:java -pl 05-exam-seating-arrangement -Dexec.args="--cli"
  ```

---

### 06. Online Survey System (Spring Boot Web)
- **Start the Server**:
  ```bash
  mvn spring-boot:run -pl 06-online-survey-system
  ```
- **Open in Browser**: [http://localhost:8080](http://localhost:8080)
- **Embedded H2 Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:surveydb`, User: `sa`, Password: empty)

---

### 07. Online Resume Builder (Spring Boot Web)
- **Start the Server**:
  ```bash
  mvn spring-boot:run -pl 07-online-resume-builder
  ```
- **Open in Browser**: [http://localhost:8080](http://localhost:8080)

---

### 08. Medical Management System (Spring Boot Web & Security)
- **Start the Server**:
  ```bash
  mvn spring-boot:run -pl 08-medical-management-system
  ```
- **Open in Browser**: [http://localhost:8080](http://localhost:8080)
- **Demo Logins**:
  - **Admin**: `admin@hospital.com` / `admin123`
  - **Doctor**: `doctor@hospital.com` / `doctor123`
  - **Patient**: `patient@hospital.com` / `patient123`

---

### 09. ISP Automation System (Spring Boot Web & Scheduled Billing)
- **Start the Server**:
  ```bash
  mvn spring-boot:run -pl 09-isp-automation
  ```
- **Open in Browser**: [http://localhost:8080](http://localhost:8080)
- **Customer Portal**: Select pre-seeded customer or view invoices.
- **NOC Incident Desk**: [http://localhost:8080/admin/tickets](http://localhost:8080/admin/tickets)

---

### 10. Computer Vision Face Detection Application
- **GUI Studio Mode**:
  ```bash
  mvn exec:java -pl 10-face-detection
  ```
- **CLI Sample Mode (Runs on bundled image)**:
  ```bash
  mvn exec:java -pl 10-face-detection -Dexec.args="--sample"
  ```
- **CLI Custom Image Mode**:
  ```bash
  mvn exec:java -pl 10-face-detection -Dexec.args="--cli --image path/to/photo.jpg --output target/detected.png"
  ```
- **View Ethical AI Principles**:
  ```bash
  mvn exec:java -pl 10-face-detection -Dexec.args="--ethics"
  ```

---

## 💡 Important Tips

1. **Stop a running web server**: Press `Ctrl + C` in the terminal.
2. **Port 8080 conflict**: If you switch between projects 6, 7, 8, or 9, remember to stop the previous project before starting the next one, since all Spring Boot web apps bind to port `8080` by default.
3. **Always run from the root**: Using the `-pl <module-name>` flag allows Maven to locate the project regardless of what folder you are in, avoiding path errors.
