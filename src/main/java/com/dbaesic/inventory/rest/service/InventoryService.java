package com.dbaesic.inventory.rest.service;

import com.dbaesic.inventory.rest.model.Inventory;
import com.dbaesic.inventory.rest.repository.InventoryRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public List<Inventory> getAllEntries() {
        return inventoryRepository.findAll();
    }

    public void saveEntry(Inventory inventory) {
        inventoryRepository.save(inventory);
    }

    public void updateBalance(String productName, BigDecimal inAmount, BigDecimal outAmount, boolean isIn) {
        // Retrieve all entries for the given product
        List<Inventory> entries = inventoryRepository.findByProductNameOrderByEntryDateAsc(productName);

        BigDecimal newBalance = BigDecimal.ZERO;

        // Calculate the current balance
        for (Inventory entry : entries) {
            if ("Purchase of Inventory".equals(entry.getDescription())) {
                newBalance = newBalance.add(entry.getInAmount());
            } else if ("Sale of Merchandise".equals(entry.getDescription())) {
                newBalance = newBalance.subtract(entry.getOutAmount());
            }
        }

        // If this is a sale, check if the sale amount exceeds the available balance
        if (!isIn && outAmount.compareTo(newBalance) > 0) {
            throw new IllegalArgumentException("Sale amount exceeds available stock.");
        }

        // Add the new entry to the list and update the balance
        Inventory newEntry = new Inventory();
        newEntry.setProductName(productName);
        newEntry.setEntryDate(String.valueOf(new java.sql.Date(System.currentTimeMillis()))); // Set current date
        newEntry.setDescription(isIn ? "Purchase of Inventory" : "Sale of Merchandise");
        newEntry.setCost(BigDecimal.ZERO); // Set an appropriate cost if needed
        newEntry.setQuantity(0); // Set the appropriate quantity if needed
        newEntry.setInAmount(isIn ? inAmount : BigDecimal.ZERO);
        newEntry.setOutAmount(isIn ? BigDecimal.ZERO : outAmount);

        // Update balance after adding the new entry
        if (isIn) {
            newBalance = newBalance.add(inAmount);
        } else {
            newBalance = newBalance.subtract(outAmount);
        }

        newEntry.setBalance(newBalance);

        // Save the new entry
        inventoryRepository.save(newEntry);
    }



    public BigDecimal getLatestBalance(String productName) {
        List<Inventory> entries = inventoryRepository.findByProductName(productName);

        // Calculate the balance by iterating through entries
        BigDecimal balance = BigDecimal.ZERO;
        for (Inventory entry : entries) {
            if ("Purchase of Inventory".equals(entry.getDescription())) {
                balance = balance.add(entry.getInAmount());
            } else if ("Sale of Merchandise".equals(entry.getDescription())) {
                balance = balance.subtract(entry.getOutAmount());
            }
        }
        return balance;
    }

    public List<Inventory> getAllSales() {
        return inventoryRepository.findAllSales();
    }

    @Transactional
    public void deleteEntryById(Long id) throws Exception {
        // Check if the entry exists
        if (!inventoryRepository.existsById(id)) {
            throw new Exception("Entry with ID " + id + " not found");
        }
        // Delete the entry
        inventoryRepository.deleteById(id);
    }

    public List<Inventory> getEntriesByProductAndMonth(String productName, String month) {
        return inventoryRepository.findByProductNameAndMonth(productName, month);
    }

    public List<Inventory> getEntriesByProductName(String productName) {
        return inventoryRepository.findByProductName(productName);
    }

    public List<String> getAvailableMonths() {
        return inventoryRepository.findDistinctMonths();
    }
}


