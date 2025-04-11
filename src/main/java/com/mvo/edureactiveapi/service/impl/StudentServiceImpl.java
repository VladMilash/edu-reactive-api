package com.mvo.edureactiveapi.service.impl;

import com.mvo.edureactiveapi.dto.requestdto.StudentTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseStudentDTO;
import com.mvo.edureactiveapi.entity.Student;
import com.mvo.edureactiveapi.mapper.StudentMapper;
import com.mvo.edureactiveapi.repository.StudentRepository;
import com.mvo.edureactiveapi.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashSet;

@RequiredArgsConstructor
@Service
@Slf4j
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    @Override
    public Mono<ResponseStudentDTO> save(StudentTransientDTO studentTransientDTO) {
        Student transientStudent = studentMapper.map(studentTransientDTO);
        return studentRepository.save(transientStudent)
            .map(studentMapper::toResponseStudentDTO);
    }
}
