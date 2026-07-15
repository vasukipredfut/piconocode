package com.aria.model;

import com.aria.dto.BranchDto;
import com.aria.dto.MachineClassificationDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "machine_classification")
public class MachineClassification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    public MachineClassification(MachineClassificationDto field) {
    	 this.id = field.getId();
         this.name = field.getName();
         this.createdBy = field.getCreatedBy();
 		this.created_date = field.getCreated_date();
 		this.updatedBy = field.getUpdatedBy();
 		this.updated_date = field.getUpdated_date();
    }
}
