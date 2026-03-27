package com.stockmaster.modules.purchase.repository;

import com.stockmaster.modules.purchase.entity.SupplierEvaluation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierEvaluationRepository extends JpaRepository<SupplierEvaluation, Long> {

    Page<SupplierEvaluation> findBySupplierIdAndDeletedFalse(Long supplierId, Pageable pageable);

    List<SupplierEvaluation> findBySupplierIdAndDeletedFalse(Long supplierId);

    @Query("SELECT e FROM SupplierEvaluation e WHERE e.deleted = false ORDER BY e.createTime DESC")
    List<SupplierEvaluation> findAllOrderByCreateTime();

    @Query("SELECT AVG(e.totalScore) FROM SupplierEvaluation e WHERE e.deleted = false AND e.supplierId = :supplierId")
    Double getAverageScoreBySupplierId(@Param("supplierId") Long supplierId);
}
