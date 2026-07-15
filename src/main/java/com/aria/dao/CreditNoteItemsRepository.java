package com.aria.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.CreditNoteItems;

@Repository
public interface CreditNoteItemsRepository extends JpaRepository<CreditNoteItems, Long> {

	@Query(value = "SELECT  *  FROM creditnote_items b WHERE b.bill_number =:billNo and b.credit_note_id =:noteId", nativeQuery = true)
	Optional<CreditNoteItems> findByBillNoAndNoteId(@Param("billNo") String billNo, @Param("noteId") Long noteId);

	List<CreditNoteItems> findByCreditNoteId(Long creditNoteId);


	/*@Query(value = "SELECT  *  FROM creditnote_items b WHERE b.bill_number =:billNo and b.credit_note_id =:noteId", nativeQuery = true)
	List<CreditNoteItemsList> findBy(@Param("creditNoteId") Long creditNoteId);*/
}
