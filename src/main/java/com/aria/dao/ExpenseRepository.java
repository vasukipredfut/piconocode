package com.aria.dao;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.CustomVouNameByUserDeptList;
import com.aria.dto.ExpensesList;
import com.aria.model.Expense;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

	@Query(value="SELECT ex.id AS id , ex.rname AS secName , ex.rtype AS secType , ex.cost AS cost , ex.unit AS unit , p.name AS primaryExpName ,eg.id AS groupId "
	        + " FROM expenses ex LEFT JOIN expgroup eg ON eg.secexpid = ex.id LEFT JOIN primary_expenses  p ON p.id = eg.expid "  
			+ "	WHERE (:rname is null or lower(ex.rname) like ('%' || lower(:rname) || '%')) "
            + " AND (:rtype is null or lower(ex.rtype) like ('%' || lower(:rtype) || '%'))AND (:name is null or lower(p.name) like ('%' || lower(:name) || '%')) "
			+ " GROUP BY  ex.id , ex.rname , ex.rtype  , ex.cost , ex.unit,p.name,eg.id ORDER BY ex.id DESC ",nativeQuery = true)
    Page<ExpensesList> filterSecondaryExpenses(@Param("rname") String rname,
                                            @Param("rtype") String rtype,@Param("name") String name, Pageable pageable);

	
	@Query(value="SELECT e.id AS id , e.rname AS name FROM expenses  e  "  
			+ "	WHERE  (:rname is null or lower(e.rname) like ('%' || lower(:rname) || '%'))  ORDER BY e.id ASC LIMIT 100",nativeQuery = true)
	List<CustomVouNameByUserDeptList> findAllSecExpList(@Param("rname") String rname );
	

}
