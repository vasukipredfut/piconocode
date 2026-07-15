package com.aria.dao;

import com.aria.model.DaCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DaCategoryRepository extends JpaRepository<DaCategory, Long> {

}
