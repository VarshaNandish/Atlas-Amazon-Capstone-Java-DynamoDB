package com.atlas.testutil;

import com.atlas.dao.LogDao;
import com.atlas.model.StudentLog;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple in-memory LogDao to capture audit events during tests.
 * Appends ISO timestamp strings.
 */
public class InMemoryLogDao implements LogDao {
    private final List<StudentLog> logs = new ArrayList<>();

    @Override
    public void append(String logId, String studentId, String action, String courseId) {
        String iso = Instant.now().toString();
        StudentLog l = new StudentLog(logId, studentId, action, courseId, iso);
        logs.add(l);
    }

    public List<StudentLog> getLogs() {
        return new ArrayList<>(logs);
    }

    public void clear() {
        logs.clear();
    }
}
