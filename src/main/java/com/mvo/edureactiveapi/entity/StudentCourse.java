package com.mvo.edureactiveapi.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "student_course")
@Data
public class StudentCourse {
    @Id
    private Long id;

    @Column("course_id")
    private Long courseId;

    @Column("student_id")
    private Long studentId;
}
