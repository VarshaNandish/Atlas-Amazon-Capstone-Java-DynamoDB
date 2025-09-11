# Student Course Registration System

## Overview
This is a **console-based Student Course Registration System** implemented in Java (Maven project) with DynamoDB Local as the database.  
It allows students to sign up, log in, view available courses, enroll, drop, and manage waitlists. All key actions are logged in an audit log.

### Features
- Student sign up & login (passwords hashed with bcrypt)
- Profile view with enrollments, waitlists, dropped/completed courses
- Preloaded courses stored in DynamoDB Local
- Course enrollment with limits and cutoffs
- FIFO waitlist management with auto-promotion
- Drop courses with waitlist promotion
- Audit logging of all actions (DynamoDB `StudentLogs` table)
- Session handling with in-memory tokens (expiry 30 mins)
- JUnit tests for core flows

---

## Requirements
- **Java 17**
- **Maven 3.9+**
- **AWS CLI**
- **DynamoDB Local** (running on `http://localhost:8000`)

---

## Setup Instructions

### 1. Clone the repository
```bash
git clone <your-repo-url>
cd student-course-registration
```

### 2. Start DynamoDB Local
From the folder where you downloaded DynamoDB Local:
```powershell
java "-Djava.library.path=./DynamoDBLocal_lib" -jar ./DynamoDBLocal.jar -sharedDb -dbPath ./local_db
```
*(or use `-inMemory` for temporary DB with no persistence)*

### 3. Create tables
```powershell
aws dynamodb create-table --table-name Students --attribute-definitions AttributeName=id,AttributeType=S --key-schema AttributeName=id,KeyType=HASH --billing-mode PAY_PER_REQUEST --endpoint-url http://localhost:8000 --region ap-south-1

aws dynamodb create-table --table-name Courses --attribute-definitions AttributeName=courseId,AttributeType=S --key-schema AttributeName=courseId,KeyType=HASH --billing-mode PAY_PER_REQUEST --endpoint-url http://localhost:8000 --region ap-south-1

aws dynamodb create-table --table-name StudentLogs --attribute-definitions AttributeName=logId,AttributeType=S --key-schema AttributeName=logId,KeyType=HASH --billing-mode PAY_PER_REQUEST --endpoint-url http://localhost:8000 --region ap-south-1
```

### 4. Build and run
```bash
mvn clean install
mvn compile exec:java -Dexec.mainClass="com.academy.app.App"
```

---

## Project Structure
```
student-course-registration/
├── pom.xml
├── README.md
├── src/
│   ├── main/java/com/academy/
│   │   ├── app/        # Main CLI (App.java)
│   │   ├── dao/        # DynamoDBClientUtil, StudentDao, CourseDao, LogDao
│   │   ├── exception/  # Custom exceptions
│   │   ├── model/      # Student, Course, StudentLog, etc.
│   │   └── service/    # StudentService, CourseService, EnrollmentService, etc.
│   └── test/java/com/academy/ # Unit tests
└── .gitignore
```

---

## Testing
Run unit tests:
```bash
mvn test
```

At least 5 JUnit tests are provided for:
- Signup & login
- Enroll (available seat)
- Enroll (waitlist)
- Drop (with promotion)
- Duplicate/limit checks

---

## Demo Script (for examiners/trainers)
1. Start DynamoDB Local and create tables.  
2. Run the app.  
3. Sign up 3 students.  
4. Enroll 2 in a course with 2 seats.  
5. Try enrolling a 3rd → goes to waitlist.  
6. Drop one enrolled → waitlisted student auto-enrolled.  
7. Show audit logs in `StudentLogs` table.

---

## License
Educational use only. For capstone/demo purposes.
