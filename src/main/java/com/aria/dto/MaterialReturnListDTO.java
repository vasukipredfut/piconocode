package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MaterialReturnListDTO {

    private Long id;

    private String itemName;

    private String issueDate;
    private String dispatchDate;
    private String requestDate;
    private Long requestDeptId;
    private String requestDeptName;
    private String requestFrom;
    private Long requestFromId;
    private Long returnId;
    private String requestFromName;

    private String quantity;
    private String toReach;
    private Long toReachId;
    private String toReachName;


    public MaterialReturnListDTO(MaterialReturnList materialReturnList) {
        DateUtil dateUtil = new DateUtil();
        this.id = materialReturnList.getId();
        this.itemName = materialReturnList.getItemName();
        this.issueDate = dateUtil.getDateTOStringWk(materialReturnList.getIssueDate());
        this.dispatchDate = dateUtil.getDateTOStringWk(materialReturnList.getDispatchDate());
        this.requestDate = dateUtil.getDateTOStringWk(materialReturnList.getRequestDate());
        this.requestDeptId = materialReturnList.getRequestDeptId();
        this.requestDeptName = materialReturnList.getRequestDeptName();
        this.requestFrom = materialReturnList.getRequestFrom();
        this.requestFromName = materialReturnList.getRequestBranchName();
        this.requestFromId = materialReturnList.getRequestFromId();
        this.quantity = materialReturnList.getQuantity();
        this.toReach = materialReturnList.getToReach();
        this.toReachId = materialReturnList.getToReachId();
        this.toReachName = materialReturnList.getToReachName();
        this.returnId = materialReturnList.getReturnId();
    }
}
