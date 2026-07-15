package com.aria.dao;


import com.aria.dto.CreditNoteList;
import com.aria.model.CreditNoteBill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



@Repository
public interface CreditNoteBillRepository extends JpaRepository<CreditNoteBill,Long> {




    @Query(value = " SELECT  cb.id as id , cb.vendor_id as vendorId ,l.name as vendorName , cb.credit_note_number as creditNoteNumber, cb.credit_bill_date as creditNoteDate, "
            + " cb.credit_note_quantity as creditQuantity, cb.type as type, cb.credit_amount as creditAmount, cb.status as status, "
            + " cb.credit_category as billType,   regexp_split_to_table(cb.inward_bill_Number, E',') AS creditNoteBillNumber , "
            + " regexp_split_to_table(cb.bill_inward_Date, E',') AS billInwardDate from credit_bill cb left join ledger l on l.id = cb.vendor_id WHERE "
            + " (:billNumber is null or (lower(cb.inward_bill_Number) LIKE ('%' || lower(:billNumber) || '%'))) "
            + " AND (:firmId IS NULL OR cb.inward_company =:firmId)  AND (:branch IS NULL OR cb.inward_branch =:branch) "
            + " AND (:vendorName is null or (lower(l.name) LIKE ('%' || lower(:vendorName) || '%'))) "
            + " AND (:status is null or (lower(cb.status) LIKE ('%' || lower(:status) || '%'))) "
            + " AND (:type is null or cb.type = :type) ORDER BY cb.id DESC ", nativeQuery = true)
    Page<CreditNoteList> findBy(@Param("billNumber") String billNumber,
                                @Param("vendorName") String vendorName,
                                @Param("type") String type,
                                @Param("status") String status,
                                @Param("branch") Long branch,
                                @Param("firmId") Long firmId ,
                                Pageable pageable);


}
