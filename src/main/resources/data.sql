INSERT INTO slot_definition (step_type, slot_name)
VALUES
    ('기','THEME'),
    ('기','PLACE'),
    ('기','ACTION'),
    ('승','NEW_ENTITY'),
    ('승','INTERACTION'),
    ('전','TURNING_POINT'),
    ('전','FEELING'),
    ('결','MORAL'),
    ('결','FINAL_ACTION')
    ON DUPLICATE KEY UPDATE
                         step_type = VALUES(step_type);