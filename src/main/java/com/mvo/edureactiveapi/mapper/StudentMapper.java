package com.mvo.edureactiveapi.mapper;
import com.mvo.edureactiveapi.dto.requestdto.StudentTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseStudentDTO;
import com.mvo.edureactiveapi.entity.Student;
import org.mapstruct.Mapper;

import java.util.*;


@Mapper(componentModel = "spring")
public interface StudentMapper {
    Student map(StudentTransientDTO studentTransientDTO);

    default ResponseStudentDTO toResponseStudentDTO(Student student) {
        return new ResponseStudentDTO(
            student.getId(),
            student.getName(),
            student.getEmail(),
            new HashSet<>()
        );
    }

//    default Flux<ResponseStudentDTO> map(Flux<Row> rowFlux) {
//        Map<Long, ResponseStudentDTO> map = new HashMap<>();
//        return rowFlux
//            .collectList()
//            .flatMapMany(rows -> {
//                for (Row row : rows) {
//                    Long studentId = row.get("student_id", Long.class);
//                    if (!map.containsKey(studentId)) {
//                        String studentName = row.get("student_name", String.class);
//                        String studentEmail = row.get("student_email", String.class);
//                        Set<CourseDTO> courses = new HashSet<>();
//                        Long courseId = row.get("course_id", Long.class);
//                        if (courseId != null) {
//                            CourseDTO courseDTO = new CourseDTO(
//                                courseId,
//                                row.get("course_title", String.class),
//                                row.get("teacher_id", Long.class) != null
//                                    ? new TeacherDTO(
//                                    row.get("teacher_id", Long.class),
//                                    row.get("teacher_name", String.class)
//                                ) : null
//                            );
//                            courses.add(courseDTO);
//                        }
//                        ResponseStudentDTO responseStudentDTO = new ResponseStudentDTO(
//                            studentId,
//                            studentName,
//                            studentEmail,
//                            courses);
//                        map.put(responseStudentDTO.id(), responseStudentDTO);
//                    } else {
//                        Long courseId = row.get("course_id", Long.class);
//                        if (courseId != null) {
//                            CourseDTO courseDTO = new CourseDTO(
//                                courseId,
//                                row.get("course_title", String.class),
//                                row.get("teacher_id", Long.class) != null
//                                    ? new TeacherDTO(
//                                    row.get("teacher_id", Long.class),
//                                    row.get("teacher_name", String.class)
//                                ) : null
//                            );
//                            map.get(studentId).courses().add(courseDTO);
//                        }
//                    }
//                }
//                return Flux.fromIterable(map.values());
//            });
//    }

}
