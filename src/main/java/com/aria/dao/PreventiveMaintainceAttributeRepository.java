package com.aria.dao;

import com.aria.model.Action;
import com.aria.model.PreventiveMaintainceAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PreventiveMaintainceAttributeRepository extends JpaRepository<PreventiveMaintainceAttributes,Long> {

    PreventiveMaintainceAttributes findBySchIdAndMcIdAndBatchNoAndTaskIdAndAtIdAndResId(Long schId,Long mcId,String batchNo,Long taskId,Long atId,Long resId);

    List<PreventiveMaintainceAttributes> findByMcId(Long mcId);

    @Transactional
    @Modifying
    void deleteByMcId(Long mcId);
}
