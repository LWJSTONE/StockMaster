package com.stockmaster.modules.stock.repository;

import com.stockmaster.modules.stock.entity.Product;
import com.stockmaster.common.enums.StockStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductCode(String productCode);

    Optional<Product> findByBarcode(String barcode);

    boolean existsByProductCode(String productCode);

    boolean existsByProductCodeAndIdNot(String productCode, Long id);

    List<Product> findByCategoryId(Long categoryId);

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND " +
            "(:keyword IS NULL OR p.productCode LIKE %:keyword% OR p.productName LIKE %:keyword% OR p.barcode LIKE %:keyword%) AND " +
            "(:categoryId IS NULL OR p.categoryId = :categoryId) AND " +
            "(:status IS NULL OR p.status = :status)")
    Page<Product> findByConditions(@Param("keyword") String keyword,
                                    @Param("categoryId") Long categoryId,
                                    @Param("status") StockStatus status,
                                    Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.status = :status")
    List<Product> findAllActive(@Param("status") StockStatus status);

    @Query("SELECT p FROM Product p WHERE p.deleted = false ORDER BY p.createTime DESC")
    List<Product> findAllOrderByCreateTime();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.deleted = false AND p.status = :status")
    Long countActiveProducts(@Param("status") StockStatus status);
}
