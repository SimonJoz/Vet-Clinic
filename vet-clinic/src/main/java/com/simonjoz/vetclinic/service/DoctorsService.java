package com.simonjoz.vetclinic.service;

import com.simonjoz.vetclinic.domain.Doctor;
import com.simonjoz.vetclinic.domain.dto.DoctorAppointmentDTO;
import com.simonjoz.vetclinic.domain.dto.DoctorDTO;
import com.simonjoz.vetclinic.domain.dto.PageDTO;
import com.simonjoz.vetclinic.exceptions.ResourceNotFoundException;
import com.simonjoz.vetclinic.mappers.PagesMapper;
import com.simonjoz.vetclinic.repository.DoctorsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorsService {

    private final PagesMapper<DoctorDTO> pagesMapper;
    private final DoctorsRepository doctorsRepository;
    private final AppointmentsService appointmentsService;


    public PageDTO<DoctorDTO> getPage(PageRequest pageRequest) {
        log.info("Fetching doctors page: '{}' ", pageRequest);
        Page<DoctorDTO> doctorsPage = doctorsRepository.getDoctorsPage(pageRequest);
        log.debug("Retrieved page: '{}' ", doctorsPage);
        PageDTO<DoctorDTO> resultPage = pagesMapper.map(doctorsPage);
        log.debug("Returned data: '{}' ", resultPage);
        return resultPage;
    }

    public PageDTO<DoctorAppointmentDTO> getAppointmentsPageById(PageRequest pageRequest, Long doctorId, LocalDate date) {
        if (date == null) {
            log.debug("Date is not present. Performing getAppointmentsPageByDoctorId() method.");
            return appointmentsService.getAppointmentsPageByDoctorId(pageRequest, doctorId);
        }
        log.debug("Date is present. Performing getAppointmentsPageByDoctorIdForDay() method.");
        return appointmentsService.getAppointmentsPageByDoctorIdForDay(pageRequest, doctorId, date);
    }

    public Doctor getDoctor(Long doctorId) {
        log.info("Fetching doctor with id '{}'.", doctorId);
        Doctor doctor = doctorsRepository.findById(doctorId).orElseThrow(
                () -> new ResourceNotFoundException(String.format("Doctor with id '%d' not found.", doctorId)));
        log.debug("Return value of doctor entity: '{}'.", doctor);
        return doctor;
    }
}
