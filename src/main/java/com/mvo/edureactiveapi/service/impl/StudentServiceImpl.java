package com.mvo.edureactiveapi.service.impl;

import com.mvo.edureactiveapi.dto.CourseDTO;
import com.mvo.edureactiveapi.dto.TeacherDTO;
import com.mvo.edureactiveapi.dto.requestdto.StudentTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseStudentDTO;
import com.mvo.edureactiveapi.entity.Course;
import com.mvo.edureactiveapi.entity.Student;
import com.mvo.edureactiveapi.entity.StudentCourse;
import com.mvo.edureactiveapi.entity.Teacher;
import com.mvo.edureactiveapi.mapper.StudentMapper;
import com.mvo.edureactiveapi.mapper.TeacherMapper;
import com.mvo.edureactiveapi.repository.CourseRepository;
import com.mvo.edureactiveapi.repository.StudentCourseRepository;
import com.mvo.edureactiveapi.repository.StudentRepository;
import com.mvo.edureactiveapi.repository.TeacherRepository;
import com.mvo.edureactiveapi.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final TeacherMapper teacherMapper;


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
        Flux<Student> students = studentRepository.findAll().cache();
        Mono<List<Long>> studentsIds = students.map(Student::getId).collectList().cache();

        Flux<StudentCourse> studentCourses = studentsIds.flatMapMany(studentCourseRepository::findAllByStudentIdIn).cache();
        Mono<List<Long>> coursesIds = getCourseIds(studentCourses);

        Flux<Course> courses = coursesIds.flatMapMany(courseRepository::findAllByIdIn).cache();
        Mono<List<Long>> teachersIds = getTeacherIds(courses);

        Flux<Teacher> teachers = getTeachers(teachersIds);

        return Mono.zip(
            students.collectList(),
            studentCourses.collectList(),
            courses.collectList(),
            teachers.collectList()
        ).flatMapMany(tuple -> {
            List<Student> studentList = tuple.getT1();
            List<StudentCourse> studentCourseList = tuple.getT2();
            List<Course> courseList = tuple.getT3();
            List<Teacher> teachersList = tuple.getT4();

            Map<Long, Teacher> teacherMap = getTeacherMap(teachersList);

            Map<Long, Course> courseMap = getCourseMap(courseList);

            Map<Long, List<Long>> studentToCourses = getStudentToCourses(studentCourseList);

            return Flux.fromIterable(studentList)
                .map(student -> {
                    List<Long> studentCourseIds = studentToCourses
                        .getOrDefault(student.getId(), Collections.emptyList());

                    Set<CourseDTO> courseDTOs = studentCourseIds.stream()
                        .map(courseId -> {
                            Course course = courseMap.get(courseId);
                            TeacherDTO teacherDTO = getTeacherDTO(course, teacherMap);
                            return new CourseDTO(course.getId(), course.getTitle(), teacherDTO);
                        })
                        .collect(Collectors.toSet());

                    return new ResponseStudentDTO(
                        student.getId(),
                        student.getName(),
                        student.getEmail(),
                        courseDTOs
                    );
                });
        });
    }


    @Override
    public Mono<ResponseStudentDTO> getById(Long id) {
        Mono<Student> student = studentRepository.findById(id).cache();
        Flux<StudentCourse> studentCourses = studentCourseRepository.findAllByStudentId(id).cache();
        Mono<List<Long>> courseIds = getCourseIds(studentCourses);
        Flux<Course> courses = courseIds.flatMapMany(courseRepository::findAllByIdIn).cache();
        Mono<List<Long>> teacherIds = getTeacherIds(courses);
        Flux<Teacher> teachers = getTeachers(teacherIds);

        return Mono.zip(
            student,
            courses.collectList(),
            teachers.collectList()
        ).map(tuple -> {
            Student studentEntity = tuple.getT1();
            List<Course> courseList = tuple.getT2();
            List<Teacher> teacherList = tuple.getT3();

            Map<Long, Teacher> teacherMap = getTeacherMap(teacherList);

            Set<CourseDTO> courseDTOs = courseList
                .stream()
                .map(course -> {
                    TeacherDTO teacherDTO = getTeacherDTO(course, teacherMap);
                    return new CourseDTO(course.getId(), course.getTitle(), teacherDTO);
                })
                .collect(Collectors.toSet());

            return new ResponseStudentDTO(
                studentEntity.getId(),
                studentEntity.getName(),
                studentEntity.getEmail(),
                courseDTOs
            );
        });
    }

    private static Map<Long, Course> getCourseMap(List<Course> courseList) {
        return courseList.stream()
            .collect(Collectors.toMap(Course::getId, c -> c));
    }

    private static Map<Long, List<Long>> getStudentToCourses(List<StudentCourse> studentCourseList) {
        return studentCourseList.stream()
            .collect(Collectors.groupingBy(
                StudentCourse::getStudentId,
                Collectors.mapping(StudentCourse::getCourseId, Collectors.toList())
            ));
    }

    private static Mono<List<Long>> getCourseIds(Flux<StudentCourse> studentCourses) {
        return studentCourses.map(StudentCourse::getCourseId).collectList().cache();
    }

    private Flux<Teacher> getTeachers(Mono<List<Long>> teacherIds) {
        return teacherIds.flatMapMany(ids ->
            ids.isEmpty() ? Flux.empty() : teacherRepository.findAllByIdIn(ids)).cache();
    }

    private static Mono<List<Long>> getTeacherIds(Flux<Course> courses) {
        return courses
            .map(Course::getTeacherId)
            .filter(Objects::nonNull)
            .collectList().cache();
    }

    private static Map<Long, Teacher> getTeacherMap(List<Teacher> teacherList) {
        return teacherList.stream()
            .collect(Collectors.toMap(Teacher::getId, t -> t));
    }

    private TeacherDTO getTeacherDTO(Course course, Map<Long, Teacher> teacherMap) {
        TeacherDTO teacherDTO = null;
        if (course.getTeacherId() != null && teacherMap.containsKey(course.getTeacherId())) {
            Teacher teacher = teacherMap.get(course.getTeacherId());
            teacherDTO = teacherMapper.map(teacher);
        }
        return teacherDTO;
    }
}
