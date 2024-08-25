package com.dbaesic.inventory.rest.controller;

import com.dbaesic.inventory.rest.model.Product;
import com.dbaesic.inventory.rest.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
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

    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable("id") Long id, Model model) {
        Product product = productService.findById(id);
        if (product == null) {
            return "redirect:/product/list-products";
        }
        model.addAttribute("product", product);
        return "edit-product"; // This should match your Thymeleaf template name
    }


    @PostMapping("/product/update")
    public String updateProduct(@RequestParam("id") Long id,
                                @RequestParam("name") String name,
                                @RequestParam("price") Double price,
                                RedirectAttributes redirectAttributes) {
        Product product = productService.findById(id);
        if (product == null) {
            // Handle case where product is not found
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found.");
            return "redirect:/product/list-products";
        }

        product.setName(name);
        product.setPrice(BigDecimal.valueOf(price));
        productService.saveProduct(product);

        redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully.");
        return "redirect:/product/list-products";
    }


}
