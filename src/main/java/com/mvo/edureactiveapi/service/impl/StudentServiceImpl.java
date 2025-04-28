package com.mvo.edureactiveapi.service.impl;

import com.mvo.edureactiveapi.dto.CourseDTO;
import com.mvo.edureactiveapi.dto.TeacherDTO;
import com.mvo.edureactiveapi.dto.requestdto.StudentTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.DeleteResponseDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseStudentDTO;
import com.mvo.edureactiveapi.entity.Course;
import com.mvo.edureactiveapi.entity.Student;
import com.mvo.edureactiveapi.entity.StudentCourse;
import com.mvo.edureactiveapi.entity.Teacher;
import com.mvo.edureactiveapi.exeption.AlReadyExistException;
import com.mvo.edureactiveapi.exeption.NotFoundEntityException;
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
import org.springframework.transaction.annotation.Transactional;
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


    @Transactional
    @Override
    public Mono<ResponseStudentDTO> save(StudentTransientDTO studentTransientDTO) {
        log.info("Check if the email {} has been used for registration before", studentTransientDTO.email());
        return studentRepository.existsByEmail(studentTransientDTO.email())
            .flatMap(exist -> {
                if (Boolean.TRUE.equals(exist)) {
                    log.error("The email {} was used for registration earlier", studentTransientDTO.email());
                    return Mono.error(new AlReadyExistException("The email: " + studentTransientDTO.email() +
                        " was used for registration earlier"));
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

    @Transactional
    @Override
    public Flux<ResponseStudentDTO> getAll() {
        Flux<Student> students = studentRepository.findAll().cache();
        Flux<StudentCourse> studentCourses = getStudentCourseFlux(students);
        Flux<Course> courses = getCourseFlux(studentCourses);
        Flux<Teacher> teachers = getTeacherFlux(courses);
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
                    Set<CourseDTO> courseDTOs = getCourseDTOs(student, studentToCourses, courseMap, teacherMap);
                    return getResponseStudentDTO(student, courseDTOs);
                });
            })
            .doOnComplete(() -> log.info("Successfully retrieved all students"))
            .doOnError(error -> log.error("Failed to found all students"));
    }

    @Transactional
    @Override
    public Mono<ResponseStudentDTO> getById(Long id) {
        log.info("Started get student with id: {}", id);
        return getStudent(id)
            .flatMap(studentEntity -> {
                Flux<Course> courses = getCourseFlux(id);
                return courses.collectList()
                    .flatMap(courseList -> {
                        Flux<Teacher> teachers = getTeacherFlux(Flux.fromIterable(courseList));
                        return teachers.collectList()
                            .map(teacherList -> {
                                Map<Long, Teacher> teacherMap = getTeacherMap(teacherList);
                                Set<CourseDTO> courseDTOs = getCourseDTOs(courseList, teacherMap);
                                return getResponseStudentDTO(studentEntity, courseDTOs);
                            });
                    });
            })
            .doOnSuccess(responseStudentDTO -> log.info("Student successfully found with id: {}", responseStudentDTO.id()))
            .doOnError(error -> log.error("Failed to found student with id: {}", id));
    }

    @Transactional
    @Override
    public Mono<ResponseStudentDTO> update(Long id, StudentTransientDTO studentTransientDTO) {
        log.info("Started update student with id: {}", id);
        Mono<Student> studentForUpdate = getStudentMono(id);
        return studentForUpdate
            .flatMap(foundedStudent -> {
                foundedStudent.setName(studentTransientDTO.name());
                foundedStudent.setEmail(studentTransientDTO.email());
                return studentRepository.save(foundedStudent)
                    .then(getById(id));
            })
            .doOnSuccess(updatedStudent -> log.info("Student with id: {} successfully updated", id))
            .doOnError(error -> log.error("Failed to update student with id: {}", id));
    }

    @Override
    public Mono<DeleteResponseDTO> delete(Long id) {
        log.info("Started delete student with id: {}", id);
        Mono<Student> studentForDelete = getStudentMono(id);
        return studentForDelete
            .flatMap(studentRepository::delete)
            .doOnSuccess(studentDeleted -> log.info("Student with id: {} successfully deleted", id))
            .doOnError(error -> log.error("Failed to delete student", error))
            .then(Mono.just(new DeleteResponseDTO("Student deleted successfully")));
    }

    @Transactional
    @Override
    public Mono<ResponseStudentDTO> setRelationWithCourse(Long studentId, Long courseId) {
        Mono<Student> studentMono = getStudentMono(studentId);
        Mono<Course> courseMono = getCourseMono(courseId);
        return Mono.zip(
                studentMono,
                courseMono
            ).flatMap(tuple -> {
                StudentCourse studentCourse = new StudentCourse(null, courseId, studentId);
                return studentCourseRepository.findAllByStudentIdAndCourseIdIs(studentId, courseId)
                    .hasElement()
                    .flatMap(exist -> {
                        if (exist) {
                            return Mono.error(new AlReadyExistException("Relation between student " + studentId +
                                " and course " + courseId + " already exists"));
                        } else {
                            return studentCourseRepository.save(studentCourse).then(getById(studentId));
                        }
                    });
            })
            .doOnSuccess(dto -> log.info("Successfully set relation between student {} and course {}", studentId, courseId))
            .doOnError(error -> log.error("Failed to set relation between student {} and course {}", studentId, courseId, error));
    }

    @Transactional
    @Override
    public Flux<CourseDTO> getStudentCourses(Long id) {
        Mono<Student> student = getStudentMono(id);
        Flux<Course> courses = getCourseFlux(id);
        Flux<Teacher> teachers = getTeacherFlux(courses);
        return Mono.zip(
                student,
                courses.collectList(),
                teachers.collectList()
            ).flatMapMany(tuple -> {
                List<Course> courseList = tuple.getT2();
                List<Teacher> teacherList = tuple.getT3();
                Map<Long, Teacher> teacherMap = getTeacherMap(teacherList);
                return getCourseDTOFlux(courseList, teacherMap);
            })
            .doOnComplete(() -> log.info("Courses for student with id: {} successfully found ", id))
            .doOnError(error -> log.error("Failed to found courses for student with id: {}", id));
    }

    private Flux<CourseDTO> getCourseDTOFlux(List<Course> courseList, Map<Long, Teacher> teacherMap) {
        return Flux.fromStream(courseList
            .stream()
            .map(course -> {
                TeacherDTO teacherDTO = getTeacherDTO(course, teacherMap);
                return new CourseDTO(course.getId(), course.getTitle(), teacherDTO);
            }));
    }

    private Set<CourseDTO> getCourseDTOs(List<Course> courseList, Map<Long, Teacher> teacherMap) {
        return courseList
            .stream()
            .map(course -> {
                TeacherDTO teacherDTO = getTeacherDTO(course, teacherMap);
                return new CourseDTO(course.getId(), course.getTitle(), teacherDTO);
            })
            .collect(Collectors.toSet());
    }

    private Mono<Student> getStudent(Long id) {
        return studentRepository.findById(id).cache()
            .switchIfEmpty(Mono.error(new NotFoundEntityException("Student with ID " + id + " not found")));
    }

    private Flux<Course> getCourseFlux(Long id) {
        Flux<StudentCourse> studentCourses = studentCourseRepository.findAllByStudentId(id);
        Mono<List<Long>> courseIds = getCourseIds(studentCourses);
        return courseIds.flatMapMany(courseRepository::findAllByIdIn);
    }

    private static ResponseStudentDTO getResponseStudentDTO(Student student, Set<CourseDTO> courseDTOs) {
        return new ResponseStudentDTO(
            student.getId(),
            student.getName(),
            student.getEmail(),
            courseDTOs
        );
    }

    private Flux<Teacher> getTeacherFlux(Flux<Course> courses) {
        Mono<List<Long>> teachersIds = getTeacherIds(courses);
        return getTeachers(teachersIds);
    }

    private Flux<Course> getCourseFlux(Flux<StudentCourse> studentCourses) {
        Mono<List<Long>> coursesIds = getCourseIds(studentCourses);
        return coursesIds.flatMapMany(courseRepository::findAllByIdIn).cache();
    }

    private Flux<StudentCourse> getStudentCourseFlux(Flux<Student> students) {
        Mono<List<Long>> studentsIds = students.map(Student::getId).collectList().cache();
        return studentsIds.flatMapMany(studentCourseRepository::findAllByStudentIdIn).cache();
    }

    private Set<CourseDTO> getCourseDTOs(Student student, Map<Long, List<Long>> studentToCourses,
                                         Map<Long, Course> courseMap, Map<Long, Teacher> teacherMap) {
        List<Long> studentCourseIds = studentToCourses
            .getOrDefault(student.getId(), Collections.emptyList());

        return studentCourseIds.stream()
            .map(courseId -> {
                Course course = courseMap.get(courseId);
                TeacherDTO teacherDTO = getTeacherDTO(course, teacherMap);
                return new CourseDTO(course.getId(), course.getTitle(), teacherDTO);
            })
            .collect(Collectors.toSet());
    }

    private Mono<Student> getStudentMono(Long id) {
        return studentRepository.findById(id)
            .switchIfEmpty(Mono.error(new NotFoundEntityException("Student with ID " + id + " not found")));
    }

    private Mono<Course> getCourseMono(Long courseId) {
        return courseRepository.findById(courseId)
            .switchIfEmpty(Mono.error(new NotFoundEntityException("Course with ID " + courseId + " not found")));
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
            .filter(course -> course.getTeacherId() != null)
            .map(Course::getTeacherId)
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
