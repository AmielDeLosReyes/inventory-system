package com.dbaesic.inventory.rest.service;

import com.dbaesic.inventory.rest.model.Product;
import com.dbaesic.inventory.rest.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductByName(String name) {
        return productRepository.findAll().stream()
                .filter(product -> product.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public void saveProduct(Product product) {
        productRepository.save(product);
    }
}

