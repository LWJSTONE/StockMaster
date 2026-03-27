package com.stockmaster.modules.system.service;

import com.stockmaster.modules.system.dto.SystemConfigDTO;
import com.stockmaster.modules.system.entity.SystemConfig;

import java.util.List;
import java.util.Map;

public interface SystemConfigService {

    /**
     * 根据ID获取配置
     * @param id 配置ID
     * @return 系统配置
     */
    SystemConfig getById(Long id);

    /**
     * 根据配置键获取配置
     * @param configKey 配置键
     * @return 系统配置
     */
    SystemConfig getByKey(String configKey);

    /**
     * 根据配置键获取配置（Controller使用）
     * @param key 配置键
     * @return 系统配置
     */
    SystemConfig getConfigByKey(String key);

    /**
     * 根据配置组获取配置列表
     * @param configGroup 配置组
     * @return 配置列表
     */
    List<SystemConfig> getByGroup(String configGroup);

    /**
     * 根据组名获取配置列表（Controller使用）
     * @param groupName 组名
     * @return 配置列表
     */
    List<SystemConfig> getConfigsByGroupName(String groupName);

    /**
     * 获取所有配置
     * @return 配置列表
     */
    List<SystemConfig> getAllConfigs();

    /**
     * 获取按组分类的配置
     * @return 组名到配置列表的映射
     */
    Map<String, List<SystemConfig>> getConfigsByGroup();

    /**
     * 创建配置
     * @param configDTO 配置DTO
     * @return 创建后的配置
     */
    SystemConfig create(SystemConfigDTO configDTO);

    /**
     * 创建配置（Controller使用）
     * @param dto 配置DTO
     * @return 创建后的配置
     */
    SystemConfig createConfig(SystemConfigDTO dto);

    /**
     * 更新配置
     * @param id 配置ID
     * @param configDTO 配置DTO
     * @return 更新后的配置
     */
    SystemConfig update(Long id, SystemConfigDTO configDTO);

    /**
     * 根据配置键更新配置（Controller使用）
     * @param key 配置键
     * @param dto 配置DTO
     * @return 更新后的配置
     */
    SystemConfig updateConfig(String key, SystemConfigDTO dto);

    /**
     * 删除配置
     * @param id 配置ID
     */
    void delete(Long id);

    /**
     * 根据配置键删除配置（Controller使用）
     * @param key 配置键
     */
    void deleteConfig(String key);

    /**
     * 更新配置状态
     * @param id 配置ID
     * @param status 状态
     */
    void updateStatus(Long id, Integer status);
}
