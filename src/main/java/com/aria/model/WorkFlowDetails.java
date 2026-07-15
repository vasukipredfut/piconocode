package com.aria.model;

import com.aria.dto.WorkFlowDetailsDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "work_flow_details")
public class WorkFlowDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "flow_id", referencedColumnName = "id")
    private WorkFlow workFlow;

    @Column(name = "flow_name")
    private String flowName;

    @Column
    private int main ;

    @Column(name = "parent_flow_id")
    private Long parentFlowId;

    @Column(name = "child_flow_id")
    private Long childFlowId;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;


    public WorkFlowDetails(WorkFlowDetailsDto workFlowDetails) {
        this.id = workFlowDetails.getId();
        if (workFlowDetails.getWorkFlowId() != null) {
            WorkFlow workFlow = new WorkFlow();
            workFlow.setId(workFlowDetails.getWorkFlowId());
            this.workFlow = workFlow;
        }
        this.createdBy = workFlowDetails.getCreatedBy();
        this.updatedBy = workFlowDetails.getUpdatedBy();
        this.created_date = workFlowDetails.getCreated_date();
        this.updated_date = workFlowDetails.getUpdated_date();

    }
}
