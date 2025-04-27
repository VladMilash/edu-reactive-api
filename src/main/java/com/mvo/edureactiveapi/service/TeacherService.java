package com.mvo.edureactiveapi.service;

import com.mvo.edureactiveapi.dto.requestdto.TeacherTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.DeleteResponseDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseCoursesDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseTeacherDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TeacherService {
    Mono<ResponseTeacherDTO> save(TeacherTransientDTO teacherTransientDTO);
    Mono<ResponseTeacherDTO> getById(Long id);
    Flux<ResponseTeacherDTO> getAll();
    Mono<ResponseTeacherDTO> update(Long id, TeacherTransientDTO teacherTransientDTO);
    Mono<DeleteResponseDTO> delete(Long id);
    Mono<ResponseCoursesDTO> setRelationTeacherWithCourse(Long teacherId, Long courseId);
}
