package com.stockmaster.modules.system.service;

import com.stockmaster.modules.system.dto.SystemConfigDTO;
import com.stockmaster.modules.system.entity.SystemConfig;

import java.util.List;

public interface SystemConfigService {

    SystemConfig getById(Long id);

    SystemConfig getByKey(String configKey);

    List<SystemConfig> getByGroup(String configGroup);

    List<SystemConfig> getAllConfigs();

    SystemConfig create(SystemConfigDTO configDTO);

    SystemConfig update(Long id, SystemConfigDTO configDTO);

    void delete(Long id);

    void updateStatus(Long id, Integer status);
}
