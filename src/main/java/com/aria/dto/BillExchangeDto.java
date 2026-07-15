package com.aria.dto;


import com.aria.model.BillExchange;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Getter
@Setter
@NoArgsConstructor
public class BillExchangeDto {

    private Long id;

    private String billNumber;

    private String billDate;

    private Long handOverToBranch;

    private String handOverToBranchName;

    private Long handOverToDepartment;

    private String handOverToDepartmentName;

    private String handOverDate;

    private String billInwardDate;

    private Long exchangeInitiativePerson;

    private String exchangeInitiativePersonName;

    private Long vendorId;

    private String vendorName;

    private String status;

    private String handOverType;

    private Long exchangePerson;

    private String exchangePersonName;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;
    
    private Long billInwardId;

    public List<BillExchangeProductDto> billDetails;

    private Double billAmount;

    private String billAmountStringValue;
    private Long firmOrgId;


    public BillExchangeDto(BillExchange billExchange) {
        this.id = billExchange.getId();
        this.billNumber = billExchange.getBillNumber();
        if (billExchange.getHandOverToBranch() != null) {
            this.handOverToBranch = billExchange.getHandOverToBranch().getId();
            this.handOverToBranchName = billExchange.getHandOverToBranch().getBranchName();
        }
        if (billExchange.getExchangeInitiativePerson() != null) {
            this.exchangeInitiativePerson = billExchange.getExchangeInitiativePerson().getId();
            this.exchangeInitiativePersonName = billExchange.getExchangeInitiativePerson().getEmployeename();
        }
        if (billExchange.getExchangePerson() != null) {
            this.exchangePerson = billExchange.getExchangePerson().getId();
            this.exchangePersonName = billExchange.getExchangePerson().getEmployeename();
        }
        if (billExchange.getHandOverDepartment() != null) {
            this.handOverToDepartment = billExchange.getHandOverDepartment().getId();
            this.handOverToDepartmentName = billExchange.getHandOverDepartment().getName();
        }
		if (billExchange.getVendor() != null) {
			this.vendorId = billExchange.getVendor().getId();
			this.vendorName = billExchange.getVendor().getName();
		}
        this.handOverType = billExchange.getHandOverType();
        this.status = billExchange.getStatus();
        this.createdBy = billExchange.getCreatedBy();
        this.updatedBy = billExchange.getUpdatedBy();
        this.created_date = billExchange.getCreated_date();
        this.updated_date = billExchange.getUpdated_date();
        this.billInwardId = billExchange.getBillInwardId();
        this.billAmount = billExchange.getBillAmount();
        this.firmOrgId = billExchange.getFirm_org_id();

        this.billDetails = billExchange.getBillExchangeProducts() != null
                ? billExchange.getBillExchangeProducts().stream().map(field -> new BillExchangeProductDto(field)).collect(Collectors.toList()) : null;


    }
}
