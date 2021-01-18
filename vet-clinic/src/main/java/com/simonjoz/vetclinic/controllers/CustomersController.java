package com.simonjoz.vetclinic.controllers;

import com.simonjoz.vetclinic.domain.AppointmentRequest;
import com.simonjoz.vetclinic.domain.dto.AppointmentDTO;
import com.simonjoz.vetclinic.domain.dto.CustomerDTO;
import com.simonjoz.vetclinic.domain.dto.PageDTO;
import com.simonjoz.vetclinic.service.CustomersService;
import com.simonjoz.vetclinic.utils.PageReqUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Api("Customers")
@RequestMapping("api/v1/customers")
@RequiredArgsConstructor
public class CustomersController {

    private final CustomersService customersService;

    @GetMapping
    @ApiOperation("Method is used to fetch customers page. Page is sortable depend on specified params.")
    public PageDTO<CustomerDTO> getCustomersPage(
            @RequestParam(defaultValue = PageReqUtils.PAGE_ZERO, required = false) int page,
            @RequestParam(defaultValue = PageReqUtils.DEFAULT_SIZE, required = false) int pageSize,
            @RequestParam(defaultValue = PageReqUtils.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = PageReqUtils.DESC_FALSE, required = false) boolean isDesc) {
        PageRequest pageRequest = PageReqUtils.getPageRequest(page, pageSize, sortBy, isDesc);
        return customersService.getPage(pageRequest);
    }

    @GetMapping("{customerId}/appointments")
    @ApiOperation("Method is used to fetch appointments page for customers with specified id. Page is sortable depend on specified params.")
    public PageDTO<AppointmentDTO> getAppointmentsPageByCustomerId(
            @RequestParam(defaultValue = PageReqUtils.PAGE_ZERO, required = false) int page,
            @RequestParam(defaultValue = PageReqUtils.DEFAULT_SIZE, required = false) int pageSize,
            @RequestParam(defaultValue = PageReqUtils.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = PageReqUtils.DESC_FALSE, required = false) boolean isDesc,
            @PathVariable Long customerId) {
        PageRequest pageRequest = PageReqUtils.getPageRequest(page, pageSize, sortBy, isDesc);
        return customersService.getAppointmentsPageById(pageRequest, customerId);
    }

    @PostMapping("{customerId}/appointments/create")
    @ApiOperation("Method is used to create new appointments with specified doctor for customers with given id. Page is sortable depend on specified params.")
    public ResponseEntity<AppointmentDTO> makeAppointment(
            @RequestBody AppointmentRequest appointmentRequest, @PathVariable Long customerId) {
        var dto = customersService.makeAppointment(appointmentRequest, customerId);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

}
