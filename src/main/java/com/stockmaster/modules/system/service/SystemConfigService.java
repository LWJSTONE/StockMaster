package com.stockmaster.modules.system.service;

import com.stockmaster.modules.system.dto.SystemConfigDTO;
import com.stockmaster.modules.system.entity.SystemConfig;

import java.util.List;
import java.util.Map;

/**
 * System configuration service interface
 */
public interface SystemConfigService {

    /**
     * Get all configurations
     */
    List<SystemConfig> getAllConfigs();

    /**
     * Get all configurations grouped by configGroup
     */
    Map<String, List<SystemConfig>> getConfigsByGroup();

    /**
     * Get configuration by key
     */
    SystemConfig getConfigByKey(String key);

    /**
     * Get configuration value by key
     */
    String getConfigValue(String key);

    /**
     * Get configuration value by key with default value
     */
    String getConfigValue(String key, String defaultValue);

    /**
     * Get integer configuration value
     */
    Integer getIntConfigValue(String key, Integer defaultValue);

    /**
     * Get boolean configuration value
     */
    Boolean getBooleanConfigValue(String key, Boolean defaultValue);

    /**
     * Update configuration
     */
    SystemConfig updateConfig(String key, SystemConfigDTO dto);

    /**
     * Create configuration
     */
    SystemConfig createConfig(SystemConfigDTO dto);

    /**
     * Delete configuration
     */
    void deleteConfig(String key);

    /**
     * Get configurations by group
     */
    List<SystemConfig> getConfigsByGroupName(String groupName);
}
