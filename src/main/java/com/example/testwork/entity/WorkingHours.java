package com.example.testwork.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "schedule_days")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkingHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date", nullable = false, unique = true)
    private LocalDate date;

    @Column(name = "is_holiday")
    @Builder.Default
    private Boolean isHoliday = false;

    @Column(name = "opening_time", nullable = false)
    @Builder.Default
    private LocalTime openingTime = LocalTime.of(8, 0);

    @Column(name = "closing_time", nullable = false)
    @Builder.Default
    private LocalTime closingTime = LocalTime.of(22, 0);

    @Column(name = "max_capacity")
    @Builder.Default
    private Integer maxCapacity = 10;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
