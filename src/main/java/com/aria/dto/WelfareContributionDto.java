package com.aria.dto;

import java.time.LocalDateTime;
import com.aria.model.WelfareContribution;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WelfareContributionDto {

	private Long id;

	private String fromDate;

	private String toDate;

	private String foodPercentage;

	private String uniformPercentage;

	private String transportationPercentage;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public WelfareContributionDto(WelfareContribution welfareContribution) {
		this.id = welfareContribution.getId();
		this.foodPercentage = welfareContribution.getFoodPercentage();
		this.uniformPercentage = welfareContribution.getUniformPercentage();
		this.transportationPercentage = welfareContribution.getTransportationPercentage();
		this.createdBy = welfareContribution.getCreatedBy();
		this.updatedBy = welfareContribution.getUpdatedBy();
		this.created_date = welfareContribution.getCreated_date();
		if (welfareContribution.getUpdated_date() != null) {
			this.updated_date = welfareContribution.getUpdated_date();
		} else {
			this.updated_date = welfareContribution.getCreated_date();
		}

	}
}
