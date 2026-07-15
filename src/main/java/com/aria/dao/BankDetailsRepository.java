package com.aria.dao;

import com.aria.model.BankDetails;
import com.aria.model.BillInward;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BankDetailsRepository extends JpaRepository<BankDetails, Long> {



    @Query(value = "SELECT  *  FROM bankdetail b   "
            + " WHERE (:bankName is null or lower(b.name) like ('%' || lower(:bankName) || '%')) "
            + " AND (:branchName is null or lower(b.branch) like ('%' || lower(:branchName) || '%')) "
            + " AND (:ifsc is null or lower(b.ifsc) like ('%' || lower(:ifsc) || '%')) ORDER BY b.id DESC ", nativeQuery = true)
    Page<BankDetails> findBy(@Param("bankName") String bankName,
                             @Param("branchName") String branchName,
                             @Param("ifsc") String ifsc , Pageable pageable );


    @Query(value = "SELECT  *  FROM bankdetail b   "
            + " WHERE (:bankName is null or lower(b.name) like ('%' || lower(:bankName) || '%')) "
            + " AND (:branchName is null or lower(b.branch) like ('%' || lower(:branchName) || '%')) "
            + " AND (:ifsc is null or lower(b.ifsc) like ('%' || lower(:ifsc) || '%')) ORDER BY b.id DESC ", nativeQuery = true)
    List<BankDetails> findBy(@Param("bankName") String bankName,
                             @Param("branchName") String branchName,
                             @Param("ifsc") String ifsc);

}
