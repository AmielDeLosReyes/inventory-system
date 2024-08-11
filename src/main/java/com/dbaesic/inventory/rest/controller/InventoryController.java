package com.dbaesic.inventory.rest.controller;

import com.dbaesic.inventory.rest.model.Inventory;
import com.dbaesic.inventory.rest.model.Product;
import com.dbaesic.inventory.rest.service.InventoryService;
import com.dbaesic.inventory.rest.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
    public ResponseEntity<?> addEntry(@RequestBody Inventory inventory) {
        log.info("===== Inside addEntry() =====" + getClass());

        // Log the received inventory details
        log.info("Received Inventory Entry: " +
                "ProductName='" + inventory.getProductName() + "', " +
                "EntryDate='" + inventory.getEntryDate() + "', " +
                "Description='" + inventory.getDescription() + "', " +
                "Cost='" + inventory.getCost() + "', " +
                "Quantity='" + inventory.getQuantity() + "', " +
                "InAmount='" + inventory.getInAmount() + "', " +
                "OutAmount='" + inventory.getOutAmount() + "'");

        // Retrieve the product details
        Product product = productService.getProductByName(inventory.getProductName());
        if (product != null) {
            inventory.setCost(product.getPrice());
            log.info("Product price: " + product.getPrice());
        }

        // Calculate inAmount and outAmount based on description
        if ("Purchase of Inventory".equals(inventory.getDescription())) {
            log.info("Invoking Purchase of Inventory...");
            inventory.setInAmount(inventory.getCost().multiply(BigDecimal.valueOf(inventory.getQuantity())));
        } else if ("Sale of Merchandise".equals(inventory.getDescription())) {
            log.info("Invoking Sale of Merchandise...");
            inventory.setOutAmount(inventory.getCost().multiply(BigDecimal.valueOf(inventory.getQuantity())));
        }

        // Save inventory entry and update balance
        log.info("Saving entry to database...");
        inventoryService.updateBalance(
                inventory.getProductName(),
                inventory.getInAmount() != null ? inventory.getInAmount() : BigDecimal.ZERO,
                inventory.getOutAmount() != null ? inventory.getOutAmount() : BigDecimal.ZERO,
                "Purchase of Inventory".equals(inventory.getDescription())
        );

        // Return JSON response
        return ResponseEntity.ok().body("{\"message\":\"Entry saved successfully\"}");
    }

    @GetMapping("/latest-balance/{productName}")
    public ResponseEntity<BigDecimal> getLatestBalance(@PathVariable String productName) {
        BigDecimal latestBalance = inventoryService.getLatestBalance(productName);
        return ResponseEntity.ok(latestBalance);
    }
}


