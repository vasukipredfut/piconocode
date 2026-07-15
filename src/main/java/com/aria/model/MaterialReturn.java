package com.aria.model;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "material_return")
public class MaterialReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "issue_id")
    private Long issueId;

    @Column(name = "from_dept_id")
    private Long fromDeptId;   // requestFrom

    @Column(name = "from_dept_name")
    private String fromDeptName;

    @Column(name = "from_id")
    private Long fromId;

    @Column(name = "from_name")
    private String fromName;

    @OneToOne
    @JoinColumn(name = "org_id", referencedColumnName = "id", updatable = false)
    private Organisation organisation;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User issueBy;

    @Column(name = "to_dept_id")
    private Long toDeptId;

    @Column(name = "to_dept_name")
    private String toDeptName;

    @Column(name = "to_id")
    private Long toId;

    @Column(name = "to_name")
    private String toName;

    @Column(name = "request_name")
    private String requestName;

    @Column(name = "request_description")
    private String requestDescription;

    @Column(name = "request_date", updatable = false)
    @Temporal(TemporalType.DATE)
    private Date requestDate;

    @Column(name = "issue_date", updatable = false)
    @Temporal(TemporalType.DATE)
    private Date issueDate;

    @Column(name = "dispatch_date", updatable = false)
    @Temporal(TemporalType.DATE)
    private Date dispatchDate;


    /*@Column(name = "return_date")
    @Temporal(TemporalType.DATE)
    private Date returnDate;*/


    @Column(name = "trn_no")
    private String trnNo;


    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;


    @OneToMany(mappedBy = "returnId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<MaterialReturnItems> returnItems;

    @Column(name = "sent_external_id")
    private Long sentExternalId;

    @Column(name = "external_godown_id")
    private Long externalGodownId;

    @Column(name = "type")
    private String type;

    @Column(name = "to_reach")
    private String toReach;

    @Column(name = "to_reach_id")
    private Long toReachId;

    @Column(name = "to_reach_name")
    private String toReachName;



    public MaterialReturn(MaterialIssue issue) {
        //this.id = issue.getId();
        this.requestId = issue.getRequestId();
        this.issueId = issue.getId();
        this.fromDeptId = issue.getRequestDepartment().getId();
        this.fromDeptName = issue.getRequestDepartment().getName();
        this.fromId = issue.getRequestBranch().getId();
        this.fromName = "Branch";
        this.organisation = issue.getOrganisation();
        this.issueBy = issue.getIssueBy();
        this.toDeptId = issue.getIssueDepartment().getId();
        this.toDeptName = issue.getIssueDepartment().getName();
        this.toId = issue.getIssueBranch().getId();
        this.toName = "Branch";
        this.toReach = "Branch";
        this.toReachId = issue.getIssueBranch().getId();
        this.toReachName = issue.getIssueBranch().getBranchName();
        this.requestName = issue.getRequestName();
        this.requestDescription = issue.getRequestDescription();
        this.requestDate = issue.getRequestDate();
        this.issueDate = issue.getIssueDate();
        this.trnNo = issue.getTrnNo();
    }



    public MaterialReturn(MaterialReturn materialReturn , SendExternalEntity issue) {

        DateUtil dateUtil = new DateUtil();

        this.id = materialReturn.getId();

        this.sentExternalId = issue.getMaterialFlowSendExternalId();

        this.fromDeptId = issue.getCreatedDepartmentId();

        this.fromId = issue.getCreatedBranchId();
        this.fromName = "Branch";

        User user = new User();
        user.setId(issue.getIssueById());
        this.issueBy = user;

        this.toDeptId = issue.getToDepartmentId();
        this.toId = issue.getToBranchId();

        if (issue.getToReach().equalsIgnoreCase("Vendor")) {
            this.toId = issue.getToVendorRefId();
            this.toName = issue.getToReach();
            this.toReach = issue.getToReach();
            this.toReachId = issue.getToVendorRefId();
        } else if (issue.getToReach().equalsIgnoreCase("Branch")) {
            this.toId = issue.getToBranchId();
            this.toName = issue.getToReach();
            this.toReach = issue.getToReach();
            this.toReachId = issue.getToBranchId();
        } else if (issue.getToReach().equalsIgnoreCase("Dep")) {
            this.toId = issue.getToDepartmentId();
            this.toName = issue.getToReach();
            this.toReach = issue.getToReach();
            this.toReachId = issue.getToDepartmentId();
        }

        this.requestDate = dateUtil.getLocalDateTimeToDate(issue.getCreated_date());
        this.issueDate = dateUtil.getLocalDateTimeToDate(issue.getCreated_date());
        this.dispatchDate = issue.getReferenceDate();

    }


    public MaterialReturn(SendExternalEntity issue) {

        DateUtil dateUtil = new DateUtil();

        this.sentExternalId = issue.getMaterialFlowSendExternalId();

        this.fromDeptId = issue.getCreatedDepartmentId();

        this.fromId = issue.getCreatedBranchId();
        this.fromName = "Branch";

        User user = new User();
        user.setId(issue.getIssueById());
        this.issueBy = user;

        this.toDeptId = issue.getToDepartmentId();
        this.toId = issue.getToBranchId();

        if (issue.getToReach().equalsIgnoreCase("Vendor")) {
            this.toId = issue.getToVendorRefId();
            this.toName = issue.getToReach();
            this.toReach = issue.getToReach();
            this.toReachId = issue.getToVendorRefId();
        } else if (issue.getToReach().equalsIgnoreCase("Branch")) {
            this.toId = issue.getToBranchId();
            this.toName = issue.getToReach();
            this.toReach = issue.getToReach();
            this.toReachId = issue.getToBranchId();
        } else if (issue.getToReach().equalsIgnoreCase("Dep")) {
            this.toId = issue.getToDepartmentId();
            this.toName = issue.getToReach();
            this.toReach = issue.getToReach();
            this.toReachId = issue.getToDepartmentId();
        }

        this.requestDate = dateUtil.getLocalDateTimeToDate(issue.getCreated_date());
        this.issueDate = dateUtil.getLocalDateTimeToDate(issue.getCreated_date());
        this.dispatchDate = issue.getReferenceDate();

    }



    public MaterialReturn(GoDownExternal issue) {

        DateUtil dateUtil = new DateUtil();

        this.externalGodownId = issue.getId();

        this.issueBy = issue.getIssueBy();
        this.toDeptId = issue.getIssueDepartment().getId();
        this.toDeptName = issue.getIssueDepartment().getName();

        this.toId = issue.getIssueBranch().getId();

        this.toName = "Branch";

        if(issue.getFrom() != null && issue.getFrom().equalsIgnoreCase("branch")) {

            this.fromName = "Branch";

        } else   if(issue.getFrom() != null && issue.getFrom().equalsIgnoreCase("godown")) {

            this.fromName = "Godown";
        }

        if(issue.getTo() != null && issue.getTo().equalsIgnoreCase("branch")) {

            this.toName = "Branch";
            this.toReach = "Branch";

        } else   if(issue.getTo() != null && issue.getTo().equalsIgnoreCase("godown")) {

            this.toName = "Godown";
            this.toReach = "Godown";
        }

        this.requestDate = Date.from(issue.getCreatedDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        this.issueDate = Date.from(issue.getCreatedDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        this.dispatchDate = issue.getDispatchOn();
        this.trnNo = issue.getTrnNo();
    }
}
