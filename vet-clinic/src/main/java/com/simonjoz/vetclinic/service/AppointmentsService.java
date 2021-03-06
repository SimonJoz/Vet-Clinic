package com.simonjoz.vetclinic.service;

import com.simonjoz.vetclinic.domain.Appointment;
import com.simonjoz.vetclinic.domain.AppointmentRequest;
import com.simonjoz.vetclinic.domain.dto.AppointmentDTO;
import com.simonjoz.vetclinic.domain.dto.PageDTO;
import com.simonjoz.vetclinic.domain.dto.TimingDetailsDTO;
import com.simonjoz.vetclinic.exceptions.RemovalFailureException;
import com.simonjoz.vetclinic.exceptions.UnavailableDateException;
import com.simonjoz.vetclinic.mappers.CustomerAppointmentMapper;
import com.simonjoz.vetclinic.mappers.PagesMapper;
import com.simonjoz.vetclinic.repository.AppointmentsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentsService {

    private final AppointmentsRepo appointmentsRepo;
    private final VisitDetailsService visitDetailsService;
    private final CustomerAppointmentMapper customerAppointmentsMapper;
    private final PagesMapper<AppointmentDTO> pageMapper;

    public PageDTO<AppointmentDTO> getAppointmentsPageByDoctorIdForDate(PageRequest pageRequest, Long doctorId, LocalDate date) {
        log.info("Fetching appointments for doctor with id: '{}' matching date: '{}'. '{}' ", doctorId, date, pageRequest);
        Page<AppointmentDTO> appointmentsPage = appointmentsRepo.getDoctorAppointmentsPage(doctorId, date, pageRequest);
        PageDTO<AppointmentDTO> resultPage = pageMapper.map(appointmentsPage);
        log.debug("Returned data: '{}' ", resultPage);
        return resultPage;
    }

    public PageDTO<AppointmentDTO> getAppointmentsPageByDoctorId(PageRequest pageRequest, Long doctorId) {
        log.info("Fetching appointments for doctor with id: '{}'. '{}' ", doctorId, pageRequest);
        Page<AppointmentDTO> appointmentsPage = appointmentsRepo.getDoctorAppointmentsPage(doctorId, pageRequest);
        PageDTO<AppointmentDTO> resultPage = pageMapper.map(appointmentsPage);
        log.debug("Returned data: '{}' ", resultPage);
        return resultPage;
    }

    @CacheEvict(value = "doctorAppointmentsPage", allEntries = true)
    public AppointmentDTO addAppointment(Appointment appointment) {
        log.info("Creating new appointment '{}'.", appointment);
        Appointment savedAppointment = appointmentsRepo.save(appointment);
        log.debug("Saved appointment entity: {}.", appointment);
        AppointmentDTO appointmentDTO = customerAppointmentsMapper.map(savedAppointment);
        log.debug("Return value of appointment dto: {}.", appointmentDTO);
        log.info("Appointment added successfully.");
        return appointmentDTO;
    }

    public void checkDateAvailabilityForDoctor(AppointmentRequest appointmentReq) {
        log.info("Checking date and time availability for request: {}", appointmentReq);

        TimingDetailsDTO timingDetails = visitDetailsService.getTimingDetails(appointmentReq.getDoctorId());
        final int appointmentDuration = timingDetails.getVisitDurationInMinutes();
        final LocalTime reqTime = appointmentReq.getTime();

        checkIsOpen(timingDetails, reqTime);

        final LocalTime startTime = reqTime.minusMinutes(appointmentDuration);
        final LocalTime endTime = reqTime.plusMinutes(appointmentDuration);

        LocalDateTime startTimestamp = LocalDateTime.of(appointmentReq.getDate(), startTime);
        LocalDateTime endTimestamp = LocalDateTime.of(appointmentReq.getDate(), endTime);
        LocalDateTime appointmentTimestamp = LocalDateTime.of(appointmentReq.getDate(), reqTime);

        //  NOTE: In case of midnight ranges e.g(23:50 - 00:50) validation would fail due to checking the same date
        if (endTime.getHour() == 0) {
            endTimestamp = LocalDateTime.of(appointmentReq.getDate().plusDays(1), endTime);
        }

        log.debug("Range of timestamps to be check: [start: {}], [end: {}].", startTimestamp, endTimestamp);
        log.debug("Actual appointment timestamp: {}.", appointmentTimestamp);

        boolean isAvailable = appointmentsRepo.isDateAndTimeAvailableForDoctorWithId(appointmentReq.getDoctorId(),
                startTimestamp, endTimestamp, appointmentTimestamp);

        log.debug("Is date available: {}", isAvailable);

        String formattedTimestamp = appointmentTimestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        throwExceptionIfDateNotAvailability(isAvailable, formattedTimestamp);
    }

    @CacheEvict(value = "doctorAppointmentsPage", allEntries = true)
    public void deleteAppointment(Long customerId, LocalDateTime appointmentTimestamp) {
        log.info("Removing appointment with timestamp '{}' of user with id '{}'.", appointmentTimestamp, customerId);
        appointmentsRepo.deleteByCustomerIdAndTimestamp(customerId, appointmentTimestamp);

        // TODO: Should I check entity existence explicitly ?? Is it good practice ??
        boolean exist = appointmentsRepo.existsByCustomerIdAndTimestamp(customerId, appointmentTimestamp);
        if (exist) {
            throw new RemovalFailureException("Appointment cancellation has failed !");
        }
        log.info("Removal transaction completed.");
    }


    private void checkIsOpen(TimingDetailsDTO timingDetails, LocalTime reqAppointmentTime) {
        final LocalTime openingAt = timingDetails.getOpeningAt();
        final LocalTime closingAt = timingDetails.getClosingAt();
        final int visitDuration = timingDetails.getVisitDurationInMinutes();

        if (reqAppointmentTime.isBefore(openingAt) || reqAppointmentTime.equals(closingAt)
                || reqAppointmentTime.isAfter(closingAt.minusMinutes(visitDuration))) {
            throw new UnavailableDateException(String.format(
                    "Cannot schedule appointment at %s. Visit duration: %s min. Opening times: %s - %s.",
                    reqAppointmentTime, visitDuration, openingAt, closingAt));
        }
    }

    private void throwExceptionIfDateNotAvailability(boolean isAvailable, String appointmentTimestamp) {
        if (!isAvailable) {
            String errMsg = String.format("Date '%s' is already taken. Please try schedule appointment at different time.", appointmentTimestamp);
            throw new UnavailableDateException(errMsg);
        }
    }


}
