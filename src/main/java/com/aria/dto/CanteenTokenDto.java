package com.aria.dto;

import java.time.LocalDateTime;
import com.aria.model.CanteenToken;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CanteenTokenDto {

	private Long id;

	private Long userId;

	private String userName;

	private String date;

    private String groups;
	
    private Long deptId;
    
	private String employeeCategory;

	private String tokenFor;
	
	private Long shiftId;

	private String shift;

	private Long noOfToken;

	private Long price;

	private Long tokenNo;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public CanteenTokenDto(CanteenToken canteenToken) {
		this.id = canteenToken.getId();
		if (canteenToken.getUser() != null) {
			this.userName = canteenToken.getUser().getEmployeename();
			this.userId = canteenToken.getUser().getId();
		}
		this.groups = canteenToken.getGroups();
		if (canteenToken.getEmployeeCategory() != null) {
			this.deptId = canteenToken.getEmployeeCategory().getId();
			this.employeeCategory = canteenToken.getEmployeeCategory().getName();
		}
		this.tokenFor = canteenToken.getTokenFor();
		if (canteenToken.getShift() != null) {
			this.shiftId = canteenToken.getShift().getId();
			this.shift = canteenToken.getShift().getShiftName();
		}
		this.noOfToken = canteenToken.getNoOfToken();
		this.price = canteenToken.getPrice();
		this.tokenNo = canteenToken.getTokenNo();
		this.createdBy = canteenToken.getCreatedBy();
		this.updatedBy = canteenToken.getUpdatedBy();
		this.created_date = canteenToken.getCreated_date();
		this.updated_date = canteenToken.getUpdated_date();
	}

}