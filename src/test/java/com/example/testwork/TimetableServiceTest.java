package com.example.testwork;

import com.example.testwork.DTO.AppointmentDTO;
import com.example.testwork.DTO.TimeSlotResponseDTO;
import com.example.testwork.entity.Appointment;
import com.example.testwork.entity.Client;
import com.example.testwork.entity.ScheduleDay;
import com.example.testwork.entity.TimeSlot;
import com.example.testwork.repository.AppointmentRepository;
import com.example.testwork.repository.ClientRepository;
import com.example.testwork.repository.ScheduleDayRepository;
import com.example.testwork.repository.TimeSlotRepository;
import com.example.testwork.service.TimetableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimetableServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private ScheduleDayRepository scheduleDayRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private TimetableService timetableService;

    private Client testClient;
    private ScheduleDay testScheduleDay;
    private Appointment testAppointment;
    private TimeSlot testTimeSlot;

    @BeforeEach
    void setUp() {
        testClient = Client.builder()
                .id(1L)
                .name("Иван Иванов")
                .phone("+79161234567")
                .build();

        testScheduleDay = ScheduleDay.builder()
                .id(1L)
                .date(LocalDate.of(2024, 1, 15))
                .isHoliday(false)
                .openingTime(LocalTime.of(8, 0))
                .closingTime(LocalTime.of(22, 0))
                .maxCapacity(10)
                .build();

        testAppointment = Appointment.builder()
                .id(UUID.randomUUID())
                .client(testClient)
                .scheduleDate(LocalDate.of(2024, 1, 15))
                .startTime(LocalTime.of(14, 0))
                .durationHours(1)
                .status("active")
                .build();

        testTimeSlot = TimeSlot.builder()
                .scheduleDate(LocalDate.of(2024, 1, 15))
                .hour(LocalTime.of(14, 0))
                .bookedCount(5)
                .build();
    }

    @Test
    void getAllBookedSlots_withScheduleDay_shouldReturnSlots() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        List<TimeSlot> timeSlots = Arrays.asList(testTimeSlot);

        when(scheduleDayRepository.findByDate(date)).thenReturn(Optional.of(testScheduleDay));
        when(timeSlotRepository.findByScheduleDate(date)).thenReturn(timeSlots);

        List<TimeSlotResponseDTO> result = timetableService.getAllBookedSlots(date);

        assertNotNull(result);
        verify(scheduleDayRepository, times(1)).findByDate(date);
        verify(timeSlotRepository, times(1)).findByScheduleDate(date);
    }

    @Test
    void getAllBookedSlots_withoutScheduleDay_shouldReturnEmptyList() {
        LocalDate date = LocalDate.of(2024, 1, 15);

        when(scheduleDayRepository.findByDate(date)).thenReturn(Optional.empty());

        List<TimeSlotResponseDTO> result = timetableService.getAllBookedSlots(date);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(scheduleDayRepository, times(1)).findByDate(date);
        verify(timeSlotRepository, never()).findByScheduleDate(any());
    }

    @Test
    void getAvailableSlots_withValidDate_shouldReturnAvailableSlots() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        List<TimeSlot> timeSlots = Arrays.asList(testTimeSlot);

        when(scheduleDayRepository.findByDate(date)).thenReturn(Optional.of(testScheduleDay));
        when(timeSlotRepository.findByScheduleDate(date)).thenReturn(timeSlots);

        List<TimeSlotResponseDTO> result = timetableService.getAvailableSlots(date);

        assertNotNull(result);
        verify(scheduleDayRepository, times(1)).findByDate(date);
        verify(timeSlotRepository, times(1)).findByScheduleDate(date);
    }

    @Test
    void getAvailableSlots_onHoliday_shouldThrowException() {
        LocalDate date = LocalDate.of(2024, 1, 1);
        ScheduleDay holidaySchedule = ScheduleDay.builder()
                .date(date)
                .isHoliday(true)
                .build();

        when(scheduleDayRepository.findByDate(date)).thenReturn(Optional.of(holidaySchedule));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> timetableService.getAvailableSlots(date)
        );

        assertEquals("Это праздничный день", exception.getMessage());
        verify(scheduleDayRepository, times(1)).findByDate(date);
    }

    @Test
    void getAvailableSlots_withoutSchedule_shouldThrowException() {
        LocalDate date = LocalDate.of(2024, 1, 15);

        when(scheduleDayRepository.findByDate(date)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> timetableService.getAvailableSlots(date)
        );

        assertEquals("На эту дату нет расписания", exception.getMessage());
        verify(scheduleDayRepository, times(1)).findByDate(date);
    }

    @Test
    void reserveAppointment_withValidData_shouldSuccess() {
        LocalDateTime datetime = LocalDateTime.of(2024, 1, 15, 14, 0);
        LocalTime hour = datetime.toLocalTime().withMinute(0).withSecond(0).withNano(0);

        AppointmentDTO appointmentDTO = new AppointmentDTO();
        appointmentDTO.setClientId(1L);
        appointmentDTO.setDatetime(datetime);

        UUID expectedId = UUID.randomUUID();
        Appointment savedAppointment = Appointment.builder()
                .id(expectedId)
                .client(testClient)
                .scheduleDate(datetime.toLocalDate())
                .startTime(hour)
                .status("active")
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(scheduleDayRepository.findByDate(datetime.toLocalDate()))
                .thenReturn(Optional.of(testScheduleDay));
        when(appointmentRepository.findActiveByClientAndDate(1L, datetime.toLocalDate()))
                .thenReturn(Optional.empty());

        when(timeSlotRepository.incrementBookedCount(
                eq(datetime.toLocalDate()),
                eq(hour),
                eq(10)))
                .thenReturn(1);

        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> {
                    Appointment app = invocation.getArgument(0);
                    // Если у переданного объекта нет ID, устанавливаем его
                    if (app.getId() == null) {
                        app.setId(expectedId);
                    }
                    return app;
                });

        UUID result = timetableService.reserveAppointment(appointmentDTO);

        assertNotNull(result);
        assertEquals(expectedId, result);

        verify(clientRepository, times(1)).findById(1L);
        verify(scheduleDayRepository, times(1)).findByDate(datetime.toLocalDate());
        verify(appointmentRepository, times(1)).findActiveByClientAndDate(1L, datetime.toLocalDate());

        verify(timeSlotRepository, times(1)).incrementBookedCount(
                datetime.toLocalDate(),
                hour,
                10);

        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    void reserveAppointment_withInvalidTime_shouldThrowException() {
        LocalDateTime datetime = LocalDateTime.of(2024, 1, 15, 14, 30); // Не на начало часа
        AppointmentDTO appointmentDTO = new AppointmentDTO();
        appointmentDTO.setClientId(1L);
        appointmentDTO.setDatetime(datetime);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> timetableService.reserveAppointment(appointmentDTO)
        );

        assertEquals("Запись возможна только на начало часа", exception.getMessage());
        verify(clientRepository, never()).findById(any());
    }

    @Test
    void reserveAppointment_withNonExistingClient_shouldThrowException() {
        LocalDateTime datetime = LocalDateTime.of(2024, 1, 15, 14, 0);
        AppointmentDTO appointmentDTO = new AppointmentDTO();
        appointmentDTO.setClientId(99L);
        appointmentDTO.setDatetime(datetime);

        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> timetableService.reserveAppointment(appointmentDTO)
        );

        assertEquals("Клиент не найден", exception.getMessage());
        verify(clientRepository, times(1)).findById(99L);
    }

    @Test
    void reserveAppointment_onHoliday_shouldThrowException() {
        LocalDateTime datetime = LocalDateTime.of(2024, 1, 1, 14, 0);
        AppointmentDTO appointmentDTO = new AppointmentDTO();
        appointmentDTO.setClientId(1L);
        appointmentDTO.setDatetime(datetime);

        ScheduleDay holidaySchedule = ScheduleDay.builder()
                .date(datetime.toLocalDate())
                .isHoliday(true)
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(scheduleDayRepository.findByDate(datetime.toLocalDate()))
                .thenReturn(Optional.of(holidaySchedule));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> timetableService.reserveAppointment(appointmentDTO)
        );

        assertEquals("Это праздничный день", exception.getMessage());
        verify(clientRepository, times(1)).findById(1L);
        verify(scheduleDayRepository, times(1)).findByDate(datetime.toLocalDate());
    }

    @Test
    void reserveAppointment_outsideWorkingHours_shouldThrowException() {
        LocalDateTime datetime = LocalDateTime.of(2024, 1, 15, 7, 0); // До открытия
        AppointmentDTO appointmentDTO = new AppointmentDTO();
        appointmentDTO.setClientId(1L);
        appointmentDTO.setDatetime(datetime);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(scheduleDayRepository.findByDate(datetime.toLocalDate()))
                .thenReturn(Optional.of(testScheduleDay));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> timetableService.reserveAppointment(appointmentDTO)
        );

        assertEquals("Вне рабочего времени", exception.getMessage());
        verify(clientRepository, times(1)).findById(1L);
        verify(scheduleDayRepository, times(1)).findByDate(datetime.toLocalDate());
    }

    @Test
    void reserveAppointment_withExistingAppointment_shouldThrowException() {
        LocalDateTime datetime = LocalDateTime.of(2024, 1, 15, 14, 0);
        AppointmentDTO appointmentDTO = new AppointmentDTO();
        appointmentDTO.setClientId(1L);
        appointmentDTO.setDatetime(datetime);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(scheduleDayRepository.findByDate(datetime.toLocalDate()))
                .thenReturn(Optional.of(testScheduleDay));
        when(appointmentRepository.findActiveByClientAndDate(1L, datetime.toLocalDate()))
                .thenReturn(Optional.of(testAppointment));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> timetableService.reserveAppointment(appointmentDTO)
        );

        assertEquals("У клиента уже есть запись на этот день", exception.getMessage());
        verify(clientRepository, times(1)).findById(1L);
        verify(scheduleDayRepository, times(1)).findByDate(datetime.toLocalDate());
        verify(appointmentRepository, times(1)).findActiveByClientAndDate(1L, datetime.toLocalDate());
    }


    @Test
    void cancelAppointment_withValidData_shouldSuccess() {
        UUID appointmentId = UUID.randomUUID();
        Long clientId = 1L;

        when(appointmentRepository.findByIdAndClientId(appointmentId, clientId))
                .thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class)))
                .thenReturn(testAppointment);
        when(timeSlotRepository.decrementBookedCount(
                testAppointment.getScheduleDate(),
                testAppointment.getStartTime()))
                .thenReturn(1);

        boolean result = timetableService.cancelAppointment(clientId, appointmentId);

        assertTrue(result);
        verify(appointmentRepository, times(1)).findByIdAndClientId(appointmentId, clientId);
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
        verify(timeSlotRepository, times(1)).decrementBookedCount(any(), any());
    }

    @Test
    void cancelAppointment_withNonExistingAppointment_shouldThrowException() {
        UUID appointmentId = UUID.randomUUID();
        Long clientId = 1L;

        when(appointmentRepository.findByIdAndClientId(appointmentId, clientId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> timetableService.cancelAppointment(clientId, appointmentId)
        );

        assertEquals("Запись не найдена", exception.getMessage());
        verify(appointmentRepository, times(1)).findByIdAndClientId(appointmentId, clientId);
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void cancelAppointment_alreadyCancelled_shouldThrowException() {
        UUID appointmentId = UUID.randomUUID();
        Long clientId = 1L;

        Appointment cancelledAppointment = Appointment.builder()
                .id(appointmentId)
                .client(testClient)
                .scheduleDate(LocalDate.of(2024, 1, 15))
                .startTime(LocalTime.of(14, 0))
                .status("cancelled")
                .build();

        when(appointmentRepository.findByIdAndClientId(appointmentId, clientId))
                .thenReturn(Optional.of(cancelledAppointment));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> timetableService.cancelAppointment(clientId, appointmentId)
        );

        assertEquals("Запись уже отменена", exception.getMessage());
        verify(appointmentRepository, times(1)).findByIdAndClientId(appointmentId, clientId);
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void cancelAppointment_wrongClient_shouldThrowException() {
        UUID appointmentId = UUID.randomUUID();
        Long wrongClientId = 2L; // Не тот клиент

        when(appointmentRepository.findByIdAndClientId(appointmentId, wrongClientId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> timetableService.cancelAppointment(wrongClientId, appointmentId)
        );

        assertEquals("Запись не найдена", exception.getMessage());
        verify(appointmentRepository, times(1)).findByIdAndClientId(appointmentId, wrongClientId);
    }
}