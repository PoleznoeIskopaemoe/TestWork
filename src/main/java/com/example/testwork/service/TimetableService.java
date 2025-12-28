package com.example.testwork.service;

import com.example.testwork.DTO.TimeSlotResponseDTO;
import com.example.testwork.entity.TimeSlot;
import com.example.testwork.entity.WorkingHours;
import com.example.testwork.repository.ClientRepository;
import com.example.testwork.repository.TimeSlotRepository;
import com.example.testwork.repository.WorkingHoursRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimetableService {

    private final TimeSlotRepository timeSlotRepository;
    private final WorkingHoursRepository workingHoursRepository;
    private final ClientRepository clientRepository;

    public List<TimeSlotResponseDTO> getAllBookedSlots(LocalDate date) {
        List<TimeSlot> timeSlots = timeSlotRepository.findByScheduleDate(date);

        // Если есть расписание на этот день
        WorkingHours schedule = workingHoursRepository.findByDate(date).orElse(null);
        if (schedule == null) {
            return new ArrayList<>();
        }

        // Генерируем все рабочие часы
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
        WorkingHours schedule = workingHoursRepository.findByDate(date)
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

//    @Transactional
//    public UUID reserveAppointment(AppointmentDTO appointmentDTO) {
//        LocalDateTime datetime = appointmentDTO.getDatetime();
//        LocalDate date = datetime.toLocalDate();
//        LocalTime hour = datetime.toLocalTime().withMinute(0).withSecond(0).withNano(0);
//
//        // Проверка времени (должно быть на начало часа)
//        if (!datetime.toLocalTime().equals(hour)) {
//            throw new IllegalArgumentException("Запись возможна только на начало часа");
//        }
//
//        // Проверяем клиента
//        Client client = clientRepository.findById(appointmentDTO.getClientId())
//                .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));
//
//        // Проверяем расписание
//        ScheduleDay schedule = scheduleDayRepository.findByDate(date)
//                .orElseThrow(() -> new IllegalArgumentException("На эту дату нет расписания"));
//
//        if (schedule.getIsHoliday()) {
//            throw new IllegalArgumentException("Это праздничный день");
//        }
//
//        // Проверяем рабочее время
//        if (!isWithinWorkingHours(hour, schedule)) {
//            throw new IllegalArgumentException("Вне рабочего времени");
//        }
//
//        // Проверяем ограничение 1 запись в день
//        if (appointmentRepository.findActiveByClientAndDate(client.getId(), date).isPresent()) {
//            throw new IllegalArgumentException("У клиента уже есть запись на этот день");
//        }
//
//        // Пытаемся зарезервировать слот
//        int updated = timeSlotRepository.incrementBookedCount(
//                date, hour, schedule.getMaxCapacity());
//
//        if (updated == 0) {
//            throw new IllegalArgumentException("Нет свободных мест на это время");
//        }
//
//        // Создаем запись
//        Appointment appointment = Appointment.builder()
//                .client(client)
//                .scheduleDate(date)
//                .startTime(hour)
//                .status("active")
//                .build();
//
//        Appointment savedAppointment = appointmentRepository.save(appointment);
//        return savedAppointment.getId();
//    }

//    @Transactional
//    public boolean cancelAppointment(Long clientId, UUID orderId) {
//        Appointment appointment = appointmentRepository
//                .findByIdAndClientId(orderId, clientId)
//                .orElseThrow(() -> new IllegalArgumentException("Запись не найдена"));
//
//        if (!"active".equals(appointment.getStatus())) {
//            throw new IllegalArgumentException("Запись уже отменена");
//        }
//
//        // Отменяем запись
//        appointment.setStatus("cancelled");
//        appointmentRepository.save(appointment);
//
//        // Освобождаем слот
//        timeSlotRepository.decrementBookedCount(
//                appointment.getScheduleDate(),
//                appointment.getStartTime());
//
//        return true;
//    }

    private List<LocalTime> generateWorkingHours(WorkingHours schedule) {
        List<LocalTime> hours = new ArrayList<>();
        LocalTime current = schedule.getOpeningTime();

        while (current.isBefore(schedule.getClosingTime())) {
            hours.add(current);
            current = current.plusHours(1);
        }

        return hours;
    }

    private boolean isWithinWorkingHours(LocalTime time, WorkingHours schedule) {
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