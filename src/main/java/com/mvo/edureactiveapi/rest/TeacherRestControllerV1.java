package com.mvo.edureactiveapi.rest;

import com.mvo.edureactiveapi.dto.requestdto.TeacherTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.DeleteResponseDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseCoursesDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseTeacherDTO;
import com.mvo.edureactiveapi.service.TeacherService;
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
@RequestMapping("api/v1/teachers")
public class TeacherRestControllerV1 {
    private final TeacherService service;

    @PostMapping
    Mono<ResponseEntity<ResponseTeacherDTO>> save(@Validated  @RequestBody TeacherTransientDTO teacherTransientDTO,
                                                  UriComponentsBuilder uriBuilder) {
        return service.save(teacherTransientDTO)
            .map(saved -> {
                URI location = uriBuilder
                    .path("api/v1/teachers/{id}")
                    .buildAndExpand(saved.id())
                    .toUri();
                return ResponseEntity
                    .created(location)
                    .body(saved);
            });
    }

    @GetMapping
    Flux<ResponseTeacherDTO> getAll(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        return service.getAll(page, size);
    }

    @GetMapping("{id}")
    Mono<ResponseTeacherDTO> getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("{id}")
    Mono<ResponseTeacherDTO> update(@PathVariable Long id, @Validated @RequestBody TeacherTransientDTO teacherTransientDTO) {
        return service.update(id, teacherTransientDTO);
    }

    @DeleteMapping("{id}")
    Mono<DeleteResponseDTO> delete(@PathVariable Long id) {
        return service.delete(id);
    }

    @PostMapping("/{teacherId}/courses/{coursesId}")
    public Mono<ResponseCoursesDTO> setRelationTeacherWithCourse(@PathVariable Long teacherId, @PathVariable Long coursesId) {
        return service.setRelationTeacherWithCourse(teacherId, coursesId);
    }
}
