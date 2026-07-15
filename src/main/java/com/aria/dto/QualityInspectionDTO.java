package com.aria.dto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.model.QualityInspectionScore;
import com.aria.model.SupplierEvaluationAttributes;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;

@Getter
@Setter
@NoArgsConstructor
public class QualityInspectionDTO {


    private Long purchaseQualityInspectionScoreId;

    private String trnNumber;

    private Long itemId;

    private Long itemType;

    private String itemName;

    private Long poId;
    private String poNumber;

    private Long billRefId;

    private String billNumber;

    private String billDate;

    private String dueDate;

    private Long vendorRefId;

    private Integer billAmount;

    private Long inwardBy;

    private String inwardDate;

    private String deliveryDelay;

    private Double poQty;

    private Double suppliedQty;

    private Double pendingQty;
    private Double qcScore;
    private Double numberOffItems;

    private Double deliveryScore;

    private Double loyaltyScore;

    private Double totalQcScore;

    private Double totalScore;

    private String emailId;

    private String emailStatusFlag;

    private String mobileNumber;

    private Integer activeFlag;

    private Long createdById;

    private Integer department;

    private Long company;

    private Long branch;

    private String date;

    private Long approveById;

    private String boxCompression;

    private String path;
    private  Map<String, String>  boxCompressionPathValue;



    public QualityInspectionDTO(QualityInspectionScore model) {
        DateUtil dateUtil = new DateUtil();
        this.purchaseQualityInspectionScoreId = model.getPurchaseQualityInspectionScoreId();
        this.trnNumber = model.getTrnNumber();
        this.itemId = model.getItemId();
        this.itemName = model.getItemName();
        this.itemType = model.getItemType();
        this.poId = model.getPoId();
        this.poNumber = model.getPoNumber();
        this.billRefId = model.getBillRefId();
        this.billNumber = model.getBillNumber();
        this.deliveryDelay = model.getDeliveryDelay();
        if(model.getBillDate() != null) {
            this.billDate = dateUtil.getDateTOStringWk(model.getBillDate());
        }
        if(model.getDueDate() != null) {
            this.dueDate = dateUtil.getDateTOStringWk(model.getDueDate());
        }
        this.vendorRefId = model.getVendorRefId();
        this.billAmount = model.getBillAmount();
        this.inwardBy = model.getInwardBy();
        if(model.getInwardDate() != null) {
            this.inwardDate = dateUtil.getDateTOStringWk(model.getInwardDate());
        }
        this.poQty = model.getPoQty();
        this.suppliedQty = model.getSuppliedQty();
        this.pendingQty = model.getPendingQty();
        this.qcScore = model.getQcScore();
        this.numberOffItems = model.getNumberOffItems();
        this.deliveryScore = model.getDeliveryScore();
        this.loyaltyScore = model.getLoyaltyScore();
        this.totalQcScore = model.getQcScore();
        this.totalScore = model.getTotalScore();
        this.approveById = model.getApproveById();
        this.emailId = model.getEmailId();
        this.emailStatusFlag = model.getEmailStatusFlag();
        this.mobileNumber = model.getMobileNumber();
        this.activeFlag = model.getActiveFlag();
        this.createdById = model.getCreatedById();
        this.department = model.getDepartment();
        this.company = model.getCompany();
        this.branch = model.getBranch();
        if( model.getDate() != null) {
            this.date = dateUtil.getDateTOStringWk(model.getDate());
        }
        this.boxCompression = model.getBoxCompression();
        ObjectMapper mapper = new ObjectMapper();
        if (model.getBox_compression_path_value() != null) {

            try {
                this.boxCompressionPathValue = mapper.readValue(model.getBox_compression_path_value(), HashMap.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
