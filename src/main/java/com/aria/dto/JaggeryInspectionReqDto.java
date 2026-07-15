package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class JaggeryInspectionReqDto {

	private Long billInwardId;
	private JaggeryInspectionDto inspection;
	private JaggeryAttributesValuesDto attributesValues;
	private JaggeryInspectionScoreDto inspectionScore;

}
