package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EvaluationBillListDTO {

    public Long branchId;
    public Long firmId;
    public Long vendorId;
    public String vendorName;
    public String billRefId;
    public String billNo;
    public String poNumber;
    public String timePeriod;
    public Integer year;

    public String inwardDate;
    public String deliveryDueDate;
    public String qcScore;
    public String deliveryScore;

    public EvaluationBillListDTO(EvaluationBillList evaluationBillList ) {
        DateUtil dateUtil = new DateUtil();
        this.branchId = evaluationBillList.getBranchId();
        this.firmId = evaluationBillList.getFirmId();
        this.vendorId = evaluationBillList.getVendorId();
        this.vendorName = evaluationBillList.getVendorName();
        this.billRefId = evaluationBillList.getBillRefId();
        this.poNumber = evaluationBillList.getPoNumber();
        this.billNo = evaluationBillList.getBillNo();

        this.timePeriod = evaluationBillList.getTimePeriod();
        this.year = evaluationBillList.getYear();

        if(evaluationBillList.getInwardDate() != null)
        this.inwardDate = dateUtil.getDateTOStringWk(evaluationBillList.getInwardDate());

        if(evaluationBillList.getDeliveryDueDate() != null)
        this.deliveryDueDate = dateUtil.getDateTOStringWk(evaluationBillList.getDeliveryDueDate());

        this.qcScore = evaluationBillList.getQcScore();

        this.deliveryScore = evaluationBillList.getDeliveryScore();


    }
}
