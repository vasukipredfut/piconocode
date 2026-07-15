package com.aria.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import com.aria.dao.*;
import com.aria.model.*;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aria.dto.CompanyList;
import com.aria.dto.CustomVouDeptDto;
import com.aria.dto.CustomVouNameByUserDeptDto;
import com.aria.dto.CustomVouNameByUserDeptList;
import com.aria.dto.CustomVoucherList;
import com.aria.dto.CustomVoucherListDto;
import com.aria.dto.CustomVoucherNameDto;
import com.aria.dto.ExpGroupList;
import com.aria.dto.ExpGroupListDto;
import com.aria.dto.ExpenseVoucherDto;
import com.aria.dto.ExpenseVoucherList;
import com.aria.dto.ExpenseVoucherListDto;
import com.aria.dto.GroupNameList;
import com.aria.dto.GroupNameListDto;
import com.aria.dto.PageDataDto;
import com.aria.dto.VUserBankDetails;
import com.aria.dto.VUserBankDetailsDto;
import com.aria.dto.VouPaymentsDto;
import com.aria.dto.VoucherEnrtyDto;
import com.aria.dto.VoucherEntryList;
import com.aria.dto.VoucherEntryListDto;
import com.aria.dto.VoucherEntryResponseDto;
import com.aria.dto.VoucherGroupPrintDto;
import com.aria.dto.VoucherGroupPrintList;
import com.aria.dto.VoucherGroupResponseDto;
import com.aria.dto.VoucherGrpList;
import com.aria.dto.VoucherGrpListDto;
import com.aria.dto.VoucherItemsDto;
import com.aria.dto.VoucherItemsPrint;
import com.aria.dto.VoucherItemsPrintDto;
import com.aria.dto.VoucherPrintDto;
import com.aria.dto.VoucherPrintList;
import com.aria.dto.VoucherSharedMemberDetailsDto;
import com.aria.utils.DateUtil;

@Service
public class CustomVoucherService {

    @Autowired
    CustomVouDeptMapRepository customVouDeptMapRepository;

    @Autowired
    CustomVoucherNameRepository customVouNameRepository;

    @Autowired
    ExpenseVoucherRepository expenseVoucherRepository;

    @Autowired
    VoucherEntryRepository voucherEntryRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    VoucherItemsRepository voucherItemsRepository;

    @Autowired
    VoucherDescRepository voucherDescRepository;

    @Autowired
    VoucherSharedMemberDetailsRepository voucherSharedMemberDetailsRepository;

    @Autowired
    TRNIndexRepo tRNIndexRepo;

    @Autowired
    UserRepository userRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    VouPaymentsRepository vouPaymentsRepository;

    @Autowired
    EmastDailyRepository emastDailyRepository;

    @Autowired
    MachineRepository machineRepository;

	@Autowired
	ExpenseRepository expenseRepository;


    public ResponseEntity<?> saveCustomVoucher(CustomVoucherNameDto customVoucherNameDto, User user) {

        List<Long> deptIds = customVoucherNameDto.getDeptList().stream()
                .map(CustomVouDeptDto::getDept)
                .collect(Collectors.toList());
        List<CustomVoucherName> oldVoucher = customVouNameRepository.findByOldVoucher(customVoucherNameDto.getName(), deptIds);

        List<CustomVouDeptMap> newVoucher = customVouDeptMapRepository.findByNewVoucher(customVoucherNameDto.getName(), deptIds);
        if (!oldVoucher.isEmpty() || !newVoucher.isEmpty()) {
//		        throw new RuntimeException("Voucher name already assigned to one or more selected departments.");
            return ResponseEntity.badRequest().body(new Status("400", "Error: Voucher name already assigned to one or more selected departments!"));
        }
        CustomVoucherName voucher = new CustomVoucherName(customVoucherNameDto);
        voucher.setRecordStatus("New Voucher");
        CustomVoucherName result = customVouNameRepository.save(voucher);
        saveVouDepMap(customVoucherNameDto, result);
        return new ResponseEntity<Status>(new Status("200", "CustomVoucher created  successfully!!!"), HttpStatus.OK);
    }

    public void saveVouDepMap(CustomVoucherNameDto customVoucherNameDto, CustomVoucherName result) {
        if (customVoucherNameDto.getDeptList() != null && !customVoucherNameDto.getDeptList().isEmpty()) {
            List<CustomVouDeptMap> deptmap = customVoucherNameDto.getDeptList().stream()
                    .map(dto -> {
                        CustomVouDeptMap entity = new CustomVouDeptMap(dto);
                        entity.setCustomVouNameId(result.getId());
                        return entity;
                    })
                    .collect(Collectors.toList());

            customVouDeptMapRepository.saveAll(deptmap);
        }
    }

    @Transactional
    public ResponseEntity<?> updateCustomVoucher(CustomVoucherNameDto customVoucherNameDto, User user) {
        CustomVoucherName customVoucher = new CustomVoucherName(customVoucherNameDto);
//		 Optional<CustomVoucherName> customVouName = customVouNameRepository.findById(customVoucher.getId());
//		 if(customVoucher.isPresent()) {			 
        if (customVoucher.getRecordStatus().equalsIgnoreCase("Old Voucher")) {
            return ResponseEntity.badRequest().body(new Status("400", "Error: This is old voucher we can't able to edit!!!"));
        } else {
            customVouDeptMapRepository.deleteByCustomVouNameId(customVoucherNameDto.getId());
            customVoucher.setId(customVoucherNameDto.getId());
            CustomVoucherName savedVoucher = customVouNameRepository.save(customVoucher);
            saveVouDepMap(customVoucherNameDto, savedVoucher);
        }
//		 }
        return new ResponseEntity<Status>(new Status("200", "CustomVoucher updated  successfully!!!"), HttpStatus.OK);
    }

