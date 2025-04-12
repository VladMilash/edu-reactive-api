package com.mvo.edureactiveapi.repository.custom;

import com.mvo.edureactiveapi.dto.responsedto.ResponseStudentDTO;
import reactor.core.publisher.Flux;

public interface CustomStudentRepository {
    Flux<ResponseStudentDTO> getAllWithCoursesAndTeacher();
}
