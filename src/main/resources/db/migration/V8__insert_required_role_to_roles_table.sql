DO $$
BEGIN
    INSERT INTO roles (id, name)
    VALUES (1, 'ADMIN'), (2, 'CONSUMER'), (3, 'COMPANY');

    IF (SELECT last_value FROM roles_id_seq) < 4 THEN
        PERFORM setval('roles_id_seq', 4, false);
    END IF;
END $$;
