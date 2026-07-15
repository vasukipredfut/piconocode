package com.aria.dao;

import java.util.List;

import com.aria.dto.ValidateSpec;
import com.aria.dto.VendorSpecList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.ProposalSpec;

@Repository
public interface ProposalSpecRepository extends JpaRepository<ProposalSpec, Long> {

	@Query(value = "SELECT * FROM proposal_spec p WHERE p.proposal_ref_id =:id and active_flag = 1 ORDER BY p.id ASC ", nativeQuery = true)
	List<ProposalSpec> getProposalspec(@Param("id") Long id);
	

	@Query(value = "SELECT ps.* FROM proposal_spec ps LEFT JOIN proposal p ON p.id = ps.proposal_ref_id WHERE (p.po_status IS NULL OR p.po_status='Open') AND p.category=39 AND ps.specid IN (:specId) and ps.spectype = :specType ", nativeQuery = true)
	List<ProposalSpec> validatespec(@Param("specType") Long specType, @Param("specId") List<Long> specId);

	@Query(value = " SELECT p.id as proposalId, ps.spectype as specType, ps.specid as specId , pv.vendor_id as vendorId, pv.status as vendorStatus   "
			+ " FROM proposal_spec ps LEFT JOIN proposal p on p.id =ps.proposal_ref_id LEFT JOIN proposal_vendor_spec pv on pv.proposal_spec_id = ps.id  "
			+ " WHERE p.category <> 41 AND p.status <> 'Closed' AND (p.po_status IS NULL OR p.po_status='Open') AND pv.active = true AND (pv.status IS NULL OR pv.status='Open') AND ps.spectype =:specType "
			+ " AND ps.specid =:specId AND pv.vendor_id =:vendorId AND ( :firmId is null or p.firm_id =:firmId )", nativeQuery = true)
	List<ValidateSpec> validateSpecWithVendor(@Param("specType") Long specType, @Param("specId") Long specId , @Param("vendorId") Long vendorId , @Param("firmId") Long firmId );

	@Query(value = "SELECT * from proposal_spec ps where ps.id in (select distinct pvs.proposal_spec_id from proposal_vendor_spec pvs where pvs.proposal_ref_id =:proposalId and pvs.vendor_id =:vendorId ) AND ps.proposal_ref_id =:proposalId", nativeQuery = true)
	List<ProposalSpec> getProposalspec(@Param("proposalId") Long proposalId, @Param("vendorId") Long vendorId );


	@Query(value = " select distinct ps.* from purchase_order po , purchase_order_items poi , proposal p , proposal_spec ps , proposal_vendor_spec pvs " +
			" where  po.proposal_id = :proposalId and po.vendor_id = :vendorId AND po.proposal_id  = p.id  and poi.purchase_order_id = po.id and ps.proposal_ref_id = p.id and " +
			" ps.spectype = poi.spec_type and ps.specid = poi.spec_id and pvs.vendor_id = po.vendor_id and pvs.proposal_ref_id = p.id   " , nativeQuery = true)
	List<ProposalSpec> getProposalspecList(@Param("proposalId") Long proposalId, @Param("vendorId") Long vendorId);

	@Query(value = " SELECT p.vendor_id as vendorId, p.proposal_ref_id as proposalId, p.proposal_spec_id as proposalSpecId , p.active as active, p.status as status, ps.specid as specId , "
			+ " ps.spectype as specType , p.net_approved_qty as netApprovedQty, p.net_approved_price as netApprovedPrice , p.lot as lot FROM "
			+ " proposal_vendor_spec p left join proposal_spec ps on p.proposal_spec_id = ps.id  WHERE p.active_flag = 1 AND p.proposal_ref_id =:proposalId and p.vendor_id = :vendorId  ", nativeQuery = true)
	List<VendorSpecList> getVendorSpec(@Param("proposalId") Long proposalId, @Param("vendorId") Long vendorId );




	@Query(value = " select distinct ps.*   from purchase_order po , purchase_order_items poi , proposal p , proposal_spec ps , proposal_vendor_spec pvs " +
			" where  po.proposal_id = :proposalId and po.vendor_id = :vendorId AND po.proposal_id  = p.id  and poi.purchase_order_id = po.id and ps.proposal_ref_id = p.id and " +
			" ps.spectype = poi.spec_type and ps.specid = poi.spec_id and pvs.vendor_id = po.vendor_id and pvs.proposal_ref_id = p.id   and ps.specid =:specId AND ps.spectype =:specType " , nativeQuery = true)
	List<ProposalSpec> getProposalSpecListWithVendor(@Param("proposalId") Long proposalId,  @Param("vendorId") Long vendorId,
													 @Param("specId") Long specId, @Param("specType") Long specType);



}
