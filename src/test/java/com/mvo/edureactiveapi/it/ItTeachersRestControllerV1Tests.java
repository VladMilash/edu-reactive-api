package com.mvo.edureactiveapi.it;

import com.mvo.edureactiveapi.dto.requestdto.TeacherTransientDTO;
import com.mvo.edureactiveapi.entity.Course;
import com.mvo.edureactiveapi.entity.Teacher;
import com.mvo.edureactiveapi.config.PostgreTestcontainerConfig;
import com.mvo.edureactiveapi.repository.CourseRepository;
import com.mvo.edureactiveapi.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import org.springframework.http.MediaType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(PostgreTestcontainerConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ItTeachersRestControllerV1Tests {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private CourseRepository courseRepository;

    private TeacherTransientDTO teacherTransientDTO;

    @BeforeEach
    void setUp() {
        teacherTransientDTO = new TeacherTransientDTO("test");
        teacherRepository.deleteAll().block();
        courseRepository.deleteAll().block();
    }

    @Test
    @DisplayName("Test save teacher functionality")
    public void givenTeacherTransientDTO_whenSaveDepartment_thenSuccessResponse() {
        // given

        // when
        WebTestClient.ResponseSpec result = webTestClient.post()
            .uri("/api/v1/teachers")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(teacherTransientDTO), TeacherTransientDTO.class)
            .exchange();

        // then
        result.expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").exists()
            .jsonPath("$.name").isEqualTo("test")
            .jsonPath("$.courses").isArray()
            .jsonPath("$.courses").isEmpty()
            .jsonPath("$.department").isEmpty();
    }

    @Test
    @DisplayName("Test get teacher by id functionality")
    public void givenTeacherId_whenGetDepartment_thenSuccessResponse() {
        // given
        Teacher teacher = new Teacher();
        teacher.setName("test");
        Teacher savedTeacher = teacherRepository.save(teacher).block();

        // when
        WebTestClient.ResponseSpec result = webTestClient.get()
            .uri("/api/v1/teachers/{id}", savedTeacher.getId())
            .exchange();

        // then
        result.expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").exists()
            .jsonPath("$.name").isEqualTo("test")
            .jsonPath("$.courses").isArray()
            .jsonPath("$.courses").isEmpty()
            .jsonPath("$.department").isEmpty();
    }

    @Test
    @DisplayName("Test get teacher by incorrect id functionality")
    public void givenIncorrectId_whenGetById_thenErrorResponse() {
        // given
        long incorrectId = 200L;

        // when
        WebTestClient.ResponseSpec result = webTestClient.get()
            .uri("/api/v1/teachers/{id}", incorrectId)
            .exchange();

        // then
        result.expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.message").isEqualTo("Teacher with ID " + incorrectId + " not found");
    }

    @Test
    @DisplayName("Test get all teachers functionality")
    public void givenGetTeachersRequest_whenGetStudents_thenNonEmptyList() {
        // given
        Teacher teacher = new Teacher();
        teacher.setName("test");
        teacherRepository.save(teacher).block();

        // when
        WebTestClient.ResponseSpec result = webTestClient.get()
            .uri("/api/v1/teachers")
            .exchange();

        // then
        result.expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$").isNotEmpty();
    }

    @Test
    @DisplayName("Update teacher by id functionality")
    public void givenDepartmentId_whenUpdateDepartment_thenSuccessResponse() {
        // given
        Teacher teacher = new Teacher();
        teacher.setName("new");
        Teacher savedTeacher = teacherRepository.save(teacher).block();

        // when
        WebTestClient.ResponseSpec result = webTestClient.put()
            .uri("/api/v1/teachers/{id}", savedTeacher.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(teacherTransientDTO), TeacherTransientDTO.class)
            .exchange();

        // then
        result.expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").exists()
            .jsonPath("$.name").isEqualTo("test")
            .jsonPath("$.courses").isArray()
            .jsonPath("$.courses").isEmpty()
            .jsonPath("$.department").isEmpty();
    }

    @Test
    @DisplayName("Update teacher by incorrect id functionality")
    public void givenIncorrectId_whenUpdateTeacher_thenErrorResponse() {
        // given
        long incorrectId = 200L;

        // when
        WebTestClient.ResponseSpec result = webTestClient.put()
            .uri("/api/v1/teachers/{id}", incorrectId)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(teacherTransientDTO), TeacherTransientDTO.class)
            .exchange();

        // then
        result.expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.message").isEqualTo("Teacher with ID " + incorrectId + " not found");
    }

    @Test
    @DisplayName("Delete teacher by id functionality")
    public void givenTeacherId_whenDeleteTeacher_thenDeletedResponse() {
        // given
        Teacher teacher = new Teacher();
        teacher.setName("new");
        Teacher savedTeacher = teacherRepository.save(teacher).block();

        // when
        WebTestClient.ResponseSpec result = webTestClient.delete()
            .uri("/api/v1/teachers/{id}", savedTeacher.getId())
            .exchange();

        // then
        result.expectStatus().isOk()
            .expectBody()
            .jsonPath("$.message").isEqualTo("Teacher deleted successfully");
    }

    @Test
    @DisplayName("Delete teacher by incorrect id functionality")
    public void givenIncorrectId_whenDeleteTeacher_thenErrorResponse() {
        // given
        long incorrectId = 200L;

        // when
        WebTestClient.ResponseSpec result = webTestClient.delete()
            .uri("/api/v1/teachers/{id}", incorrectId)
            .exchange();

        // then
        result.expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.message").isEqualTo("Teacher with ID " + incorrectId + " not found");
    }

    @Test
    @DisplayName("Set relation teacher-course functionality")
    public void givenTeacherIdAndCourseId_whenSetRelationWitTeacherCourse_thenSuccessResponse() {
        // given
        Teacher teacher = new Teacher();
        teacher.setName("test");
        Teacher savedTeacher = teacherRepository.save(teacher).block();
        Course course = new Course();
        course.setTitle("test");
        Course savedCourse = courseRepository.save(course).block();

        // when
        WebTestClient.ResponseSpec result = webTestClient.post()
            .uri("/api/v1/teachers/{teacherId}/courses/{courseId}", savedTeacher.getId(), savedCourse.getId())
            .exchange();

        // then
        result.expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").exists()
            .jsonPath("$.title").isEqualTo("test")
            .jsonPath("$.teacher.id").exists()
            .jsonPath("$.teacher.name").isEqualTo("test")
            .jsonPath("$.students").isEmpty();
    }

    @Test
    @DisplayName("Set relation teacher-course with incorrect teacher id and correct course id functionality")
    public void givenIncorrectTeacherIdAndCorrectCourseId_whenSetRelationWitTeacherCourse_thenErrorResponse() {
        // given
        Course course = new Course();
        course.setTitle("test");
        Course savedCourse = courseRepository.save(course).block();
        long incorrectTeacherId = 200L;

        // when
        WebTestClient.ResponseSpec result = webTestClient.post()
            .uri("/api/v1/teachers/{teacherId}/courses/{courseId}", incorrectTeacherId, savedCourse.getId())
            .exchange();

        // then
        result.expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.message").isEqualTo("Teacher with ID " + incorrectTeacherId + " not found");
    }

    @Test
    @DisplayName("Set relation teacher-course with correct teacher id and incorrect course id functionality")
    public void givenCorrectTeacherIdAndIncorrectCourseId_whenSetRelationWitTeacherCourse_thenErrorResponse() {
        // given
        Teacher teacher = new Teacher();
        teacher.setName("test");
        Teacher savedTeacher = teacherRepository.save(teacher).block();
        long incorrectCourseId = 200L;

        // when
        WebTestClient.ResponseSpec result = webTestClient.post()
            .uri("/api/v1/teachers/{teacherId}/courses/{courseId}", savedTeacher.getId(), incorrectCourseId)
            .exchange();

        // then
        result.expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.message").isEqualTo("Course with ID " + incorrectCourseId + " not found");
    }
}
