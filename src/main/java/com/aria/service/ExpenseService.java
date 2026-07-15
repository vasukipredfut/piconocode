package com.aria.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import com.aria.dao.MachineRepository;
import com.aria.dao.PrimaryExpensesRepository;
import com.aria.dao.UserRepository;
import com.aria.dao.VoucherFormatRepository;
import com.aria.dto.*;
import com.aria.model.*;
import org.apache.xmlbeans.impl.xb.xsdschema.Attribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.aria.dao.ExpenseRepository;
import com.aria.dao.ExpensesMappingRepository;
import com.aria.dao.PurchaseExpenseRepository;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PurchaseExpenseRepository purchaseExpenseRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MachineRepository machineRepository;
    
    @Autowired
    private PrimaryExpensesRepository primaryExpensesRepository;
    
    @Autowired
    private VoucherFormatRepository voucherFormatRepository;
    
    @Autowired
    ExpensesMappingRepository expensesMappingRepository;


    public ResponseEntity<?> getAllSecondaryExpense(User user, Pageable pageable, String rname, String rtype,String primaryExpenseName) {
        Page<ExpensesList> expenses = expenseRepository.filterSecondaryExpenses(rname, rtype,primaryExpenseName, pageable);     
        List<ExpensesListDto> dtoList = expenses.stream()
		        .map(ExpensesListDto::new)
		        .collect(Collectors.toList());
		 PageDataDto<ExpensesListDto> pageData = new PageDataDto<>(dtoList, expenses);
	        return new ResponseEntity<>(pageData, HttpStatus.OK);
    }
    public ExpensesMappingResponse getSecondaryExpense(Long id) {
    	Optional<ExpensesMapping> groupId = expensesMappingRepository.findById(id);
    	if(groupId.isPresent()) {
    		 Optional<Expense> optional = expenseRepository.findById(groupId.get().getSecExpId());
    		 if (optional.isPresent()) {
  	        	ExpensesMappingResponse response = new ExpensesMappingResponse (optional.get());
  	        	response.setGroupId(groupId.get().getId());
  	        	response.setPrimaryId(groupId.get().getExpId());
  	        	Optional<PrimaryExpenses> primary = primaryExpensesRepository.findById(groupId.get().getExpId());
    	if(primary.isPresent())
    	{
    		response.setPrimaryName(primary.get().getName());
    	}
    	return response;
    		 }    		 
    	}
		return new ExpensesMappingResponse(); 
    }


