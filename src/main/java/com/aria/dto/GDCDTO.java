package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GDCDTO {

    public String gdcNo;
    public Long issueBranchId;
    public String issueBranchName;
    public Long issueDeptId;
    public String issueDeptName;
    public Long reqDeptId;
    public String reqDeptName;
    public Long requestBranchId;
    public String requestBranchName;
    public String gdcDate;

    public GDCDTO(GDCList gdcList) {
        DateUtil dateUtil = new DateUtil();
        this.gdcNo = gdcList.getGdcNo();
        this.issueBranchId = gdcList.getIssueBranchId();
        this.issueBranchName = gdcList.getIssueBranchName();
        this.issueDeptId = gdcList.getIssueDeptId();
        this.issueDeptName = gdcList.getIssueDeptName();
        this.reqDeptId = gdcList.getReqDeptId();
        this.reqDeptName = gdcList.getReqDeptName();
        this.requestBranchId = gdcList.getRequestBranchId();
        this.requestBranchName = gdcList.getRequestBranchName();
        this.gdcDate = dateUtil.getDateTOStringWk(gdcList.getGdcDate());
    }
}
