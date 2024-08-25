package com.dbaesic.inventory.rest.controller;

import com.dbaesic.inventory.rest.model.Product;
import com.dbaesic.inventory.rest.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String getProducts(@RequestParam(value = "deleted", required = false) boolean deleted, Model model) {
        List<Product> products = productService.getActiveProducts();
        model.addAttribute("products", products);

        if (deleted) {
            model.addAttribute("successMessage", "Product successfully deleted.");
        }

        return "products";
    }


    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        productService.markProductAsDeleted(id);
        redirectAttributes.addFlashAttribute("successMessage", "Product successfully deleted.");
        return "redirect:/product/list-products";
    }

}
