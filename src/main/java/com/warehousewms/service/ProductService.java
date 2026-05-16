package com.warehousewms.service;

import com.warehousewms.model.Product;
import com.warehousewms.repository.ProductRepository;

import javax.sql.DataSource;
import java.util.List;

public class ProductService implements AutoCloseable {
    private final ProductRepository productRepo;

    public ProductService(DataSource dataSource) {
        this.productRepo = new ProductRepository(dataSource);
    }

    public List<Product> listAll() throws Exception {
        return productRepo.listAll();
    }

    public List<Product> listActive() throws Exception {
        return productRepo.listActive();
    }

    public Product findById(int id) throws Exception {
        return productRepo.findById(id);
    }

    public Product findByBarcode(String barcode) throws Exception {
        return productRepo.findByBarcode(barcode);
    }

    public boolean skuExists(String sku) throws Exception {
        return productRepo.skuExists(sku);
    }

    public void add(Product product) throws Exception {
        productRepo.insert(product);
    }

    public void update(Product product) throws Exception {
        productRepo.update(product);
    }

    public boolean delete(int id) throws Exception {
        return productRepo.delete(id);
    }

    @Override
    public void close() {
    }
}
