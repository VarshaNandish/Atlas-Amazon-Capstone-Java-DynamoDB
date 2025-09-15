package com.atlas.dao;

import com.atlas.model.Student;
import java.util.List;

public interface StudentDao {
    void save(Student s);
    Student getById(String id);
    Student findByEmail(String email);
    List<Student> listAll();
}
