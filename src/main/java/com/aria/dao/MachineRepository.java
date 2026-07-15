package com.aria.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;


import com.aria.model.ProductMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.CustomVouNameByUserDeptList;
import com.aria.model.Machine;
import com.aria.model.MachineDropDown;

import jakarta.transaction.Transactional;

@Repository
public interface MachineRepository extends JpaRepository<Machine, Long> {
	@Query(value = "SELECT cm FROM Machine cm WHERE "
			+ " (:deptId is null or cm.departmentDetails.id = :deptId) "
			+ " AND (:branchId is null or cm.branch.id = :branchId) "
			+ " AND (:mainMachine is null or (lower(cm.isMainMachine)) = lower(:mainMachine)) "
			+ " AND (:panel is null or (lower(cm.panel)) = lower(:panel)) "
			+ " AND (:machineName is null or (lower(cm.name) LIKE ('%' || lower(:machineName) || '%'))) "
			+ " AND (:isoNo is null or (lower(cm.iso_no) LIKE ('%' || lower(:isoNo) || '%'))) " 
			+ " AND (:status is null or (lower(cm.status) LIKE ('%' || lower(:status) || '%'))) "
			+ " AND (cast(:dateFrom as timestamp) is null or (cm.dateOfInstallation >= :dateFrom)) "
			+ " AND (cast(:dateTo as timestamp) is null or (cm.dateOfInstallation <= :dateTo)) order by id desc")
	Page<Machine> machineFilterList(
	        @Param("deptId") Long deptId,
	        @Param("branchId") Long  branchId,
			@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, @Param("mainMachine") String mainMachine,
			@Param("machineName") String machineName, @Param("isoNo") String isoNo,
			@Param("status") String status,@Param("panel") String panel,
			Pageable pageable);

	@Query(value = "SELECT cm FROM Machine cm WHERE "
			+ " (cm.cStatus is null or (lower(cm.cStatus) not like('%' || lower('sold') || '%')))"
			+ " AND (:deptId is null or cm.departmentDetails.id = :deptId) "
			+ " AND (:branchId is null or cm.branch.id = :branchId) "
			+ " AND (:mainMachine is null or (lower(cm.isMainMachine)) = lower(:mainMachine)) "
			+ " AND (:panel is null or (lower(cm.panel)) = lower(:panel)) "
			+ " AND (:machineName is null or (lower(cm.name) LIKE ('%' || lower(:machineName) || '%'))) "
			+ " AND (:isoNo is null or (lower(cm.iso_no) LIKE ('%' || lower(:isoNo) || '%'))) "
			+ " AND (:status is null or (lower(cm.status) LIKE ('%' || lower(:status) || '%'))) "
			+ " AND (cast(:dateFrom as timestamp) is null or (cm.dateOfInstallation >= :dateFrom)) "
			+ " AND (cast(:dateTo as timestamp) is null or (cm.dateOfInstallation <= :dateTo)) order by id desc")
	Page<Machine> machineFilterNotSold(
			@Param("deptId") Long deptId,
			@Param("branchId") Long  branchId,
			@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, @Param("mainMachine") String mainMachine,
			@Param("machineName") String machineName, @Param("isoNo") String isoNo,
			@Param("status") String status,@Param("panel") String panel,
			Pageable pageable);

	@Query(value = "SELECT cm FROM Machine cm WHERE "
			+ " (:deptId is null or cm.departmentDetails.id = :deptId) "
			+ " AND (:branchId is null or cm.branch.id = :branchId) "
			+ " AND (:mainMachine is null or (lower(cm.isMainMachine)) = lower(:mainMachine)) "
			+ " AND (:panel is null or (lower(cm.panel)) = lower(:panel)) "
			+ " AND (:machineName is null or (lower(cm.name) LIKE (lower(:machineName) || '%'))) "
			+ " AND (:isoNo is null or (lower(cm.iso_no) LIKE ('%' || lower(:isoNo) || '%'))) "
			+ " AND (:status is null or (lower(cm.status) LIKE ('%' || lower(:status) || '%'))) "
			+ " AND (cast(:dateFrom as timestamp) is null or (cm.dateOfInstallation >= :dateFrom)) "
			+ " AND (cast(:dateTo as timestamp) is null or (cm.dateOfInstallation <= :dateTo)) order by id desc")
	List<Machine> machinefilterList(
			@Param("deptId") Long deptId,
			@Param("branchId") Long  branchId,
			@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, @Param("mainMachine") String mainMachine,
			@Param("machineName") String machineName, @Param("isoNo") String isoNo,
			@Param("status") String status,@Param("panel") String panel);

