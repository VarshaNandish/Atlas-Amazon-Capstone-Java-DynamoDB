package com.atlas.model;

public class StudentLog {
    private String logId;
    private String studentId;
    private String action;
    private String courseId;
    private long timestamp;

    public StudentLog() {}

    public StudentLog(String logId, String studentId, String action, String courseId, long timestamp) {
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
    public long getTimestamp() { return timestamp; }
}
