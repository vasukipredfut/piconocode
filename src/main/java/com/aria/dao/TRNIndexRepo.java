package com.aria.dao;


import com.aria.model.TRNIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TRNIndexRepo extends JpaRepository<TRNIndex, Long> {


    @Query(value = "SELECT *  FROM trnindex p WHERE p.prefix =:prefix limit 1", nativeQuery = true)
    Optional<TRNIndex> findByPrefix(@Param("prefix") String prefix);


    @Query(value = "SELECT *  FROM trnindex p WHERE p.prefix =:prefix AND p.compcode =:compCode limit 1", nativeQuery = true)
    Optional<TRNIndex> findByPrefix(@Param("prefix") String prefix, @Param("compCode") Long compCode);

}
