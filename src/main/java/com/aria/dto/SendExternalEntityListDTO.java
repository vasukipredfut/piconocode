package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SendExternalEntityListDTO {

    private Long materialFlowSendExternalId;
    private String toReach;
    private long createdBy;
    private String createdByName;
    private String referenceDate;
    private String itemNames;
    private String fromBranchName;
    private Long status;


    public SendExternalEntityListDTO(SendExternalEntityList sendExternalEntityList ) {
        DateUtil dateUtil = new DateUtil();
        this.materialFlowSendExternalId = sendExternalEntityList.getMaterialFlowSendExternalId();
        this.toReach = sendExternalEntityList.getToReach();
        this.createdBy = sendExternalEntityList.getCreatedBy();
        this.createdByName = sendExternalEntityList.getCreatedByName();
        this.referenceDate = dateUtil.getDateTOStringWk(sendExternalEntityList.getReferenceDate());
        this.itemNames = sendExternalEntityList.getItemNames();
        this.fromBranchName = sendExternalEntityList.getFromBranchName();
        this.status = sendExternalEntityList.getStatus();
    }
}
