package com.aria.model;

import com.aria.dto.BillReceiveDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@NoArgsConstructor
@Table(name = "bill_receive")
@Getter
@Setter
public class BillReceive {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String billNumber;

    @Column
    private Date billDate;

    @OneToOne
    @JoinColumn(name = "from_branch_id", referencedColumnName = "id")
    private Branch fromBranch;

   /* @OneToOne
    @JoinColumn(name = "to_branch_id", referencedColumnName = "id")
    private Branch toBranch;*/

    @Column
    private Date billInwardDate;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User fromPerson;
    
    @OneToOne
    @JoinColumn(name = "vendor_id", referencedColumnName = "id")
    private Vendor vendor;

    @Column
    private Date forwardDate;

    @Column
    private Date exchangeDate;

    @Column
    private Date receivedDate;

    @OneToOne
    @JoinColumn(name = "received_user_id", referencedColumnName = "id")
    private User receivedPerson;

    @OneToOne
    @JoinColumn(name = "received_branch_id", referencedColumnName = "id")
    private Branch receivedBranch;

    @Column
    private String status;

    @Column(updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @Column
    private String billReceiveValue;

    @Column
    private String mdStatus;
    
    @Column
    private Date mdapprovalDate;

    @OneToMany(mappedBy = "receiveId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BillReceiveProduct> billReceiveProducts;

    @Column
    private Long firm_org_id;



    public BillReceive(BillReceiveDto billreceive) {
        this.id = billreceive.getId();
        this.billNumber = billreceive.getBillNumber();
        this.firm_org_id = billreceive.getFirmOrgId();
        if(billreceive.getFromBranch() != null){
            Branch branch = new Branch();
            branch.setId(billreceive.getFromBranch());
            this.fromBranch = branch;
        }
        if(billreceive.getFromPerson() != null){
            User user = new User();
            user.setId(billreceive.getFromPerson());
            this.fromPerson = user;
        }
        if(billreceive.getVendorId() != null){
            Vendor vendor = new Vendor();
            vendor.setId(billreceive.getVendorId());
            this.vendor = vendor;
        }
        this.createdBy = billreceive.getCreatedBy();
        this.updatedBy = billreceive.getUpdatedBy();
        this.created_date = billreceive.getCreated_date();
        this.updated_date = billreceive.getUpdated_date();

    }

    public BillReceive(BillForward billreceive) {
        this.billNumber = billreceive.getBillNumber();
        this.firm_org_id = billreceive.getFirm_org_id();
        this.billDate = billreceive.getBillDate();
        this.forwardDate = billreceive.getForwardDate();
        this.billInwardDate = billreceive.getBillInwardDate();
        this.fromBranch = billreceive.getFromBranch();
        this.fromPerson = billreceive.getFromPerson();
        this.vendor = billreceive.getVendor();
    }

    public BillReceive(BillExchange billreceive) {
        this.billNumber = billreceive.getBillNumber();
        this.firm_org_id = billreceive.getFirm_org_id();
        this.billDate = billreceive.getBillDate();
        this.exchangeDate = billreceive.getHandOverDate();
        this.billInwardDate = billreceive.getBillInwardDate();
        this.fromPerson = billreceive.getExchangeInitiativePerson();
        if(billreceive.getExchangeInitiativePerson().getBranch_id() != null){
            Branch branch = new Branch();
            branch.setId(billreceive.getExchangeInitiativePerson().getBranch_id());
            this.fromBranch = branch;
        }
        this.vendor = billreceive.getVendor();
    }

}