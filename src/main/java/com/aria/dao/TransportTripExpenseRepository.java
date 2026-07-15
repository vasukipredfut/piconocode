package com.aria.dao;

import com.aria.model.Action;
import com.aria.model.TransportTrip;
import com.aria.model.TransportTripExpenses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransportTripExpenseRepository extends JpaRepository<TransportTripExpenses,Long> {

    @Modifying
    @Transactional
    void deleteByTrip(TransportTrip trip);

    @Query(value = "select distinct t from TransportTrip t  right join" +
            " t.transportTripExpensesList e where (:tripName is null or (lower(t.name) LIKE ('%' || lower(:tripName) || '%'))) " +
            " AND (:distance is null or (lower(t.distance) LIKE ('%' || lower(:distance) || '%')))  " +
            " AND (:tripId is null or t.id=:tripId)  ")
    Page<TransportTrip> getByTransportExpense(@Param("tripName") String tripName,@Param("distance") String distance,@Param("tripId") Long tripId,Pageable pageable);
}
