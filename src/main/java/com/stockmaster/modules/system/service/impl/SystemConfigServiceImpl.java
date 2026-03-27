package com.stockmaster.modules.system.service.impl;

import com.stockmaster.common.exception.BusinessException;
import com.stockmaster.modules.system.dto.SystemConfigDTO;
import com.stockmaster.modules.system.entity.SystemConfig;
import com.stockmaster.modules.system.repository.SystemConfigRepository;
import com.stockmaster.modules.system.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    @Override
    public SystemConfig getById(Long id) {
        return systemConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException("配置不存在"));
    }

    @Override
    public SystemConfig getByKey(String configKey) {
        return systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new BusinessException("配置不存在"));
    }

    @Override
    public SystemConfig getConfigByKey(String key) {
        return systemConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new BusinessException("配置不存在: " + key));
    }

    @Override
    public List<SystemConfig> getByGroup(String configGroup) {
        return systemConfigRepository.findByConfigGroup(configGroup);
    }

    @Override
    public List<SystemConfig> getConfigsByGroupName(String groupName) {
        return systemConfigRepository.findByConfigGroup(groupName);
    }

    @Override
    public List<SystemConfig> getAllConfigs() {
        return systemConfigRepository.findAll();
    }

    @Override
    public Map<String, List<SystemConfig>> getConfigsByGroup() {
        List<SystemConfig> allConfigs = systemConfigRepository.findAll();
        return allConfigs.stream()
                .collect(Collectors.groupingBy(
                        config -> config.getConfigGroup() != null ? config.getConfigGroup() : "default"
                ));
    }

    @Override
    @Transactional
    public SystemConfig create(SystemConfigDTO configDTO) {
        if (systemConfigRepository.existsByConfigKey(configDTO.getConfigKey())) {
            throw new BusinessException("配置键已存在");
        }

        SystemConfig config = convertToEntity(configDTO);
        return systemConfigRepository.save(config);
    }

    @Override
    @Transactional
    public SystemConfig createConfig(SystemConfigDTO dto) {
        if (systemConfigRepository.existsByConfigKey(dto.getConfigKey())) {
            throw new BusinessException("配置键已存在");
        }

        SystemConfig config = convertToEntity(dto);
        return systemConfigRepository.save(config);
    }

    @Override
    @Transactional
    public SystemConfig update(Long id, SystemConfigDTO configDTO) {
        SystemConfig config = systemConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException("配置不存在"));

        // 如果修改了配置键，检查新键是否已存在
        if (configDTO.getConfigKey() != null && !configDTO.getConfigKey().equals(config.getConfigKey())) {
            if (systemConfigRepository.existsByConfigKey(configDTO.getConfigKey())) {
                throw new BusinessException("配置键已存在");
            }
        }

        updateEntityFromDTO(config, configDTO);
        return systemConfigRepository.save(config);
    }

    @Override
    @Transactional
    public SystemConfig updateConfig(String key, SystemConfigDTO dto) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new BusinessException("配置不存在: " + key));

        updateEntityFromDTO(config, dto);
        return systemConfigRepository.save(config);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SystemConfig config = systemConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException("配置不存在"));

        if (Boolean.TRUE.equals(config.getIsSystem())) {
            throw new BusinessException("系统配置不能删除");
        }

        systemConfigRepository.delete(config);
    }

    @Override
    @Transactional
    public void deleteConfig(String key) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new BusinessException("配置不存在: " + key));

        if (Boolean.TRUE.equals(config.getIsSystem())) {
            throw new BusinessException("系统配置不能删除");
        }

        systemConfigRepository.delete(config);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        SystemConfig config = systemConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException("配置不存在"));
        config.setIsEnabled(status == 1);
        systemConfigRepository.save(config);
    }

    private void updateEntityFromDTO(SystemConfig config, SystemConfigDTO dto) {
        if (dto.getConfigKey() != null) {
            config.setConfigKey(dto.getConfigKey());
        }
        if (dto.getConfigValue() != null) {
            config.setConfigValue(dto.getConfigValue());
        }
        if (dto.getConfigName() != null) {
            config.setConfigName(dto.getConfigName());
        }
        if (dto.getDescription() != null) {
            config.setDescription(dto.getDescription());
        }
        if (dto.getConfigType() != null) {
            config.setConfigType(dto.getConfigType());
        }
        if (dto.getConfigGroup() != null) {
            config.setConfigGroup(dto.getConfigGroup());
        }
        if (dto.getSortOrder() != null) {
            config.setSortOrder(dto.getSortOrder());
        }
        if (dto.getIsEnabled() != null) {
            config.setIsEnabled(dto.getIsEnabled());
        }
    }

    private SystemConfig convertToEntity(SystemConfigDTO dto) {
        SystemConfig config = new SystemConfig();
        config.setId(dto.getId());
        config.setConfigKey(dto.getConfigKey());
        config.setConfigValue(dto.getConfigValue());
        config.setConfigName(dto.getConfigName());
        config.setDescription(dto.getDescription());
        config.setConfigType(dto.getConfigType() != null ? dto.getConfigType() : "STRING");
        config.setConfigGroup(dto.getConfigGroup() != null ? dto.getConfigGroup() : "default");
        config.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        config.setIsSystem(dto.getIsSystem() != null ? dto.getIsSystem() : false);
        config.setIsEnabled(dto.getIsEnabled() != null ? dto.getIsEnabled() : true);
        return config;
    }
}
