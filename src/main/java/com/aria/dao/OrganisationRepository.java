package com.aria.dao;


import com.aria.dto.CompanyList;
import com.aria.model.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, Long> {

    @Query(value = "SELECT * FROM organisation WHERE lower(name) like ('%' || lower(:name) || '%') Limit 1", nativeQuery = true)
    Optional<Organisation> findBy(@Param("name") String name);

    @Query(value = " SELECT o.id as id, o.name as name, oc.address as address, o.city as city , o.fax as fax , oc.pincode as pinCode, " +
            " o.gst as GST , o.email as email, o.phone as phoneNo , o.state as state FROM organisation o  " +
            " left join  our_company_address oc on oc.company = o.id and oc.branch = 10 WHERE  o.id =:id ", nativeQuery = true)
    CompanyList findByFirm(@Param("id") Long id);



    @Query(value = " SELECT o.id as id, o.name as name, o.address as address, o.city as city , o.fax as fax , oc.pincode as pinCode, " +
            " o.gst as GST , o.email as email, o.phone as phoneNo , o.state as state FROM organisation o  " +
            " left join  our_company_address oc on oc.company = o.id and oc.branch = 10 WHERE  o.id =:id ", nativeQuery = true)
    CompanyList findByFirmWithOrgAddress(@Param("id") Long id);
}
