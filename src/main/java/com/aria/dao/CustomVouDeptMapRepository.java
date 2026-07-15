package com.aria.dao;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.CustomVouDeptMap;

@Repository
public interface CustomVouDeptMapRepository extends JpaRepository<CustomVouDeptMap, Long> {

	@Query(value = "SELECT m.* FROM customvou_deptmap m " +
            "JOIN vouname v ON m.customvounameid = v.id " +
            "WHERE v.name = :name AND m.dept IN (:dept)", nativeQuery = true)
List<CustomVouDeptMap> findByNewVoucher(@Param("name") String name,
                                                  @Param("dept") List<Long> dept);

	List<CustomVouDeptMap> findByCustomVouNameId(Long id);

	@Modifying
	@Transactional
	@Query(value = "delete from customvou_deptmap c WHERE c.customvounameid=:customvounameid" , nativeQuery = true)
	void deleteByCustomVouNameId(@Param("customvounameid") Long customvounameid);
}
