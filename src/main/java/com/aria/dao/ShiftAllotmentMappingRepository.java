package com.aria.dao;


import com.aria.model.ShitAllotmentMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ShiftAllotmentMappingRepository extends JpaRepository<ShitAllotmentMapping, Long> {

    @Query(value = "SELECT *  FROM shift_allotment_mapping s WHERE s.shift_id =:shiftId and s.user_id =:userId", nativeQuery = true)
    Optional<ShitAllotmentMapping> findByShiftIdANdUserId (@Param("shiftId") Long shiftId, @Param("userId") Long userId);
}
