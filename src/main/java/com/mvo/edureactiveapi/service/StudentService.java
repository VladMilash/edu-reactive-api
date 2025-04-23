package com.mvo.edureactiveapi.service;

import com.mvo.edureactiveapi.dto.CourseDTO;
import com.mvo.edureactiveapi.dto.requestdto.StudentTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.DeleteResponseDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseStudentDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StudentService {
    Mono<ResponseStudentDTO> save(StudentTransientDTO studentTransientDTO);
    Flux<ResponseStudentDTO> getAll();
    Mono<ResponseStudentDTO> getById(Long id);
    Mono<ResponseStudentDTO> update(Long id, StudentTransientDTO studentTransientDTO);
    Mono<DeleteResponseDTO> delete(Long id);
    Mono<ResponseStudentDTO> setRelationWithCourse(Long studentId, Long courseId);
    Flux<CourseDTO> getStudentCourses(Long id);
}
