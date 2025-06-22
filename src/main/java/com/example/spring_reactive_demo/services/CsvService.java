package com.example.spring_reactive_demo.services;
import com.example.spring_reactive_demo.dto.EmployeeDto;
import com.example.spring_reactive_demo.models.Employee;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.CsvToBean;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.FileReader;
import java.io.Reader;
import java.util.Iterator;

@Service
public class CsvService {

    private final ModelMapper modelMapper;

    public CsvService(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public Flux<Employee> loadEmployees() {
        return Flux.using(
                this::openCsvBeanIterator,
                wrapper -> {
                    Iterator<EmployeeDto> iterator = wrapper.iterator();
                    return Flux.<Employee>generate(
                            sink -> {
                                if (iterator.hasNext()) {
                                    EmployeeDto dto = iterator.next();
                                    Employee employee = modelMapper.map(dto, Employee.class);
                                    sink.next(employee);
                                } else {
                                    sink.complete();
                                }
                            });
                },
                this::closeCsvIteratorResources
        ).subscribeOn(Schedulers.boundedElastic());
    }

    private CsvIteratorWrapper openCsvBeanIterator() {
        try {
            Reader reader = new FileReader("employees.csv");
            CsvToBean<EmployeeDto> csvToBean = new CsvToBeanBuilder<EmployeeDto>(reader)
                    .withType(EmployeeDto.class)
                    .withSkipLines(1)
                    .build();

            Iterator<EmployeeDto> iterator = csvToBean.iterator();
            return new CsvIteratorWrapper(reader, iterator);
        } catch (Exception e) {
            throw new RuntimeException("Failed to open CSV file", e);
        }
    }

    private void closeCsvIteratorResources(CsvIteratorWrapper wrapper) {
        try {
            wrapper.reader().close();
        } catch (Exception e) {
            // log or ignore
        }
    }

    // Helper class to hold both reader and iterator
    private static class CsvIteratorWrapper {
        private final Reader reader;
        private final Iterator<EmployeeDto> iterator;

        public CsvIteratorWrapper(Reader reader, Iterator<EmployeeDto> iterator) {
            this.reader = reader;
            this.iterator = iterator;
        }

        public Reader reader() {
            return reader;
        }

        public Iterator<EmployeeDto> iterator() {
            return iterator;
        }


    }
}

