package com.mvo.edureactiveapi.service.impl;

import com.mvo.edureactiveapi.entity.Course;
import com.mvo.edureactiveapi.repository.CourseRepository;
import com.mvo.edureactiveapi.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;

    @Override
    public Flux<Course> getCoursesWithTeachersByStudentId(Long studentId) {
        return null;
    }
}
