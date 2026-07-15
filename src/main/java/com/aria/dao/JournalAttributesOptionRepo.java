package com.aria.dao;


import com.aria.model.JournalAttributesOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalAttributesOptionRepo extends JpaRepository<JournalAttributesOption, Long> {

    @Query(value = "SELECT * FROM journal_attributes_option pm WHERE pm.attributes_id =:attId and pm.attributes_map_id =:mapId ", nativeQuery = true)
    List<JournalAttributesOption> findByAttributesOption(@Param("mapId") Long mapId, @Param("attId") Long attId);


}
