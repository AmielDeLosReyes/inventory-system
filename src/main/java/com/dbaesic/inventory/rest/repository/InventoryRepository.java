package com.dbaesic.inventory.rest.repository;

import com.dbaesic.inventory.rest.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    @Query("SELECT i FROM Inventory i WHERE i.productName = :productName ORDER BY i.entryDate ASC")
    List<Inventory> findByProductName(String productName);


    @Query("SELECT DISTINCT i.productName FROM Inventory i")
    List<String> findAllUniqueProductNames();

    List<Inventory> findByProductNameOrderByEntryDateAsc(String productName);

    @Query("SELECT i FROM Inventory i WHERE i.outAmount > 0")
    List<Inventory> findAllSales();

    // InventoryRepository.java
    @Query("SELECT i FROM Inventory i WHERE i.productName = :productName AND FUNCTION('DATE_FORMAT', i.entryDate, '%Y-%m') = :month ORDER BY i.entryDate ASC")
    List<Inventory> findByProductNameAndMonth(@Param("productName") String productName, @Param("month") String month);

    @Query("SELECT DISTINCT FUNCTION('DATE_FORMAT', i.entryDate, '%Y-%m') FROM Inventory i")
    List<String> findDistinctMonths();

}
