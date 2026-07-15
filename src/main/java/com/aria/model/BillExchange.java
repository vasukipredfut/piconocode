package com.aria.model;


import com.aria.dto.BillExchangeDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


@Entity
@Table(name = "bill_exchange")
@Getter
@Setter
@NoArgsConstructor
public class BillExchange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bill_number")
    private String billNumber;


    @Column(name = "bill_amount")
    private Double billAmount;

    @Column(name = "bill_date")
    private Date billDate;

    @OneToOne
    @JoinColumn(name = "hand_over_to_branch_id", referencedColumnName = "id")
    private Branch handOverToBranch;

    @OneToOne
    @JoinColumn(name = "hand_over_to_dept_id", referencedColumnName = "id")
    private DepartmentDetails handOverDepartment;

    @Column(name = "hand_over_date")
    private Date handOverDate;

    @Column(name = "verification_date")
    private Date verificationDate;

    @Column(name = "bill_inward_date")
    private Date billInwardDate;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    public User exchangeInitiativePerson;

    @OneToOne
    @JoinColumn(name = "exchange_user_id", referencedColumnName = "id")
    public User exchangePerson;

    @OneToOne
    @JoinColumn(name = "vendor_id", referencedColumnName = "id")
    private Vendor vendor;

    @Column
    private String status;

    @Column(name = "hand_over_type")
    private String handOverType;

    @Column(name = "verification_status")
    private String verificationStatus;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @Column(name = "bill_inward_id")
    private Long billInwardId;

    @Column
    private Long firm_org_id;

    @OneToMany(mappedBy = "exchangeId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BillExchangeProduct> billExchangeProducts;


    public BillExchange(BillExchangeDto billExchange) {

        this.id = billExchange.getId();
        this.billNumber = billExchange.getBillNumber();
        if (billExchange.getHandOverToBranch() != null) {
            Branch branch = new Branch();
            branch.setId(billExchange.getHandOverToBranch());
            this.handOverToBranch = branch;
        }
        if (billExchange.getExchangeInitiativePerson() != null) {
            User user = new User();
            user.setId(billExchange.getExchangeInitiativePerson());
            this.exchangeInitiativePerson = user;
        }
        if (billExchange.getExchangePerson() != null) {
            User user = new User();
            user.setId(billExchange.getExchangePerson());
            this.exchangePerson = user;
        }
        if (billExchange.getHandOverToDepartment() != null) {
            DepartmentDetails details = new DepartmentDetails();
            details.setId(billExchange.getHandOverToDepartment());
            this.handOverDepartment = details;
        }
        if (billExchange.getVendorId() != null) {
            Vendor vendor = new Vendor();
            vendor.setId(billExchange.getVendorId());
            this.vendor = vendor;
        }
        this.handOverType = billExchange.getHandOverType();
        this.billAmount = billExchange.getBillAmount();
        this.status = billExchange.getStatus();
        this.createdBy = billExchange.getCreatedBy();
        this.updatedBy = billExchange.getUpdatedBy();
        this.created_date = billExchange.getCreated_date();
        this.billInwardId = billExchange.getBillInwardId();
        this.firm_org_id = billExchange.getFirmOrgId();
        this.updated_date = billExchange.getUpdated_date();

    }


}
