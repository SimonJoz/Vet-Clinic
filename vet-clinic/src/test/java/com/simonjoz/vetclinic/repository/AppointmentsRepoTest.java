package com.simonjoz.vetclinic.repository;

import com.simonjoz.vetclinic.domain.dto.AppointmentDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
@Sql(scripts = "classpath:test.sql")
class AppointmentsRepoTest {

    private static final long ID_ONE = 1L;
    private static final long NONE_EXISTING_ID = 100L;
    private static final PageRequest PAGE_REQUEST = PageRequest.of(0, 1);

    @Autowired
    private AppointmentsRepo appointmentsRepo;

    @Test
    void testGetDoctorAppointmentsPageForSpecifiedDate() {
        LocalDate expectedDate = LocalDate.parse("2022-01-23");
        Page<AppointmentDTO> receivedPage =
                appointmentsRepo.getDoctorAppointmentsPage(ID_ONE, expectedDate, PAGE_REQUEST);

        assertFalse(receivedPage.isEmpty());
        assertTrue(receivedPage.isLast());
        assertTrue(receivedPage.isFirst());
        assertTrue(receivedPage.hasContent());
        assertEquals(1, receivedPage.getTotalPages());
        assertEquals(1, receivedPage.getTotalElements());
        assertEquals("APPOINTMENT3", receivedPage.getContent().get(0).getNote());
        assertEquals(expectedDate, receivedPage.getContent().get(0).getScheduledDate());
        assertEquals("CUSTOMER2", receivedPage.getContent().get(0).getPersonName());
    }

    @Test
    void testGetDoctorAppointmentsPageForSpecifiedDateNoneExistingId() {
        LocalDate expectedDate = LocalDate.parse("2022-01-23");
        Page<AppointmentDTO> receivedPage =
                appointmentsRepo.getDoctorAppointmentsPage(NONE_EXISTING_ID, expectedDate, PAGE_REQUEST);

        assertTrue(receivedPage.isEmpty());
        assertTrue(receivedPage.isLast());
        assertTrue(receivedPage.isFirst());
        assertTrue(receivedPage.getContent().isEmpty());
        assertFalse(receivedPage.hasContent());
    }

    @Test
    void testGetDoctorAppointmentsPageForDoctorById() {
        Page<AppointmentDTO> receivedPage =
                appointmentsRepo.getDoctorAppointmentsPage(ID_ONE, PAGE_REQUEST);

        assertFalse(receivedPage.isLast());
        assertFalse(receivedPage.isEmpty());
        assertTrue(receivedPage.isFirst());
        assertTrue(receivedPage.hasContent());
        assertEquals(2, receivedPage.getTotalPages());
        assertEquals(2, receivedPage.getTotalElements());
        assertEquals("APPOINTMENT1", receivedPage.getContent().get(0).getNote());
    }

    @Test
    void testGetDoctorAppointmentsPageForDoctorByIdNotFound() {
        Page<AppointmentDTO> receivedPage =
                appointmentsRepo.getDoctorAppointmentsPage(NONE_EXISTING_ID, PAGE_REQUEST);

        assertTrue(receivedPage.isEmpty());
        assertTrue(receivedPage.isLast());
        assertTrue(receivedPage.isFirst());
        assertTrue(receivedPage.getContent().isEmpty());
        assertFalse(receivedPage.hasContent());
    }


    @ParameterizedTest
    @MethodSource("customerIdAndTimestampSupplier")
    void testExistsByIdAndTimestampShouldReturnTrue(Long id, LocalDateTime timestamp) {
        assertTrue(appointmentsRepo.existsByCustomerIdAndTimestamp(id, timestamp));
    }

    @ParameterizedTest
    @MethodSource("customerIdAndTimestampSupplier")
    @Sql(scripts = "classpath:clean-up.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testExistsByIdAndTimestampShouldReturnFalse(Long id, LocalDateTime timestamp) {
        assertFalse(appointmentsRepo.existsByCustomerIdAndTimestamp(id, timestamp));
    }

