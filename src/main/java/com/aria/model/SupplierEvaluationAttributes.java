package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


import com.aria.dto.PanelDto;
import com.aria.dto.SpecificationDto;
import com.aria.dto.SupplierEvaluationAttributesDTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "supplierevaluationattributes")
public class SupplierEvaluationAttributes {

	 	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(name = "scorefrom")
	    private Double scoreFrom;

	    @Column(name = "scoreto")
	    private Double scoreTo;

	    @Column(name = "scoretype")
	    private String scoreType;

	    @Column(name = "comment")
	    private String comment;

	    @Column(name = "colors")
	    private Integer colors;

	    @Column(name = "updatedby")
	    private Integer updatedBy;

	    @Column(name = "lastupdate")
	    private LocalDateTime lastUpdate;

	    @Column(name = "status")
	    private String status;

	    @Column(name = "maximum")
	    private Double maximum;


		public SupplierEvaluationAttributes(SupplierEvaluationAttributesDTO supplierEvaluationAttributes) {
			this.id = supplierEvaluationAttributes.getId();
			this.scoreFrom = supplierEvaluationAttributes.getScoreFrom();
			this.scoreTo = supplierEvaluationAttributes.getScoreTo();
			this.scoreType = supplierEvaluationAttributes.getScoreType();
			this.comment = supplierEvaluationAttributes.getComment();
			this.colors = supplierEvaluationAttributes.getColors();
			this.updatedBy = supplierEvaluationAttributes.getUpdatedBy();
			this.lastUpdate = supplierEvaluationAttributes.getLastUpdate();
			this.status = supplierEvaluationAttributes.getStatus();
			this.maximum = supplierEvaluationAttributes.getMaximum();
		}
	
	
}
