package com.mvo.edureactiveapi.service.impl;

import com.mvo.edureactiveapi.dto.CourseShortDTO;
import com.mvo.edureactiveapi.dto.DepartmentShortDTO;
import com.mvo.edureactiveapi.dto.requestdto.TeacherTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.DeleteResponseDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseCoursesDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseTeacherDTO;
import com.mvo.edureactiveapi.entity.Course;
import com.mvo.edureactiveapi.entity.Department;
import com.mvo.edureactiveapi.entity.Teacher;
import com.mvo.edureactiveapi.exeption.NotFoundEntityException;
import com.mvo.edureactiveapi.mapper.TeacherMapper;
import com.mvo.edureactiveapi.repository.CourseRepository;
import com.mvo.edureactiveapi.repository.DepartmentRepository;
import com.mvo.edureactiveapi.repository.TeacherRepository;
import com.mvo.edureactiveapi.service.CourseService;
import com.mvo.edureactiveapi.service.TeacherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
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
        log.info("Started creating teacher with name: {}", teacherTransientDTO.name());
        return teacherRepository
            .save(teacherMapper.fromTeacherTransientDTO(teacherTransientDTO))
            .map(teacherMapper::toResponseTeacherDTO)
            .doOnSuccess(dto -> log.info("Teacher successfully created with id: {}", dto.id()))
            .doOnError(error -> log.error("Failed to saving Teacher", error));
    }

    @Transactional
    @Override
    public Flux<ResponseTeacherDTO> getAll() {
        log.info("Started get all teachers");
        Flux<Teacher> teacherFlux = teacherRepository.findAll();
        Mono<List<Long>> teacherIds = teacherFlux.map(Teacher::getId).collectList();
        Flux<Course> courseFlux = teacherIds.flatMapMany(courseRepository::findAllByTeacherIdIn);
        Flux<Department> departmentFlux = teacherIds.flatMapMany(departmentRepository::findAllByHeadOfDepartmentIn);

        return Mono.zip(
                teacherFlux.collectList(),
                courseFlux.collectList(),
                departmentFlux.collectList()
            ).flatMapMany(tuple -> {
                List<Teacher> teacherList = tuple.getT1();
                List<Course> courseList = tuple.getT2();
                List<Department> departmentList = tuple.getT3();
                Map<Long, List<Long>> teacherToCourses = getTeacherToCourses(courseList);
                Map<Long, Department> teacherIdToDepartment = getTeacherIdToDepartment(departmentList);
                Map<Long, Course> courseMap = getCourseMap(courseList);

                return Flux.fromIterable(teacherList)
                    .map(teacher -> {
                        List<Long> courseIds = teacherToCourses
                            .getOrDefault(teacher.getId(), Collections.emptyList());
                        Set<CourseShortDTO> courseShortDTOs = getCourseShortDTOs(courseIds, courseMap);
                        DepartmentShortDTO departmentShortDTO = getDepartmentShortDTO(teacher, teacherIdToDepartment);
                        return getResponseTeacherDTO(teacher, courseShortDTOs, departmentShortDTO);
                    });
            })
            .doOnComplete(() -> log.info("Successfully retrieved all teachers"))
            .doOnError(error -> log.error("Failed to found all teachers"));
    }

    @Transactional
    @Override
    public Mono<ResponseTeacherDTO> getById(Long id) {
        log.info("Started get teacher with id: {}", id);
        Mono<Teacher> teacherMono = getTeacherMono(id);
        return teacherMono.flatMap(teacher -> {
                Mono<List<Course>> courseListMono = getCourseListMono(id);
                Mono<Department> departmentMono = getDepartmentMonoByHeadOfDepartment(id);
                return Mono.zip(
                    courseListMono,
                    departmentMono
                ).map(tuple -> {
                    List<Course> courseList = tuple.getT1();
                    Department department = tuple.getT2();
                    Set<CourseShortDTO> courseShortDTOs = getCourseShortDTOs(courseList);
                    DepartmentShortDTO departmentShortDTO = getDepartmentShortDTO(department);
                    return getResponseTeacherDTO(teacher, courseShortDTOs, departmentShortDTO);
                });
            })
            .doOnSuccess(responseTeacherDTO -> log.info("Teacher successfully found with id: {}", id))
            .doOnError(error -> log.error("Failed to found teacher with id: {}", id));
    }


    @Transactional
    @Override
    public Mono<ResponseTeacherDTO> update(Long id, TeacherTransientDTO teacherTransientDTO) {
        log.info("Started update teacher with id: {}", id);
        Mono<Teacher> teacherForUpdate = getTeacherMono(id);
        return teacherForUpdate
            .flatMap(teacher -> {
                teacher.setName(teacherTransientDTO.name());
                return teacherRepository
                    .save(teacher)
                    .flatMap(teacher1 -> getById(teacher1.getId()));
            })
            .doOnSuccess(updatedTeacher -> log.info("Teacher with id: {} successfully updated", id))
            .doOnError(error -> log.error("Failed to update teacher with id: {}", id));
    }

    @Override
    public Mono<DeleteResponseDTO> delete(Long id) {
        log.info("Started delete teacher with id: {}", id);
        Mono<Teacher> teacherForDelete = getTeacherMono(id);
        return teacherForDelete
            .flatMap(teacherRepository::delete)
            .doOnSuccess(studentDeleted -> log.info("Teacher with id: {} successfully deleted", id))
            .doOnError(error -> log.error("Failed to delete teacher", error))
            .then(Mono.just(new DeleteResponseDTO("Teacher deleted successfully")));
    }

    @Transactional
    @Override
    public Mono<ResponseCoursesDTO> setRelationTeacherWithCourse(Long teacherId, Long courseId) {
        log.info("Setting relations for teacher-course, with teacher id: {}, and course id: {}", teacherId, courseId);
        Mono<Teacher> teacherMono = getTeacherMono(teacherId);
        Mono<Course> courseMono = getCourseMono(courseId);
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

    private Mono<Course> getCourseMono(Long courseId) {
        return courseRepository.findById(courseId)
            .switchIfEmpty(Mono.error(new NotFoundEntityException("Course with ID " + courseId + " not found")));
    }

    private static ResponseTeacherDTO getResponseTeacherDTO(Teacher teacher, Set<CourseShortDTO> courseShortDTOs, DepartmentShortDTO departmentShortDTO) {
        return new ResponseTeacherDTO(
            teacher.getId(),
            teacher.getName(),
            courseShortDTOs,
            departmentShortDTO
        );
    }

    private static DepartmentShortDTO getDepartmentShortDTO(Teacher teacher, Map<Long, Department> teacherIdToDepartment) {
        DepartmentShortDTO departmentShortDTO = null;

        if (teacherIdToDepartment.containsKey(teacher.getId())) {
            Department department = teacherIdToDepartment.get(teacher.getId());
            departmentShortDTO = new DepartmentShortDTO(department.getId(), department.getName());
        }
        return departmentShortDTO;
    }

    private static DepartmentShortDTO getDepartmentShortDTO(Department department) {
        DepartmentShortDTO departmentShortDTO;
        if (department.getId() != null) {
            departmentShortDTO = new DepartmentShortDTO(
                department.getId(),
                department.getName()
            );
        } else {
            departmentShortDTO = null;
        }
        return departmentShortDTO;
    }

    private static Set<CourseShortDTO> getCourseShortDTOs(List<Long> courseIds, Map<Long, Course> courseMap) {
        Set<CourseShortDTO> courseShortDTOs;

        if (!courseIds.isEmpty()) {
            courseShortDTOs = courseIds.stream()
                .map(id -> {
                    Course course = courseMap.get(id);
                    return new CourseShortDTO(
                        course.getId(),
                        course.getTitle()
                    );
                }).collect(Collectors.toSet());
        } else {
            courseShortDTOs = Collections.emptySet();
        }
        return courseShortDTOs;
    }

    private static Set<CourseShortDTO> getCourseShortDTOs(List<Course> courseList) {
        Set<CourseShortDTO> courseShortDTOs;
        if (!courseList.isEmpty()) {
            courseShortDTOs = courseList
                .stream()
                .map(course -> {
                    return new CourseShortDTO(
                        course.getId(),
                        course.getTitle()
                    );
                })
                .collect(Collectors.toSet());
        } else {
            courseShortDTOs = Collections.emptySet();
        }
        return courseShortDTOs;
    }

    private static Map<Long, Course> getCourseMap(List<Course> courseList) {
        return courseList
            .stream()
            .collect(Collectors.toMap(Course::getId, c -> c));
    }

    private static Map<Long, Department> getTeacherIdToDepartment(List<Department> departmentList) {
        return departmentList
            .stream()
            .collect(Collectors.toMap(
                Department::getHeadOfDepartment,
                d -> d
            ));
    }

    private static Map<Long, List<Long>> getTeacherToCourses(List<Course> courseList) {
        return courseList
            .stream()
            .collect(Collectors.groupingBy(
                Course::getTeacherId,
                Collectors.mapping(Course::getId, Collectors.toList())
            ));
    }

    private Mono<Teacher> getTeacherMono(Long id) {
        return teacherRepository.findById(id)
            .switchIfEmpty(Mono.error(new NotFoundEntityException("Teacher with ID " + id + " not found")));
    }


    private Mono<List<Course>> getCourseListMono(Long id) {
        return courseRepository.findAllByTeacherId(id).collectList().defaultIfEmpty(Collections.emptyList());
    }

    private Mono<Department> getDepartmentMonoByHeadOfDepartment(Long headOfDepartment) {
        return departmentRepository
            .findByHeadOfDepartment(headOfDepartment)
            .switchIfEmpty(Mono.just(new Department()));
    }
}
