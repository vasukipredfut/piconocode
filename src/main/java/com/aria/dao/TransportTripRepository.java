
package com.aria.dao;

import com.aria.model.AdminInspection;
import com.aria.model.BillExchange;
import com.aria.model.TransportTrip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Set;

@Repository
public interface TransportTripRepository extends JpaRepository<TransportTrip, Long> {


    @Query(value = "SELECT be FROM TransportTrip be WHERE (:name is null or lower(be.name) like ('%' || lower(:name) || '%')) "
            + "AND (:cate is null or lower(be.cate) like ('%' || lower(:cate) || '%')) ")
    Page<TransportTrip> filterTransportTrip(@Param("name") String name,
                                              @Param("cate") String cate,Pageable pageable);

}
