package com.aria.dao;

import com.aria.model.JaggeryAttributes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JaggeryAttributesRepository extends JpaRepository<JaggeryAttributes, Long> {

}
