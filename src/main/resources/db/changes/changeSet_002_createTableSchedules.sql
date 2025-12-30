CREATE TABLE schedule_days
(
    id           SERIAL PRIMARY KEY,
    date         DATE NOT NULL UNIQUE,
    is_holiday   BOOLEAN       DEFAULT FALSE,
    opening_time TIME NOT NULL DEFAULT '08:00',
    closing_time TIME NOT NULL DEFAULT '22:00',
    max_capacity INTEGER       DEFAULT 10,
    created_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);
