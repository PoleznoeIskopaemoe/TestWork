package com.example.testwork.entity;

import java.time.LocalDate;
import java.time.LocalTime;

// Класс для составного ключа
public class TimeSlotId implements java.io.Serializable {
    private LocalDate scheduleDate;
    private LocalTime hour;

    public TimeSlotId() {}

    public TimeSlotId(LocalDate scheduleDate, LocalTime hour) {
        this.scheduleDate = scheduleDate;
        this.hour = hour;
    }
}
