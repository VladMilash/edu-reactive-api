package com.mvo.edureactiveapi.repository;

import com.mvo.edureactiveapi.entity.Department;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface DepartmentRepository extends R2dbcRepository<Department, Long> {
}
