package com.mvo.edureactiveapi.rest;

import com.mvo.edureactiveapi.dto.requestdto.TeacherTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.DeleteResponseDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseCoursesDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseTeacherDTO;
import com.mvo.edureactiveapi.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/teachers")
public class TeacherRestControllerV1 {
    private final TeacherService service;

    @PostMapping
    Mono<ResponseTeacherDTO> save(@RequestBody TeacherTransientDTO teacherTransientDTO) {
        return service.save(teacherTransientDTO);
    }

    @GetMapping
    Flux<ResponseTeacherDTO> getAll() {
        return service.getAll();
    }

    @GetMapping("{id}")
    Mono<ResponseTeacherDTO> getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("{id}")
    Mono<ResponseTeacherDTO> update(@PathVariable Long id, @RequestBody TeacherTransientDTO teacherTransientDTO) {
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
