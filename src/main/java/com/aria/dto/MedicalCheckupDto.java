package com.aria.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.model.MedicalCheckup;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class MedicalCheckupDto {

	private Long id;

	private Long orgId;

	private String orgName;

	private Long deptId;

	private String deptName;

	private Long branchId;

	private String branchName;

	private String staffCategory;

	private Long staffId;

	private String staffName;

	private String checkupDate;

	private String checkupType;

	private String status;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	private String proofReport;

	private String typeDate;

	private String vaccinationName;

	private Map<String, Object> medicalCheckUpValue;

	public MedicalCheckupDto(MedicalCheckup medicalCheckup) {
		DateUtil dateUtil = new DateUtil();
		this.id = medicalCheckup.getId();
		if (medicalCheckup.getOrganisation() != null) {
			this.orgId = medicalCheckup.getOrganisation().getId();
			this.orgName = medicalCheckup.getOrganisation().getName();
		}
		if (medicalCheckup.getDepartmentDetails() != null) {
			this.deptId = medicalCheckup.getDepartmentDetails().getId();
			this.deptName = medicalCheckup.getDepartmentDetails().getName();
		}
		if (medicalCheckup.getBranch() != null) {
			this.branchId = medicalCheckup.getBranch().getId();
			this.branchName = medicalCheckup.getBranch().getBranchName();
		}
		if (medicalCheckup.getStaff() != null) {
			this.staffId = medicalCheckup.getStaff().getId();
			this.staffName = medicalCheckup.getStaff().getEmployeename();
		}
		this.staffCategory = medicalCheckup.getStaffCategory();
		this.status = medicalCheckup.getStatus();
		this.checkupType = medicalCheckup.getCheckupType();
		this.createdBy = medicalCheckup.getCreatedBy();
		this.updatedBy = medicalCheckup.getUpdatedBy();
		this.created_date = medicalCheckup.getCreated_date();
		this.updated_date = medicalCheckup.getUpdated_date();
		this.proofReport = medicalCheckup.getProofReport();
		this.vaccinationName = medicalCheckup.getVaccinationName();
		if (medicalCheckup.getTypeDate() != null) {
			this.typeDate = dateUtil.getDateTOStringWk(medicalCheckup.getTypeDate());
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			this.medicalCheckUpValue = mapper.readValue(medicalCheckup.getMedicalCheckUpValue(), HashMap.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
