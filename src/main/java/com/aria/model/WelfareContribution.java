
package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.aria.dto.WelfareContributionDto;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "welfare_contribution ")
public class WelfareContribution {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "from_Date")
	private Date fromDate;

	@Column(name = "to_Date")
	private Date toDate;

	@Column(name = "food_Percentage")
	private String foodPercentage;

	@Column(name = "uniform_percentage")
	private String uniformPercentage;

	@Column(name = "transportation_Percentage")
	private String transportationPercentage;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;


	public WelfareContribution(WelfareContributionDto welfareContribution) {
		this.id = welfareContribution.getId();
		this.foodPercentage = welfareContribution.getFoodPercentage();
		this.uniformPercentage = welfareContribution.getUniformPercentage();
		this.transportationPercentage = welfareContribution.getTransportationPercentage();
		this.createdBy = welfareContribution.getCreatedBy();
		this.updatedBy = welfareContribution.getUpdatedBy();
		this.created_date = welfareContribution.getCreated_date();
		this.updated_date = welfareContribution.getUpdated_date();

	}
}
