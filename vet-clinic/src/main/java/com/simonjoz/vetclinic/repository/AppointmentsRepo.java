package com.simonjoz.vetclinic.repository;

import com.simonjoz.vetclinic.domain.Appointment;
import com.simonjoz.vetclinic.domain.dto.AppointmentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface AppointmentsRepo extends JpaRepository<Appointment, Long> {

    @Query("SELECT new com.simonjoz.vetclinic.domain.dto.AppointmentDTO(a.id, a.note, a.scheduledDate, a.customer.name, a.customer.surname) " +
            "FROM appointments a WHERE a.doctor.id = :doctorId")
    Page<AppointmentDTO> getDoctorAppointmentsPage(Long doctorId, Pageable pageable);

    @Query("SELECT new com.simonjoz.vetclinic.domain.dto.AppointmentDTO(a.id, a.note, a.scheduledDate, a.customer.name, a.customer.surname) " +
            "FROM appointments a WHERE a.doctor.id = :doctorId AND a.scheduledDate = :date")
    Page<AppointmentDTO> getDoctorAppointmentsPage(Long doctorId, LocalDate date, Pageable pageable);


    @Query("SELECT new com.simonjoz.vetclinic.domain.dto.AppointmentDTO(a.id, a.note, a.scheduledDate, a.doctor.name, a.doctor.surname) " +
            "FROM appointments a WHERE a.customer.id = :customerId")
    Page<AppointmentDTO> getCustomerAppointmentsPage(Long customerId, Pageable pageable);

}
