package com.aria.dao;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.ExpensesMapingList;
import com.aria.dto.ExpensesMappingDto;
import com.aria.model.ExpensesMapping;

@Repository
public interface ExpensesMappingRepository extends JpaRepository<ExpensesMapping, Long> {
	
@Query(value="select distinct id as id, expid as expId, secexpid as secExpId from expgroup where secexpid=:secexpid " , nativeQuery = true)
	List<ExpensesMapingList> findBySecExpId(@Param("secexpid") Long secexpid);

@Modifying
@Transactional
@Query(value = "delete from expgroup e WHERE e.secexpid =:secexpid" , nativeQuery = true)
void deleteBySecExpId(@Param("secexpid") Long secexpid);

@Modifying
@Transactional
@Query(value = "delete from expgroup e WHERE e.secexpid =:secexpid" , nativeQuery = true)
void deleteAllBySecExpId(@Param("secexpid") Long secexpid);

}
