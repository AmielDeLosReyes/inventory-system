package com.dbaesic.inventory.rest.controller;

import com.dbaesic.inventory.rest.model.Product;
import com.dbaesic.inventory.rest.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/add-product")
    public String showAddProductForm() {
        return "add-product"; // Your Thymeleaf template name for the Add Product form
    }

    @PostMapping("/add")
    public String addProduct(Product product) {
        System.out.println("Adding product: " + product);
        productService.saveProduct(product);
        return "redirect:/product/list-products";
    }


    @GetMapping("/list-products")
    public String getProducts(Model model) {
        List<Product> products = productService.getAllProducts(); // Assuming you have a service to fetch products
        model.addAttribute("products", products);
        return "products";
    }
}
