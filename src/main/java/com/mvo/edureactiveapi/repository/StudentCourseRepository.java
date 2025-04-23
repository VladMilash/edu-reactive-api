package com.mvo.edureactiveapi.repository;

import com.mvo.edureactiveapi.entity.StudentCourse;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface StudentCourseRepository extends R2dbcRepository<StudentCourse, Long> {

    Flux<StudentCourse> findAllByStudentIdIn(List<Long> studentIds);

    Flux<StudentCourse> findAllByStudentId(Long id);

    Mono<StudentCourse> findAllByStudentIdAndCourseIdIs(Long studentId, Long courseId);

}
