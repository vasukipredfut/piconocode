package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.SkillGradeLevel;

@Repository
public interface SkillGradeLevelRepository extends JpaRepository<SkillGradeLevel, Long> {

}
