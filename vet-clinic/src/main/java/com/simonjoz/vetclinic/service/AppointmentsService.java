package com.simonjoz.vetclinic.service;

import com.simonjoz.vetclinic.domain.Appointment;
import com.simonjoz.vetclinic.domain.dto.AppointmentDTO;
import com.simonjoz.vetclinic.domain.dto.PageDTO;
import com.simonjoz.vetclinic.mappers.CustomerAppointmentMapper;
import com.simonjoz.vetclinic.mappers.PagesMapper;
import com.simonjoz.vetclinic.repository.AppointmentsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentsService {

    private final AppointmentsRepo appointmentsRepo;
    private final CustomerAppointmentMapper customerAppointmentsMapper;
    private final PagesMapper<AppointmentDTO> pageMapper;


    public PageDTO<AppointmentDTO> getAppointmentsPageByDoctorIdForDay(PageRequest pageRequest, Long doctorId, LocalDate date) {
        log.info("Fetching appointments for doctor with id: '{}' matching date: '{}'. '{}' ", doctorId, date, pageRequest);
        Page<AppointmentDTO> appointmentsPage = appointmentsRepo.getDoctorAppointmentsPage(doctorId, date, pageRequest);
        log.debug("Retrieved page: '{}' ", appointmentsPage);
        PageDTO<AppointmentDTO> resultPage = pageMapper.map(appointmentsPage);
        log.debug("Returned data: '{}' ", resultPage);
        return resultPage;
    }

    public PageDTO<AppointmentDTO> getAppointmentsPageByDoctorId(PageRequest pageRequest, Long doctorId) {
        log.info("Fetching appointments for doctor with id: '{}'. '{}' ", doctorId, pageRequest);
        Page<AppointmentDTO> appointmentsPage = appointmentsRepo.getDoctorAppointmentsPage(doctorId, pageRequest);
        log.debug("Retrieved page: '{}' ", appointmentsPage);
        PageDTO<AppointmentDTO> resultPage = pageMapper.map(appointmentsPage);
        log.debug("Returned data: '{}' ", resultPage);
        return resultPage;
    }

    public PageDTO<AppointmentDTO> getAppointmentsPageByCustomerId(PageRequest pageRequest, Long customerId) {
        log.info("Fetching appointments for customer with id: '{}'. '{}'", customerId, pageRequest);
        Page<AppointmentDTO> appointmentsPage =
                appointmentsRepo.getCustomerAppointmentsPage(customerId, pageRequest);
        log.debug("Retrieved page: '{}' ", appointmentsPage);
        PageDTO<AppointmentDTO> resultPage = pageMapper.map(appointmentsPage);
        log.debug("Returned data: '{}' ", resultPage);
        return resultPage;
    }

    public AppointmentDTO addAppointment(Appointment appointment) {
        log.info("Creating new appointment '{}'.", appointment);
        Appointment savedAppointment = appointmentsRepo.save(appointment);
        log.debug("Created appointment: {}.", appointment);
        AppointmentDTO appointmentDTO = customerAppointmentsMapper.map(savedAppointment);
        log.debug("Return value of appointment dto: {}.", appointmentDTO);
        return appointmentDTO;
    }
}
