package com.simonjoz.vetclinic.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // on db side.
    private Long id;
    private String note;
    private LocalDate scheduledDate;

    // ManyToOne fetch type is eager by default.
    // Lazy fetch will cause jackson to fail on empty bean.
    // NOTE: In case of direct fetching without mapping to DTO - n + 1 issue need to be handled.

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;
}