    public CustomVoucherNameDto getCustomVoucher(Long id) {
        Optional<CustomVoucherName> optional = customVouNameRepository.findById(id);
        if (optional.isPresent()) {
            CustomVoucherNameDto dto = new CustomVoucherNameDto(optional.get());
            if (dto.getDeptList() == null || dto.getDeptList().isEmpty()) {
                List<CustomVouDeptMap> deptMap = customVouDeptMapRepository.findByCustomVouNameId(id);
                List<CustomVouDeptDto> deptDtoList = deptMap.stream()
                        .map(CustomVouDeptDto::new)
                        .collect(Collectors.toList());
                dto.setDeptList(deptDtoList);
            }
            return dto;
        }
        return new CustomVoucherNameDto();
    }

    public ResponseEntity<?> getAllcustomVoucher(User user, Pageable pageable, String name, Long dept, Long firmId) {
        Page<CustomVoucherList> customVouList = customVouNameRepository.filterCustomVoucher(name, dept, firmId, pageable);
        List<CustomVoucherListDto> dtoList = customVouList.getContent().stream()
                .map(CustomVoucherListDto::new)
                .collect(Collectors.toList());
        PageDataDto<CustomVoucherListDto> pageData = new PageDataDto<>(dtoList, customVouList);
        return new ResponseEntity<>(pageData, HttpStatus.OK);
    }

