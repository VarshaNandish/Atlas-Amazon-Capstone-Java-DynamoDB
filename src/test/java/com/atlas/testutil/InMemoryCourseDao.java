package com.atlas.testutil;

import com.atlas.dao.CourseDao;
import com.atlas.model.Course;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory CourseDao for unit tests.
 * - putCourse(...) allows tests to preload a course (mimics pre-seeded Courses table).
 * - enrollStudentAtomic / waitlist methods are synchronized for simple safety in tests.
 */
public class InMemoryCourseDao implements CourseDao {
    private final Map<String, Course> store = new ConcurrentHashMap<>();

    /**
     * Put a Course instance into the in-memory store.
     * Tests should call this to simulate pre-seeded courses.
     */
    public void putCourse(Course c) {
        // store the provided course instance (tests may pass new Course(...))
        store.put(c.getCourseId(), c);
    }

    @Override
    public Course getById(String courseId) {
        return store.get(courseId);
    }

    @Override
    public List<Course> listAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public synchronized boolean enrollStudentAtomic(String courseId, String studentId, int maxSeats) {
        Course c = store.get(courseId);
        if (c == null) return false;
        if (c.getCurrentEnrolledCount() < maxSeats) {
            c.getEnrolledIds().add(studentId);
            c.setCurrentEnrolledCount(c.getCurrentEnrolledCount() + 1);
            return true;
        }
        return false;
    }

    @Override
    public synchronized void addToWaitlist(String courseId, String studentId) {
        Course c = store.get(courseId);
        if (c == null) return;
        c.getWaitlistIds().add(studentId);
    }

    @Override
    public synchronized void replaceWaitlist(String courseId, List<String> newWaitlist) {
        Course c = store.get(courseId);
        if (c == null) return;
        c.getWaitlistIds().clear();
        c.getWaitlistIds().addAll(newWaitlist);
    }

    @Override
    public synchronized void replaceEnrolled(String courseId, List<String> newEnrolledIds, int newCount) {
        Course c = store.get(courseId);
        if (c == null) return;
        c.getEnrolledIds().clear();
        c.getEnrolledIds().addAll(newEnrolledIds);
        c.setCurrentEnrolledCount(newCount);
    }

    // test helper
    public void clear() {
        store.clear();
    }
}
