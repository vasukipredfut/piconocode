package com.aria.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.aria.dao.UserRepository;
import com.aria.dto.DepayMasterDto;
import com.aria.model.DepayMaster;
import com.aria.model.User;
import com.aria.service.DepayMasterService;

@Controller
@CrossOrigin
@RequestMapping("/depaymaster")
public class DepayMasterController {
	@Autowired
	private DepayMasterService depayMasterService;

	@Autowired
	private UserRepository userRepository;
	
	@PostMapping("/creatEmployee")
	public ResponseEntity<?> uploadBulkEmlpoyeeForStaff(@RequestBody DepayMasterDto depayMasterDto,Authentication authentication) throws IOException{
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		return depayMasterService.createEmployee(depayMasterDto);
	}
	
	@PostMapping("/updateBulkEmlpoyee")
	public ResponseEntity<?> uploadBulkEmlpoyeeForStaffBasedOnEmail(@RequestParam("file") MultipartFile file,Authentication authentication) throws IOException{
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		return depayMasterService.uploadBulkEmlpoyeeForStaffBasedOnEmail(file);
	}

	@GetMapping("/allDepayMasterEmployee")
	public ResponseEntity<?> allDepayMasterEmployee(Pageable page, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			try {
				return depayMasterService.allDepayMasterEmployee(page);
			} catch (Exception e) {
				return new ResponseEntity<Object>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}else
			return new ResponseEntity<Object>("Invalid User...", HttpStatus.OK);
	}

	@GetMapping("/singleDepayMasterEmployee")
	public ResponseEntity<?> allDepayMasterEmployee(@RequestParam("employeeId") Long employeeId,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		try {
			DepayMaster employee = depayMasterService.singleDepayMasterEmployee(employeeId);
			if (employee != null)
				return new ResponseEntity<Object>(employee, HttpStatus.OK);
			return new ResponseEntity<Object>("user Not Found..", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<Object>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/deleteDepaymaster")
	public ResponseEntity<?> deleteEpaymaster(@RequestParam("employeeId") Long employeeId,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		try {
			if (depayMasterService.deleteDepaymaster(employeeId))
				return new ResponseEntity<Object>("Employee Deleted successfully...", HttpStatus.OK);
			return new ResponseEntity<Object>("Employee Not Deleted ...", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<Object>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

    @GetMapping("/dutyList")
    public ResponseEntity<?> dutyList(Pageable page,Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return depayMasterService.findAllByDesignation(page);   
    }

    @GetMapping(value = "/designationList")
    public ResponseEntity<?> designationList(Authentication authentication, @RequestParam(value = "designation", required = false) String designation,Pageable page) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return depayMasterService.findAllByDesignationByDesgnation(designation,page);
    }
    
    @GetMapping("/allListFilterUser")
    public ResponseEntity<?> filterMapListUser(@RequestParam(value = "branchId") Long branchId,
                                               @RequestParam(value = "departmentId", required = false) Long departmentId,
                                               @RequestParam(value = "designationId", required = false) Long designationId,
                                               Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return depayMasterService.wgesEmployeelistwfilter( branchId, departmentId, designationId);
        } else {
            return new ResponseEntity<List<User>>(new ArrayList<>(), HttpStatus.OK);
        }
    }
}
