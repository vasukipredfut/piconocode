package com.aria.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.EB;
import com.aria.model.MachineDropDown;

@Repository
public interface EBRepository extends JpaRepository<EB, Long> {

	@Query(value = "SELECT du FROM EB du WHERE "
			+ "(:id is null or (du.id = :id)) "
			+ "AND (:serviceNo is null or (lower(du.serviceNo) LIKE ('%' || lower(:serviceNo) || '%'))) "
			+ "AND (:serviceName is null or (lower(du.serviceName) LIKE ( lower(:serviceName) || '%'))) "
			+ "AND (:serviceCategory is null or (lower(du.serviceCategory) LIKE ('%' || lower(:serviceCategory) || '%'))) "
			+ "AND (:status is null or (lower(du.status) LIKE ('%' || lower(:status) || '%'))) ")
	Page<EB> findAllEBDetails(@Param("id") Long id, @Param("serviceNo") String serviceNo, @Param("serviceName") String serviceName, @Param("serviceCategory") String serviceCategory,
			@Param("status") String status, Pageable pageable);

	@Query(value = "SELECT cm.id as id, cm.serviceName as name FROM eb_master cm", nativeQuery = true)
	List<MachineDropDown> getEbNames();

	@Query(value = "SELECT du FROM EB du WHERE "
			+ " lower(du.serviceName) = lower(:serviceName) ")
	List<EB> getEBByServiceName(String serviceName);

	@Query(value = "SELECT du FROM EB du WHERE "
			+ " (:serviceNo is null or (lower(du.serviceNo) LIKE ('%' || lower(:serviceNo) || '%'))) "
			+ "AND (:serviceName is null or (lower(du.serviceName) LIKE ( lower(:serviceName) || '%'))) "
			+ "AND (:serviceCategory is null or (lower(du.serviceCategory) LIKE ('%' || lower(:serviceCategory) || '%'))) "
			+ "AND (:status is null or (lower(du.status) LIKE ('%' || lower(:status) || '%'))) ")
	List<EB> findAllEBDetails(@Param("serviceNo") String serviceNo, @Param("serviceName") String serviceName, @Param("serviceCategory") String serviceCategory,
							  @Param("status") String status);

	@Query(value = "SELECT du FROM EB du WHERE "
			+ " (:id is null or (du.id = :id)) "
			+ "AND (:serviceNo is null or (lower(du.serviceNo) LIKE ('%' || lower(:serviceNo) || '%'))) "
			+ "AND (:serviceName is null or (lower(du.serviceName) LIKE ( lower(:serviceName) || '%'))) "
			+ "AND (:serviceCategory is null or (lower(du.serviceCategory) LIKE ('%' || lower(:serviceCategory) || '%'))) "
			+ "AND (:status is null or (lower(du.status) LIKE ('%' || lower(:status) || '%'))) ")
	List<EB> findAllEBDetailsById(@Param("id") Long id,@Param("serviceNo") String serviceNo, @Param("serviceName") String serviceName, @Param("serviceCategory") String serviceCategory,
								  @Param("status") String status);

}
