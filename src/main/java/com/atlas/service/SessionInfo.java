package com.atlas.service;

import java.time.Instant;

public class SessionInfo {
    private final String studentId;
    private final Instant expiry;

    public SessionInfo(String studentId, Instant expiry) {
        this.studentId = studentId;
        this.expiry = expiry;
    }

    public String getStudentId() { return studentId; }
    public Instant getExpiry() { return expiry; }
}
