package com.mvo.edureactiveapi.rest;

import com.mvo.edureactiveapi.dto.requestdto.DepartmentTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.DeleteResponseDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseDepartmentDTO;
import com.mvo.edureactiveapi.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public Mono<ResponseEntity<ResponseDepartmentDTO>> saveDepartment(@RequestBody DepartmentTransientDTO departmentTransientDTO,
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

    @GetMapping
    public Flux<ResponseDepartmentDTO> getAll(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return service.getAll(page, size);
    }

    @GetMapping("{id}")
    public Mono<ResponseDepartmentDTO> getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("{id}")
    public Mono<ResponseDepartmentDTO> update(@PathVariable Long id, @RequestBody DepartmentTransientDTO departmentTransientDTO) {
        return service.update(id, departmentTransientDTO);
    }

    @DeleteMapping("{id}")
    public Mono<DeleteResponseDTO> delete(@PathVariable Long id) {
        return service.delete(id);
    }

    @PostMapping("{departmentId}/teacher/{teacherId}")
    public Mono<ResponseDepartmentDTO> setRelationWitTeacher(@PathVariable Long departmentId, @PathVariable Long teacherId) {
        return service.setRelationWithTeacher(departmentId, teacherId);
    }
}
