package com.mvo.edureactiveapi.rest;

import com.mvo.edureactiveapi.dto.requestdto.StudentTransientDTO;
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
}
