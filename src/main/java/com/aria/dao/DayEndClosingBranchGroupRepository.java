package com.aria.dao;

import com.aria.model.DayEndClosingBranchGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DayEndClosingBranchGroupRepository extends JpaRepository<DayEndClosingBranchGroup,Long> {


    List<DayEndClosingBranchGroup> findByBranchId(Long branchId);

    @Query(value = "SELECT d.child_branch_id FROM day_end_closing_branch_group d WHERE d.branch_id = :branchId", nativeQuery = true)
    List<Long> findChildBranchIdByBranchId(Long branchId);

}
