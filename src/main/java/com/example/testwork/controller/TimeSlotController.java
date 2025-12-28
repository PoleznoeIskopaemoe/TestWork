package com.example.testwork.controller;

import com.example.testwork.DTO.TimeSlotResponseDTO;
import com.example.testwork.service.TimetableService;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/v0/pool/timetable")
@RequiredArgsConstructor
public class TimeSlotController {

    private final TimetableService timetableService;

    @GetMapping("/all")
    public ResponseEntity<List<TimeSlotResponseDTO>> getAllBookedSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<TimeSlotResponseDTO> slots = timetableService.getAllBookedSlots(date);
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/available")
    public ResponseEntity<List<TimeSlotResponseDTO>> getAvailableSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<TimeSlotResponseDTO> slots = timetableService.getAvailableSlots(date);
        return ResponseEntity.ok(slots);
    }

//    @PostMapping("/reserve")
//    public ResponseEntity<Map<String, String>> reserveAppointment(
//            @Valid @RequestBody AppointmentDTO appointmentDTO) {
//        UUID orderId = timetableService.reserveAppointment(appointmentDTO);
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(Map.of("orderId", orderId.toString()));
//    }
//
//    @GetMapping("/cancel")
//    public ResponseEntity<Map<String, Boolean>> cancelAppointment(
//            @Valid @RequestBody CancelRequestDTO cancelRequest) {
//        boolean cancelled = timetableService.cancelAppointment(
//                cancelRequest.getClientId(),
//                cancelRequest.getOrderId());
//        return ResponseEntity.ok(Map.of("success", cancelled));
//    }
}
