package com.simonjoz.vetclinic.domain.dto;

import lombok.Value;

import java.time.LocalDate;


@Value
public class DoctorAppointmentDTO {
    Long id;
    String note;
    LocalDate scheduledDate;
    String customerName;
    String customerSurname;
}
