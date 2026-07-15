package com.aria.model;

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
@Table(name = "with_in_branch_receive")
public class WithInBranchReceive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_Id")
    private Long requestId;

    @Column(name = "issue_Id")
    private Long issueId;

    @OneToOne
    @JoinColumn(name = "request_dept_id", referencedColumnName = "id")
    private DepartmentDetails requestDepartment;

    @OneToOne
    @JoinColumn(name = "request_branch_id", referencedColumnName = "id")
    private Branch requestBranch;

    @OneToOne
    @JoinColumn(name = "request_org_id", referencedColumnName = "id" , updatable = false)
    private Organisation requestOrg;

    @OneToOne
    @JoinColumn(name = "issue_dept_id", referencedColumnName = "id")
    private DepartmentDetails issueDepartment;

    @OneToOne
    @JoinColumn(name = "issue_branch_id", referencedColumnName = "id")
    private Branch issueBranch;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User issueBy;

    @OneToOne
    @JoinColumn(name = "issue_org_id", referencedColumnName = "id")
    private Organisation issueOrg;

    @Column(name = "request_date", updatable = false)
    @Temporal(TemporalType.DATE)
    private Date requestDate;

    @Column(name = "issue_date")
    @Temporal(TemporalType.DATE)
    private Date issueDate;

    @Column(name = "entry_date")
    @Temporal(TemporalType.DATE)
    private Date entryDate;

    @Column(name = "received_date")
    @Temporal(TemporalType.DATE)
    private Date receivedDate;

    @OneToOne
    @JoinColumn(name = "received_user_id", referencedColumnName = "id")
    private User receivedBy;

    @Column
    private String status;

    @Column(name = "trn_no")
    private String trnNo;
    @Column
    private int active;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;


    @OneToMany(mappedBy = "withinId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<WithInBranchReceiveItems> withInBranchReceiveItems;
}
