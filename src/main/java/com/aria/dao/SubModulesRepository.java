package com.aria.dao;
import com.aria.model.SubModules;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubModulesRepository extends JpaRepository<SubModules, Long> {

}
