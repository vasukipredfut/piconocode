package com.aria.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.Proposal;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {

	@Query(value = "SELECT p.* FROM proposal p, proposal_spec ps "
			+ " WHERE p.active= :active  AND p.id=ps.proposal_ref_id "
			+ " AND ( :proposalId is null or p.id =:proposalId) "
			+ " AND ( :category is null or p.category =:category)  "
			+ " AND (:status is null or p.status =:status) "
			+ " AND (:specType is null or ps.spectype =:specType) "
			+ " AND (:specId is null or ps.specid =:specId) "
			+ " AND (:frimId is null or p.firm_id = :frimId) "
			+ " AND (:branch is null or p.branch_id = :branch) "
			+ " AND (:dept is null or p.dept_id = :dept) "
			+ " GROUP BY p.id ORDER BY p.id DESC", nativeQuery = true)
	Page<Proposal> getProposals(@Param("status") String Proposal, @Param("proposalId") Long proposalId, @Param("specId") Long specId,
			@Param("active") Long active, Pageable page, @Param("category") Long category,
			@Param("specType") Long specType, @Param("frimId") Long frimId, @Param("dept") Long dept,
			@Param("branch") Long branch);

}
