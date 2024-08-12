package com.dbaesic.inventory.rest.service;

import com.dbaesic.inventory.rest.model.Inventory;
import com.dbaesic.inventory.rest.repository.InventoryRepository;
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
        // Retrieve the most recent entry for the given product
        Inventory latestEntry = inventoryRepository.findTopByProductNameOrderByEntryDateDescIdDesc(productName);

        // Determine the current balance
        BigDecimal currentBalance = (latestEntry != null) ? latestEntry.getBalance() : BigDecimal.ZERO;

        // Calculate the new balance
        BigDecimal newBalance = currentBalance;
        if (isIn) {
            newBalance = newBalance.add(inAmount);
        } else {
            newBalance = newBalance.subtract(outAmount);
        }

        // Check if we need to create a new entry
        if (latestEntry == null || !newBalance.equals(latestEntry.getBalance())) {
            Inventory newEntry = new Inventory();
            newEntry.setProductName(productName);
            newEntry.setEntryDate(String.valueOf(new java.sql.Date(System.currentTimeMillis()))); // Set current date
            newEntry.setDescription(isIn ? "Purchase of Inventory" : "Sale of Merchandise");
            newEntry.setCost(BigDecimal.ZERO); // Set an appropriate cost if needed
            newEntry.setQuantity(0); // Set the appropriate quantity if needed
            newEntry.setInAmount(isIn ? inAmount : BigDecimal.ZERO);
            newEntry.setOutAmount(isIn ? BigDecimal.ZERO : outAmount);
            newEntry.setBalance(newBalance);

            // Save the new entry
            inventoryRepository.save(newEntry);
        }
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


}


