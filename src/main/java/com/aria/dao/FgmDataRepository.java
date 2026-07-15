package com.aria.dao;

import com.aria.model.FgmData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FgmDataRepository extends JpaRepository<FgmData, Long> {

    Optional<FgmData> findByRefid(Long refId);

}
