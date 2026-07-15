package com.aria.dao;


import com.aria.dto.ShiftRotationMappingData;
import com.aria.model.ShiftRotationMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftRotationMappingRepository  extends JpaRepository<ShiftRotationMapping, Long> {


    @Query(value = "SELECT *  FROM shift_rotation_mapping s WHERE s.shift_rotation_id =:shiftId and s.staff_id =:userId and s.shift =:shiftName", nativeQuery = true)
    Optional<ShiftRotationMapping> findByShiftIdANdUserIdAndSiftName (@Param("shiftId") Long shiftId, @Param("userId") Long userId, @Param("shiftName") String shiftName);


    @Query(value = "SELECT sr.to_Date as createdDate, sr.shift_name as shiftName  FROM shift_rotation_mapping srm left join shift_rotation sr on sr.id = srm.shift_rotation_id  where srm.emp_id =:userId and srm.emp_type =:empType order by srm.shift_rotation_id desc limit 1" , nativeQuery = true)
    ShiftRotationMappingData findByShiftNameANdCreatedDate (@Param("userId") Long userId, @Param("empType") int empType);



    @Query(value = "SELECT *  FROM shift_rotation_mapping s WHERE s.shift_rotation_id =:shiftId and s.shift =:shiftName", nativeQuery = true)
    List<ShiftRotationMapping> findBy (@Param("shiftId") Long shiftId, @Param("shiftName") String shiftName);


}
