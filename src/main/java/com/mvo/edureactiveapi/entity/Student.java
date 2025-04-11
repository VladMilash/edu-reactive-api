package com.mvo.edureactiveapi.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "student")
@Data
public class Student {
    @Id
    private Long id;

    @Column("name")
    private String name;

    @Column("email")
    private String email;

}
