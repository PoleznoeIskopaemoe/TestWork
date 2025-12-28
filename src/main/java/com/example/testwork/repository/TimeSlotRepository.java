package com.example.testwork.repository;

import com.example.testwork.entity.TimeSlot;
import com.example.testwork.entity.TimeSlotId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, TimeSlotId> {

    Optional<TimeSlot> findByScheduleDateAndHour(LocalDate date, LocalTime hour);

    List<TimeSlot> findByScheduleDate(LocalDate date);

    @Modifying
    @Query("UPDATE TimeSlot ts SET ts.bookedCount = ts.bookedCount + 1 " +
            "WHERE ts.scheduleDate = :date AND ts.hour = :hour " +
            "AND ts.bookedCount < :maxCapacity")
    int incrementBookedCount(
            @Param("date") LocalDate date,
            @Param("hour") LocalTime hour,
            @Param("maxCapacity") Integer maxCapacity);

    @Modifying
    @Query("UPDATE TimeSlot ts SET ts.bookedCount = ts.bookedCount - 1 " +
            "WHERE ts.scheduleDate = :date AND ts.hour = :hour " +
            "AND ts.bookedCount > 0")
    int decrementBookedCount(
            @Param("date") LocalDate date,
            @Param("hour") LocalTime hour);
}