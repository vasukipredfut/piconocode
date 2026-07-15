package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class PrintSupplierEvaluationReportResult {


    Company companyDetails;

    PartyDetails vendorDetails;
    SupplierEvalValidUsersDTO validateUserDetails;

    List<SupplierEvaluationAttributesDTO> commentAndDescription;

    List<EvaluationReportListDto> branchWiseScoreResult;

    Map<String, List<EvaluationBillListDTO>> poAndBillWiseScoreResult;

}
