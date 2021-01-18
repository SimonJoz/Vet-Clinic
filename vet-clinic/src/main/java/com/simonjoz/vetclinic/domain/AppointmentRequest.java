package com.simonjoz.vetclinic.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class AppointmentRequest {
    int customerPin;
    Long doctorId;
    String note;
    LocalDate date;
}
