package com.example.spring_reactive_demo.services;

import com.example.spring_reactive_demo.models.Employee;
import com.example.spring_reactive_demo.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;


@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Mono<List<Employee>> saveBatch(List<Employee> batch) {
        return Mono.fromCallable(() -> {
                    // Optionally log batch size + thread
                    System.out.println("Saving batch of size: " + batch.size() +
                            " on thread: " + Thread.currentThread().getName());
                    return employeeRepository.saveAll(batch);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<Employee> saveAll(Flux<Employee> employees) {
        return employees
                .buffer(10)  // Collect 1000 employees per batch (tune as needed)
                .flatMap(batch -> saveBatch(batch).flatMapMany(Flux::fromIterable));
    }
//    public Mono<Employee> saveEmployee(Employee employee) {
//        return Mono.fromCallable(() -> employeeRepository.save(employee))
//                .subscribeOn(Schedulers.boundedElastic());
//    }
//
//
//    public Flux<Employee> saveAll(Flux<Employee> employees) {
//        return employees.flatMap(this::saveEmployee);
//    }

}
