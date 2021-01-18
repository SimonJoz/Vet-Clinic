package com.simonjoz.vetclinic.domain.dto;

import lombok.Value;

import java.time.LocalDate;

@Value
public class AppointmentDTO {
    Long id;
    String note;
    LocalDate scheduledDate;
    String personName;
    String personSurname;
}
