package com.example.testwork.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class TimeSlotId implements Serializable {
    private LocalDate scheduleDate;
    private LocalTime hour;

    public TimeSlotId() {}

    public TimeSlotId(LocalDate scheduleDate, LocalTime hour) {
        this.scheduleDate = scheduleDate;
        this.hour = hour;
    }
}
