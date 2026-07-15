package com.aria.model;

import com.aria.dto.DirectPurchaseDto;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "direct_purchase")
public class DirectPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_no")
    private String referenceNo;

    @OneToOne
    @JoinColumn(name = "branch_id", referencedColumnName = "id")
    private Branch branch;

    @Column(name = "storage_date")
    private Date storageDate;


    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @Column
    private Long vendor_id;

    @Column
    private Date inward_date;

    @Column
    private Double bill_amount;

    @Column
    private Double lorry_freight_amount;

    //Pending - List for all, Complete - List for Review and Approve, Approved - List for MD, Authorized - Move off DP
    @Column
    private String stock_status;


    @Column(name = "product_category")
    private String productCategory;

    @OneToMany(mappedBy = "entryId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<DpEntryStoredProduct> storedProducts;


    @Column
    private Date lorry_date;

    @Column
    private Date bill_date;

    @OneToOne
    @JoinColumn(name = "camp_id", referencedColumnName = "id")
    private Camp camp;

    @Column(name = "bill_ref_id", updatable = false)
    private Long billRefId;

    @Column
    private Long firm_org_id;

    @Column(name = "excess_shortage")
    private Boolean excessOrShortage;


    @Column
    private String credit_or_debit;

    @Column
    private String eTransfer;

    @Column
    private Long verified_by;

    @Column
    private LocalDateTime verified_date;

    @Column
    private Long approved_by;

    @Column
    private LocalDateTime approved__date;

    @Column
    private Long authorised_by;

    @Column
    private LocalDateTime authorised_date;


    @OneToOne
    @JoinColumn(name = "hand_over_to_dept_id", referencedColumnName = "id")
    private DepartmentDetails handOverDepartment;

    @Column
    private Long rejected_by;

    @Column
    private LocalDateTime rejected_date;


    public DirectPurchase(DirectPurchaseDto directPurchase) {
        DateUtil dateUtil = new DateUtil();
        this.id = directPurchase.getId();
        this.referenceNo = directPurchase.getReferenceNo();
        if (directPurchase.getBranchId() != null) {
            Branch branch = new Branch();
            branch.setId(directPurchase.getBranchId());
            this.branch = branch;
        }
        this.createdBy = directPurchase.getCreatedBy();
        this.updatedBy = directPurchase.getUpdatedBy();
        this.created_date = directPurchase.getCreated_date();
        this.updated_date = directPurchase.getUpdated_date();
        this.vendor_id = directPurchase.getVendorId();
        this.bill_amount = directPurchase.getBillAmount();
        this.lorry_freight_amount = directPurchase.getLorryFreightAmount();
        if (directPurchase.getInwardDate() != null) {
            this.inward_date = dateUtil.getStringTODateWk(directPurchase.getInwardDate());
        }
        if (directPurchase.getLorryDate() != null) {
            this.lorry_date = dateUtil.getStringTODateWk(directPurchase.getLorryDate());
        }
        if (directPurchase.getBillDate() != null) {
            this.bill_date = dateUtil.getStringTODateWk(directPurchase.getBillDate());
        }
        this.stock_status = directPurchase.getStockStatus();
        this.productCategory = directPurchase.getProductCategory();
        this.excessOrShortage = directPurchase.getExcessOrShortage();
        if(directPurchase.getCampId() != null) {
            Camp camp = new Camp();
            camp.setId(directPurchase.getCampId());
            this.camp = camp;
        }
        this.firm_org_id = directPurchase.getFirmOrgId();
        this.credit_or_debit = directPurchase.getCreditOrDebit();
        this.eTransfer = directPurchase.getETransfer();
        if (directPurchase.getHandOverToDepartment() != null) {
            DepartmentDetails details = new DepartmentDetails();
            details.setId(directPurchase.getHandOverToDepartment());
            this.handOverDepartment = details;
        }
    }
}
