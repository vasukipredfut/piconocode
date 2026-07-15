package com.aria.dao;


import com.aria.model.ProfessionalTaxMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfessionalTaxMasterRepository extends JpaRepository<ProfessionalTaxMaster, Long> {

    @Query(value = "SELECT * from professional_tax_master pt where (:salary between pt.salaryrange_from  AND pt.salaryrange_to ) AND pt.branch =1 AND pt.status ='A' ", nativeQuery = true)
    Optional<ProfessionalTaxMaster> findBy(@Param("salary") double salary);

}
