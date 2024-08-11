package com.dbaesic.inventory.rest.repository;


import com.dbaesic.inventory.rest.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByProductName(String productName);

    Inventory findTopByProductNameOrderByEntryDateDescIdDesc(String productName);
    @Query(value = "SELECT * FROM inventory WHERE product_name = :productName ORDER BY entry_date DESC, id DESC LIMIT 1", nativeQuery = true)
    Inventory findLatestEntryByProductName(@Param("productName") String productName);
}
