# PROJECT RULES & STANDING GUIDELINES

This document defines the strict development standards, architectural rules, and quality guidelines for all modules within the `java-projects` repository. These rules must be consulted at the beginning of each project task.

---

## 1. Core Technical Stack
- **Language**: Java 17 (LTS)
- **Build Tool**: Maven (Multi-module architecture with parent POM)
- **Testing**: JUnit 5 (Jupiter), AssertJ / Mockito where applicable
- **Version Control**: Git with Conventional Commits (e.g., `feat(snake-game): add score and speed levels`)

---

## 2. Architecture & Design Principles
- **Package Hierarchy**:
  Standardized layered architecture across all projects:
  - `model` / `entity`: Domain entities and data structures
  - `repository` / `dao`: Data access layer (Spring Data JPA or JDBC DAOs)
  - `service`: Business logic and orchestration
  - `controller` / `ui`: Presentation layer (REST controllers, Thymeleaf controllers, or Swing UI)
  - `util`: Reusable utilities and helper classes
  - `dto` / `exception`: Request/response models and custom exceptions (as needed)
- **Code Simplicity & Readability**:
  - Keep methods short, cohesive, and focused on a single responsibility.
  - Write concise comments explaining **WHY** non-obvious logic exists, not merely WHAT the code does.
  - Avoid over-engineering, unnecessary abstractions, or bloated patterns.

---

## 3. Database & Persistence Standards
- **Zero-Setup Default**: H2 in-memory or embedded database by default so any project can be cloned and run immediately with zero external dependencies.
- **Production-Ready Profiles**: Provide an optional MySQL profile (`application-mysql.properties`) and a root or module-level `docker-compose.yml` for DB-backed projects.
- **SQL Security**:
  - Strictly use JPA/Hibernate query methods or `PreparedStatement` with parameterized placeholders.
  - **NEVER** use string concatenation or dynamic string interpolation to construct SQL queries.

---

## 4. Security & Configuration Best Practices
- **No Hard-Coded Secrets**: All credentials, API tokens, and environment parameters must be driven by `application.properties` or system environment variables.
- Provide `.env.example` templates where external configuration is needed.
- **Password Security**: Always use strong cryptographic hashing (BCrypt with appropriate work factor) wherever user authentication or credential storage is involved.
- **Validation**: Validate all inputs at presentation boundaries and handle exceptions gracefully with clear, user-friendly error messages.

---

## 5. Testing & Verification Standards
- Every project must include **at least 5 meaningful JUnit 5 tests** testing real business logic and boundary scenarios.
- All tests must pass cleanly with `mvn test`.
- Never commit fake tests, empty assertions, TODOs, or placeholder stub code.

---

## 6. Execution & Verification Workflow
For each project in sequence:
1. **Brief Plan**: Formulate scope and architecture before coding.
2. **Incremental Implementation**: Code in clean, modular steps.
3. **Automated Testing**: Write unit/integration tests and verify with `mvn test`.
4. **Interactive Verification**: Run the application and confirm end-to-end functionality.
   - For web apps: verify key user flows and capture UI screenshots into `docs/screenshots/`.
5. **Documentation**: Write a comprehensive module `README.md` containing:
   - One-line description
   - Feature summary
   - Tech stack breakdown
   - Run instructions (copy-pasteable commands)
   - Project directory structure
   - Mermaid class / ER diagram
   - Screenshots in `docs/screenshots/`
   - Future improvements
6. **Git Commit**: Commit all module files with a conventional commit message.
7. **Progress Tracking**: Update `PROGRESS.md` after completion.
8. **Java Concept Review**: Provide a clear educational recap of Java concepts utilized before proceeding.
