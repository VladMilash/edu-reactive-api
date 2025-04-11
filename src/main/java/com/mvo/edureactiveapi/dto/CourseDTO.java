package com.mvo.edureactiveapi.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.mvo.edureactiveapi.entity.Teacher;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CourseDTO(Long id, String title, TeacherDTO teacher) {
}
