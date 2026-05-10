package com.warehousewms.service;

import com.warehousewms.model.Bin;
import com.warehousewms.repository.BinRepository;

import javax.sql.DataSource;
import java.util.List;

public class BinService implements AutoCloseable {
    private final BinRepository binRepo;

    public BinService(DataSource dataSource) {
        this.binRepo = new BinRepository(dataSource);
    }

    public List<Bin> findRootBins() throws Exception {
        return binRepo.findRootBins();
    }

    public List<Bin> findChildren(int parentBinId) throws Exception {
        return binRepo.findChildren(parentBinId);
    }

    public List<Bin> listAll() throws Exception {
        return binRepo.listAll();
    }

    public Bin findById(int id) throws Exception {
        return binRepo.findById(id);
    }

    public void add(Bin bin) throws Exception {
        binRepo.insert(bin);
    }

    public void update(Bin bin) throws Exception {
        binRepo.update(bin);
    }

    public boolean delete(int id) throws Exception {
        return binRepo.delete(id);
    }

    @Override
    public void close() {
    }
}
