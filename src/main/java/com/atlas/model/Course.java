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

    @Override
    public String toString() {
        return String.format("%s | %s | enrolled: %d/%d | enrollBy: %s",
                courseId, courseName, currentEnrolledCount, maxSeats,
                (latestEnrollmentBy == null || latestEnrollmentBy.isEmpty() ? "-" : latestEnrollmentBy));
    }
}
