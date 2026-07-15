package com.aria.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.Machine;
import com.aria.model.MachineClassification;
import com.aria.model.MachineCluster;
import com.aria.model.MachineClusterItems;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface MachineClusterItemsRepository extends JpaRepository<MachineClusterItems, Long> {
//	@Query(value = "SELECT cm FROM MachineCluster cm")
////			+ "WHERE (:name is null or cm.name= :name)")
//	Page<MachineCluster> machineClusterFilterList(Pageable pageable);
    @Modifying
    @Transactional
    void deleteByClusterId(Long clusterId);

}