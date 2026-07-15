package com.aria.dao;

import com.aria.model.BranchMapping;
import com.aria.model.Lot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchMappingRepository extends JpaRepository<BranchMapping, Long> {



    @Query(value = "select string_agg(cast(company as TEXT),',') as firmId from branch_Org_Map Where branch =:branch", nativeQuery = true)
    String findBy(@Param("branch") Long branch);

    @Query(value = "SELECT * FROM branch_Org_Map WHERE  branch =:branch AND company=:firmId Limit 1", nativeQuery = true)
    Optional<BranchMapping> findByCompany(@Param("branch") Long branch, @Param("firmId") Long firmId);


    @Query(value = "SELECT * FROM branch_Org_Map WHERE  branch =:branch AND company=:firmId ", nativeQuery = true)
    List<BranchMapping> findBy(@Param("branch") Long branch, @Param("firmId") Long firmId);
    
}
