
package com.aria.dao;


import com.aria.dto.BlendingBinList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.BinDetails;

import java.util.List;

@Repository
public interface BinDetailsRepository extends JpaRepository<BinDetails, Long> {


    @Query(value = "SELECT b.binname as bin_no, b.branch as branch_id , SUM(b.stkqty) as quantity , b.refid as product_id , b.batchno as batch_no "
            + " FROM bin_details bd left join binstock b on b.ltrnno = bd.trn_no and b.binname = bd.binid and bd.batchno = b.batchno "
            + " WHERE  b.ltrnno =:trnNo "
            + " GROUP BY b.binname, b.batchno, b.refid , b.branch ", nativeQuery = true)
    List<BlendingBinList> findByTrnNo(@Param("trnNo") String trnNo);




}
