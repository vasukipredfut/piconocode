package com.aria.dao;

import com.aria.dto.*;
import com.aria.model.BillInward;
import com.aria.model.InwardInspectionBatchList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillInwardRepository extends JpaRepository<BillInward, Long> {

    @Query(value = "SELECT  *  FROM bill_inward b WHERE b.billno =:billNo and b.bill_date =:date", nativeQuery = true)
    Optional<BillInward> findByBillNoAndDate(@Param("billNo") String billNo, @Param("date") Date date);

    @Query(value = "SELECT  *  FROM bill_inward b WHERE b.billno =:billNo and b.vendor_id=:vendorId ", nativeQuery = true)
    Optional<BillInward> findByBillNoAndVendorId(@Param("billNo") String billNo, @Param("vendorId") Long vendorId);

    @Query(value = "SELECT  *  FROM bill_inward b WHERE b.billno =:billNo and b.vendor_id=:vendorId and b.bill_date =:date", nativeQuery = true)
    Optional<BillInward> findByBillNoAndVendorIdBillDate(@Param("billNo") String billNo, @Param("vendorId") Long vendorId, @Param("date") Date date);

    @Query(value = "SELECT  *  FROM bill_inward  WHERE billno LIKE :referenceNumber", nativeQuery = true)
    List<BillInward> findByNumber(@Param("referenceNumber") String referenceNumber);


    @Query(value = " SELECT bi.received_date as inwardDate ,  bi.billno as billNo ,  inp.batch_no as batchNo , max(ic.bag) as noOfBag, max(b.lot) as lot, ic.recheck_log as entryType, "
            + " max(inp.id) as receivedId FROM bill_inward bi LEFT JOIN  bill_inward_product_item_received inp ON inp.inward_id = bi.id  "
            + " LEFT JOIN  inward_check ic ON ic.batch_no = inp.batch_no LEFT JOIN  binstock b on b.batchno = inp.batch_no and b.refid = inp.item_id  "
            + " WHERE  (inp.inspection_status IS NULL OR inp.inspection_status ='Open' )  AND ( DATE(b.inwarddate) = DATE(bi.received_date ) )"
            + " AND (:productId is null or inp.item_id = :productId) "
            + " AND ( DATE(bi.received_date) = DATE(:dateFrom) ) "
            + " GROUP BY bi.received_date  ,  bi.billno  ,  inp.batch_no , ic.recheck_log , inp.item_id ", nativeQuery = true)
    List<InwardInspectionBatchList> filterInwardInspectionBatchLists(@Param("productId") Long productId,
                                                                     @Param("dateFrom") Date dateFrom,
                                                                     Pageable pageable);

    @Query(value = "SELECT  *  FROM bill_inward bi LEFT JOIN ledger le ON le.id = bi.vendor_id WHERE "
            + " (:referenceNumber is null or lower(bi.billno) like ('%' || lower(:referenceNumber) || '%')) "
            + "AND (:vendorName is null or lower(le.name) like ('%' || lower(:vendorName) || '%')) "
            + "AND (:vendorId is null or le.id =:vendorId) "
            + "AND (:deliveryBranchId is null or bi.deliver_branch_id =:deliveryBranchId) ORDER BY bi.id DESC", nativeQuery = true)
    List<BillInward> findBy(@Param("referenceNumber") String referenceNumber, @Param("vendorName") String vendorName,
                            @Param("vendorId") Long vendorId, @Param("deliveryBranchId") Long deliveryBranchId);

    @Query(value = "SELECT  *  FROM bill_inward b WHERE b.ho_bill_id =:hoBillId AND  b.billno =:billNo AND b.vendor_id=:vendorId", nativeQuery = true)
    Optional<BillInward> findByBillNoAndVendorIdANDHoBillId(@Param("hoBillId") Long hoBillId,
                                                            @Param("billNo") String billNo, @Param("vendorId") Long vendorId);

    @Query(value = "SELECT  *  FROM bill_inward b WHERE b.ho_bill_id =:hoBillId ", nativeQuery = true)
    Optional<BillInward> findByHoBillId(@Param("hoBillId") Long hoBillId);

    @Query(value = "SELECT bi.*  FROM bill_inward bi LEFT JOIN branch inb ON inb.id = bi.inward_branch_id  LEFT JOIN ledger l ON  l.id = bi.vendor_id  LEFT JOIN  organisation org on org.id = bi.firm_org_id   "
            + " WHERE (:firmId is null or bi.firm_org_id =:firmId) "
            + " AND (:firmName is null or lower(org.name) like ('%' || lower(:firmName) || '%')) "
            + " AND (:referenceNumber is null or lower(bi.billno) like ('%' || lower(:referenceNumber) || '%')) "
            + " AND (:vendorId is null or l.id = :vendorId) "
            + " AND (:vendorName is null or  lower(l.name) like ('%' || lower(:vendorName) || '%') ) "
            + " AND (:inwardBranchId is null or bi.inward_branch_id = :inwardBranchId) "
            + " AND (:inwardBranchName is null or lower(inb.name) like ('%' || lower(:inwardBranchName) || '%')) "
            + " AND (:status is null or lower(bi.status) like ('%' || lower(:status) || '%')) "
            + " AND (cast(:dateFrom as timestamp) is null or (bi.received_date >= :dateFrom)) "
            + " AND (cast(:dateTo as timestamp) is null or (bi.received_date <= :dateTo)) order by bi.id DESC ", nativeQuery = true)
    Page<BillInward> filterBillInwardList(@Param("firmId") Long firmId, @Param("firmName") String firmName,
                                          @Param("referenceNumber") String referenceNumber, @Param("vendorId") Long vendorId,
                                          @Param("vendorName") String vendorName, @Param("inwardBranchId") Long inwardBranchId,
                                          @Param("inwardBranchName") String inwardBranchName, @Param("status") String status,
                                          @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, Pageable pageable);

    @Query(value = "SELECT bi.* FROM bill_inward bi left join bill_inward_product_item_received bip on   bi.id = bip.inward_id  where (cast (:inwardDate as date) is null OR bi.received_date =:inwardDate) AND bip.item_id IN (:itemId) order by bi.updated_date desc LIMIT 1 ", nativeQuery = true)
    Optional<BillInward> findByLorrySerialNoOnly(@Param("inwardDate") Date inwardDate,
                                                 @Param("itemId") List<Long> itemId);

    @Query(value = "SELECT  distinct bi.* FROM bill_inward bi left join bill_inward_product_item_received bip on   bi.id = bip.inward_id  where (cast (:inwardDate as date) is null OR bi.received_date =:inwardDate) AND bip.item_id  IN (:itemId)  AND (:lorryNo is null OR bi.lorryno =:lorryNo ) order by bi.updated_date desc ", nativeQuery = true)
    List<BillInward> findByLorrySerialNo(@Param("inwardDate") Date inwardDate, @Param("itemId") List<Long> itemId,
                                         @Param("lorryNo") String lorryNo);

    @Query(value = "SELECT  b.billno AS billNo , b.vendor_id AS vendorId, b.received_date AS inwardDate, "
            + " b.bill_date as billDate FROM bill_inward b " + "LEFT JOIN direct_purchase dp ON dp.bill_ref_id = b.id "
            + " WHERE (:billNo IS NULL OR  b.billno =:billNo ) " + "AND (:vendorId IS NULL OR b.vendor_id = :vendorId) "
            + " AND (:firmId IS NULL OR b.firm_org_id =:firmId)  AND (:branch IS NULL OR b.deliver_branch_id =:branch) "
            + " AND ( CAST(:inwardDate AS date) IS NULL OR  ( DATE(b.received_date) = DATE(:inwardDate) ) )"
            + " AND ( CAST(:billDate AS date) IS NULL OR  ( DATE(b.bill_date) = DATE(:billDate) ) )"
            + " AND (dp.stock_status = 'Authorised')", nativeQuery = true)
    List<CreditNoteBillList> findByCreditNoteBill(@Param("vendorId") Long vendorId, @Param("billNo") String billNo,
                                                  @Param("inwardDate") Date inwardDate, @Param("billDate") Date billDate,
                                                  @Param("branch") Long branch, @Param("firmId") Long firmId);

    @Query(value = " SELECT  b.billno as billNumber , b.vendor_id as vendorId, b.received_date as inwardDate , "
            + " ( case when b.created_by is null then b.updated_by else b.created_by end) as stockInwardBy ,  "
            + " ( case when b.created_date is null then b.updated_date else b.created_date end) as stockInwardDate ,  "
            + " gtc.cgst as cgstPercentage, bi.cgst as cgstAmount,gtc.igst  as igstPercentage, bi.igst as igstAmount, "
            + " gtc.sgst  as sgstPercentage, bi.sgst as sgstAmount , bi.item_id as productId , pm.name as productName, "
            + " bi.lot as lot , bi.received_quantity as quantity , "
            + " bi.final_order_price as unitRate FROM bill_inward b inner join bill_inward_product_item_received bi on "
            + " bi.inward_id = b.id  and bi.item_id  IS NOT NULL left join gst_tax_category gtc on gtc.item_id = bi.item_id  "
            + " LEFT JOIN ledger l on l.id = b.vendor_id LEFT JOIN  products_master pm ON pm.id=bi.item_id  "
            + " WHERE (:billNo is null or  b.billno =:billNo ) " + " AND (:vendorId is null or b.vendor_id =:vendorId) "
            + " AND (cast(:date as date) is null or ( DATE(b.received_date) =:date) ) ", nativeQuery = true)
    List<CreditNoteBillItemList> findByCreditNoteBillItems(@Param("vendorId") Long vendorId,
                                                           @Param("billNo") String billNo, @Param("date") Date date);

    @Query(value = " SELECT bi.id as billInwardId, bi.bill_date as billDate, bi.billno as billNo, bi.bill_amount as billAmount,  "
            + " bi.camp as place, bipir.unit_rate as unitRate, v.id as vendorId , v.name as vendorName,  b.name as deliverBranchName ,   "
            + " bipir.received_quantity  as qty from bill_inward bi left join ledger v on v.id = bi.vendor_id   "
            + " left join branch b on  b.id = bi.deliver_branch_id left join bill_inward_product_item_received bipir "
            + " on bipir.inward_id = bi.id WHERE (:billNo is null or  bi.billno =:billNo ) "
            + " AND (:vendorId is null or bi.vendor_id =:vendorId) "
            + " AND (cast(:billDate as timestamp) is null or (bi.bill_date =:billDate)) ", nativeQuery = true)
    JaggeryInspectionSubmitRes findBy(@Param("vendorId") Long vendorId, @Param("billNo") String billNo,
                                      @Param("billDate") Date billDate);

    @Query(value = "SELECT * from bill_inward bi  WHERE " + " (:vendorId is null or bi.vendor_id =:vendorId) "
            + " AND (cast(:billDate as timestamp) is null or (bi.bill_date =:billDate)) ", nativeQuery = true)
    List<BillInward> findBy(@Param("vendorId") Long vendorId, @Param("billDate") Date billDate);

    @Query(value = "SELECT  bi.* FROM bill_inward bi WHERE (:vendorId is null or bi.vendor_id =:vendorId) AND  bi.eway_bill =:ewayBill  order by bi.id desc limit 1", nativeQuery = true)
    Optional<BillInward> findByEwayBillNo(@Param("vendorId") Long vendorId, @Param("ewayBill") String ewayBill);

    @Query(value = "SELECT  distinct bi.* FROM bill_inward bi left join bill_inward_product_item_received bip on bi.id = bip.inward_id  where (cast (:inwardDate as date) is null OR bi.received_date =:inwardDate) AND ( bip.item_id IN (:itemId) ) AND (:lorryNo is null OR bi.lorryno =:lorryNo ) AND (:lorrySerialNo is null OR bi.lorry_serial_no =:lorrySerialNo ) order by bi.updated_date desc ", nativeQuery = true)
    List<BillInward> checkByLorrySerialNo(@Param("inwardDate") Date inwardDate, @Param("itemId") List<Long> itemId,
                                          @Param("lorrySerialNo") String lorrySerialNo, @Param("lorryNo") String lorryNo);

    @Query(value = " SELECT  bi.id as refTrnId, b.trnno as trnNo ,  dp.branch_id as issueBranchId, b.dept as deptId,  " +
            " :transactionType as trnName, bi.received_date as trnDate  " +
            " FROM bill_inward bi left join direct_purchase dp on dp.bill_ref_id = bi.id   " +
            " left join stbin b ON  b.billid= bi.id AND b.entry_type = 'Stock Inward'   " +
            " WHERE ( CAST(:shiftDate as date ) is null Or DATE(bi.received_date) =:shiftDate )   " +
            " AND (:branch is null or dp.branch_id =:branch )   " +
            " AND (:deptId is null or b.dept =:deptId )   " +
            " AND b.trnno IS NOT NULL AND ( b.sal_status IS NULL  OR b.sal_status <> 'Completed' )  " +
            " GROUP BY bi.id, b.trnno, dp.branch_id , b.dept , bi.received_date order by bi.id desc", nativeQuery = true)
    List<TransactionList> getTransactionListFilterBySTK_IN(@Param("shiftDate") Date shiftDate,
                                                           @Param("transactionType") String transactionType,
                                                           @Param("branch") Long branch, @Param("deptId") Long deptId);


    @Query(value = "select string_agg(bi.billno,',') as billNo from bill_inward bi where id in (:ids)", nativeQuery = true)
    String findByBillNo(@Param("ids") List<Long> ids);

    @Query(value = "SELECT  *  FROM bill_inward b WHERE b.billno =:billNo and b.vendor_id =:vendorId and b.received_date =:date limit 1 ", nativeQuery = true)
    Optional<BillInward> findByBillNoAndInwardDate(@Param("billNo") String billNo, @Param("vendorId") Long vendorId, @Param("date") Date date);


    @Query(value = "SELECT  distinct bi.* FROM bill_inward bi left join bill_inward_product_item_received bip on  bi.id = bip.inward_id  " +
            " where (cast (:inwardDate as date) is null OR bi.received_date =:inwardDate) " +
            " AND (:batchId is null OR bip.batch_id =:batchId ) AND (:batchNo is null OR bip.batch_no =:batchNo ) " +
            " order by bi.updated_date desc  limit 1", nativeQuery = true)
    Optional<BillInward> findByStockLorrySerialNo(@Param("inwardDate") Date inwardDate, @Param("batchId") Long batchId,
                                                  @Param("batchNo") String batchNo);


    @Query(value = "SELECT bi.id AS inwardId, bi.billno AS billNo, bi.deliver_branch_id AS branchId, " +
            "MAX(rb.name) AS branchName, bi.firm_org_id AS firmId, s.trnno AS trnNo, bi.received_date AS inwardDate " +
            "FROM bill_inward bi " +
            "LEFT JOIN branch rb ON rb.id = bi.deliver_branch_id " +
            "LEFT JOIN direct_purchase dp ON dp.bill_ref_id = bi.id " +
            "LEFT JOIN stbin s ON s.billid = bi.id " +
            "WHERE bi.product_category = 'RawMaterial' " +
            "AND dp.stock_status <> 'Completed' " +
            "AND s.trnno IS NULL " +
            "AND (CAST(:inwardDate AS DATE) IS NULL OR bi.received_date = CAST(:inwardDate AS DATE)) " +
            "AND (:branchId IS NULL OR bi.deliver_branch_id = CAST(:branchId AS BIGINT)) " +
            "GROUP BY bi.id, bi.billno, bi.deliver_branch_id, bi.firm_org_id, bi.received_date, s.trnno", 
            nativeQuery = true)
    List<JournalOrMrnNotClosingList> findByPending(@Param("inwardDate") Date inwardDate, 
                                                   @Param("branchId") Long branchId);



    @Query(
            value =
                    " SELECT " +
                            "   bi.id AS inwardId, " +
                            "   bi.billno AS billNo, " +
                            "   bi.deliver_branch_id AS branchId, " +
                            "   MAX(rb.name) AS branchName, " +
                            "   bi.firm_org_id AS firmId, " +
                            "   s.trnno AS trnNo, " +
                            "   bi.received_date AS inwardDate " +
                            " FROM bill_inward bi " +
                            " LEFT JOIN branch rb ON rb.id = bi.deliver_branch_id " +
                            " LEFT JOIN direct_purchase dp ON dp.bill_ref_id = bi.id " +
                            " LEFT JOIN stbin s ON s.billid = bi.id " +
                            " WHERE bi.product_category = 'RawMaterial' " +
                            "   AND dp.stock_status <> 'Completed' " +
                            "   AND s.trnno IS NULL " +
                            "   AND (CAST(:inwardDate AS DATE) IS NULL OR bi.received_date = CAST(:inwardDate AS DATE)) " +
                            "   AND bi.deliver_branch_id IN (:branchList) " +
                            " GROUP BY " +
                            "   bi.id, bi.billno, bi.deliver_branch_id, bi.firm_org_id, bi.received_date, s.trnno ",
            nativeQuery = true
    )
    List<JournalOrMrnNotClosingList> findByPending(
            @Param("inwardDate") Date inwardDate,
            @Param("branchList") List<Long> branchList
    );



    @Query(value = "SELECT  distinct bi.* FROM bill_inward bi " +
            " left join bill_inward_product_item_received bip on bi.id = bip.inward_id  " +
            " where (cast (:inwardDate as date) is null OR bi.received_date =:inwardDate) " +
            " AND ( :itemId is null or bip.item_id = :itemId ) " +
            " AND (:batchNo is null OR bip.batch_no = :batchNo ) LIMIT 1 ", nativeQuery = true)
    Optional<BillInward> findByInspectionDetails(@Param("inwardDate") Date inwardDate,
                                                 @Param("itemId") Long itemId,
                                                 @Param("batchNo") String batchNo);

    @Query(value = "SELECT  bi.id as id , bi.received_date as receivedDate , bi.bill_date as billDate , " +
            " bi.lorry_serial_no as lorrySerialNo , " +
            " bi.billno as billNo , bi.vendor_id as vendorId , le.name as vendorName " +
            " FROM bill_inward bi " +
            " LEFT JOIN direct_purchase dp on dp.vendor_id = bi.vendor_id and dp.bill_ref_id = bi.id " +
            " LEFT JOIN ledger le ON le.id = bi.vendor_id  " +
            " WHERE dp.excess_shortage = true AND dp.stock_status IN ('Authorised' , 'Completed') " +
            " AND (:billNo is null or lower(bi.billno) like ('%' || lower(:billNo) || '%')) " +
            " AND (:vendorId is null or le.id =:vendorId) " +
            " AND (:deliveryBranchId is null or bi.deliver_branch_id =:deliveryBranchId) ORDER BY bi.id DESC limit 15 ", nativeQuery = true)
    List<ProductDistruptancesBillInwardList> excessShortageBillInwardListWithLimit(@Param("billNo") String billNo,
                                                        @Param("vendorId") Long vendorId,
                                                        @Param("deliveryBranchId") Long deliveryBranchId);



    @Query(value = "SELECT  bi.id as id , bi.received_date as receivedDate , bi.bill_date as billDate ," +
            " bi.lorry_serial_no as lorrySerialNo , " +
            " bi.billno as billNo , bi.vendor_id as vendorId , le.name as vendorName " +
            " FROM bill_inward bi " +
            " LEFT JOIN direct_purchase dp on dp.vendor_id = bi.vendor_id and dp.bill_ref_id = bi.id " +
            " LEFT JOIN ledger le ON le.id = bi.vendor_id  " +
            " WHERE dp.excess_shortage = true AND dp.stock_status IN ('Authorised' , 'Completed') " +
            " AND (:billNo is null or lower(bi.billno) like ('%' || lower(:billNo) || '%')) " +
            " AND (:vendorId is null or le.id =:vendorId) " +
            " AND (:deliveryBranchId is null or bi.deliver_branch_id =:deliveryBranchId) ORDER BY bi.id DESC ", nativeQuery = true)
    List<ProductDistruptancesBillInwardList> excessShortageBillInwardListWithOutLimit(@Param("billNo") String billNo,
                                                                          @Param("vendorId") Long vendorId,
                                                                          @Param("deliveryBranchId") Long deliveryBranchId);


    @Query(value = "SELECT bi.id as id , bi.received_date as receivedDate , bi.bill_date as billDate , " +
            " bi.lorry_serial_no as lorrySerialNo , " +
            " bi.billno as billNo , bi.vendor_id as vendorId , le.name as vendorName " +
            " FROM bill_inward bi " +
            " LEFT JOIN direct_purchase dp on dp.vendor_id = bi.vendor_id and dp.bill_ref_id = bi.id " +
            " LEFT JOIN ledger le ON le.id = bi.vendor_id  " +
            " WHERE dp.stock_status IN ('Authorised' , 'Completed')  " +
            " AND (:billNo is null or lower(bi.billno) like ('%' || lower(:billNo) || '%')) " +
            " AND (:vendorId is null or le.id =:vendorId) " +
            " AND (:deliveryBranchId is null or bi.deliver_branch_id =:deliveryBranchId) ORDER BY bi.id DESC limit 15 ", nativeQuery = true)
    List<ProductDistruptancesBillInwardList> purchaseReturnBillInwardListWithLimit(@Param("billNo") String billNo,
                                                                          @Param("vendorId") Long vendorId,
                                                                          @Param("deliveryBranchId") Long deliveryBranchId);



    @Query(value = " SELECT bi.id as id , bi.received_date as receivedDate , bi.bill_date as billDate ," +
            " bi.lorry_serial_no as lorrySerialNo , " +
            " bi.billno as billNo , bi.vendor_id as vendorId , le.name as vendorName " +
            " FROM bill_inward bi " +
            " LEFT JOIN direct_purchase dp on dp.vendor_id = bi.vendor_id and dp.bill_ref_id = bi.id " +
            " LEFT JOIN ledger le ON le.id = bi.vendor_id  " +
            " WHERE dp.stock_status IN ('Authorised' , 'Completed') " +
            " AND (:billNo is null or lower(bi.billno) like ('%' || lower(:billNo) || '%')) " +
            " AND (:vendorId is null or le.id =:vendorId) " +
            " AND (:deliveryBranchId is null or bi.deliver_branch_id =:deliveryBranchId) ORDER BY bi.id DESC limit 30 ", nativeQuery = true)
    List<ProductDistruptancesBillInwardList> purchaseReturnBillInwardListWithOutLimit(@Param("billNo") String billNo,
                                                                                   @Param("vendorId") Long vendorId,
                                                                                   @Param("deliveryBranchId") Long deliveryBranchId);

    @Query(value = " SELECT bi.id AS billId,bi.bill_date AS bilDate,bi.billno AS billNo, bi.eway_bill AS eWayBil , bi.lorry_date AS lorryDate , bi.bill_amount AS billAmount , bi.lorryno AS lorryNo , "
        	+ "	bi.lorry_serial_no AS lorrySerialNo , v.name AS vendorName ,v.city AS vendorCity, ib.name AS inwardBranch , db.name AS deliverdBranch ,bi.camp AS camp , item.lot AS lot , item.item_name as productName , "
        	+ "	item.unit_rate AS unitRate , item.unit_cost AS unitCost ,item.received_quantity AS rcvQty , item.package_quantity AS pacQuantity ,ld.payable_freight_amount AS freightAmount , bi.received_date AS billInwarDate , "
        	+ " bi.status AS inwardStatus , bi.updated_date AS inwardCreatedDate , dp.updated_date  as stockInwardDate , dp.approved__date as approvedDate ,item.batch_no as batchNo , "
        	+ "	item.po_number AS poNumber ,  aud.status AS audiStatus , dp.stock_status AS status ,dp.id AS stockId  FROM bill_inward bi LEFT JOIN ledger v ON v.id = bi.vendor_id LEFT JOIN  branch ib ON ib.id= bi.inward_branch_id LEFT JOIN  branch db ON db.id= bi.deliver_branch_id  "
        	+ " left join direct_purchase dp ON dp.bill_ref_id = bi.id left join bill_inward_product_item_received item ON item.inward_id = bi.id  left join bill_lorry_details ld ON ld.inward_id = bi.id "
        	+ "      	LEFT JOIN ("
        	+ "   SELECT a.*\r\n"
        	+ "    FROM inward_audit a "
        	+ "    JOIN ( "
        	+ "        SELECT batch_no, MAX(id) AS max_id "
        	+ "        FROM inward_audit "
        	+ "        GROUP BY batch_no "
        	+ "    ) x ON a.id = x.max_id "
        	+ " ) aud ON aud.batch_no = item.batch_no "
        	+ " where (:lorryNo IS NULL OR lower(bi.lorry_serial_no) LIKE ('%' || lower(:lorryNo) || '%')) AND "
        	+ "  (cast(:dateInward as date) is null or (bi.received_date = :dateInward)) AND     "
        	+ "  (:inwardBranchName IS NULL OR lower(ib.name) LIKE ('%' || lower(:inwardBranchName) || '%')) AND "
        	+ "(cast(:dateBill as date) is null or ( bi.bill_date = :dateBill))  AND  "
        	+ "  (:billNo IS NULL OR lower(bi.billno) LIKE ('%' || lower(:billNo) || '%')) AND "
        	+ "  (:place IS NULL OR lower(v.city) LIKE '%' || lower(:place) || '%') AND "
        	+ "  (:vendorName IS NULL OR lower(v.name) LIKE ('%'  || lower(:vendorName) || '%')) AND "
        	+ "  (:rate IS NULL OR CAST(item.unit_rate AS text) LIKE ('%' || :rate || '%')) AND "
        	+ "  (:lot IS NULL OR item.lot LIKE ('%' || :lot || '%')) AND "
        	+ "  (:ewayBillNo IS NULL OR bi.eway_bill  LIKE ('%' || :ewayBillNo || '%')) AND "
        	+ "  (:quantity IS NULL OR CAST(item.package_quantity AS TEXT) LIKE('%' || :quantity || '%')) AND "        	
        	+ " (:amount IS NULL OR CAST(bi.bill_amount AS TEXT) LIKE ('%' || :amount || '%')) "
        	+ " AND (:month IS NULL OR EXTRACT(MONTH FROM bi.received_date) = :month) "
        	+ "  AND (:year IS NULL OR EXTRACT(YEAR FROM bi.received_date) = :year) AND "
        	+ "  (:deliverBranchId IS NULL OR bi.deliver_branch_id = :deliverBranchId) AND "
        	+ "  (:inwarBranchId IS NULL OR bi.inward_branch_id = :inwarBranchId) AND "
        	+ "  (:deliverBranchName IS NULL OR lower(ib.name) LIKE ('%' || lower(:deliverBranchName) || '%')) AND "
        	+ "  (:freightPaid IS NULL OR CAST(ld.payable_freight_amount AS TEXT) LIKE ('%' || :freightPaid || '%')) AND "
        	+ "   ( CAST(:dateUpdated  as date) IS NULL "
        	+ "      OR CAST(bi.updated_date AS DATE) = :dateUpdated "
        	+ "      OR CAST(dp.updated_date AS DATE) = :dateUpdated "
        	+ "      OR CAST(dp.approved__date AS DATE) = :dateUpdated ) AND "
        	+ "   (:productName IS NULL OR lower(item.item_name) LIKE ('%' || lower(:productName) || '%')) AND "        	
        	+ " LOWER(bi.product_category) = 'rawmaterial'"
        	+ " ORDER BY bi.id desc " ,nativeQuery = true)    
	Page<LorryArrivalDataList> filterLorryArrivalData(@Param("lorryNo") String lorryNo,@Param("inwarBranchId") Long inwarBranchId,@Param("month") Long month,@Param("year") Long year,@Param("deliverBranchId") Long deliverBranchId,@Param("dateInward") Date dateInward,@Param("inwardBranchName") String inwardBranchName,
			@Param("dateBill") Date dateBill,@Param("billNo") String billNo,@Param("place") String place,@Param("vendorName") String vendorName,@Param("rate") String rate,@Param("lot") String lot,@Param("ewayBillNo") String ewayBillNo,
			@Param("quantity")	String quantity,@Param("amount") String amount,@Param("freightPaid") String freightPaid,@Param("deliverBranchName") String deliverBranchName,@Param("dateUpdated")	Date dateUpdated ,@Param("productName")	String productName ,Pageable pageable);



}
