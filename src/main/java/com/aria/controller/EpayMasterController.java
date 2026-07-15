package com.aria.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.aria.dto.EpayMasterDto;
import com.aria.model.EpayMaster;
import com.aria.service.EpayMasterService;

@Controller
@CrossOrigin
@RequestMapping("/epaymaster")
public class EpayMasterController {
	@Autowired
	private EpayMasterService epayMasterService;
	
	@PostMapping("/createStaff")
	public ResponseEntity<?> uploadBulkEmlpoyeeForStaff(@RequestBody EpayMasterDto epayMasterDto,Authentication authentication) throws IOException{
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		return epayMasterService.createStaff(epayMasterDto);
	}
	
	@PostMapping("/updateBulkStaff")
	public ResponseEntity<?> uploadBulkEmlpoyeeForStaffBasedOnEmail(@RequestParam("file") MultipartFile file,Authentication authentication) throws IOException{
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		return epayMasterService.uploadBulkEmlpoyeeForStaffBasedOnEmail(file);
	}
	
	@GetMapping("/allEpayMasterEmployee")
	public ResponseEntity<?> allEpayMasterEmployee(Authentication authentication){
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		try {
				List<EpayMaster> employees=epayMasterService.allEpayMasterEmployee();
				if(employees.size()>0)
					return new ResponseEntity<Object>(employees,HttpStatus.OK);
				return new ResponseEntity<Object>("No users Exist..",HttpStatus.OK);
		}
		catch(Exception e){
			return new ResponseEntity<Object>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}	
	}
	
	@GetMapping("/singleEpayMasterEmployee")
	public ResponseEntity<?> allEpayMasterEmployee(@RequestParam("employeeId") Long employeeId, Authentication authentication){
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		try {
				EpayMaster employee=epayMasterService.singleEpayMasterEmployee(employeeId);
				if(employee!=null)
					return new ResponseEntity<Object>(employee,HttpStatus.OK);
				return new ResponseEntity<Object>("user Not Found..",HttpStatus.OK);
		}
		catch(Exception e){
			return new ResponseEntity<Object>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}	
	}
	
	@DeleteMapping("/deleteEpaymaster")
	public ResponseEntity<?> deleteEpaymaster(@RequestParam("employeeId") Long employeeId,Authentication authentication){
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		try {
				if(epayMasterService.deleteEpaymaster(employeeId))
					return new ResponseEntity<Object>("Employee Deleted successfully...",HttpStatus.OK);	
				return new ResponseEntity<Object>("Employee Not Deleted ...",HttpStatus.OK);	
		}
		catch(Exception e){
			return new ResponseEntity<Object>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}	
	}
}
