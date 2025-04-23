package com.mvo.edureactiveapi.mapper;

import com.mvo.edureactiveapi.dto.CourseDTO;
import com.mvo.edureactiveapi.entity.Course;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseMapper {
    CourseDTO map(Course course);

    @InheritInverseConfiguration
    Course map(CourseDTO courseDTO);
}
