CREATE INDEX idx_student_course_student_id ON student_course(student_id);
CREATE INDEX idx_student_course_course_id ON student_course(course_id);
CREATE INDEX idx_course_teacher_id ON course(teacher_id);
CREATE INDEX idx_department_head_of_department_id ON department(head_of_department_id);