package com.atlas.tests;

import com.atlas.model.Course;
import com.atlas.model.Student;
import com.atlas.service.CourseService;
import com.atlas.service.EnrollmentService;
import com.atlas.service.StudentService;
import com.atlas.testutil.InMemoryCourseDao;
import com.atlas.testutil.InMemoryLogDao;
import com.atlas.testutil.InMemoryStudentDao;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests using in-memory DAOs (no Dynamo). Covers the required core flows.
 * Password strings updated to be >= 6 chars to satisfy StudentService validation.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EnrollmentFlowsUnitTest {

    private InMemoryStudentDao studentDao;
    private InMemoryCourseDao courseDao;
    private InMemoryLogDao logDao;
    private StudentService studentService;
    private EnrollmentService enrollmentService;
    private CourseService courseService;

    @BeforeAll
    void setup() {
        studentDao = new InMemoryStudentDao();
        courseDao = new InMemoryCourseDao();
        logDao = new InMemoryLogDao();

        studentService = new StudentService(studentDao, logDao);
        enrollmentService = new EnrollmentService(studentDao, courseDao, logDao);
        courseService = new CourseService(courseDao);
    }

    @BeforeEach
    void clearAll() {
        studentDao.clear();
        courseDao.clear();
        logDao.clear();
    }

    @Test
    void signupAndLogin_flow() {
        studentService.register("S1", "Alice", "alice@example.com", "Secret1"); // 7 chars
        String token = studentService.login("alice@example.com", "Secret1");
        assertNotNull(token);
    }

    @Test
    void enroll_whenSeatAvailable() {
        Course c = new Course("C1", "TestCourse", 2);
        courseDao.putCourse(c);

        studentService.register("S2", "Bob", "bob@example.com", "BobPwd1"); // >=6
        enrollmentService.enroll("S2", "C1");

        Student s = studentDao.getById("S2");
        assertTrue(s.getEnrolledCourseIds().contains("C1"));

        Course saved = courseDao.getById("C1");
        assertEquals(1, saved.getCurrentEnrolledCount());
        assertTrue(saved.getEnrolledIds().contains("S2"));
    }

    @Test
    void enroll_whenFull_waitlist() {
        Course c = new Course("C2", "FullCourse", 1);
        courseDao.putCourse(c);

        studentService.register("S3", "Carol", "carol@example.com", "Carol12");
        studentService.register("S4", "Dave", "dave@example.com", "DavePwd2");

        enrollmentService.enroll("S3", "C2"); // takes the seat
        enrollmentService.enroll("S4", "C2"); // should go to waitlist

        Student s4 = studentDao.getById("S4");
        assertTrue(s4.getWaitlistedCourseIds().contains("C2"));

        Course saved = courseDao.getById("C2");
        assertTrue(saved.getWaitlistIds().contains("S4"));
    }

    @Test
    void drop_promotes_from_waitlist() {
        Course c = new Course("C3", "PromoteCourse", 1);
        courseDao.putCourse(c);

        studentService.register("S5", "Eve", "eve@example.com", "EvePwd6");
        studentService.register("S6", "Frank", "frank@example.com", "Frank06");

        enrollmentService.enroll("S5", "C3"); // seat
        enrollmentService.enroll("S6", "C3"); // waitlist

        enrollmentService.drop("S5", "C3");   // should promote S6

        Student promoted = studentDao.getById("S6");
        assertTrue(promoted.getEnrolledCourseIds().contains("C3"));
        assertFalse(promoted.getWaitlistedCourseIds().contains("C3"));

        Course saved = courseDao.getById("C3");
        assertEquals(1, saved.getCurrentEnrolledCount());
        assertTrue(saved.getEnrolledIds().contains("S6"));
    }

    @Test
    void duplicatePrevention_and_limits() {
        // duplicate id/email prevention
        studentService.register("S7", "Gina", "gina@example.com", "GinaPwd7");
        assertThrows(IllegalArgumentException.class, () -> studentService.register("S7", "Gina2", "g2@example.com", "Another1"));
        assertThrows(IllegalArgumentException.class, () -> studentService.register("S8", "Gina3", "gina@example.com", "Another2"));

        // enrollment limit (max 5)
        for (int i = 0; i < 6; i++) courseDao.putCourse(new Course("CX" + i, "C" + i, 5));
        studentService.register("S9", "Henry", "henry@example.com", "Henry99");
        Student s9 = studentDao.getById("S9");
        for (int i = 0; i < 5; i++) s9.getEnrolledCourseIds().add("CX" + i);
        studentDao.save(s9);
        assertThrows(IllegalStateException.class, () -> enrollmentService.enroll("S9", "CX5"));

        // waitlist limit (max 3)
        studentService.register("S10", "Ivy", "ivy@example.com", "IvyPwd0");
        Student s10 = studentDao.getById("S10");
        s10.getWaitlistedCourseIds().add("W1");
        s10.getWaitlistedCourseIds().add("W2");
        s10.getWaitlistedCourseIds().add("W3");
        studentDao.save(s10);

        courseDao.putCourse(new Course("CFull", "FullCourse", 1));
        studentService.register("S11", "Jack", "jack@example.com", "JackPwd1");
        enrollmentService.enroll("S11", "CFull"); // fills the seat

        assertThrows(IllegalStateException.class, () -> enrollmentService.enroll("S10", "CFull"));
    }
}
