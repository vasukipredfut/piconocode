package com.aria.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.AttributesAssignDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "attributes_assignments")
public class AttributesAssign {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "screen_Name")
	private String screenName;

	@Column(name = "field_Name")
	private String fieldName;

	@Column
	private String process;

	@Column(name = "task_Type")
	private String taskType;

	@Column(name = "mode_Of_Entry")
	private String modeOfEntry;
	
	@OneToOne
	@JoinColumn(name = "kpi_name_id", referencedColumnName = "id")
	private Kpiname kpiName;
	
	@OneToOne
	@JoinColumn(name = "product_master_id", referencedColumnName = "id")
//	@Where(clause = " inward = 'YES' ")
	private ProductMaster product;
	
	@OneToOne
	@JoinColumn(name = "dept_id", referencedColumnName = "id")
	private DepartmentDetails dept;



	@Column(name = "created_by",updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;
	
	/*@OneToMany(mappedBy = "attibutesAssignId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<AttributesSteps> attributesSteps;*/

	public AttributesAssign(AttributesAssignDto attributesAssign) {
		this.id = attributesAssign.getId();

		this.screenName = attributesAssign.getScreenName();

		this.fieldName = attributesAssign.getFieldName();
		this.taskType = attributesAssign.getTaskType();
		this.process = attributesAssign.getProcess();
		
		if(attributesAssign.getKpiNameId() != null){
            Kpiname tn = new Kpiname();
            tn.setId(attributesAssign.getKpiNameId());
            this.kpiName = tn;
        }
		
		if(attributesAssign.getDepartmentId() != null){
            DepartmentDetails tn = new DepartmentDetails();
            tn.setId(attributesAssign.getDepartmentId());
            this.dept = tn;
        }
		
		if(attributesAssign.getProductId() != null){
			ProductMaster tn = new ProductMaster();
            tn.setId(attributesAssign.getProductId());
            this.product = tn;
        }
		this.modeOfEntry = attributesAssign.getModeOfEntry();
		
		this.createdBy = attributesAssign.getCreatedBy();
		this.updatedBy = attributesAssign.getUpdatedBy();
		this.created_date = attributesAssign.getCreated_date();
		this.updated_date = attributesAssign.getCreated_date();
	}
}
