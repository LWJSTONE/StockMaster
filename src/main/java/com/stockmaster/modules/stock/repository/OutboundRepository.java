package com.stockmaster.modules.stock.repository;

import com.stockmaster.modules.stock.entity.Outbound;
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
public interface OutboundRepository extends JpaRepository<Outbound, Long> {

    Optional<Outbound> findByOutboundNo(String outboundNo);

    List<Outbound> findByProductId(Long productId);

    @Query("SELECT o FROM Outbound o WHERE o.deleted = false AND " +
            "(:keyword IS NULL OR o.outboundNo LIKE %:keyword% OR o.batchNo LIKE %:keyword%) AND " +
            "(:productId IS NULL OR o.productId = :productId)")
    Page<Outbound> findByConditions(@Param("keyword") String keyword,
                                     @Param("productId") Long productId,
                                     Pageable pageable);

    @Query("SELECT o FROM Outbound o WHERE o.deleted = false ORDER BY o.createTime DESC")
    List<Outbound> findAllOrderByCreateTime();

    @Query("SELECT SUM(o.totalPrice) FROM Outbound o WHERE o.deleted = false AND o.createTime BETWEEN :startTime AND :endTime")
    Double sumTotalPriceBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(o) FROM Outbound o WHERE o.deleted = false AND o.createTime BETWEEN :startTime AND :endTime")
    Long countByTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT FUNCTION('DATE', o.createTime) as date, COUNT(o) as count FROM Outbound o " +
           "WHERE o.deleted = false AND o.createTime BETWEEN :startTime AND :endTime " +
           "GROUP BY FUNCTION('DATE', o.createTime) ORDER BY FUNCTION('DATE', o.createTime)")
    List<Object[]> getDailyStats(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
