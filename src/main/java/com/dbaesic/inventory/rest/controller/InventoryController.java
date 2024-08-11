package com.dbaesic.inventory.rest.controller;

import com.dbaesic.inventory.rest.model.Inventory;
import com.dbaesic.inventory.rest.model.Product;
import com.dbaesic.inventory.rest.service.InventoryService;
import com.dbaesic.inventory.rest.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductService productService;

    @GetMapping
    public String getAllEntries(Model model) {
        // Fetch all inventory entries
        List<Inventory> entries = inventoryService.getAllEntries();

        // Extract distinct product names for the dropdown
        List<String> products = entries.stream()
                .map(Inventory::getProductName)
                .distinct()
                .collect(Collectors.toList());

        // Group entries by product name
        model.addAttribute("entries", entries.stream()
                .collect(Collectors.groupingBy(Inventory::getProductName)));

        // Add products to the model for the dropdown
        model.addAttribute("products", productService.getAllProducts());

        return "inventory";
    }

    @PostMapping("/add")
    public String addEntry(@ModelAttribute Inventory inventory) {
        // Retrieve the product details
        Product product = productService.getProductByName(inventory.getProductName());
        if (product != null) {
            inventory.setCost(product.getPrice());
        }

        // Calculate inAmount and outAmount based on description
        if (inventory.getDescription().equals("Purchase of Inventory")) {
            inventory.setInAmount(inventory.getCost().multiply(BigDecimal.valueOf(inventory.getQuantity())));
            inventory.setOutAmount(BigDecimal.ZERO);
        } else if (inventory.getDescription().equals("Sale of Merchandise")) {
            inventory.setInAmount(BigDecimal.ZERO);
            inventory.setOutAmount(inventory.getCost().multiply(BigDecimal.valueOf(inventory.getQuantity())));
        } else {
            inventory.setInAmount(BigDecimal.ZERO);
            inventory.setOutAmount(BigDecimal.ZERO);
        }

        // Save inventory entry
        inventoryService.saveEntry(inventory);

        // Update balance
        inventoryService.updateBalance(
                inventory.getProductName(),
                inventory.getInAmount(),
                inventory.getOutAmount(),
                inventory.getDescription().equals("Purchase of Inventory")
        );

        return "redirect:/inventory";
    }
}

