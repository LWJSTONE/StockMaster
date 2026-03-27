package com.stockmaster.modules.stock.service;

import com.stockmaster.common.dto.PageResult;
import com.stockmaster.modules.stock.dto.OutboundDTO;
import com.stockmaster.modules.stock.entity.Outbound;

import java.util.List;

public interface OutboundService {

    Outbound getById(Long id);

    PageResult<Outbound> getList(String keyword, Long productId, Integer pageNum, Integer pageSize);

    Outbound create(OutboundDTO outboundDTO);

    Outbound update(Long id, OutboundDTO outboundDTO);

    void delete(Long id);

    void batchDelete(List<Long> ids);

    String generateOutboundNo();
}
