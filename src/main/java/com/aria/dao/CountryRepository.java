package com.aria.dao;


import com.aria.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository <Country, Long> {




    @Query(value = "SELECT c.* FROM country c left join ledger l on l.countryid = c.id  WHERE l.id = :partyId and countryid IS NOT NULL and countryid <> 0 limit 1", nativeQuery = true)
    Optional<Country> findBy(@Param("partyId") Long partyId);


}
