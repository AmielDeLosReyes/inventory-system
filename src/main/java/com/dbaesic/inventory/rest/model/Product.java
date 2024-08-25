package com.dbaesic.inventory.rest.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "products", schema = "inventory_management")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;
    @Column(name = "price")
    private BigDecimal price;
    @Column(name = "status_code")
    private String statusCode;
    @Column(name = "added_by")
    private String addedBy;
    @Column(name = "added_date")
    private String addedDate;
    @Column(name = "modified_by")
    private String modifiedBy;
    @Column(name = "modified_date")
    private String modifiedDate;
}
