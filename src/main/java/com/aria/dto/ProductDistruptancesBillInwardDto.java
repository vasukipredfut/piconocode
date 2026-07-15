package com.aria.dto;

import com.aria.model.BillInward;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProductDistruptancesBillInwardDto {


    private Long hoBillId;
    private Long id;

    private Long inwardBranchId;

    private String inwardBranchName;

    private Long deliverBranchId;

    private String deliverBranchName;

    private String receivedDate;

    private String billDate;

    private String roundOfType;
    private String roundOfValue;

    private Long vendorId;

    private String vendorName;

    private String billNo;

    private String tcs;

    private Long firmOrgId;

    private String firmOrgName;

    private String status;

    private String ewayBill;

    private String cessTax;

    private String billCopy;

    private String productCategory;

    private String lorryDate;

    private String lorryWeight;

    private Double insurance;

    private Double billFreightAmount;

    private Double billAmount;

    private String billAmountStringValue;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;


    private List<BillInwardItemReceivedDto> itemReceives;


    private List<DpEntryStoredProductDto> storedProductDtos;


    private String lorryWithWeight;

    private String lorrySerialNo;
    private String lorryno;

    private String lorryWithoutWeight;

    private Boolean lorry_freight;


    public ProductDistruptancesBillInwardDto(BillInward billInward) {
        DateUtil dateUtil = new DateUtil();
        this.id = billInward.getId();
        this.hoBillId = billInward.getHoBillId();
        if (billInward.getInwardBranch() != null) {
            this.inwardBranchId = billInward.getInwardBranch().getId();
            this.inwardBranchName = billInward.getInwardBranch().getBranchName();
        }
        if (billInward.getDeliverBranch() != null) {
            this.deliverBranchId = billInward.getDeliverBranch().getId();
            this.deliverBranchName = billInward.getDeliverBranch().getBranchName();
        }
        if (billInward.getVendor() != null) {
            this.vendorId = billInward.getVendor().getId();
            this.vendorName = billInward.getVendor().getName();
        }
        if (billInward.getFirmOrg() != null) {
            this.firmOrgId = billInward.getFirmOrg().getId();
            this.firmOrgName = billInward.getFirmOrg().getName();
        }
        this.billNo = billInward.getBillNo();
        this.tcs = billInward.getTcs();
        this.status = billInward.getStatus();
        this.ewayBill = billInward.getEwayBill();
        this.cessTax = billInward.getCessTax();
        this.productCategory = billInward.getProductCategory();
        this.billCopy = billInward.getBillCopy();
        this.createdBy = billInward.getCreatedBy();
        this.updatedBy = billInward.getUpdatedBy();
        this.created_date = billInward.getCreated_date();
        this.updated_date = billInward.getUpdated_date();

        this.billAmount = (Double) billInward.getBillAmount();
        this.billFreightAmount = billInward.getBillFreightAmount();
        this.insurance = billInward.getInsurance();
        this.lorryno = billInward.getLorryno();
        this.lorryWithWeight = billInward.getLorry_with_Weight();
        this.lorryWithoutWeight = billInward.getLorry_without_Weight();
        this.lorry_freight = billInward.getLorry_freight();
        this.roundOfType = billInward.getRound_of_type();
        this.roundOfValue = billInward.getRound_of_value();
        this.lorrySerialNo = billInward.getLorrySerialNo();

        if (billInward.getReceivedDate() != null)
            this.receivedDate = dateUtil.getDateTOStringWk(billInward.getReceivedDate());

        if (billInward.getBillDate() != null)
            this.billDate = dateUtil.getDateTOStringWk(billInward.getBillDate());

        if (billInward.getLorryDate() != null)
            this.lorryDate = dateUtil.getDateTOStringWk(billInward.getLorryDate());

       /* this.itemReceives = billInward.getItemReceived() != null
                ? billInward.getItemReceived().stream().map(field -> new BillInwardItemReceivedDto(field)).collect(Collectors.toList()) : null;
*/
    }


    public ProductDistruptancesBillInwardDto(ProductDistruptancesBillInwardList billInward) {
        DateUtil dateUtil = new DateUtil();
        this.id = billInward.getId();
        this.billNo = billInward.getBillNo();
        this.lorrySerialNo = billInward.getLorrySerialNo();
        this.receivedDate = dateUtil.getDateTOStringWk(billInward.getReceivedDate());
        this.billDate = dateUtil.getDateTOStringWk(billInward.getBillDate());
        this.vendorId = billInward.getVendorId();
        this.vendorName = billInward.getVendorName();


    }
}
