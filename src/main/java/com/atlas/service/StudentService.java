package com.atlas.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.atlas.dao.LogDao;
import com.atlas.dao.StudentDao;
import com.atlas.exception.AuthenticationException;
import com.atlas.exception.StudentNotFoundException;
import com.atlas.model.Student;

import java.util.UUID;

public class StudentService {
    private final StudentDao studentDao = new StudentDao();
    private final LogDao logDao = new LogDao();

    public void register(String id, String name, String email, String rawPassword) {

        if (id == null || id.isBlank()) throw new IllegalArgumentException("StudentId required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name required");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email required");
        if (rawPassword == null || rawPassword.length() < 6) throw new IllegalArgumentException("Password too short");

        email = email.trim().toLowerCase(); // normalize
        if (studentDao.getById(id) != null) throw new IllegalArgumentException("StudentId already exists");
        if (studentDao.findByEmail(email) != null) throw new IllegalArgumentException("Email already used");
        String hash = BCrypt.withDefaults().hashToString(10, rawPassword.toCharArray());
        Student s = new Student(id, name, email, hash);
        studentDao.save(s);
        logDao.append(UUID.randomUUID().toString(), id, "SIGNUP", null);
    }

    public String login(String email, String rawPassword) {

        if (email == null || rawPassword == null) throw new AuthenticationException("Invalid credentials");
        email = email.trim().toLowerCase(); // normalize

        Student s = studentDao.findByEmail(email);

        if (s == null) throw new AuthenticationException("Invalid credentials");
        BCrypt.Result r = BCrypt.verifyer().verify(rawPassword.toCharArray(), s.getPasswordHash());
        if (!r.verified) throw new AuthenticationException("Invalid credentials");
        String token = SessionStore.create(s.getId());  // still use studentId for session
        logDao.append(UUID.randomUUID().toString(), s.getId(), "LOGIN", null);
        return token;
    }


    public String profile(String id) {
        Student s = studentDao.getById(id);
        if (s == null) throw new StudentNotFoundException("Student with ID " + id + " not found");

        return String.format(
                "ID: %s\nName: %s\nEmail: %s\nEnrolled: %s\nWaitlisted: %s",
                s.getId(),
                s.getName(),
                s.getEmail(),
                s.getEnrolledCourseIds().toString(),
                s.getWaitlistedCourseIds().toString()
        );
    }

}
