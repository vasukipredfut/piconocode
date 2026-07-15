package com.aria.controller;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dao.ExpenseVoucherRepository;
import com.aria.dao.UserRepository;
import com.aria.dto.CustomVouNameDropDownDto;
import com.aria.dto.CustomVoucherNameDto;
import com.aria.dto.ExpenseDto;
import com.aria.dto.ExpenseVoucherDto;
import com.aria.dto.VouPaymentsDto;
import com.aria.dto.VoucherEnrtyDto;
import com.aria.dto.VoucherEntryResponseDto;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.service.CustomVoucherService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class CustomVoucherController {

	 @Autowired
	    UserRepository userRepository;
	
	 @Autowired
	 CustomVoucherService customVoucherService; 	 
	
	
	@PostMapping("/addOrEdit/customVoucher")
    public ResponseEntity<?> addOrEditCustomVoucher(@RequestBody CustomVoucherNameDto customVoucherNameDto, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());       
        if (  customVoucherNameDto.getId() == null || customVoucherNameDto.getId() == 0) {
        return	customVoucherService.saveCustomVoucher(customVoucherNameDto, user.get());           
        } else {
        return	customVoucherService.updateCustomVoucher(customVoucherNameDto, user.get());
        }		
    }
	
	 @GetMapping("/customVoucher/{id}")
	    public ResponseEntity<?> customVoucher(@PathVariable Long id,Authentication authentication) {
	        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
	        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
	        return new ResponseEntity<CustomVoucherNameDto>(customVoucherService.getCustomVoucher(id), HttpStatus.OK);
	    }
	 @GetMapping("/customVoucherList")
	    public ResponseEntity<?> customVoucherList(@RequestParam(value = "name",required = false) String name,@RequestParam(value = "dept",required = false) Long dept,@RequestParam(value = "firmId",required = false) Long firmId, Authentication authentication,Pageable pageable) {
	        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
	        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
	        return customVoucherService.getAllcustomVoucher(user.get(),pageable,name,dept,firmId);
	    }
	
	 @GetMapping("/customVoucherbydept/{dept}")
	    public ResponseEntity<?> secondaryExpense(@PathVariable Long dept,Authentication authentication) {
	        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
	        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
	        return customVoucherService.getCustomVoucherNameList(dept);
	    }
	 
	 @PostMapping("/addOrEdit/expenseVoucher")
	    public ResponseEntity<?> addOrEditExpenseVoucher(@RequestBody ExpenseVoucherDto expenseVoucherDto, Authentication authentication) {
	        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
	        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());       
	        if (  expenseVoucherDto.getId() == null || expenseVoucherDto.getId() == 0) {
	        return	customVoucherService.saveExpenseVoucher(expenseVoucherDto, user.get());           
	        } else {
	        return	customVoucherService.updateExpenseVoucher(expenseVoucherDto, user.get());
	        }		
	    }
		
	 @GetMapping("/expenseVoucher/{id}")
	    public ResponseEntity<?> expenseVoucher(@PathVariable Long id,Authentication authentication) {
	        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
	        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
	        return customVoucherService.getexpenseVoucher(id);
	    }
	 @GetMapping("/expenseVoucherList")
	    public ResponseEntity<?> expenseVoucherList(@RequestParam(value = "name",required = false) String name,@RequestParam(value = "dept",required = false) Long dept,@RequestParam(value = "rname",required = false) String rname, Authentication authentication,Pageable pageable) {
	        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
	        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
	        return customVoucherService.getExpenseVoucherList(user.get(),pageable,name,dept,rname);
	    }
	 @GetMapping("/customVoucherName")
	    public ResponseEntity<?> customVoucherName(Authentication authentication) {
	        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
	        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
	        return customVoucherService.customVoucherName(user.get());
	    }
	 
	 @GetMapping("/expGroupData")
	    public ResponseEntity<?> expGroupData(@RequestParam Long vouId, Authentication authentication) {
	        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
	        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
	        return customVoucherService.expGroupData(vouId,user.get());
	    }
	 
	 @PostMapping("/saveVoucherEntry")
	    public ResponseEntity<?> saveVoucherEntry(@RequestBody VoucherEnrtyDto voucherEnrtyDto, Authentication authentication) {
	        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
	        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());       
	        if (  voucherEnrtyDto.getId() == null || voucherEnrtyDto.getId() == 0) {
	        return	customVoucherService.saveVoucherEntry(voucherEnrtyDto, user.get());           
	        } else {
	        return	customVoucherService.saveVoucherEntry(voucherEnrtyDto, user.get());
	        }		
	    }
	 
	 @GetMapping("/voucherList")
	    public ResponseEntity<?> voucherList(@RequestParam(value = "formid",required = false) Long formid,@RequestParam(value = "dept",required = false) Long dept,@RequestParam(value = "branch",required = false) Long branch,@RequestParam(value = "status",required = false) String status,@RequestParam(value = "deptId",required = false) Long deptId,@RequestParam(value = "branchId",required = false) Long branchId ,@RequestParam(value = "orgId",required = false) Long orgId , @RequestParam(value = "grpNo",required = false) String  grpNo , @RequestParam(value = "vouNo",required = false) String  vouNo,@RequestParam(value = "vouType",required = false) String  vouType,@RequestParam(value = "createdDate",required = false) String createdDate,@RequestParam(value = "vouName",required = false) String  vouName, Authentication authentication,Pageable pageable) {
	        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
	        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
	        return customVoucherService.getAllVoucherList(user.get(),pageable,formid,dept,branch,status , deptId , branchId, orgId ,grpNo ,vouNo,vouType,vouName,createdDate );
	    }
	 @PutMapping("/voucherVerifyOrApproveOrAuthorize")
	    public ResponseEntity<?> voucherVerifyOrApproveOrAuthorize(@RequestParam(value = "id",required = false) Long id,@RequestParam(value = "vouno",required = false) Long vouno,@RequestParam(value = "type",required = false) String type, Authentication authentication) {
	        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
	        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
	        return customVoucherService.voucherVerifyOrApproveOrAuthorize(user.get(),id,vouno,type);
	 }
	 @GetMapping("/getVoucherEntry/{id}")
	    public ResponseEntity<?> getVoucherEntry(@PathVariable Long id,Authentication authentication) {
	        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
	        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
	        System.out.println("controll reaced");
	        return new ResponseEntity<>(customVoucherService.getVoucherEntry(id ,user.get()), HttpStatus.OK);
	    }
	 
	 @GetMapping("/voucherListGrp")
	    public ResponseEntity<?> voucherListGrp(@RequestParam(value = "formid",required = false) Long formid,@RequestParam(value = "dept",required = false) Long dept,@RequestParam(value = "branch",required = false) Long branch,@RequestParam(value = "vouid",required = false) Long vouid,@RequestParam(value = "dateFrom",required = false) String dateFrom ,@RequestParam(value = "dateTo",required = false) String dateTo , Authentication authentication) {
	        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
	        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
	        return customVoucherService.voucherListGrp(vouid,formid,dept,branch,dateFrom,dateTo);
	    }
	 @PostMapping("/saveVoucherGroup")
	    public ResponseEntity<?> saveVoucherGroup(@RequestBody VouPaymentsDto vouPaymentsDto, Authentication authentication) {
	        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
	        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());       
