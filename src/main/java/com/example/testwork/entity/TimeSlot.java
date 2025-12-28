package com.example.testwork.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "time_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(TimeSlotId.class)
public class TimeSlot {

    @Id
    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    @Id
    @Column(name = "hour", nullable = false)
    private LocalTime hour;

    @Column(name = "booked_count")
    @Builder.Default
    private Integer bookedCount = 0;
}

