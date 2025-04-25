package com.mvo.edureactiveapi.mapper;

import com.mvo.edureactiveapi.dto.CourseDTO;
import com.mvo.edureactiveapi.dto.requestdto.CourseTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseCoursesDTO;
import com.mvo.edureactiveapi.entity.Course;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

import java.util.HashSet;

@Mapper(componentModel = "spring")
public interface CourseMapper {
    CourseDTO map(Course course);

    @InheritInverseConfiguration
    Course map(CourseDTO courseDTO);

    default  Course fromCourseTransientDTO(CourseTransientDTO courseTransientDTO) {
        return new Course(
            null,
            courseTransientDTO.title(),
            null
        );
    }

    default ResponseCoursesDTO toResponseCoursesDTO(Course course) {
        return new ResponseCoursesDTO(
            course.getId(),
            course.getTitle(),
            null,
            new HashSet<>()
        );
    }
}
