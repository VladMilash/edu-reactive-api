package com.mvo.edureactiveapi.service.impl;

import com.mvo.edureactiveapi.dto.CourseDTO;
import com.mvo.edureactiveapi.dto.requestdto.StudentTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.DeleteResponseDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseStudentDTO;
import com.mvo.edureactiveapi.entity.Course;
import com.mvo.edureactiveapi.entity.Student;
import com.mvo.edureactiveapi.entity.StudentCourse;
import com.mvo.edureactiveapi.exeption.AlReadyExistException;
import com.mvo.edureactiveapi.mapper.StudentMapper;
import com.mvo.edureactiveapi.repository.CourseRepository;
import com.mvo.edureactiveapi.repository.StudentCourseRepository;
import com.mvo.edureactiveapi.repository.StudentRepository;
import com.mvo.edureactiveapi.repository.TeacherRepository;
import com.mvo.edureactiveapi.service.StudentService;
import com.mvo.edureactiveapi.service.util.EntityFetcher;
import com.mvo.edureactiveapi.service.util.ResponseDtoBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
@Slf4j
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final StudentCourseRepository studentCourseRepository;

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
                    .doOnError(error -> log.error("Failed to saving student", error))
                    .log();
            });
    }

    @Transactional
    @Override
    public Flux<ResponseStudentDTO> getAll() {
        log.info("Starting get all students");
        return studentRepository.findAll()
            .flatMap(student -> {
                Flux<CourseDTO> courseDTOFlux = EntityFetcher.getCourseDTOFlux(student, studentCourseRepository, courseRepository, teacherRepository);
                return ResponseDtoBuilder.getResponseStudentDTOMono(student, courseDTOFlux);
            })
            .log()
            .doOnComplete(() -> log.info("Successfully retrieved all students"))
            .doOnError(error -> log.error("Failed to found all students", error));
    }

    @Transactional
    @Override
    public Mono<ResponseStudentDTO> getById(Long id) {
        log.info("Starting get student with id: {}", id);
        Mono<Student> studentMono = EntityFetcher.getStudentMono(id, studentRepository);
        return studentMono.flatMap(student -> {
                Flux<CourseDTO> courseDTOFlux = EntityFetcher.getCourseDTOFlux(student, studentCourseRepository, courseRepository, teacherRepository);
                return ResponseDtoBuilder.getResponseStudentDTOMono(student, courseDTOFlux);
            })
            .doOnSuccess(responseStudentDTO -> log.info("Student successfully found with id: {}", responseStudentDTO.id()))
            .doOnError(error -> log.error("Failed to found student with id: {}", id, error));
    }

    @Transactional
    @Override
    public Mono<ResponseStudentDTO> update(Long id, StudentTransientDTO studentTransientDTO) {
        log.info("Started update student with id: {}", id);
        Mono<Student> studentForUpdate = EntityFetcher.getStudentMono(id, studentRepository);
        return studentForUpdate
            .flatMap(foundedStudent -> {
                foundedStudent.setName(studentTransientDTO.name());
                foundedStudent.setEmail(studentTransientDTO.email());
                return studentRepository.save(foundedStudent)
                    .then(getById(id));
            })
            .doOnSuccess(updatedStudent -> log.info("Student with id: {} successfully updated", id))
            .doOnError(error -> log.error("Failed to update student with id: {}", id, error));
    }

    @Override
    public Mono<DeleteResponseDTO> delete(Long id) {
        log.info("Started delete student with id: {}", id);
        Mono<Student> studentForDelete = EntityFetcher.getStudentMono(id, studentRepository);
        return studentForDelete
            .flatMap(studentRepository::delete)
            .doOnSuccess(studentDeleted -> log.info("Student with id: {} successfully deleted", id))
            .doOnError(error -> log.error("Failed to delete student", error))
            .then(Mono.just(new DeleteResponseDTO("Student deleted successfully")));
    }

    @Transactional
    @Override
    public Mono<ResponseStudentDTO> setRelationWithCourse(Long studentId, Long courseId) {
        Mono<Student> studentMono = EntityFetcher.getStudentMono(studentId, studentRepository);
        Mono<Course> courseMono = EntityFetcher.getCourseMono(courseId, courseRepository);
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
            .log()
            .doOnSuccess(dto -> log.info("Successfully set relation between student {} and course {}", studentId, courseId))
            .doOnError(error -> log.error("Failed to set relation between student {} and course {}", studentId, courseId, error));
    }

    @Transactional
    @Override
    public Flux<CourseDTO> getStudentCourses(Long id) {
        Mono<Student> studentMono = EntityFetcher.getStudentMono(id, studentRepository);
        return studentMono.flatMapMany(student -> EntityFetcher.getCourseDTOFlux(student, studentCourseRepository, courseRepository, teacherRepository))
            .log()
            .doOnComplete(() -> log.info("Courses for student with id: {} successfully found ", id))
            .doOnError(error -> log.error("Failed to found courses for student with id: {}", id, error));
    }

}
