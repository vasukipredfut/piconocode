package com.aria.dao;

import com.aria.dto.*;
import com.aria.model.Kpi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KpiRepository extends JpaRepository<Kpi, Long> {


    @Query(value = "SELECT tgid FROM kpi k where k.id =:id and k.refid=:proId and k.active='Y'",nativeQuery = true)
    List<Long> findAll(@Param("id") Long id, @Param("proId") Long proId );

    @Query(value = "SELECT * FROM kpi k where k.id =:id and k.refid=:proId and k.tgid =:tgId limit 1",nativeQuery = true)
    Optional<Kpi> findBy(@Param("id") Long id, @Param("proId") Long proId, @Param("tgId") Long tgId  );


    @Query(value = "select k.priid as stepId, k.active as active, k.dispord as stepNumber, k.skip as skip ,  tn.task_name as taskName,  "
            + " tgm.task_group_name as taskGroup, tc.task_category_name as taskCategory, kpi.id as kpiNameId, kpi.name as kpiName, "
            + " pc.id as productMasterId, pc.name as productMasterName , tg.id as taskGroupMapId FROM kpi k left join task_group tg on tg.id = k.tgid "
            + " left join task_name tn on tn.id = tg.task left join task_category tc on tc.id = tg.taskcategory left join kpiname kpi on kpi.id = k.id left join  "
            + " products_master pc on pc.id = k.refid left join  task_group_master tgm on tgm.id = tg.taskgroup  "
            + " where k.active ='Y' AND  k.id=:kpiNameId and k.refid>0 and k.id=kpi.id and pc.id=kpi.proid and tg.taskgroup = tgm.id "
            + "	and k.tgid=tg.id and tn.id=tg.task and tc.id=tg.taskcategory  order by k.dispord", nativeQuery = true)
    List<KpiList> getKpiNameStepsDetails(@Param("kpiNameId") Long kpiNameId);


    @Query(value = "select k.priid as stepId, k.active as active, k.dispord as stepNumber, k.skip as skip ,  tn.task_name as taskName,  "
            + " tgm.task_group_name as taskGroup, tc.task_category_name as taskCategory, kpi.id as kpiNameId, kpi.name as kpiName, "
            + " pc.id as productMasterId, pc.name as productMasterName , tg.id as taskGroupMapId FROM kpi k left join task_group tg on tg.id = k.tgid "
            + " left join task_name tn on tn.id = tg.task left join task_category tc on tc.id = tg.taskcategory left join kpiname kpi on kpi.id = k.id left join  "
            + " products_master pc on pc.id = k.refid left join  task_group_master tgm on tgm.id = tg.taskgroup  "
            + " where k.id=:kpiNameId and k.refid>0 and k.id=kpi.id and pc.id=kpi.proid and tg.taskgroup = tgm.id "
            + "	and k.tgid=tg.id and tn.id=tg.task and tc.id=tg.taskcategory  order by k.dispord", nativeQuery = true)
    List<KpiList> getAllKpiNameStepsDetails(@Param("kpiNameId") Long kpiNameId);
}


