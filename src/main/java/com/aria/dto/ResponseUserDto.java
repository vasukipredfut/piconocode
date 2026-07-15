package com.aria.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.aria.model.BloodGroup;
import com.aria.model.Branch;
import com.aria.model.DaCategory;
import com.aria.model.Organisation;
import com.aria.model.Role;
import com.aria.model.Roles;
import com.aria.model.User;
import com.aria.model.UserDependency;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResponseUserDto {

	public Map<String, Object> userDetails;
	public Map<String, String> bankDetails;
	public Map<String, String> adharAndOtherDetails;
	public List<UserDependency> dependencies;
	public Map<String, String> salary;

	public ResponseUserDto(User user, List<UserDependency> dependency, Branch branch, Organisation organisation,
			Roles role, BloodGroup bloodGroup, DaCategory daCategory) {
		DateUtil dateUtil = new DateUtil();
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("id", user.getId().toString());
		userInfo.put("username", user.getUsername());
		userInfo.put("password", user.getPassword());
		userInfo.put("active", String.valueOf(user.getActive()));
		if (role != null) {
			userInfo.put("role", role.getName());
			userInfo.put("roleId", role.getId());
		}
		if (organisation != null) {
			userInfo.put("organisation", organisation.getName());
			userInfo.put("orgId", organisation.getId().toString());
		} else {
			userInfo.put("organisation", "");
			userInfo.put("orgId", "");
		}
		if (branch != null) {
			userInfo.put("branchName", branch.getBranchName());
			userInfo.put("branchId", branch.getId().toString());
		} else {
			userInfo.put("branchName", "");
			userInfo.put("branchId", "");
		}
		if (user.getDepartment() != null) {
			userInfo.put("deptName", user.getDepartment().getName());
			userInfo.put("deptId", user.getDepartment().getId().toString());
		} else {
			userInfo.put("deptName", "");
			userInfo.put("deptId", "");
		}
		if (Objects.nonNull(user.getCreated_date())) {
			userInfo.put("created_date", user.getCreated_date().toString());
			if (user.getUpdated_date() != null) {
				userInfo.put("updated_date", user.getUpdated_date().toString());
			} else {
				userInfo.put("updated_date", user.getCreated_date().toString());
			}
		}

		userInfo.put("batchNo", user.getBatchNo());
		userInfo.put("employeeName", user.getEmployeename());
		userInfo.put("employeeType", user.getEmployeetype());
		userInfo.put("bioId", user.getBioid());
		if (user.getDesignation() != null) {
			userInfo.put("designation", user.getDesignation().getName());
			userInfo.put("designationId", String.valueOf(user.getDesignation().getId()));
		}
		userInfo.put("gender", user.getGender());
		userInfo.put("qualification", user.getQualification());
		userInfo.put("location", user.getLocation());
		userInfo.put("address", user.getAddress());

		if (bloodGroup != null) {
			userInfo.put("bloodGroup", bloodGroup.getName());
			userInfo.put("bloodGroupId", bloodGroup.getId().toString());
		}
		if (daCategory != null) {
			userInfo.put("dacatid", daCategory.getId().toString());
			userInfo.put("actCategory", daCategory.getName());
		}

		if (Objects.nonNull(user.getPhoto())) {
			userInfo.put("photo", user.getPhoto().toString());
		}
		userInfo.put("email", user.getEmail());
		userInfo.put("phoneNo", user.getPhoneno());
		userInfo.put("emergencyContactNo", user.getEmergencycontactno());
		userInfo.put("emergentContactPerson", user.getEmergentcontactperson());
		userInfo.put("emergencyContactPersonRelationship", user.getEmergencyContactPersonRelationship());
		userInfo.put("maritalStatus", user.getMaritalstatus());

		if (user.getDateofbirth() != null) {
			userInfo.put("dateOfBirth", dateUtil.getDateTOStringWk(user.getDateofbirth()));
		}
		userInfo.put("cstatus", user.getCstatus());
		userInfo.put("status", user.getStatus());
		// userInfo.put("relationShip", "");
		this.userDetails = userInfo;

		Map<String, String> bankInfo = new HashMap<>();
		bankInfo.put("bankName", user.getBankname());
		bankInfo.put("accountNo", user.getAccountno());
		bankInfo.put("ifsccode", user.getIfsccode());
		bankInfo.put("panNo", user.getPanNo());
		this.bankDetails = bankInfo;

		Map<String, String> aadharInfo = new HashMap<>();
		aadharInfo.put("aadharNo", user.getAadharno());
		aadharInfo.put("uanNo", user.getUanno());
		aadharInfo.put("esiNo", user.getEsino());
		aadharInfo.put("epfNo", user.getEpfno());
		this.adharAndOtherDetails = aadharInfo;

		Map<String, String> salaryInfo = new HashMap<>();
		salaryInfo.put("basicSalary", user.getBasicsalary());
		salaryInfo.put("allowance", user.getAllowance());
		salaryInfo.put("deamessAllowance", user.getDeamessAllowance());
		salaryInfo.put("houseRentAllowance", user.getHouseRentAllowance());
		salaryInfo.put("goodWorkAllowance", user.getGoodWorkAllowance());
		this.salary = salaryInfo;

		this.dependencies = dependency;

	}

	public ResponseUserDto(User user) {
		DateUtil dateUtil = new DateUtil();
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("id", user.getId().toString());
		userInfo.put("username", user.getUsername());
		userInfo.put("password", user.getPassword());
		userInfo.put("active", String.valueOf(user.getActive()));
		userInfo.put("role", user.getRoles_id().toString());
		userInfo.put("orgId", user.getOrg_id().toString());
		userInfo.put("branchId", user.getBranch_id().toString());

		if (user.getDepartment() != null) {
			userInfo.put("deptName", user.getDepartment().getName());
			userInfo.put("deptId", user.getDepartment().getId().toString());
		} else {
			userInfo.put("deptName", "");
			userInfo.put("deptId", "");
		}
		if (Objects.nonNull(user.getCreated_date())) {
			userInfo.put("created_date", user.getCreated_date().toString());
			if (user.getUpdated_date() != null) {
				userInfo.put("updated_date", user.getUpdated_date().toString());
			} else {
				userInfo.put("updated_date", user.getCreated_date().toString());
			}
		}

		userInfo.put("batchNo", user.getBatchNo());
		userInfo.put("employeeName", user.getEmployeename());
		userInfo.put("employeeType", user.getEmployeetype());
		userInfo.put("bioId", user.getBioid());
		if (user.getDesignation() != null) {
			userInfo.put("designation", user.getDesignation().getName());
		}
		userInfo.put("gender", user.getGender());
		userInfo.put("qualification", user.getQualification());
		userInfo.put("location", user.getLocation());
		userInfo.put("address", user.getAddress());
		userInfo.put("bloodGroup", user.getBloodgroupid().toString());

		if (Objects.nonNull(user.getPhoto())) {
			userInfo.put("photo", user.getPhoto().toString());

		}
		userInfo.put("email", user.getEmail());
		userInfo.put("phoneNo", user.getPhoneno());
		userInfo.put("emergencyContactNo", user.getEmergencycontactno());
		userInfo.put("emergentContactPerson", user.getEmergentcontactperson());
		userInfo.put("emergencyContactPersonRelationship", user.getEmergencyContactPersonRelationship());
		userInfo.put("maritalStatus", user.getMaritalstatus());
		// userInfo.put("actCategory", user.getActCategory());
		if (user.getDateofbirth() != null) {
			userInfo.put("dateOfBirth", dateUtil.getDateTOStringWk(user.getDateofbirth()));
		}
		userInfo.put("cstatus", user.getCstatus());
		userInfo.put("status", user.getStatus());
		// userInfo.put("relationShip", "");
		this.userDetails = userInfo;

		Map<String, String> bankInfo = new HashMap<>();
		bankInfo.put("bankName", user.getBankname());
		bankInfo.put("accountNo", user.getAccountno());
		bankInfo.put("ifsccode", user.getIfsccode());
		bankInfo.put("panNo", user.getPanNo());
		this.bankDetails = bankInfo;

		Map<String, String> aadharInfo = new HashMap<>();
		aadharInfo.put("aadharNo", user.getAadharno());
		aadharInfo.put("uanNo", user.getUanno());
		aadharInfo.put("esiNo", user.getEsino());
		aadharInfo.put("epfNo", user.getEpfno());
		this.adharAndOtherDetails = aadharInfo;

		Map<String, String> salaryInfo = new HashMap<>();
		salaryInfo.put("basicSalary", user.getBasicsalary());
		salaryInfo.put("allowance", user.getAllowance());
		salaryInfo.put("deamessAllowance", user.getDeamessAllowance());
		salaryInfo.put("houseRentAllowance", user.getHouseRentAllowance());
		salaryInfo.put("goodWorkAllowance", user.getGoodWorkAllowance());
		this.salary = salaryInfo;

	}

	/*
	 * public ResponseUserDto(EmastDaily user, List<DailyUserDependency> dependency,
	 * Branch branch, Organisation organisation, BloodGroup bloodGroup) { DateUtil
	 * dateUtil = new DateUtil(); Map<String, String> userInfo = new HashMap<>();
	 * userInfo.put("id", user.getId().toString());
	 * 
	 * if (organisation != null) { userInfo.put("organisation",
	 * organisation.getName()); userInfo.put("orgId",
	 * organisation.getId().toString()); } else { userInfo.put("organisation", "");
	 * userInfo.put("orgId", ""); } if (branch != null) { userInfo.put("branchName",
	 * branch.getBranchName()); userInfo.put("branchId", branch.getId().toString());
	 * } else { userInfo.put("branchName", ""); userInfo.put("branchId", ""); } if
	 * (user.getDepartment() != null) { userInfo.put("deptName",
	 * user.getDepartment().getName()); userInfo.put("deptId",
	 * user.getDepartment().getId().toString()); } else { userInfo.put("deptName",
	 * ""); userInfo.put("deptId", ""); }
	 *//*
		 * if (Objects.nonNull(user.getCreated_date())) { userInfo.put("created_date",
		 * user.getCreated_date().toString()); if (user.getUpdated_date() != null) {
		 * userInfo.put("updated_date", user.getUpdated_date().toString()); } else {
		 * userInfo.put("updated_date", user.getCreated_date().toString()); } }
		 *//*
			 * 
			 * //userInfo.put("batchNo", user.getBatchNo()); userInfo.put("employeeName",
			 * user.getName()); userInfo.put("employeeType", user.getCategory());
			 * 
			 * userInfo.put("gender", user.getGender()); userInfo.put("qualification",
			 * user.getQuali().toString()); userInfo.put("location",
			 * user.getLocation().toString()); userInfo.put("address", user.getAddress());
			 * 
			 * if (bloodGroup != null) { userInfo.put("bloodGroup", bloodGroup.getName());
			 * userInfo.put("bloodGroupId", bloodGroup.getId().toString()); }
			 * 
			 * if (Objects.nonNull(user.getPhoto())) { userInfo.put("photo",
			 * user.getPhoto().toString()); } //userInfo.put("email", user.get());
			 * userInfo.put("phoneNo", user.getPhoneno());
			 * userInfo.put("emergencyContactNo", user.getEmergencycontactno());
			 * userInfo.put("emergentContactPerson", user.getEmergentcontactperson());
			 * userInfo.put("maritalStatus", user.getMaritalstatus());
			 * userInfo.put("actCategory", user.getActCategory()); if (user.getDateofbirth()
			 * != null) { userInfo.put("dateOfBirth",
			 * dateUtil.getDateTOStringWk(user.getDateofbirth())); } userInfo.put("cstatus",
			 * user.getCstatus()); userInfo.put("status", user.getStatus()); //
			 * userInfo.put("relationShip", ""); this.userDetails = userInfo;
			 * 
			 * Map<String, String> bankInfo = new HashMap<>(); bankInfo.put("bankName",
			 * user.getBankname()); bankInfo.put("accountNo", user.getAccountno());
			 * bankInfo.put("ifsccode", user.getIfsccode()); bankInfo.put("panNo",
			 * user.getPanNo()); this.bankDetails = bankInfo;
			 * 
			 * Map<String, String> aadharInfo = new HashMap<>(); aadharInfo.put("aadharNo",
			 * user.getAadharno()); aadharInfo.put("uanNo", user.getUanno());
			 * aadharInfo.put("esiNo", user.getEsino()); aadharInfo.put("epfNo",
			 * user.getEpfno()); this.adharAndOtherDetails = aadharInfo;
			 * 
			 * Map<String, String> salaryInfo = new HashMap<>();
			 * salaryInfo.put("basicSalary", user.getBasicsalary());
			 * salaryInfo.put("allowance", user.getAllowance());
			 * salaryInfo.put("deamessAllowance", user.getDeamessAllowance());
			 * salaryInfo.put("houseRentAllowance", user.getHouseRentAllowance());
			 * salaryInfo.put("goodWorkAllowance", user.getGoodWorkAllowance()); this.salary
			 * = salaryInfo;
			 * 
			 * this.dependencies = dependency;
			 * 
			 * }
			 */
}
