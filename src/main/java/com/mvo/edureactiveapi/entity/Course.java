package com.mvo.edureactiveapi.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "course")
@Data
public class Course {
    @Id
    private Long id;

    @Column("title")
    private String title;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Column("teacher_id")
    private Long teacher_id;

}
