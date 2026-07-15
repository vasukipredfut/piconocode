package com.aria.dao;

import com.aria.model.Scrap;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ScrapRepository extends JpaRepository<Scrap, Long> {

	@Query(value = "SELECT s FROM Scrap s WHERE (:scrapItem is null or (lower(s.scrapItem) =lower(:scrapItem))) "
			+ "AND (:departmentId is null or s.fromDepartment.id = :departmentId) "
			+ "AND (:fromProcess is null or lower(s.fromProcess) = lower(:fromProcess)) "
			+ "AND (:selectStorageBin is null or lower(s.selectStorageBin) = lower(:selectStorageBin)) "
			+ "AND (cast(:dateFrom as timestamp) is null or (s.storedOn >= :dateFrom)) "
			+ "AND (cast(:dateTo as timestamp) is null or (s.storedOn <= :dateTo))")

	List<Scrap> scrapfilterList(@Param("scrapItem") String scrapItem, @Param("departmentId") Long deptId,
			@Param("fromProcess") String fromProcess, @Param("selectStorageBin") String selectStorageBin,
			@Param("dateFrom")  Date dateFrom, @Param("dateTo") Date dateTo);

	
}