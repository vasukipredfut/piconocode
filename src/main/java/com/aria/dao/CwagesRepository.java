package com.aria.dao;

import com.aria.dto.CWagesDTOList;
import com.aria.dto.CwagesStructureList;
import com.aria.dto.cwagesList;
import com.aria.model.Cwages;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CwagesRepository extends JpaRepository<Cwages, Long> {


    @Query(value = "SELECT *  FROM  cwages c WHERE "
            + "  (:name is null or (lower(c.name) LIKE ('%' || lower(:name) || '%')) ) "
            + " AND (:journal is null or c.journal=:journal ) "
            + " AND (:codeno is null OR c.codeno =:codeno)  "
            + " AND (:type is null OR c.type =:type)  ", nativeQuery = true)
    Page<Cwages> findAll(@Param("name") String name,
                         @Param("journal") Long journal,
                         @Param("type") Long type,
                         @Param("codeno") String codeno,
                         Pageable pageable);


    @Query(value = "SELECT c.id as id , c.salstruid as salId, c.codeno as codeNo, c.designation as designation , c.trnyear as trnYear ,   " +
            " c.name as name, c.amount as cost, c.journal as journal, c.mvmt as materialMovement, c.activity as activityBased    " +
            " FROM  cwages c  LEFT JOIN salary_structure s ON s.id=c.salstruid and s.status='A' WHERE   " +
            " (:name is null or (lower(c.name) LIKE ('%' || lower(:name) || '%')) )   " +
            " AND (:designation is null or (lower(c.designation) LIKE ('%' || lower(:designation) || '%')) )   " +
            " AND (:status is null or (lower(c.activity) LIKE ('%' || lower(:status) || '%')) )   " +
            " AND (:journal is null or c.journal=:journal )   " +
            " AND (:codeno is null OR c.codeno =:codeno)    " +
            " AND (:year is null OR c.trnyear =:year)    " +
            " AND (:type is null OR c.type =:type)", nativeQuery = true)
    Page<CWagesDTOList> findBy(@Param("name") String name,
                               @Param("journal") String journal,
                               @Param("type") Long type,
                               @Param("codeno") String codeno,
                               @Param("designation") String designation,
                               @Param("status") String status,
                               @Param("year") Long year,
                               Pageable pageable);



    @Query(value = "SELECT c.id as id , c.salstruid as salId, c.codeno as codeNo, c.designation as designation , c.trnyear as trnYear ,   " +
            " c.name as name, c.amount as cost, c.journal as journal, c.mvmt as materialMovement, c.activity as activityBased    " +
            " FROM  cwages c  LEFT JOIN salary_structure s ON s.id=c.salstruid and s.status='A' WHERE   " +
            " (:name is null or (lower(c.name) LIKE ('%' || lower(:name) || '%')) )   " +
            " AND (:designation is null or (lower(c.designation) LIKE ('%' || lower(:designation) || '%')) )   " +
            " AND (:status is null or (lower(c.activity) LIKE ('%' || lower(:status) || '%')) )   " +
            " AND (:journal is null or c.journal=:journal )   " +
            " AND (:codeno is null OR c.codeno =:codeno)    " +
            " AND (:year is null OR c.trnyear =:year)    " +
            " AND (:type is null OR c.type =:type)", nativeQuery = true)
    List<CWagesDTOList> findBy(@Param("name") String name,
                               @Param("journal") String journal,
                               @Param("type") Long type,
                               @Param("codeno") String codeno,
                               @Param("designation") String designation,
                               @Param("status") String status,
                               @Param("year") Long year);



    @Query(value = "SELECT c.id as id,  max(c.codeno) as codeNo,  max(c.salstruid) as salStructId, max(ss.name) as designation , "
            + " c.trnyear as trnYear, c.name as name, max(c.amount) as amount, max(c.journal) as journal, max(c.mvmt) as materialMovement,"
            + " max(c.tport) as transport, max(c.usecid) as userId, max(c.type) as type, max(c.slab) as slab , max(c.activity) as activity "
            + " FROM cwages c left join salary_structure ss on ss.id = c.salstruid   WHERE  (:year is null or c.trnyear=:year ) "
            + " group by c.id, c.name, c.trnyear", nativeQuery = true)
    List<CwagesStructureList> findBy(@Param("year") Long year);


    @Query(value = "SELECT * FROM cwages c "
            + " WHERE (:trnYear is null OR c.trnyear =:trnYear) "
            + " AND  (:status is null or (lower(c.activity) LIKE ('%' || lower(:status) || '%')) ) "
            + " AND  (:designationId is null or c.salstruid =:designationId) ", nativeQuery = true)
    List<Cwages> findByRejectOrVerifyList(@Param("trnYear") Long trnYear,
                                          @Param("status") String status,
                                          @Param("designationId") Long designationId);


    @Query(value = "SELECT c.id as id,  max(c.codeno) as codeNo,  max(c.salstruid) as salStructId, max(ss.name) as designation , "
            + " c.trnyear as trnYear, c.name as name, max(c.amount) as amount, max(c.journal) as journal, max(c.mvmt) as materialMovement,"
            + " max(c.tport) as transport, max(c.usecid) as userId, max(c.type) as type, max(c.slab) as slab , max(c.activity) as activity "
            + " FROM cwages c left join salary_structure ss on ss.id = c.salstruid  "
            + " WHERE  (:year is null or c.trnyear=:year ) AND c.activity=:status "
            + " group by c.id, c.name, c.trnyear", nativeQuery = true)
    List<CwagesStructureList> findBy(@Param("year") Long year, @Param("status") String status);


    @Query(value = "SELECT DISTINCT cw.id as cwagesId, cw.name as cwagesName, "
            + " cw.codeno as codeNo, cw.amount as amount, cw.salstruid as salStrucId,  ss.name as designationName "
            + " FROM cwages cw  JOIN acwages ac ON cw.id = ac.cwagesid "
            + " JOIN salary_structure ss ON cw.salstruid = ss.id "
            + " WHERE ac.refid = :refId AND CASE "
            + " WHEN :transactionType = 'INTRA' THEN ac.intra "
            + " WHEN :transactionType = 'INTER_IN' THEN ac.interin "
            + " WHEN :transactionType = 'INTER_OUT' THEN ac.interout "
            + " WHEN :transactionType = 'STK_IN' THEN ac.stkin "
            + " WHEN :transactionType = 'STK_OUT' THEN ac.stkout "
            + " WHEN :transactionType = 'JOURNAL' THEN ac.ajournal "
            + " WHEN :transactionType = 'EXT_IN' THEN ac.extin "
            + " WHEN :transactionType = 'EXT_OUT' THEN ac.extout "
            + " WHEN :transactionType = 'QC_PROCESS' THEN ac.qcproce "
            + " ELSE 'NO' END = 'YES'", nativeQuery = true)
    List<cwagesList> getCwagesFilterListByTransactionType(@Param("refId") Long refId,
                                                          @Param("transactionType") String transactionType);

}
