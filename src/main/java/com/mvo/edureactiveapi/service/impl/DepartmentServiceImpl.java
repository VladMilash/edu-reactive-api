package com.mvo.edureactiveapi.service.impl;

import com.mvo.edureactiveapi.dto.TeacherDTO;
import com.mvo.edureactiveapi.dto.requestdto.DepartmentTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.DeleteResponseDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseDepartmentDTO;
import com.mvo.edureactiveapi.entity.Department;
import com.mvo.edureactiveapi.entity.Teacher;
import com.mvo.edureactiveapi.exeption.NotFoundEntityException;
import com.mvo.edureactiveapi.mapper.DepartmentMapper;
import com.mvo.edureactiveapi.repository.DepartmentRepository;
import com.mvo.edureactiveapi.repository.TeacherRepository;
import com.mvo.edureactiveapi.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final TeacherRepository teacherRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    public Mono<ResponseDepartmentDTO> save(DepartmentTransientDTO departmentTransientDTO) {
        log.info("Started creating department with name: {}", departmentTransientDTO.name());
        return departmentRepository
            .save(departmentMapper.fromDepartmentTransientDTO(departmentTransientDTO))
            .map(departmentMapper::toResponseDepartmentDTO)
            .doOnSuccess(dto -> log.info("Department successfully created with id: {}", dto.id()))
            .doOnError(error -> log.error("Failed to saving department", error));
    }

    @Transactional
    @Override
    public Flux<ResponseDepartmentDTO> getAll() {
        log.info("Started get all departments");
        Flux<Department> departmentFlux = departmentRepository.findAll();
        Mono<List<Teacher>> teacherFlux = getTeacherFlux(departmentFlux);

        return Mono.zip(
                departmentFlux.collectList(),
                teacherFlux
            ).flatMapMany(tuple -> {
                List<Department> departmentList = tuple.getT1();
                List<Teacher> teacherList = tuple.getT2();
                Map<Long, Teacher> teacherMap = getTeacherMap(teacherList);

                return Flux.fromIterable(departmentList)
                    .map(department -> {
                        TeacherDTO headOfDepartment = getTeacherDTO(department, teacherMap);
                        return getResponseDepartmentDTO(department, headOfDepartment);
                    });
            })
            .doOnComplete(() -> log.info("Successfully retrieved all departments"))
            .doOnError(error -> log.error("Failed to found all departments"));
    }

    @Transactional
    @Override
    public Mono<ResponseDepartmentDTO> getById(Long id) {
        log.info("Started get department with id: {}", id);
        Mono<Department> departmentMono = getDepartmentMono(id);
        return departmentMono.flatMap(department -> {
                Mono<Teacher> teacherMono = getTeacherMono(department);
                return teacherMono.map(teacher -> {
                    TeacherDTO headOfDepartment = getTeacherDTO(department, teacher);
                    return getResponseDepartmentDTO(department, headOfDepartment);
                });
            })
            .doOnSuccess(dto -> log.info("Department successfully found with id: {}", id))
            .doOnError(error -> log.error("Failed to found department with id: {}", id));
    }

    @Override
    public Mono<DeleteResponseDTO> delete(Long id) {
        log.info("Started delete department with id: {}", id);
        Mono<Department> departmentForDelete = getDepartmentMono(id);
        return departmentForDelete
            .flatMap(departmentRepository::delete)
            .doOnSuccess(studentDeleted -> log.info("Department with id: {} successfully deleted", id))
            .doOnError(error -> log.error("Failed to delete department", error))
            .then(Mono.just(new DeleteResponseDTO("Department deleted successfully")));
    }

    @Transactional
    @Override
    public Mono<ResponseDepartmentDTO> update(Long id, DepartmentTransientDTO departmentTransientDTO) {
        log.info("Started update department with id: {}", id);
        Mono<Department> departmentForUpdate = getDepartmentMono(id);
        return departmentForUpdate
            .flatMap(department -> {
                department.setName(departmentTransientDTO.name());
                return departmentRepository
                    .save(department)
                    .flatMap(updatedDepartment -> getById(updatedDepartment.getId()));
            })
            .doOnSuccess(updatedDepartment -> log.info("Department with id: {} successfully updated", id))
            .doOnError(error -> log.error("Failed to update department with id: {}", id));
    }

    @Transactional
    @Override
    public Mono<ResponseDepartmentDTO> setRelationWithTeacher(Long departmentId, Long teacherId) {
        log.info("Setting relations for department-teacher, with department id: {}, and teacher id: {}", departmentId, teacherId);
        Mono<Department> departmentMono = getDepartmentMono(departmentId);
        Mono<Teacher> teacherMono = getTeacherMono(teacherId);
        return Mono.zip(
                departmentMono,
                teacherMono
            ).flatMap(tuple -> {
                Department department = tuple.getT1();
                Teacher teacher = tuple.getT2();
                return saveRelationDepartmentWithTeacher(department, teacher);
            })
            .doOnSuccess(studentDeleted -> log.info("Successfully added teacher with id: {} to department with id: {}", teacherId, departmentId))
            .doOnError(error -> log.error("Failed to added teacher with id: {} to department with id: {}", teacherId, departmentId, error));
    }

    private Mono<ResponseDepartmentDTO> saveRelationDepartmentWithTeacher(Department department, Teacher teacher) {
        department.setHeadOfDepartment(teacher.getId());
        return departmentRepository
            .save(department)
            .flatMap(department1 -> departmentRepository.findById(department1.getId()))
            .map(department2 -> {
                TeacherDTO headOfDepartment = new TeacherDTO(
                    teacher.getId(),
                    teacher.getName()
                );
                return new ResponseDepartmentDTO(
                    department2.getId(),
                    department2.getName(),
                    headOfDepartment
                );
            });
    }

    private Mono<Teacher> getTeacherMono(Long teacherId) {
        return teacherRepository.findById(teacherId)
            .switchIfEmpty(Mono.error(new NotFoundEntityException("Teacher with ID " + teacherId + " not found")));
    }

    private Mono<List<Teacher>> getTeacherFlux(Flux<Department> departmentFlux) {
        Mono<List<Long>> teacherIds = departmentFlux.filter(department -> department.getHeadOfDepartment() != null).map(Department::getHeadOfDepartment).collectList();
        return teacherIds.flatMapMany(teacherRepository::findAllByIdIn).collectList().switchIfEmpty(Mono.just(Collections.emptyList()));
    }

    private static Map<Long, Teacher> getTeacherMap(List<Teacher> teacherList) {
        return teacherList
            .stream()
            .collect(Collectors.toMap(
                Teacher::getId,
                t -> t
            ));
    }

    private static ResponseDepartmentDTO getResponseDepartmentDTO(Department department, TeacherDTO headOfDepartment) {
        return new ResponseDepartmentDTO(
            department.getId(),
            department.getName(),
            headOfDepartment
        );
    }

    private static TeacherDTO getTeacherDTO(Department department, Map<Long, Teacher> teacherMap) {
        TeacherDTO headOfDepartment = null;
        if (department.getHeadOfDepartment() != null && teacherMap.containsKey(department.getHeadOfDepartment())) {
            Teacher teacher = teacherMap.get(department.getHeadOfDepartment());
            headOfDepartment = new TeacherDTO(
                teacher.getId(),
                teacher.getName()
            );
        }
        return headOfDepartment;
    }

    private static TeacherDTO getTeacherDTO(Department department, Teacher teacher) {
        TeacherDTO headOfDepartment = null;
        if (department.getHeadOfDepartment() != null) {
            headOfDepartment = new TeacherDTO(
                teacher.getId(),
                teacher.getName()
            );
        }
        return headOfDepartment;
    }

    private Mono<Department> getDepartmentMono(Long id) {
        return departmentRepository.findById(id)
            .switchIfEmpty(Mono.error(new NotFoundEntityException("Department with ID " + id + " not found")));
    }

    private Mono<Teacher> getTeacherMono(Department department) {
        return Optional.ofNullable(department.getHeadOfDepartment())
            .map(teacherRepository::findById)
            .orElse(Mono.just(new Teacher()));
    }
}
