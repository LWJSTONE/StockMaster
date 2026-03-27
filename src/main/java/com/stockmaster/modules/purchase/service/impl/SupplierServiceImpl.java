package com.stockmaster.modules.purchase.service.impl;

import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.exception.BusinessException;
import com.stockmaster.modules.purchase.dto.SupplierDTO;
import com.stockmaster.modules.purchase.dto.SupplierVO;
import com.stockmaster.modules.purchase.entity.Supplier;
import com.stockmaster.modules.purchase.repository.SupplierEvaluationRepository;
import com.stockmaster.modules.purchase.repository.SupplierRepository;
import com.stockmaster.modules.purchase.repository.PurchaseOrderRepository;
import com.stockmaster.modules.purchase.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierEvaluationRepository supplierEvaluationRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    @Override
    public SupplierVO getById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new BusinessException("供应商不存在"));
        return convertToVO(supplier);
    }

    @Override
    public PageResult<SupplierVO> getList(String keyword, Integer status, Integer pageNum, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Supplier> page = supplierRepository.findByConditions(keyword, status, pageRequest);
        
        List<SupplierVO> voList = page.getContent().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageResult.of(voList, page.getTotalElements(), pageNum, pageSize);
    }

    @Override
    public List<SupplierVO> getAllSuppliers() {
        List<Supplier> suppliers = supplierRepository.findAllActive();
        return suppliers.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SupplierVO create(SupplierDTO supplierDTO) {
        if (supplierRepository.existsBySupplierCode(supplierDTO.getSupplierCode())) {
            throw new BusinessException("供应商编码已存在");
        }

        Supplier supplier = convertToEntity(supplierDTO);
        if (supplier.getStatus() == null) {
            supplier.setStatus(1);
        }
        supplier = supplierRepository.save(supplier);
        return convertToVO(supplier);
    }

    @Override
    @Transactional
    public SupplierVO update(Long id, SupplierDTO supplierDTO) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new BusinessException("供应商不存在"));

        if (!supplier.getSupplierCode().equals(supplierDTO.getSupplierCode())) {
            if (supplierRepository.existsBySupplierCode(supplierDTO.getSupplierCode())) {
                throw new BusinessException("供应商编码已存在");
            }
        }

        supplier.setSupplierCode(supplierDTO.getSupplierCode());
        supplier.setSupplierName(supplierDTO.getSupplierName());
        supplier.setContactPerson(supplierDTO.getContactPerson());
        supplier.setContactPhone(supplierDTO.getContactPhone());
        supplier.setEmail(supplierDTO.getEmail());
        supplier.setAddress(supplierDTO.getAddress());
        supplier.setBankName(supplierDTO.getBankName());
        supplier.setBankAccount(supplierDTO.getBankAccount());
        supplier.setTaxNumber(supplierDTO.getTaxNumber());
        supplier.setRating(supplierDTO.getRating());
        supplier.setDescription(supplierDTO.getDescription());

        supplier = supplierRepository.save(supplier);
        return convertToVO(supplier);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new BusinessException("供应商不存在"));
        supplier.setDeleted(true);
        supplierRepository.save(supplier);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new BusinessException("供应商不存在"));
        supplier.setStatus(status);
        supplierRepository.save(supplier);
    }

    private SupplierVO convertToVO(Supplier supplier) {
        SupplierVO vo = new SupplierVO();
        vo.setId(supplier.getId());
        vo.setSupplierCode(supplier.getSupplierCode());
        vo.setSupplierName(supplier.getSupplierName());
        vo.setContactPerson(supplier.getContactPerson());
        vo.setPhone(supplier.getContactPhone());
        vo.setEmail(supplier.getEmail());
        vo.setAddress(supplier.getAddress());
        vo.setBankName(supplier.getBankName());
        vo.setBankAccount(supplier.getBankAccount());
        vo.setTaxNumber(supplier.getTaxNumber());
        vo.setStatus(supplier.getStatus());
        vo.setRating(supplier.getRating());
        vo.setDescription(supplier.getDescription());
        vo.setCreateTime(supplier.getCreateTime());
        vo.setUpdateTime(supplier.getUpdateTime());

        // 计算平均评分
        Double avgScore = supplierEvaluationRepository.getAverageScoreBySupplierId(supplier.getId());
        vo.setAverageScore(avgScore);

        return vo;
    }

    private Supplier convertToEntity(SupplierDTO dto) {
        Supplier entity = new Supplier();
        entity.setId(dto.getId());
        entity.setSupplierCode(dto.getSupplierCode());
        entity.setSupplierName(dto.getSupplierName());
        entity.setContactPerson(dto.getContactPerson());
        entity.setContactPhone(dto.getContactPhone());
        entity.setEmail(dto.getEmail());
        entity.setAddress(dto.getAddress());
        entity.setBankName(dto.getBankName());
        entity.setBankAccount(dto.getBankAccount());
        entity.setTaxNumber(dto.getTaxNumber());
        entity.setStatus(dto.getStatus());
        entity.setRating(dto.getRating());
        entity.setDescription(dto.getDescription());
        return entity;
    }
}
