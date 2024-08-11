package com.dbaesic.inventory.rest.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "inventory", schema = "inventory_management")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;
    private LocalDate entryDate;
    private String description;
    private BigDecimal cost;
    private int quantity;
    private BigDecimal inAmount;
    private BigDecimal outAmount;
    private BigDecimal balance;
    private String remarks;

    // Getters and setters
}