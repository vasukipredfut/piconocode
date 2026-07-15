package com.aria.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.TimingRawDataProjection;
import com.aria.dto.TimingReadingDto;
import com.aria.dto.WindmillTrnProjection;
import com.aria.dto.WindmillWorkingDayProjection;
import com.aria.model.WindmillTrn;

@Repository
public interface WindmillTrnRepository extends JpaRepository<WindmillTrn, Long> {

	@Query(value = "SELECT fromDate FROM windmilltrn WHERE windmillId =:id\r\n"
			+ "ORDER BY id desc LIMIT 1", nativeQuery = true)
	LocalDate getFromDate(@Param("id") Long id);

	@Query(value = "select t.id as timingid,t.name as timingname,a.name as attributename,a.id as attributeid, tr.openingReading as openingreading, \r\n"
			+ "			tr.closingReading as closingreading, tr.difference as difference\r\n"
			+ "			from WindmillTimming t left join WindmillTrn tr\r\n"
			+ "			on tr.timmingId = t.id left join WindmillAttributes a \r\n"
			+ "			on a.id = tr.attributeId where t.status = 'A'\r\n"
			+ "			and tr.windmillid=:windmillid and tr.fromdate=:fromdate\r\n"
			+ "			order by t.id, a.orderId", nativeQuery = true)
	List<TimingRawDataProjection> getAllTimingRawDatas(@Param("windmillid") Long windmillid,
			@Param("fromdate") LocalDate fromdate);

	@Query(value = "select * from windmilltrn where windmillid=:id and fromdate=CAST(:fromDate AS date) and attributeid=0 and timmingid=6", nativeQuery = true)
	public WindmillTrn findByWindmillIdAndFromdate(@Param("id") Long id, @Param("fromDate") LocalDate fromdate);
	
	@Query(value="select * from windmilltrn where windmillid=:id and fromdate=CAST(:fromDate AS date) and todate=CAST(:todate AS date) and attributeid=0 and timmingid=6",nativeQuery = true)
	public WindmillTrn findbyWindmillidandFromdateandTodate(@Param("id") Long id, @Param("fromDate") LocalDate fromdate,@Param("todate") LocalDate todate);

	// TODO(mariadb-port): PostgreSQL `SELECT DISTINCT ON (...)` is not supported on MariaDB.
	// Rewrote using a GROUP BY / subquery approach that picks the row with the latest createddate
	// per (windmillid, fromdate, todate). ILIKE -> LIKE with LOWER() wrapping;
	// CAST(... AS TEXT) -> CAST(... AS CHAR); `||` string concatenation -> CONCAT().
	@Query(value = "SELECT * FROM (\r\n"
			+ "  SELECT w.id, w.windmillid as windmillid, w.fromdate as fromdate, w.todate as todate, w.month as month, w.year as year, w.createddate as createddate, w.workingdays as workingdays, wm.`name` as plan, wm.`type` as sourcetype\r\n"
			+ "  FROM windmilltrn w LEFT JOIN windmill wm ON wm.id = w.windmillid\r\n"
			+ "  WHERE w.entposition !='L'\r\n"
			+ "  AND w.workingdays != 0\r\n"
			+ "  AND (:plan is null or w.windmillid=:plan) AND (:type is null or wm.type=:type) AND (:month is null or w.month=:month)\r\n"
			+ "  AND (:year is null or w.year=:year) AND (:createddate is null or w.createddate=CAST(:createddate AS date))\r\n"
			+ "  AND (:workingdays IS NULL OR LOWER(CAST(w.workingdays AS CHAR)) LIKE CONCAT('%', LOWER(:workingdays), '%'))\r\n"
			+ "  AND w.id = (SELECT w2.id FROM windmilltrn w2 WHERE w2.windmillid=w.windmillid AND w2.fromdate=w.fromdate AND w2.todate=w.todate ORDER BY w2.createddate DESC, w2.id DESC LIMIT 1)\r\n"
			+ ") t\r\n"
			+ "ORDER BY t.todate desc", countQuery = "SELECT COUNT(*) FROM (\r\n"
					+ "  SELECT w.id\r\n"
					+ "  FROM windmilltrn w\r\n"
					+ "  LEFT JOIN windmill wm ON wm.id = w.windmillid\r\n"
					+ "  WHERE w.entposition <> 'L'\r\n"
					+ "    AND (:plan IS NULL OR w.windmillid = :plan)\r\n"
					+ "    AND (:type IS NULL OR wm.type = :type)\r\n"
					+ "    AND (:month IS NULL OR w.month = :month)\r\n"
					+ "    AND (:year IS NULL OR w.year = :year)\r\n"
					+ "    AND (:createddate is null or w.createddate=CAST(:createddate AS date))\r\n"
					+ "    AND (:workingdays IS NULL OR LOWER(CAST(w.workingdays AS CHAR)) LIKE CONCAT('%', LOWER(:workingdays), '%'))\r\n"
					+ "    AND w.id = (SELECT w2.id FROM windmilltrn w2 WHERE w2.windmillid=w.windmillid AND w2.fromdate=w.fromdate AND w2.todate=w.todate ORDER BY w2.createddate DESC, w2.id DESC LIMIT 1)\r\n"
					+ ") c", nativeQuery = true)
	Page<WindmillWorkingDayProjection> findList(@Param("month") Long month, @Param("year") Long year,
			@Param("plan") Long plan, @Param("type") String type, @Param("createddate") String createddate,
			@Param("workingdays") Long workingdays, Pageable pageable);

	@Query(value = "select * from windmilltrn wt where wt.windmillid =:windmillid and wt.fromdate =:fromdate\r\n"
			+ " and wt.entposition='L'", nativeQuery = true)
	List<WindmillTrn> findlistForUpdate(@Param("windmillid") Long windmillid, @Param("fromdate") LocalDate fromdate);

	@Query(value = "select wt.timmingid as timmingid,wtt.name as timmingname,wt.attributeid as attributeid,wa.name as attributename,\r\n"
			+ "			wt.openingreading  as opening,wt.closingreading as closing,wt.difference as difference,wt.windmillid as windmillid,\r\n"
			+ "			w.name as plan,w.type as sourcetype,wt.fromdate as fromdate,wt.todate as todate,wt.workingdays as workingdays,wt.\"month\" as month,wt.\"year\" as year,\r\n"
			+ "			wt.createddate as createddate,wt.updatedby as updatedby from windmilltrn wt left join windmillattributes wa on wa.id=wt.attributeid \r\n"
			+ "			left join windmilltimming wtt on wtt.id=wt.timmingid left join windmill w on w.id=wt.windmillid\r\n"
			+ "			where wt.windmillid =:windmillid and wt.fromdate =:fromdate  and wt.todate =:todate", nativeQuery = true)
	List<WindmillTrnProjection> findWindmillDetailsById(@Param("windmillid") Long windmillid,
			@Param("fromdate")LocalDate fromdate,@Param("todate") LocalDate todate);

}
