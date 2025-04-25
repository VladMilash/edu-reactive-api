package com.mvo.edureactiveapi.rest;

import com.mvo.edureactiveapi.dto.requestdto.CourseTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseCoursesDTO;
import com.mvo.edureactiveapi.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/courses/")
public class CoursesRestControllerV1 {
    private final CourseService service;

    @PostMapping
    Mono<ResponseCoursesDTO> save(@RequestBody CourseTransientDTO courseTransientDTO) {
        return service.save(courseTransientDTO);
    }

    @GetMapping
    Flux<ResponseCoursesDTO> getAll() {
        return service.getAll();
    }

}
