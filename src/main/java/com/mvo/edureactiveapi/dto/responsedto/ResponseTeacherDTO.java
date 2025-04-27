package com.mvo.edureactiveapi.dto.responsedto;

import com.mvo.edureactiveapi.dto.CourseDTO;
import com.mvo.edureactiveapi.dto.CourseShortDTO;
import com.mvo.edureactiveapi.dto.DepartmentDTO;
import com.mvo.edureactiveapi.dto.DepartmentShortDTO;

import java.util.Set;

public record ResponseTeacherDTO
    (Long id, String name, Set<CourseShortDTO> courses, DepartmentShortDTO department){
}
