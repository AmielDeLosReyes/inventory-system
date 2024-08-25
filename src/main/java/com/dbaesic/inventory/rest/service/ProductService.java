package com.dbaesic.inventory.rest.service;

import com.dbaesic.inventory.rest.model.Product;
import com.dbaesic.inventory.rest.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getActiveProducts() {
        return productRepository.findAll().stream()
                .filter(product -> "1".equals(product.getStatusCode())) // Filter products with status_code = 1
                .collect(Collectors.toList());
    }

    public Product getProductByName(String name) {
        return productRepository.findAll().stream()
                .filter(product -> product.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public void saveProduct(Product product) {
        // Set default values
        if (product.getStatusCode() == null) {
            product.setStatusCode("1"); // Default status code
        }

        // Set dates as strings
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(formatter);

        if (product.getAddedBy() == null) {
            product.setAddedBy("admin"); // Default user or get from authentication context
        }
        if (product.getAddedDate() == null) {
            product.setAddedDate(now);
        }
        if (product.getModifiedBy() == null) {
            product.setModifiedBy("admin"); // Default user or get from authentication context
        }
        if (product.getModifiedDate() == null) {
            product.setModifiedDate(now);
        }

        // Save the product to the database
        productRepository.save(product);
    }

    public void markProductAsDeleted(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            product.setStatusCode("0");  // Set status_code to 0
            productRepository.save(product);
        }
    }
}

