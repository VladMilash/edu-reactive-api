package com.mvo.edureactiveapi.it;

import com.mvo.edureactiveapi.dto.requestdto.CourseTransientDTO;
import com.mvo.edureactiveapi.entity.Course;
import com.mvo.edureactiveapi.config.PostgreTestcontainerConfig;
import com.mvo.edureactiveapi.repository.CourseRepository;
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
public class ItCoursesRestControllerV1Tests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CourseRepository courseRepository;

    private CourseTransientDTO courseTransientDTO;

    @BeforeEach
    void setUp() {
        courseTransientDTO = new CourseTransientDTO("test");
        courseRepository.deleteAll().block();
    }

    @Test
    @DisplayName("Test save course functionality")
    public void givenCourseTransientDTO_whenSaveCourse_thenSuccessResponse() {
        // given

        // when
        WebTestClient.ResponseSpec result = webTestClient.post()
            .uri("/api/v1/courses/")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(courseTransientDTO), CourseTransientDTO.class)
            .exchange();

        // then
        result.expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.id").exists()
            .jsonPath("$.title").isEqualTo("test")
            .jsonPath("$.teacher").isEmpty()
            .jsonPath("$.students").isEmpty();
    }

    @Test
    @DisplayName("Test get course by id functionality")
    public void givenCourseId_whenGetCourse_thenSuccessResponse() {
        // given
        Course course = new Course();
        course.setTitle("Test");
        Course savedCourse = courseRepository.save(course).block();

        // when
        WebTestClient.ResponseSpec result = webTestClient.get()
            .uri("/api/v1/courses/{id}", savedCourse.getId())
            .exchange();

        // then
        result.expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").exists()
            .jsonPath("$.title").isEqualTo("Test")
            .jsonPath("$.teacher").isEmpty()
            .jsonPath("$.students").isEmpty();
    }

    @Test
    @DisplayName("Test get course by incorrect id functionality")
    public void givenIncorrectId_whenGetById_thenErrorResponse() {
        // given
        long incorrectId = 200L;

        // when
        WebTestClient.ResponseSpec result = webTestClient.get()
            .uri("/api/v1/courses/{id}", incorrectId)
            .exchange();

        // then
        result.expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.message").isEqualTo("Course with ID " + incorrectId + " not found");
    }

    @Test
    @DisplayName("Test get all courses functionality")
    public void givenGetCoursesRequest_whenGetCourses_thenNonEmptyList() {
        // given
        Course course = new Course();
        course.setTitle("Test");
        courseRepository.save(course).block();

        // when
        WebTestClient.ResponseSpec result = webTestClient.get()
            .uri("/api/v1/courses/")
            .exchange();

        // then
        result.expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$").isNotEmpty();
    }

    @Test
    @DisplayName("Update course by id functionality")
    public void givenCourseId_whenUpdateCourse_thenSuccessResponse() {
        // given
        Course course = new Course();
        course.setTitle("New");
        Course savedCourse = courseRepository.save(course).block();

        // when
        WebTestClient.ResponseSpec result = webTestClient.put()
            .uri("/api/v1/courses/{id}", savedCourse.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(courseTransientDTO), CourseTransientDTO.class)
            .exchange();

        // then
        result.expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").exists()
            .jsonPath("$.title").isEqualTo("test")
            .jsonPath("$.teacher").isEmpty()
            .jsonPath("$.students").isEmpty();
    }

    @Test
    @DisplayName("Update course by incorrect id functionality")
    public void givenIncorrectId_whenUpdateCourse_thenErrorResponse() {
        // given
        long incorrectId = 200L;

        // when
        WebTestClient.ResponseSpec result = webTestClient.put()
            .uri("/api/v1/courses/{id}", incorrectId)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(courseTransientDTO), CourseTransientDTO.class)
            .exchange();

        // then
        result.expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.message").isEqualTo("Course with ID " + incorrectId + " not found");
    }

    @Test
    @DisplayName("Delete course by id functionality")
    public void givenCourseId_whenDeleteCourse_thenDeletedResponse() {
        // given
        Course course = new Course();
        course.setTitle("New");
        Course savedCourse = courseRepository.save(course).block();

        // when
        WebTestClient.ResponseSpec result = webTestClient.delete()
            .uri("/api/v1/courses/{id}", savedCourse.getId())
            .exchange();

        // then
        result.expectStatus().isOk()
            .expectBody()
            .jsonPath("$.message").isEqualTo("Course deleted successfully");
    }

    @Test
    @DisplayName("Delete course by incorrect id functionality")
    public void givenIncorrectId_whenDeleteCourse_thenErrorResponse() {
        // given
        long incorrectId = 200L;

        // when
        WebTestClient.ResponseSpec result = webTestClient.delete()
            .uri("/api/v1/courses/{id}", incorrectId)
            .exchange();

        // then
        result.expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.message").isEqualTo("Course with ID " + incorrectId + " not found");
    }
}
