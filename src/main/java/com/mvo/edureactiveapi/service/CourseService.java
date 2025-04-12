package com.mvo.edureactiveapi.service;

import com.mvo.edureactiveapi.entity.Course;
import reactor.core.publisher.Flux;

public interface CourseService {
    Flux<Course> getCoursesWithTeachersByStudentId(Long studentId);
}
