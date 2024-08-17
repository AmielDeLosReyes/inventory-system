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

    @Query(value = "SELECT entry_date, product_name, description, SUM(quantity) AS total_quantity_day, SUM(out_amount) AS total_out_amount_day " +
            "FROM inventory " +
            "WHERE product_name = :productName " +
            "AND description = :description " +
            "GROUP BY entry_date, product_name, description " +
            "ORDER BY entry_date", nativeQuery = true)
    List<Object[]> findDailyQuantitiesAndAmounts(@Param("productName") String productName, @Param("description") String description);

    @Query(value = "SELECT YEAR(entry_date) AS year, MONTH(entry_date) AS month, product_name, description, " +
            "SUM(quantity) AS total_quantity_month, SUM(out_amount) AS total_out_amount_month " +
            "FROM inventory " +
            "WHERE product_name = :productName " +
            "AND description = :description " +
            "GROUP BY YEAR(entry_date), MONTH(entry_date), product_name, description " +
            "ORDER BY year, month", nativeQuery = true)
    List<Object[]> findMonthlyTotals(@Param("productName") String productName, @Param("description") String description);

    @Query("SELECT DISTINCT i.productName FROM Inventory i")
    List<String> findAllUniqueProductNames();

    List<Inventory> findByProductNameOrderByEntryDateAsc(String productName);
}
