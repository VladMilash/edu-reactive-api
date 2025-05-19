package com.mvo.edureactiveapi.rest;

import com.mvo.edureactiveapi.dto.CourseDTO;
import com.mvo.edureactiveapi.dto.requestdto.StudentTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.DeleteResponseDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseStudentDTO;
import com.mvo.edureactiveapi.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(
        summary = "Создание студента",
        description = "Позволяет создать нового студента"
    )
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

    @Operation(
        summary = "Получение всех студентов",
        description = "Позволяет получить всех студентов"
    )
    @GetMapping
    public Flux<ResponseStudentDTO> getAll(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return service.getAll(page, size);
    }

    @Operation(
        summary = "Получение студента по id",
        description = "Позволяет получить студента по id"
    )
    @GetMapping("{id}")
    public Mono<ResponseStudentDTO> getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @Operation(
        summary = "Обновление студента по id",
        description = "Позволяет обновить студента по id"
    )
    @PutMapping("{id}")
    public Mono<ResponseStudentDTO> update(@PathVariable Long id, @Validated @RequestBody StudentTransientDTO studentTransientDTO) {
        return service.update(id, studentTransientDTO);
    }

    @Operation(
        summary = "Удаление студента по id",
        description = "Позволяет удалить студента по id"
    )
    @DeleteMapping("{id}")
    public Mono<DeleteResponseDTO> delete(@PathVariable Long id) {
        return service.delete(id);
    }

    @Operation(
        summary = "Установление связи студент-курс",
        description = "Позволяет установить связь студент-курс"
    )
    @PostMapping("{studentId}/courses/{courseId}")
    public Mono<ResponseStudentDTO> setRelationWithCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
        return service.setRelationWithCourse(studentId, courseId);
    }

    @Operation(
        summary = "Получить все курсы студента по id ",
        description = "Позволяет установить получить все курсы стундента по его id"
    )
    @GetMapping("{id}/courses")
    public Flux<CourseDTO> getStudentCourses(@PathVariable Long id) {
        return service.getStudentCourses(id);
    }

}
