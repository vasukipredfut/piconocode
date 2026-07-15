package com.aria.dao;


import com.aria.model.DpEntryProposalParamInspectionScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DpEntryProposalParamInspectionScoreRepository  extends JpaRepository<DpEntryProposalParamInspectionScore,Long> {

    @Query(value = "SELECT *  FROM dp_entry_proposal_param_inspection_score dp   "
            + "WHERE (:poNo is null or dp.po_number = :poNo) "
            + "AND (:billId is null or dp.bill_ref_id = :billId) "
            + "AND (:itemId is null or dp.item_id = :itemId) "
            + "AND ( dp.param_score =:paramScore) ",nativeQuery = true)
    List<DpEntryProposalParamInspectionScore> getListParamScore(@Param("poNo") String poNo, @Param("billId") Long billId, @Param("itemId") Long itemId, @Param("paramScore") int paramScore);

    @Query(value = "SELECT *  FROM dp_entry_proposal_param_inspection_score dp   "
            + "WHERE (:poNo is null or dp.po_number = :poNo) "
            + "AND (:billId is null or dp.bill_ref_id = :billId) "
            + "AND (:itemId is null or dp.item_id = :itemId)  ",nativeQuery = true)
    List<DpEntryProposalParamInspectionScore> getListParamScorewithall(@Param("poNo") String poNo, @Param("billId") Long billId, @Param("itemId") Long itemId);

    
    @Query(value = "SELECT *  FROM dp_entry_proposal_param_inspection_score dp   "
            + "WHERE (:billId is null or dp.bill_ref_id = :billId) "
            + "AND (:itemId is null or dp.item_id = :itemId) ",nativeQuery = true)
    List<DpEntryProposalParamInspectionScore> getListParamScoreData(@Param("billId") Long billId, @Param("itemId") Long itemId);

    
    @Query(value = "SELECT *  FROM dp_entry_proposal_param_inspection_score dp   "
            + "WHERE (:poNo is null or dp.po_number = :poNo) "
            + "AND (:billId is null or dp.bill_ref_id = :billId) "
            + "AND (:itemId is null or dp.item_id = :itemId) "
            + "AND ( :paramId is null or dp.param_id =:paramId) ",nativeQuery = true)
    DpEntryProposalParamInspectionScore getListParamScoreduplicate(@Param("poNo") String poNo, @Param("billId") Long billId, @Param("itemId") Long itemId, @Param("paramId") String paramId);

    
    
    

}
