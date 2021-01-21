package com.simonjoz.vetclinic.domain.dto;

import lombok.Value;

import java.time.LocalTime;

@Value
public class TimingDetailsDTO {
    int visitDurationInMinutes;
    LocalTime openingAt;
    LocalTime closingAt;
}
