package com.stockmaster.modules.purchase.service.impl;

import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.exception.BusinessException;
import com.stockmaster.modules.purchase.dto.SupplierEvaluationDTO;
import com.stockmaster.modules.purchase.entity.Supplier;
import com.stockmaster.modules.purchase.entity.SupplierEvaluation;
import com.stockmaster.modules.purchase.repository.SupplierEvaluationRepository;
import com.stockmaster.modules.purchase.repository.SupplierRepository;
import com.stockmaster.modules.purchase.service.SupplierEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierEvaluationServiceImpl implements SupplierEvaluationService {

    private final SupplierEvaluationRepository evaluationRepository;
    private final SupplierRepository supplierRepository;

    @Override
    public PageResult<SupplierEvaluation> getList(Long supplierId, Integer pageNum, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        
        Page<SupplierEvaluation> page;
        if (supplierId != null) {
            page = evaluationRepository.findBySupplierIdAndDeletedFalse(supplierId, pageRequest);
        } else {
            page = evaluationRepository.findAll(pageRequest);
        }
        
        return PageResult.of(page.getContent(), page.getTotalElements(), pageNum, pageSize);
    }

    @Override
    public SupplierEvaluation getById(Long id) {
        return evaluationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("评价记录不存在"));
    }

    @Override
    public List<SupplierEvaluation> getBySupplierId(Long supplierId) {
        return evaluationRepository.findBySupplierIdAndDeletedFalse(supplierId);
    }

    @Override
    @Transactional
    public SupplierEvaluation create(SupplierEvaluationDTO dto) {
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new BusinessException("供应商不存在"));

        SupplierEvaluation evaluation = new SupplierEvaluation();
        evaluation.setSupplierId(dto.getSupplierId());
        evaluation.setOrderId(dto.getOrderId());
        evaluation.setQualityScore(dto.getQualityScore());
        evaluation.setDeliveryScore(dto.getDeliveryScore());
        evaluation.setServiceScore(dto.getServiceScore());
        evaluation.setPriceScore(dto.getPriceScore());
        evaluation.setContent(dto.getContent());
        
        // 计算总分
        BigDecimal totalScore = calculateTotalScore(dto.getQualityScore(), dto.getDeliveryScore(), 
                dto.getServiceScore(), dto.getPriceScore());
        evaluation.setTotalScore(totalScore);

        evaluation = evaluationRepository.save(evaluation);

        // 更新供应商评分
        updateSupplierRating(supplier.getId());

        return evaluation;
    }

    @Override
    @Transactional
    public SupplierEvaluation update(Long id, SupplierEvaluationDTO dto) {
        SupplierEvaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("评价记录不存在"));

        evaluation.setQualityScore(dto.getQualityScore());
        evaluation.setDeliveryScore(dto.getDeliveryScore());
        evaluation.setServiceScore(dto.getServiceScore());
        evaluation.setPriceScore(dto.getPriceScore());
        evaluation.setContent(dto.getContent());

        // 重新计算总分
        BigDecimal totalScore = calculateTotalScore(dto.getQualityScore(), dto.getDeliveryScore(), 
                dto.getServiceScore(), dto.getPriceScore());
        evaluation.setTotalScore(totalScore);

        evaluation = evaluationRepository.save(evaluation);

        // 更新供应商评分
        updateSupplierRating(evaluation.getSupplierId());

        return evaluation;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SupplierEvaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("评价记录不存在"));
        
        Long supplierId = evaluation.getSupplierId();
        evaluation.setDeleted(true);
        evaluationRepository.save(evaluation);

        // 更新供应商评分
        updateSupplierRating(supplierId);
    }

    private BigDecimal calculateTotalScore(Integer quality, Integer delivery, Integer service, Integer price) {
        int count = 0;
        int sum = 0;
        
        if (quality != null) { sum += quality; count++; }
        if (delivery != null) { sum += delivery; count++; }
        if (service != null) { sum += service; count++; }
        if (price != null) { sum += price; count++; }

        if (count == 0) {
            return BigDecimal.ZERO;
        }
        
        return BigDecimal.valueOf(sum)
                .divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private void updateSupplierRating(Long supplierId) {
        Double avgScore = evaluationRepository.getAverageScoreBySupplierId(supplierId);
        
        Supplier supplier = supplierRepository.findById(supplierId).orElse(null);
        if (supplier != null) {
            supplier.setRating(avgScore != null ? BigDecimal.valueOf(avgScore) : null);
            supplierRepository.save(supplier);
        }
    }
}
