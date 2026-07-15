package com.aria.dao;

import com.aria.model.TrainerDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.SkillDetails;

import java.util.Optional;

@Repository
public interface SkillDetailsRepository extends JpaRepository<SkillDetails, Long> {

    @Query(value = "SELECT * FROM skill_details td WHERE td.competency_id =:competencyId AND td.id =:skillId ", nativeQuery = true)
    Optional<SkillDetails> optionalSkillDetails(@Param("competencyId") Long competencyId, @Param("skillId") Long skillId);

}