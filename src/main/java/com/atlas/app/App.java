package com.atlas.app;

import com.atlas.model.Course;
import com.atlas.service.CourseService;
import com.atlas.service.EnrollmentService;
import com.atlas.service.SessionStore;
import com.atlas.service.StudentService;

import java.util.List;
import java.util.Scanner;

public class App {
    private static final StudentService studentService = new StudentService();
    private static final CourseService courseService = new CourseService();
    private static final EnrollmentService enrollmentService = new EnrollmentService();

    public static void main(String[] args) {
        System.out.println("======= Welcome to Atlas Academy \uD83C\uDF93 =======");
        System.out.println("======= Register for courses, manage your enrollments, and track your waitlists !! =======");
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n1) Sign Up  2) Login  3) Exit");
            System.out.print("Choose: ");
            String ch = sc.nextLine().trim();
            try {
                if ("1".equals(ch)) doSignup(sc);
                else if ("2".equals(ch)) {
                    String token = doLogin(sc);
                    if (token != null) studentMenu(sc, token);
                } else if ("3".equals(ch)) {
                    System.out.println("Session ended. See you next time."); return;
                } else System.out.println("Invalid option");
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }


    private static void doSignup(Scanner sc) {
        System.out.print("StudentId: "); String id = sc.nextLine().trim();
        System.out.print("Name: "); String name = sc.nextLine().trim();
        System.out.print("Email: "); String email = sc.nextLine().trim();
        System.out.print("Password: "); String pwd = sc.nextLine().trim();
        studentService.register(id, name, email, pwd);
        System.out.println("Account created successfully. You may now log in to continue.");
    }

    private static String doLogin(Scanner sc) {
        System.out.print("Email: "); String email = sc.nextLine().trim();
        System.out.print("Password: "); String pwd = sc.nextLine().trim();
        String token = studentService.login(email, pwd);
        System.out.println("Login successful — welcome back!");
        return token;
    }


    private static void studentMenu(Scanner sc, String token) {
        String studentId = SessionStore.getStudentId(token);
        if (studentId == null) { System.out.println("Session invalid."); return; }
        while (true) {
            System.out.println("\n--- Student Menu ---");
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
                    System.out.println("Enroll attempt processed.");
                } else if ("4".equals(ch)) {
                    System.out.print("CourseId: "); String cid = sc.nextLine().trim();
                    enrollmentService.drop(studentId, cid);
                    System.out.println("Drop processed.");
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
