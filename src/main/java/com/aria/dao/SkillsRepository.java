package com.aria.dao;

import com.aria.model.Skills;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkillsRepository  extends JpaRepository<Skills, Long> {

    Optional<Skills> findByName(String name);
}
