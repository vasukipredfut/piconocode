package com.aria.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.ExpGroupList;
import com.aria.dto.ExpenseVoucherList;
import com.aria.model.ExpenseVoucher;

@Repository
public interface ExpenseVoucherRepository extends JpaRepository<ExpenseVoucher, Long> {

	@Query(value="SELECT ev.id, v.name AS customVoucherName, e.rname AS secondaryExpenseName,"
   +" d.name AS departmentName, ev.contype, ev.category FROM expvoucher ev LEFT JOIN vouname v ON ev.vouid = v.id"
+ " LEFT JOIN expenses e ON ev.expid = e.id LEFT JOIN department_details d ON ev.dept = d.id WHERE (:dept IS NULL OR ev.dept = :dept)"
+ " AND (:name IS NULL OR lower (v.name) like ('%' || lower(:name) ||'%'))"
+ " AND (:rname IS NULL OR lower (e.rname) like ('%' || lower(:rname) ||'%'))GROUP BY  ev.id, v.name,e.rname,d.name,ev.contype, ev.category  ORDER BY ev.id DESC ",nativeQuery = true)
	Page<ExpenseVoucherList> filterExpenseVoucher(@Param("name")  String name,@Param("dept") Long dept,@Param("rname") String rname, Pageable pageable);
	
	@Query(value="SELECT  DISTINCT ON (e.id) e.id, e.rname AS expName , e.rtype AS expType, e.cost  AS cost , e.unit  AS unit , ev.contype AS associatedWith ,"
			+ "		 ev.category AS category , max(ex.id) as groupId ,p.id AS primaryExpId , p.name AS primaryExpName "
			+ "	 FROM expenses e "
			+ "	JOIN expvoucher ev ON ev.expid = e.id "
			+ "	LEFT JOIN expgroup ex ON ex.secexpid = e.id "
			+ "		 LEFT JOIN primary_expenses p ON p.id = ex.expid "
			+ "	 WHERE ev.vouid = :vouid AND ev.dept = :dept "
			+ "		group by e.id, e.rname , e.rtype ,e.cost ,e.unit,ev.contype,p.id, ev.category ,p.name  ORDER by e.id, max(ex.id )desc ",nativeQuery = true)
	List<ExpGroupList> findByExpenseGrope( @Param("vouid") Long vouid  , @Param("dept") Long dept );





}
