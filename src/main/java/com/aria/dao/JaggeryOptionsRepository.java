package com.aria.dao;

import com.aria.model.JaggeryOptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JaggeryOptionsRepository extends JpaRepository<JaggeryOptions, Long> {

}
