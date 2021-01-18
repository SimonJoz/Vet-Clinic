package com.simonjoz.vetclinic.mappers;

import com.simonjoz.vetclinic.domain.Appointment;
import com.simonjoz.vetclinic.domain.dto.CustomerAppointmentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentsMapper {

    @Mapping(source = "doctor.name", target = "doctorName")
    @Mapping(source = "doctor.surname", target = "doctorSurname")
    CustomerAppointmentDTO map(Appointment appointment);

}
