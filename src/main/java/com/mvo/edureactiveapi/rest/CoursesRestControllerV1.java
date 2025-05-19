package com.mvo.edureactiveapi.rest;

import com.mvo.edureactiveapi.dto.requestdto.CourseTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.DeleteResponseDTO;
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
    Flux<ResponseCoursesDTO> getAll(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        return service.getAll(page, size);
    }

    @GetMapping("{id}")
    Mono<ResponseCoursesDTO> getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("{id}")
    Mono<ResponseCoursesDTO> update(@PathVariable Long id, @RequestBody CourseTransientDTO courseTransientDTO) {
        return service.update(id, courseTransientDTO);
    }

    @DeleteMapping("{id}")
    Mono<DeleteResponseDTO> delete(@PathVariable Long id) {
        return service.delete(id);
    }

}
