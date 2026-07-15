package com.aria.model;

import com.aria.dto.ProductExcessStorageDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "product_excess_storage")
public class ProductExcessStorage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "firm_id", referencedColumnName = "id")
    private Organisation firmOrg;

    @OneToOne
    @JoinColumn(name = "vendor_id", referencedColumnName = "id")
    private Vendor vendor;

    @Column(name = "bill_no")
    private String billNo;

    @Column(name = "bill_Date")
    @Temporal(TemporalType.DATE)
    private Date billDate;

    @Column(name = "bill_Inward_Date")
    @Temporal(TemporalType.DATE)
    private Date billInwardDate;

    @Column(name = "bill_Transactionno")
    private String billTransactionno;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @Column
    private String status;


    @OneToMany(mappedBy = "excessStorageId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ExcessShortageProducts> excessShortageProducts;


    @Column(name = "lorry_Serial_No")
    private String lorrySerialNo;


    @Column(name = "branch_id", updatable = false)
    private Long branchId;



    @Column(name = "verified_by")
    private Long verifiedBy;

    @Column
    private LocalDateTime verified_date;

    @Column(name = "rejected_by")
    private Long rejectedBy;

    @Column
    private LocalDateTime rejected_date;





    public ProductExcessStorage(ProductExcessStorageDto productExcessStorage) {

        this.id = productExcessStorage.getId();
        if (productExcessStorage.getFirmId() != null) {
            Organisation organisation = new Organisation();
            organisation.setId(productExcessStorage.getFirmId());
            this.firmOrg = organisation;
        }
        if (productExcessStorage.getVendorId() != null) {
            Vendor vendor = new Vendor();
            vendor.setId(productExcessStorage.getVendorId());
            this.vendor = vendor;
        }
        this.billNo = productExcessStorage.getBillNo();
        this.billTransactionno = productExcessStorage.getBillTransactionno();

        this.status = productExcessStorage.getStatus();
        this.lorrySerialNo = productExcessStorage.getLorrySerialNo();
        this.branchId = productExcessStorage.getBranchId();
    }

}
