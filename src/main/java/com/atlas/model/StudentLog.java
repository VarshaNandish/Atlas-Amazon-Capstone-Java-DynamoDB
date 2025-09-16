package com.atlas.model;

/**
 * StudentLog holds a human-readable ISO timestamp in the 'timestamp' field.
 * This model assumes the DB stores timestamp as ISO string under attribute name "timestamp".
 */
public class StudentLog {
    private String logId;
    private String studentId;
    private String action;
    private String courseId;
    private String timestamp; // ISO-8601 string e.g. 2025-09-16T12:34:56.789Z

    public StudentLog() {}

    public StudentLog(String logId, String studentId, String action, String courseId, String timestamp) {
        this.logId = logId;
        this.studentId = studentId;
        this.action = action;
        this.courseId = courseId;
        this.timestamp = timestamp;
    }

    public String getLogId() { return logId; }
    public String getStudentId() { return studentId; }
    public String getAction() { return action; }
    public String getCourseId() { return courseId; }
    public String getTimestamp() { return timestamp; }
}
