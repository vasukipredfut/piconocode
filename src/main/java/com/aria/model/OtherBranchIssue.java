package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;


import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "other_branch_issue")
public class OtherBranchIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "issue_id")
    private Long issueId;

    @Column(name = "material_with_in_branch_id")
    private Long materialWithInBranchId;

    @OneToOne
    @JoinColumn(name = "request_dept_id", referencedColumnName = "id")
    private DepartmentDetails requestDepartment;

    @OneToOne
    @JoinColumn(name = "request_branch_id", referencedColumnName = "id")
    private Branch requestBranch;

    @OneToOne
    @JoinColumn(name = "org_id", referencedColumnName = "id" , updatable = false )
    private Organisation organisation;

    @OneToOne
    @JoinColumn(name = "issue_dept_id", referencedColumnName = "id")
    private DepartmentDetails issueDepartment;

    @OneToOne
    @JoinColumn(name = "issue_branch_id", referencedColumnName = "id")
    private Branch issueBranch;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User issueBy;


    @Column
    private String status;

    @Column(name = "trn_no")
    private String trnNo;

    @Column(name = "issue_reference")
    private String issueReference;

    @Column(name = "issue_mode")
    private String issueMode;

    @Column
    private Long delivery_user_id;

    @Column(name = "transport_mode")
    private String transportMode;

    @Column(name = "transport_vehicle_type")
    private String transportVehicleType;

    @Column(name = "transport_vehicle_no")
    private String transportVehicleNo;

    @Column(name = "dispatch_on")
    @Temporal(TemporalType.DATE)
    private Date dispatchOn;

   /* @Column(name = "material_return")
    private String materialReturn;*/

    @Column(name = "generateeway_bill")
    private String generateEWayBill;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @OneToMany(mappedBy = "issueItemId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Where(clause = "active=0")
    private List<OtherBranchIssueItems> otherBranchIssueItems;


    @Column(name = "gdc_no")
    private String GDCNo;
}
