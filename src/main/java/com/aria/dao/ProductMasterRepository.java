package com.aria.dao;

import com.aria.dto.DayEndStockList;
import com.aria.dto.ProductsWithLotList;
import com.aria.model.ProductList;
import com.aria.model.ProductMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductMasterRepository extends JpaRepository<ProductMaster, Long> {

	@Query(value = "SELECT * FROM products_master  ORDER BY id DESC LIMIT 500", nativeQuery = true)
	List<ProductMaster> findAll();

	@Query(value = "SELECT distinct category FROM products_master WHERE category LIKE :category", nativeQuery = true)
	List<String> findAllByCategory(@Param("category") String category);

	@Query(value = "SELECT distinct category FROM products_master", nativeQuery = true)
	List<String> findAllByCategory();

	@Query(value = "SELECT distinct rawmaterial FROM products_master WHERE rawmaterial LIKE :rawmaterial", nativeQuery = true)
	List<String> findAllByRawmaterial(@Param("rawmaterial") String rawmaterial);

	@Query(value = "SELECT distinct rawmaterial FROM products_master", nativeQuery = true)
	List<String> findAllByRawmaterial();

	@Query(value = "SELECT id, rawmaterial as name FROM products_master WHERE case when :rawmaterial is null then "
			+ " (ID in (SELECT MIN(ID) FROM PRODUCTS_MASTER GROUP BY RAWMATERIAL)) else (id in ( "
			+ " select MIN(id) from products_master where (:rawmaterial is null or rawmaterial LIKE :rawmaterial)  GROUP BY rawmaterial)) end", nativeQuery = true)
	List<Tuple> findAllByRawmaterial1(@Param("rawmaterial") String rawmaterial, Pageable pageable);

	@Query(value = "SELECT distinct productcategory FROM products_master WHERE productcategory LIKE :productcategory", nativeQuery = true)
	List<String> findAllByProductCategory(@Param("productcategory") String productcategory);

	@Query(value = "SELECT distinct productcategory FROM products_master", nativeQuery = true)
	List<String> findAllByProductCategory();

	@Query(value = "SELECT distinct packingtype FROM products_master WHERE packingtype LIKE :packingtype", nativeQuery = true)
	List<String> findAllByPackingType(@Param("packingtype") String packingtype);

	@Query(value = "SELECT distinct packingtype FROM products_master", nativeQuery = true)
	List<String> findAllByPackingType();

	@Query(value = "SELECT distinct unit FROM products_master WHERE unit LIKE :unit", nativeQuery = true)
	List<String> findAllByUnit(@Param("unit") String unit);

	@Query(value = "SELECT distinct unit FROM products_master", nativeQuery = true)
	List<String> findAllByUnit();

	@Query(value = "SELECT distinct altunit FROM products_master", nativeQuery = true)
	List<String> findAllByAltUnit();

	@Query(value = "SELECT distinct altunit FROM products_master WHERE altunit LIKE :altunit", nativeQuery = true)
	List<String> findAllByAltUnit(@Param("altunit") String altunit);

	@Query(value = "SELECT distinct taskcategory FROM products_master WHERE taskcategory LIKE :taskcategory", nativeQuery = true)
	List<String> findAllByTaskCategory(@Param("taskcategory") String taskcategory);

	@Query(value = "SELECT distinct taskcategory FROM products_master", nativeQuery = true)
	List<String> findAllByTaskCategory();

	@Query(value = "SELECT * FROM products_master where blending=true", nativeQuery = true)
	List<ProductMaster> findBlendingEnabled();

	@Query(value = "SELECT id , name as productName, category as productType, picture, search, recycle , taskcategory as taskCategory FROM products_master pm WHERE "
			+ " (:productName is null or (lower(pm.name) LIKE (lower(:productName) || '%')) )"
			+ " AND (:productType is null or (lower(pm.category) LIKE ('%' || lower(:productType) || '%')) ) ORDER BY pm.id DESC", nativeQuery = true)
	Page<ProductList> productFilterList(@Param("productName") String productName,
			@Param("productType") String productType, Pageable pageable);

	@Query(value = "SELECT * FROM products_master WHERE LOWER(name) LIKE LOWER(CONCAT('%', :searchProduct, '%'))", nativeQuery = true)
	public List<ProductMaster> findByString(@Param("searchProduct") String searchProduct);

	@Query(value = "SELECT id , name as productName, category as productType, picture, search, recycle , taskcategory as taskCategory FROM products_master pm WHERE "
			+ " (:productName is null or (lower(pm.name) LIKE ('%' || lower(:productName) || '%')) )"
			+ "AND (:pcategory is null or pm.pcategory=:pcategory ) ORDER BY pm.id DESC", nativeQuery = true)
	List<ProductList> productFilterList(@Param("productName") String productName, @Param("pcategory") String pcategory);

	@Query(value = "SELECT * FROM products_master WHERE (:searchProduct is null or LOWER(name) LIKE LOWER(CONCAT('%', :searchProduct, '%'))) and (:branchId is null or purchase_responsibility_plant=:branchId) and (:departmentId is null or dept_id=:departmentId)", nativeQuery = true)
	public List<ProductMaster> findByDepAndBranch(@Param("searchProduct") String searchProduct,
			@Param("branchId") Long branchId, @Param("departmentId") Long departmentId);

	@Query(value = "SELECT * FROM products_master ", nativeQuery = true)
	public List<ProductMaster> findBy(Pageable pageable);

	@Query(value = "SELECT * FROM products_master WHERE (:searchProduct is null or LOWER(name) LIKE LOWER(CONCAT('%', :searchProduct, '%')))", nativeQuery = true)
	public List<ProductMaster> findBy(@Param("searchProduct") String searchProduct);

	@Query(value = "SELECT * FROM products_master WHERE id in (:ids) "
			+ " AND (:searchProduct is null or LOWER(name) LIKE LOWER(CONCAT('%', :searchProduct, '%')))", nativeQuery = true)
	public List<ProductMaster> findBy(@Param("ids") List<Long> ids, @Param("searchProduct") String searchProduct);

	@Query(value = "SELECT pm.id, pm.name as productName, pm.category as productType, pm.picture, pm.search, pm.recycle, pm.taskcategory as taskCategory, pmapping.product_category_ref_id FROM products_master pm "
			+ "JOIN product_mapping pmapping ON pm.id = pmapping.product_ref_id "
			+ "WHERE pmapping.product_category_ref_id =:categoryId "
			+ "AND (:productType is null OR lower(pm.category) LIKE ('%' || lower(:productType) || '%')) "
			+ "AND (:productName is null OR lower(pm.name) LIKE ('%' || lower(:productName) || '%')) ", nativeQuery = true)
	Page<ProductList> productFilterListByCategory(@Param("productName") String productName,
			@Param("productType") String productType, @Param("categoryId") Long categoryId, Pageable pageable);

	@Query(value = "select pm.id as itemId, pm.name as itemName from products_master pm on pm.id in (:itemIds) ", nativeQuery = true)
	List<DayEndStockList> findBY(@Param("itemIds") List<Long> ids);

	@Query(value = "SELECT p.hsn_ref_id FROM products_master p WHERE p.id = :itemId", nativeQuery = true)
	String findHsnNoByitemId(@Param("itemId") Long itemId);


	@Query(value = " select pm.id as productId, pm.name as productName from products_master pm WHERE (:productName is null OR lower(pm.name) LIKE ('%' || lower(:productName) || '%')) ", nativeQuery = true)
	List<ProductsWithLotList> findBY(@Param("productName") String productName);

	@Query(value = " select pm.id as productId, pm.name as productName, pm.split_config_based as splitConfigbased from products_master pm WHERE  split_config = true AND (:productName is null OR lower(pm.name) LIKE ('%' || lower(:productName) || '%'))   ", nativeQuery = true)
	List<ProductsWithLotList> findBYSplit(@Param("productName") String productName);


	@Query(value = " select pm.id as productId from products_master pm WHERE  pm.product_category_type='jaggery'", nativeQuery = true)
	List<Long> findBYJaggerProducts();

	@Query(value = " select pm.id as productId from products_master pm WHERE  pm.product_category_type='sesame'", nativeQuery = true)
	List<Long> findBYSesameProducts();

	@Query(value = " select pm.id as productId from products_master pm WHERE  pm.product_category_type='groundnut'", nativeQuery = true)
	List<Long> findBYGroundnutProducts();


	@Query(value = "SELECT * FROM products_master WHERE id in (select distinct refid from stbin s where DATE(s.accounting_date) =DATE(:trnDate) and branch =:branch) ORDER BY id ASC ", nativeQuery = true)
	List<ProductMaster> rawMaterialProductList(@Param("branch") Long branch,
											   @Param("trnDate") Date trnDate);

	@Query(value = "SELECT * FROM products_master WHERE (:productId is null or id =:productId) AND (:hsn is null or hsn_Ref_Id=:hsn ) AND hsn_Ref_Id IS NOT NULL AND hsn_Ref_Id NOT IN ('') LIMIT 1 ", nativeQuery = true)
	Optional<ProductMaster> findByHsn(@Param("productId") Long productId, @Param("hsn") String hsn);

	@Query(value = "SELECT * FROM products_master WHERE id=:id ", nativeQuery = true)
	Optional<ProductMaster> findBy(@Param("id") Long id);


	@Query(value = "SELECT * FROM products_master WHERE  (:hsn is null or hsn_Ref_Id=:hsn )  ", nativeQuery = true)
	List<ProductMaster> findByHsn( @Param("hsn") String hsn);



	@Query(value = " select pm.id as productId from products_master pm WHERE pm.pcategory =:pcategory  ", nativeQuery = true)
	List<Long> findBYAllProductIds(@Param("pcategory") String pcategory);


	@Query(value = " select pm.id as productId from products_master pm WHERE pm.pcategory IS NOT NULL limit 100 ", nativeQuery = true)
	List<Long> findBYAllProductIdList();


	@Query(value = "SELECT id , name as productName, category as productType, picture, search, recycle , taskcategory as taskCategory " +
			" FROM products_master pm WHERE pm.id not in (select distinct s.refid  from salesproductfilm s ) " +
			" AND pm.id not in (select distinct l.refid  from labelproduct l) " +
			" ORDER BY pm.id desc  limit 30 ", nativeQuery = true)
	List<ProductList> restrictLabelOrFlimProductMasterListWithlimit();

	@Query(value = "SELECT id , name as productName, category as productType, picture, search, recycle , taskcategory as taskCategory "
			+ " FROM products_master pm WHERE pm.id not in (select distinct s.refid  from salesproductfilm s ) "
			+ " AND pm.id not in (select distinct l.refid  from labelproduct l)  "
			+ " AND (:productName is null or (lower(pm.name) LIKE (lower(:productName) || '%')) )"
			+ " AND (:productType is null or (lower(pm.category) LIKE ('%' || lower(:productType) || '%')) ) ORDER BY pm.id DESC", nativeQuery = true)
	List<ProductList> restrictLabelOrFlimProductMasterListWithOutlimit(@Param("productName") String productName,
										@Param("productType") String productType);


	@Transactional
	@Modifying
	@Query(value = "UPDATE products_master pm set hsn_Ref_Id=null WHERE pm.hsn_Ref_Id =:hsnCode",nativeQuery = true)
	void updateHsn(@Param("hsnCode") String hsnCode);




}
