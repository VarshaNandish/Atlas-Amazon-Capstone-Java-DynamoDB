package com.atlas.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Course {
    private String courseId;
    private String courseName;
    private int maxSeats;
    private int currentEnrolledCount = 0;
    private final List<String> enrolledIds = new ArrayList<>();
    private final List<String> waitlistIds = new ArrayList<>();
    private String startDate;         // yyyy-MM-dd
    private String endDate;           // yyyy-MM-dd
    private String latestEnrollmentBy;// yyyy-MM-dd

    public Course() {}

    public Course(String courseId, String courseName, int maxSeats) {
        this.courseId = Objects.requireNonNull(courseId);
        this.courseName = Objects.requireNonNull(courseName);
        this.maxSeats = maxSeats;
    }

    public String getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public int getMaxSeats() { return maxSeats; }
    public int getCurrentEnrolledCount() { return currentEnrolledCount; }
    public void setCurrentEnrolledCount(int currentEnrolledCount) { this.currentEnrolledCount = currentEnrolledCount; }
    public List<String> getEnrolledIds() { return enrolledIds; }
    public List<String> getWaitlistIds() { return waitlistIds; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public String getLatestEnrollmentBy() { return latestEnrollmentBy; }
    public void setLatestEnrollmentBy(String latestEnrollmentBy) { this.latestEnrollmentBy = latestEnrollmentBy; }

    /**
     * Display-friendly string used by CLI.
     * Shows: courseId | courseName | enrolled: current/max | remaining: X | startDate | endDate | enrollBy
     * Empty dates are shown as '-'.
     */
    @Override
    public String toString() {
        String start = (startDate == null || startDate.isEmpty()) ? "-" : startDate;
        String end = (endDate == null || endDate.isEmpty()) ? "-" : endDate;
        String enrollBy = (latestEnrollmentBy == null || latestEnrollmentBy.isEmpty()) ? "-" : latestEnrollmentBy;
        int remaining = Math.max(0, maxSeats - currentEnrolledCount);
        return String.format("%s | %s | enrolled seats: %d/%d | remaining seats: %d | start date: %s | end date: %s | enrollBy date: %s",
                courseId, courseName, currentEnrolledCount, maxSeats, remaining, start, end, enrollBy);
    }
}
