package com.example.testwork.service;

import com.example.testwork.DTO.AppointmentDTO;
import com.example.testwork.DTO.TimeSlotResponseDTO;
import com.example.testwork.entity.Appointment;
import com.example.testwork.entity.Client;
import com.example.testwork.entity.TimeSlot;
import com.example.testwork.entity.ScheduleDay;
import com.example.testwork.repository.AppointmentRepository;
import com.example.testwork.repository.ClientRepository;
import com.example.testwork.repository.ScheduleDayRepository;
import com.example.testwork.repository.TimeSlotRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimetableService {

    private final TimeSlotRepository timeSlotRepository;
    private final ScheduleDayRepository scheduleDayRepository;
    private final ClientRepository clientRepository;
    private final AppointmentRepository appointmentRepository;

    public List<TimeSlotResponseDTO> getAllBookedSlots(LocalDate date) {
        Optional<ScheduleDay> scheduleOpt = scheduleDayRepository.findByDate(date);

        if (scheduleOpt.isEmpty()) {
            return new ArrayList<>();
        }

        ScheduleDay schedule = scheduleOpt.get();
        List<TimeSlot> timeSlots = timeSlotRepository.findByScheduleDate(date);

        return generateWorkingHours(schedule).stream()
                .map(hour -> {
                    TimeSlot slot = findTimeSlot(timeSlots, hour);
                    return new TimeSlotResponseDTO(
                            hour.toString(),
                            slot != null ? slot.getBookedCount() : 0
                    );
                })
                .collect(Collectors.toList());
    }

    public List<TimeSlotResponseDTO> getAvailableSlots(LocalDate date) {
        ScheduleDay schedule = scheduleDayRepository.findByDate(date)
                .orElseThrow(() -> new IllegalArgumentException("На эту дату нет расписания"));

        if (schedule.getIsHoliday()) {
            throw new IllegalArgumentException("Это праздничный день");
        }

        List<TimeSlot> timeSlots = timeSlotRepository.findByScheduleDate(date);
        List<TimeSlotResponseDTO> availableSlots = new ArrayList<>();

        for (LocalTime hour : generateWorkingHours(schedule)) {
            TimeSlot slot = findTimeSlot(timeSlots, hour);
            int bookedCount = slot != null ? slot.getBookedCount() : 0;
            int available = schedule.getMaxCapacity() - bookedCount;

            if (available > 0) {
                availableSlots.add(new TimeSlotResponseDTO(hour.toString(), available));
            }
        }

        return availableSlots;
    }

    @Transactional
    public UUID reserveAppointment(AppointmentDTO appointmentDTO) {
        LocalDateTime datetime = appointmentDTO.getDatetime();
        LocalDate date = datetime.toLocalDate();
        LocalTime hour = datetime.toLocalTime().withMinute(0).withSecond(0).withNano(0);

        if (!datetime.toLocalTime().equals(hour)) {
            throw new IllegalArgumentException("Запись возможна только на начало часа");
        }

        Client client = clientRepository.findById(appointmentDTO.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

        ScheduleDay schedule = scheduleDayRepository.findByDate(date)
                .orElseThrow(() -> new IllegalArgumentException("На эту дату нет расписания"));

        if (schedule.getIsHoliday()) {
            throw new IllegalArgumentException("Это праздничный день");
        }

        if (!isWithinWorkingHours(hour, schedule)) {
            throw new IllegalArgumentException("Вне рабочего времени");
        }

        if (appointmentRepository.findActiveByClientAndDate(client.getId(), date).isPresent()) {
            throw new IllegalArgumentException("У клиента уже есть запись на этот день");
        }

        int count = schedule.getMaxCapacity();
        if(count > 10) {
            throw new IllegalArgumentException("Нет свободных мест на это время");
        }

        Appointment appointment = Appointment.builder()
                .client(client)
                .scheduleDate(date)
                .startTime(hour)
                .status("active")
                .build();

        timeSlotRepository.incrementBookedCount(date, hour, count);
        appointmentRepository.save(appointment);

        return appointment.getId();
    }

    @Transactional
    public boolean cancelAppointment(Long clientId, UUID orderId) {
        Appointment appointment = appointmentRepository
                .findByIdAndClientId(orderId, clientId)
                .orElseThrow(() -> new IllegalArgumentException("Запись не найдена"));

        if (!"active".equals(appointment.getStatus())) {
            throw new IllegalArgumentException("Запись уже отменена");
        }

        appointment.setStatus("cancelled");
        appointmentRepository.save(appointment);

        timeSlotRepository.decrementBookedCount(
                appointment.getScheduleDate(),
                appointment.getStartTime());

        return true;
    }

    private List<LocalTime> generateWorkingHours(ScheduleDay schedule) {
        List<LocalTime> hours = new ArrayList<>();
        LocalTime current = schedule.getOpeningTime();

        while (current.isBefore(schedule.getClosingTime())) {
            hours.add(current);
            current = current.plusHours(1);
        }

        return hours;
    }

    private boolean isWithinWorkingHours(LocalTime time, ScheduleDay schedule) {
        return !time.isBefore(schedule.getOpeningTime()) &&
                time.isBefore(schedule.getClosingTime());
    }

    private TimeSlot findTimeSlot(List<TimeSlot> slots, LocalTime hour) {
        return slots.stream()
                .filter(slot -> slot.getHour().equals(hour))
                .findFirst()
                .orElse(null);
    }
}