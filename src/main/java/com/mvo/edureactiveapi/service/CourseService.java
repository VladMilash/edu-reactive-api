package com.mvo.edureactiveapi.service;

import com.mvo.edureactiveapi.dto.requestdto.CourseTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.DeleteResponseDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseCoursesDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseStudentDTO;
import com.mvo.edureactiveapi.entity.Course;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CourseService {
   Mono<ResponseCoursesDTO> save(CourseTransientDTO courseTransientDTO);
   Flux<ResponseCoursesDTO> getAll();
   Mono<ResponseCoursesDTO> getById(Long id);
   Mono<ResponseCoursesDTO> update(Long id, CourseTransientDTO courseTransientDTO);
   Mono<DeleteResponseDTO> delete(Long id);;
}
