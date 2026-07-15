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
@Table(name = "purchase_quality_inspection_spec_description_detailspur")
public class QualityInspectionSpec {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long purchase_quality_inspection_score_id;
	
	@Column(name="bill_item_ref_id")
	private Long billItemRefId;
	
	@Column(name="spec_id")
	private Long specId;
	
	@Column(name="item_ref_id")
	private Long itemRefId;
	
	@Column(name="description")
	private String description;
	
	@Column(name="parameter_score")
	private Integer parameterScore;
	
	@Column(name="active_flag")
	private Integer activeFlag;
	
	
}
