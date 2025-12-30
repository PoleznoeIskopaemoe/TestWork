package com.example.testwork.repository;

import com.example.testwork.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    Optional<Appointment> findByIdAndClientId(UUID id, Long clientId);

    List<Appointment> findByScheduleDate(LocalDate date);

    Optional<Appointment> findByClientIdAndScheduleDateAndStatus(
            Long clientId, LocalDate scheduleDate, String status);

    @Query("SELECT COUNT(a) FROM Appointment a " +
            "WHERE a.scheduleDate = :date " +
            "AND a.startTime = :hour " +
            "AND a.status = 'active'")
    Integer countActiveAppointmentsByDateAndHour(
            @Param("date") LocalDate date,
            @Param("hour") LocalTime hour);

    @Query("SELECT a FROM Appointment a " +
            "WHERE a.client.id = :clientId " +
            "AND a.scheduleDate = :date " +
            "AND a.status = 'active'")
    Optional<Appointment> findActiveByClientAndDate(
            @Param("clientId") Long clientId,
            @Param("date") LocalDate date);
}