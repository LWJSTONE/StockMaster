package com.stockmaster.modules.system.controller;

import com.stockmaster.common.aop.LogOperation;
import com.stockmaster.common.dto.ApiResponse;
import com.stockmaster.common.enums.OperationType;
import com.stockmaster.modules.system.dto.SystemConfigDTO;
import com.stockmaster.modules.system.entity.SystemConfig;
import com.stockmaster.modules.system.service.SystemConfigService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * System configuration controller
 */
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    /**
     * Get all configurations
     */
    @GetMapping
    @LogOperation(value = OperationType.QUERY, module = "系统配置", description = "获取所有配置")
    public ApiResponse<List<SystemConfig>> getAllConfigs() {
        List<SystemConfig> configs = systemConfigService.getAllConfigs();
        return ApiResponse.success(configs);
    }

    /**
     * Get configurations grouped by configGroup
     */
    @GetMapping("/grouped")
    @LogOperation(value = OperationType.QUERY, module = "系统配置", description = "获取分组配置")
    public ApiResponse<Map<String, List<SystemConfig>>> getConfigsByGroup() {
        Map<String, List<SystemConfig>> groupedConfigs = systemConfigService.getConfigsByGroup();
        return ApiResponse.success(groupedConfigs);
    }

    /**
     * Get configuration by key
     */
    @GetMapping("/{key}")
    @LogOperation(value = OperationType.QUERY, module = "系统配置", description = "获取指定配置")
    public ApiResponse<SystemConfig> getConfigByKey(@PathVariable String key) {
        SystemConfig config = systemConfigService.getConfigByKey(key);
        return ApiResponse.success(config);
    }

    /**
     * Get configurations by group name
     */
    @GetMapping("/group/{groupName}")
    @LogOperation(value = OperationType.QUERY, module = "系统配置", description = "获取分组配置列表")
    public ApiResponse<List<SystemConfig>> getConfigsByGroupName(@PathVariable String groupName) {
        List<SystemConfig> configs = systemConfigService.getConfigsByGroupName(groupName);
        return ApiResponse.success(configs);
    }

    /**
     * Create new configuration
     */
    @PostMapping
    @LogOperation(value = OperationType.CREATE, module = "系统配置", description = "创建配置")
    public ApiResponse<SystemConfig> createConfig(@Valid @RequestBody SystemConfigDTO dto) {
        SystemConfig config = systemConfigService.createConfig(dto);
        return ApiResponse.success("配置创建成功", config);
    }

    /**
     * Update configuration
     */
    @PutMapping("/{key}")
    @LogOperation(value = OperationType.UPDATE, module = "系统配置", description = "更新配置")
    public ApiResponse<SystemConfig> updateConfig(@PathVariable String key, @Valid @RequestBody SystemConfigDTO dto) {
        SystemConfig config = systemConfigService.updateConfig(key, dto);
        return ApiResponse.success("配置更新成功", config);
    }

    /**
     * Delete configuration
     */
    @DeleteMapping("/{key}")
    @LogOperation(value = OperationType.DELETE, module = "系统配置", description = "删除配置")
    public ApiResponse<Void> deleteConfig(@PathVariable String key) {
        systemConfigService.deleteConfig(key);
        return ApiResponse.success("配置删除成功", null);
    }
}
