package com.stockmaster.modules.system.repository;

import com.stockmaster.modules.system.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findByParentIdOrderBySortOrder(Long parentId);

    boolean existsByPermission(String permission);

    @Query("SELECT m FROM Menu m WHERE m.deleted = false AND m.status = 1 ORDER BY m.sortOrder")
    List<Menu> findAllActive();

    @Query("SELECT m FROM Menu m WHERE m.deleted = false ORDER BY m.sortOrder")
    List<Menu> findAllOrderBySortOrder();

    @Query("SELECT m FROM Menu m WHERE m.deleted = false AND (m.parentId = 0 OR m.parentId IS NULL) ORDER BY m.sortOrder")
    List<Menu> findRootMenus();
}
