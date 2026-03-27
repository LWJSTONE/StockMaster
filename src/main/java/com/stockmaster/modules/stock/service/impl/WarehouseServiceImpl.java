package com.stockmaster.modules.stock.service.impl;

import com.stockmaster.common.dto.PageResult;
import com.stockmaster.common.exception.BusinessException;
import com.stockmaster.modules.stock.entity.Warehouse;
import com.stockmaster.modules.stock.repository.WarehouseRepository;
import com.stockmaster.modules.stock.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @Override
    public PageResult<Warehouse> getList(String keyword, Integer status, Integer pageNum, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Warehouse> page = warehouseRepository.search(keyword, status, pageRequest);

        return PageResult.of(page.getContent(), page.getTotalElements(), pageNum, pageSize);
    }

    @Override
    public Warehouse getById(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("仓库不存在"));
    }

    @Override
    public Warehouse getByCode(String code) {
        return warehouseRepository.findByWarehouseCode(code)
                .orElseThrow(() -> new BusinessException("仓库不存在"));
    }

    @Override
    public List<Warehouse> getAll() {
        return warehouseRepository.findAllOrderByCreateTime();
    }

    @Override
    public List<Warehouse> getAllActive() {
        return warehouseRepository.findAllActive();
    }

    @Override
    @Transactional
    public Warehouse create(Warehouse warehouse) {
        // 检查编码是否已存在
        if (warehouseRepository.existsByWarehouseCode(warehouse.getWarehouseCode())) {
            throw new BusinessException("仓库编码已存在");
        }

        // 设置默认状态
        if (warehouse.getStatus() == null) {
            warehouse.setStatus(1);
        }

        return warehouseRepository.save(warehouse);
    }

    @Override
    @Transactional
    public Warehouse update(Long id, Warehouse warehouse) {
        Warehouse existingWarehouse = getById(id);

        // 检查编码是否被其他仓库使用
        if (!existingWarehouse.getWarehouseCode().equals(warehouse.getWarehouseCode())) {
            if (warehouseRepository.existsByWarehouseCode(warehouse.getWarehouseCode())) {
                throw new BusinessException("仓库编码已存在");
            }
        }

        existingWarehouse.setWarehouseCode(warehouse.getWarehouseCode());
        existingWarehouse.setWarehouseName(warehouse.getWarehouseName());
        existingWarehouse.setAddress(warehouse.getAddress());
        existingWarehouse.setContactPerson(warehouse.getContactPerson());
        existingWarehouse.setContactPhone(warehouse.getContactPhone());
        existingWarehouse.setCapacity(warehouse.getCapacity());
        existingWarehouse.setDescription(warehouse.getDescription());

        return warehouseRepository.save(existingWarehouse);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Warehouse warehouse = getById(id);
        warehouse.setDeleted(true);
        warehouseRepository.save(warehouse);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        Warehouse warehouse = getById(id);
        warehouse.setStatus(status);
        warehouseRepository.save(warehouse);
    }

    @Override
    public Long countActiveWarehouses() {
        return warehouseRepository.countActiveWarehouses();
    }
}
