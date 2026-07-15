package com.aria.dao;

import java.util.List;
import java.util.Optional;

import com.aria.dto.BranchAddress;
import com.aria.dto.BranchAddressList;
import com.aria.model.SalesReturnProducts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.bAddressList;
import com.aria.model.Branch;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

	@Query(value = "SELECT * FROM branch WHERE rmwh ='Y' order by id ASC ", nativeQuery = true)
	List<Branch> findBy();

	@Query(value = "SELECT * FROM branch WHERE name =:name Limit 1", nativeQuery = true)
	Optional<Branch> findBy(@Param("name") String name);

	@Query(value = "SELECT b.id as branchId, b.name as branchName, b.address as address, b.city as city FROM branch b WHERE b.id = :branchId  order by id ASC ", nativeQuery = true)
	List<bAddressList> getBranchById(@Param("branchId") Long branchId);

	@Query(value = "SELECT * FROM branch b WHERE b.id in ( select distinct branch from branch_org_map where ( :orgId is null OR company = :orgId) ) order by b.id ASC ", nativeQuery = true)
	List<Branch> findAll (@Param("orgId") Long orgId);

	@Query(value = "SELECT b.id as branchId, b.name as branchName, oca.address as address, b.state as state, b.city as city , oca.pincode as pinCode, " +
			" b.email as email , b.fax as fax , b.phone as phoneNo  FROM branch b  LEFT JOIN our_company_address oca on oca.company = b.org_id AND oca.branch = b.id  " +
			" WHERE b.id = :branchId AND b.org_id =:orgId  ", nativeQuery = true)
	BranchAddressList getBranchByIdANDOrgId(@Param("branchId") Long branchId,
											@Param("orgId") Long orgId);

	@Query(value = "SELECT b.id as branchId, b.name as branchName, b.address as address, b.state as state, b.city as city , b.pincode as pinCode, " +
			" b.email as email , b.fax as fax , b.phone as phoneNo  FROM branch b  WHERE b.id = :branchId   ", nativeQuery = true)
	BranchAddressList getBranchId(@Param("branchId") Long branchId);


	@Query(value = "SELECT b.* FROM branch b left join organisation o on b.org_id  = o.id " +
			"WHERE  (:orgId is null OR  b.org_id =:orgId)  " +
			"AND (:OrgName is null or lower(o.name) like ('%' || lower(:OrgName) || '%')) " +
			"AND (:branchName is null or lower(b.name) like ('%' || lower(:branchName) || '%')) order by b.id ASC ", nativeQuery = true)
	Page<Branch> branchDetailList(@Param("orgId") Long orgId, @Param("branchName") String branchName,
								  @Param("OrgName") String OrgName , Pageable pageable);

	@Query(value = "SELECT * FROM branch WHERE id in (1,2,3, 4, 22 ) AND id NOT IN (:branch) order by id ASC ", nativeQuery = true)
	List<Branch> findBy(@Param("branch") Long branch);
	@Query(value = "SELECT b.id FROM branch b where b.org_id =:org_id and b.costreport = 'Y' ", nativeQuery = true)
	List<Long> findByBranch(Long org_id);


}