	@Query(value = "SELECT cm FROM Machine cm WHERE "
			+ " (cm.cStatus is null or (lower(cm.cStatus) not like('%' || lower('sold') || '%')))"
			+ " AND (:deptId is null or cm.departmentDetails.id = :deptId) "
			+ " AND (:branchId is null or cm.branch.id = :branchId) "
			+ " AND (:mainMachine is null or (lower(cm.isMainMachine)) = lower(:mainMachine)) "
			+ " AND (:panel is null or (lower(cm.panel)) = lower(:panel)) "
			+ " AND (:machineName is null or (lower(cm.name) LIKE (lower(:machineName) || '%'))) "
			+ " AND (:isoNo is null or (lower(cm.iso_no) LIKE ('%' || lower(:isoNo) || '%'))) "
			+ " AND (:status is null or (lower(cm.status) LIKE ('%' || lower(:status) || '%'))) "
			+ " AND (cast(:dateFrom as timestamp) is null or (cm.dateOfInstallation >= :dateFrom)) "
			+ " AND (cast(:dateTo as timestamp) is null or (cm.dateOfInstallation <= :dateTo)) order by id desc")
	List<Machine> machineFilterListNotSold(
			@Param("deptId") Long deptId,
			@Param("branchId") Long  branchId,
			@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, @Param("mainMachine") String mainMachine,
			@Param("machineName") String machineName, @Param("isoNo") String isoNo,
			@Param("status") String status,@Param("panel") String panel);
	
	
	@Query(value = "SELECT cm.id as id, cm.name as name FROM Machine cm", nativeQuery = true)
	List<MachineDropDown> machinelist();

	@Query(value="SELECT * FROM machine where classifyid = 5 OR classifyid = 10",nativeQuery = true)
	public List<Machine> findAllVehicles();

	@Query(value="SELECT * FROM machine ",nativeQuery = true)
	public List<Machine> findBy(Pageable pageable);

	@Query(value="SELECT * FROM machine WHERE (:searchProduct is null or LOWER(name) LIKE LOWER(CONCAT('%', :searchProduct, '%')))",nativeQuery = true)
	public List<Machine> findBy(@Param("searchProduct") String searchProduct);

	@Query(value = "SELECT * FROM machine WHERE (:machineId is null or id =:machineId) AND (:hsn is null or hsn_ref_id=:hsn )  AND hsn_ref_id IS NOT NULL AND hsn_Ref_Id NOT IN ('')  LIMIT 1 ", nativeQuery = true)
	Optional<Machine> findByHsn(@Param("machineId") Long machineId, @Param("hsn") String hsn);

	@Query(value = "SELECT * FROM machine WHERE  (:hsn is null or hsn_ref_id=:hsn ) ", nativeQuery = true)
	List<Machine> findByHsn( @Param("hsn") String hsn);

	@Transactional
	@Modifying
	@Query(value = "UPDATE machine m set hsn_ref_id=null WHERE m.hsn_ref_id =:hsnCode",nativeQuery = true)
	void updateHsn(@Param("hsnCode") String hsnCode);
	
	
	 @Query(value = "SELECT m.id as id , m.name as name FROM machine m WHERE m.status = 'ACTIVE' "
		 		+ "      AND ((:deptId IS NOT NULL AND m.department = :deptId) OR (:branchId IS NOT NULL AND m.branch = :branchId) "
		 		+ " OR (:deptId IS NULL AND :branchId IS NULL))", nativeQuery = true)
		 List<CustomVouNameByUserDeptList> findActiveMachine(@Param("deptId") Long deptId, @Param("branchId") Long branchId);

}