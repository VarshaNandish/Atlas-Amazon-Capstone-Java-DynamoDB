package com.atlas.service;

import com.atlas.dao.CourseDao;
import com.atlas.model.Course;

import java.util.List;

/**
 * Small wrapper over CourseDao interface.
 */
public class CourseService {
    private final CourseDao dao;

    // constructor injection only
    public CourseService(CourseDao dao) { this.dao = dao; }

    public Course get(String id) { return dao.getById(id); }
    public List<Course> list() { return dao.listAll(); }
}
