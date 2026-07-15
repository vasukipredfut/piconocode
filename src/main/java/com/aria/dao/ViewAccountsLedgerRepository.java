package com.aria.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.ViewAccountLedgerList;
import com.aria.model.ViewAccountsLedger;

@Repository
public interface ViewAccountsLedgerRepository extends JpaRepository<ViewAccountsLedger, Long> {

	@Query(value = "SELECT al.id as id ,  al.trnno as refNo,  al.trn_amount as amount , al.trn_Type as trnType , al.trn_date  as date , "
			+ " al.ref_date  as refDate, al.party_id as partyId, al.description as narration  FROM view_accounts_ledger al "
			+ " WHERE al.trn_date IS NOT NULL " + " AND (:company IS NULL OR al.company = :company) "
			+ " AND (:partyId IS NULL OR al.party_id = :partyId) "
			+ " AND ( cast(:dateFrom as date) is null or (al.trn_date >= :dateFrom) ) "
			+ " AND ( cast(:dateTo as date) is null or (al.trn_date <= :dateTo) ) order by al.id asc  ", nativeQuery = true)
	Page<ViewAccountLedgerList> getAccountsLedgerList(@Param("company") Long company, @Param("partyId") Long partyId,
			@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, Pageable pageable);

	@Query(value = "SELECT al.id as id , al.trnno as refNo,  al.trn_amount as amount , al.trn_Type as trnType , al.trn_date  as date , "
			+ " al.ref_date  as refDate, al.party_id as partyId, al.description as narration  FROM view_accounts_ledger al "
			+ " WHERE al.trn_date IS NOT NULL " + " AND (:partyId IS NULL OR al.party_id = :partyId) "
			+ " AND (cast(:dateFrom as date) is null or (al.trn_date >= :dateFrom)) "
			+ " AND (cast(:dateTo as date) is null or (al.trn_date <= :dateTo)) order by al.id asc ", nativeQuery = true)
	List<ViewAccountLedgerList> getAccountsLedgerList(@Param("partyId") Long partyId, @Param("dateFrom") Date dateFrom,
			@Param("dateTo") Date dateTo);

	List<ViewAccountsLedger> findByTrnno(String trnNo);

	ViewAccountsLedger findByTrnnoAndTrnType(String trnNo, Long trnType);

	@Query(value = "SELECT * FROM view_accounts_ledger al WHERE (:trnNo IS NULL OR al.trnno=:trnNo )  AND (:partyId IS NULL OR al.party_id =:partyId) ", nativeQuery = true)
	Optional<ViewAccountsLedger> findByTrnNoANDPartyId(@Param("partyId") Long partyId, @Param("trnNo") String trnNo);

	@Query(value = "select coalesce (sum( CAST (trn_amount as double precision ) ),0.0 ) as amount from view_accounts_ledger val where (:company IS NULL OR val.company = :company)  AND (:partyId IS NULL OR val.party_id =:partyId) AND DATE(val.trn_date) < DATE(:dateFrom) AND val.trn_type in(2,3,4) ", nativeQuery = true)
	double debitAmount(@Param("company") Long company, @Param("partyId") Long partyId,
			@Param("dateFrom") Date dateFrom);

	@Query(value = "select coalesce (sum( CAST (trn_amount as double precision ) ),0.0 ) as amount from view_accounts_ledger val where (:company IS NULL OR val.company = :company)  AND (:partyId IS NULL OR val.party_id =:partyId) AND DATE(val.trn_date) < DATE(:dateFrom)  AND val.trn_type NOT IN (2,3,4) ", nativeQuery = true)
	double creditAmount(@Param("company") Long company, @Param("partyId") Long partyId,
			@Param("dateFrom") Date dateFrom);

}
