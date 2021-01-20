package com.simonjoz.vetclinic.service;

import com.simonjoz.vetclinic.domain.Appointment;
import com.simonjoz.vetclinic.domain.AppointmentRequest;
import com.simonjoz.vetclinic.domain.Customer;
import com.simonjoz.vetclinic.domain.Doctor;
import com.simonjoz.vetclinic.domain.dto.AppointmentDTO;
import com.simonjoz.vetclinic.exceptions.RemovalFailureException;
import com.simonjoz.vetclinic.exceptions.UnavailableDateException;
import com.simonjoz.vetclinic.mappers.CustomerAppointmentMapper;
import com.simonjoz.vetclinic.repository.AppointmentsRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@SpringBootTest
class AppointmentsServiceTest {

    private static final PageRequest PAGE_REQUEST = PageRequest.of(0, 2);

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
                anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class));

        assertThrows(UnavailableDateException.class,
                () -> appointmentsService.checkDateAvailabilityForDoctor(APPOINTMENT_REQUEST));

        Mockito.verify(appointmentsRepo).isDateAndTimeAvailableForDoctorWithId(
                anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class));

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

}
