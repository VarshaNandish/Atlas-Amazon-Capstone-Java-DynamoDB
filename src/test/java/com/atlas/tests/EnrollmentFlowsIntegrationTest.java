package com.atlas.tests;

import com.atlas.repository.DynamoCourseDao;
import com.atlas.repository.DynamoLogDao;
import com.atlas.repository.DynamoStudentDao;
import com.atlas.service.CourseService;
import com.atlas.service.EnrollmentService;
import com.atlas.service.StudentService;
import com.atlas.repository.DynamoDBClientUtil;
import org.junit.jupiter.api.*;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests using DynamoDB Local.
 * Each test creates its own course item with all required attributes including a future latestEnrollmentBy.
 */


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EnrollmentFlowsIntegrationTest {

    private DynamoDbClient client;
    private DynamoStudentDao studentDao;
    private DynamoCourseDao courseDao;
    private DynamoLogDao logDao;
    private StudentService studentService;
    private EnrollmentService enrollmentService;
    private CourseService courseService;

    @BeforeAll
    void setup() {
        try {
            client = DynamoDBClientUtil.client();
            client.listTables(); // quick check
        } catch (Exception e) {
            client = null;
        }
        assumeTrue(client != null, "DynamoDB Local is not available; skipping integration tests");

        studentDao = new DynamoStudentDao();
        courseDao = new DynamoCourseDao();
        logDao = new DynamoLogDao();

        studentService = new StudentService(studentDao, logDao);
        enrollmentService = new EnrollmentService(studentDao, courseDao, logDao);
        courseService = new CourseService(courseDao);
    }

    /**
     * Helper: create a full course item in Dynamo with latestEnrollmentBy set to future date.
     * Insert temporary courses to cover edge cases.
     */


    private void putCourseDirectly(String courseId, String courseName, int maxSeats) {
        // ensure latestEnrollmentBy is in the future (30 days from today)
        String futureIso = LocalDate.now().plusDays(30).toString(); // yyyy-MM-dd
        Map<String, AttributeValue> item = Map.of(
                "courseId", AttributeValue.builder().s(courseId).build(),
                "courseName", AttributeValue.builder().s(courseName).build(),
                "maxSeats", AttributeValue.builder().n(String.valueOf(maxSeats)).build(),
                "currentEnrolledCount", AttributeValue.builder().n("0").build(),
                "startDate", AttributeValue.builder().s(LocalDate.now().toString()).build(),
                "endDate", AttributeValue.builder().s(LocalDate.now().plusMonths(3).toString()).build(),
                "latestEnrollmentBy", AttributeValue.builder().s(futureIso).build()
        );
        client.putItem(PutItemRequest.builder().tableName("Courses").item(item).build());
    }

    @Test
    public void integration_signup_and_login() {
        String id = "S" + UUID.randomUUID().toString().substring(0, 8);
        String email = id + "@example.com";
        studentService.register(id, "Alice", email, "Secret1");
        String token = studentService.login(email, "Secret1");
        assertNotNull(token);
    }

    @Test
    public void integration_enroll_whenSeatAvailable() {
        String studentId = "S" + UUID.randomUUID().toString().substring(0, 8);
        String email = studentId + "@example.com";
        studentService.register(studentId, "Bob", email, "Secret2");

        // create a dedicated course for this test
        String courseId = "ITEST1-" + UUID.randomUUID().toString().substring(0, 6);
        putCourseDirectly(courseId, "Integration Course A", 2);

        enrollmentService.enroll(studentId, courseId);

        assertTrue(studentDao.getById(studentId).getEnrolledCourseIds().contains(courseId));
    }

    @Test
    public void integration_enroll_and_waitlist() {
        String s1 = "S" + UUID.randomUUID().toString().substring(0, 8);
        String s2 = "S" + UUID.randomUUID().toString().substring(0, 8);
        studentService.register(s1, "User1", s1 + "@ex.com", "Pass123");
        studentService.register(s2, "User2", s2 + "@ex.com", "Pass456");

        String courseId = "ITEST2-" + UUID.randomUUID().toString().substring(0, 6);
        putCourseDirectly(courseId, "Integration Course B", 1);

        // first takes seat
        enrollmentService.enroll(s1, courseId);
        // second should be waitlisted
        enrollmentService.enroll(s2, courseId);

        assertTrue(studentDao.getById(s1).getEnrolledCourseIds().contains(courseId));
        assertTrue(studentDao.getById(s2).getWaitlistedCourseIds().contains(courseId));
    }

    @Test
    public void integration_drop_promotes_from_waitlist() {
        String s1 = "S" + UUID.randomUUID().toString().substring(0, 8);
        String s2 = "S" + UUID.randomUUID().toString().substring(0, 8);
        studentService.register(s1, "Dropper", s1 + "@ex.com", "Pass123");
        studentService.register(s2, "Waiter", s2 + "@ex.com", "Pass456");

        String courseId = "ITEST3-" + UUID.randomUUID().toString().substring(0, 6);
        putCourseDirectly(courseId, "Integration Course C", 1);

        enrollmentService.enroll(s1, courseId); // seat
        enrollmentService.enroll(s2, courseId); // waitlist

        enrollmentService.drop(s1, courseId); // should auto-promote s2

        assertFalse(studentDao.getById(s1).getEnrolledCourseIds().contains(courseId));
        assertTrue(studentDao.getById(s2).getEnrolledCourseIds().contains(courseId));
    }

    @Test
    public void integration_duplicatePrevention_and_limits() {
        // register a fresh student
        String sid = "S" + UUID.randomUUID().toString().substring(0, 8);
        studentService.register(sid, "LimitUser", sid + "@ex.com", "Pass123");

        // --- Part A: duplicate enrollment prevention ---
        String dupCourse = "ITEST_DUP_" + UUID.randomUUID().toString().substring(0, 6);
        putCourseDirectly(dupCourse, "DupCourse", 5);

        // first enroll should succeed
        enrollmentService.enroll(sid, dupCourse);

        // second enroll into same course should throw AlreadyEnrolledException
        assertThrows(com.atlas.exception.AlreadyEnrolledException.class,
                () -> enrollmentService.enroll(sid, dupCourse),
                "Enrolling twice in the same course should be prevented");

        // --- Part B: max 5 active enrollments limit ---
        // Already 1 enrollment above; add 4 more distinct courses to reach the limit (total = 5)
        for (int i = 0; i < 4; i++) {
            String cid = "ITEST_LIMIT_" + UUID.randomUUID().toString().substring(0, 6) + "_" + i;
            putCourseDirectly(cid, "LimitCourse" + i, 5);
            enrollmentService.enroll(sid, cid); // should succeed until we reach 5 total
        }

        // Now student has 5 active enrollments. Attempting one more should throw IllegalStateException.
        String extra = "ITEST_EXTRA_" + UUID.randomUUID().toString().substring(0, 6);
        putCourseDirectly(extra, "ExtraCourse", 5);
        assertThrows(IllegalStateException.class,
                () -> enrollmentService.enroll(sid, extra),
                "Enrolling beyond 5 active courses should be rejected");
    }

}