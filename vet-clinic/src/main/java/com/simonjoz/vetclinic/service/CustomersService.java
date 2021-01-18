package com.simonjoz.vetclinic.service;

import com.simonjoz.vetclinic.domain.Appointment;
import com.simonjoz.vetclinic.domain.AppointmentRequest;
import com.simonjoz.vetclinic.domain.Customer;
import com.simonjoz.vetclinic.domain.Doctor;
import com.simonjoz.vetclinic.domain.dto.AppointmentDTO;
import com.simonjoz.vetclinic.domain.dto.CustomerDTO;
import com.simonjoz.vetclinic.domain.dto.PageDTO;
import com.simonjoz.vetclinic.exceptions.InvalidPinException;
import com.simonjoz.vetclinic.exceptions.ResourceNotFoundException;
import com.simonjoz.vetclinic.mappers.PagesMapper;
import com.simonjoz.vetclinic.repository.CustomersRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomersService {

    private final CustomersRepo customersRepo;
    private final PagesMapper<CustomerDTO> pagesMapper;
    private final AppointmentsService appointmentsService;
    private final DoctorsService doctorsService;

    public PageDTO<CustomerDTO> getPage(PageRequest pageRequest) {
        log.info("Fetching customers page: '{}' ", pageRequest);
        Page<CustomerDTO> doctorsPage = customersRepo.getCustomersPage(pageRequest);
        log.debug("Retrieved page: '{}' ", doctorsPage);
        PageDTO<CustomerDTO> resultPage = pagesMapper.map(doctorsPage);
        log.debug("Returned data: '{}' ", resultPage);
        return resultPage;
    }

    public PageDTO<AppointmentDTO> getAppointmentsPageById(PageRequest pageRequest, Long customerId) {
        return appointmentsService.getAppointmentsPageByCustomerId(pageRequest, customerId);
    }

    public AppointmentDTO makeAppointment(AppointmentRequest appointmentReq, Long customerId) {
        Customer customer = getCustomer(customerId);
        validateCustomerPin(customer.getPin(), appointmentReq.getCustomerPin());

        long doctorId = appointmentReq.getDoctorId();
        Doctor doctor = doctorsService.getDoctor(doctorId);

        Appointment appointment = Appointment.builder()
                .customer(customer)
                .doctor(doctor)
                .note(appointmentReq.getNote())
                .scheduledDate(appointmentReq.getDate())
                .build();

        log.debug("Created appointment entity: {}", customerId);
        return appointmentsService.addAppointment(appointment);
    }

    public Customer getCustomer(Long customerId) {
        log.info("Fetching customer with id '{}'.", customerId);
        Customer customer = customersRepo.findById(customerId).orElseThrow(
                () -> new ResourceNotFoundException(String.format("Customer with id '%d' not found.", customerId)));
        log.debug("Return value of customer entity: '{}'.", customer);
        return customer;
    }


    private void validateCustomerPin(int validPin, int pin) {
        log.info("Checking pin validity... Given: '{}'. Valid: '{}'. ", pin, validPin);
        if (validPin != pin) {
            String errorMsg = String.format("Given pin '%d' is invalid", pin);
            log.warn("InvalidPinException:  {}", errorMsg);
            throw new InvalidPinException(errorMsg);
        }
        log.debug("Pin number '{}' valid.", pin);

    }
}
