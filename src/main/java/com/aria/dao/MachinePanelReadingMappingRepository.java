package com.aria.dao;


import com.aria.model.MachinePanelReadingMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MachinePanelReadingMappingRepository extends JpaRepository<MachinePanelReadingMapping, Long> {

    @Query(value = "SELECT * FROM machine_workflow_panel_reading mc where (:workFlowId is null or (mc.work_flow_id = :workFlowId )) ", nativeQuery = true)
    List<MachinePanelReadingMapping> findByWorkFlowId(@Param("workFlowId") Long workFlowId);

    @Query(value = "SELECT * FROM machine_workflow_panel_reading mc where (:workFlowId is null or (mc.work_flow_id = :workFlowId )) AND (:machineId is null or (mc.machine_id = :machineId ))  ", nativeQuery = true)
    List<MachinePanelReadingMapping> findBy(@Param("workFlowId") Long workFlowId, @Param("machineId") Long machineId );

    @Query(value = "SELECT * FROM machine_workflow_panel_reading mc where (:workFlowId is null or (mc.work_flow_id = :workFlowId )) AND (:machineId is null or (mc.machine_id = :machineId )) AND (:panelId is null or (mc.panel_id = :panelId ))  LIMIT 1 ", nativeQuery = true)
    Optional<MachinePanelReadingMapping> findBy(@Param("workFlowId") Long workFlowId, @Param("machineId") Long machineId , @Param("panelId") Long panelId  );



    @Query(value = "SELECT * FROM machine_workflow_panel_reading mc where  (:machineId is null or (mc.machine_id = :machineId )) AND (:panelId is null or (mc.panel_id = :panelId ))  LIMIT 1 ", nativeQuery = true)
    Optional<MachinePanelReadingMapping> findByReading(@Param("machineId") Long machineId , @Param("panelId") Long panelId  );


    @Query(value = "SELECT * FROM machine_workflow_panel_reading mc where (:panelId is null or (mc.panel_id = :panelId ))  LIMIT 1 ", nativeQuery = true)
    Optional<MachinePanelReadingMapping> findByReading( @Param("panelId") Long panelId  );

}
