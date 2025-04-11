package com.mvo.edureactiveapi.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "teacher")
@Data
public class Teacher {
    @Id
    @Column("id")
    private Long id;

    @Column("name")
    private String name;
}
