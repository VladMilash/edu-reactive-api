package com.mvo.edureactiveapi.service.impl;

import com.mvo.edureactiveapi.dto.requestdto.StudentTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseStudentDTO;
import com.mvo.edureactiveapi.entity.Student;
import com.mvo.edureactiveapi.mapper.StudentCustomMapper;
import com.mvo.edureactiveapi.mapper.StudentMapper;
import com.mvo.edureactiveapi.repository.StudentRepository;
import com.mvo.edureactiveapi.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
@Slf4j
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final StudentCustomMapper studentCustomMapper;


    @Override
    public Mono<ResponseStudentDTO> save(StudentTransientDTO studentTransientDTO) {
        log.info("Check if the email {} has been used for registration before", studentTransientDTO.email());
        return studentRepository.existsByEmail(studentTransientDTO.email())
            .flatMap(exist -> {
                if (Boolean.TRUE.equals(exist)) {
                    log.error("The email {} was used for registration earlier", studentTransientDTO.email());
                    return Mono.error(new RuntimeException(""));
                }
                log.info("Email {} has not been used for registration before", studentTransientDTO.email());
                log.info("Starting registration student with email {}", studentTransientDTO.email());
                Student transientStudent = studentMapper.map(studentTransientDTO);
                return studentRepository.save(transientStudent)
                    .map(studentMapper::toResponseStudentDTO)
                    .doOnSuccess(dto -> log.info("Student successfully created with id: {}", dto.id()))
                    .doOnError(error -> log.error("Failed to saving student", error));
            });
    }

    @Override
    public Flux<ResponseStudentDTO> getAll() {
        return studentCustomMapper.processing(studentRepository.getAllWithCoursesAndTeacher())
            .doOnError(error -> log.error(""));
    }
}