    public ResponseEntity<?> getCustomVoucherNameList(Long dept) {
        List<CustomVouNameByUserDeptList> customVouName = customVouNameRepository.findDistinctVoucherNamesByDept(dept);
        List<CustomVouNameByUserDeptDto> result = customVouName.stream()
                .map(CustomVouNameByUserDeptDto::new)
                .collect(Collectors.toList());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    public ResponseEntity<?> saveExpenseVoucher(ExpenseVoucherDto expenseVoucherDto, User user) {
        ExpenseVoucher expenseVoucher = new ExpenseVoucher(expenseVoucherDto);
        expenseVoucherRepository.save(expenseVoucher);
        return new ResponseEntity<Status>(new Status("200", "ExpenseVoucher created  successfully!!!"), HttpStatus.OK);
    }


    public ResponseEntity<?> updateExpenseVoucher(ExpenseVoucherDto expenseVoucherDto, User user) {
        ExpenseVoucher expenseVoucher = new ExpenseVoucher(expenseVoucherDto);
        expenseVoucherRepository.save(expenseVoucher);
        return new ResponseEntity<Status>(new Status("200", "ExpenseVoucher updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> getexpenseVoucher(Long id) {
        Optional<ExpenseVoucher> expvou = expenseVoucherRepository.findById(id);
        if (expvou.isPresent()) {
            ExpenseVoucherDto expenseVoucherDto = new ExpenseVoucherDto(expvou.get());
            Optional<Expense>  Op = expenseRepository.findById(expvou.get().getExpId());
			if(Op.isPresent()){
				expenseVoucherDto.setExpName(StringEscapeUtils.unescapeHtml4(Op.get().getRname()));
			}
            return new ResponseEntity<>(expenseVoucherDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("404", "Not Found"), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> getExpenseVoucherList(User user, Pageable pageable, String name, Long dept,
                                                   String rname) {
        Page<ExpenseVoucherList> filterExpenseVoucher = expenseVoucherRepository.filterExpenseVoucher(name, dept, rname, pageable);
        List<ExpenseVoucherListDto> dtoList = filterExpenseVoucher.stream()
                .map(ExpenseVoucherListDto::new)
                .collect(Collectors.toList());
        PageDataDto<ExpenseVoucherListDto> pageData = new PageDataDto<>(dtoList, filterExpenseVoucher);
        return new ResponseEntity<>(pageData, HttpStatus.OK);
    }

    public ResponseEntity<?> customVoucherName(User user) {
        List<CustomVouNameByUserDeptList> customeVouName = customVouNameRepository.findByUserDeptId(user.getDepartment().getId());
        List<CustomVouNameByUserDeptDto> dtoList = customeVouName.stream()
                .map(CustomVouNameByUserDeptDto::new)
                .collect(Collectors.toList());
        return new ResponseEntity<>(dtoList, HttpStatus.OK);
    }

    public ResponseEntity<?> expGroupData(Long vouId, User user) {
        Long dept = user.getDepartment().getId();
        List<ExpGroupList> expenseGroup = expenseVoucherRepository.findByExpenseGrope(vouId, dept);
        List<ExpGroupListDto> result = expenseGroup.stream().map(data -> {
            ExpGroupListDto expGroupListDto = new ExpGroupListDto(data);

            if ("Staff".equalsIgnoreCase(data.getAssociatedWith()) || "man".equalsIgnoreCase(data.getAssociatedWith())) {
                List<CustomVouNameByUserDeptList> employees = null;
                if ("All".equalsIgnoreCase(data.getCategory())) {
                    employees = userRepository.findActiveEmployees(null, null);
                } else if ("Dept".equalsIgnoreCase(data.getCategory())) {
                    Long deptId = user.getDepartment().getId();
                    employees = userRepository.findActiveEmployees(deptId, null);
                } else if ("Plant".equalsIgnoreCase(data.getCategory())) {
                    Long branchId = user.getBranch_id();
                    employees = userRepository.findActiveEmployees(null, branchId);
                }
                List<CustomVouNameByUserDeptDto> dtoList = employees.stream()
                        .map(CustomVouNameByUserDeptDto::new)
                        .collect(Collectors.toList());
                expGroupListDto.setDetalis(dtoList);
            } else if ("Machine".equalsIgnoreCase(data.getAssociatedWith())) {
                List<CustomVouNameByUserDeptList> machine = null;
                if ("All".equalsIgnoreCase(data.getCategory())) {
                    machine = machineRepository.findActiveMachine(null, null);
                } else if ("Dept".equalsIgnoreCase(data.getCategory())) {
                    Long deptId = user.getDepartment().getId();
                    machine = machineRepository.findActiveMachine(deptId, null);
                } else if ("Plant".equalsIgnoreCase(data.getCategory())) {
                    Long branchId = user.getBranch_id();
                    machine = machineRepository.findActiveMachine(null, branchId);
                }
                List<CustomVouNameByUserDeptDto> dtoList = machine.stream()
                        .map(CustomVouNameByUserDeptDto::new)
                        .collect(Collectors.toList());
                expGroupListDto.setDetalis(dtoList);
            } else if ("contractLabors".equalsIgnoreCase(data.getAssociatedWith()) || "dwages".equalsIgnoreCase(data.getAssociatedWith())) {
                List<CustomVouNameByUserDeptList> labors = null;
                if ("All".equalsIgnoreCase(data.getCategory())) {
                    labors = emastDailyRepository.findActiveLabors(null, null);
                } else if ("Dept".equalsIgnoreCase(data.getCategory())) {
                    Long deptId = user.getDepartment().getId();
                    labors = emastDailyRepository.findActiveLabors(deptId, null);
                } else if ("Plant".equalsIgnoreCase(data.getCategory())) {
                    Long branchId = user.getBranch_id();
                    labors = emastDailyRepository.findActiveLabors(null, branchId);
                }
                List<CustomVouNameByUserDeptDto> dtoList = labors.stream()
                        .map(CustomVouNameByUserDeptDto::new)
                        .collect(Collectors.toList());
                expGroupListDto.setDetalis(dtoList);
            }
            return expGroupListDto;
        }).collect(Collectors.toList());
        return new ResponseEntity<>(result, HttpStatus.OK);

    }

    @Transactional
    public ResponseEntity<?> saveVoucherEntry(VoucherEnrtyDto voucherEnrtyDto, User user) {
        try {
            VoucherEntry voucherEntry = new VoucherEntry(voucherEnrtyDto);
            voucherEntry.setVoushift(voucherEntryRepository.findByShiftId(voucherEnrtyDto.getVouDt(), user.getId()));
            voucherEntry.setInv("Y");
            voucherEntry.setIstaff(user.getId());
            voucherEntry.setIdate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            voucherEntry.setEntry(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            voucherEntry.setStatus("Created");
            Optional<CustomVoucherName> findById = customVouNameRepository.findById(voucherEnrtyDto.getVouId());
            if(findById.isPresent())
            {
            	voucherEntry.setVouName(findById.get().getName());
            }
            String isono = voucherEntryRepository.findByIsono(voucherEnrtyDto.getVouId());
            String dept = user.getDepartment().getTrnprefix();
            String plant = voucherEntryRepository.findByUserId(user.getBranch_id());
            String prefix = voucherEntryRepository.findByVouId(voucherEnrtyDto.getVouId());
            if (voucherEnrtyDto.getVouType().equalsIgnoreCase("Self-Claim")) {
                voucherEntry.setBranch(user.getBranch_id());
                voucherEntry.setDept(user.getDepartment().getId());
                voucherEntry.setPayto(user.getId());
                voucherEntry.setPaytable("emaster");

            } else {
                voucherEntry.setPayto(0l);
                voucherEntry.setPaytable("Others");
            }
            String prefixType = isono + "/" + plant + "/" + dept;
          System.out.print(prefixType);
            TRNIndex trnIndex = tRNIndexRepo.findByPrefix(prefixType)
                    .orElseGet(() -> {
                        TRNIndex t = new TRNIndex();
                        t.setPrefix(prefixType);
                        t.setVouno(0L);
                        return t;
                    });
            if(trnIndex.getVouno()==null)
            {
            	trnIndex.setVouno(0L);
            }
            Long newNo = trnIndex.getVouno() + 1;
            trnIndex.setVouno(newNo);
            tRNIndexRepo.save(trnIndex);
            voucherEntry.setVouno("VOU-" + isono + "/" + plant + "/" + dept + "/(" + prefix + ")-" + newNo);
            VoucherEntry result = voucherEntryRepository.save(voucherEntry);
            saveVoucherItems(result, voucherEnrtyDto);
            saveDesc(result, voucherEnrtyDto);
            return new ResponseEntity<Status>(new Status("200", "Voucher created  successfully!!!"), HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException("Voucher save failed: " + e.getMessage(), e);
        }
    }

    public void saveVoucherItems(VoucherEntry result, VoucherEnrtyDto voucherEnrtyDto) {
        if (!voucherEnrtyDto.getItems().isEmpty() && !voucherEnrtyDto.getItems().isEmpty()) {
            List<VoucherItems> vouitems = voucherEnrtyDto.getItems().stream().map(dto -> {
                VoucherItems item = new VoucherItems(dto);
                Optional<Expense>  optional = expenseRepository.findById(dto.getRefid());
				if(optional.isPresent()){
					item.setSecExpName(StringEscapeUtils.unescapeHtml4(optional.get().getRname()));
				}
                item.setVouid(result.getId());
                item.setVouno(result.getVouno());
                item.setFormid(result.getFormid());
                item.setBranch(result.getBranch());
                item.setDept(result.getDept());
                item.setCid(result.getCompany());
                item.setReftype("EXP");
                item.setStatus(result.getStatus());
                saveSharedDetails(result, item, dto.getSharedDetails());
                return item;
            }).collect(Collectors.toList());
            voucherItemsRepository.saveAll(vouitems);
        }
    }

    public void saveDesc(VoucherEntry result, VoucherEnrtyDto voucherEnrtyDto) {
        if (!voucherEnrtyDto.getItems().isEmpty() && !voucherEnrtyDto.getItems().isEmpty()) {
            List<VoucherDesc> vouDesc = voucherEnrtyDto.getItems().stream().map(dto -> {
                VoucherDesc desc = new VoucherDesc();
                desc.setBId(result.getId());
                desc.setBatchNo(result.getVouno());
                desc.setDescription(voucherEnrtyDto.getDescription());
                desc.setRefId(dto.getRefid());
                return desc;
            }).collect(Collectors.toList());
            voucherDescRepository.saveAll(vouDesc);
        }
    }

    public void saveSharedDetails(VoucherEntry result, VoucherItems item, List<VoucherSharedMemberDetailsDto> sharedDetails) {
        if (sharedDetails != null && !sharedDetails.isEmpty()) {
            List<VoucherSharedMemberDetails> vexgens = sharedDetails.stream().map(shared -> {
                VoucherSharedMemberDetails vex = new VoucherSharedMemberDetails(shared);
                vex.setBid(result.getId());
                vex.setBatchno(result.getVouno());
                vex.setContype(item.getContype());
                vex.setRefid(item.getRefid());
                System.out.println("contype"+item.getContype());
                if ("STAFF".equalsIgnoreCase(item.getContype()) || "man".equalsIgnoreCase(item.getContype())) {
                    Long deptId = customVouNameRepository.findDeptIdByStaffId(shared.getConid());
                    Long branchId = customVouNameRepository.findBranchIdByStaffId(shared.getConid());
                    vex.setCondept(deptId);
                    vex.setConbranch(branchId);
                } else if ("DEPT".equalsIgnoreCase(item.getContype())) {
                    vex.setCondept(shared.getConid());
                } else if ("MACHINE".equalsIgnoreCase(item.getContype())) {
                	  System.out.println("conid"+shared.getConid());
                    Long deptId = customVouNameRepository.findDeptIdByMachineId(shared.getConid());
                    Long branchId = customVouNameRepository.findBranchIdByMachineId(shared.getConid());
                    System.out.println( "branch"+branchId);
                    vex.setCondept(deptId);
                    vex.setConbranch(branchId);
                } else if ("contractLabors".equalsIgnoreCase(item.getContype()) || "dwages".equalsIgnoreCase(item.getContype())) {
                    Long deptId = emastDailyRepository.findDeptIdByStaffId(shared.getConid());
                    Long branchId = emastDailyRepository.findBranchIdByStaffId(shared.getConid());
                    vex.setCondept(deptId);
                    vex.setConbranch(branchId);
                }
                return vex;
            }).collect(Collectors.toList());

            voucherSharedMemberDetailsRepository.saveAll(vexgens);

        }

    }

    public ResponseEntity<?> getAllVoucherList(User user, Pageable pageable, Long formId, Long dept, Long branch,
                                               String status, Long deptId, Long branchId, Long orgId, String grpNo, String vouNo, String vouType, String vouName, String createdDate) {
    	Date fromDate = null;

		if (createdDate != null ) {
			fromDate = dateUtil.getStringTODateWk(createdDate);
		}
		Page<VoucherEntryList> voucherList = voucherEntryRepository .filterVoucherList(formId,dept,branch,status,deptId,branchId,orgId,grpNo,vouNo,vouType,vouName,fromDate ,pageable);
		List<VoucherEntryListDto> dtoList = voucherList.stream()
		        .map(VoucherEntryListDto::new)
		        .collect(Collectors.toList());		
		 PageDataDto<VoucherEntryListDto> pageData = new PageDataDto<>(dtoList, voucherList);
	        return new ResponseEntity<>(pageData, HttpStatus.OK);

    }

    @Transactional
    public ResponseEntity<?> voucherVerifyOrApproveOrAuthorize(User user, Long id, Long vouno, String type) {
        Optional<VoucherEntry> voucher = voucherEntryRepository.findById(id);
        VoucherEntry voucherEntry = voucher.get();
        if ("Verify".equalsIgnoreCase(type) && voucherEntry.getGrpNo()==null) {
        	if(voucherEntry.getStatus().equalsIgnoreCase("Created")) {
            voucherEntry.setVerify("Y");
            voucherEntry.setVdate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            voucherEntry.setVstaff(user.getId());
            voucherEntry.setStatus("Verified");
        	}else {
        		return new ResponseEntity<>(new Status("400", "Invalid action type: " + type), HttpStatus.BAD_REQUEST);
            }
        } else if ("Approve".equalsIgnoreCase(type) && voucherEntry.getGrpNo()==null) {
        	if(voucherEntry.getStatus().equalsIgnoreCase("Verified")) {
            voucherEntry.setApproval("Y");
            voucherEntry.setApdate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            voucherEntry.setApstaff(user.getId());
            voucherEntry.setStatus("Approved");
        	}else {
        		return new ResponseEntity<>(new Status("400", "Invalid action type: " + type), HttpStatus.BAD_REQUEST);
            }
        } else if ("Authorize".equalsIgnoreCase(type) && voucherEntry.getGrpNo()==null) {
        	if(voucherEntry.getStatus().equalsIgnoreCase("Approved")) {
            System.out.print("Authorized");
            voucherEntry.setAuthenty("Y");
            voucherEntry.setAdate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            voucherEntry.setAstaff(user.getId());
            voucherEntry.setStatus("Authorized");
        	}else {
        		return new ResponseEntity<>(new Status("400", "Invalid action type: " + type), HttpStatus.BAD_REQUEST);
              	}
        } else if ("Reject".equalsIgnoreCase(type) &&voucherEntry.getGrpNo()==null) {
        	if(voucherEntry.getStatus().equalsIgnoreCase("Rejected") ){
        		return new ResponseEntity<>(new Status("400", "Invalid action type: " + type), HttpStatus.BAD_REQUEST);
        	
        	}else {
        		voucherEntry.setReject("Y");
            	voucherEntry.setRBy(user.getId());
            	voucherEntry.setRDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                voucherEntry.setStatus("Rejected");
        	}
        } else {
            return new ResponseEntity<>(new Status("400", "Invalid action type: " + type), HttpStatus.BAD_REQUEST);
        }
        VoucherEntry result = voucherEntryRepository.save(voucherEntry);
        List<VoucherItems> findByVouid = voucherItemsRepository.findByVouid(id);
        for (VoucherItems item : findByVouid) {
            item.setStatus(result.getStatus());
        }

        voucherItemsRepository.saveAll(findByVouid);
        return new ResponseEntity<>(new Status("200", "Voucher " + type + " successful"), HttpStatus.OK);
    }

    public VoucherEntryResponseDto getVoucherEntry(Long id, User user) {
        Optional<VoucherEntry> voucherOpt = voucherEntryRepository.findById(id);
        if (voucherOpt.isPresent()) {
            VoucherEntry voucher = voucherOpt.get();           
            VoucherEntryResponseDto dto = new VoucherEntryResponseDto(voucher);
            if(dto.getVouName()==null)
            {
            	Optional<CustomVoucherName> findById = customVouNameRepository.findById(dto.getFormid());
                if(findById.isPresent())
                {
                	dto.setVouName(findById.get().getName());
                }            	
            }
            Optional<VoucherPrintList> vouPrint = voucherEntryRepository.findPrintByVouid(id);
            if(vouPrint.isPresent())
            {
            	dto.setCreatedPerson(vouPrint.get().getCreatedPerson());
            	dto.setVerifiedPerson(vouPrint.get().getVerifiedPerson());
            	dto.setApprovedPerson(vouPrint.get().getApprovedEmployee());
            	dto.setAuthorizedPerson(vouPrint.get().getAuthorizedPerson());
            	dto.setRejectedPerson(vouPrint.get().getRejectedPerson());
            }

            List<VoucherItems> items = voucherItemsRepository.findByVouid(voucher.getId());
            List<VoucherItemsDto> itemDtos = items.stream().map(item -> {
                VoucherItemsDto itemDto = new VoucherItemsDto(item);
                System.out.println(itemDto.getSecondaryExpenseName());
                if(itemDto.getSecondaryExpenseName()==null) {
                	System.out.println(itemDto.getSecondaryExpenseName());
				Optional<Expense>  optional = expenseRepository.findById(itemDto.getRefid());
				if(optional.isPresent()){
					itemDto.setSecondaryExpenseName(StringEscapeUtils.unescapeHtml4(optional.get().getRname()));
				}
                }
                List<VoucherSharedMemberDetails> sharedList = voucherSharedMemberDetailsRepository.findByBid(item.getVouid(), item.getRefid());
                List<VoucherSharedMemberDetailsDto> sharedDtos = sharedList.stream()
                        .map(shared -> new VoucherSharedMemberDetailsDto(shared))
                        .collect(Collectors.toList());
                itemDto.setSharedDetails(sharedDtos);
                return itemDto;
            }).collect(Collectors.toList());
            dto.setSitems(itemDtos);
            if (voucher.getVouType() != null && !voucher.getVouType().isEmpty()) {
                if ("self-claim".equalsIgnoreCase(voucher.getVouType())) {
                    Long staffId = voucher.getIstaff();
                    Optional<VUserBankDetails> findByBankDetails = userRepository.findByBankDetails(staffId);
                    if (findByBankDetails.isPresent()) {
                        VUserBankDetailsDto vBankDetails = new VUserBankDetailsDto(findByBankDetails.get());
                        dto.setPayableToName(vBankDetails.getName());
                        dto.setBankacno(vBankDetails.getBAcNo());
                        dto.setBankcode(vBankDetails.getBName());
                        dto.setIfscCode(vBankDetails.getIfscCode());
                    }
                }
            }
            VoucherDesc findByBid = voucherDescRepository.findByBid(voucher.getId());
            dto.setDescription(findByBid.getDescription());
            CompanyList findByFirmWithOrgAddress = organisationRepository.findByFirmWithOrgAddress(voucher.getCompany());
            dto.setAddress(findByFirmWithOrgAddress.getAddress());
            dto.setCCity(findByFirmWithOrgAddress.getCity());
            dto.setCState(findByFirmWithOrgAddress.getState());
            dto.setCEmail(findByFirmWithOrgAddress.getEmail());
            dto.setCMobileNo(findByFirmWithOrgAddress.getMobileNo());
            dto.setCPhoneNo(findByFirmWithOrgAddress.getPhoneNo());
            dto.setCPincode(findByFirmWithOrgAddress.getPinCode());
            dto.setCName(findByFirmWithOrgAddress.getName());
            return dto;
        } else {
            return new VoucherEntryResponseDto();
        }

    }

    public ResponseEntity<?> voucherListGrp(Long vouid, Long formid, Long dept, Long branch, String dateFrom,
                                            String dateTo) {
        Date fromDate = null, toDate = null;

        if (dateFrom != null && dateTo != null) {
            fromDate = dateUtil.getStringTODateWk(dateFrom);
            toDate = dateUtil.getStringTODateWk(dateTo);
        }


        List<VoucherEntry> voucherList = voucherEntryRepository.filterVoucherListGroup(vouid, formid, dept, branch, fromDate, toDate);
        List<VoucherEntryResponseDto> dtoList = voucherList.stream()
                .map(voucher -> {
                    VoucherEntryResponseDto dto = new VoucherEntryResponseDto(voucher);
                    if (voucher.getVouType() != null && !voucher.getVouType().isEmpty()) {
                        if ("self-claim".equalsIgnoreCase(voucher.getVouType())) {
                            Long staffId = voucher.getIstaff();
                            Optional<VUserBankDetails> findByBankDetails = userRepository.findByBankDetails(staffId);
                            if (findByBankDetails.isPresent()) {
                                VUserBankDetailsDto vBankDetails = new VUserBankDetailsDto(findByBankDetails.get());
                                System.out.println("data come");
                                System.out.print(vBankDetails.getName());
                                dto.setPayableToName(vBankDetails.getName());
                                dto.setBankacno(vBankDetails.getBAcNo());
                                dto.setBankcode(vBankDetails.getBName());
                                dto.setIfscCode(vBankDetails.getIfscCode());
                            }
                        }
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        return new ResponseEntity<List<VoucherEntryResponseDto>>(dtoList, HttpStatus.OK);

    }

    @Transactional
    public ResponseEntity<?> saveVoucherGroup(VouPaymentsDto vouPaymentsDto, User user) {
        try {
            VouPayments vouGroup = new VouPayments(vouPaymentsDto);
            vouGroup.setCby(user.getId());
            ZonedDateTime istNow = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            long epoch = istNow.toEpochSecond();
            vouGroup.setCon(String.valueOf(epoch));
            vouGroup.setConNew(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            String prefixType = "VPAY";
            TRNIndex trnIndex = tRNIndexRepo.findByPrefix(prefixType)
                    .orElseGet(() -> {
                        TRNIndex t = new TRNIndex();
                        t.setPrefix(prefixType);
                        t.setVouno(0L);
                        return t;
                    });
            if(trnIndex.getVouno()==null)
            {
            	trnIndex.setVouno(0L);
            }
            Long newNo = trnIndex.getVouno() + 1;
            trnIndex.setVouno(newNo);
            tRNIndexRepo.save(trnIndex);
            vouGroup.setTrnno(prefixType + "-" + newNo);
            vouGroup.setUsecid("VouConsol_" + epoch);
            vouGroup.setStatus("Created");
            vouGroup.setTrndate(dateUtil.getISTDate());
            VouPayments result = vouPaymentsRepository.save(vouGroup);
            if (vouPaymentsDto.getSinglevouid() != null && !vouPaymentsDto.getSinglevouid().isEmpty()) {
                List<VoucherEntry> vouchers = voucherEntryRepository.findAllById(vouPaymentsDto.getSinglevouid());
                for (VoucherEntry voucher : vouchers) {
                    voucher.setGrpNo(result.getTrnno());
                }
                voucherEntryRepository.saveAll(vouchers);
            }
            return new ResponseEntity<Status>(new Status("200", "Vouchergroup created  successfully!!!"), HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException("VoucherGroup save failed: " + e.getMessage(), e);
        }
    }

    public VoucherGroupResponseDto getVoucherGroup(Long id, User user) {
        Optional<VouPayments> optional = vouPaymentsRepository.findById(id);
        VoucherGroupPrintList findPrintByGroupId = vouPaymentsRepository.findPrintByGroupId(id);
        System.out.println("find :" +findPrintByGroupId);
        if (optional.isPresent()) {
            VouPayments vouPayments = optional.get();
            VoucherGroupResponseDto vouPayDto = new VoucherGroupResponseDto(vouPayments);
            vouPayDto.setCreatedPerson(findPrintByGroupId.getCreatedPerson());
            vouPayDto.setApprovedPerson(findPrintByGroupId.getApprovedPerson());
            vouPayDto.setAuthorizedPerson(findPrintByGroupId.getAuthorizedPerson());
            vouPayDto.setRejectedPerson(findPrintByGroupId.getRejectedPerson());
            vouPayDto.setVerifiedPerson(findPrintByGroupId.getVerifiedPerson());
            List<VoucherEntry> vouchers = voucherEntryRepository.findByGrpno(vouPayments.getTrnno());
            List<VoucherEntryResponseDto> voucherDtos = vouchers.stream().map(voucher -> {
                VoucherEntryResponseDto dto = new VoucherEntryResponseDto(voucher);
                if(dto.getVouName()==null)
                {
                	Optional<CustomVoucherName> findById = customVouNameRepository.findById(dto.getFormid());
                    if(findById.isPresent())
                    {
                    	dto.setVouName(findById.get().getName());
                    }            	
                }
                List<VoucherItems> items = voucherItemsRepository.findByVouid(voucher.getId());
                List<VoucherItemsDto> itemDtos = items.stream().map(item -> {
                    VoucherItemsDto itemDto = new VoucherItemsDto(item);
                    System.out.println(itemDto.getSecondaryExpenseName());
                    if(itemDto.getSecondaryExpenseName()==null) {
                    	System.out.println(itemDto.getSecondaryExpenseName());
                    Optional<Expense>  Op = expenseRepository.findById(itemDto.getRefid());
    				if(Op.isPresent()){
    					itemDto.setSecondaryExpenseName(StringEscapeUtils.unescapeHtml4(Op.get().getRname()));
    				}
                    }
                    List<VoucherSharedMemberDetails> sharedList =
                            voucherSharedMemberDetailsRepository.findByBid(item.getVouid(), item.getRefid());
                    List<VoucherSharedMemberDetailsDto> sharedDtos = sharedList.stream()
                            .map(VoucherSharedMemberDetailsDto::new)
                            .collect(Collectors.toList());
                    itemDto.setSharedDetails(sharedDtos);
                    return itemDto;
                }).collect(Collectors.toList());
                dto.setSitems(itemDtos);
                if (voucher.getVouType() != null && "self-claim".equalsIgnoreCase(voucher.getVouType())) {
                    Long staffId = voucher.getIstaff();
                    Optional<VUserBankDetails> findByBankDetails = userRepository.findByBankDetails(staffId);
                    if (findByBankDetails.isPresent()) {
                        VUserBankDetailsDto vBankDetails = new VUserBankDetailsDto(findByBankDetails.get());
                        dto.setPayableToName(vBankDetails.getName());
                        dto.setBankacno(vBankDetails.getBAcNo());
                        dto.setBankcode(vBankDetails.getBName());
                        dto.setIfscCode(vBankDetails.getIfscCode());
                    }
                }
                VoucherDesc findByBid = voucherDescRepository.findByBid(voucher.getId());
                if (findByBid != null) {
                    dto.setDescription(findByBid.getDescription());
                }
                CompanyList org = organisationRepository.findByFirmWithOrgAddress(voucher.getCompany());
                if (org != null) {
                    dto.setAddress(org.getAddress());
                    dto.setCCity(org.getCity());
                    dto.setCState(org.getState());
                    dto.setCEmail(org.getEmail());
                    dto.setCMobileNo(org.getMobileNo());
                    dto.setCPhoneNo(org.getPhoneNo());
                    dto.setCPincode(org.getPinCode());
                    dto.setCName(org.getName());
                }
                return dto;
            }).collect(Collectors.toList());
//            vouPayDto.setSingleVouchers(voucherDtos);
            Map<String, List<VoucherEntryResponseDto>> vouchersByPayableName = voucherDtos.stream()
                    .filter(dto -> dto.getPayableToName() != null)
                    .collect(Collectors.groupingBy(VoucherEntryResponseDto::getPayableToName));

            vouPayDto.setVouchersByPayableName(vouchersByPayableName);

            return vouPayDto;
        } else {
            return new VoucherGroupResponseDto();
        }
    }

    @Transactional
    public ResponseEntity<?> voucherGroupVerifyOrApproveOrAuthorize(User user, Long id, Long vouno, String type) {

        Optional<VouPayments> voucher = vouPaymentsRepository.findById(id);
        VouPayments voucherPayments = voucher.get();
        List<VoucherEntry> linkedVouchers = voucherEntryRepository.findByGrpno(voucherPayments.getTrnno());
        if ("Verify".equalsIgnoreCase(type)) {
        	if(voucherPayments.getStatus().equalsIgnoreCase("Created")) {
            voucherPayments.setVerify("Y");
            ZonedDateTime istNow = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            long epoch = istNow.toEpochSecond();
            voucherPayments.setVon((String.valueOf(epoch)));
            voucherPayments.setVby(user.getId());
            voucherPayments.setStatus("Verified");
            voucherPayments.setVonNew(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        	}else {
        		 return new ResponseEntity<>(new Status("400", "Invalid action type: " + type), HttpStatus.BAD_REQUEST);
        	}
        } else if ("Approve".equalsIgnoreCase(type)) {
        	if(voucherPayments.getStatus().equalsIgnoreCase("Created")) {
            voucherPayments.setApprove("Y");
            ZonedDateTime istNow = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            long epoch = istNow.toEpochSecond();
            voucherPayments.setApon((String.valueOf(epoch)));
            voucherPayments.setApby(user.getId());
            voucherPayments.setStatus("Approved");
            voucherPayments.setAponNew(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            for (VoucherEntry voucherEntry : linkedVouchers) {
                voucherEntry.setApproval("Y");
                voucherEntry.setApdate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                voucherEntry.setApstaff(user.getId());
                voucherEntry.setStatus("Approved");
            }
        	}else {
       		 return new ResponseEntity<>(new Status("400", "Invalid action type: " + type), HttpStatus.BAD_REQUEST);
       	}
        } else if ("Authorize".equalsIgnoreCase(type)) {
        	if(voucherPayments.getStatus().equalsIgnoreCase("Approved")) {
            voucherPayments.setAuthenty("Y");
            ZonedDateTime istNow = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            long epoch = istNow.toEpochSecond();
            voucherPayments.setAon((String.valueOf(epoch)));
            voucherPayments.setAby(user.getId());
            voucherPayments.setStatus("Authorized");
            voucherPayments.setAonNew(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            for (VoucherEntry voucherEntry : linkedVouchers) {
                voucherEntry.setAuthenty("Y");
                voucherEntry.setAdate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                voucherEntry.setAstaff(user.getId());
                voucherEntry.setStatus("Authorized");
            }
        	}else {
          		 return new ResponseEntity<>(new Status("400", "Invalid action type: " + type), HttpStatus.BAD_REQUEST);
           	}
        } else if ("Reject".equalsIgnoreCase(type)) {
        	if(voucherPayments.getStatus().equalsIgnoreCase("Rejected")) {
        		 return new ResponseEntity<>(new Status("400", "Invalid action type: " + type), HttpStatus.BAD_REQUEST);     	     	 
        	}else {
        		 voucherPayments.setStatus("Rejected");
                 voucherPayments.setReject("Y");
                 voucherPayments.setRBy(user.getId());
                 voucherPayments.setRDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                 for (VoucherEntry voucherEntry : linkedVouchers) {
                     voucherEntry.setGrpNo(null);
                 }
           	}
             } else {
            return new ResponseEntity<>(new Status("400", "Invalid action type: " + type), HttpStatus.BAD_REQUEST);
        }
        VouPayments savePayments = vouPaymentsRepository.save(voucherPayments);
        List<VoucherEntry> result = voucherEntryRepository.saveAll(linkedVouchers);
        List<VoucherItems> findByVouid = null;
        if (savePayments.getStatus() != "Rejected") {
            for (VoucherEntry entry : result) {
                findByVouid = voucherItemsRepository.findByVouid(entry.getId());
                for (VoucherItems item : findByVouid) {
                    item.setStatus(entry.getStatus());
                }
            }
            voucherItemsRepository.saveAll(findByVouid);
        }
        return new ResponseEntity<>(new Status("200", "Voucher " + type + " successful"), HttpStatus.OK);
    }

    public ResponseEntity<?> getAllVoucherGrpList(User user, Pageable pageable, Long formid, Long dept, Long branch,
                                                  String status, String grpNo, String dateFrom, String dateTo, Long deptId, Long branchId, Long orgId) {

        Date fromDate = null, toDate = null;

        if (dateFrom != null && dateTo != null) {
            fromDate = dateUtil.getStringTODateWk(dateFrom);
            toDate = dateUtil.getStringTODateWk(dateTo);
        }

        Page<VoucherGrpList> filterList = vouPaymentsRepository.GetFilterList(formid, dept, branch, grpNo, fromDate, toDate, status,deptId,branchId,orgId, pageable);
        List<VoucherGrpListDto> dtoList = filterList.stream()
                .map(VoucherGrpListDto::new)
                .collect(Collectors.toList());
        PageDataDto<VoucherGrpListDto> pageData = new PageDataDto<>(dtoList, filterList);
        return new ResponseEntity<>(pageData, HttpStatus.OK);
    }
    public VoucherPrintDto getVoucherPrint(Long id, User user) {
    	Optional<VoucherPrintList> optional = voucherEntryRepository.findPrintByVouid(id);
    	if(optional.isPresent())
    	{
    		VoucherPrintDto voucherPrintList = new VoucherPrintDto( optional.get());		
    		if (voucherPrintList.getVouType() != null && "self-claim".equalsIgnoreCase(voucherPrintList.getVouType())) {
                Long staffId = voucherPrintList.getEmpId();
                Optional<VUserBankDetails> findByBankDetails = userRepository.findByBankDetails(staffId);
                if (findByBankDetails.isPresent()) {
                    VUserBankDetailsDto vBankDetails = new VUserBankDetailsDto(findByBankDetails.get());
                    voucherPrintList.setPayTo(vBankDetails.getName());
                    voucherPrintList.setBankAcNo(vBankDetails.getBAcNo());
                    voucherPrintList.setBankName(vBankDetails.getBName());
                    voucherPrintList.setIfscCode(vBankDetails.getIfscCode());
                }
            }     
    		List<VoucherItemsPrint> vouitems = voucherItemsRepository.findExpenseByVouid(voucherPrintList.getId());
    		List<VoucherItemsPrintDto> itemDtos = vouitems.stream().map(dto->{
    	            	VoucherItemsPrintDto voucherItemsPrintDto = new VoucherItemsPrintDto(dto);
    	            	System.out.println(dto.getSecName());
    	            	if(dto.getSecName()==null)
    	            	{
    	            		 Optional<Expense>  Op = expenseRepository.findById(dto.getRefId());
    	     				if(Op.isPresent()){
    	     					voucherItemsPrintDto.setSecName(StringEscapeUtils.unescapeHtml4(Op.get().getRname()));
    	     				}
    	            	}
    	            	return voucherItemsPrintDto;
    	            })
    	            .collect(Collectors.toList());
    	voucherPrintList.setSitems(itemDtos);
    		return voucherPrintList;
    	}else {	
    	return  new VoucherPrintDto();
    	}
    	
    }

    public VoucherGroupPrintDto getVoucherGroupPrint(Long id, User user) {
    	VoucherGroupPrintList findPrintByGroupId = vouPaymentsRepository.findPrintByGroupId(id);
        if (findPrintByGroupId == null) {
            throw new RuntimeException("Voucher group not found for ID: " + id);
        }
        
        VoucherGroupPrintDto voucherGroupPrintDto = new VoucherGroupPrintDto(findPrintByGroupId);   
        Optional<VoucherEntry> findByVoucherName = voucherEntryRepository.findByVoucherName(voucherGroupPrintDto.getGrpNo());
        if(findByVoucherName.isPresent())
        {
        	voucherGroupPrintDto.setCVouName(findByVoucherName.get().getVouName());
        }
        if(voucherGroupPrintDto.getCVouName()==null)
        {        	
        	Optional<CustomVoucherName> vouName = customVouNameRepository.findById(findPrintByGroupId.getCVouId());
        	if(vouName.isPresent())
        	{
        		voucherGroupPrintDto.setCVouName(vouName.get().getName());
        	}
        }
        List<Long> vouIdList = voucherEntryRepository.findByGrpnoList(voucherGroupPrintDto.getGrpNo());  
        System.out.println(vouIdList);
        List<GroupNameList> groupNameList = voucherItemsRepository.findByAmount(vouIdList);
        List<GroupNameListDto> dtoList = new ArrayList<>();
        for (GroupNameList nameList : groupNameList) {
            GroupNameListDto dto = new GroupNameListDto(nameList);
            dtoList.add(dto);
        }       
        voucherGroupPrintDto.setList(dtoList);   

        return voucherGroupPrintDto;
      
       
    }

}
