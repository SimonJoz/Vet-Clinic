package com.simonjoz.vetclinic.repository;

import com.simonjoz.vetclinic.domain.Appointment;
import com.simonjoz.vetclinic.domain.dto.AppointmentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface AppointmentsRepo extends JpaRepository<Appointment, Long> {

    @Query("SELECT new com.simonjoz.vetclinic.domain.dto.AppointmentDTO(a.id, a.note, a.scheduledDate, a.scheduledTime, " +
            "a.customer.name, a.customer.surname) FROM appointments a WHERE a.doctor.id = :doctorId")
    Page<AppointmentDTO> getDoctorAppointmentsPage(Long doctorId, Pageable pageable);

    @Query("SELECT new com.simonjoz.vetclinic.domain.dto.AppointmentDTO(a.id, a.note, a.scheduledDate, a.scheduledTime, " +
            "a.customer.name, a.customer.surname) FROM appointments a WHERE a.doctor.id = :doctorId AND a.scheduledDate = :date")
    Page<AppointmentDTO> getDoctorAppointmentsPage(Long doctorId, LocalDate date, Pageable pageable);


    @Query("SELECT CASE WHEN count(a.id) = 0 THEN true ELSE false END FROM appointments a " +
            "WHERE a.doctor.id = :doctorId AND a.scheduledDate = :date AND a.scheduledTime > :startTime AND a.scheduledTime < :endTime")
    boolean isDateAndTimeAvailableForDoctorWithId(Long doctorId, LocalDate date, LocalTime startTime, LocalTime endTime);

    @Modifying
    @Transactional
    @Query("DELETE FROM appointments a WHERE a.customer.id = :customerId AND a.timestamp = :timestamp")
    void deleteByCustomerIdAndTimestamp(Long customerId, LocalDateTime timestamp);


    @Query("SELECT CASE WHEN count(a.id) > 0 THEN true ELSE false END FROM appointments a " +
            "WHERE a.customer.id = :customerId AND a.timestamp = :timestamp")
    boolean existsByCustomerIdAndTimestamp(Long customerId, LocalDateTime timestamp);
}
