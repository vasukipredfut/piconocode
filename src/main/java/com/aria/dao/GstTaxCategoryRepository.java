package com.aria.dao;


import java.util.List;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.GstTaxCategory;


@Repository
public interface GstTaxCategoryRepository extends JpaRepository<GstTaxCategory, Long> {
	
	@Query(value = "SELECT * FROM gst_tax_category WHERE hsn_code = :hsnCode limit 1 ", nativeQuery = true)
    GstTaxCategory findByHsnCode(@Param("hsnCode") String hsnCode);


    @Query(value = "SELECT * FROM gst_tax_category WHERE hsn_code = :hsnCode AND category=:category limit 1 ", nativeQuery = true)
    GstTaxCategory findByHsnCode(@Param("hsnCode") String hsnCode,
                                 @Param("category") String category );

	@Query(value = "SELECT * FROM gst_tax_category WHERE hsn_code in (:hsnCode)", nativeQuery = true)
    List<GstTaxCategory> findAllByHsnCode(@Param("hsnCode") List<String> hsnCode);

    @Query(value = "SELECT pm.* FROM gst_tax_category pm WHERE "
            + " (:hsn is null or (lower(pm.hsn_code) LIKE (lower(:hsn) || '%')) )"
            + " AND (:category is null or (lower(pm.category) LIKE (lower(:category) || '%')) )"
            + " AND (:itemCategory is null or (lower(pm.item_category) LIKE ('%' || lower(:itemCategory) || '%')) ) order by pm.id desc", nativeQuery = true)
    Page<GstTaxCategory> hsnFilterList(@Param("hsn") String hsn, @Param("category") String category,@Param("itemCategory") String itemCategory, Pageable pageable);


    @Query(value = "SELECT * FROM gst_tax_category WHERE category = 'P' ", nativeQuery = true)
    List<GstTaxCategory> findAll1();
}
