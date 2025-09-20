package com.atlas.app;


import com.atlas.model.Course;
import com.atlas.service.CourseService;
import com.atlas.service.EnrollmentService;
import com.atlas.service.SessionStore;
import com.atlas.service.StudentService;
import com.atlas.repository.DynamoStudentDao;
import com.atlas.repository.DynamoCourseDao;
import com.atlas.repository.DynamoLogDao;

import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        // explicit wiring (composition root)
        DynamoStudentDao studentDaoImpl = new DynamoStudentDao();
        DynamoCourseDao courseDaoImpl = new DynamoCourseDao();
        DynamoLogDao logDaoImpl = new DynamoLogDao();

        StudentService studentService = new StudentService(studentDaoImpl, logDaoImpl);
        CourseService courseService = new CourseService(courseDaoImpl);
        EnrollmentService enrollmentService = new EnrollmentService(studentDaoImpl, courseDaoImpl, logDaoImpl);

        System.out.println("=== Welcome to Atlas Academy 🎓 ===");
        System.out.println("=== Student Course Registration System ===");

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n1) Sign Up  2) Login  3) Exit");
            System.out.print("Choose: ");
            String ch = sc.nextLine().trim();
            try {
                if ("1".equals(ch)) doSignup(sc, studentService);
                else if ("2".equals(ch)) {
                    String token = doLogin(sc, studentService);
                    if (token != null) studentMenu(sc, token, studentService, courseService, enrollmentService);
                } else if ("3".equals(ch)) {
                    System.out.println("Goodbye — thanks for using Atlas Academy!");
                    com.atlas.repository.DynamoDBClientUtil.closeClient();
                    return;
                } else System.out.println("Invalid option");
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private static void doSignup(Scanner sc, StudentService studentService) {
        System.out.print("StudentId: "); String id = sc.nextLine().trim();
        System.out.print("Name: "); String name = sc.nextLine().trim();
        System.out.print("Email: "); String email = sc.nextLine().trim();
        System.out.print("Password: "); String pwd = sc.nextLine().trim();
        studentService.register(id, name, email, pwd);
        System.out.println("Account created successfully. You may now log in to continue.");
    }

    private static String doLogin(Scanner sc, StudentService studentService) {
        System.out.print("Email: "); String email = sc.nextLine().trim();
        System.out.print("Password: "); String pwd = sc.nextLine().trim();
        String token = studentService.login(email, pwd);
        System.out.println("Login successful — welcome back!");
        return token;
    }

    private static void studentMenu(Scanner sc, String token, StudentService studentService, CourseService courseService, EnrollmentService enrollmentService) {
        // Check token validity (enforces expiry)
        if (!SessionStore.isValid(token)) {
            System.out.println("Session invalid or expired. Please login again.");
            return;
        }
        String studentId = SessionStore.getStudentId(token);
        if (studentId == null) { System.out.println("Session invalid."); return; }
        while (true) {

            // enforce expiry before each interaction
            if (!SessionStore.isValid(token)) {
                System.out.println("Session expired. Please login again.");
                return;
            }
            System.out.println("\n----- Student Menu -----");
            System.out.println("1) View courses  2) View profile  3) Enroll  4) Drop  5) Logout");
            System.out.print("Choose: ");
            String ch = sc.nextLine().trim();
            try {
                if ("1".equals(ch)) {
                    List<Course> courses = courseService.list();
                    courses.forEach(System.out::println);
                } else if ("2".equals(ch)) {
                    String profileText = studentService.profile(studentId);
                    System.out.println(profileText);
                } else if ("3".equals(ch)) {
                    System.out.print("CourseId: "); String cid = sc.nextLine().trim();
                    enrollmentService.enroll(studentId, cid);
                    System.out.println("Enroll attempt processed. Please view your profile details to check the updated status.");
                } else if ("4".equals(ch)) {
                    System.out.print("CourseId: "); String cid = sc.nextLine().trim();
                    enrollmentService.drop(studentId, cid);
                    System.out.println("Drop processed. Please view your profile details to check the updated status.");
                } else if ("5".equals(ch)) {
                    SessionStore.invalidate(token);
                    System.out.println("You have been logged out. See you soon!");
                    return;
                } else System.out.println("Invalid option");
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }
}
