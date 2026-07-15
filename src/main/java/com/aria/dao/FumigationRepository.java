package com.aria.dao;

import com.aria.model.Fumigation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FumigationRepository extends JpaRepository<Fumigation, Long> {


}
