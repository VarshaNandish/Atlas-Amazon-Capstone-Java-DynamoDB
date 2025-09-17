package com.atlas.testutil;

import com.atlas.dao.StudentDao;
import com.atlas.model.Student;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of StudentDao for unit tests.
 * Stores Student objects in a concurrent map. All email lookups use normalized (trim+lowercase) matching.
 */
public class InMemoryStudentDao implements StudentDao {
    private final Map<String, Student> store = new ConcurrentHashMap<>();

    @Override
    public void save(Student s) {
        // store a shallow copy to mimic persistence semantics
        Student copy = new Student(s.getId(), s.getName(), s.getEmail(), s.getPasswordHash());
        copy.getEnrolledCourseIds().addAll(s.getEnrolledCourseIds());
        copy.getWaitlistedCourseIds().addAll(s.getWaitlistedCourseIds());
        store.put(copy.getId(), copy);
    }

    @Override
    public Student getById(String id) {
        return store.get(id);
    }

    @Override
    public Student findByEmail(String email) {
        if (email == null) return null;
        String normalized = email.trim().toLowerCase();
        return store.values().stream()
                .filter(s -> s.getEmail() != null && s.getEmail().trim().toLowerCase().equals(normalized))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Student> listAll() {
        return store.values().stream().map(s -> getById(s.getId())).collect(Collectors.toList());
    }

    // test helper
    public void clear() {
        store.clear();
    }
}