@Transactional
    public void deleteSecondaryExpense(Long id) {
    	expensesMappingRepository.deleteAllBySecExpId(id);
        expenseRepository.deleteById(id);
    }

    public void saveSecondaryExpense(ExpenseDto expenseDto, User user) {
    	 Expense expense = new Expense(expenseDto);       
         expense.setCreatedBy(user.getId());
         expense.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
          Expense result = expenseRepository.save(expense);       
         	Optional<PrimaryExpenses> primaryExpenses = primaryExpensesRepository.findById(expenseDto.getPrimaryExpId());
         	if(primaryExpenses.isPresent())     	{
         		ExpensesMapping expensesMapping = new ExpensesMapping();
         		expensesMapping.setExpId(expenseDto.getPrimaryExpId());
         		expensesMapping.setSecExpId(result.getId());         		
         		expensesMappingRepository.save(expensesMapping);
         	}
        }   

  


    public ResponseEntity<?> findAllPurchaseExpense(User user) {

        List<PurchaseExpenseList> expenses = purchaseExpenseRepo.findallExpense();

        List<PurchaseExpenseListDto> expenseDtos = expenses.stream().map(val -> new PurchaseExpenseListDto(val)).collect(Collectors.toList());

        return new ResponseEntity<List<PurchaseExpenseListDto>>(expenseDtos, HttpStatus.OK);
    }

    public ResponseEntity<?> findAllPurchaseAMcExpense(User user) {

        List<PurchaseExpenseList> expenses = purchaseExpenseRepo.findallAMCMachine();

        List<PurchaseExpenseListDto> expenseDtos = expenses.stream().map(val -> new PurchaseExpenseListDto(val)).collect(Collectors.toList());

        return new ResponseEntity<List<PurchaseExpenseListDto>>(expenseDtos, HttpStatus.OK);
    }


    public ResponseEntity<?> saveAMCExpense(PurchaseExpenseDto amcExpenseDto, User loggedUser) {
        PurchaseExpense amcExpense = new PurchaseExpense(amcExpenseDto);
        amcExpense.setCreated_Timestamp(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        amcExpense.setAmc_Flag(1);
        amcExpense.setCreatedBy(loggedUser.getId());
        amcExpense.setCompany(Integer.valueOf(loggedUser.getOrg_id().toString()));
        amcExpense.setBranch(Integer.valueOf(loggedUser.getBranch_id().toString()));
        amcExpense.setDepartment(Integer.valueOf(loggedUser.getDepartment().getId().toString()));
        PurchaseExpense result = purchaseExpenseRepo.save(amcExpense);
        return new ResponseEntity<Status>(new Status("200", "AMCExpense created SuccessFully"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateAMCExpense(PurchaseExpenseDto amcExpenseDto, User loggedUser) {
        Optional<PurchaseExpense> optionalAMCExpense = purchaseExpenseRepo.findById(amcExpenseDto.getAmc_Id());
        if (optionalAMCExpense.isPresent()) {
            PurchaseExpense amcExpense = optionalAMCExpense.get();
            amcExpense.setAmc_Category_Id(amcExpenseDto.getAmc_Category_Id());
            amcExpense.setAmc_Expense_unit(amcExpenseDto.getAmc_Expense_unit());
            amcExpense.setAmc_Expense_Variable_type(amcExpenseDto.getAmc_Expense_Variable_type());
            amcExpense.setAmc_Expense_Name(amcExpenseDto.getAmc_Expense_Name());
            amcExpense.setAmc_Machine_Ref_Id(amcExpenseDto.getAmc_Machine_Ref_Id());
            amcExpense.setUpdated_Timestamp(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            amcExpense.setUpdatedBy(loggedUser.getId());
            PurchaseExpense result = purchaseExpenseRepo.save(amcExpense);
            return new ResponseEntity<Status>(new Status("200", "AMCExpense updated SuccessFully"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("200", "AMCExpense updated SuccessFully"), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> getAMCExpense(Long id) throws UsernameNotFoundException {
        Optional<PurchaseExpense> optionalAMCExpense = purchaseExpenseRepo.findById(id);
        if (optionalAMCExpense.isPresent()) {
            PurchaseExpenseDto amcExpenseDto = new PurchaseExpenseDto(optionalAMCExpense.get());
            if (amcExpenseDto.getCreatedBy() != null) {
                Optional<User> optional = userRepository.findById(amcExpenseDto.getCreatedBy());
                if (optional.isPresent()) {
                    amcExpenseDto.setCreatedByName(optional.get().getEmployeename());
                }
            }
            if (amcExpenseDto.getAmc_Machine_Ref_Id() != null) {
                Optional<Machine> optional = machineRepository.findById(amcExpenseDto.getAmc_Machine_Ref_Id());
                if (optional.isPresent()) {
                    amcExpenseDto.setAmc_Machine_Ref_Name(optional.get().getName());
                }
            }
            return new ResponseEntity<PurchaseExpenseDto>(amcExpenseDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity<?> getAMCExpenseListByPage(Long amcCategoryId, Long amcExpenseVariableType, Long amcExpenseUnit, int amcFlag, Long amcMachineRefId, Pageable pageable) throws UsernameNotFoundException {
        Page<PurchaseExpense> amcExpenseList = purchaseExpenseRepo.getAMCExpenseListByPage(amcCategoryId, amcExpenseVariableType, amcExpenseUnit, amcFlag, amcMachineRefId, pageable);
        if (!amcExpenseList.isEmpty()) {
            List<PurchaseExpenseDto> result = amcExpenseList.stream().map(data -> {
                PurchaseExpenseDto amcExpenseDto = new PurchaseExpenseDto(data);
                if(amcExpenseDto.getCreatedBy() != null){
                    Optional<User> optional = userRepository.findById(amcExpenseDto.getCreatedBy());
                    if(optional.isPresent()){
                        amcExpenseDto.setCreatedByName(optional.get().getEmployeename());
                    }
                }
                if(amcExpenseDto.getAmc_Machine_Ref_Id() != null){
                    Optional<Machine> optional = machineRepository.findById(amcExpenseDto.getAmc_Machine_Ref_Id());
                    if(optional.isPresent()){
                        amcExpenseDto.setAmc_Machine_Ref_Name(optional.get().getName());
                    }
                }
                return amcExpenseDto;
            }).collect(Collectors.toList());
            PageDataDto<PurchaseExpenseDto> finalResult = new PageDataDto<>(result, pageable, amcExpenseList);
            return new ResponseEntity<Object>(finalResult, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }


    public ResponseEntity<?> deleteAMCExpense(Long id) throws UsernameNotFoundException {
        try {
            Optional<PurchaseExpense> optionalAMCExpense = purchaseExpenseRepo.findById(id);
            if (optionalAMCExpense.isPresent()) {
                PurchaseExpense amcExpense = optionalAMCExpense.get();
                purchaseExpenseRepo.delete(amcExpense);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }
 //   created by dhivya ,primaryexpenses for expenses, discussed by subhash sir and alaguraja sir   
    public ResponseEntity<?> savePrimaryExpenses( PrimaryExpensesDto primaryExpenseDto, User loggedUser) { 
    	boolean existsByName = primaryExpensesRepository.existsByName(primaryExpenseDto.getName());
    	if(!existsByName) {
       PrimaryExpenses primaryExpenses=new PrimaryExpenses(primaryExpenseDto);
       primaryExpensesRepository.save(primaryExpenses);    
        return new ResponseEntity<Status>(new Status("200", "PrimaryExpenses created SuccessFully"), HttpStatus.OK);
    	}
    	else {
    		return ResponseEntity.badRequest().body(new Status("400", "Error:PrimaryExpense is already exists!"));
    	}
    }
    
    //   created by dhivya ,primaryexpenses for expenses, discussed by subhash sir and alaguraja sir    
    public ResponseEntity<?> getAllPrimaryExpense() {
    	List<PrimaryExpenses> primaryExpenses = primaryExpensesRepository.findAll();
    	if(!primaryExpenses.isEmpty()) {
        List<PrimaryExpensesDto> expenseDtos = primaryExpenses.stream().map(val -> new PrimaryExpensesDto(val)).collect(Collectors.toList());
        return new ResponseEntity<List<PrimaryExpensesDto>>(expenseDtos, HttpStatus.OK);
    	}
    	else {
    		  return new ResponseEntity<List<PrimaryExpensesDto>>(new ArrayList<>(), HttpStatus.OK);
    	}
    }
//
    public ResponseEntity<?> saveVoucherFormat( VoucherFormatDto voucherFormatDto, User loggedUser) { 
    	boolean existsByName = voucherFormatRepository.existsByName(voucherFormatDto.getName());
    	if(!existsByName) {
    		VoucherFormat voucherFormat=new VoucherFormat(voucherFormatDto);
    		voucherFormatRepository.save(voucherFormat);    
        return new ResponseEntity<Status>(new Status("200", "voucherFormat created SuccessFully"), HttpStatus.OK);
    	}
    	else {
    		return ResponseEntity.badRequest().body(new Status("400", "Error:voucherFormatRepository is already exists!"));
    	}
    }
//    
    public ResponseEntity<?> getAllVoucherFormat() {
    	List<VoucherFormat> voucherFormats = voucherFormatRepository.findAll();
    	if(!voucherFormats.isEmpty()) {
        List<VoucherFormatDto> voucherFormatDtos = voucherFormats.stream().map(val -> new VoucherFormatDto(val)).collect(Collectors.toList());
        return new ResponseEntity<List<VoucherFormatDto>>(voucherFormatDtos, HttpStatus.OK);
    	}
    	else {
    		  return new ResponseEntity<List<VoucherFormatDto>>(new ArrayList<>(), HttpStatus.OK);
    	}
    }
	public ResponseEntity<?> findAllSecondaryExpense(String rname ) {
		List<CustomVouNameByUserDeptList> secondaryExpenses = expenseRepository.findAllSecExpList(rname);
		if(!secondaryExpenses.isEmpty())		{
			List<CustomVouNameByUserDeptDto> result = secondaryExpenses.stream()
			        .map(CustomVouNameByUserDeptDto::new)
			        .collect(Collectors.toList());
				return new ResponseEntity<>(result, HttpStatus.OK);
		}else {
  		  return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
  	}
	}
	@Transactional
	public ResponseEntity<?> editExpenses(ExpensesMappingResponse expensesMappingResponse, User user) {
		Expense expense = new Expense(expensesMappingResponse);	 
		 expense.setUpdatedBy(user.getId());
	        expense.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
//		expensesMappingRepository.deleteBySecExpId(expensesMappingResponse.getId());				
		expense.setId(expensesMappingResponse.getId()); 				   
				    Expense savedExpense = expenseRepository.save(expense);	
				    Optional<ExpensesMapping> groupData = expensesMappingRepository.findById(expensesMappingResponse.getGroupId());
				        	if(groupData.isPresent()) {
				        		 ExpensesMapping expGroup = groupData.get();				                
				                 expGroup.setSecExpId(expensesMappingResponse.getId());
				                 expGroup.setExpId(expensesMappingResponse.getPrimaryId());
				                 expGroup.setId(expensesMappingResponse.getGroupId())  ; 				        	
				            expensesMappingRepository.save(expGroup);
				        	}
				        				        
				    	    
					 return new ResponseEntity<Status>(new Status("200", "expense updated  successfully!!!"), HttpStatus.OK);

		}
	public ResponseEntity<?> getAllSecondaryExpensesList() {
		List<Expense> expenses = expenseRepository.findAll();
    	if(!expenses.isEmpty()) {
        List<ExpenseDto> expenseDtos = expenses.stream().map(val -> new ExpenseDto(val)).collect(Collectors.toList());
        return new ResponseEntity<List<ExpenseDto>>(expenseDtos, HttpStatus.OK);
    	}
    	else {
    		  return new ResponseEntity<List<ExpenseDto>>(new ArrayList<>(), HttpStatus.OK);
    	}
	}
	
}
