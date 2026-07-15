package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.HoPurchaseOrderDto;
import com.aria.dto.PurchaseOrderDto;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "purchase_order")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "po_no")
    private String poNumber;

    @Column(name = "po_date")
    private Date poDate;

    @Column(name = "company_branch_id")
    private Long companyBranchId;

    @Column(name = "item_group_id")
    private Long itemGroupId;

    @Column(name = "item_group")
    private String itemGroup;

    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "po_total_amount")
    private Double poTotalAmount;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "city_id")
    private Long cityId;

    @Column(name = "city")
    private String city;

    @Column(name = "address_id")
    private Long addressId;

    @Column(name = "state_id")
    private Long stateId;

    @Column(name = "state_name")
    private String stateName;

    @Column(name = "gst_no")
    private String gstNO;

    @Column(name = "broker_id")
    private Long brokerId;

    @Column(name = "broker_name")
    private String brokerName;

    @Column(name = "sub_broker_id")
    private Long subBrokerId;

    @Column(name = "sub_broker_name")
    private String subBrokerName;

    @Column(name = "total_initial_price")
    private Double totalInitialPrice;

    @Column(name = "total_final_price")
    private Double totalFinalPrice;

    @Column(name = "camp_id")
    private Long campId;

    @Column(name = "camp")
    private String camp;

    @Column(name = "po_notes")
    private String poNotes;

    @OneToOne
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private User createdBy;

    @Column(name = "created_on")
    private Date createdON;

    @Column(name = "po_transfered_by")
    private String poTransferedBy;

    @Column(name = "po_Transfered_on")
    private Date poTransferedON;

    @Column(name = "reference_no")
    private String referenceNo;

    @Column(name = "po_mode")
    private String poMode;

    @Column(name = "status")
    private String status;

    @Column(name = "firm_id")
    private Long firmId;

    @Column(name = "received_by")
    private String receivedBy;

    @Column(name = "receiver_designation")
    private String receiverDesignation;

    @Column(name = "received_oN")
    private Date receivedON;

    @Column(name = "approved_on")
    private Date approvedOn;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "approval_status")
    private String approvalStatus;

    @Column(name = "cess_date")
    private Date cessDate;

    @Column(name = "cess_no")
    private String cessNo;

    @Column(name = "cess_amount")
    private Double cessAmount;

    @Column(name = "po_no_original")
    private String poNoOriginal;

    @Column(name = "cess_type")
    private String cessType;

    @Column(name = "type_flag", updatable = false)
    private String typeFlag;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_id", referencedColumnName = "id", nullable = false)
    private Vendor vendor;

    @OneToOne
    @JoinColumn(name = "verified_by", referencedColumnName = "id")
    private User verifiedBy;

    @Column(name = "verified_on")
    private Date verifiedOn;

    @OneToOne
    @JoinColumn(name = "approved_by", referencedColumnName = "id")
    private User approvedBy;

    @OneToOne
    @JoinColumn(name = "authorized_by", referencedColumnName = "id")
    private User authorizedBy;

    @Column(name = "authorized_on")
    private Date authorizedOn;

    @OneToOne
    @JoinColumn(name = "rejected_by", referencedColumnName = "id")
    private User rejectedBy;

    @Column(name = "rejected_on")
    private Date rejectedOn;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @OneToOne
    @JoinColumn(name = "updated_by", referencedColumnName = "id")
    private User updatedBy;

    @Column
    private LocalDateTime updated_date;

    @OneToMany(mappedBy = "purchaseOrderId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Where(clause = "active = true")
    private List<PurchaseOrderItems> purchaseOrderItems;

    @OneToMany(mappedBy = "purchaseOrderId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PurchasePayment> purchasePaymentOption;

    @Column(name = "proposal_id")
    private Long proposalId;

    @Column(name = "deliveryplant")
    private Long deliveryplant;

    @Column(name = "deliveryduedate")
    private Date deliveryduedate;

    @Column(name = "partnercontribution")
    private Long partnercontribution;

    @Column(name = "po_status")
    private String po_status;

    @Column(updatable = false)
    private String raw_ho_Po;

    @Column(updatable = false)
    private Long ho_po_id;

    @Column(name = "advance_type")
    private boolean advanceType;

    @OneToOne
    @JoinColumn(name = "dept_id", referencedColumnName = "id")
    private DepartmentDetails dept;

    @OneToOne
    @JoinColumn(name = "branch_id", referencedColumnName = "id")
    private Branch branch;


    @OneToOne
    @JoinColumn(name = "amendment_by", referencedColumnName = "id")
    private User amendmentBy;

    @Column
    private LocalDateTime amendment_date;

    @Column
    private Double roundOff_amount;

    @Column
    private String roundOffType;

    public boolean isAdvanceType() {
        return advanceType;
    }

    public PurchaseOrder(PurchaseOrderDto purchaseOrderDto) {
        DateUtil dateUtil = new DateUtil();
        this.id = purchaseOrderDto.getId();
        this.poNumber = purchaseOrderDto.getPoNumber();
        if (purchaseOrderDto.getPo_date() != null && purchaseOrderDto.getPo_date() != "") {
            this.poDate = dateUtil.getStringTODateYYYYMMDD(purchaseOrderDto.getPo_date());
        }
        this.firmId = purchaseOrderDto.getFirmId();
        this.companyBranchId = purchaseOrderDto.getCompany_branch_id();
        this.itemGroupId = purchaseOrderDto.getItem_group_id();
        this.itemGroup = purchaseOrderDto.getItem_group();
        this.supplierId = purchaseOrderDto.getVendorId();
        this.poTotalAmount = purchaseOrderDto.getPo_total_amount();
        this.roundOff_amount = purchaseOrderDto.getRoundOff_amount();
        this.roundOffType = purchaseOrderDto.getRoundOffType();
        this.supplierName = purchaseOrderDto.getVendorName();
        this.cityId = purchaseOrderDto.getCity_id();
        this.city = purchaseOrderDto.getCity();
        this.addressId = purchaseOrderDto.getAddress_id();
        this.stateId = purchaseOrderDto.getState_id();
        this.stateName = purchaseOrderDto.getState_name();
        this.gstNO = purchaseOrderDto.getGst_no();
        this.brokerId = purchaseOrderDto.getBroker_id();
        this.brokerName = purchaseOrderDto.getBroker_name();
        this.subBrokerId = purchaseOrderDto.getSub_broker_id();
        this.subBrokerName = purchaseOrderDto.getSub_broker_name();
        this.totalInitialPrice = purchaseOrderDto.getTotal_initial_price();
        this.totalFinalPrice = purchaseOrderDto.getTotal_final_price();
        this.campId = purchaseOrderDto.getCamp_id();
        this.camp = purchaseOrderDto.getCamp();
        this.poNotes = purchaseOrderDto.getPo_notes();
        if (purchaseOrderDto.getCreated_on() != null && purchaseOrderDto.getCreated_on() != "")
            this.createdON = dateUtil.getStringTODateYYYYMMDD(purchaseOrderDto.getCreated_on());
        this.poTransferedBy = purchaseOrderDto.getPo_transfered_by();
        if (purchaseOrderDto.getPo_transfered_on() != null && purchaseOrderDto.getPo_transfered_on() != "")
            this.poTransferedON = dateUtil.getStringTODateYYYYMMDD(purchaseOrderDto.getPo_transfered_on());
        this.referenceNo = purchaseOrderDto.getReference_no();
        this.poMode = purchaseOrderDto.getPo_mode();
        this.status = purchaseOrderDto.getStatus();
        this.receivedBy = purchaseOrderDto.getReceived_by();
        this.receiverDesignation = purchaseOrderDto.getReceiver_designation();
        if (purchaseOrderDto.getReceived_on() != null && purchaseOrderDto.getReceived_on() != "")
            this.receivedON = dateUtil.getStringTODateYYYYMMDD(purchaseOrderDto.getReceived_on());
        if (purchaseOrderDto.getApproved_on() != null && purchaseOrderDto.getApproved_on() != "")
            this.approvedOn = dateUtil.getStringTODateYYYYMMDD(purchaseOrderDto.getApproved_on());
        this.remarks = purchaseOrderDto.getRemarks();
        this.approvalStatus = purchaseOrderDto.getApproval_status();
        if (purchaseOrderDto.getCess_date() != null && purchaseOrderDto.getCess_date() != "")
            this.cessDate = dateUtil.getStringTODateYYYYMMDD(purchaseOrderDto.getCess_date());
        this.cessNo = purchaseOrderDto.getCess_no();
        this.cessAmount = purchaseOrderDto.getCess_amount();
        this.poNoOriginal = purchaseOrderDto.getPo_no_original();
        this.cessType = purchaseOrderDto.getCess_type();
        this.typeFlag = purchaseOrderDto.getTypeflag();
        this.proposalId = purchaseOrderDto.getProposalId();
        if (purchaseOrderDto.getDeliveryduedate() != null && purchaseOrderDto.getDeliveryduedate() != "")
            this.deliveryduedate = dateUtil.getStringTODateYYYYMMDD(purchaseOrderDto.getDeliveryduedate());
        this.deliveryplant = purchaseOrderDto.getDeliveryplant();
        this.partnercontribution = purchaseOrderDto.getPartnercontribution();
        if (purchaseOrderDto.isAdvanceType()) {
            this.advanceType = purchaseOrderDto.isAdvanceType();
        } else {
            this.advanceType = false;
        }
        if (purchaseOrderDto.getDept() != null) {
            DepartmentDetails departmentDetails = new DepartmentDetails();
            departmentDetails.setId(purchaseOrderDto.getDept());
            this.dept = departmentDetails;
        }
        if (purchaseOrderDto.getBranch() != null) {
            Branch branch = new Branch();
            branch.setId(purchaseOrderDto.getBranch());
            this.branch = branch;
        }
        if (purchaseOrderDto.getVerifiedBy() != null) {
            User user = new User();
            user.setId(purchaseOrderDto.getVerifiedBy());
            this.verifiedBy = user;
        }
        if (purchaseOrderDto.getApprovedBy() != null) {
            User user = new User();
            user.setId(purchaseOrderDto.getApprovedBy());
            this.approvedBy = user;
        }
        if (purchaseOrderDto.getAuthorizedBy() != null) {
            User user = new User();
            user.setId(purchaseOrderDto.getAuthorizedBy());
            this.authorizedBy = user;
        }
    }

    public PurchaseOrder(HoPurchaseOrderDto purchaseOrderDto) {
        DateUtil dateUtil = new DateUtil();
        this.ho_po_id = purchaseOrderDto.getId();
        this.poNumber = purchaseOrderDto.getPo_no();
        if (purchaseOrderDto.getPo_date() != null && purchaseOrderDto.getPo_date() != "")
            this.poDate = dateUtil.getStringTODateWk(purchaseOrderDto.getPo_date());
        this.companyBranchId = purchaseOrderDto.getCompany_branch_id();
        this.itemGroupId = purchaseOrderDto.getItem_group_id();
        this.itemGroup = purchaseOrderDto.getItem_group();
        this.supplierId = purchaseOrderDto.getEarms_party_id();
        this.poTotalAmount = purchaseOrderDto.getPo_total_amount();
        this.supplierName = purchaseOrderDto.getSupplier_name();
        this.cityId = purchaseOrderDto.getCity_id();
        this.city = purchaseOrderDto.getCity();
        this.addressId = purchaseOrderDto.getAddress_id();
        this.stateId = purchaseOrderDto.getState_id();
        this.stateName = purchaseOrderDto.getState_name();
        this.gstNO = purchaseOrderDto.getGst_no();
        this.brokerId = purchaseOrderDto.getBroker_id();
        this.brokerName = purchaseOrderDto.getBroker_name();
        this.subBrokerId = purchaseOrderDto.getSub_broker_id();
        this.subBrokerName = purchaseOrderDto.getSub_broker_name();
        this.totalInitialPrice = purchaseOrderDto.getTotal_initial_price();
        this.totalFinalPrice = purchaseOrderDto.getTotal_final_price();
        this.campId = purchaseOrderDto.getCamp_id();
        this.camp = purchaseOrderDto.getCamp();
        this.poNotes = purchaseOrderDto.getPo_notes();
        if (purchaseOrderDto.getCreated_on() != null && purchaseOrderDto.getCreated_on() != "")
            this.createdON = dateUtil.getStringTODateWk(purchaseOrderDto.getCreated_on());
        this.poTransferedBy = purchaseOrderDto.getPo_transfered_by();
        if (purchaseOrderDto.getPo_transfered_on() != null && purchaseOrderDto.getPo_transfered_on() != "")
            this.poTransferedON = dateUtil.getStringTODateWk(purchaseOrderDto.getPo_transfered_on());
        this.referenceNo = purchaseOrderDto.getReference_no();
        this.poMode = purchaseOrderDto.getPo_mode();
        this.status = purchaseOrderDto.getStatus();
        this.receivedBy = purchaseOrderDto.getReceived_by();
        this.receiverDesignation = purchaseOrderDto.getReceiver_designation();
        if (purchaseOrderDto.getReceived_on() != null && purchaseOrderDto.getReceived_on() != "")
            this.receivedON = dateUtil.getStringTODateWk(purchaseOrderDto.getReceived_on());
        if (purchaseOrderDto.getApproved_on() != null && purchaseOrderDto.getApproved_on() != "")
            this.approvedOn = dateUtil.getStringTODateWk(purchaseOrderDto.getApproved_on());
        this.remarks = purchaseOrderDto.getRemarks();
        this.approvalStatus = purchaseOrderDto.getApproval_status();
        if (purchaseOrderDto.getCess_date() != null && purchaseOrderDto.getCess_date() != "")
            this.cessDate = dateUtil.getStringTODateWk(purchaseOrderDto.getCess_date());
        this.cessNo = purchaseOrderDto.getCess_no();
        this.cessAmount = purchaseOrderDto.getCess_amount();
        this.poNoOriginal = purchaseOrderDto.getPo_no_original();
        this.cessType = purchaseOrderDto.getCess_type();
        this.typeFlag = purchaseOrderDto.getTypeflag();

    }
}
