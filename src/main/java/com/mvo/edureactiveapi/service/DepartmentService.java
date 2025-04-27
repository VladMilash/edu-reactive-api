package com.mvo.edureactiveapi.service;

import com.mvo.edureactiveapi.dto.requestdto.DepartmentTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.DeleteResponseDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseDepartmentDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DepartmentService {
    Mono<ResponseDepartmentDTO> save(DepartmentTransientDTO departmentTransientDTO);
    Mono<ResponseDepartmentDTO> getById(Long id);
    Flux<ResponseDepartmentDTO> getAll();
    Mono<DeleteResponseDTO> delete(Long id);
    Mono<ResponseDepartmentDTO> update(Long id, DepartmentTransientDTO departmentTransientDTO);
    Mono<ResponseDepartmentDTO> setRelationWithTeacher(Long departmentId, Long teacherId);
}
