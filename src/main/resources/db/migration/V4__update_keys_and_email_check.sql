ALTER TABLE teacher
ALTER
COLUMN id TYPE BIGINT;

ALTER TABLE department
ALTER
COLUMN id TYPE BIGINT,
    ALTER
COLUMN head_of_department_id TYPE BIGINT;

ALTER TABLE student
ALTER
COLUMN id TYPE BIGINT;

ALTER TABLE course
ALTER
COLUMN id TYPE BIGINT,
    ALTER
COLUMN teacher_id TYPE BIGINT;

ALTER TABLE student_course
ALTER
COLUMN id TYPE BIGINT,
    ALTER
COLUMN course_id TYPE BIGINT,
    ALTER
COLUMN student_id TYPE BIGINT;

ALTER TABLE student
    ADD CONSTRAINT chk_student_email_format
        CHECK (email ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$');