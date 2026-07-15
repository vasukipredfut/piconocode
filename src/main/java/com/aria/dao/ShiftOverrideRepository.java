package com.aria.dao;

import com.aria.model.ShiftAssign;
import com.aria.model.ShiftOverride;
import com.aria.model.ShiftRotation;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftOverrideRepository extends JpaRepository<ShiftOverride, Long> {

	
    //@Query(value = "SELECT *  FROM shift_rotation s WHERE s.shift_id =:shiftId ORDER BY id DESC limit 1", nativeQuery = true)
    //ShiftRotation findByShiftId (@Param("shiftId") Long shiftId);
    
	@Query(value = "SELECT so FROM shift_override so WHERE so.shift_date >= :fromDate and so.shift_date <= :toDate" , nativeQuery = true)
	List<ShiftOverride> viewfilterlist(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate); 
}
