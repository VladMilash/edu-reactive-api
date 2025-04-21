package com.mvo.edureactiveapi.mapper;

import com.mvo.edureactiveapi.dto.TeacherDTO;
import com.mvo.edureactiveapi.entity.Teacher;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TeacherMapper {
    TeacherDTO map(Teacher teacher);

    @InheritInverseConfiguration
    Teacher map(TeacherDTO teacherDTO);

}
