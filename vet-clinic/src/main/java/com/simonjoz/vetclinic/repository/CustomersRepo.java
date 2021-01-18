package com.simonjoz.vetclinic.repository;

import com.simonjoz.vetclinic.domain.Customer;
import com.simonjoz.vetclinic.domain.dto.CustomerDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CustomersRepo extends JpaRepository<Customer, Long> {

    @Query("SELECT new com.simonjoz.vetclinic.domain.dto.CustomerDTO(c.id, c.pin, c.name, c.surname) FROM customers c")
    Page<CustomerDTO> getCustomersPage(PageRequest pageRequest);
}