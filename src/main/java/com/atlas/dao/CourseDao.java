package com.atlas.dao;

import com.atlas.model.Course;
import java.util.List;

public interface CourseDao {
    Course getById(String courseId);
    List<Course> listAll();

    /**
     * Atomically attempts to enroll studentId in courseId if currentEnrolledCount < maxSeats.
     * Returns true if succeeded, false if condition failed (course full).
     */
    boolean enrollStudentAtomic(String courseId, String studentId, int maxSeats);

    void addToWaitlist(String courseId, String studentId);
    void replaceWaitlist(String courseId, List<String> newWaitlist);

    /**
     * Replace enrolledIds list and currentEnrolledCount for course (targeted update).
     */
    void replaceEnrolled(String courseId, List<String> newEnrolledIds, int newCount);
}
