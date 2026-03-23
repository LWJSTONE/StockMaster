package com.stockmaster.modules.system.repository;

import com.stockmaster.modules.system.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * System configuration repository
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {

    /**
     * Find by config key
     */
    Optional<SystemConfig> findByConfigKey(String configKey);

    /**
     * Check if config key exists
     */
    boolean existsByConfigKey(String configKey);

    /**
     * Find all enabled configs
     */
    @Query("SELECT c FROM SystemConfig c WHERE c.deleted = false AND c.isEnabled = true ORDER BY c.configGroup, c.sortOrder")
    List<SystemConfig> findAllEnabled();

    /**
     * Find by config group
     */
    @Query("SELECT c FROM SystemConfig c WHERE c.deleted = false AND c.configGroup = :configGroup ORDER BY c.sortOrder")
    List<SystemConfig> findByConfigGroup(@Param("configGroup") String configGroup);

    /**
     * Find all configs ordered by group and sort
     */
    @Query("SELECT c FROM SystemConfig c WHERE c.deleted = false ORDER BY c.configGroup, c.sortOrder")
    List<SystemConfig> findAllOrderByGroupAndSort();

    /**
     * Find distinct config groups
     */
    @Query("SELECT DISTINCT c.configGroup FROM SystemConfig c WHERE c.deleted = false ORDER BY c.configGroup")
    List<String> findAllConfigGroups();
}
