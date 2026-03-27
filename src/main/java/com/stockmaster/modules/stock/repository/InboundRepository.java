package com.stockmaster.modules.stock.repository;

import com.stockmaster.modules.stock.entity.Inbound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InboundRepository extends JpaRepository<Inbound, Long> {

    Optional<Inbound> findByInboundNo(String inboundNo);

    List<Inbound> findByProductId(Long productId);

    List<Inbound> findBySupplierId(Long supplierId);

    @Query("SELECT i FROM Inbound i WHERE i.deleted = false AND " +
            "(:keyword IS NULL OR i.inboundNo LIKE %:keyword% OR i.batchNo LIKE %:keyword%) AND " +
            "(:productId IS NULL OR i.productId = :productId) AND " +
            "(:supplierId IS NULL OR i.supplierId = :supplierId)")
    Page<Inbound> findByConditions(@Param("keyword") String keyword,
                                    @Param("productId") Long productId,
                                    @Param("supplierId") Long supplierId,
                                    Pageable pageable);

    @Query("SELECT i FROM Inbound i WHERE i.deleted = false ORDER BY i.createTime DESC")
    List<Inbound> findAllOrderByCreateTime();
}
