package com.mvo.edureactiveapi.service.impl;

import com.mvo.edureactiveapi.dto.requestdto.StudentTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseStudentDTO;
import com.mvo.edureactiveapi.entity.Student;
import com.mvo.edureactiveapi.exeption.AlReadyExistException;
import com.mvo.edureactiveapi.mapper.StudentMapper;
import com.mvo.edureactiveapi.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private StudentMapper studentMapper;
    @InjectMocks
    private StudentServiceImpl serviceUnderTest;

    private StudentTransientDTO studentTransientDTO;
    private Student persistStudent;
    private ResponseStudentDTO responseStudentDTO;

    @BeforeEach
    void setUp() {
        studentTransientDTO = new StudentTransientDTO("test", "test@test.ru");
        persistStudent = new Student();
        persistStudent.setId(1L);
        persistStudent.setName("test");
        persistStudent.setEmail("test@test.ru");
        responseStudentDTO = new ResponseStudentDTO(1L, "test", "test@test.ru", new HashSet<>());
    }

    @Test
    @DisplayName("Test save student functionality")
    public void givenStudentToSave_whenSaveStudent_thenRepositoryIsCalled() {
        // given
        when(studentRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
        when(studentRepository.save(any())).thenReturn(Mono.just(persistStudent));
        when(studentMapper.toResponseStudentDTO(any())).thenReturn(responseStudentDTO);

        // when
        Mono<ResponseStudentDTO> responseMono = serviceUnderTest.save(studentTransientDTO);

        // then
        StepVerifier.create(responseMono)
            .expectNext(responseStudentDTO)
            .verifyComplete();

        verify(studentRepository).existsByEmail(studentTransientDTO.email());
        verify(studentRepository).save(any());
        verify(studentMapper).toResponseStudentDTO(persistStudent);
    }

    @Test
    @DisplayName("Test save student with duplicate email functionality")
    public void givenStudentToSaveWithDuplicateEmail_whenSaveDeveloper_thenExceptionIsThrown() {
        // given
        when(studentRepository.existsByEmail(anyString())).thenReturn(Mono.just(true));

        // when
        Mono<ResponseStudentDTO> responseMono = serviceUnderTest.save(studentTransientDTO);

        // then
        StepVerifier.create(responseMono)
            .expectErrorMatches(throwable -> throwable instanceof AlReadyExistException &&
                throwable.getMessage().contains("email"))
            .verify();

        verify(studentRepository).existsByEmail(studentTransientDTO.email());
        verify(studentRepository, never()).save(any());
        verify(studentMapper, never()).toResponseStudentDTO(any());
    }
}