package com.simonjoz.vetclinic.domain.dto;

import lombok.Value;

@Value // all fields are private and final by default
public class CustomerDTO {
    Long id;
    int pin;
    String name;
    String surname;
}
