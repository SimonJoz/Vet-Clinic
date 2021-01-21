package com.simonjoz.vetclinic.service;

import com.simonjoz.vetclinic.domain.Appointment;
import com.simonjoz.vetclinic.domain.AppointmentRequest;
import com.simonjoz.vetclinic.domain.Customer;
import com.simonjoz.vetclinic.domain.Doctor;
import com.simonjoz.vetclinic.domain.dto.AppointmentDTO;
import com.simonjoz.vetclinic.domain.dto.PageDTO;
import com.simonjoz.vetclinic.exceptions.RemovalFailureException;
import com.simonjoz.vetclinic.exceptions.UnavailableDateException;
import com.simonjoz.vetclinic.mappers.CustomerAppointmentMapper;
import com.simonjoz.vetclinic.mappers.PagesMapper;
import com.simonjoz.vetclinic.repository.AppointmentsRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@SpringBootTest
class AppointmentsServiceTest {

    private static final AppointmentRequest APPOINTMENT_REQUEST = new AppointmentRequest(1234, 1L,
            "some note here", LocalDate.now(), LocalTime.now());

    @Autowired
    private AppointmentsService appointmentsService;

    @SpyBean
    private CustomerAppointmentMapper customerAppointmentMapper;

    @MockBean
    private AppointmentsRepo appointmentsRepo;

    @AfterEach
    void reset() {
        Mockito.reset(appointmentsRepo, customerAppointmentMapper);
    }

    @Test
    void testAddAppointmentSuccess() {
        List<Appointment> emptyList = Collections.emptyList();
        Customer customer = new Customer(1L, 1234, "CUSTOMER1", "SURNAME1", emptyList);
        Doctor doctor = new Doctor(1L, "DR", "DOCTOR1", "SURNAME1", emptyList);

        Appointment appointment = new Appointment(1L, "note",
                LocalDate.now(), LocalTime.now(), LocalDateTime.now(), customer, doctor);


        AppointmentDTO expectedDTO = customerAppointmentMapper.map(appointment);
        Mockito.doReturn(appointment).when(appointmentsRepo).save(appointment);
        AppointmentDTO actualDTO = appointmentsService.addAppointment(appointment);

        assertEquals(expectedDTO, actualDTO);
        assertEquals(expectedDTO.getId(), actualDTO.getId());
        assertEquals(expectedDTO.getScheduledTime(), actualDTO.getScheduledTime());
        assertEquals(expectedDTO.getScheduledDate(), actualDTO.getScheduledDate());
        assertEquals(expectedDTO.getPersonName(), actualDTO.getPersonName());
        assertEquals(expectedDTO.getPersonSurname(), actualDTO.getPersonSurname());

        Mockito.verify(customerAppointmentMapper, Mockito.times(2)).map(appointment);
        Mockito.verify(appointmentsRepo).save(appointment);
    }


    @Test
    void checkDateAvailabilityForDoctorDateIsUnavailable() {

        Mockito.doReturn(false).when(appointmentsRepo).isDateAndTimeAvailableForDoctorWithId(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), any(LocalDateTime.class));

        assertThrows(UnavailableDateException.class,
                () -> appointmentsService.checkDateAvailabilityForDoctor(APPOINTMENT_REQUEST));

        Mockito.verify(appointmentsRepo).isDateAndTimeAvailableForDoctorWithId(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), any(LocalDateTime.class));

    }

    @Test
    void testDeleteAppointmentRemovalFailed() {
        Mockito.doReturn(true).when(appointmentsRepo)
                .existsByCustomerIdAndTimestamp(anyLong(), any(LocalDateTime.class));

        assertThrows(RemovalFailureException.class,
                () -> appointmentsService.deleteAppointment(1L, LocalDateTime.now()));

        Mockito.verify(appointmentsRepo).deleteByCustomerIdAndTimestamp(anyLong(), any(LocalDateTime.class));
        Mockito.verify(appointmentsRepo).existsByCustomerIdAndTimestamp(anyLong(), any(LocalDateTime.class));
    }

    @Test
    void testGetAppointmentsPageByDoctorId() {
        PageRequest pageRequest = PageRequest.of(0, 1);
        AppointmentDTO appointmentDTO = new AppointmentDTO(1L, "note", LocalDate.now(),
                LocalTime.now(), "DOCTOR1", "SURNAME1");
        List<AppointmentDTO> expectedContent = List.of(appointmentDTO);

        Page<AppointmentDTO> page = new PageImpl<>(expectedContent, pageRequest, 1);
        Mockito.doReturn(page).when(appointmentsRepo).getDoctorAppointmentsPage(1L, pageRequest);
        PagesMapper<AppointmentDTO> pagesMapper = new PagesMapper<>();
        PageDTO<AppointmentDTO> expectedPage = pagesMapper.map(page);
        PageDTO<AppointmentDTO> actualPage = appointmentsService.getAppointmentsPageByDoctorId(pageRequest, 1L);

        assertFalse(actualPage.isEmpty());
        assertTrue(actualPage.isFirst());
        assertTrue(actualPage.isLast());
        assertEquals(expectedPage, actualPage);
        assertEquals(expectedContent, actualPage.getContent());
        assertEquals(1, actualPage.getTotalElements());
        assertEquals(1, actualPage.getTotalPages());

        Mockito.verify(appointmentsRepo).getDoctorAppointmentsPage(1L, pageRequest);
    }


    @Test
    void testGetAppointmentsPageByDoctorIdForDate() {
        PageRequest pageRequest = PageRequest.of(0, 1);
        AppointmentDTO appointmentDTO = new AppointmentDTO(1L, "note", LocalDate.now(),
                LocalTime.now(), "DOCTOR1", "SURNAME1");
        List<AppointmentDTO> expectedContent = List.of(appointmentDTO);

        Page<AppointmentDTO> page = new PageImpl<>(expectedContent, pageRequest, 1);
        Mockito.doReturn(page).when(appointmentsRepo).getDoctorAppointmentsPage(1L, LocalDate.now(), pageRequest);

        PagesMapper<AppointmentDTO> pagesMapper = new PagesMapper<>();
        PageDTO<AppointmentDTO> expectedPage = pagesMapper.map(page);
        PageDTO<AppointmentDTO> actualPage = appointmentsService
                .getAppointmentsPageByDoctorIdForDate(pageRequest, 1L, LocalDate.now());

        assertFalse(actualPage.isEmpty());
        assertTrue(actualPage.isFirst());
        assertTrue(actualPage.isLast());
        assertEquals(expectedPage, actualPage);
        assertEquals(expectedContent, actualPage.getContent());
        assertEquals(1, actualPage.getTotalElements());
        assertEquals(1, actualPage.getTotalPages());

        Mockito.verify(appointmentsRepo).getDoctorAppointmentsPage(1L, LocalDate.now(), pageRequest);
    }

}
