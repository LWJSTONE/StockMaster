package com.stockmaster.modules.purchase.repository;

import com.stockmaster.modules.purchase.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findBySupplierCode(String supplierCode);

    boolean existsBySupplierCode(String supplierCode);

    @Query("SELECT s FROM Supplier s WHERE s.deleted = false AND " +
            "(:keyword IS NULL OR s.supplierCode LIKE %:keyword% OR s.supplierName LIKE %:keyword% OR s.contactPerson LIKE %:keyword%) AND " +
            "(:status IS NULL OR s.status = :status)")
    Page<Supplier> findByConditions(@Param("keyword") String keyword,
                                     @Param("status") Integer status,
                                     Pageable pageable);

    @Query("SELECT s FROM Supplier s WHERE s.deleted = false AND s.status = 1")
    List<Supplier> findAllActive();

    @Query("SELECT s FROM Supplier s WHERE s.deleted = false ORDER BY s.createTime DESC")
    List<Supplier> findAllOrderByCreateTime();
}
