INSERT INTO doctors (id, name, surname, title)
VALUES (1, 'DOCTOR1', 'SURNAME1', 'DR'),
       (2, 'DOCTOR2', 'SURNAME2', 'DR'),
       (3, 'DOCTOR3', 'SURNAME3', 'DR'),
       (4, 'DOCTOR4', 'SURNAME4', 'DR'),
       (5, 'DOCTOR5', 'SURNAME5', 'DR');


INSERT INTO customers (id, pin, name, surname)
VALUES (1, 1234, 'CUSTOMER1', 'SURNAME1'),
       (2, 1234, 'CUSTOMER2', 'SURNAME2'),
       (3, 1234, 'CUSTOMER3', 'SURNAME3'),
       (4, 1234, 'CUSTOMER4', 'SURNAME4'),
       (5, 1234, 'CUSTOMER5', 'SURNAME5');


INSERT INTO appointments (id, note, scheduled_date, customer_id, doctor_id)
VALUES (1, 'APPOINTMENT1', '2021-01-21', 1, 1),
       (2, 'APPOINTMENT2', '2021-01-22', 1, 2),
       (3, 'APPOINTMENT3', '2021-01-23', 2, 1),
       (4, 'APPOINTMENT4', '2021-01-24', 2, 2);