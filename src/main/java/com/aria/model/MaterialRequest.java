
package com.aria.model;

import com.aria.dto.MaterialRequestDto;


import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import jakarta.persistence.*;

import org.hibernate.annotations.Where;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "material_request")
public class MaterialRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "dept_id", referencedColumnName = "id")
    private DepartmentDetails department;

    @OneToOne
    @JoinColumn(name = "branch_id", referencedColumnName = "id")
    private Branch branch;

    @OneToOne
    @JoinColumn(name = "org_id", referencedColumnName = "id")
    private Organisation organisation;

    @Column(name = "request_name")
    private String requestName;

    @Column(name = "request_Description")
    private String requestDescription;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User requestBy;

    @Column(name = "request_Date")
    @Temporal(TemporalType.DATE)
    private Date requestDate;

    @Column
    private String status;

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


    @OneToMany(mappedBy = "requestId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Where(clause = "active=0")
    private List<RequestItem> requestItems;


    public MaterialRequest(MaterialRequestDto materialRequest) {
        this.id = materialRequest.getId();
        if (materialRequest.getRequestById() != null) {
            User user = new User();
            user.setId(materialRequest.getRequestById());
            this.requestBy = user;
        }
        if (materialRequest.getRequestFromBranchId() != null) {
            Branch branch = new Branch();
            branch.setId(materialRequest.getRequestFromBranchId());
            this.branch = branch;
        }
        if (materialRequest.getRequestFromDepartmentId() != null) {
            DepartmentDetails departmentDetails = new DepartmentDetails();
            departmentDetails.setId(materialRequest.getRequestFromDepartmentId());
            this.department = departmentDetails;
        }
        if (materialRequest.getRequestFromFirmId() != null) {
            Organisation organisation = new Organisation();
            organisation.setId(materialRequest.getRequestFromFirmId());
            this.organisation = organisation;
        }
        this.requestDescription = materialRequest.getRequestDescription();
        this.requestName = materialRequest.getRequestDescription();
        this.status = materialRequest.getStatus();
        this.active = materialRequest.getActive();
        this.createdBy = materialRequest.getCreatedBy();
        this.updatedBy = materialRequest.getUpdatedBy();
        this.created_date = materialRequest.getCreated_date();
        this.updated_date = materialRequest.getUpdated_date();
    }


}