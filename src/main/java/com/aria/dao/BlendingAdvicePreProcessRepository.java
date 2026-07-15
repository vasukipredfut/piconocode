package com.aria.dao;

import com.aria.model.BlendingAdvicePreProcess;
import com.aria.model.BlendingAdviceStockDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlendingAdvicePreProcessRepository extends JpaRepository<BlendingAdvicePreProcess, Long> {


    @Query(value = "SELECT  *  FROM blending_advice_preprocess b WHERE  b.blending_advice_details_id =:blendingAdviceDetailsId ", nativeQuery = true)
    List<BlendingAdvicePreProcess> findBy(@Param("blendingAdviceDetailsId") Long blendingAdviceDetailsId);

}
