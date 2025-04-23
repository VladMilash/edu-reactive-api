package com.mvo.edureactiveapi.rest;

import com.mvo.edureactiveapi.dto.CourseDTO;
import com.mvo.edureactiveapi.dto.requestdto.StudentTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.DeleteResponseDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseStudentDTO;
import com.mvo.edureactiveapi.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/students/")
public class StudentsRestControllerV1 {
    private final StudentService service;

    @PostMapping
    public Mono<ResponseStudentDTO> save(@RequestBody StudentTransientDTO studentTransientDTO) {
        return service.save(studentTransientDTO);
    }

    @GetMapping
    public Flux<ResponseStudentDTO> getAll() {
        return service.getAll();
    }

    @GetMapping("{id}")
    public Mono<ResponseStudentDTO> getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("{id}")
    public Mono<ResponseStudentDTO> update(@PathVariable Long id, @RequestBody StudentTransientDTO studentTransientDTO) {
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
