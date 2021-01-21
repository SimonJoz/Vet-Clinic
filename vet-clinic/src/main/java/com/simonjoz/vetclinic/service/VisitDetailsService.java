package com.simonjoz.vetclinic.service;

import com.simonjoz.vetclinic.domain.dto.TimingDetailsDTO;
import com.simonjoz.vetclinic.exceptions.ResourceNotFoundException;
import com.simonjoz.vetclinic.repository.VisitDetailsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitDetailsService {

    private final VisitDetailsRepo visitDetailsRepo;

    @Cacheable("doctorTimeDetails")
    public TimingDetailsDTO getTimingDetails(Long doctorId) {
        log.info("Fetching timing details for doctor with id '{}'.", doctorId);
        TimingDetailsDTO timingDetailsDTO = visitDetailsRepo.getTimingDetails(doctorId).orElseThrow(() -> new
                ResourceNotFoundException(String.format("Timing details not found for doctor with id '%d'.", doctorId)));
        log.debug("Retrieved details: {}.", timingDetailsDTO);
        return timingDetailsDTO;
    }
}
