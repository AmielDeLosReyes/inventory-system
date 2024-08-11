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
    @Query("SELECT i FROM Inventory i WHERE i.productName = :productName ORDER BY i.entryDate DESC, i.id DESC")
    Inventory findLatestEntry(@Param("productName") String productName);
}
