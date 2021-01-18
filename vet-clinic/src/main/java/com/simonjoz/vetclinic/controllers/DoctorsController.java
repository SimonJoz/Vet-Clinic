package com.simonjoz.vetclinic.controllers;

import com.simonjoz.vetclinic.domain.dto.AppointmentDTO;
import com.simonjoz.vetclinic.domain.dto.DoctorDTO;
import com.simonjoz.vetclinic.domain.dto.PageDTO;
import com.simonjoz.vetclinic.service.DoctorsService;
import com.simonjoz.vetclinic.utils.PageReqUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Api("Doctors")
@RestController
@RequestMapping("api/v1/doctors")
@RequiredArgsConstructor
public class DoctorsController {

    private final DoctorsService doctorsService;

    @GetMapping
    @ApiOperation("Method is used to fetch doctors page. Page is sortable depend on specified params.")
    public PageDTO<DoctorDTO> getDoctorsPage(
            @RequestParam(defaultValue = PageReqUtils.PAGE_ZERO, required = false) int page,
            @RequestParam(defaultValue = PageReqUtils.DEFAULT_SIZE, required = false) int pageSize,
            @RequestParam(defaultValue = PageReqUtils.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = PageReqUtils.DESC_FALSE, required = false) boolean isDesc) {
        PageRequest pageRequest = PageReqUtils.getPageRequest(page, pageSize, sortBy, isDesc);
        return doctorsService.getPage(pageRequest);
    }



    @GetMapping("{doctorId}/appointments")
    @ApiOperation(value = "Get all appointments by doctor id", notes = "Method is used to fetch appointments " +
            "page for doctor with specified id. Method takes optional argument of date in order to narrow result to certain period. " +
            "It is also sortable depend on specified params.")
    public PageDTO<AppointmentDTO> getAppointmentsPageByDoctorId(
            @RequestParam(defaultValue = PageReqUtils.PAGE_ZERO, required = false)
            @ApiParam(value = "Page number") int page,

            @RequestParam(defaultValue = PageReqUtils.DEFAULT_SIZE, required = false)
            @ApiParam(value = "Size of page") int pageSize,

            @RequestParam(defaultValue = PageReqUtils.DEFAULT_SORT_BY, required = false)
            @ApiParam(value = "Sort by value") String sortBy,

            @RequestParam(defaultValue = PageReqUtils.DESC_FALSE, required = false)
            @ApiParam(value = "Sort direction descending ?") boolean isDesc,

            @RequestParam(required = false)
            @ApiParam(format = "yyyy-MM-dd", example = "2021-01-24", value = "Appointments date") LocalDate date,

            @PathVariable @ApiParam(value = "Doctor ID", required = true) Long doctorId) {
        PageRequest pageRequest = PageReqUtils.getPageRequest(page, pageSize, sortBy, isDesc);
        return doctorsService.getAppointmentsPageById(pageRequest, doctorId, date);
    }
}
