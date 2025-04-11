package com.mvo.edureactiveapi.repository;

import com.mvo.edureactiveapi.entity.Student;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends R2dbcRepository<Student, Long> {
}
