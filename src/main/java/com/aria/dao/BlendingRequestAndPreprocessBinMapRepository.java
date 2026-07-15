package com.aria.dao;


import com.aria.model.BlendingRequestAndPreprocessBinMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface BlendingRequestAndPreprocessBinMapRepository extends JpaRepository<BlendingRequestAndPreprocessBinMap, Long> {


    @Query(value = "SELECT * FROM blending_request_preprocess_bin_map b WHERE b.type_Id =:requestId AND b.type='Blending_Material_Request' LIMIT 1 ", nativeQuery = true)
    Optional<BlendingRequestAndPreprocessBinMap> findByMaterialRequest(@Param("requestId") Long requestId);


    @Query(value = "SELECT * FROM blending_request_preprocess_bin_map b WHERE b.type_Id =:godownId AND b.type='Blending_External_Request' LIMIT 1 ", nativeQuery = true)
    Optional<BlendingRequestAndPreprocessBinMap> findByExternalRequest(@Param("godownId") Long godownId);



    @Query(value = "SELECT * FROM blending_request_preprocess_bin_map b WHERE b.trn_no =:trnNo AND b.type='Blending_External_Request' LIMIT 1 ", nativeQuery = true)
    Optional<BlendingRequestAndPreprocessBinMap> findByExternalRequestWithTrnNo(@Param("trnNo") String trnNo);


}
