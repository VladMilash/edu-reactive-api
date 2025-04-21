package com.mvo.edureactiveapi.service;

import com.mvo.edureactiveapi.dto.requestdto.StudentTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseStudentDTO;
import com.mvo.edureactiveapi.entity.Student;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StudentService {
    Mono<ResponseStudentDTO> save(StudentTransientDTO studentTransientDTO);
    Flux<ResponseStudentDTO> getAll();
    Mono<ResponseStudentDTO> getById(Long id);
}
