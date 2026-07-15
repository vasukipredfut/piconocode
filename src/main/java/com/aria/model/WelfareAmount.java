package com.aria.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.aria.dto.WelfareAmountDto;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "welfare_amount")
public class  WelfareAmount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "welfare_category")
    private String welfareCategory;

	@Column(name = "welfare_name")
	private String welfareName;

	@Column(name = "from_Date")
	private Date fromDate;

	@Column(name = "to_date")
	private Date toDate;
	
    @OneToOne
    @JoinColumn(name = "productionentryid", referencedColumnName = "id",nullable = true)
    private ProductionEntry productionEntry;
	
	@OneToOne
    @JoinColumn(name = "welfareId", referencedColumnName = "id",nullable = true)
    private WelfareContribution welfareContribution;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;


	public WelfareAmount(WelfareAmountDto welfareAmount) {
		this.id = welfareAmount.getId();
		this.welfareCategory =welfareAmount.getWelfareCategory();
		this.welfareName =welfareAmount.getWelfareName();
		if(welfareAmount.getProductionEntry() != null){
			ProductionEntry productionEntry = new ProductionEntry();
			productionEntry.setId(welfareAmount.getProductionEntry());
            this.productionEntry = productionEntry;
        }
		if(welfareAmount.getWelfareContribution() != null){
			WelfareContribution welfareContribution = new WelfareContribution();
			welfareContribution.setId(welfareAmount.getWelfareContribution());
            this.welfareContribution = welfareContribution;
        }
		//this.payableAmount = welfareAmount.getPayableAmount();
		//this.employeePayableAmount = welfareAmount.getEmployeePayableAmount();
		this.createdBy = welfareAmount.getCreatedBy();
		this.updatedBy = welfareAmount.getUpdatedBy();
		this.created_date = welfareAmount.getCreated_date();
		this.updated_date = welfareAmount.getUpdated_date();
		
	}
}
