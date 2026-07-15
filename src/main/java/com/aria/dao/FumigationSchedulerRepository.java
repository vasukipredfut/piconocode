package com.aria.dao;


import com.aria.model.FumigationScheduler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FumigationSchedulerRepository extends JpaRepository<FumigationScheduler, Long> {


    @Query(value = "SELECT *  FROM fumigation_scheduler fs WHERE fs.fumigation_type =:fumigationType AND fs.godown_id =:godownId", nativeQuery = true)
    Optional<FumigationScheduler> findBy(@Param("fumigationType") String fumigationType, @Param("godownId") Long godownId );



}
