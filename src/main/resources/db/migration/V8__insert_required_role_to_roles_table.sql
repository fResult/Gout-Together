INSERT INTO roles (id, name)
VALUES (1, 'ADMIN'), (2, 'CONSUMER'), (3, 'COMPANY');
SELECT setval('roles_id_seq', 4, false);
