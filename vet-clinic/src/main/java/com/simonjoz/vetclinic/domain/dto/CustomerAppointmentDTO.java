package com.simonjoz.vetclinic.domain.dto;

import lombok.Value;

import java.time.LocalDate;

@Value
public class CustomerAppointmentDTO {
    Long id;
    String note;
    LocalDate scheduledDate;
    String doctorName;
    String doctorSurname;
}
