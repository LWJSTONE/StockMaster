package com.stockmaster.modules.purchase.service;

import com.stockmaster.common.dto.PageResult;
import com.stockmaster.modules.purchase.dto.SupplierDTO;
import com.stockmaster.modules.purchase.dto.SupplierVO;

import java.util.List;

public interface SupplierService {

    SupplierVO getById(Long id);

    PageResult<SupplierVO> getList(String keyword, Integer status, Integer pageNum, Integer pageSize);

    List<SupplierVO> getAllSuppliers();

    SupplierVO create(SupplierDTO supplierDTO);

    SupplierVO update(Long id, SupplierDTO supplierDTO);

    void delete(Long id);

    void updateStatus(Long id, Integer status);
}
