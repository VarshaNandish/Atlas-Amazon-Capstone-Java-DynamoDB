package com.atlas.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Student {
    private String id;
    private String name;
    private String email;
    private String passwordHash;
    private final List<String> enrolledCourseIds = new ArrayList<>();
    private final List<String> waitlistedCourseIds = new ArrayList<>();

    public Student() {}

    public Student(String id, String name, String email, String passwordHash) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.email = Objects.requireNonNull(email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public List<String> getEnrolledCourseIds() { return enrolledCourseIds; }
    public List<String> getWaitlistedCourseIds() { return waitlistedCourseIds; }

    @Override
    public String toString() {
        return String.format("ID: %s | Name: %s | Email: %s", id, name, email);
    }
}
