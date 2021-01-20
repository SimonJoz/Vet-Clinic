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
import com.simonjoz.vetclinic.exceptions.UnavailableDateException;
import com.simonjoz.vetclinic.mappers.PagesMapper;
import com.simonjoz.vetclinic.repository.CustomersRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomersService {

    private final CustomersRepo customersRepo;
    private final PagesMapper<CustomerDTO> pagesMapper;
    private final AppointmentsService appointmentsService;
    private final DoctorsService doctorsService;

    public Customer getCustomer(Long customerId) {
        log.info("Fetching customer with id '{}'.", customerId);
        Customer customer = customersRepo.findById(customerId)
                .orElseThrow(getNotFoundExceptionSupplier(customerId));

        log.debug("Return value of customer entity: '{}'.", customer);
        return customer;
    }

    public PageDTO<CustomerDTO> getPage(PageRequest pageRequest) {
        log.info("Fetching customers page: '{}' ", pageRequest);
        Page<CustomerDTO> doctorsPage = customersRepo.getCustomersPage(pageRequest);
        PageDTO<CustomerDTO> resultPage = pagesMapper.map(doctorsPage);
        log.debug("Returned data: '{}' ", resultPage);
        return resultPage;
    }

    public AppointmentDTO makeAppointment(AppointmentRequest appointmentReq, Long customerId) {
        log.info("Appointment making process for request '{}' has started.", appointmentReq);

        validateIsAppointmentTimeInPast(appointmentReq.getDate(), appointmentReq.getTime());

        Customer customer = getCustomer(customerId);
        validateCustomerPin(customer.getPin(), appointmentReq.getCustomerPin());

        appointmentsService.checkDateAvailabilityForDoctor(appointmentReq);
        Doctor doctor = doctorsService.getDoctor(appointmentReq.getDoctorId());

        Appointment appointment = Appointment.builder()
                .customer(customer)
                .doctor(doctor)
                .note(appointmentReq.getNote())
                .scheduledDate(appointmentReq.getDate())
                .scheduledTime(appointmentReq.getTime())
                .timestamp(LocalDateTime.of(appointmentReq.getDate(), appointmentReq.getTime()))
                .build();

        AppointmentDTO appointmentDTO = appointmentsService.addAppointment(appointment);
        log.info("Appointment making process completed.");
        return appointmentDTO;
    }


    public void cancelAppointment(AppointmentRequest appointmentReq, Long customerId) {
        log.info("Appointment cancellation process for request '{}' has started.", appointmentReq);
        int customerValidPin = getCustomerPinById(customerId);
        validateCustomerPin(customerValidPin, appointmentReq.getCustomerPin());
        LocalDateTime appointmentTimestamp = LocalDateTime.of(appointmentReq.getDate(), appointmentReq.getTime());
        appointmentsService.deleteAppointment(customerId, appointmentTimestamp);
        log.info("Appointment has been canceled successfully.");
    }

    private void validateCustomerPin(int validPin, int pin) {
        log.info("Checking pin validity. Given: '{}'. Valid: '{}'. ", pin, validPin);
        if (validPin != pin) {
            throw new InvalidPinException(String.format("Given pin '%d' is invalid", pin));
        }
        log.debug("Pin number '{}' valid.", pin);
    }

    private int getCustomerPinById(Long customerId) {
        log.info("Fetching pin of customer with id '{}'", customerId);
        int customerPin = customersRepo.getCustomerPinById(customerId)
                .orElseThrow(getNotFoundExceptionSupplier(customerId));
        log.debug("Retrieved pin: '{}'.", customerPin);
        return customerPin;
    }

    private void validateIsAppointmentTimeInPast(LocalDate date, LocalTime time) {
        log.info("Validating requested time for appointment.");
        LocalDate todayDate = LocalDate.now();
        if (date.isEqual(todayDate) && time.isBefore(LocalTime.now())) {
            throw new UnavailableDateException(
                    String.format("Appointment time must not be in past. Request time: '%s'.", time));
        }
    }

    private Supplier<ResourceNotFoundException> getNotFoundExceptionSupplier(long customerId) {
        return () -> new ResourceNotFoundException(String.format("Customer with id '%d' not found.", customerId));
    }


}