    @ParameterizedTest
    @MethodSource("customerIdAndTimestampSupplier")
    void testDeleteByCustomerIdAndTimestamp(Long id, LocalDateTime timestamp) {
        appointmentsRepo.deleteByCustomerIdAndTimestamp(id, timestamp);
        assertFalse(appointmentsRepo.existsByCustomerIdAndTimestamp(id, timestamp));
    }

    private static Stream<Arguments> customerIdAndTimestampSupplier() {
        return Stream.of(
                Arguments.of(1L, LocalDateTime.parse("2022-01-21T12:00:00")),
                Arguments.of(1L, LocalDateTime.parse("2022-01-22T12:00:00")),
                Arguments.of(2L, LocalDateTime.parse("2022-01-23T12:00:00")),
                Arguments.of(2L, LocalDateTime.parse("2022-01-24T12:00:00")));
    }

    @ParameterizedTest
    @MethodSource("timeRangeSupplierFalseValues")
    void testIsDateAndTimeAvailableForDoctorWithIdShouldReturnFalse(LocalTime startTime, LocalTime endTime) {
        /*
            Original appointment request time would be the middle of provided range
                e.g(start - 11:30; end - 12:30; original - 12:00)
        */

        assertFalse(appointmentsRepo.isDateAndTimeAvailableForDoctorWithId(
                1L, LocalDate.parse("2022-01-21"), startTime, endTime));

        assertFalse(appointmentsRepo.isDateAndTimeAvailableForDoctorWithId(
                2L, LocalDate.parse("2022-01-22"), startTime, endTime));

        assertFalse(appointmentsRepo.isDateAndTimeAvailableForDoctorWithId(
                1L, LocalDate.parse("2022-01-23"), startTime, endTime));

        assertFalse(appointmentsRepo.isDateAndTimeAvailableForDoctorWithId(
                2L, LocalDate.parse("2022-01-24"), startTime, endTime));
    }

    private static Stream<Arguments> timeRangeSupplierFalseValues() {
        return Stream.of(
                Arguments.of(LocalTime.parse("11:30"), LocalTime.parse("12:30")),
                Arguments.of(LocalTime.parse("11:29"), LocalTime.parse("12:29")),
                Arguments.of(LocalTime.parse("11:01"), LocalTime.parse("12:01")),
                Arguments.of(LocalTime.parse("11:20"), LocalTime.parse("12:20")),
                Arguments.of(LocalTime.parse("11:10"), LocalTime.parse("12:10")),
                Arguments.of(LocalTime.parse("11:05"), LocalTime.parse("12:05")));
    }

    @ParameterizedTest
    @MethodSource("timeRangeSupplierTrueValues")
    void testIsDateAndTimeAvailableForDoctorWithIdShouldReturnTrue(LocalTime startTime, LocalTime endTime) {
        /*
            Original appointment request time would be the middle of provided  range
                e.g(start - 12:00; end - 13:00; original - 12:30)
        */
        assertTrue(appointmentsRepo.isDateAndTimeAvailableForDoctorWithId(
                1L, LocalDate.parse("2022-01-21"), startTime, endTime));

        assertTrue(appointmentsRepo.isDateAndTimeAvailableForDoctorWithId(
                2L, LocalDate.parse("2022-01-22"), startTime, endTime));

        assertTrue(appointmentsRepo.isDateAndTimeAvailableForDoctorWithId(
                1L, LocalDate.parse("2022-01-23"), startTime, endTime));

        assertTrue(appointmentsRepo.isDateAndTimeAvailableForDoctorWithId(
                2L, LocalDate.parse("2022-01-24"), startTime, endTime));
    }

    private static Stream<Arguments> timeRangeSupplierTrueValues() {
        return Stream.of(
                Arguments.of(LocalTime.parse("12:00"), LocalTime.parse("13:00")),
                Arguments.of(LocalTime.parse("12:35"), LocalTime.parse("13:35")),
                Arguments.of(LocalTime.parse("12:20"), LocalTime.parse("13:20")),
                Arguments.of(LocalTime.parse("12:29"), LocalTime.parse("13:29")),
                Arguments.of(LocalTime.parse("13:00"), LocalTime.parse("14:00")),
                Arguments.of(LocalTime.parse("12:01"), LocalTime.parse("13:01")));
    }

}
