package com.mvo.edureactiveapi.service.impl;

import com.mvo.edureactiveapi.dto.TeacherDTO;
import com.mvo.edureactiveapi.dto.requestdto.DepartmentTransientDTO;
import com.mvo.edureactiveapi.dto.responsedto.DeleteResponseDTO;
import com.mvo.edureactiveapi.dto.responsedto.ResponseDepartmentDTO;
import com.mvo.edureactiveapi.entity.Department;
import com.mvo.edureactiveapi.entity.Teacher;
import com.mvo.edureactiveapi.mapper.DepartmentMapper;
import com.mvo.edureactiveapi.repository.DepartmentRepository;
import com.mvo.edureactiveapi.repository.TeacherRepository;
import com.mvo.edureactiveapi.service.DepartmentService;
import com.mvo.edureactiveapi.service.util.EntityFetcher;
import com.mvo.edureactiveapi.service.util.ResponseDtoBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final TeacherRepository teacherRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    public Mono<ResponseDepartmentDTO> save(DepartmentTransientDTO departmentTransientDTO) {
        return departmentRepository
            .save(departmentMapper.fromDepartmentTransientDTO(departmentTransientDTO))
            .map(departmentMapper::toResponseDepartmentDTO)
            .doOnSuccess(dto -> log.info("Department successfully created with id: {}", dto.id()))
            .doOnError(error -> log.error("Failed to saving department", error));
    }

    @Transactional
    @Override
    public Flux<ResponseDepartmentDTO> getAll(int page, int size) {
        long offset = (long) page * size;
        return departmentRepository.findAllWithPagination(size, offset)
            .flatMap(department -> {
                Mono<TeacherDTO> teacherDTOMono = EntityFetcher.getTeacherDTOMono(department, teacherRepository);
                return ResponseDtoBuilder.getResponseDepartmentDTOMono(department, teacherDTOMono);
            })
            .doOnComplete(() -> log.info("Successfully retrieved all departments"))
            .doOnError(error -> log.error("Failed to found all departments", error));
    }

    @Transactional
    @Override
    public Mono<ResponseDepartmentDTO> getById(Long id) {
        Mono<Department> departmentMono = EntityFetcher.getDepartmentMono(id, departmentRepository);
        return departmentMono.flatMap(department -> {
                Mono<TeacherDTO> teacherDTOMono = EntityFetcher.getTeacherDTOMono(department, teacherRepository);
                return ResponseDtoBuilder.getResponseDepartmentDTOMono(department, teacherDTOMono);
            })
            .doOnSuccess(dto -> log.info("Department successfully found with id: {}", id))
            .doOnError(error -> log.error("Failed to found department with id: {}", id, error));
    }

    @Override
    public Mono<DeleteResponseDTO> delete(Long id) {
        Mono<Department> departmentForDelete = EntityFetcher.getDepartmentMono(id, departmentRepository);
        return departmentForDelete
            .flatMap(departmentRepository::delete)
            .doOnSuccess(studentDeleted -> log.info("Department with id: {} successfully deleted", id))
            .doOnError(error -> log.error("Failed to delete department", error))
            .then(Mono.just(new DeleteResponseDTO("Department deleted successfully")));
    }

    @Transactional
    @Override
    public Mono<ResponseDepartmentDTO> update(Long id, DepartmentTransientDTO departmentTransientDTO) {
        Mono<Department> departmentForUpdate = EntityFetcher.getDepartmentMono(id, departmentRepository);
        return departmentForUpdate
            .flatMap(department -> {
                department.setName(departmentTransientDTO.name());
                return departmentRepository
                    .save(department)
                    .flatMap(updatedDepartment -> getById(updatedDepartment.getId()));
            })
            .doOnSuccess(updatedDepartment -> log.info("Department with id: {} successfully updated", id))
            .doOnError(error -> log.error("Failed to update department with id: {}", id, error));
    }

    @Transactional
    @Override
    public Mono<ResponseDepartmentDTO> setRelationWithTeacher(Long departmentId, Long teacherId) {
        Mono<Department> departmentMono = EntityFetcher.getDepartmentMono(departmentId, departmentRepository);
        Mono<Teacher> teacherMono = EntityFetcher.getTeacherMono(teacherId, teacherRepository);
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

}
