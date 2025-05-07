package com.mvo.edureactiveapi.service.util;

import com.mvo.edureactiveapi.dto.*;
import com.mvo.edureactiveapi.entity.*;
import com.mvo.edureactiveapi.exeption.NotFoundEntityException;
import com.mvo.edureactiveapi.repository.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public final class EntityFetcher {

    private EntityFetcher() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static Mono<Course> getCourseMono(Long courseId, CourseRepository courseRepository) {
        return courseRepository.findById(courseId)
            .switchIfEmpty(Mono.error(new NotFoundEntityException("Course with ID " + courseId + " not found")));
    }

    public static Mono<Teacher> getTeacherMono(Long id, TeacherRepository teacherRepository) {
        return teacherRepository.findById(id)
            .switchIfEmpty(Mono.error(new NotFoundEntityException("Teacher with ID " + id + " not found")));
    }

    public static Mono<Student> getStudentMono(Long id, StudentRepository studentRepository) {
        return studentRepository.findById(id)
            .switchIfEmpty(Mono.error(new NotFoundEntityException("Student with ID " + id + " not found")));
    }

    public static Mono<Department> getDepartmentMono(Long id, DepartmentRepository departmentRepository) {
        return departmentRepository.findById(id)
            .switchIfEmpty(Mono.error(new NotFoundEntityException("Department with ID " + id + " not found")));
    }

    public static Mono<TeacherDTO> getTeacherDTOMono(Course course, TeacherRepository teacherRepository) {
        return Mono.justOrEmpty(course.getTeacherId())
            .flatMap(teacherRepository::findById)
            .map(teacher -> new TeacherDTO(teacher.getId(), teacher.getName()))
            .defaultIfEmpty(new TeacherDTO(null, null))
            .log();
    }

    public static Mono<TeacherDTO> getTeacherDTOMono(Department department, TeacherRepository teacherRepository) {
        return Mono.justOrEmpty(department.getHeadOfDepartment())
            .flatMap(teacherRepository::findById)
            .map(teacher -> new TeacherDTO(teacher.getId(), teacher.getName()))
            .defaultIfEmpty(new TeacherDTO(null, null))
            .log();
    }

    public static Flux<StudentDTO> getStudentDTOFlux(Course course,
                                                     StudentCourseRepository studentCourseRepository,
                                                     StudentRepository studentRepository) {
        return studentCourseRepository.findAllByCourseId(course.getId())
            .map(StudentCourse::getStudentId)
            .buffer(100)
            .flatMapSequential(studentRepository::findAllByIdIn)
            .map(student -> new StudentDTO(
                student.getId(),
                student.getName(),
                student.getEmail()
            ))
            .log();
    }

    public static Mono<DepartmentShortDTO> getDepartmentShortDTOMono(Teacher teacher,
                                                                     DepartmentRepository departmentRepository) {
        return departmentRepository.findByHeadOfDepartment(teacher.getId())
            .map(department -> new DepartmentShortDTO(
                department.getId(),
                department.getName()
            ))
            .switchIfEmpty(Mono.just(new DepartmentShortDTO(null, null)))
            .log();
    }

    public static Flux<CourseShortDTO> getCourseShortDTOFlux(Teacher teacher, CourseRepository courseRepository) {
        return courseRepository.findAllByTeacherId(teacher.getId())
            .map(course -> new CourseShortDTO(
                course.getId(),
                course.getTitle()
            ))
            .buffer(100)
            .flatMapSequential(Flux::fromIterable, 4)
            .log();
    }

  public static Flux<CourseDTO> getCourseDTOFlux(Student student,
                                             StudentCourseRepository studentCourseRepository,
                                             CourseRepository courseRepository,
                                             TeacherRepository teacherRepository) {
        return studentCourseRepository.findAllByStudentId(student.getId())
            .map(StudentCourse::getCourseId)
            .log()
            .buffer(100)
            .flatMapSequential(courseRepository::findAllByIdIn)
            .flatMap(course -> Mono.justOrEmpty(course.getTeacherId())
                .flatMap(teacherRepository::findById)
                .log()
                .map(teacher -> new TeacherDTO(teacher.getId(), teacher.getName()))
                .defaultIfEmpty(new TeacherDTO(null, null))
                .map(teacherDTO -> new CourseDTO(
                    course.getId(),
                    course.getTitle(),
                    teacherDTO
                )));
    }

}
