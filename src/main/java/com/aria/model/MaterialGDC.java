package com.aria.model;

import com.aria.dto.GDCDetailsList;
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
@Table(name = "material_flow_gdc")
public class MaterialGDC {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long  material_Flow_Gdc_Id ;

    @Column
    private Long gdc_Company_Id ;
    @Column
    private String gdc_Number ;
    @Column
    private String gdc_Transaction_Type ;
    @Column
    private Long gdc_Transaction_Type_Detail_Id ;
    @Column
    private LocalDateTime gdc_Date ;
    @Column
    private String docket_Number ;
    @Column
    private Long sender_By_Id ;
    @Column
    private Long sender_By_Department ;
    @Column
    private Long sender_By_Branch_Id ;
    @Column
    private String truck_Number ;
    @Column
    private String material_Return ;
    @Column
    private String material_Return_Status ;
    @Column
    private Long created_By_Id ;
    @Column
    private Long created_By_Department_Id ;
    @Column
    private Long created_By_Branch_Id ;
    @Column
    private LocalDateTime created_Time_Stamp ;
    @Column
    private int active_Flag ;
    @Column
    private Long ho_goods_trans_id ;
    @Column
    private int eWayBill_flag ;
    @Column
    private String eWayBill_QRCode ;
    @Column
    private String eWayBill_no ;
    @Column
    private Long gdcVerifiedBy ;
    @Column
    private Long eWayBill_cancelledBy ;
    @Column
    private LocalDateTime eWayBill_cancelledOn ;

    public MaterialGDC(OtherBranchIssue otherBranchIssue  ) {

        this.gdc_Company_Id = otherBranchIssue.getOrganisation().getId();
        this.gdc_Number = otherBranchIssue.getGDCNo();
        this.gdc_Transaction_Type = otherBranchIssue.getGDCNo();
        this.docket_Number = otherBranchIssue.getGDCNo();
        this.sender_By_Id = otherBranchIssue.getIssueBy().getId();
        this.sender_By_Department = otherBranchIssue.getIssueDepartment().getId();
        this.sender_By_Branch_Id = otherBranchIssue.getIssueBranch().getId();
        this.truck_Number = otherBranchIssue.getTransportVehicleNo();
        this.created_By_Department_Id = otherBranchIssue.getRequestDepartment().getId();
        this.created_By_Branch_Id = otherBranchIssue.getRequestBranch().getId();
        this.created_Time_Stamp = otherBranchIssue.getCreated_date();

    }

    /*public MaterialGDC(GDCDetailsList gdc ) {

        //this.gdc_Company_Id = otherBranchIssue.getOrganisation().getId();
        this.gdc_Number = gdc.getGdcNo();
        this.gdc_Transaction_Type = otherBranchIssue.getGDCNo();
        this.docket_Number = otherBranchIssue.getGDCNo();
        this.sender_By_Id = otherBranchIssue.getIssueBy().getId();
        this.sender_By_Department = otherBranchIssue.getIssueDepartment().getId();
        this.sender_By_Branch_Id = otherBranchIssue.getIssueBranch().getId();
        this.truck_Number = otherBranchIssue.getTransportVehicleNo();
        this.created_By_Department_Id = otherBranchIssue.getRequestDepartment().getId();
        this.created_By_Branch_Id = otherBranchIssue.getRequestBranch().getId();
        this.created_Time_Stamp = otherBranchIssue.getCreated_date();

    }*/
}
