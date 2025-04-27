package com.mvo.edureactiveapi.mapper;

import com.mvo.edureactiveapi.dto.TeacherDTO;
import com.mvo.edureactiveapi.dto.requestdto.TeacherTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseTeacherDTO;
import com.mvo.edureactiveapi.entity.Teacher;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

import java.util.HashSet;

@Mapper(componentModel = "spring")
public interface TeacherMapper {
    TeacherDTO map(Teacher teacher);

    @InheritInverseConfiguration
    Teacher map(TeacherDTO teacherDTO);

    default Teacher fromTeacherTransientDTO(TeacherTransientDTO teacherTransientDTO) {
        return new Teacher(
            null,
            teacherTransientDTO.name()
        );
    }

    default ResponseTeacherDTO toResponseTeacherDTO(Teacher teacher) {
        return new ResponseTeacherDTO(
            teacher.getId(),
            teacher.getName(),
            new HashSet<>(),
            null
        );
    }

}
