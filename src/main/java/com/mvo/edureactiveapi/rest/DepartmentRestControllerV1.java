package com.mvo.edureactiveapi.rest;

import com.mvo.edureactiveapi.dto.requestdto.DepartmentTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.DeleteResponseDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseDepartmentDTO;
import com.mvo.edureactiveapi.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/departments/")
public class DepartmentRestControllerV1 {
    private final DepartmentService service;

    @PostMapping
    public Mono<ResponseDepartmentDTO> saveDepartment(@RequestBody DepartmentTransientDTO departmentTransientDTO) {
        return service.save(departmentTransientDTO);
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
