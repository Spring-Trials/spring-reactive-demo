package com.example.spring_reactive_demo.controllers;

//import com.example.spring_reactive_demo.services.ReactiveImportService;

import com.example.spring_reactive_demo.models.Employee;
import com.example.spring_reactive_demo.services.CsvService;
import com.example.spring_reactive_demo.services.EmployeeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/import")
public class ImportController {
    private final EmployeeService employeeService;
    private final CsvService csvService;

    public ImportController(
            EmployeeService employeeService,
            CsvService csvService
    ) {
        this.employeeService = employeeService;
        this.csvService = csvService;
    }

    @PostMapping("/persons")
    public Flux<Employee> importPersons() {
        // Pass the entire Flux<Employee> to saveAll
        return employeeService.saveAll(csvService.loadEmployees());
    }
}
