package com.aria.dao;

import com.aria.model.WorkFlow;
import com.aria.model.WorkFlowDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkFlowDetailsRepository extends JpaRepository<WorkFlowDetails, Long> {

    @Query(value = "SELECT *  FROM work_flow w WHERE w.work_flow_name =:workFlowName", nativeQuery = true)
    Optional<WorkFlow> findByWorkFlowName(@Param("workFlowName") String workFlowName);


    @Query(value = "SELECT *  FROM work_flow_details w WHERE w.flow_name =:workFlowName", nativeQuery = true)
    Optional<WorkFlowDetails> findByFlowName(@Param("workFlowName") String workFlowName);

    @Query(value = "SELECT *  FROM work_flow_details w WHERE w.main = 0", nativeQuery = true)
    List<WorkFlowDetails> findByWorkFlowList();


    @Query(value = "SELECT *  FROM work_flow_details w WHERE w.parent_flow_id =:parentFlowId", nativeQuery = true)
    Optional<WorkFlowDetails> findByWorkParentFlowId(@Param("parentFlowId") Long parentFlowId);

    @Query(value = "SELECT *  FROM work_flow_details w WHERE w.child_flow_id =:childFlowId", nativeQuery = true)
    Optional<WorkFlowDetails> findByWorkChildFlowId(@Param("childFlowId") Long childFlowId);

    @Query(value = "SELECT *  FROM work_flow_details w WHERE w.flow_id =:flowId AND w.main=1 ORDER BY w.id ASC ", nativeQuery = true)
    List<WorkFlowDetails> findByWorkFlowList(@Param("flowId") Long flowId);
}
