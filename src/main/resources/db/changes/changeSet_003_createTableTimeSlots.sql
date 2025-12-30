CREATE TABLE time_slots
(
    schedule_date DATE NOT NULL REFERENCES schedule_days (date),
    hour          TIME NOT NULL,
    booked_count  INTEGER DEFAULT 0,
    PRIMARY KEY (schedule_date, hour)
);