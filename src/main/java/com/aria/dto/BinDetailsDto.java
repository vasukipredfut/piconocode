package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.BinDetails;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class BinDetailsDto {

	private Long id;

	private Long binid;

	private String batchno;

	private Long stock;

	private String taken;

	private int active;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public BinDetailsDto(BinDetails binDetails) {
		this.id = binDetails.getId();
		this.binid = binDetails.getBinid();
		this.batchno = binDetails.getBatchno();
		this.updatedBy = binDetails.getUpdatedBy();
		if (binDetails.getUpdated_date() != null) {
			this.updated_date = binDetails.getUpdated_date();
		}
	}
}