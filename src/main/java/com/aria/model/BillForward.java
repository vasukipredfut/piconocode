package com.aria.model;


import com.aria.dto.BillForwardDto;

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
@Table(name = "bill_forward")
@Getter
@Setter
public class BillForward {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long firm_org_id;
    @Column
    private Long bill_ref_id;

    @Column(name = "bill_number")
    private String billNumber;

    @Column(name = "bill_date")
    private Date billDate;

    @OneToOne
    @JoinColumn(name = "from_branch_id", referencedColumnName = "id", nullable = true)
    private Branch fromBranch;

    @OneToOne
    @JoinColumn(name = "to_branch_id", referencedColumnName = "id", nullable = true)
    private Branch toBranch;

    @Column(name = "bill_inward_date")
    private Date billInwardDate;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = true)
    private User fromPerson;
    
    @OneToOne
    @JoinColumn(name = "vendor_id", referencedColumnName = "id")
    private Vendor vendor;

    @OneToOne
    @JoinColumn(name = "hard_copy_user_id", referencedColumnName = "id", nullable = true)
    private User hardCopyHandOverToUser;

    @Column(name = "forward_date")
    private Date forwardDate;

    @Column
    private String status;

    @Column(name = "created_by",updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @Column(name = "bill_approved_by")
    private Long billApprovedBy;

    @Column
    private Date bill_approved_date;


    @Column
    private Long bill_verified_by;

    @Column
    private Date bill_verified_date;

    @OneToMany(mappedBy = "forwardId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BillForwardProduct> billForwardProducts;

    @Column
    private Long trn_no;

    public BillForward(BillForwardDto billForward) {
        DateUtil dateUtil = new DateUtil();
        this.id = billForward.getId();
        this.billNumber = billForward.getBillNumber();
        this.bill_ref_id = billForward.getBillInwardId();
        this.firm_org_id = billForward.getFirmOrgId();
        if(billForward.getFromBranch() != null){
            Branch branch = new Branch();
            branch.setId(billForward.getFromBranch());
            this.fromBranch = branch;
        }
        if(billForward.getToBranch() != null){
            Branch branch = new Branch();
            branch.setId(billForward.getToBranch());
            this.toBranch = branch;
        }
        if(billForward.getFromPerson() != null){
            User user = new User();
            user.setId(billForward.getFromPerson());
            this.fromPerson = user;
        }
        if(billForward.getVendorId() != null){
            Vendor vendor = new Vendor();
            vendor.setId(billForward.getVendorId());
            this.vendor = vendor;
        }
        if(billForward.getFromPerson() != null){
            User user = new User();
            user.setId(billForward.getFromPerson());
            this.fromPerson = user;
        }
        if(billForward.getHardCopyHandOverToUser() != null){
            User user = new User();
            user.setId(billForward.getHardCopyHandOverToUser());
            this.hardCopyHandOverToUser = user;
        }
        this.status = billForward.getStatus();
        this.createdBy = billForward.getCreatedBy();
        this.updatedBy = billForward.getUpdatedBy();
        this.created_date = billForward.getCreated_date();
        this.updated_date = billForward.getUpdated_date();
        this.billApprovedBy = billForward.getBillApprovedBy();
        if(billForward.getBillApprovedDate() != null) {
            this.bill_approved_date = dateUtil.getStringTODateWk(billForward.getBillApprovedDate());
        }
        this.bill_verified_by = billForward.getBillVerifiedBy();
        if(billForward.getBillVerifiedDate() != null) {
            this.bill_verified_date = dateUtil.getStringTODateWk(billForward.getBillVerifiedDate());
        }
    }
}
