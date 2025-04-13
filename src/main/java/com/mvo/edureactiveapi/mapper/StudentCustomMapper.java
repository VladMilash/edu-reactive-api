package com.mvo.edureactiveapi.mapper;

import com.mvo.edureactiveapi.dto.CourseDTO;
import com.mvo.edureactiveapi.dto.TeacherDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseStudentDTO;
import io.r2dbc.spi.Row;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.function.BiFunction;

@Slf4j
@Component
public class StudentCustomMapper implements BiFunction<Row, Object, ResponseStudentDTO> {
    @Override
    public ResponseStudentDTO apply(Row row, Object o) {
        Long studentId = row.get("student_id", Long.class);
        String studentName = row.get("student_name", String.class);
        String studentEmail = row.get("student_email", String.class);

        Set<CourseDTO> courses = new HashSet<>();
        if (row.get("course_id", Long.class) != null) {
            CourseDTO courseDTO = new CourseDTO(
                row.get("course_id", Long.class),
                row.get("course_title", String.class),
                row.get("teacher_id", Long.class) != null
                    ? new TeacherDTO(
                    row.get("teacher_id", Long.class),
                    row.get("teacher_name", String.class)
                ) : null
            );
            courses.add(courseDTO);
        }
        return new ResponseStudentDTO(studentId, studentName, studentEmail, courses);
    }

    public Flux<ResponseStudentDTO> processing(Flux<ResponseStudentDTO> students) {
        log.info("Starting processing");
        return students.groupBy(ResponseStudentDTO::id)
            .flatMap(group -> group.reduce((existingStudent, newStudent) -> {
                existingStudent.courses().addAll(newStudent.courses());
                return existingStudent;
            }));
    }

//    public Flux<ResponseStudentDTO> processing(Flux<ResponseStudentDTO> students) {
//        return students.groupBy(ResponseStudentDTO::id)
//            .flatMap(group -> group.reduce((existingStudent, newStudent) -> {
//                Set<CourseDTO> mergedCourse = existingStudent.courses();
//                mergedCourse.addAll(newStudent.courses());
//                return new ResponseStudentDTO(
//                    existingStudent.id(),
//                    existingStudent.name(),
//                    existingStudent.email(),
//                    mergedCourse
//                );
//            }));
//    }

}
