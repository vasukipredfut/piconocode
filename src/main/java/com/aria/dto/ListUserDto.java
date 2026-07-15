package com.aria.dto;



import com.aria.model.ListUser;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class ListUserDto {

    private String employeeName;

    private Long id;

    private Long designationId;

    private String designationName;

    private String status;

    private String joiningDate;

    private Long branchId;

    private Long departmentId;

    private String branchName;

    private String departmentName;

    private Long weekDayOff;

    public ListUserDto(ListUser user) {

        this.employeeName = user.getEmployeeName();
        this.id = user.getId();
        this.designationId = user.getDesignationId();
        this.designationName = user.getDesignationName();
        this.status = user.getStatus();
        this.branchId = user.getBranchId();
        this.departmentId = user.getDepartmentId();
        this.branchName = user.getBranchName();
        this.departmentName = user.getDepartmentName();
        this.weekDayOff = user.getWeekOffDay();

        DateUtil dateUtil = new DateUtil();
        if (user.getJoiningDate() != null) {
            joiningDate = dateUtil.getDateTOStringWk(user.getJoiningDate());
        }

    }
}