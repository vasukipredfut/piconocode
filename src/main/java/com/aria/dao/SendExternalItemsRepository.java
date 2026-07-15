package com.aria.dao;

import com.aria.model.SendExternalItemsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SendExternalItemsRepository extends JpaRepository<SendExternalItemsEntity,Long> {

    List<SendExternalItemsEntity> findBySendExternalRefId(Long sendExternalRefId);

    @Modifying
    @Transactional
    void deleteBySendExternalRefId(Long sendExternalRefId);


    @Query(value = "SELECT * FROM material_flow_send_external_items ob WHERE ob.send_External_Ref_Id =:issueId AND ob.material_return='YES' ", nativeQuery = true)
    List<SendExternalItemsEntity> findBy(@Param("issueId") Long issueId);

}
