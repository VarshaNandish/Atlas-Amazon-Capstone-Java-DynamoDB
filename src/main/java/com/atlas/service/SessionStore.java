package com.atlas.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionStore {
    private static final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();
    private static final Duration TTL = Duration.ofMinutes(30);

    public static String create(String studentId) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, new SessionInfo(studentId, Instant.now().plus(TTL)));
        return token;
    }

    public static boolean isValid(String token) {
        SessionInfo info = sessions.get(token);
        if (info == null) return false;
        if (Instant.now().isAfter(info.getExpiry())) {
            sessions.remove(token);
            return false;
        }
        return true;
    }

    public static String getStudentId(String token) {
        SessionInfo info = sessions.get(token);
        return info == null ? null : info.getStudentId();
    }

    public static void invalidate(String token) { sessions.remove(token); }
}
