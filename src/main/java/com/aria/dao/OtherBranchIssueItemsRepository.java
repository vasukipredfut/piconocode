package com.aria.dao;

import com.aria.model.OtherBranchIssueItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.Tuple;
import java.util.List;

@Repository
public interface OtherBranchIssueItemsRepository extends JpaRepository<OtherBranchIssueItems, Long> {

/*
    @Query(value = "SELECT * FROM other_branch_issue_items ob WHERE ob.issue_Item_Id =:issueId AND ob.material_return='YES' ", nativeQuery = true)
    List<OtherBranchIssueItems> findBy(@Param("issueId") Long issueId);*/


    @Query(value = "SELECT ob.item_id as itemId , ob.item_name as itemName, SUM( CAST (ob.allocated_quantity  as DOUBLE PRECISION) ) as qty FROM other_branch_issue_items ob WHERE ob.issue_Item_Id =:issueId AND ob.material_return='YES' group by ob.item_id, ob.item_name ", nativeQuery = true)
    List<Tuple> findBy(@Param("issueId") Long issueId);
}
