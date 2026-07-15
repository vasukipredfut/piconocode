package com.aria.dao;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.SalesProductFilm;

@Repository
public interface SaleProductFilmRepository extends JpaRepository<SalesProductFilm, Long> {

	@Query(value = "SELECT s.* " + "FROM salesproductfilm s "
			+ "LEFT JOIN salesproductgroup s2 ON s2.id = s.productgroupid "
			+ "LEFT JOIN salesproductionbranches s3 ON s3.id = s.branchid "
			+ "LEFT JOIN salesproductname s4 ON s4.id = s.productnameid "
			+ "WHERE (:productId IS NULL OR CONCAT(s.refid , '') LIKE ('%' || :productId || '%')) "
			+ "AND (:productName IS NULL OR lower(s.name) LIKE ('%' || lower(:productName) || '%')) "
			+ "AND (:productGroup IS NULL OR lower(s2.name) LIKE ('%' || lower(:productGroup) || '%')) "
			+ "AND (:branch IS NULL OR lower(s3.name) LIKE ('%' || lower(:branch) || '%')) "
			+ "AND (cast(:dateFrom AS timestamp) IS NULL OR (s.created_date >= :dateFrom)) "
			+ "AND (cast(:dateTo AS timestamp) IS NULL OR (s.created_date <= :dateTo)) "
			+ "AND (:finalProductName IS NULL OR lower(s4.productname) LIKE ('%' || lower(:finalProductName) || '%')) "
			+ "ORDER BY s.id DESC ", nativeQuery = true)
	Page<SalesProductFilm> getSalesProductFilmList(@Param("productId") Long productId,
			@Param("productName") String productName, @Param("productGroup") String productGroup,
			@Param("branch") String branch, @Param("finalProductName") String finalProductName,
			@Param("dateFrom") LocalDateTime dateFrom, @Param("dateTo") LocalDateTime dateTo, Pageable pageable);

	@Query(value = "SELECT * FROM salesproductfilm WHERE refid = :productId AND branchid = :branchId", nativeQuery = true)
	Optional<SalesProductFilm> findByProductIdAndBranchId(@Param("productId") Long productId,
			@Param("branchId") Long branchId);

}
