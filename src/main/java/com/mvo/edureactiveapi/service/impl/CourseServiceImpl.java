package com.mvo.edureactiveapi.service.impl;

import com.mvo.edureactiveapi.dto.CourseDTO;
import com.mvo.edureactiveapi.dto.StudentDTO;
import com.mvo.edureactiveapi.dto.TeacherDTO;
import com.mvo.edureactiveapi.dto.requestdto.CourseTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.DeleteResponseDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseCoursesDTO;
import com.mvo.edureactiveapi.entity.Course;
import com.mvo.edureactiveapi.entity.Student;
import com.mvo.edureactiveapi.entity.StudentCourse;
import com.mvo.edureactiveapi.entity.Teacher;
import com.mvo.edureactiveapi.mapper.CourseMapper;
import com.mvo.edureactiveapi.mapper.TeacherMapper;
import com.mvo.edureactiveapi.repository.CourseRepository;
import com.mvo.edureactiveapi.repository.StudentCourseRepository;
import com.mvo.edureactiveapi.repository.StudentRepository;
import com.mvo.edureactiveapi.repository.TeacherRepository;
import com.mvo.edureactiveapi.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
    private final TeacherMapper teacherMapper;

    @Override
    public Mono<ResponseCoursesDTO> save(CourseTransientDTO courseTransientDTO) {
        log.info("Started creating course with title: {}", courseTransientDTO.title());
        return courseRepository
            .save(courseMapper.fromCourseTransientDTO(courseTransientDTO))
            .map(courseMapper::toResponseCoursesDTO)
            .doOnSuccess(dto -> log.info("Course successfully created with id: {}", dto.id()))
            .doOnError(error -> log.error("Failed to saving course", error));
    }

    @Override
    public Flux<ResponseCoursesDTO> getAll() {
        log.info("Started found all courses");
        Flux<Course> coursesFlux = courseRepository.findAll().cache().doOnNext(course -> log.debug("Course: {}", course));
        Mono<List<Long>> courseIds = coursesFlux.map(Course::getId).collectList();

        Flux<StudentCourse> studentCourseFlux = courseIds.flatMapMany(studentCourseRepository::findAllByCourseIdIn).cache();
        Mono<List<Long>> studentsIds = studentCourseFlux.map(StudentCourse::getStudentId).collectList().cache();

        Flux<Student> studentFlux = studentsIds.flatMapMany(studentRepository::findAllByIdIn).cache();

        Mono<List<Long>> teacherIds = coursesFlux.filter(course -> course.getTeacherId() != null).map(Course::getTeacherId).filter(Objects::nonNull).collectList();
        Flux<Teacher> teacherFlux = teacherIds.flatMapMany(ids -> ids.isEmpty() ? Flux.empty() : teacherRepository.findAllByIdIn(ids)).cache();

        return Mono.zip(
                coursesFlux.collectList(),
                studentCourseFlux.collectList(),
                studentFlux.collectList(),
                teacherFlux.collectList()
            ).doOnNext(z -> log.info("Started Mono.zip"))
            .flatMapMany(tuple -> {
                List<Course> courseList = tuple.getT1();
                log.info("CourseList size: {}", courseList.size());
                List<StudentCourse> studentCourseList = tuple.getT2();
                log.info("StudentCourseList size: {}", studentCourseList.size());
                List<Student> studentList = tuple.getT3();
                log.info("StudentList size: {}", studentList.size());
                List<Teacher> teacherList = tuple.getT4();
                log.info("TeacherList size: {}", teacherList.size());

                Map<Long, Teacher> teacherMap = teacherList
                    .stream()
                    .collect(Collectors.toMap(Teacher::getId, t -> t));

                Map<Long, Student> studentMap = studentList
                    .stream()
                    .collect(Collectors.toMap(Student::getId, s -> s));

                Map<Long, List<Long>> mapCourseToStudents = studentCourseList
                    .stream()
                    .collect(Collectors.groupingBy(
                        StudentCourse::getCourseId,
                        Collectors.mapping(StudentCourse::getStudentId, Collectors.toList())
                    ));

                return Flux.fromIterable(courseList)
                    .map(course -> {
                        List<Long> studentsFromCourse = mapCourseToStudents
                            .getOrDefault(course.getId(), Collections.emptyList());

                        Set<StudentDTO> studentDTOs = studentsFromCourse
                            .stream()
                            .map(studentId -> {
                                Student student = studentMap.get(studentId);
                                if(student != null) {
                                    return new StudentDTO(studentId, student.getName(), student.getEmail());
                                } else {
                                    return null;
                                }
                            }).filter(Objects::nonNull).collect(Collectors.toSet());

                        TeacherDTO teacherDTO = null;

                        if (course.getTeacherId() != null && teacherMap.containsKey(course.getTeacherId())) {
                            Teacher teacher = teacherMap.get(course.getTeacherId());
                            teacherDTO = new TeacherDTO(teacher.getId(), teacher.getName());
                        }

                        return new ResponseCoursesDTO(
                            course.getId(),
                            course.getTitle(),
                            teacherDTO,
                            studentDTOs
                        );
                    });
            });

    }

    @Override
    public Mono<ResponseCoursesDTO> getById(Long id) {
        return null;
    }

    @Override
    public Mono<ResponseCoursesDTO> update(Long id, CourseTransientDTO courseTransientDTO) {
        return null;
    }

    @Override
    public Mono<DeleteResponseDTO> delete(Long id) {
        return null;
    }
}
