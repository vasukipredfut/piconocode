package com.aria.model;

import com.aria.dto.AttributesDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "attributes")
public class Attributes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;


    @OneToOne
    @JoinColumn(name = "task_name_id", referencedColumnName = "id")
    private TaskName taskNameDet;

    @OneToOne
    @JoinColumn(name = "task_category_id", referencedColumnName = "id")
    private TaskCategory taskCategory;

    @Column
    private String purpose_benefit;

    @Column
    private String workinst_saftmeasure;

    @Column(name = "disp_Order")
    private Long dispOrder;

    @Column(name = "position_Number")
    private Long positionNumber;

    @Column(name = "attribute_Type")
    private String attributeType;

    @Column(name = "input_Type")
    private String inputType;

    @Column(name = "value_Type")
    private String valueType;

    @Column
    private String direct_cummulative;

    @Column
    private String formula;

    @OneToMany(mappedBy = "attributes_id", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<AttributeFieldsProcessTypeOptionBased> processType;

    @Column(updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    public Attributes(Long taskCategoryId, Long taskNameId, Long createdBy) {

        this.attributeType = "Maintenance";
        if (taskCategoryId != null) {
            TaskCategory tn = new TaskCategory();
            tn.setId(taskCategoryId);
            this.taskCategory = tn;
        }

        if (taskNameId != null) {
            TaskName tn = new TaskName();
            tn.setId(taskNameId);
            this.taskNameDet = tn;
        }
        this.createdBy = createdBy;
        this.created_date = LocalDateTime.now();

    }

    public Attributes(AttributesDto model) {

        this.id = model.getId();
        this.name = model.getName();
        if (model.getTaskCategoryId() != null) {
            TaskCategory tn = new TaskCategory();
            tn.setId(model.getTaskCategoryId());
            this.taskCategory = tn;
        }

        if (model.getTaskNameId() != null) {
            TaskName tn = new TaskName();
            tn.setId(model.getTaskNameId());
            this.taskNameDet = tn;
        }

        this.positionNumber = model.getPositionNumber();
        this.attributeType = model.getAttributeType();
        this.inputType = model.getInputType();
        this.valueType = model.getValueType();
        this.direct_cummulative = model.getDirect_cummulative();
        this.formula = model.getFormula();

        this.purpose_benefit = model.getPurpose_benefit();
        this.workinst_saftmeasure = model.getWorkinst_saftmeasure();

        this.createdBy = model.getCreatedBy();
        this.updatedBy = model.getUpdatedBy();
        this.created_date = model.getCreated_date();
        this.updated_date = model.getUpdated_date();
    }
}
