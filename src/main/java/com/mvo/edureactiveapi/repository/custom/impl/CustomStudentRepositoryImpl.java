package com.mvo.edureactiveapi.repository.custom.impl;

import com.mvo.edureactiveapi.dto.responsedto.ResponseStudentDTO;
import com.mvo.edureactiveapi.mapper.StudentCustomMapper;
import com.mvo.edureactiveapi.mapper.StudentMapper;
import com.mvo.edureactiveapi.repository.custom.CustomStudentRepository;
import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Repository
public class CustomStudentRepositoryImpl implements CustomStudentRepository {
    private final DatabaseClient databaseClient;
    private final StudentCustomMapper studentCustomMapper;
    private final StudentMapper studentMapper;

    @Override
    public Flux<ResponseStudentDTO> getAllWithCoursesAndTeacher() {
        String query = """
            SELECT s.id AS student_id, s.name AS student_name, 
                   s.email AS student_email, c.id AS course_id, 
                   c.title AS course_title, t.id AS teacher_id, t.name AS teacher_name
            FROM Student s
            LEFT JOIN student_course s_c ON s.id = s_c.student_id
            LEFT JOIN course c ON s_c.course_id = c.id 
            LEFT JOIN teacher t ON c.teacher_id = t.id
            """;
        Flux<ResponseStudentDTO> result = databaseClient
            .sql(query)
            .map(studentCustomMapper::apply)
            .all();
        return studentCustomMapper.processing(result);
    }
}
