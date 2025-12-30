CREATE TABLE appointments
(
    id             SERIAL PRIMARY KEY,
    client_id      INTEGER REFERENCES clients (id) ON DELETE CASCADE,
    schedule_date  DATE REFERENCES schedules (date),
    start_time     TIME NOT NULL,
    duration_hours INTEGER     DEFAULT 1,
    status         VARCHAR(20) DEFAULT 'active',
    created_at     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (client_id, schedule_date),
    CONSTRAINT valid_start_time
        CHECK (EXTRACT(MINUTE FROM start_time) = 0 AND EXTRACT(SECOND FROM start_time) = 0)
);