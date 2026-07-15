package com.aria.dao;

import com.aria.model.ACwages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ACwagesRepository extends JpaRepository<ACwages,Long> {

}
