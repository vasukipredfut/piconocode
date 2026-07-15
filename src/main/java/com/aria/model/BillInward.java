package com.aria.model;

import com.aria.dto.BillInwardDto;

import com.aria.dto.GRNDto;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity

@NoArgsConstructor
@Table(name = "bill_inward")
@Getter
@Setter
public class BillInward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "inward_branch_id", referencedColumnName = "id")
    private Branch inwardBranch;

    @OneToOne
    @JoinColumn(name = "deliver_branch_id", referencedColumnName = "id")
    private Branch deliverBranch;

    @Column(name = "received_date")
    private Date receivedDate;

    @Column(name = "bill_date")
    private Date billDate;

    @OneToOne
    @JoinColumn(name = "vendor_id", referencedColumnName = "id")
    private Vendor vendor;

    @Column(name = "billno")
    private String billNo;


    @OneToOne
    @JoinColumn(name = "firm_org_id", referencedColumnName = "id")
    private Organisation firmOrg;

    @Column
    private String tcs;

    @Column
    private Double tcsper;

    @Column
    private String status;

    @Column(name = "eway_bill")
    private String ewayBill;

    @Column(name = "cess_tax")
    private String cessTax;

    @Column(name = "product_category")
    private String productCategory;

    @Column(name = "bill_copy")
    private String billCopy;

    @Column(name = "created_by",updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    /* @Column(name = "plat_status")
     private String platStatus;

     @Column(name = "dgm_status")
     private String dgmStatus;

     @Column(name = "md_status")
     private String mdStatus;
 */
    @Column(name = "lorry_date")
    private Date lorryDate;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @OneToMany(mappedBy = "inwardId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BillInwardItemReceived> itemReceived;

    @OneToMany(mappedBy = "inwardId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BillInwardLorryDetails> billInwardLorryDetails;

    @Column(name = "insurance")
    private Double insurance;

    @Column(name = "bill_freight_amount")
    private Double billFreightAmount;

    @Column(name = "bill_amount")
    private Double billAmount;

    @Column
    private String lorryno;

    @Column
    private String lorry_with_Weight;

    @Column
    private String lorry_without_Weight;

    @Column
    private Boolean lorry_freight;
    @Column
    private String round_of_type;
    @Column
    private String round_of_value;

    @Column(name = "ho_bill_id", updatable = false)
    private Long hoBillId;

    @Column(updatable = false)
    private String raw_ho_grn;

    @Column(name = "lorry_serial_no")
    private String lorrySerialNo;

    @Column(name = "per_bag_cost",updatable = false)
    private String perBagCost;

    @Column
    private Double cgst;

    @Column
    private Double igst;

    @Column
    private Double sgst;

    @Column(updatable = false)
    private Double cgstper;

    @Column(updatable = false)
    private Double igstper;

    @Column(updatable = false)
    private Double sgstper;

    @Column(name = "camp", updatable = false)
    private String camp;

    @Column(name = "lorryfreight", updatable = false)
    private Double lorryfreight;

    @Column(name = "dedLorryfreight",updatable = false)
    private Double dedLorryfreight;

    @Column(name = "otheramount",updatable = false)
    private Double otherAmount;

    @Column(name = "dedotheramount",updatable = false)
    private Double dedOtherAmount;

    @Column(name = "totBags",updatable = false)
    private Double totBags;

    @Column(name = "purpose",updatable = false)
    private String purpose;



    public BillInward(BillInwardDto billInward) {
        this.id = billInward.getId();
        this.hoBillId = billInward.getHoBillId();

        if (billInward.getInwardBranchId() != null) {
            Branch branch = new Branch();
            branch.setId(billInward.getInwardBranchId());
            this.inwardBranch = branch;
        }
        if (billInward.getDeliverBranchId() != null) {
            Branch branch = new Branch();
            branch.setId(billInward.getDeliverBranchId());
            this.deliverBranch = branch;
        }
        if (billInward.getVendorId() != null) {
            Vendor vendor = new Vendor();
            vendor.setId(billInward.getVendorId());
            this.vendor = vendor;
        }
        if (billInward.getFirmOrgId() != null) {
            Organisation organisation = new Organisation();
            organisation.setId(billInward.getFirmOrgId());
            this.firmOrg = organisation;
        }

        this.billNo = billInward.getBillNo().trim();
        this.tcs = billInward.getTcs();
        this.tcsper = billInward.getTcsper();
        this.status = billInward.getStatus();
        this.ewayBill = billInward.getEwayBill();
        this.cessTax = billInward.getCessTax();
        this.productCategory = billInward.getProductCategory();
        this.billCopy = billInward.getBillCopy();
        this.createdBy = billInward.getCreatedBy();
        this.updatedBy = billInward.getUpdatedBy();
        this.created_date = billInward.getCreated_date();
        this.updated_date = billInward.getUpdated_date();
        this.lorry_with_Weight = billInward.getLorryWithWeight();
        this.lorry_without_Weight = billInward.getLorryWithoutWeight();
        this.billAmount = billInward.getBillAmount();
        this.billFreightAmount = billInward.getBillFreightAmount();
        this.insurance = billInward.getInsurance();
        this.lorryno = billInward.getLorryno().toUpperCase();
        this.lorry_freight = billInward.getLorry_freight();
        this.round_of_type = billInward.getRoundOfType();
        this.round_of_value = billInward.getRoundOfValue();
        this.lorrySerialNo = billInward.getLorrySerialNo();

        this.igst = billInward.getIgst();
        this.sgst = billInward.getSgst();
        this.cgst = billInward.getCgst();
        this.igstper = billInward.getIgstper();
        this.sgstper = billInward.getSgstper();
        this.cgstper = billInward.getCgstper();

        this.perBagCost = billInward.getPerBagCost();

    }


    public BillInward(BillInwardDto billInward, Boolean status) {
        DateUtil dateUtil = new DateUtil();
        this.id = billInward.getId();
        if (billInward.getVendorId() != null) {
            Vendor vendor = new Vendor();
            vendor.setId(billInward.getVendorId());
            this.vendor = vendor;
        }
        this.ewayBill = billInward.getEwayBill();
        this.billNo = billInward.getBillNo();
    }

    public BillInward(GRNDto billInward) {

        DateUtil dateUtil = new DateUtil();
        this.id = billInward.getId();
        this.hoBillId = billInward.getHoBillId();

        if (billInward.getVendorId() != null) {
            Vendor vendor = new Vendor();
            vendor.setId(billInward.getVendorId());
            this.vendor = vendor;
        }

        if (billInward.getFirmOrgId() != null) {
            Organisation organisation = new Organisation();
            organisation.setId(billInward.getFirmOrgId());
            this.firmOrg = organisation;
        }
        this.billNo = billInward.getBillNo();
        if (billInward.getBillDate() != null) {
            this.billDate = dateUtil.getStringTODateWk(billInward.getBillDate());
        }
        this.billAmount = billInward.getBillAmount();

        /*if (billInward.getDeliverBranchId() != null && billInward.getDeliverBranchId() > 0) {
            Branch branch = new Branch();
            branch.setId(billInward.getDeliverBranchId());
            this.deliverBranch = branch;
        }*/
        this.ewayBill = billInward.getEwayBill();
        this.productCategory = billInward.getProductCategory();
        this.camp = billInward.getCamp();
        this.cessTax = billInward.getCessTax();
        this.insurance = billInward.getInsurance();
        this.igst = billInward.getIgst();
        this.sgst = billInward.getSgst();
        this.cgst = billInward.getCgst();
        this.igstper = billInward.getIgstper();
        this.sgstper = billInward.getSgstper();
        this.cgstper = billInward.getCgstper();

        this.tcs = billInward.getTcs();
        this.tcsper = billInward.getTcsper();

        this.lorryfreight = billInward.getLorryfreight();
        this.dedLorryfreight = billInward.getDedLorryfreight();
        this.otherAmount = billInward.getOtherAmount();
        this.dedOtherAmount = billInward.getDedOtherAmount();
        if(billInward.getRoundOff() > 0){
            this.round_of_value = String.valueOf(billInward.getRoundOff());
            this.round_of_type = "Add";
        }
        if(billInward.getDedRoundOff() > 0){
            this.round_of_value = String.valueOf(billInward.getDedRoundOff());
            this.round_of_type = "Less";
        }

        this.totBags = billInward.getTotBags();

        this.perBagCost = billInward.getPerBagCost();

        this.purpose = billInward.getPurpose();

        this.lorry_freight = billInward.isLorry_freight();
        this.lorryno = billInward.getLorryNo();
        this.lorry_freight = billInward.isLorry_freight();

    }
}
