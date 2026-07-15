package com.aria.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aria.dao.DepayMasterRepository;
import com.aria.dao.EmastDailyRepository;
import com.aria.dto.DepayMasterDto;
import com.aria.dto.ListUserDto;
import com.aria.dto.MyCellDto;
import com.aria.model.DepayMaster;
import com.aria.model.EmastDaily;
import com.aria.model.ListUser;
import com.aria.utils.ExcelUtil;

@Service
public class DepayMasterService {
	@Autowired
	private DepayMasterRepository depayMasterRepository;

	@Autowired
	private ExcelUtil excelUtil;

	@Autowired
	private EmastDailyRepository emastDailyRepository;

	public ResponseEntity<?> createEmployee(DepayMasterDto depayMasterDto) {
		Optional<EmastDaily> employeeDetail = emastDailyRepository.findById(Long.parseLong(depayMasterDto.getCode()));

		if (employeeDetail.get() != null) {
			DepayMaster employee = setDepayObject(depayMasterDto, employeeDetail.get());
			if (depayMasterRepository.save(employee) != null)
				return new ResponseEntity<Object>("Employee saved successfully...", HttpStatus.OK);
			return new ResponseEntity<Object>("Something Went Wrong...", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<Object>("Employee ID is wrong..", HttpStatus.OK);
	}

	public static DepayMaster setDepayObject(DepayMasterDto depayMasterDto, EmastDaily employeeDetail) {
		DepayMaster employee = new DepayMaster();
		employee.setCode(depayMasterDto.getCode());
		employee.setBasic(depayMasterDto.getBasic());
		employee.setEsiceil(depayMasterDto.getEsiceil());
		employee.setEpfbase(depayMasterDto.getEpfbase());
		employee.setStatus("0");
		employee.setBid(employeeDetail.getBranch());
//		employee.setDid(Integer.parseInt(employeeDetail.getDepartment().getId() + ""));
		employee.setSex(employeeDetail.getGender());
		employee.setFathername(employeeDetail.getFathername());
		employee.setEsino(employeeDetail.getEsiNo());
		employee.setEpfno(employeeDetail.getEpfno());
		employee.setName(employeeDetail.getName());
		return employee;
	}

	public ResponseEntity<?> uploadBulkEmlpoyeeForStaffBasedOnEmail(MultipartFile file) throws IOException {
		if (!ExcelUtil.checkExcelFormat(file))
			return null;

		Map<Integer, List<MyCellDto>> data = ExcelUtil.readExcel(file.getInputStream(), file.getOriginalFilename());
		List<DepayMaster> allEmployeeList = null;
		allEmployeeList = excelUtil.convertExcelToListOfUpdateEmployee(data);
		if(depayMasterRepository.saveAll(allEmployeeList)!=null)
			return new ResponseEntity<Object>("Employee Updated successfully...", HttpStatus.OK);
		return new ResponseEntity<Object>("Something went wrong...", HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public ResponseEntity<?> allDepayMasterEmployee(Pageable page) {
		Page<DepayMaster> employee = depayMasterRepository.findAll(page);
		if (!employee.toList().isEmpty()) {
			List<DepayMaster> employees = employee.toList().stream().map(emp -> {
				return emp;
			}).collect(Collectors.toList());
			return new ResponseEntity<Object>(employees, HttpStatus.OK);
		}
		return new ResponseEntity<Object>("NO user Exist...", HttpStatus.OK);
	}
	
	public DepayMaster singleDepayMasterEmployee(Long employeeId){
		Optional<DepayMaster> employee=depayMasterRepository.findById(employeeId);
		if(employee.isPresent())
			return employee.get();
		return null;
	}

	public Boolean deleteDepaymaster(Long employeeId) {
		Optional<DepayMaster> depayMaster=depayMasterRepository.findById(employeeId);
		if(depayMaster.isPresent()) {
			DepayMaster e=depayMaster.get();

			e.setStatus("1");
			if (depayMasterRepository.save(e) != null)
				return true;
		}
		return false;
	}

	public ResponseEntity<?> findAllByDesignation(Pageable page) {
		List<String> designations = depayMasterRepository.findAllByDesignation(page);
		if (designations.size() > 0)
			return new ResponseEntity<>(designations, HttpStatus.OK);
		return new ResponseEntity<>("No designation found...", HttpStatus.OK);

	}

	public ResponseEntity<?> findAllByDesignationByDesgnation(String designation,Pageable page) {
		if (designation != null || designation != "") {
			List<String> designations = depayMasterRepository.findAllByDesignationByDesgnation("%" + designation + "%",page);
			if (designations.size() > 0)
				return new ResponseEntity<>(designations, HttpStatus.OK);
		}
		return new ResponseEntity<>(depayMasterRepository.findAllByDesignation(page), HttpStatus.OK);

	}

	public ResponseEntity<?> wgesEmployeelistwfilter(Long branchId, Long departmentId, Long designationId) throws UsernameNotFoundException {
		List<ListUser> userList = depayMasterRepository.wagesEmployeelistwfilter(branchId, departmentId, designationId);

		return new ResponseEntity<List<ListUserDto>>(userList.stream().map(user -> {
			ListUserDto userDto = new ListUserDto(user);
			return userDto;
		}).collect(Collectors.toList()), HttpStatus.OK);
	}
}
