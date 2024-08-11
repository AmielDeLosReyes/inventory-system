package com.dbaesic.inventory.rest.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "entry_date")
    private String entryDate;

    @Column(name = "description")
    private String description;

    @Column(name = "cost")
    private BigDecimal cost;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "in_amount")
    private BigDecimal inAmount;

    @Column(name = "out_amount")
    private BigDecimal outAmount;

    @Column(name = "balance")
    private BigDecimal balance;

    @Column(name = "remarks")
    private String remarks;

    // Getters and setters
}
