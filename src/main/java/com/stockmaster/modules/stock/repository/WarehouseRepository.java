package com.stockmaster.modules.stock.repository;

import com.stockmaster.modules.stock.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findByWarehouseCode(String warehouseCode);

    boolean existsByWarehouseCode(String warehouseCode);

    @Query("SELECT w FROM Warehouse w WHERE w.deleted = false AND w.status = 1")
    List<Warehouse> findAllActive();

    @Query("SELECT w FROM Warehouse w WHERE w.deleted = false")
    List<Warehouse> findAllOrderByCreateTime();
}
