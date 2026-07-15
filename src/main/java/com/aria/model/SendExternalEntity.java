package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "material_flow_send_external")
public class SendExternalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_Flow_Send_External_Id")
    private Long materialFlowSendExternalId;

    @Column(name = "transaction_Number")
    private String transactionNumber="";

    @Column(name = "reference_Date")
    private Date referenceDate;

    @Column(name = "active_Flag")
    private long activeFlag;

    @Column(name = "issue_Mode")
    private String issueMode="";

    @Column(name = "issue_By_Id")
    private Long issueById;

    @Column(name = "material_Return")
    private String materialReturn="";

    @Column(name = "to_Reach")
    private String toReach="";

    @Column(name = "to_Vendor_Ref_Id")
    private Long toVendorRefId;

    @Column(name = "to_Department_Id")
    private Long toDepartmentId;

    @Column(name = "to_Branch_Id")
    private Long toBranchId;

    @Column(name = "created_Department_Id")
    private Long createdDepartmentId;

    @Column(name = "created_Branch_Id")
    private Long createdBranchId;

    @Column(name = "status")
    private Long status;

    @Column(name = "vehicle")
    private String vehicle;

    @Column(name = "vehicle_Number")
    private String vehicleNumber;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @Column(name = "gdc_no")
    private String GDCNO;

    @Column
    private LocalDateTime gdc_date;

    @Column(name = "eway_bill")
    private String ewayBill;

    @Column(name = "firm_Id", updatable = false)
    private Long firmId;


}
