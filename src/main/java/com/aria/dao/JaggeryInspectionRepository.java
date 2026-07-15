package com.aria.dao;

import com.aria.dto.JaggeryInspectionList;
import com.aria.model.JaggeryInspection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface JaggeryInspectionRepository extends JpaRepository<JaggeryInspection, Long> {

    @Query(value = "SELECT ji.* , l.name as vendorName from jaggeryinsp ji "
			+ " LEFT JOIN ledger l on l.id = ji.party  "
            + " WHERE (:vendorId is null or ji.party =:vendorId) "
            + " AND (:vendorName is null or lower(l.name) like ('%' || lower(:vendorName) || '%'))  "
            + " AND (:billNo is null or lower(ji.billno) like ('%' || lower(:billNo) || '%'))  "
            + " AND (:status is null or lower(ji.status) like ('%' || lower(:status) || '%'))  "
            + " AND (:trnNo is null or lower(ji.uniqno) like ('%' || lower(:trnNo) || '%'))  "
            + " AND (cast(:billDate as date) is null or (ji.billdt =:billDate)) "
            + " AND (cast(:jagDate as date) is null or (ji.jdate =:jagDate)) ORDER BY id DESC ", nativeQuery = true)
    Page<JaggeryInspectionList> findBy(@Param("vendorId") Long vendorId, @Param("vendorName") String vendorName,
                                       @Param("billNo") String billNo, @Param("billDate") Date billDate,
									   @Param("jagDate") Date jagDate,
                                       @Param("status") String status, @Param("trnNo") String trnNo , Pageable pageable);

    @Query(value = "SELECT * from jaggeryinsp ji  WHERE "
            + " (:vendorId is null or ji.party =:vendorId) "
            + " AND (:billNo is null or ji.billno =:billNo ) "
            + " AND (cast(:billDate as date) is null or (ji.billdt =:billDate)) LIMIT 1 ", nativeQuery = true)
    Optional<JaggeryInspection> findBy(@Param("vendorId") Long vendorId, @Param("billNo") String billNo, @Param("billDate") Date billDate);
}
