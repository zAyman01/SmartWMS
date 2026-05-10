package com.warehousewms.service;

import com.warehousewms.model.Customer;
import com.warehousewms.repository.CustomerRepository;

import javax.sql.DataSource;
import java.util.List;

public class CustomerService implements AutoCloseable {
    private final CustomerRepository customerRepo;

    public CustomerService(DataSource dataSource) {
        this.customerRepo = new CustomerRepository(dataSource);
    }

    public List<Customer> listAll() throws Exception {
        return customerRepo.listAll();
    }

    public Customer findById(int id) throws Exception {
        return customerRepo.findById(id);
    }

    public void add(Customer customer) throws Exception {
        customerRepo.insert(customer);
    }

    public void update(Customer customer) throws Exception {
        customerRepo.update(customer);
    }

    public boolean delete(int id) throws Exception {
        return customerRepo.delete(id);
    }

    @Override
    public void close() {
    }
}
