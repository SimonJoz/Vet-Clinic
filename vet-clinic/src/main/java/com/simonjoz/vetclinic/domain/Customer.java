package com.simonjoz.vetclinic.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity(name = "customers")
@ToString(exclude = "appointments")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(4)
    private int pin;

    private String name;
    private String surname;

    @JsonIgnore
    @OneToMany(mappedBy = "customer") // lazy by default
    private List<Appointment> appointments = new ArrayList<>();
}
