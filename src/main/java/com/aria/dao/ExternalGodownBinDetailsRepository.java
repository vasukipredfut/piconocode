package com.aria.dao;

import com.aria.model.ExternalGodownBinDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExternalGodownBinDetailsRepository extends JpaRepository<ExternalGodownBinDetails, Long> {


}
