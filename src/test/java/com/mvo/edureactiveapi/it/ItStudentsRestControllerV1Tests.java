package com.mvo.edureactiveapi.it;

import com.mvo.edureactiveapi.dto.requestdto.StudentTransientDTO;
import com.mvo.edureactiveapi.entity.Course;
import com.mvo.edureactiveapi.entity.Student;
import com.mvo.edureactiveapi.entity.StudentCourse;
import com.mvo.edureactiveapi.config.PostgreTestcontainerConfig;
import com.mvo.edureactiveapi.repository.CourseRepository;
import com.mvo.edureactiveapi.repository.StudentCourseRepository;
import com.mvo.edureactiveapi.repository.StudentRepository;
import com.mvo.edureactiveapi.util.DataUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(PostgreTestcontainerConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ItStudentsRestControllerV1Tests {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentCourseRepository studentCourseRepository;

    private StudentTransientDTO studentTransientDTO;

    private Student student;

    private Course course;

    @BeforeEach
    void setUp() {
        studentTransientDTO = DataUtil.getStudentTransientDTO();
        student = DataUtil.getStudentEntity();
        course = DataUtil.getCourseEntity();
        studentCourseRepository.deleteAll().block();
        studentRepository.deleteAll().block();
        courseRepository.deleteAll().block();
    }

    @Test
    @DisplayName("Test save student functionality")
    public void givenStudentTransientDTO_whenSaveStudent_thenSuccessResponse() {
        // given

        // when
        WebTestClient.ResponseSpec result = webTestClient.post()
            .uri("/api/v1/students/")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(studentTransientDTO), StudentTransientDTO.class)
            .exchange();

        //then
        result.expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.id").exists()
            .jsonPath("$.name").isEqualTo("test")
            .jsonPath("$.email").isEqualTo("test@test.ru")
            .jsonPath("$.courses").isEmpty();
    }

    @Test
    @DisplayName("Test create student with duplicate email functionality")
    public void givenStudentTransientDTOWithDuplicateEmail_whenSaveStudent_thenErrorResponse() {
        // given
        String duplicateEmail = "duplicate@mail.com";
        student.setEmail(duplicateEmail);
        studentRepository.save(student).block();
        StudentTransientDTO studentTransientDTOWithDuplicateEmail = new StudentTransientDTO("new", duplicateEmail);

        // when
        WebTestClient.ResponseSpec result = webTestClient.post()
            .uri("/api/v1/students/")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(studentTransientDTOWithDuplicateEmail), StudentTransientDTO.class)
            .exchange();

        // then
        result.expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.status").isEqualTo(400)
            .jsonPath("$.message").isEqualTo("The email: " + duplicateEmail + " was used for registration earlier");
    }

    @Test
    @DisplayName("Test get student by id functionality")
    public void givenStudentId_whenGetStudent_thenSuccessResponse() {
        // given
        studentRepository.save(student).block();

        // when
        WebTestClient.ResponseSpec result = webTestClient.get()
            .uri("/api/v1/students/{id}", student.getId())
            .exchange();

        // then
        result.expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").exists()
            .jsonPath("$.name").isEqualTo(student.getName())
            .jsonPath("$.email").isEqualTo(student.getEmail())
            .jsonPath("$.courses").isEmpty();
    }

    @Test
    @DisplayName("Test get student by incorrect id functionality")
    public void givenIncorrectId_whenGetById_thenErrorResponse() {
        // given
        long incorrectId = 200L;

        // when
        WebTestClient.ResponseSpec result = webTestClient.get()
            .uri("/api/v1/students/{id}", incorrectId)
            .exchange();

        // then
        result.expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.message").isEqualTo("Student with ID " + incorrectId + " not found");
    }

    @Test
    @DisplayName("Test get all students functionality")
    public void givenGetStudentsRequest_whenGetStudents_thenNonEmptyList() {
        // given
        studentRepository.save(student).block();

        // when
        WebTestClient.ResponseSpec result = webTestClient.get()
            .uri("/api/v1/students/")
            .exchange();

        // then
        result.expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$").isNotEmpty();
    }

    @Test
    @DisplayName("Update student by id functionality")
    public void givenStudentId_whenUpdateStudent_thenSuccessResponse() {
        // given
        studentRepository.save(student).block();

        // when
        WebTestClient.ResponseSpec result = webTestClient.put()
            .uri("/api/v1/students/{id}", student.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(studentTransientDTO), StudentTransientDTO.class)
            .exchange();

        // then
        result.expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").exists()
            .jsonPath("$.name").isEqualTo(studentTransientDTO.name())
            .jsonPath("$.email").isEqualTo(studentTransientDTO.email())
            .jsonPath("$.courses").isEmpty();
    }

    @Test
    @DisplayName("Update student by incorrect id functionality")
    public void givenIncorrectId_whenUpdateStudent_thenErrorResponse() {
        // given
        long incorrectId = 200L;

        // when
        WebTestClient.ResponseSpec result = webTestClient.put()
            .uri("/api/v1/students/{id}", incorrectId)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(studentTransientDTO), StudentTransientDTO.class)
            .exchange();

        // then
        result.expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.message").isEqualTo("Student with ID " + incorrectId + " not found");
    }

    @Test
    @DisplayName("Delete student by id functionality")
    public void givenStudentId_whenDeleteStudent_thenDeletedResponse() {
        // given
        studentRepository.save(student).block();

        // when
        WebTestClient.ResponseSpec result = webTestClient.delete()
            .uri("/api/v1/students/{id}", student.getId())
            .exchange();

        // then
        result.expectStatus().isOk()
            .expectBody()
            .jsonPath("$.message").isEqualTo("Student deleted successfully");
    }

    @Test
    @DisplayName("Delete student by incorrect id functionality")
    public void givenIncorrectId_whenDeleteStudent_thenErrorResponse() {
        // given
        long incorrectId = 200L;

        // when
        WebTestClient.ResponseSpec result = webTestClient.delete()
            .uri("/api/v1/students/{id}", incorrectId)
            .exchange();

        // then
        result.expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.message").isEqualTo("Student with ID " + incorrectId + " not found");
    }

    @Test
    @DisplayName("Test get student courses functionality")
    public void givenStudentId_whenGetStudentCourses_thenSuccessResponse() {
        // given
        studentRepository.save(student).block();
        courseRepository.save(course).block();
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setCourseId(course.getId());
        studentCourse.setStudentId(student.getId());
        studentCourseRepository.save(studentCourse).block();

        // when
        WebTestClient.ResponseSpec result = webTestClient.get()
            .uri("/api/v1/students/{id}/courses", student.getId())
            .exchange();

        // then
        result.expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[0].id").isEqualTo(course.getId())
            .jsonPath("$[0].title").isEqualTo(course.getTitle())
            .jsonPath("$[0].teacher").isEmpty();
    }

    @Test
    @DisplayName("Set relation student-course functionality")
    public void givenStudentIdAndCourseId_whenSetRelationWithCourse_thenSuccessResponse() {
        // given
        studentRepository.save(student).block();
        courseRepository.save(course).block();

        // when
        WebTestClient.ResponseSpec result = webTestClient.post()
            .uri("/api/v1/students/{studentId}/courses/{courseId}", student.getId(), course.getId())
            .exchange();

        // then
        result.expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").exists()
            .jsonPath("$.name").isEqualTo("test")
            .jsonPath("$.email").isEqualTo("test@test.ru")
            .jsonPath("$.courses").isArray()
            .jsonPath("$.courses[0].id").isEqualTo(course.getId())
            .jsonPath("$.courses[0].title").isEqualTo(course.getTitle())
            .jsonPath("$.courses[0].teacher").isEmpty();
    }

    @Test
    @DisplayName("Set relation student-course with incorrect student id and correct course id functionality")
    public void givenIncorrectStudentIdAndCorrectCourseId_whenSetRelationWitStudentCourse_thenErrorResponse() {
        // given
        courseRepository.save(course).block();
        long incorrectStudentId = 200L;

        // when
        WebTestClient.ResponseSpec result = webTestClient.post()
            .uri("/api/v1/students/{studentId}/courses/{courseId}", incorrectStudentId, course.getId())
            .exchange();

        // then
        result.expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.message").isEqualTo("Student with ID " + incorrectStudentId + " not found");
    }

    @Test
    @DisplayName("Set relation student-course with correct student id and incorrect course id functionality")
    public void givenCorrectStudentIdAndIncorrectCourseId_whenSetRelationWitStudentCourse_thenErrorResponse() {
        // given
        studentRepository.save(student).block();
        long incorrectCourseId = 200L;

        // when
        WebTestClient.ResponseSpec result = webTestClient.post()
            .uri("/api/v1/students/{studentId}/courses/{courseId}", student.getId(), incorrectCourseId)
            .exchange();

        // then
        result.expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.message").isEqualTo("Course with ID " + incorrectCourseId + " not found");
    }
}
