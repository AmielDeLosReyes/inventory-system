package com.dbaesic.inventory.rest.controller;

import com.dbaesic.inventory.rest.model.Inventory;
import com.dbaesic.inventory.rest.model.Product;
import com.dbaesic.inventory.rest.service.InventoryService;
import com.dbaesic.inventory.rest.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        // Log the received inventory entry
        log.info("Received Inventory Entry: " +
                "ProductName='" + inventory.getProductName() + "', " +
                "EntryDate='" + inventory.getEntryDate() + "', " +
                "Description='" + inventory.getDescription() + "', " +
                "Cost='" + inventory.getCost() + "', " +
                "Quantity='" + inventory.getQuantity() + "', " +
                "InAmount='" + inventory.getInAmount() + "', " +
                "OutAmount='" + inventory.getOutAmount() + "'");

        // Set EntryDate if not provided
        if (inventory.getEntryDate() == null) {
            inventory.setEntryDate(String.valueOf(LocalDate.now())); // Set current date if EntryDate is not provided
        } else {
            inventory.setEntryDate(inventory.getEntryDate()); // Set current date if EntryDate is not
        }

        Product product = productService.getProductByName(inventory.getProductName());
        if (product != null) {
            inventory.setCost(product.getPrice());
            log.info("Product price: " + product.getPrice());
        }

        if ("Purchase of Inventory".equals(inventory.getDescription())) {
            log.info("Invoking Purchase of Inventory...");
            inventory.setInAmount(inventory.getCost().multiply(BigDecimal.valueOf(inventory.getQuantity())));
            inventory.setOutAmount(BigDecimal.ZERO);
        } else if ("Sale of Merchandise".equals(inventory.getDescription())) {
            log.info("Invoking Sale of Merchandise...");
            inventory.setOutAmount(inventory.getCost().multiply(BigDecimal.valueOf(inventory.getQuantity())));
            inventory.setInAmount(BigDecimal.ZERO);
        }

        inventory.setQuantity(inventory.getQuantity() != 0 ? inventory.getQuantity() : 0);
        inventory.setCost(inventory.getCost() != null ? inventory.getCost() : BigDecimal.ZERO);

        try {
            inventoryService.updateBalance(inventory.getProductName(), inventory.getInAmount(), inventory.getOutAmount(), "Purchase of Inventory".equals(inventory.getDescription()), inventory.getEntryDate());
            return ResponseEntity.ok().body("{\"message\":\"Entry saved successfully\"}");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }



    @GetMapping("/latest-balance")
    public ResponseEntity<Map<String, Object>> getLatestBalance(@RequestParam String productName) {
        BigDecimal latestBalance = inventoryService.getLatestBalance(productName); // Fetch from service
        Map<String, Object> response = new HashMap<>();
        response.put("latestBalance", latestBalance);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable("id") Long id) {
        // Log the ID we are trying to delete
        log.info("Attempting to delete entry with ID: {}", id);

        try {
            inventoryService.deleteEntryById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting entry with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // InventoryController.java
    @GetMapping("/entries")
    public ResponseEntity<List<Inventory>> getEntriesByProductAndMonth(
            @RequestParam String productName,
            @RequestParam(required = false) String month) {
        List<Inventory> entries;
        if ("all".equals(month)) {
            entries = inventoryService.getEntriesByProductName(productName);
        } else {
            entries = inventoryService.getEntriesByProductAndMonth(productName, month);
        }
        return ResponseEntity.ok(entries);
    }


    @GetMapping("/months")
    public ResponseEntity<List<String>> getAvailableMonths() {
        List<String> months = inventoryService.getAvailableMonths();
        return ResponseEntity.ok(months);
    }
}


