package com.mvo.edureactiveapi.repository;

import com.mvo.edureactiveapi.entity.Student;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface StudentRepository extends R2dbcRepository<Student, Long> {

   Mono<Boolean> existsByEmail(String email);

   Flux<Student> findAllByIdIn(List<Long> ids);

   @Query("SELECT * FROM student ORDER BY id LIMIT :limit OFFSET :offset")
   Flux<Student> findAllWithPagination(long limit, long offset);

}

