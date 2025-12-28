package com.example.testwork.repository;

import com.example.testwork.entity.WorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Long> {
    Optional<WorkingHours> findByDate(LocalDate date);
    boolean existsByDate(LocalDate date);
}
