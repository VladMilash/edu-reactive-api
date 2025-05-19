package com.mvo.edureactiveapi.rest;

import com.mvo.edureactiveapi.dto.requestdto.DepartmentTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.DeleteResponseDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseDepartmentDTO;
import com.mvo.edureactiveapi.service.DepartmentService;
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
@RequestMapping("api/v1/departments/")
public class DepartmentRestControllerV1 {
    private final DepartmentService service;

    @Operation(
        summary = "Создание департамента",
        description = "Позволяет создать новый департамент"
    )
    @PostMapping
    public Mono<ResponseEntity<ResponseDepartmentDTO>> saveDepartment(@Validated  @RequestBody DepartmentTransientDTO departmentTransientDTO,
                                                                      UriComponentsBuilder uriBuilder) {
        return service.save(departmentTransientDTO)
            .map(saved -> {
                URI location = uriBuilder
                    .path("api/v1/departments/{id}")
                    .buildAndExpand(saved.id())
                    .toUri();
                return ResponseEntity
                    .created(location)
                    .body(saved);
            });
    }

    @Operation(
        summary = "Получение всех департаментов",
        description = "Позволяет получить все департаменты"
    )
    @GetMapping
    public Flux<ResponseDepartmentDTO> getAll(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return service.getAll(page, size);
    }

    @Operation(
        summary = "Получение департамента по id",
        description = "Позволяет получить департамент по id"
    )
    @GetMapping("{id}")
    public Mono<ResponseDepartmentDTO> getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @Operation(
        summary = "Обновление департамента по id",
        description = "Позволяет обновить департамент по id"
    )
    @PutMapping("{id}")
    public Mono<ResponseDepartmentDTO> update(@PathVariable Long id, @Validated @RequestBody DepartmentTransientDTO departmentTransientDTO) {
        return service.update(id, departmentTransientDTO);
    }

    @Operation(
        summary = "Удаление департамента по id",
        description = "Позволяет удалить департамент по id"
    )
    @DeleteMapping("{id}")
    public Mono<DeleteResponseDTO> delete(@PathVariable Long id) {
        return service.delete(id);
    }

    @Operation(
        summary = "Установление связи департамент-учитель",
        description = "Позволяет установить связь департамент-учитель"
    )
    @PostMapping("{departmentId}/teacher/{teacherId}")
    public Mono<ResponseDepartmentDTO> setRelationWitTeacher(@PathVariable Long departmentId, @PathVariable Long teacherId) {
        return service.setRelationWithTeacher(departmentId, teacherId);
    }
}