//	        if (  voucherEnrtyDto.getId() == null || voucherEnrtyDto.getId() == 0) {
	        return	customVoucherService.saveVoucherGroup(vouPaymentsDto, user.get());           
//	        } else {
//	        return	customVoucherService.saveVoucherEntry(voucherEnrtyDto, user.get());
//	        }		
	    }
	 @GetMapping("/getVoucherGroup/{id}")
	    public ResponseEntity<?> getVoucherGroup(@PathVariable Long id,Authentication authentication) {
	        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
	        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
	        System.out.println("controll reaced");
	        return new ResponseEntity<>(customVoucherService.getVoucherGroup(id ,user.get()), HttpStatus.OK);
	    }
	 @PutMapping("/voucherGroupVerifyOrApproveOrAuthorize")
	    public ResponseEntity<?> voucherGroupVerifyOrApproveOrAuthorize(@RequestParam(value = "id",required = false) Long id,@RequestParam(value = "vouno",required = false) Long vouno,@RequestParam(value = "type",required = false) String type, Authentication authentication) {
	        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
	        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
	        return customVoucherService.voucherGroupVerifyOrApproveOrAuthorize(user.get(),id,vouno,type);
	 }
	 @GetMapping("/voucherGroupList")
	    public ResponseEntity<?> voucherGroupList(@RequestParam(value = "formid",required = false) Long formid,@RequestParam(value = "dept",required = false) Long dept,@RequestParam(value = "branch",required = false) Long branch,@RequestParam(value = "status",required = false) String status,@RequestParam(value = "grpNo",required = false) String grpNo ,@RequestParam(value = "dateFrom",required = false) String dateFrom ,@RequestParam(value = "dateTo",required = false) String dateTo,@RequestParam(value = "deptId",required = false) Long deptId,@RequestParam(value = "branchId",required = false) Long branchId ,@RequestParam(value = "orgId",required = false) Long orgId , Authentication authentication,Pageable pageable) {
	        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
	        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
	        return customVoucherService.getAllVoucherGrpList(user.get(),pageable,formid,dept,branch,status,grpNo ,dateFrom , dateTo , deptId , branchId , orgId);
	    }
	 @GetMapping("/getVoucherPrint/{id}")
	    public ResponseEntity<?> getVoucherPrint(@PathVariable Long id,Authentication authentication) {
	        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
	        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
	        System.out.println("controll reaced");
	        return new ResponseEntity<>(customVoucherService.getVoucherPrint(id ,user.get()), HttpStatus.OK);
	    }
	 @GetMapping("/getVoucherGroupPrint/{id}")
	    public ResponseEntity<?> getVoucherGroupPrint(@PathVariable Long id,Authentication authentication) {
	        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
	        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
	        System.out.println("controll reaced");
	        return new ResponseEntity<>(customVoucherService.getVoucherGroupPrint(id ,user.get()), HttpStatus.OK);
	 }
	 }

