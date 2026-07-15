package com.aria.dao;

import com.aria.model.Action;
import com.aria.model.ListUser;
import com.aria.model.VehicleRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.Optional;

@Repository
public interface VehicleRequestRepository extends JpaRepository<VehicleRequestEntity,Long> {

    @Query(value = "select ed.* from vehicle_request ed INNER JOIN vtrips br ON br.id = ed.trip_id"
            + " WHERE (:tripName is null or (lower(br.name) LIKE ('%' || lower(:tripName) || '%'))) "
            + " AND (:capacity is null or (lower(ed.capacity) LIKE ('%' || lower(:capacity) || '%')))"
            + " AND (:typeofVehicle is null or (lower(ed.type_of_vehicle) LIKE ('%' || lower(:typeofVehicle) || '%')))"
            + " AND (cast(:travelDate as timestamp) is null or ed.travel_date = :travelDate) "
            +" order by ed.id desc",  nativeQuery = true)
    Page<VehicleRequestEntity> filterVehicleRequest(@Param("travelDate") Date travelDate, @Param("capacity") String capacity, @Param("typeofVehicle") String typeofVehicle, @Param("tripName") String tripName,
                                                    Pageable pageable);

}
