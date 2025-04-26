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

    @Override
    public Flux<ResponseCoursesDTO> getAll() {
        log.info("Started found all courses");
        Flux<Course> coursesFlux = courseRepository.findAll().cache();
        Flux<StudentCourse> studentCourseFlux = getStudentCourseFlux(coursesFlux);
        Flux<Student> studentFlux = getStudentFlux(studentCourseFlux);
        Flux<Teacher> teacherFlux = getTeacherFlux(coursesFlux);
        return Mono.zip(
                coursesFlux.collectList(),
                studentCourseFlux.collectList(),
                studentFlux.collectList(),
                teacherFlux.collectList()
            ).flatMapMany(tuple -> {
                List<Course> courseList = tuple.getT1();
                List<StudentCourse> studentCourseList = tuple.getT2();
                List<Student> studentList = tuple.getT3();
                List<Teacher> teacherList = tuple.getT4();
                Map<Long, Teacher> teacherMap = getTeacherMap(teacherList);
                Map<Long, Student> studentMap = getStudentMap(studentList);
                Map<Long, List<Long>> mapCourseToStudents = getMapCourseToStudents(studentCourseList);
                return Flux.fromIterable(courseList)
                    .map(course -> {
                        Set<StudentDTO> studentDTOs = getStudentDTOS(course, mapCourseToStudents, studentMap);
                        TeacherDTO teacherDTO = getTeacherDTO(course, teacherMap);
                        return getResponseCoursesDTO(course, teacherDTO, studentDTOs);
                    });
            })
            .doOnComplete(() -> log.info("Successfully retrieved all courses"))
            .doOnError(error -> log.error("Failed to found all courses"));
    }

    @Override
    public Mono<ResponseCoursesDTO> getById(Long id) {
        log.info("Started get course with id: {}", id);
        Mono<Course> courseMono = getCourseMono(id);
        return courseMono.flatMap(course -> {
                Mono<List<Student>> studentListMono = getListMono(id);
                Mono<Teacher> teacherMono = getTeacherMono(course);
                return Mono.zip(
                    studentListMono,
                    teacherMono
                ).map(tuple -> {
                    List<Student> studentList = tuple.getT1();
                    Teacher teacher = tuple.getT2();
                    Set<StudentDTO> studentDTOs = getStudentDTOs(studentList);
                    TeacherDTO teacherDTO = getTeacherDTO(course, teacher);
                    return getResponseCoursesDTO(course, teacherDTO, studentDTOs);
                });
            })
            .doOnSuccess(responseCoursesDTO -> log.info("Successfully found course with id: {}", id))
            .doOnError(error -> log.error("Failed to found course with id: {}", id));
    }

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
            .doOnError(error -> log.error("Failed to update course with id: {}", id));
    }

    @Override
    public Mono<DeleteResponseDTO> delete(Long id) {
        log.info("Started delete course with id: {}", id);
        Mono<Course> courseForDelete = getCourseMono(id);
        return courseForDelete
            .flatMap(courseRepository::delete)
            .doOnSuccess(studentDeleted -> log.info("Course with id: {} successfully deleted", id))
            .doOnError(error -> log.error("Failed to delete course", error))
            .then(Mono.just(new DeleteResponseDTO("Course deleted successfully")));
    }

    private static Set<StudentDTO> getStudentDTOS(Course course, Map<Long, List<Long>> mapCourseToStudents, Map<Long, Student> studentMap) {
        List<Long> studentsFromCourse = mapCourseToStudents
            .getOrDefault(course.getId(), Collections.emptyList());

        return studentsFromCourse
            .stream()
            .map(studentId -> {
                Student student = studentMap.get(studentId);
                return new StudentDTO(studentId, student.getName(), student.getEmail());
            }).collect(Collectors.toSet());
    }

    private Mono<List<Student>> getListMono(Long id) {
        Flux<StudentCourse> studentCourseFlux = studentCourseRepository.findAllByCourseId(id);
        Mono<List<Long>> studentIds = studentCourseFlux.map(StudentCourse::getStudentId).collectList().defaultIfEmpty(Collections.emptyList());
        Flux<Student> studentFlux = studentIds.flatMapMany(studentRepository::findAllByIdIn);
        return studentFlux.collectList().defaultIfEmpty(Collections.emptyList());
    }

    private Flux<StudentCourse> getStudentCourseFlux(Flux<Course> coursesFlux) {
        Mono<List<Long>> courseIds = coursesFlux.map(Course::getId).collectList();
        return courseIds.flatMapMany(studentCourseRepository::findAllByCourseIdIn).cache();
    }

    private Flux<Student> getStudentFlux(Flux<StudentCourse> studentCourseFlux) {
        Mono<List<Long>> studentsIds = studentCourseFlux.map(StudentCourse::getStudentId).collectList();

        return studentsIds.flatMapMany(studentRepository::findAllByIdIn).cache();
    }

    private Flux<Teacher> getTeacherFlux(Flux<Course> coursesFlux) {
        Mono<List<Long>> teacherIds = coursesFlux.filter(course -> course.getTeacherId() != null).map(Course::getTeacherId).collectList();
        return teacherIds.flatMapMany(ids -> ids.isEmpty() ? Flux.empty() : teacherRepository.findAllByIdIn(ids)).cache();
    }

    private Mono<Teacher> getTeacherMono(Course course) {
        return Optional.ofNullable(course.getTeacherId())
            .map(teacherRepository::findById)
            .orElse(Mono.just(new Teacher()));
    }

    private static ResponseCoursesDTO getResponseCoursesDTO(Course course, TeacherDTO teacherDTO, Set<StudentDTO> studentDTOs) {
        return new ResponseCoursesDTO(
            course.getId(),
            course.getTitle(),
            teacherDTO,
            studentDTOs
        );
    }

    private static TeacherDTO getTeacherDTO(Course course, Teacher teacher) {
        TeacherDTO teacherDTO = null;
        if (course.getTeacherId() != null) {
            teacherDTO = new TeacherDTO(teacher.getId(), teacher.getName());
        }
        return teacherDTO;
    }

    private static Set<StudentDTO> getStudentDTOs(List<Student> studentList) {
        Set<StudentDTO> studentDTOs = Collections.emptySet();

        if (!studentList.isEmpty()) {
            studentDTOs = studentList
                .stream()
                .map(student -> new StudentDTO(
                    student.getId(),
                    student.getName(),
                    student.getEmail()
                ))
                .collect(Collectors.toSet());
        }
        return studentDTOs;
    }

    private Mono<Course> getCourseMono(Long id) {
        return courseRepository.findById(id)
            .switchIfEmpty(Mono.error(new NotFoundEntityException("Course with ID " + id + " not found")));
    }

    private static TeacherDTO getTeacherDTO(Course course, Map<Long, Teacher> teacherMap) {
        TeacherDTO teacherDTO = null;

        if (course.getTeacherId() != null && teacherMap.containsKey(course.getTeacherId())) {
            Teacher teacher = teacherMap.get(course.getTeacherId());
            teacherDTO = new TeacherDTO(teacher.getId(), teacher.getName());
        }
        return teacherDTO;
    }

    private static Map<Long, Teacher> getTeacherMap(List<Teacher> teacherList) {
        return teacherList
            .stream()
            .collect(Collectors.toMap(Teacher::getId, t -> t));
    }

    private static Map<Long, Student> getStudentMap(List<Student> studentList) {
        return studentList
            .stream()
            .collect(Collectors.toMap(Student::getId, s -> s));
    }

    private static Map<Long, List<Long>> getMapCourseToStudents(List<StudentCourse> studentCourseList) {
        return studentCourseList
            .stream()
            .collect(Collectors.groupingBy(
                StudentCourse::getCourseId,
                Collectors.mapping(StudentCourse::getStudentId, Collectors.toList())
            ));
    }
}
