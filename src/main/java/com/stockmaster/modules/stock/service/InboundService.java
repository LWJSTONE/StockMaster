package com.stockmaster.modules.stock.service;

import com.stockmaster.common.dto.PageResult;
import com.stockmaster.modules.stock.dto.InboundDTO;
import com.stockmaster.modules.stock.entity.Inbound;

import java.util.List;

public interface InboundService {

    Inbound getById(Long id);

    PageResult<Inbound> getList(String keyword, Long productId, Long supplierId, Integer pageNum, Integer pageSize);

    Inbound create(InboundDTO inboundDTO);

    Inbound update(Long id, InboundDTO inboundDTO);

    void delete(Long id);

    void batchDelete(List<Long> ids);

    String generateInboundNo();
}
