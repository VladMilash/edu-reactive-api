package com.mvo.edureactiveapi.service.util;

import com.mvo.edureactiveapi.dto.*;
import com.mvo.edureactiveapi.dto.responsedto.ResponseCoursesDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseDepartmentDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseStudentDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseTeacherDTO;
import com.mvo.edureactiveapi.entity.Course;
import com.mvo.edureactiveapi.entity.Department;
import com.mvo.edureactiveapi.entity.Student;
import com.mvo.edureactiveapi.entity.Teacher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

public final class ResponseDtoBuilder {

    private ResponseDtoBuilder() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static ResponseTeacherDTO getResponseTeacherDTO(Teacher teacher,
                                                           Set<CourseShortDTO> courseShortDTOs,
                                                           DepartmentShortDTO departmentShortDTO) {
        return new ResponseTeacherDTO(
            teacher.getId(),
            teacher.getName(),
            courseShortDTOs,
            departmentShortDTO.id() != null ? departmentShortDTO : null
        );
    }

    public static ResponseCoursesDTO getResponseCoursesDTO(Course course,
                                                           TeacherDTO teacherDTO,
                                                           Set<StudentDTO> studentDTOs) {
        return new ResponseCoursesDTO(
            course.getId(),
            course.getTitle(),
            course.getTeacherId() != null ? teacherDTO : null,
            studentDTOs
        );
    }

    public static Mono<ResponseDepartmentDTO> getResponseDepartmentDTOMono(Department department,
                                                                           Mono<TeacherDTO> teacherDTOMono) {
        return teacherDTOMono.map(teacherDTO -> new ResponseDepartmentDTO(
            department.getId(),
            department.getName(),
            department.getHeadOfDepartment() != null ? teacherDTO : null
        ));
    }

    public static Mono<ResponseStudentDTO> getResponseStudentDTOMono(Student student, Flux<CourseDTO> courseDTOFlux) {
        return courseDTOFlux.collect(Collectors.toSet())
            .map(courseDTOs -> new ResponseStudentDTO(
                student.getId(),
                student.getName(),
                student.getEmail(),
                courseDTOs
            ));
    }

}
