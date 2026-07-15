package com.aria.dao;


import com.aria.model.DeliteBinStockQuantity;
import com.aria.model.DeliteStockMoveRequestBin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeliteStockMoveRequestBinRepository extends JpaRepository<DeliteStockMoveRequestBin, Long> {

    @Query(value = " SELECT b.branch as branch, b.dept as dept, b.refId as itemId, b.lot as lot, sum(b.stkqty ) as quantity  " +
            " FROM  delitestock_moverequest_bin dm LEFT JOIN binstock b on b.id = dm.binstock_id " +
            " WHERE (:deliteStockMoverequestId is null or dm.delitestock_moverequest_id = :deliteStockMoverequestId) AND b.stkqty > 0 " +
            " group by b.branch , b.dept , b.refId , b.lot", nativeQuery = true)
    List<DeliteBinStockQuantity> getDeliteBinStockListGroupByBranchDeptItemLot(@Param("deliteStockMoverequestId") Long deliteStockMoverequestId);


    @Query(value = "  SELECT distinct b.binstock_id FROM delitestock_moverequest dm LEFT JOIN delitestock_moverequest_bin b on b.delitestock_moverequest_id = dm.id " +
            " WHERE dm.material_request_id = :materialRequestId ", nativeQuery = true)
    List<Long> deliteBinStockIdList(@Param("materialRequestId") Long materialRequestId);




}
