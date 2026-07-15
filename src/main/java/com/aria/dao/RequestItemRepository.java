package com.aria.dao;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.aria.model.RequestItem;
import org.springframework.transaction.annotation.Transactional;


import jakarta.persistence.Tuple;
import java.util.List;

@Repository
public interface RequestItemRepository extends JpaRepository<RequestItem, Long> {

    @Query(value = "SELECT * FROM request_item i WHERE i.request_id =:requestId", nativeQuery = true)
    List<RequestItem> findAllByRequestId(@Param("requestId") Long requestId);

    @Query(value = "SELECT i.dept_id as deptId, i.branch_id as branchId , i.request_id as requestId FROM request_item i WHERE i.request_id =:requestId GROUP BY i.dept_id , i.branch_id , i.request_id ", nativeQuery = true)
    List<Tuple> findByRequestId(@Param("requestId") Long requestId);

    @Query(value = "SELECT * FROM request_item i WHERE i.request_id =:requestId AND i.branch_id =:branch AND i.dept_id =:deptId ", nativeQuery = true)
    List<RequestItem> findBy(@Param("requestId") Long requestId, @Param("branch") Long branch, @Param("deptId") Long deptId);



    @Query(value = "SELECT i.id FROM request_item i WHERE i.request_id =:requestId AND i.branch_id =:branch AND i.dept_id =:deptId ", nativeQuery = true)
    List<Long> findByIdList(@Param("requestId") Long requestId, @Param("branch") Long branch, @Param("deptId") Long deptId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE request_item set status='REJECTED' WHERE id in (:idList) ", nativeQuery = true)
    void updateStatus(List<Long> idList);

    @Query(value = "SELECT * FROM request_item i WHERE i.request_id =:requestId AND i.branch_id =:branch AND i.dept_id =:deptId AND ( i.status is null or i.status <>'REJECTED') ", nativeQuery = true)
    List<RequestItem> findByStatus(@Param("requestId") Long requestId, @Param("branch") Long branch, @Param("deptId") Long deptId);



}