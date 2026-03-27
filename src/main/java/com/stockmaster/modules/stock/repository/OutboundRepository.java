package com.stockmaster.modules.stock.repository;

import com.stockmaster.modules.stock.entity.Outbound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
