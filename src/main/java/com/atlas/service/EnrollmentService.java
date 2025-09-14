package com.atlas.service;

import com.atlas.dao.CourseDao;
import com.atlas.dao.LogDao;
import com.atlas.dao.StudentDao;
import com.atlas.exception.*;
import com.atlas.model.Course;
import com.atlas.model.Student;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class EnrollmentService {
    private final StudentDao studentDao = new StudentDao();
    private final CourseDao courseDao = new CourseDao();
    private final LogDao logDao = new LogDao();
    private final DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_DATE;

    public void enroll(String studentId, String courseId) {
        Student s = studentDao.getById(studentId);
        if (s == null) throw new StudentNotFoundException(studentId);
        Course c = courseDao.getById(courseId);
        if (c == null) throw new CourseNotFoundException(courseId);

        LocalDate today = LocalDate.now();
        if (c.getLatestEnrollmentBy() != null && !c.getLatestEnrollmentBy().isEmpty() && today.isAfter(LocalDate.parse(c.getLatestEnrollmentBy(), df)))
            throw new IllegalStateException("Enrollment closed for " + courseId);

        if (s.getEnrolledCourseIds().contains(courseId)) throw new AlreadyEnrolledException("Already enrolled");
        if (s.getWaitlistedCourseIds().contains(courseId)) throw new AlreadyEnrolledException("Already waitlisted");

        if (s.getEnrolledCourseIds().size() >= 5) throw new IllegalStateException("Max 5 active enrollments reached");

        boolean success = courseDao.enrollStudentAtomic(courseId, studentId, c.getMaxSeats());
        if (success) {
            s.getEnrolledCourseIds().add(courseId);
            studentDao.save(s);
            logDao.append(UUID.randomUUID().toString(), studentId, "ENROLL", courseId);
            return;
        }

        // course full -> waitlist
        if (s.getWaitlistedCourseIds().size() >= 3) throw new IllegalStateException("Max 3 waitlists reached");
        courseDao.addToWaitlist(courseId, studentId);
        s.getWaitlistedCourseIds().add(courseId);
        studentDao.save(s);
        logDao.append(UUID.randomUUID().toString(), studentId, "WAITLIST_JOIN", courseId);
    }

    public void drop(String studentId, String courseId) {
        Student s = studentDao.getById(studentId);
        if (s == null) throw new StudentNotFoundException(studentId);
        Course c = courseDao.getById(courseId);
        if (c == null) throw new CourseNotFoundException(courseId);

        LocalDate today = LocalDate.now();
        if (c.getEndDate() != null && !c.getEndDate().isEmpty() && today.isAfter(LocalDate.parse(c.getEndDate(), df)))
            throw new IllegalStateException("Cannot drop after end date");

        boolean wasEnrolled = s.getEnrolledCourseIds().remove(courseId);
        if (wasEnrolled) {
            // rebuild course - remove student from enrolledIds & decrement count
            Course fresh = courseDao.getById(courseId);
            boolean removed = fresh.getEnrolledIds().remove(studentId);
            if (removed) {
                int newCount = Math.max(0, fresh.getCurrentEnrolledCount() - 1);
                List<String> newEnrolled = new ArrayList<>(fresh.getEnrolledIds());
                // Persist only the enrollment-related fields (no full item save)
                courseDao.replaceEnrolled(courseId, newEnrolled, newCount);
            }
            studentDao.save(s);
            logDao.append(UUID.randomUUID().toString(), studentId, "DROP", courseId);
            promoteFromWaitlist(courseId);
            return;
        }


        // if on waitlist
        boolean wasWaitlisted = s.getWaitlistedCourseIds().remove(courseId);
        if (wasWaitlisted) {
            Course fresh = courseDao.getById(courseId);
            List<String> newWait = new ArrayList<>(fresh.getWaitlistIds());
            newWait.removeIf(id -> id.equals(studentId));
            courseDao.replaceWaitlist(courseId, newWait);
            studentDao.save(s);
            logDao.append(UUID.randomUUID().toString(), studentId, "WAITLIST_OPT_OUT", courseId);
            return;
        }

        throw new IllegalStateException("Student not enrolled or waitlisted for " + courseId);
    }

    private void promoteFromWaitlist(String courseId) {
        Course c = courseDao.getById(courseId);
        if (c == null) return;
        List<String> wait = new ArrayList<>(c.getWaitlistIds());
        for (String candidateId : wait) {
            Student cand = studentDao.getById(candidateId);
            if (cand == null) continue;
            if (cand.getEnrolledCourseIds().size() >= 5) continue; // skip but keep in place
            boolean ok = courseDao.enrollStudentAtomic(courseId, candidateId, c.getMaxSeats());
            if (ok) {
                cand.getEnrolledCourseIds().add(courseId);
                cand.getWaitlistedCourseIds().remove(courseId);
                studentDao.save(cand);
                // rebuild waitlist removing candidate
                List<String> newWait = new ArrayList<>(c.getWaitlistIds());
                newWait.remove(candidateId);
                courseDao.replaceWaitlist(courseId, newWait);
                logDao.append(UUID.randomUUID().toString(), candidateId, "AUTO_ENROLL", courseId);
                return; // only one promotion per seat opening
            }
        }
    }
}
