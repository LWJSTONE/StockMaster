package com.stockmaster.modules.system.repository;

import com.stockmaster.modules.system.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleCode(String roleCode);

    boolean existsByRoleCode(String roleCode);

    boolean existsByRoleCodeAndIdNot(String roleCode, Long id);

    @Query("SELECT r FROM Role r WHERE r.deleted = false AND r.status = 1 ORDER BY r.sortOrder")
    List<Role> findAllActive();

    @Query("SELECT r FROM Role r WHERE r.deleted = false ORDER BY r.sortOrder")
    List<Role> findAllOrderBySortOrder();

    @Query("SELECT r FROM Role r WHERE r.deleted = false AND (r.roleCode LIKE %:keyword% OR r.roleName LIKE %:keyword% OR r.description LIKE %:keyword%)")
    Page<Role> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT r FROM Role r WHERE r.deleted = false ORDER BY r.sortOrder")
    Page<Role> findAllByDeletedFalse(Pageable pageable);
}
