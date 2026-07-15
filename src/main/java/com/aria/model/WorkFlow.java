package com.aria.model;

import com.aria.dto.WorkFlowDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "work_flow")
public class WorkFlow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_flow_name")
    private String workFlowName;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;


    public WorkFlow(WorkFlowDto workFlow) {

        this.id = workFlow.getId();

        this.workFlowName = workFlow.getWorkFlowName();

        this.createdBy = workFlow.getCreatedBy();

        this.updatedBy = workFlow.getUpdatedBy();

        this.created_date = workFlow.getCreated_date();

        this.updated_date = workFlow.getUpdated_date();
    }
}
