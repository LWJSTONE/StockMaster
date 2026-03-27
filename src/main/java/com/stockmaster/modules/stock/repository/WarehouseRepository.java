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

    @Query("SELECT w FROM Warehouse w WHERE w.deleted = false AND w.status = 1")
    List<Warehouse> findAllActive();

    @Query("SELECT w FROM Warehouse w WHERE w.deleted = false ORDER BY w.createTime DESC")
    List<Warehouse> findAllOrderByCreateTime();

    @Query("SELECT w FROM Warehouse w WHERE w.deleted = false AND " +
            "(:keyword IS NULL OR w.warehouseCode LIKE %:keyword% OR w.warehouseName LIKE %:keyword%) AND " +
            "(:status IS NULL OR w.status = :status)")
    Page<Warehouse> search(@Param("keyword") String keyword,
                           @Param("status") Integer status,
                           Pageable pageable);

    @Query("SELECT COUNT(w) FROM Warehouse w WHERE w.deleted = false AND w.status = 1")
    Long countActiveWarehouses();
}
