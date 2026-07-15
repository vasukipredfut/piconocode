
package com.aria.dao;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.BranchStockList;
import com.aria.dto.DayEndStockList;
import com.aria.model.DayEndStockClosingSummary;


import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface DayEndStockClosingSummaryRepository extends JpaRepository<DayEndStockClosingSummary, Long> {


    @Query(value = "select d.today_closing as closingStock from day_end_stock_closing_summary  d  WHERE "
            + "  (:productId is null or d.item_id = :productId) "
            + " AND (:branch is null or d.branch = :branch) "
            + "  AND (cast(:adate as date) is null or (d.adate =:adate)) ", nativeQuery = true)
    Double getCosingStock(@Param("branch") Long branch, @Param("productId") Long productId, @Param("adate") Date adate);


    @Query(value = "select * from day_end_stock_closing_summary  d   "
            + " WHERE (:branch is null or d.branch = :branch) "
            + " AND (cast(:adate as date) is null or (d.adate =:adate)) ", nativeQuery = true)
    List<DayEndStockClosingSummary> findBy(@Param("branch") Long branch, @Param("adate") Date adate);


    @Query(value = "select * from day_end_stock_closing_summary  d   "
            + " WHERE (:branch is null or d.branch = :branch) "
            + " AND (cast(:adate as date) is null or (d.adate =:adate)) LIMIT 1", nativeQuery = true)
    Optional<DayEndStockClosingSummary> findByDateAndBranch(@Param("branch") Long branch, @Param("adate") Date adate);


    @Query(value = "select * from day_end_stock_closing_summary  d   "
            + " WHERE  d.status = 'Completed' "
            + " AND (:branch is null or d.branch = :branch)  "
            + " AND (cast(:adate as date) is null or (d.adate =:adate)) "
            + " ORDER BY d.id DESC LIMIT 1", nativeQuery = true)
    Optional<DayEndStockClosingSummary> findByCompletedDateAndBranch(@Param("branch") Long branch, @Param("adate") Date adate);


    @Query(value = "select * from day_end_stock_closing_summary  d   "
            + " WHERE (:branch is null or d.branch = :branch) "
            + " AND (:itemId is null or d.item_id = :itemId) "
            + " AND (cast(:adate as date) is null or (d.adate =:adate)) LIMIT 1", nativeQuery = true)
    Optional<DayEndStockClosingSummary> findByDateAndBranchAndItem(@Param("branch") Long branch, @Param("itemId") Long itemId, @Param("adate") Date adate);


    @Transactional
    @Modifying
    @Query(value = "Update day_end_stock_closing_summary  SET status = 'Completed' , updated_date =:dateTime "
            + " WHERE (:branch is null or branch = :branch) AND (cast(:adate as date) is null or (adate =:adate)) ", nativeQuery = true)
    void closeDayEnd(@Param("branch") Long branch, @Param("adate") Date adate, @Param("dateTime") LocalDateTime dateTime);


    @Query(value = "select * from day_end_stock_closing_summary  d   "
            + " WHERE (:branch is null or d.branch = :branch) AND d.status = 'Completed' "
            + " AND (cast(:adate as date) is null or (d.adate =:adate)) LIMIT 1", nativeQuery = true)
    Optional<DayEndStockClosingSummary> checkCloseOrNot(@Param("branch") Long branch, @Param("adate") Date adate);


    @Query(value = "select * from day_end_stock_closing_summary  d   "
            + " WHERE (:branch is null or d.branch = :branch) AND d.status = 'Completed' "
            + " ORDER BY d.id DESC LIMIT 1", nativeQuery = true)
    Optional<DayEndStockClosingSummary> checkLastDate(@Param("branch") Long branch);

    @Query(value = "SELECT dec.branch   AS branchId, SUM(dec.today_closing * fc.conversion) AS totalValue "
            + "FROM finclose fc JOIN day_end_stock_closing_summary dec ON dec.item_id = fc.refid WHERE (:orderNo is null or fc.parentid  = :orderNo) "
            + "  AND (:tableId is null or fc.gparentid = :tableId) AND  (cast(:adate as date) is null or (dec.adate =:adate))  GROUP BY dec.branch ", nativeQuery = true)
    List<Object[]> getClosedCosingStock(@Param("tableId") Long tableId, @Param("orderNo") Long orderNo, @Param("adate") Date adate);

    @Query(value = "SELECT dec.branch   AS branchId, SUM(dec.today_closing * fc.conversion) AS totalValue "
            + "FROM finclose fc JOIN day_end_stock_closing_summary dec ON dec.item_id = fc.refid WHERE (:orderNo is null or fc.parentid  = :orderNo) "
            + "  AND (:tableId is null or fc.gparentid = :tableId) AND  (:month IS NULL OR EXTRACT(MONTH FROM dec.adate) = :month) "
            + "  AND (:year IS NULL OR EXTRACT(YEAR FROM dec.adate) = :year)  GROUP BY dec.branch ", nativeQuery = true)
    List<Object[]> getClosedCosingStockByMonth(@Param("tableId") Long tableId, @Param("orderNo") Long orderNo, @Param("month") Long month, @Param("year") Long year);

    @Query(value = "SELECT DISTINCT b.id as id, b.name as branchName "
    		+ " FROM branch b "
    		+ " JOIN day_end_stock_closing_summary de "
    		+ "     ON de.branch = b.id left join day_end_closing_branch_group c on c.branch_id =b.id "
    		+ "    WHERE c.branch_id  <> :branch ", nativeQuery = true)
    List<BranchStockList> findDayEndClosedBranches( Long branch);


    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            "UPDATE DayEndStockClosingSummary d " +
                    "SET d.status = 'Completed', d.updated_date = :dateTime " +
                    "WHERE d.branch IN :branchList " +
                    "AND d.adate = :adate"
    )
    int closeDayEnd(List<Long> branchList, Date adate, LocalDateTime dateTime);


    @Query(value = "select d.today_closing as qty , d.item_id as itemId from day_end_stock_closing_summary  d  WHERE "
            + "   d.item_id IN(:productId) "
            + " AND (:branch is null or d.branch = :branch) "
            + "  AND (cast(:adate as date) is null or (d.adate =:adate)) ", nativeQuery = true)
    List<DayEndStockList> getClosingStock(@Param("branch") Long branch, @Param("productId") List<Long> productId, @Param("adate") Date adate);

    @Query(value = "SELECT DISTINCT b.id as id, b.name as branchName "
    		+ "  FROM branch b  JOIN day_end_stock_closing_summary de "
    		+ "  ON de.branch = b.id left join day_end_closing_branch_group db on db.branch_id =de.branch  where db.branch_id is not null  ", nativeQuery = true)
	List<BranchStockList> findDayEndClosedBranchesList();

    @Query(value = " select b.branch_id  from day_end_stock_closing_summary  d   left join day_end_closing_branch_group b on b.branch_id =d.branch "
    		+ "  WHERE d.branch IN (:branch) and d.status ='Completed' and  b.branch_id is not null "
    		+ "  AND (cast(:adate as date) is null or (d.adate =:adate)) group by b.branch_id  ", nativeQuery = true)
    List<Long> findByClosedBranchDetails(@Param("branch") List<Long> branch, @Param("adate") Date adate);

    @Query(value = "select COALESCE(SUM(d.today_closing), 0) AS qty , d.item_id as itemId, d.branch as branchId from day_end_stock_closing_summary  d  WHERE "
            + "   d.item_id IN(:productId)  AND (cast(:adate as date) is null or (d.adate =:adate)) group by d.branch ,d.item_id ", nativeQuery = true)
    List<DayEndStockList> getClosingStockforall( @Param("productId") List<Long> productId, @Param("adate") Date adate);


}
