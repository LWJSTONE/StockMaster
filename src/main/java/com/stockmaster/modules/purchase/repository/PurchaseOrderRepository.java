package com.stockmaster.modules.purchase.repository;

import com.stockmaster.modules.purchase.entity.PurchaseOrder;
import com.stockmaster.common.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByOrderNo(String orderNo);

    boolean existsByOrderNo(String orderNo);

    List<PurchaseOrder> findBySupplierId(Long supplierId);

    @Query("SELECT p FROM PurchaseOrder p WHERE p.deleted = false AND " +
            "(:keyword IS NULL OR p.orderNo LIKE %:keyword% OR p.remark LIKE %:keyword%) AND " +
            "(:supplierId IS NULL OR p.supplierId = :supplierId) AND " +
            "(:status IS NULL OR p.status = :status)")
    Page<PurchaseOrder> findByConditions(@Param("keyword") String keyword,
                                          @Param("supplierId") Long supplierId,
                                          @Param("status") OrderStatus status,
                                          Pageable pageable);

    @Query("SELECT p FROM PurchaseOrder p WHERE p.deleted = false ORDER BY p.createTime DESC")
    List<PurchaseOrder> findAllOrderByCreateTime();

    @Query("SELECT COUNT(p) FROM PurchaseOrder p WHERE p.deleted = false AND p.createTime BETWEEN :startTime AND :endTime")
    Long countByTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT SUM(p.totalAmount) FROM PurchaseOrder p WHERE p.deleted = false AND p.createTime BETWEEN :startTime AND :endTime")
    Double sumAmountBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(p) FROM PurchaseOrder p WHERE p.deleted = false AND p.status = :status")
    Long countByStatus(@Param("status") OrderStatus status);
}
