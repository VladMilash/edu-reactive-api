package com.mvo.edureactiveapi.dto.responsedto;

import com.mvo.edureactiveapi.dto.CourseDTO;

import java.util.Set;

public record ResponseStudentDTO(Long id, String name, String email, Set<CourseDTO> courses) {
}
