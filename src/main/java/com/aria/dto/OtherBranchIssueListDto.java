package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OtherBranchIssueListDto {

    private Long id;
    private Long materialWithInBranchId;
    private String trnNo;

    //private String dktNo;
    private String requestFromBranch;
    private String requestFromDepartment;
    private String status;
    private String issueFromBranch;
    private String issueFromDepartment;
    private Long materialIssueId;

    public OtherBranchIssueListDto(OtherBranchIssueList materialRequest) {
        DateUtil dateUtil = new DateUtil();
        this.id = materialRequest.getId();
        //this.dktNo = "DKT-"+materialRequest.getId();
        this.materialWithInBranchId = materialRequest.getMaterialWithInBranchId();
        this.trnNo = materialRequest.getTrnNo();
        this.requestFromBranch = materialRequest.getRequestFromBranch();
        this.requestFromDepartment = materialRequest.getRequestFromDepartment();
        this.status = materialRequest.getStatus();
        this.issueFromBranch = materialRequest.getIssueFromBranch();
        this.issueFromDepartment = materialRequest.getIssueFromDepartment();
        this.materialIssueId = materialRequest.getMaterialIssueId();
    }
}
