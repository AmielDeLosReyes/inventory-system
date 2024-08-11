package com.dbaesic.inventory.rest.service;

import com.dbaesic.inventory.rest.model.Inventory;
import com.dbaesic.inventory.rest.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

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
        List<Inventory> entries = inventoryRepository.findAll();
        BigDecimal balance = BigDecimal.ZERO;

        for (Inventory entry : entries) {
            if (entry.getProductName().equals(productName)) {
                if (isIn) {
                    balance = balance.add(entry.getInAmount() != null ? entry.getInAmount() : BigDecimal.ZERO);
                } else {
                    balance = balance.subtract(entry.getOutAmount() != null ? entry.getOutAmount() : BigDecimal.ZERO);
                }
            }
        }

        // Update balance for each entry
        BigDecimal finalBalance = balance;
        entries.forEach(entry -> {
            if (entry.getProductName().equals(productName)) {
                entry.setBalance(finalBalance);
                inventoryRepository.save(entry);
            }
        });
    }
}

