package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StringEscapeUtils;

@Getter
@Setter
@NoArgsConstructor
public class MaterialIssueListDto {

    private  Long id;
    private  Long requestId;
    private  String itemNames;
    private  String requestFromBranch;
    private  String requestFromDepartment;
    private  String status;
    private  String trnNo;
    private  String requestDate;
    private  String requestedPerson;
    private  String requestName;

    public MaterialIssueListDto(MaterialIssueList materialRequest) {
        DateUtil dateUtil = new DateUtil();
        this.id = materialRequest.getId();
        this.requestId = materialRequest.getRequestId();
        this.trnNo = materialRequest.getTrnNo();
        this.itemNames = StringEscapeUtils.unescapeHtml4(materialRequest.getItemNames());
        this.requestFromBranch = materialRequest.getRequestFromBranch();
        this.requestFromDepartment = materialRequest.getRequestFromDepartment();
        this.status = materialRequest.getStatus();
        this.requestDate = dateUtil.getDateTOStringWk(materialRequest.getRequestDate());
        this.requestedPerson = materialRequest.getRequestedPerson();
        this.requestName = materialRequest.getRequestName();
    }
}
