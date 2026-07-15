package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.ProposalQuotationEmbedded;
import com.aria.model.ProposalQuotationFile;

@Repository
public interface ProposalQuotationFileRepo extends JpaRepository<ProposalQuotationFile, Long> {

	@Query(value = "select * from proposal_quotation_file where proposal_id= :proposalId and vendor_id= :vendorId", nativeQuery = true)
	ProposalQuotationFile getProposalQuotation(@Param("proposalId") Long proposalId, @Param("vendorId") Long vendorId);

	@Query(value = "select * from proposal_quotation_file where proposal_id= :proposalId", nativeQuery = true)
	List<ProposalQuotationFile> ListProposalsQuotation(@Param("proposalId") Long proposalId);

	ProposalQuotationFile findByProposalId(Long proposalId);

}
