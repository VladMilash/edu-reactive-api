package com.mvo.edureactiveapi.rest;

import com.mvo.edureactiveapi.dto.requestdto.CourseTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.DeleteResponseDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseCoursesDTO;
import com.mvo.edureactiveapi.service.CourseService;
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
@RequestMapping("api/v1/courses/")
public class CoursesRestControllerV1 {
    private final CourseService service;

    @Operation(
        summary = "Создание курса",
        description = "Позволяет создать новый курс"
    )
    @PostMapping
    Mono<ResponseEntity<ResponseCoursesDTO>> save(@Validated @RequestBody CourseTransientDTO courseTransientDTO,
                                                  UriComponentsBuilder uriBuilder) {
        return service.save(courseTransientDTO)
            .map(saved -> {
                URI location = uriBuilder
                    .path("/api/v1/courses/{id}")
                    .buildAndExpand(saved.id())
                    .toUri();
                return ResponseEntity
                    .created(location)
                    .body(saved);
            });
    }

    @Operation(
        summary = "Получение всех курсов",
        description = "Позволяет получить все курсы"
    )
    @GetMapping
    Flux<ResponseCoursesDTO> getAll(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        return service.getAll(page, size);
    }

    @Operation(
        summary = "Получение курса по id",
        description = "Позволяет получить курс по id"
    )
    @GetMapping("{id}")
    Mono<ResponseCoursesDTO> getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @Operation(
        summary = "Обновление курса по id",
        description = "Позволяет обновить курс по id"
    )
    @PutMapping("{id}")
    Mono<ResponseCoursesDTO> update(@PathVariable Long id, @Validated @RequestBody CourseTransientDTO courseTransientDTO) {
        return service.update(id, courseTransientDTO);
    }

    @Operation(
        summary = "Удаление курса по id",
        description = "Позволяет удалить курс по id"
    )
    @DeleteMapping("{id}")
    Mono<DeleteResponseDTO> delete(@PathVariable Long id) {
        return service.delete(id);
    }

}
