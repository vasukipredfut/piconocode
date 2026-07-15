package com.aria.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.HtDayTrnViewProjection;
import com.aria.dto.ServiceTimingProjection;
import com.aria.model.HtDayTrn;

@Repository
public interface HtDayTrnRepository extends JpaRepository<HtDayTrn, Long> {

	@Query(value = "SELECT *FROM (SELECT DISTINCT ON (ht.shiftdate, ht.servid, ht.tsid)ht.shiftdate AS readingdate,\r\n"
			+ "        hm.name AS servicename,htt.name AS timeslap,ht.\"month\" AS month,ht.year AS year,ht.status AS status,\r\n"
			+ "        ht.con AS createddate,ht.servid,ht.tsid,ht.id FROM htdaytrn ht\r\n"
			+ "    LEFT JOIN htmasters hm ON hm.id = ht.servid\r\n"
			+ "    LEFT JOIN httslap htt  ON htt.id = ht.tsid\r\n" + "    WHERE ht.entposition = ''\r\n"
			+ "      AND (:shiftdate is null or ht.shiftdate=CAST(:shiftdate AS date))\r\n"
			+ "      AND (:serviceid IS NULL OR ht.servid = :serviceid)\r\n"
			+ "      AND (:timingid IS NULL OR ht.tsid = :timingid)\r\n"
			+ "      AND (:status IS NULL OR ht.status = :status)\r\n"
			+ "      AND (:month IS NULL OR ht.\"month\" = :month)\r\n"
			+ "      AND (:year IS NULL OR ht.year = :year)\r\n"
			+ "    ORDER by ht.shiftdate,ht.servid,ht.tsid,ht.id,ht.con DESC) t\r\n"
			+ "    where t.servicename is not null\r\n"
			+ "ORDER BY t.readingdate DESC\r\n", countQuery = "SELECT COUNT(*)\r\n" + "        FROM (\r\n"
					+ "            SELECT DISTINCT ON (ht.shiftdate, ht.servid, ht.tsid)\r\n"
					+ "                   ht.id\r\n" + "            FROM htdaytrn ht\r\n"
					+ "            WHERE ht.entposition = ''\r\n" + "        ) t", nativeQuery = true)
	Page<HtDayTrnViewProjection> findListUsingFilters(@Param("shiftdate") String shiftdate,
			@Param("serviceid") Long serviceid, @Param("timingid") Long timingid, @Param("month") Long month,
			@Param("year") Long year,@Param("status") String status, Pageable pageable);

	@Query(value = "select * from htdaytrn h where h.entposition ='L' and h.shiftdate=CAST(:shiftdate AS date) and h.servid =:serviceid and h.tsid=:timmingid", nativeQuery = true)
	List<HtDayTrn> findListdetails(@Param("shiftdate") String shiftdate, @Param("serviceid") Long serviceid,
			@Param("timmingid") Long timmingid);
	
	@Query(value="select ht.servid  as serviceid,hm.name as servicename,ht.tsid as timmingid,htt.name as timeslap\r\n"
			+ "from htdaytrn ht left join htmasters hm on hm.id=ht.servid left join httslap htt on htt.id=ht.tsid  \r\n"
			+ "where ht.servid=:serviceid and ht.tsid=:timmingid limit 1",nativeQuery = true)
	ServiceTimingProjection findServices(@Param("serviceid") Long serviceid,@Param("timmingid") Long timmingid);
	
	@Query(value="select shiftdate from htdaytrn where servid =:serviceid and tsid=:timmingid order by shiftdate desc limit 1;",nativeQuery = true)
	LocalDate getFromDate(@Param("serviceid") Long serviceid,@Param("timmingid") Long timmingid);

}
