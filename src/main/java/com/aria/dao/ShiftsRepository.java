package com.aria.dao;


import java.util.Date;
import java.util.List;
import java.util.Map;

import com.aria.model.ShiftsList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.ShiftCategory;
import com.aria.model.Shifts;

@Repository
public interface ShiftsRepository extends JpaRepository<Shifts,Long> {
	
	
	@Query(value = "SELECT *  FROM shifts s WHERE s.name =:shiftName", nativeQuery = true)
    List<Shifts> findByShiftName (@Param("shiftName") String shiftName);

	@Query(value = "SELECT *  FROM shifts s WHERE s.name =:shiftName and branch_id =:branch", nativeQuery = true)
    List<Shifts> findByShiftNameBranch (@Param("shiftName") String shiftName, @Param("branch") Long branch);

	@Query(value = "select sh.id as shiftId, sh.name as shiftName, sh.category as shiftType, sh.lunchtime as lunchBreak, sh.hours as WorkingHours FROM shifts sh "
            +"WHERE (:shiftType is null or (lower(sh.category) LIKE ('%' || lower(:shiftType) || '%')) ) ORDER BY sh.id DESC", nativeQuery = true)
    Page<ShiftsList> findByShifts (@Param("shiftType") String shiftType, Pageable pageable);

    @Query(value = "SELECT sh.category as shiftCategory, count(id) as shiftCount FROM shifts sh WHERE (:shiftType is null or (lower(sh.category) LIKE ('%' || lower(:shiftType) || '%')) ) AND sh.category IS NOT NULL GROUP BY Category", nativeQuery = true)
    List<ShiftCategory> findAllShiftCategory(@Param("shiftType") String shiftType);

    @Query(value = "SELECT *  FROM shifts s WHERE s.category =:category", nativeQuery = true)
    List<Shifts> findByShifts (@Param("category") String category);

    @Query(value = "SELECT count(*) FROM shifts s WHERE s.intime =:intime and s.outtime=:outtime "
    		+ "and s.lunchtime=:lunchtime", nativeQuery = true)
	long checkShiftsAlreadyExisit(@Param("intime") String intime, @Param("outtime") String outtime, 
			@Param("lunchtime") String lunchtime);
    @Query(value = " SELECT s2.* FROM shift_assign sa JOIN shifts s2 ON sa.shift_id = s2.id WHERE sa.emp_id = :userId AND sa.shift_date = :date " , nativeQuery = true)
    Shifts findTodayShift(Long userId, Date date);




}
