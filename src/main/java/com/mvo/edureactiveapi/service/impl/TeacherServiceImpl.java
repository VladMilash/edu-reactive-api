package com.mvo.edureactiveapi.service.impl;

import com.mvo.edureactiveapi.dto.CourseShortDTO;
import com.mvo.edureactiveapi.dto.DepartmentShortDTO;
import com.mvo.edureactiveapi.dto.requestdto.TeacherTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.DeleteResponseDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseCoursesDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseTeacherDTO;
import com.mvo.edureactiveapi.entity.Course;
import com.mvo.edureactiveapi.entity.Teacher;
import com.mvo.edureactiveapi.mapper.TeacherMapper;
import com.mvo.edureactiveapi.repository.CourseRepository;
import com.mvo.edureactiveapi.repository.DepartmentRepository;
import com.mvo.edureactiveapi.repository.TeacherRepository;
import com.mvo.edureactiveapi.service.CourseService;
import com.mvo.edureactiveapi.service.TeacherService;
import com.mvo.edureactiveapi.service.util.EntityFetcher;
import com.mvo.edureactiveapi.service.util.ResponseDtoBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final TeacherMapper teacherMapper;
    private final CourseService courseService;

    @Override
    public Mono<ResponseTeacherDTO> save(TeacherTransientDTO teacherTransientDTO) {
        return teacherRepository
            .save(teacherMapper.fromTeacherTransientDTO(teacherTransientDTO))
            .map(teacherMapper::toResponseTeacherDTO)
            .doOnSuccess(dto -> log.info("Teacher successfully created with id: {}", dto.id()))
            .doOnError(error -> log.error("Failed to saving Teacher", error));
    }

    @Transactional
    @Override
    public Flux<ResponseTeacherDTO> getAll(int page, int size) {
        long offset = (long) page * size;
        return teacherRepository.findAllWithPagination(size, offset)
            .flatMap(getTeacherMonoFunction())
            .doOnComplete(() -> log.info("Successfully retrieved all teachers"))
            .doOnError(error -> log.error("Failed to found all teachers", error));
    }

    @Transactional
    @Override
    public Mono<ResponseTeacherDTO> getById(Long id) {
        Mono<Teacher> teacherMono = EntityFetcher.getTeacherMono(id, teacherRepository);
        return teacherMono.flatMap(getTeacherMonoFunction())
            .doOnSuccess(responseTeacherDTO -> log.info("Teacher successfully found with id: {}", id))
            .doOnError(error -> log.error("Failed to found teacher with id: {}", id, error));
    }

    @Transactional
    @Override
    public Mono<ResponseTeacherDTO> update(Long id, TeacherTransientDTO teacherTransientDTO) {
        Mono<Teacher> teacherForUpdate = EntityFetcher.getTeacherMono(id, teacherRepository);
        return teacherForUpdate
            .flatMap(teacher -> {
                teacher.setName(teacherTransientDTO.name());
                return teacherRepository
                    .save(teacher)
                    .flatMap(updatedTeacher -> getById(updatedTeacher.getId()));
            })
            .doOnSuccess(updatedTeacher -> log.info("Teacher with id: {} successfully updated", id))
            .doOnError(error -> log.error("Failed to update teacher with id: {}", id, error));
    }

    @Override
    public Mono<DeleteResponseDTO> delete(Long id) {
        Mono<Teacher> teacherForDelete = EntityFetcher.getTeacherMono(id, teacherRepository);
        return teacherForDelete
            .flatMap(teacherRepository::delete)
            .doOnSuccess(studentDeleted -> log.info("Teacher with id: {} successfully deleted", id))
            .doOnError(error -> log.error("Failed to delete teacher", error))
            .then(Mono.just(new DeleteResponseDTO("Teacher deleted successfully")));
    }

    @Transactional
    @Override
    public Mono<ResponseCoursesDTO> setRelationTeacherWithCourse(Long teacherId, Long courseId) {
        Mono<Teacher> teacherMono = EntityFetcher.getTeacherMono(teacherId, teacherRepository);
        Mono<Course> courseMono = EntityFetcher.getCourseMono(courseId, courseRepository);
        return Mono.zip(
                teacherMono,
                courseMono
            ).flatMap(tuple -> {
                Teacher teacher = tuple.getT1();
                Course course = tuple.getT2();
                return saveRelationTeacherWithCourse(course, teacher);
            })
            .doOnSuccess(studentDeleted -> log.info("Successfully added teacher with id: {} to course with id: {}", teacherId, courseId))
            .doOnError(error -> log.error("Failed to added teacher with id: {} to course with id: {}", teacherId, courseId, error));
    }

    private Mono<ResponseCoursesDTO> saveRelationTeacherWithCourse(Course course, Teacher teacher) {
        course.setTeacherId(teacher.getId());
        return courseRepository
            .save(course)
            .flatMap(course1 -> courseService.getById(course1.getId()));
    }

    private Function<Teacher, Mono<? extends ResponseTeacherDTO>> getTeacherMonoFunction() {
        return teacher -> {
            Flux<CourseShortDTO> courseShortDTOFlux = EntityFetcher.getCourseShortDTOFlux(teacher, courseRepository);
            Mono<DepartmentShortDTO> departmentShortDTOMono = EntityFetcher.getDepartmentShortDTOMono(teacher, departmentRepository);

            return courseShortDTOFlux.collect(Collectors.toSet())
                .zipWith(departmentShortDTOMono)
                .map(tuple -> {
                    Set<CourseShortDTO> courseShortDTOs = tuple.getT1();
                    DepartmentShortDTO departmentShortDTO = tuple.getT2();

                    return ResponseDtoBuilder.getResponseTeacherDTO(teacher, courseShortDTOs, departmentShortDTO);
                });
        };
    }

}
