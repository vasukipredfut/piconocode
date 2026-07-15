package com.aria.dao;

import com.aria.model.OurCompanyAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OurCompanyAddressRepository extends JpaRepository<OurCompanyAddress,Long> {

    @Query(value = "SELECT * FROM our_company_address WHERE company=:company AND branch=10 limit 1 ", nativeQuery = true)
    Optional<OurCompanyAddress> findBy(@Param("company") Long company);
}
