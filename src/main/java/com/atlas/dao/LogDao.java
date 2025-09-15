package com.atlas.dao;

public interface LogDao {
    void append(String logId, String studentId, String action, String courseId);
}
