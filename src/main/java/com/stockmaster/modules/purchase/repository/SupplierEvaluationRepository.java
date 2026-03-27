package com.stockmaster.modules.purchase.repository;

import com.stockmaster.modules.purchase.entity.SupplierEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierEvaluationRepository extends JpaRepository<SupplierEvaluation, Long> {

    List<SupplierEvaluation> findBySupplierId(Long supplierId);

    @Query("SELECT e FROM SupplierEvaluation e WHERE e.deleted = false ORDER BY e.createTime DESC")
    List<SupplierEvaluation> findAllOrderByCreateTime();

    @Query("SELECT AVG(e.totalScore) FROM SupplierEvaluation e WHERE e.deleted = false AND e.supplierId = :supplierId")
    Double getAverageScoreBySupplierId(Long supplierId);
}
