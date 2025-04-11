package com.mvo.edureactiveapi.dto.responsedto;

import com.mvo.edureactiveapi.dto.CourseDTO;
import com.mvo.edureactiveapi.dto.DepartmentDTO;

import java.util.Set;

public record ResponseTeacherDTO
    (Long id, String name, Set<CourseDTO> courses, DepartmentDTO department){
}
