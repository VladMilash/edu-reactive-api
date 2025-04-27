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
    @Query("""
        SELECT c.id, c.title, t.id, t.name
        FROM student_course s_c
        LEFT JOIN course c ON c.id = s_c.course_id
        LEFT JOIN teacher t ON c.teacher_id = t.id
        WHERE s_c.student_id = :student_id
        """)
    Flux<Course> getCoursesWithTeachersByStudentId(Long studentId);

    Flux<Course> findAllByIdIn(List<Long> courseIds);

    Flux<Course> findAllByTeacherId(Long teacherId);

    Flux<Course> findAllByTeacherIdIn(List<Long> teacherIds);

}
