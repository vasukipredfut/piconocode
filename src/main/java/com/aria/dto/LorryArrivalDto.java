package com.aria.dto;

import com.aria.model.LorryArrival;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LorryArrivalDto {

    private Long productId;
    private String productName;
    private Long inwardId;
    private String lorryDate;
    private String lorrySerialNo;
    private String inwardDate;
    private String billDate;
    private Long vendorId;
    private String vendorName;
    private String quantity;
    private Long branchId;
    private String branchName;
    private Long employeeId;
    private String employeeName;
    private String upDatedOn;

    public LorryArrivalDto(LorryArrival lorryArrival) {

        this.productId = lorryArrival.getProductId();
        this.productName = lorryArrival.getProductName();
        this.inwardId = lorryArrival.getInwardId();
        DateUtil dateUtil = new DateUtil();
        if (lorryArrival.getLorryDate() != null) {
            this.lorryDate = dateUtil.getDateToStringDDMMYYYY(lorryArrival.getLorryDate());
        }
        this.lorrySerialNo = lorryArrival.getLorrySerialNo();
        if (lorryArrival.getInwardDate() != null) {
            this.inwardDate = dateUtil.getDateToStringDDMMYYYY(lorryArrival.getInwardDate());
        }
        if (lorryArrival.getBillDate() != null) {
            this.billDate = dateUtil.getDateToStringDDMMYYYY(lorryArrival.getBillDate());
        }

        this.vendorId = lorryArrival.getVendorId();
        this.vendorName = lorryArrival.getVendorName();
        this.quantity = lorryArrival.getQuantity();
        this.branchId = lorryArrival.getBranchId();
        this.branchName = lorryArrival.getBranchName();
        this.employeeId = lorryArrival.getEmployeeId();
        this.employeeName = lorryArrival.getEmployeeName();
        if (lorryArrival.getUpDatedOn() != null) {
            this.upDatedOn = dateUtil.getDateToStringDDMMYYYY(lorryArrival.getUpDatedOn());
        }

    }


}
