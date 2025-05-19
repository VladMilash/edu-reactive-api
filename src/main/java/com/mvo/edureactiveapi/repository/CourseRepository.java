package com.mvo.edureactiveapi.repository;

import com.mvo.edureactiveapi.entity.Course;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.List;

@Repository
public interface CourseRepository extends R2dbcRepository<Course, Long> {

    Flux<Course> findAllByIdIn(List<Long> courseIds);

    Flux<Course> findAllByTeacherId(Long teacherId);

    @Query("SELECT * FROM course ORDER BY id LIMIT :limit OFFSET :offset")
    Flux<Course> findAllWithPagination(long limit, long offset);

}
