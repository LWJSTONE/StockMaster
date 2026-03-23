package com.stockmaster.modules.stock.repository;

import com.stockmaster.modules.stock.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findByWarehouseCode(String warehouseCode);

    boolean existsByWarehouseCode(String warehouseCode);

    List<Warehouse> findByStatus(Integer status);

    @Query("SELECT w FROM Warehouse w WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "w.warehouseName LIKE %:keyword% OR " +
           "w.warehouseCode LIKE %:keyword% OR " +
           "w.address LIKE %:keyword%) AND " +
           "(:status IS NULL OR w.status = :status)")
    Page<Warehouse> search(@Param("keyword") String keyword,
                           @Param("status") Integer status,
                           Pageable pageable);

    @Query("SELECT COUNT(w) FROM Warehouse w WHERE w.status = 1")
    Long countActiveWarehouses();

    @Query("SELECT w FROM Warehouse w WHERE w.status = 1 ORDER BY w.warehouseName")
    List<Warehouse> findAllActive();
}
