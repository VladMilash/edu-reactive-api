package com.mvo.edureactiveapi.repository;

import com.mvo.edureactiveapi.entity.Student;
import com.mvo.edureactiveapi.repository.custom.CustomStudentRepository;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface StudentRepository extends R2dbcRepository<Student, Long>, CustomStudentRepository {
   Mono<Student> findByEmail(String email);

   Mono<Boolean> existsByEmail(String email);

   Flux<Student> findAllByIdIn(List<Long> ids);

}

