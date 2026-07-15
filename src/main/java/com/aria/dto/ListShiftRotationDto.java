package com.aria.dto;



import com.aria.model.ListShiftRotation;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class ListShiftRotationDto {
    private String employeeName;

    private Long empId;

    private String departmentName;

    private String toDate;
    private String fromDate;

    private Long rotationId;

    private String empType;

    private String createdDate;
    private String category;


    public ListShiftRotationDto(ListShiftRotation rotation) {
        this.employeeName = rotation.getEmployeeName();
        this.empId = rotation.getEmpId();
        this.departmentName = rotation.getDepartmentName();

		if(rotation.getEmpType() == 0) {
			this.empType = "staff";
		}else {
			this.empType = "wages";
		}

        this.rotationId = rotation.getRotationId();

        this.category = rotation.getCategory();

        DateUtil dateUtil = new DateUtil();
        if (rotation.getToDate() != null) {
            toDate = dateUtil.getDateToStringDDMMYYYY(rotation.getToDate());
        }
        if (rotation.getFromDate() != null) {
            fromDate = dateUtil.getDateToStringDDMMYYYY(rotation.getFromDate());
        }
        if (rotation.getCreatedDate() != null) {
            createdDate = dateUtil.getLocalDateTimeToStringDDMMYYYY1(rotation.getCreatedDate());
        }

    }
}