package com.aria.dto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aria.dao.*;
import com.aria.model.PurchaseReturn;
import com.aria.model.User;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseReturnReceiptDto {

    private Long id;

    private PartyDetails vendorDetails;


    private Company firmDetails;

    private String billTransactionNo;

    private String billDate;

    private String billInwardDate;

    private String billNo;

    private BranchAddress fromBranch;

    private List<PurchaseReturnProductsReceiptDto> purchaseReturnProducts;

    private Long billRefId;

    private String status;

    private String lorrySerialNo;


    private Long createdBy;

    private String createdByName;
    private String createdDate;

    private String createdDateTime;

    private Long updatedBy;

    private String updatedByName;

    private Long verifiedBy;
    private String verifiedByName;
    private String verifiedDate;
    private String verifiedDateTime;
    private Long rejectedBy;
    private String rejectedByName;
    private String rejectedDate;
    private String rejectedDateTime;

    private String vehicleMode;
    private String vehicleNo;
    private String vehicleType;


    private String gdcNo;
    private String gdcDate;
    private String gdcDateTime;


    public PurchaseReturnReceiptDto(PurchaseReturn purchaseReturn, UserRepository userRepository,
                                    BinStockRepository binStockRepository,  BillInwardItemReceivedRepository inwardItemReceivedRepository) {
        DateUtil dateUtil = new DateUtil();
        System.out.println("BillNo" + purchaseReturn.getBillNo());
        this.id = purchaseReturn.getId();

        this.billNo = purchaseReturn.getBillNo();
        this.billTransactionNo = purchaseReturn.getBillTransactionNo();
        this.status = purchaseReturn.getStatus();
        this.lorrySerialNo = purchaseReturn.getLorrySerialNo();
        this.purchaseReturnProducts = purchaseReturn.getPurchaseReturnProducts() != null ? purchaseReturn
                .getPurchaseReturnProducts().stream()
                .map(obj -> new PurchaseReturnProductsReceiptDto(obj, binStockRepository, inwardItemReceivedRepository))
                .collect(Collectors.toList()) : null;
        if (purchaseReturn.getCreated_date() != null) {
            this.createdDate = dateUtil.getLocalDateTimeToDateString(purchaseReturn.getCreated_date());
            this.createdDateTime = dateUtil.getLocalDateTimeToDateTimeString(purchaseReturn.getCreated_date());
        }

        if (purchaseReturn.getRejected_date() != null) {
            this.rejectedDate = dateUtil.getLocalDateTimeToDateString(purchaseReturn.getRejected_date());
            this.rejectedDateTime = dateUtil.getLocalDateTimeToDateTimeString(purchaseReturn.getRejected_date());
        }

        if (purchaseReturn.getVerified_date() != null) {
            this.verifiedDate = dateUtil.getLocalDateTimeToDateString(purchaseReturn.getVerified_date());
            this.verifiedDateTime = dateUtil.getLocalDateTimeToDateTimeString(purchaseReturn.getVerified_date());
        }
        this.vehicleMode = purchaseReturn.getVehicleMode();
        this.vehicleNo = purchaseReturn.getVehicleNo();
        this.vehicleType = purchaseReturn.getVehicleType();

        Optional<User> user = userRepository.findById(purchaseReturn.getCreatedBy());
        if (user.isPresent()) {
            this.createdByName = user.get().getEmployeename();
        }
        if (purchaseReturn.getUpdatedBy() != null) {
            Optional<User> user1 = userRepository.findById(purchaseReturn.getUpdatedBy());
            if (user1.isPresent()) {
                this.updatedByName = user1.get().getEmployeename();
            }
        }
        if (purchaseReturn.getRejectedBy() != null) {
            Optional<User> userOptional = userRepository.findById(purchaseReturn.getRejectedBy());
            if (userOptional.isPresent()) {
                this.rejectedByName = userOptional.get().getEmployeename();
            }
        }
        if (purchaseReturn.getVerifiedBy() != null) {
            Optional<User> userOptional = userRepository.findById(purchaseReturn.getVerifiedBy());
            if (userOptional.isPresent()) {
                this.verifiedByName = userOptional.get().getEmployeename();
            }
        }
        this.gdcNo = purchaseReturn.getGDCNo();
        if (purchaseReturn.getGdc_date() != null) {
            this.gdcDate = dateUtil.getLocalDateTimeToDateString(purchaseReturn.getGdc_date());
            this.gdcDateTime = dateUtil.getLocalDateTimeToDateTimeString(purchaseReturn.getGdc_date());
        }
    }
}
