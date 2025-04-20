package com.mvo.edureactiveapi.repository;

import com.mvo.edureactiveapi.entity.Teacher;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface TeacherRepository extends R2dbcRepository<Teacher, Long> {
    Flux<Teacher> findAllByIdIn(List<Long> teachersIds);
}
