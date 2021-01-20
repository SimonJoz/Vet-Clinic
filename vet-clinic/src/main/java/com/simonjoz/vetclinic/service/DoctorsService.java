package com.simonjoz.vetclinic.service;

import com.simonjoz.vetclinic.domain.Doctor;
import com.simonjoz.vetclinic.domain.dto.AppointmentDTO;
import com.simonjoz.vetclinic.domain.dto.DoctorDTO;
import com.simonjoz.vetclinic.domain.dto.PageDTO;
import com.simonjoz.vetclinic.exceptions.ResourceNotFoundException;
import com.simonjoz.vetclinic.mappers.PagesMapper;
import com.simonjoz.vetclinic.repository.DoctorsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorsService {

    private final PagesMapper<DoctorDTO> pagesMapper;
    private final DoctorsRepo doctorsRepo;
    private final AppointmentsService appointmentsService;


    public Doctor getDoctor(Long doctorId) {
        log.info("Fetching doctor with id '{}'.", doctorId);
        Doctor doctor = doctorsRepo.findById(doctorId)
                .orElseThrow(getDoctorNotFoundException(doctorId));
        log.debug("Return value of doctor entity: '{}'.", doctor);
        return doctor;
    }

    public PageDTO<DoctorDTO> getPage(PageRequest pageRequest) {
        log.info("Fetching doctors page: '{}' ", pageRequest);
        Page<DoctorDTO> doctorsPage = doctorsRepo.getDoctorsPage(pageRequest);
        PageDTO<DoctorDTO> resultPage = pagesMapper.map(doctorsPage);
        log.debug("Returned data: '{}' ", resultPage);
        return resultPage;
    }

    public PageDTO<AppointmentDTO> getAppointmentsPageById(PageRequest pageRequest, Long doctorId, LocalDate date) {
        throwExceptionIfNotExist(doctorId);
        if (date == null) {
            log.debug("Date is not present. Performing getAppointmentsPageByDoctorId() method.");
            return appointmentsService.getAppointmentsPageByDoctorId(pageRequest, doctorId);
        }
        log.debug("Date is present. Performing getAppointmentsPageByDoctorIdForDate() method.");
        return appointmentsService.getAppointmentsPageByDoctorIdForDate(pageRequest, doctorId, date);
    }


    private void throwExceptionIfNotExist(Long doctorId) {
        if (!doctorsRepo.existsById(doctorId)) {
            throw getDoctorNotFoundException(doctorId).get();
        }
    }

    private Supplier<ResourceNotFoundException> getDoctorNotFoundException(Long doctorId) {
        return () -> new ResourceNotFoundException(String.format("Doctor with id '%d' not found.", doctorId));
    }
}
