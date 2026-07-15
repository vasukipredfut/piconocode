package com.aria.dao;

import com.aria.model.ConsumptionDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;



@Repository
public interface ConsumptionDetailsRepository extends JpaRepository<ConsumptionDetails, Long> {

    List<ConsumptionDetails> findByConsumptionName(String consumptionName);

}