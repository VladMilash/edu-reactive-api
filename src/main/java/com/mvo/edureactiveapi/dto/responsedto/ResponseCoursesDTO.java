package com.mvo.edureactiveapi.dto.responsedto;

import com.mvo.edureactiveapi.dto.StudentDTO;
import com.mvo.edureactiveapi.dto.TeacherDTO;

import java.util.Set;

public record ResponseCoursesDTO
    (Long id, String title, TeacherDTO teacher, Set<StudentDTO> students){
}
