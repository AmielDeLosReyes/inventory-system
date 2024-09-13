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

    public void updateBalance(String productName, BigDecimal inAmount, BigDecimal outAmount, boolean isIn, String entryDate, BigDecimal cost, int quantity, boolean isDamaged, boolean isInitialInventory) {
        // Retrieve all entries for the given product
        List<Inventory> entries = inventoryRepository.findByProductNameOrderByEntryDateAsc(productName);

        BigDecimal newBalance = BigDecimal.ZERO;

        // Calculate the current balance
        for (Inventory entry : entries) {
            if ("Purchase of Inventory".equals(entry.getDescription()) || "Initial Inventory".equals(entry.getDescription())) {
                newBalance = newBalance.add(entry.getInAmount());
            } else if ("Sale of Merchandise".equals(entry.getDescription()) || "Damaged Goods".equals(entry.getDescription())) {
                newBalance = newBalance.subtract(entry.getOutAmount());
            }
        }

        // Check if the sale or damage amount exceeds the available balance
        if (!isIn && !isInitialInventory && outAmount.compareTo(newBalance) > 0) {
            throw new IllegalArgumentException("Sale or damaged goods amount exceeds available stock.");
        }

        // Create a new inventory entry
        Inventory newEntry = new Inventory();
        newEntry.setProductName(productName);
        newEntry.setEntryDate(entryDate); // Set the entry date

        if (isInitialInventory) {
            newEntry.setDescription("Initial Inventory");
        } else {
            newEntry.setDescription(isIn ? "Purchase of Inventory" : (isDamaged ? "Damaged Goods" : "Sale of Merchandise"));
        }

        newEntry.setCost(cost); // Set the cost if applicable
        newEntry.setQuantity(quantity); // Set the quantity

        // Update the inAmount and outAmount based on the type of entry
        if (isIn || isInitialInventory) {
            newEntry.setInAmount(inAmount);
            newEntry.setOutAmount(BigDecimal.ZERO);
            newBalance = newBalance.add(inAmount);
        } else {
            newEntry.setInAmount(BigDecimal.ZERO);
            newEntry.setOutAmount(outAmount);
            newBalance = newBalance.subtract(outAmount);
        }

        // Set the new balance
        newEntry.setBalance(newBalance);

        // Save the new entry
        inventoryRepository.save(newEntry);
    }




    public BigDecimal getLatestBalance(String productName) {
        List<Inventory> entries = inventoryRepository.findByProductName(productName);

        // Calculate the balance by iterating through entries
        BigDecimal balance = BigDecimal.ZERO;
        for (Inventory entry : entries) {
            if ("Purchase of Inventory".equals(entry.getDescription()) || "Initial Inventory".equals(entry.getDescription())) {
                balance = balance.add(entry.getInAmount());
            } else if ("Sale of Merchandise".equals(entry.getDescription()) || "Damaged Goods".equals(entry.getDescription())) {
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


