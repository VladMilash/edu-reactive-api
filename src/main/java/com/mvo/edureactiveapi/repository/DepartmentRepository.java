package com.mvo.edureactiveapi.repository;

import com.mvo.edureactiveapi.entity.Department;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface DepartmentRepository extends R2dbcRepository<Department, Long> {

    Flux<Department> findAllByHeadOfDepartmentIn(List<Long> headOfDepartments);

    Mono<Department> findByHeadOfDepartment(Long headOfDepartment);
}
