package com.warehousewms.service;

import com.warehousewms.model.Supplier;
import com.warehousewms.repository.SupplierRepository;

import javax.sql.DataSource;
import java.util.List;

public class SupplierService implements AutoCloseable {
    private final SupplierRepository supplierRepo;

    public SupplierService(DataSource dataSource) {
        this.supplierRepo = new SupplierRepository(dataSource);
    }

    public List<Supplier> listAll() throws Exception {
        return supplierRepo.listAll();
    }

    public Supplier findById(int id) throws Exception {
        return supplierRepo.findById(id);
    }

    public void add(Supplier supplier) throws Exception {
        supplierRepo.insert(supplier);
    }

    public void update(Supplier supplier) throws Exception {
        supplierRepo.update(supplier);
    }

    public boolean delete(int id) throws Exception {
        return supplierRepo.delete(id);
    }

    @Override
    public void close() {
    }
}
