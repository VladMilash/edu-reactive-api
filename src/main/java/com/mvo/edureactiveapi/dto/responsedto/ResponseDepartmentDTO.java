package com.mvo.edureactiveapi.dto.responsedto;

import com.mvo.edureactiveapi.dto.TeacherDTO;

public record ResponseDepartmentDTO(Long id, String name, TeacherDTO headOfDepartment) {
}
