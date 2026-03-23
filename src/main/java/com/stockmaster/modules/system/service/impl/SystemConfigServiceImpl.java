package com.stockmaster.modules.system.service.impl;

import com.stockmaster.common.exception.BusinessException;
import com.stockmaster.modules.system.dto.SystemConfigDTO;
import com.stockmaster.modules.system.entity.SystemConfig;
import com.stockmaster.modules.system.repository.SystemConfigRepository;
import com.stockmaster.modules.system.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * System configuration service implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    private static final String CACHE_NAME = "system_config";

    @Override
    @Cacheable(value = CACHE_NAME, key = "'all'")
    public List<SystemConfig> getAllConfigs() {
        return systemConfigRepository.findAllOrderByGroupAndSort();
    }

    @Override
    public Map<String, List<SystemConfig>> getConfigsByGroup() {
        List<SystemConfig> configs = getAllConfigs();
        return configs.stream()
                .collect(Collectors.groupingBy(
                        config -> config.getConfigGroup() != null ? config.getConfigGroup() : "default",
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "#key")
    public SystemConfig getConfigByKey(String key) {
        return systemConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new BusinessException("配置项不存在: " + key));
    }

    @Override
    public String getConfigValue(String key) {
        return getConfigValue(key, null);
    }

    @Override
    public String getConfigValue(String key, String defaultValue) {
        try {
            SystemConfig config = systemConfigRepository.findByConfigKey(key)
                    .orElse(null);
            if (config != null && Boolean.TRUE.equals(config.getIsEnabled()) && !Boolean.TRUE.equals(config.getDeleted())) {
                return config.getConfigValue() != null ? config.getConfigValue() : defaultValue;
            }
            return defaultValue;
        } catch (Exception e) {
            log.warn("Failed to get config value for key: {}", key, e);
            return defaultValue;
        }
    }

    @Override
    public Integer getIntConfigValue(String key, Integer defaultValue) {
        String value = getConfigValue(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse integer config value for key: {}, value: {}", key, value);
            return defaultValue;
        }
    }

    @Override
    public Boolean getBooleanConfigValue(String key, Boolean defaultValue) {
        String value = getConfigValue(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value);
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public SystemConfig createConfig(SystemConfigDTO dto) {
        // Check if key already exists
        if (systemConfigRepository.existsByConfigKey(dto.getConfigKey())) {
            throw new BusinessException("配置键已存在: " + dto.getConfigKey());
        }

        SystemConfig config = new SystemConfig();
        config.setConfigKey(dto.getConfigKey());
        config.setConfigValue(dto.getConfigValue());
        config.setConfigName(dto.getConfigName());
        config.setDescription(dto.getDescription());
        config.setConfigType(dto.getConfigType() != null ? dto.getConfigType() : "STRING");
        config.setConfigGroup(dto.getConfigGroup() != null ? dto.getConfigGroup() : "system");
        config.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        config.setIsSystem(dto.getIsSystem() != null ? dto.getIsSystem() : false);
        config.setIsEnabled(dto.getIsEnabled() != null ? dto.getIsEnabled() : true);

        return systemConfigRepository.save(config);
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public SystemConfig updateConfig(String key, SystemConfigDTO dto) {
        SystemConfig config = getConfigByKey(key);

        // Check if trying to modify system config
        if (Boolean.TRUE.equals(config.getIsSystem()) && dto.getConfigKey() != null && !dto.getConfigKey().equals(key)) {
            throw new BusinessException("系统配置不允许修改配置键");
        }

        // Update fields
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

        return systemConfigRepository.save(config);
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void deleteConfig(String key) {
        SystemConfig config = getConfigByKey(key);

        if (Boolean.TRUE.equals(config.getIsSystem())) {
            throw new BusinessException("系统配置不允许删除");
        }

        config.setDeleted(true);
        systemConfigRepository.save(config);
    }

    @Override
    public List<SystemConfig> getConfigsByGroupName(String groupName) {
        return systemConfigRepository.findByConfigGroup(groupName);
    }
}
