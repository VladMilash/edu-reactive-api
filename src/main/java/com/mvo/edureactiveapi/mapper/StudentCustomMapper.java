package com.mvo.edureactiveapi.mapper;

import com.mvo.edureactiveapi.dto.responsedto.ResponseStudentDTO;
import io.r2dbc.spi.Row;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.function.BiFunction;

@Component
public class StudentCustomMapper implements BiFunction<Row, Object, ResponseStudentDTO> {
    @Override
    public ResponseStudentDTO apply(Row row, Object o) {
        Long studentId = row.get("student_id", Long.class);
        String studentName = row.get("student_name", String.class);
        String studentEmail = row.get("student_email", String.class);
        return new ResponseStudentDTO(
            studentId,
            studentName,
            studentEmail,
            new HashSet<>()
        );
    }
}
