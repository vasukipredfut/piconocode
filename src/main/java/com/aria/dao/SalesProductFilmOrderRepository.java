package com.aria.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aria.model.SalesProductFilm;
import com.aria.model.SalesProductFilmOrder;

public interface SalesProductFilmOrderRepository extends JpaRepository<SalesProductFilmOrder, Long> {

	@Query(value = "Select * from salesproductfilmorder ORDER BY id DESC", nativeQuery = true)
	public Page<SalesProductFilmOrder> findAll(Pageable page);

	public List<SalesProductFilmOrder> findByFilmIdAndLocationAndSubLocationIdAndOrderBranchAndLot(
			SalesProductFilm filmId, String location, Long subLocationId, Long branch, Long lot);

	public Optional<SalesProductFilmOrder> findByMrnNo(Long mrnNo);

	@Query(value = "SELECT * from salesproductfilmorder s " +
			" WHERE (cast(:oDate as date) IS NULL OR  s.orderdate = :oDate) " +
			" AND (:status IS NULL OR  s.mrnstatus = :status) ORDER BY s.id DESC ", nativeQuery = true)
	public Page<SalesProductFilmOrder> findByFilter(String status, Date oDate, Pageable page);

	@Query(value = "SELECT * from salesproductfilmorder s " +
			" WHERE (cast(:oDate as date) IS NULL OR  s.orderdate = :oDate) " +
			" AND (:status IS NULL OR  s.mrnstatus = :status) ORDER BY s.id DESC ", nativeQuery = true)
	public List<SalesProductFilmOrder> findByMrnStatusAndOrderDate(String status, Date oDate);

}
