package com.mvo.edureactiveapi.rest;

import com.mvo.edureactiveapi.dto.CourseDTO;
import com.mvo.edureactiveapi.dto.requestdto.StudentTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.DeleteResponseDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseStudentDTO;
import com.mvo.edureactiveapi.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/students/")
public class StudentsRestControllerV1 {
    private final StudentService service;

    @PostMapping
    public Mono<ResponseEntity<ResponseStudentDTO>> save(@Validated  @RequestBody StudentTransientDTO studentTransientDTO,
                                                         UriComponentsBuilder uriBuilder) {
        return service.save(studentTransientDTO)
            .map(saved -> {
                URI location = uriBuilder
                    .path("api/v1/students/{id}")
                    .buildAndExpand(saved.id())
                    .toUri();
                return ResponseEntity
                    .created(location)
                    .body(saved);
            });
    }

    @GetMapping
    public Flux<ResponseStudentDTO> getAll(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return service.getAll(page, size);
    }

    @GetMapping("{id}")
    public Mono<ResponseStudentDTO> getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("{id}")
    public Mono<ResponseStudentDTO> update(@PathVariable Long id, @Validated @RequestBody StudentTransientDTO studentTransientDTO) {
        return service.update(id, studentTransientDTO);
    }

    @PostMapping("{studentId}/courses/{courseId}")
    public Mono<ResponseStudentDTO> setRelationWithCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
        return service.setRelationWithCourse(studentId, courseId);
    }

    @GetMapping("{id}/courses")
    public Flux<CourseDTO> getStudentCourses(@PathVariable Long id) {
        return service.getStudentCourses(id);
    }

    @DeleteMapping("{id}")
    public Mono<DeleteResponseDTO> delete(@PathVariable Long id) {
        return service.delete(id);
    }
}
