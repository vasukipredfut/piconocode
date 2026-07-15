package com.aria.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.aria.dto.ActionDto;
import com.aria.utils.CustomConfiguration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "actions")
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "action_config_id", referencedColumnName = "id", nullable = false)
    private ActionConfiguration actionConfig;

//	@Column
//	private Long taskId;

    @Column(name = "task_description")
    private String taskDescription;

    @Column(name = "unique_ref")
    private String uniqueRef;

    // unique refid is added for  find particular blending advice cas group completed status update
    @Column(name = "unique_ref_id")
    private Long uniqueRefId;

    @Column(name = "unique_ref_description")
    private String uniqueRefDescription;

    @Column(name = "ref_type")
    private String refType;

    @Column
    private Long quantity;

    @Column
    private String status;

    // Hibernate 6 native JSON mapping (replaces hibernate-types-52's @Type("jsonb")).
    // MariaDB stores as JSON; Hibernate handles the serialization via @JdbcTypeCode.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_configuration",columnDefinition = "JSON")
    private CustomConfiguration customConfiguration;

    @OneToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id", nullable = false)
    private User owner;

    @OneToOne
    @JoinColumn(name = "department_id", referencedColumnName = "id", nullable = false)
    private DepartmentDetails departmentDetails;

    @OneToOne
    @JoinColumn(name = "branch_id", referencedColumnName = "id", nullable = false)
    private Branch branch;

    @Column( name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    public Action(ActionDto model) {

        this.id = model.getId();

        if (model.getDeptID() != null) {
            DepartmentDetails departmentDetails = new DepartmentDetails();
            departmentDetails.setId(model.getDeptID());
            this.departmentDetails = departmentDetails;
        }
        if (model.getBranchId() != null) {
            Branch branch = new Branch();
            branch.setId(model.getBranchId());
            this.branch = branch;
        }

        if (model.getOwnerId() != null) {
            User user = new User();
            user.setId(model.getOwnerId());
            this.owner = user;
        }

        if (model.getActionConfigId() != null) {
            ActionConfiguration ac = new ActionConfiguration();
            ac.setId(model.getActionConfigId());
            this.actionConfig = ac;
        }

//		this.taskId = model.getTaskId();
        this.taskDescription = model.getTaskDescription();
        this.uniqueRef = model.getUniqueRef();
        this.uniqueRefId = model.getUniqueRefId();
        this.uniqueRefDescription = model.getUniqueRefDescription();
        this.quantity = model.getQuantity();
        this.status = model.getStatus();
        this.refType = model.getRefType();

        this.createdBy = model.getCreatedBy();
        this.updatedBy = model.getUpdatedBy();
        this.created_date = model.getCreated_date();
        this.updated_date = model.getUpdated_date();
    }
}
