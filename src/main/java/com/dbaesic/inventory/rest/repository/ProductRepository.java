package com.dbaesic.inventory.rest.repository;

import com.dbaesic.inventory.rest.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
