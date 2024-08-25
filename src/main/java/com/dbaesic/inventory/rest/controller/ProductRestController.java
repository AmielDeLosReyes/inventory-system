package com.dbaesic.inventory.rest.controller;

import com.dbaesic.inventory.rest.model.Product;
import com.dbaesic.inventory.rest.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductRestController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{name}/cost")
    public BigDecimal getProductCost(@PathVariable String name) {
        Product product = productService.getProductByName(name);
        return (product != null) ? product.getPrice() : BigDecimal.ZERO;
    }
}

