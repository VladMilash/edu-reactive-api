INSERT INTO teacher (name)
VALUES ('Профессор Смирнов'),
       ('Доцент Петров'),
       ('Старший преподаватель Иванов'),
       ('Преподаватель Кузнецов');

INSERT INTO department (name, head_of_department_id)
VALUES ('Кафедра математики', 1),
       ('Кафедра информационных технологий', 2),
       ('Кафедра гуманитарных наук', 3);

INSERT INTO student (name, email)
VALUES ('Иван Петров', 'ivan.petrov@example.com'),
       ('Анна Сидорова', 'anna.sidorova@example.com'),
       ('Михаил Кузнецов', 'mikhail.kuznetsov@example.com'),
       ('Елена Волкова', 'elena.volkova@example.com'),
       ('Дмитрий Морозов', 'dmitry.morozov@example.com');

INSERT INTO course (title, teacher_id)
VALUES ('Математический анализ', 1),
       ('Программирование на Java', 2),
       ('История философии', 3),
       ('Базы данных', 4),
       ('Линейная алгебра', 4);

INSERT INTO student_course (course_id, student_id)
VALUES (1, 1),
       (2, 3),
       (2, 4),
       (3, 5),
       (4, 1),
       (4, 3),
       (5, 2),
       (5, 4);