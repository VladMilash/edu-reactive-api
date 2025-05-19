package com.mvo.edureactiveapi.service.impl;

import com.mvo.edureactiveapi.dto.StudentDTO;
import com.mvo.edureactiveapi.dto.TeacherDTO;
import com.mvo.edureactiveapi.dto.requestdto.CourseTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.DeleteResponseDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseCoursesDTO;
import com.mvo.edureactiveapi.entity.Course;
import com.mvo.edureactiveapi.mapper.CourseMapper;
import com.mvo.edureactiveapi.repository.CourseRepository;
import com.mvo.edureactiveapi.repository.StudentCourseRepository;
import com.mvo.edureactiveapi.repository.StudentRepository;
import com.mvo.edureactiveapi.repository.TeacherRepository;
import com.mvo.edureactiveapi.service.CourseService;
import com.mvo.edureactiveapi.service.util.EntityFetcher;
import com.mvo.edureactiveapi.service.util.ResponseDtoBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final CourseMapper courseMapper;

    @Override
    public Mono<ResponseCoursesDTO> save(CourseTransientDTO courseTransientDTO) {
        return courseRepository
            .save(courseMapper.fromCourseTransientDTO(courseTransientDTO))
            .map(courseMapper::toResponseCoursesDTO)
            .doOnSuccess(dto -> log.info("Course successfully created with id: {}", dto.id()))
            .doOnError(error -> log.error("Failed to saving course", error));
    }

    @Transactional
    @Override
    public Flux<ResponseCoursesDTO> getAll(int page, int size) {
        long offset = (long) page * size;
        return courseRepository.findAllWithPagination(size, offset)
            .flatMap(course -> {
                Mono<TeacherDTO> teacherDTOMono = EntityFetcher.getTeacherDTOMono(course, teacherRepository);
                Flux<StudentDTO> studentDTOFlux = EntityFetcher.getStudentDTOFlux(course, studentCourseRepository,studentRepository);

                return studentDTOFlux
                    .collect(Collectors.toSet())
                    .zipWith(teacherDTOMono)
                    .map(tuple -> {
                        Set<StudentDTO> studentDTOs = tuple.getT1();
                        TeacherDTO teacherDTO = tuple.getT2();
                        return ResponseDtoBuilder.getResponseCoursesDTO(course, teacherDTO, studentDTOs);
                    });
            })
            .doOnComplete(() -> log.info("Successfully retrieved all courses"))
            .doOnError(error -> log.error("Failed to find all courses: {}", error.getMessage()));
    }

    @Transactional
    @Override
    public Mono<ResponseCoursesDTO> getById(Long id) {
        Mono<Course> courseMono = EntityFetcher.getCourseMono(id, courseRepository);
        return courseMono.flatMap(course -> {
                Mono<TeacherDTO> teacherDTOMono = EntityFetcher.getTeacherDTOMono(course, teacherRepository);

                Flux<StudentDTO> studentDTOFlux = EntityFetcher.getStudentDTOFlux(course, studentCourseRepository,studentRepository);

                return Mono.zip(
                    teacherDTOMono,
                    studentDTOFlux.collect(Collectors.toSet())
                ).map(tuple -> {
                    TeacherDTO teacherDTO = tuple.getT1();
                    Set<StudentDTO> studentDTOs = tuple.getT2();
                    return ResponseDtoBuilder.getResponseCoursesDTO(course, teacherDTO, studentDTOs);
                });

            })
            .doOnSuccess(responseCoursesDTO -> log.info("Successfully found course with id: {}", id))
            .doOnError(error -> log.error("Failed to found course with id: {}", id, error));
    }

    @Transactional
    @Override
    public Mono<ResponseCoursesDTO> update(Long id, CourseTransientDTO courseTransientDTO) {
        Mono<Course> courseForDelete = EntityFetcher.getCourseMono(id, courseRepository);
        return courseForDelete
            .flatMap(foundedCourse -> {
                foundedCourse.setTitle(courseTransientDTO.title());
                return courseRepository.save(foundedCourse).then(getById(id));
            })
            .doOnSuccess(updatedStudent -> log.info("Course with id: {} successfully updated", id))
            .doOnError(error -> log.error("Failed to update course with id: {}", id, error));
    }

    @Override
    public Mono<DeleteResponseDTO> delete(Long id) {
        Mono<Course> courseForDelete = EntityFetcher.getCourseMono(id, courseRepository);
        return courseForDelete
            .flatMap(courseRepository::delete)
            .doOnSuccess(studentDeleted -> log.info("Course with id: {} successfully deleted", id))
            .doOnError(error -> log.error("Failed to delete course", error))
            .then(Mono.just(new DeleteResponseDTO("Course deleted successfully")));
    }

}
