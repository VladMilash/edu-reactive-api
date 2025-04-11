package com.mvo.edureactiveapi.mapper;

import com.mvo.edureactiveapi.dto.requestdto.StudentTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseDepartmentDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseStudentDTO;
import com.mvo.edureactiveapi.entity.Student;
import org.mapstruct.Mapper;

import java.util.HashSet;

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
}
