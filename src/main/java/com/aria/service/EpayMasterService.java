package com.aria.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.xmlbeans.impl.xb.xsdschema.Attribute.Use;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aria.dao.EpayMasterRepository;
import com.aria.dao.UserRepository;
import com.aria.dto.EpayMasterDto;
import com.aria.dto.MyCellDto;
import com.aria.model.DepayMaster;
import com.aria.model.EpayMaster;
import com.aria.model.User;
import com.aria.utils.ExcelUtil;

@Service
public class EpayMasterService {
	@Autowired
	private EpayMasterRepository epayMasterRepository;

	@Autowired
	private ExcelUtil excelUtil;

	@Autowired
	private UserRepository userRepository;

	public ResponseEntity<?> createStaff(EpayMasterDto epayMasterDto) {
		Optional<User> employeeDetail = userRepository.findById(epayMasterDto.getEmpcode());

		if (employeeDetail.get() != null) {
			EpayMaster employee = setEpayObject(epayMasterDto, employeeDetail.get());
			if (epayMasterRepository.save(employee) != null)
				return new ResponseEntity<Object>("Employee saved successfully...", HttpStatus.OK);
		}
		return new ResponseEntity<Object>("Something Went Wrong...", HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public static EpayMaster setEpayObject(EpayMasterDto epayMasterDto, User employeeDetail) {
		EpayMaster employee = new EpayMaster();
		employee.setEmpcode(epayMasterDto.getEmpcode());
		employee.setTdeduction(epayMasterDto.getTdeduction());
		employee.setNetsalary(epayMasterDto.getNetsalary());
		employee.setBlsalary(epayMasterDto.getBlsalary());
		employee.setFlsalary(epayMasterDto.getFlsalary());
		employee.setAlsalary(epayMasterDto.getAlsalary());
		employee.setLsalary(epayMasterDto.getLsalary());
		employee.setPerdayamount(epayMasterDto.getPerdayamount());
		employee.setLstatus("0");
		employee.setBankcode(employeeDetail.getBankname());
		if (employeeDetail.getAccountno() != null && employeeDetail.getAccountno() != "")
			employee.setBankacno(Long.parseLong(employeeDetail.getAccountno()));
		employee.setBank_ifsc(employeeDetail.getIfsccode());
		employee.setBid(employeeDetail.getBranch_id());
		employee.setDid(employeeDetail.getDepartment().getId());
		employee.setDesigid(employeeDetail.getDesignation().getId());
		employee.setJdate(employeeDetail.getJoiningDate());
		employee.setSex(employeeDetail.getGender());
		if (employeeDetail.getEsino() != null && employeeDetail.getEsino() != "")
			employee.setEsino(Long.parseLong(employeeDetail.getEsino()));
		employee.setEpfno(employeeDetail.getEpfno());
		employee.setName(employeeDetail.getEmployeename());
		return employee;
	}

	public ResponseEntity<?> uploadBulkEmlpoyeeForStaffBasedOnEmail(MultipartFile file) throws IOException {
		if (!ExcelUtil.checkExcelFormat(file))
			return null;

		Map<Integer, List<MyCellDto>> data = ExcelUtil.readExcel(file.getInputStream(), file.getOriginalFilename());
		List<EpayMaster> allEmployeeList = null;
		allEmployeeList = excelUtil.convertExcelToListOfUpdateStaff(data);
		for (int i = 0; i < allEmployeeList.size(); i++) {
			EpayMaster employee = epayMasterRepository.findByEmpcode(allEmployeeList.get(i).getEmpcode());
			if (employee != null) {
				if (allEmployeeList.get(i).getName() != "")
					employee.setName(allEmployeeList.get(i).getName());
				if (allEmployeeList.get(i).getTdeduction() != null)
					employee.setTdeduction(allEmployeeList.get(i).getTdeduction());
				if (allEmployeeList.get(i).getNetsalary() != null)
					employee.setNetsalary(allEmployeeList.get(i).getNetsalary());
				if (allEmployeeList.get(i).getBlsalary() != null)
					employee.setBlsalary(allEmployeeList.get(i).getBlsalary());
				if (allEmployeeList.get(i).getFlsalary() != null)
					employee.setFlsalary((int) Math.round(allEmployeeList.get(i).getBlsalary()));
				if (allEmployeeList.get(i).getLsalary() != null)
					employee.setLsalary(allEmployeeList.get(i).getLsalary());
				if (allEmployeeList.get(i).getAlsalary() != null)
					employee.setAlsalary(allEmployeeList.get(i).getAlsalary());
				if (allEmployeeList.get(i).getPerdayamount() != null)
					employee.setPerdayamount(allEmployeeList.get(i).getPerdayamount());
				epayMasterRepository.save(employee);
			}
		}

		return new ResponseEntity<Object>("Employee Updated successfully...", HttpStatus.OK);
	}

	public List<EpayMaster> allEpayMasterEmployee() {
		List<EpayMaster> employees = epayMasterRepository.findAll();
		return employees;
	}
	
	public EpayMaster singleEpayMasterEmployee(Long employeeId){
		Optional<EpayMaster> employee=epayMasterRepository.findById(employeeId);
		if(employee.isPresent())

			return employee.get();
		return null;
	}

	public Boolean deleteEpaymaster(Long employeeId) {
		Optional<EpayMaster> epayMaster=epayMasterRepository.findById(employeeId);
		if(epayMaster.isPresent()) {
			EpayMaster e=epayMaster.get();

			e.setLstatus("1");
			if (epayMasterRepository.save(e) != null)
				return true;
		}
		return false;
	}
}
