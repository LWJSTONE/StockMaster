package com.stockmaster.modules.system.repository;

import com.stockmaster.modules.system.entity.SysLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SysLogRepository extends JpaRepository<SysLog, Long> {

    @Query("SELECT s FROM SysLog s WHERE " +
            "(:operationType IS NULL OR s.operationType = :operationType) AND " +
            "(:module IS NULL OR s.module LIKE %:module%) AND " +
            "(:keyword IS NULL OR s.username LIKE %:keyword% OR s.description LIKE %:keyword%) AND " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:startTime IS NULL OR s.createTime >= :startTime) AND " +
            "(:endTime IS NULL OR s.createTime <= :endTime)")
    Page<SysLog> findByConditions(@Param("operationType") String operationType,
                                   @Param("module") String module,
                                   @Param("keyword") String keyword,
                                   @Param("status") Integer status,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime,
                                   Pageable pageable);

    List<SysLog> findByUsernameOrderByCreateTimeDesc(String username);
}
