package com.stockmaster.modules.stock.repository;

import com.stockmaster.modules.stock.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    List<Inventory> findByWarehouseCode(String warehouseCode);

    @Query("SELECT i FROM Inventory i JOIN Product p ON i.productId = p.id WHERE i.deleted = false AND (:keyword IS NULL OR p.productName LIKE %:keyword% OR p.productCode LIKE %:keyword%)")
    Page<Inventory> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT i FROM Inventory i WHERE i.deleted = false AND i.warningMin IS NOT NULL AND i.quantity <= i.warningMin")
    List<Inventory> findLowStock();

    @Query("SELECT i FROM Inventory i WHERE i.deleted = false AND i.warningMax IS NOT NULL AND i.quantity >= i.warningMax")
    List<Inventory> findOverStock();

    @Modifying
    @Query("UPDATE Inventory i SET i.quantity = i.quantity + :quantity, i.availableQuantity = i.availableQuantity + :quantity WHERE i.productId = :productId")
    void addStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE Inventory i SET i.quantity = i.quantity - :quantity, i.availableQuantity = i.availableQuantity - :quantity WHERE i.productId = :productId AND i.quantity >= :quantity")
    int reduceStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Query("SELECT SUM(i.quantity) FROM Inventory i WHERE i.deleted = false")
    Long getTotalStockQuantity();

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.deleted = false AND i.warningMin IS NOT NULL AND i.quantity <= i.warningMin")
    Long countLowStockProducts();

    @Query("SELECT i FROM Inventory i WHERE i.deleted = false")
    List<Inventory> findAllNotDeleted();

    @Query("SELECT SUM(i.quantity * p.costPrice) FROM Inventory i JOIN Product p ON i.productId = p.id WHERE i.deleted = false AND i.quantity > 0")
    BigDecimal getTotalInventoryValue();
}
