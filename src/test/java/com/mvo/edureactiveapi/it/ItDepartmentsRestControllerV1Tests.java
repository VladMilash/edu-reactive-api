package com.mvo.edureactiveapi.it;

import com.mvo.edureactiveapi.dto.requestdto.DepartmentTransientDTO;
import com.mvo.edureactiveapi.entity.Department;
import com.mvo.edureactiveapi.entity.Teacher;
import com.mvo.edureactiveapi.config.PostgreTestcontainerConfig;
import com.mvo.edureactiveapi.repository.DepartmentRepository;
import com.mvo.edureactiveapi.repository.TeacherRepository;
import com.mvo.edureactiveapi.util.DataUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(PostgreTestcontainerConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ItDepartmentsRestControllerV1Tests {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    private DepartmentTransientDTO departmentTransientDTO;

    private Department department;

    private Teacher teacher;

    @BeforeEach
    void setUp() {
        departmentTransientDTO = DataUtil.getDepartmentTransientDTO();
        department = DataUtil.getDepartmentEntity();
        teacher = DataUtil.getTeacherEntity();
    }

    @Test
    @DisplayName("Test save department functionality")
    public void givenDepartmentTransientDTO_whenSaveDepartment_thenSuccessResponse() {
        // given

        // when
        WebTestClient.ResponseSpec result = webTestClient.post()
            .uri("/api/v1/departments/")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(departmentTransientDTO), DepartmentTransientDTO.class)
            .exchange();

        // then
        result.expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.id").exists()
            .jsonPath("$.name").isEqualTo("test")
            .jsonPath("$.headOfDepartment").isEmpty();
    }

    @Test
    @DisplayName("Test get department by id functionality")
    public void givenDepartmentId_whenGetDepartment_thenSuccessResponse() {
        // given
        departmentRepository.save(department).block();

        // when
        WebTestClient.ResponseSpec result = webTestClient.get()
            .uri("/api/v1/departments/{id}", department.getId())
            .exchange();

        // then
        result.expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").exists()
            .jsonPath("$.name").isEqualTo("test")
            .jsonPath("$.headOfDepartment").isEmpty();
    }

    @Test
    @DisplayName("Test get department by incorrect id functionality")
    public void givenIncorrectId_whenGetById_thenErrorResponse() {
        // given
        long incorrectId = 200L;

        // when
        WebTestClient.ResponseSpec result = webTestClient.get()
            .uri("/api/v1/departments/{id}", incorrectId)
            .exchange();

        // then
        result.expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.message").isEqualTo("Department with ID " + incorrectId + " not found");
    }

    @Test
    @DisplayName("Test get all departments functionality")
    public void givenGetDepartmentsRequest_whenGetStudents_thenNonEmptyList() {
        // given
        departmentRepository.save(department).block();

        // when
        WebTestClient.ResponseSpec result = webTestClient.get()
            .uri("/api/v1/departments/")
            .exchange();

        // then
        result.expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$").isNotEmpty();
    }

    @Test
    @DisplayName("Update department by id functionality")
    public void givenDepartmentId_whenUpdateDepartment_thenSuccessResponse() {
        // given
        departmentRepository.save(department).block();

        // when
        WebTestClient.ResponseSpec result = webTestClient.put()
            .uri("/api/v1/departments/{id}", department.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(departmentTransientDTO), DepartmentTransientDTO.class)
            .exchange();

        // then
        result.expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").exists()
            .jsonPath("$.name").isEqualTo(departmentTransientDTO.name())
            .jsonPath("$.headOfDepartment").isEmpty();
    }

    @Test
    @DisplayName("Update department by incorrect id functionality")
    public void givenIncorrectId_whenUpdateDepartment_thenErrorResponse() {
        // given
        long incorrectId = 200L;

        // when
        WebTestClient.ResponseSpec result = webTestClient.put()
            .uri("/api/v1/departments/{id}", incorrectId)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(departmentTransientDTO), DepartmentTransientDTO.class)
            .exchange();

        // then
        result.expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.message").isEqualTo("Department with ID " + incorrectId + " not found");
    }

    @Test
    @DisplayName("Delete department by id functionality")
    public void givenDepartmentId_whenDeleteDepartment_thenDeletedResponse() {
        // given
        departmentRepository.save(department).block();

        // when
        WebTestClient.ResponseSpec result = webTestClient.delete()
            .uri("/api/v1/departments/{id}", department.getId())
            .exchange();

        // then
        result.expectStatus().isOk()
            .expectBody()
            .jsonPath("$.message").isEqualTo("Department deleted successfully");
    }

    @Test
    @DisplayName("Delete department by incorrect id functionality")
    public void givenIncorrectId_whenDeleteDepartment_thenErrorResponse() {
        // given
        long incorrectId = 200L;

        // when
        WebTestClient.ResponseSpec result = webTestClient.delete()
            .uri("/api/v1/departments/{id}", incorrectId)
            .exchange();

        // then
        result.expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.message").isEqualTo("Department with ID " + incorrectId + " not found");
    }

    @Test
    @DisplayName("Set relation department-teacher functionality")
    public void givenDepartmentIdAndTeacherId_whenSetRelationWitTeacherDepartment_thenSuccessResponse() {
        // given
        departmentRepository.save(department).block();
        teacherRepository.save(teacher).block();

        // when
        WebTestClient.ResponseSpec result = webTestClient.post()
            .uri("/api/v1/departments/{departmentId}/teacher/{teacherId}", department.getId(), teacher.getId())
            .exchange();

        // then
        result.expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").exists()
            .jsonPath("$.name").isEqualTo("test")
            .jsonPath("$.headOfDepartment.id").isEqualTo(teacher.getId())
            .jsonPath("$.headOfDepartment.name").isEqualTo("test");
    }

    @Test
    @DisplayName("Set relation department-teacher with incorrect department id and correct teacher id functionality")
    public void givenIncorrectDepartmentIdAndCorrectTeacherId_whenSetRelationWitTeacherDepartment_thenErrorResponse() {
        // given
        teacherRepository.save(teacher).block();
        long incorrectDepartmentId = 200L;

        // when
        WebTestClient.ResponseSpec result = webTestClient.post()
            .uri("/api/v1/departments/{departmentId}/teacher/{teacherId}", incorrectDepartmentId, teacher.getId())
            .exchange();

        // then
        result.expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.message").isEqualTo("Department with ID " + incorrectDepartmentId + " not found");
    }

    @Test
    @DisplayName("Set relation department-teacher with correct department id and incorrect teacher id functionality")
    public void givenCorrectDepartmentIdIncorrectAndTeacherId_whenSetRelationWitTeacherDepartment_thenErrorResponse() {
        // given
        departmentRepository.save(department).block();
        long incorrectTeacherId = 200L;

        // when
        WebTestClient.ResponseSpec result = webTestClient.post()
            .uri("/api/v1/departments/{departmentId}/teacher/{teacherId}", department.getId(), incorrectTeacherId)
            .exchange();

        // then
        result.expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.message").isEqualTo("Teacher with ID " + incorrectTeacherId + " not found");
    }
}
