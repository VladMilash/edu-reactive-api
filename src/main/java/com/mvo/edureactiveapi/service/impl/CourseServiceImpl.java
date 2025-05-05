package com.mvo.edureactiveapi.service.impl;

import com.mvo.edureactiveapi.dto.StudentDTO;
import com.mvo.edureactiveapi.dto.TeacherDTO;
import com.mvo.edureactiveapi.dto.requestdto.CourseTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.DeleteResponseDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseCoursesDTO;
import com.mvo.edureactiveapi.entity.Course;
import com.mvo.edureactiveapi.entity.Student;
import com.mvo.edureactiveapi.entity.StudentCourse;
import com.mvo.edureactiveapi.entity.Teacher;
import com.mvo.edureactiveapi.exeption.NotFoundEntityException;
import com.mvo.edureactiveapi.mapper.CourseMapper;
import com.mvo.edureactiveapi.repository.CourseRepository;
import com.mvo.edureactiveapi.repository.StudentCourseRepository;
import com.mvo.edureactiveapi.repository.StudentRepository;
import com.mvo.edureactiveapi.repository.TeacherRepository;
import com.mvo.edureactiveapi.service.CourseService;
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
        log.info("Started creating course with title: {}", courseTransientDTO.title());
        return courseRepository
            .save(courseMapper.fromCourseTransientDTO(courseTransientDTO))
            .map(courseMapper::toResponseCoursesDTO)
            .doOnSuccess(dto -> log.info("Course successfully created with id: {}", dto.id()))
            .doOnError(error -> log.error("Failed to saving course", error));
    }

    @Transactional
    @Override
    public Flux<ResponseCoursesDTO> getAll() {
        log.info("Starting gel all courses");
        return courseRepository.findAll()
            .flatMap(course -> {
                Mono<TeacherDTO> teacherDTOMono = Mono.justOrEmpty(course.getTeacherId())
                    .flatMap(teacherRepository::findById)
                    .map(teacher -> new TeacherDTO(teacher.getId(), teacher.getName()))
                    .defaultIfEmpty(new TeacherDTO(null, null))
                    .log();

                Flux<StudentDTO> studentDTOFlux = studentCourseRepository.findAllByCourseId(course.getId())
                    .map(StudentCourse::getStudentId)
                    .buffer(100)
                    .flatMapSequential(studentRepository::findAllByIdIn)
                    .map(student -> new StudentDTO(
                        student.getId(),
                        student.getName(),
                        student.getEmail()
                    ))
                    .log();

                return studentDTOFlux
                    .collect(Collectors.toSet())
                    .zipWith(teacherDTOMono)
                    .map(tuple -> {
                        Set<StudentDTO> studentDTOs = tuple.getT1();
                        TeacherDTO teacherDTO = tuple.getT2();

                        return new ResponseCoursesDTO(
                            course.getId(),
                            course.getTitle(),
                            course.getTeacherId() != null ? teacherDTO : null,
                            studentDTOs
                        );
                    });
            })
            .doOnComplete(() -> log.info("Successfully retrieved all courses"))
            .doOnError(error -> log.error("Failed to find all courses: {}", error.getMessage()))
            .log();
    }

    @Transactional
    @Override
    public Mono<ResponseCoursesDTO> getById(Long id) {
        log.info("Started get course with id: {}", id);
        Mono<Course> courseMono = getCourseMono(id);
        return courseMono.flatMap(course -> {
                Mono<TeacherDTO> teacherDTOMono = Mono.justOrEmpty(course.getTeacherId())
                    .flatMap(teacherRepository::findById)
                    .map(teacher -> new TeacherDTO(teacher.getId(), teacher.getName()))
                    .defaultIfEmpty(new TeacherDTO(null, null))
                    .log();

                Flux<StudentDTO> studentDTOFlux = studentCourseRepository.findAllByCourseId(course.getId())
                    .map(StudentCourse::getStudentId)
                    .buffer(100)
                    .flatMapSequential(studentRepository::findAllByIdIn)
                    .map(student -> new StudentDTO(
                        student.getId(),
                        student.getName(),
                        student.getEmail()
                    ))
                    .log();

                return Mono.zip(
                    teacherDTOMono,
                    studentDTOFlux.collect(Collectors.toSet())
                ).map(tuple -> {
                    TeacherDTO teacherDTO = tuple.getT1();
                    Set<StudentDTO> studentDTOs = tuple.getT2();
                    return new ResponseCoursesDTO(
                        course.getId(),
                        course.getTitle(),
                        course.getTeacherId() != null ? teacherDTO : null,
                        studentDTOs
                    );
                });

            })
            .doOnSuccess(responseCoursesDTO -> log.info("Successfully found course with id: {}", id))
            .doOnError(error -> log.error("Failed to found course with id: {}", id, error))
            .log();
    }

    @Transactional
    @Override
    public Mono<ResponseCoursesDTO> update(Long id, CourseTransientDTO courseTransientDTO) {
        log.info("Started update course with id: {}", id);
        Mono<Course> courseForDelete = getCourseMono(id);
        return courseForDelete
            .flatMap(foundedCourse -> {
                foundedCourse.setTitle(courseTransientDTO.title());
                return courseRepository.save(foundedCourse).then(getById(id));
            })
            .doOnSuccess(updatedStudent -> log.info("Course with id: {} successfully updated", id))
            .doOnError(error -> log.error("Failed to update course with id: {}", id, error))
            .log();
    }

    @Override
    public Mono<DeleteResponseDTO> delete(Long id) {
        log.info("Started delete course with id: {}", id);
        Mono<Course> courseForDelete = getCourseMono(id);
        return courseForDelete
            .flatMap(courseRepository::delete)
            .doOnSuccess(studentDeleted -> log.info("Course with id: {} successfully deleted", id))
            .doOnError(error -> log.error("Failed to delete course", error))
            .then(Mono.just(new DeleteResponseDTO("Course deleted successfully")))
            .log();
    }

    private Mono<Course> getCourseMono(Long id) {
        log.info("Starting get course with id: {}", id);
        return courseRepository.findById(id)
            .switchIfEmpty(Mono.error(new NotFoundEntityException("Course with ID " + id + " not found")))
            .log();
    }

}
