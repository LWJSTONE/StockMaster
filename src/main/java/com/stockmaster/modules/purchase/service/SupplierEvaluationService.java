package com.stockmaster.modules.purchase.service;

import com.stockmaster.modules.purchase.dto.SupplierEvaluationDTO;
import com.stockmaster.modules.purchase.entity.SupplierEvaluation;

import java.util.List;

public interface SupplierEvaluationService {

    SupplierEvaluation getById(Long id);

    List<SupplierEvaluation> getBySupplierId(Long supplierId);

    SupplierEvaluation create(SupplierEvaluationDTO dto);

    SupplierEvaluation update(Long id, SupplierEvaluationDTO dto);

    void delete(Long id);
}
