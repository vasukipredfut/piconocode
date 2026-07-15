package com.aria.dao;

import java.util.List;
import java.util.Optional;

import com.aria.model.Machine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.PurchaseExpense;
import com.aria.model.PurchaseExpenseList;

import jakarta.transaction.Transactional;

@Repository
public interface PurchaseExpenseRepository extends JpaRepository<PurchaseExpense, Long> {

	
	@Query(value="select  ex.amc_Id as id, ex.amc_Expense_Name as expenseName from purchase_amc_expense ex where ex.amc_flag =1 and ex.amc_Category_Id = 30 and ex.amc_machine_ref_id=0",nativeQuery = true)
	public List<PurchaseExpenseList> findallExpense();
	
	@Query(value="select ex.amc_Id as id, ex.amc_Expense_Name as expenseName, mc.id as machineId, mc.name as machineName from purchase_amc_expense ex, machine mc where ex.amc_flag =1 and ex.amc_Category_Id = 31 and mc.id=ex.amc_machine_ref_id and ex.amc_machine_ref_id > 0 ",nativeQuery = true)
	public List<PurchaseExpenseList> findallAMCMachine();

	@Query(value = "select * from purchase_amc_expense ex WHERE "
			+ " (:amcCategoryId is null or ex.amc_Category_Id =:amcCategoryId )"
			+ " AND (:amcMachineRefId is null or ex.amc_Machine_Ref_Id =:amcMachineRefId )"
			+ " AND (:amcExpenseVariableType is null or ex.amc_Expense_Variable_type =:amcExpenseVariableType )"
			+ " AND (:amcExpenseUnit is null or ex.amc_Expense_unit =:amcExpenseUnit )"
			+ " AND (:amcFlag is null or ex.amc_Flag =:amcFlag )", nativeQuery = true)
	Page<PurchaseExpense> getAMCExpenseListByPage(@Param("amcCategoryId") Long amcCategoryId,
											 @Param("amcExpenseVariableType") Long amcExpenseVariableType ,
											 @Param("amcExpenseUnit") Long amcExpenseUnit ,
											 @Param("amcFlag") int amcFlag ,
											 @Param("amcMachineRefId") Long amcMachineRefId, Pageable pageable);


	@Query(value="SELECT * FROM purchase_amc_expense WHERE amc_Category_Id=:amcCategoryId",nativeQuery = true)
	public List<PurchaseExpense> findBy(@Param("amcCategoryId") Long amcCategoryId, Pageable pageable);

	@Query(value="SELECT * FROM purchase_amc_expense WHERE amc_Category_Id=:amcCategoryId AND (:searchProduct is null or LOWER(amc_Expense_Name) LIKE LOWER(CONCAT('%', :searchProduct, '%')))",nativeQuery = true)
	public List<PurchaseExpense> findBy(@Param("amcCategoryId") Long amcCategoryId, @Param("searchProduct") String searchProduct);

	@Query(value = "SELECT * FROM purchase_amc_expense WHERE (:id is null or amc_Id =:id ) AND (:hsn is null or hsn_Ref_Id=:hsn ) AND hsn_Ref_Id IS NOT NULL AND hsn_Ref_Id NOT IN ('') LIMIT 1 ", nativeQuery = true)
	Optional<PurchaseExpense> findByHsn(@Param("id") Long id, @Param("hsn") String hsn);

	@Query(value = "SELECT * FROM purchase_amc_expense WHERE (:hsn is null or hsn_Ref_Id=:hsn) ", nativeQuery = true)
	List<PurchaseExpense> findByHsn( @Param("hsn") String hsn);


	@Transactional
	@Modifying
	@Query(value = "UPDATE purchase_amc_expense m set hsn_Ref_Id=null WHERE m.hsn_Ref_Id =:hsnCode",nativeQuery = true)
	void updateHsn(@Param("hsnCode") String hsnCode);
}
