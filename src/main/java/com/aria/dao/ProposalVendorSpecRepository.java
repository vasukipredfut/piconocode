package com.aria.dao;

import java.util.List;
import java.util.Optional;

import com.aria.dto.ProposalVendorList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.ProposalVendorSpec;

@Repository
public interface ProposalVendorSpecRepository extends JpaRepository<ProposalVendorSpec, Long> {

	
	@Query(value = "SELECT * FROM proposal_vendor_spec p WHERE p.proposal_ref_id =:proposalid and p.proposal_spec_id =:specid and p.active_flag = 1 order by p.id ASC ", nativeQuery = true)
	List<ProposalVendorSpec> getProposalVendorspecbyids(@Param("proposalid") Long proposalid, @Param("specid") Long specid);
	
	
	@Query(value = "SELECT * FROM proposal_vendor_spec p WHERE p.proposal_ref_id =:proposalid and p.proposal_spec_id =:specid and p.active_flag = 1 ", nativeQuery = true)
	List<ProposalVendorSpec> getProposalVendorbyids(@Param("proposalid") Long proposalid, @Param("specid") Long specid);


	@Query(value = "SELECT * FROM proposal_vendor_spec p WHERE p.proposal_ref_id =:proposalid and p.vendor_id =:vendorid and p.active_flag = 1 order by p.id desc limit 1", nativeQuery = true)
	Optional<ProposalVendorSpec> getProposalVendorSpecById(@Param("proposalid") Long proposalid, @Param("vendorid") Long vendorid);

	@Query(value = "SELECT p.* FROM proposal_vendor_spec p left join proposal_spec ps on ps.id = p.proposal_spec_id WHERE p.proposal_ref_id =:proposalid and p.vendor_id =:vendorid and ps.specid =:specId and p.active_flag = 1 order by p.id desc limit 1", nativeQuery = true)
	Optional<ProposalVendorSpec> getProposalVendorSpecById(@Param("proposalid") Long proposalid, @Param("vendorid") Long vendorid, @Param("specId") Long specId);

	@Query(value = "SELECT * FROM proposal_vendor_spec p WHERE p.active = true AND p.proposal_ref_id =:proposalid and ( p.status ='Open' or p.status IS NULL )  and p.active_flag = 1 ", nativeQuery = true)
	List<ProposalVendorSpec> getProposalVendorSpecList(@Param("proposalid") Long proposalid);

	@Query(value = "SELECT * FROM proposal_vendor_spec p WHERE p.proposal_ref_id =:proposalid and p.proposal_spec_id =:specid and p.active_flag = 1 ", nativeQuery = true)
	List<ProposalVendorSpec> getProposalVendorspecbyids1(@Param("proposalid") Long proposalid, @Param("specid") Long specid);

	@Query(value = "SELECT * FROM proposal_vendor_spec p WHERE p.proposal_ref_id =:proposalid  AND p.proposal_spec_id =:proposalSpecId AND p.vendor_id =:vendorid and p.active_flag = 1 ", nativeQuery = true)
	Optional<ProposalVendorSpec> getProposalVendorSpecBySpecId(@Param("proposalid") Long proposalid, @Param("proposalSpecId") Long proposalSpecId, @Param("vendorid") Long vendorid);

	@Query(value = " SELECT p.vendor_id as vendorId, p.proposal_ref_id as proposalId, max(l.name) as vendorName , max(p.expiry_date) as expiryDate, " +
			" max(p.po_validity_request) as poValidityRequest FROM proposal_vendor_spec p left join ledger l on l.id = p.vendor_id " +
			" WHERE  p.proposal_ref_id =:proposalid and p.active_flag = 1 group by p.vendor_id , p.proposal_ref_id   ", nativeQuery = true)
	List<ProposalVendorList> getProposalVendorList(@Param("proposalid") Long proposalid);

	@Query(value = "SELECT * FROM proposal_vendor_spec p WHERE p.proposal_ref_id =:proposalid and p.vendor_id =:vendorid and p.active_flag = 1 ", nativeQuery = true)
	List<ProposalVendorSpec> getProposalVendor(@Param("proposalid") Long proposalid, @Param("vendorid") Long vendorid);

}
