package com.aria.dto;

import java.time.LocalDateTime;
import java.util.Date;

import com.aria.model.SupplierEvaluationAttributes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SupplierEvaluationAttributesDTO {

	private Long id;
	private Double scoreFrom;
	private Double scoreTo;
	private String scoreType;
	private String comment;
	private Integer colors;
	private Integer updatedBy;
	private LocalDateTime lastUpdate;
	private String status;
	private Double maximum;

	public SupplierEvaluationAttributesDTO(SupplierEvaluationAttributes supplierEvaluationAttributes) {
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
