package com.atlas.service;

import com.atlas.dao.CourseDao;
import com.atlas.model.Course;

import java.util.List;

public class CourseService {
    private final CourseDao dao = new CourseDao();

    public Course get(String id) { return dao.getById(id); }
    public List<Course> list() { return dao.listAll(); }
}

