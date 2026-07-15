package com.aria.dto;

public class dwagesDTO {

	private Long empId;
	private String empName;

	public Long getEmpId() {
		return empId;
	}

	public String getEmpName() {
		return empName;
	}

	public dwagesDTO(dwagesList dwages) {
		this.empId = dwages.getDWagesEmpId();
		this.empName = dwages.getDWagesEmpName();
	}

}
