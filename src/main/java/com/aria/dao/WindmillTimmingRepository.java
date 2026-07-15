package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.WindmillTimming;

@Repository
public interface WindmillTimmingRepository extends JpaRepository<WindmillTimming, Long> {

}
