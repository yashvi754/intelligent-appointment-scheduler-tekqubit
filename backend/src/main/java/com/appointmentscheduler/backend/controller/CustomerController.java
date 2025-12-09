package com.appointmentscheduler.backend.controller;

import com.appointmentscheduler.backend.entity.Customer;
import com.appointmentscheduler.backend.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customerRepository;

    @GetMapping("/search")
    public List<Customer> searchCustomers(@RequestParam(name = "q", required = false) String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        return customerRepository
                .findByNameContainingIgnoreCaseOrPhoneContainingIgnoreCase(query, query);
    }
}
