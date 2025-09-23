# 📚 Student Course Registration System

A Java-based **console and web application (demo UI)** that simulates a **Student Course Registration System** with
enrollment, waitlisting, and profile management. Built with **Maven**, uses **DynamoDB Local** as the datastore, and
supports both **console interaction** and a lightweight **web UI** built with [Spark Java](http://sparkjava.com/).  
The project demonstrates how to integrate **Java, DynamoDB Local, Docker, and Jenkins** into a CI/CD pipeline.

The application runs in **two modes**:
- **Console Application** → interactive student signup, login, enroll/drop.
- **WebApp Demo UI (Spark Java)** → runs in the browser with simple HTML (inline CSS).

---

## 🚀 Features

- **Student Operations**
    - Signup, login, logout
    - View profile & enrolled courses
    - Enroll into or drop courses (with seat limits, deadlines, waitlist)

- **Course Management**
    - Preloaded courses from `seed-courses.json`
    - Enforces enrollment limits and deadlines

- **Infrastructure**
    - **DynamoDB Local** as the persistence layer (runs locally or via Docker)
    - **JUnit 5** testing (unit & integration)
    - **Jenkins CI/CD pipeline** with automated build, test, and deploy
    - **Dockerized deployment**

- **WebApp Demo UI**
    - Built with **Spark Java**
    - Accessible at [http://localhost:3000](...)
    - Provides routes:
        - `/` → Home
        - `/courses` → View all courses
        - `/signup` → Student signup form
        - `/login` → Login form
        - `/profile` → Profile page with enroll/drop forms
    - **Inline CSS** written inside `WebApp.java` (no separate CSS/JS files)

---

## 🛠️ Tech Stack

- **Java 17**
- **Maven**
- **JUnit 5**
- **DynamoDB Local**
- **Docker & Docker Compose**
- **Jenkins**
- **Spark Java (for demo UI)**

---

## 📂 Project Structure

```
src/
 ├── main/java/com/atlas
 │   ├── app/              # Console App (App.java)
 │   ├── dao/              # DAO interfaces
 │   ├── exception/        # Custom exceptions
 │   ├── model/            # Entities (Student, Course, Logs)
 │   ├── repository/       # DynamoDB DAO implementations
 │   ├── service/          # Business logic
 │   └── web/              # WebApp.java (Spark-based demo UI, inline CSS/HTML)
 │
 ├── main/resources
 │   └── seed-courses.json # Preloaded course data
 │
 └── test/java/com/atlas
     ├── tests/            # JUnit 5 tests (integration + unit)
     └── testutil/         # In-memory DAO fakes for testing

Other supporting files:
 ├── pom.xml               # Maven build configuration
 ├── Jenkinsfile           # CI/CD pipeline definition
 ├── Dockerfile            # App container definition
 ├── docker-compose.yml    # Orchestration (Jenkins + DynamoDB + App)
 ├── README.md             # Project documentation
```

---

## ⚡ Getting Started

### Clone and Build
```bash
git clone https://github.com/<your-username>/StudentCourseRegistrationSystem.git
cd StudentCourseRegistrationSystem
mvn clean install
```

### Run Console App
```bash
java -cp target/StudentCourseRegistrationSystem-1.0-SNAPSHOT.jar com.atlas.app.App
```

### Run WebApp (Spark)
```bash
java -cp target/StudentCourseRegistrationSystem-1.0-SNAPSHOT.jar com.atlas.web.WebApp
```
Open [http://localhost:3000](http://localhost:3000).

---

## 🐳 Running with Docker

### Build Docker image
```bash
docker build -t student-course-registry .
```

### Run with DynamoDB Local
```bash
docker run -d -p 8000:8000 --name dynamodb amazon/dynamodb-local
docker run -d -p 3000:3000 student-course-registry
```

### Orchestrate with Docker Compose
```bash
docker-compose up -d
```

This starts:
- Jenkins UI → [http://localhost:8080](http://localhost:8080)
- DynamoDB Local → [http://localhost:8000](http://localhost:8000)
- Student App (Spark UI) → [http://localhost:3000](http://localhost:3000)

---

## 🔄 CI/CD Pipeline (Jenkins)

Jenkins pipeline defined in Jenkinsfile:
1. Checkout code from GitHub
2. Run **JUnit 5** tests
3. Package `.jar` + build Docker image
4. Run DynamoDB Local (if not running)
5. Deploy app container
6. Cleanup after tests

- Builds and logs → **Jenkins UI**
- Containers and volumes → **Docker Desktop**

---

## ✅ Testing

Run all tests:
```bash
mvn test
```

- Integration tests spin up a **DynamoDB Local** instance.
- Coverage includes:
    - Authentication & session handling
    - Seat capacity and waitlist promotion
    - Enrollment deadlines

---

## 📊 Monitoring

- **Jenkins UI** → Pipeline status, logs
- **Docker Desktop** → Container & volume status
- **Console/Web logs** → Runtime logs

---

## 📌 Notes

- Jenkins uses **port 8080**
- Spark WebApp uses **port 3000** (configurable)
- DynamoDB Local uses **port 8000**
- Console app and WebApp can be run independently
- All commits are tracked in **GitHub repo** with updated pipeline configs

---

## 📜 License

MIT License – feel free to fork, modify and use for learning/demo purposes.


