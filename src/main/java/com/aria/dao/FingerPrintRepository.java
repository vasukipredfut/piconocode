package com.aria.dao;

import com.aria.model.FingerPrint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FingerPrintRepository extends JpaRepository<FingerPrint, Long> {

    Optional<FingerPrint> findByUserId(Long userId);

    Optional<FingerPrint> findBySecureKey(String secureKey);
}
