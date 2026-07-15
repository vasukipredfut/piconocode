package com.aria.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aria.model.*;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransportRepository extends JpaRepository<Transport,Long>{
	
//	@Query(value="SELECT * FROM vinfo WHERE vid =:vehicleId",nativeQuery = true)
	public Transport findByVid(Machine vehicleId);
	@Query(value="SELECT t FROM Transport t WHERE t.capacity =:capacity and t.cate=:category")
	public List<Transport> findByCapacityAndVehicleType(@Param("capacity") String capacity,@Param("category") String category);

	@Query(value = "SELECT be FROM Transport be WHERE (:vehicleName is null or lower(be.vid.name) like ('%' || lower(:vehicleName) || '%')) "
			+ "AND (:vehicleType is null or lower(be.cate) like ('%' || lower(:vehicleType) || '%')) "
			+ "AND (:capacity is null or lower(be.capacity) like ('%' || lower(:capacity) || '%')) ")
	Page<Transport> filterTransport(@Param("vehicleName") String vehicleName,
											@Param("vehicleType") String vehicleType,@Param("capacity") String capacity, Pageable pageable);
//	
//	@Query("SELECT t FROM Transport t WHERE " +
//		       "(:vehicleName is null or lower(t.vid.name) like ('%' || lower(:vehicleName) || '%')) " +
//		       "AND (:vehicleType is null or lower(t.cate) like ('%' || lower(:vehicleType) || '%')) " +
//		       "AND (:vehicleTypeId = 1 AND t.cate = 'passenger' OR :vehicleTypeId = 2 AND t.cate = 'truck')")
//		Optional<Transport> findByVehicleType(@Param("vehicleTypeId") Integer vehicleTypeId,
//		                                      @Param("vehicleName") String vehicleName,
//		                                      @Param("vehicleType") String vehicleType);
//	@Query(value = "select be from Transport be where (:vehicleType is null or lower(be.cate) like ('%' || lower(:vehicleType) || '%'))")
//	public Page<Transport> findByCate( @Param("vehicleType")String vehicleType);
//	
	@Query(value = "SELECT be FROM Transport be WHERE (:vehicleName is null or lower(be.vid.name) like ('%' || lower(:vehicleName) || '%')) "
			+ "AND (:vehicleType is null or lower(be.cate) like ('%' || lower(:vehicleType) || '%')) ")	
	public List<Transport> filterTransport(String vehicleName, String vehicleType);
	
	
}
