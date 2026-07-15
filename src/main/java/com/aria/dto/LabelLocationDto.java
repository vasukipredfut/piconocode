package com.aria.dto;

import com.aria.model.LabelLocation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LabelLocationDto {

	private Long id;

	private String name;

	private String type;

	private String ceilingLimit;

	private String ip;

	private Long locationId;

	private Long createdBy;

	public LabelLocationDto(LabelLocation model) {
		this.id = model.getId();
		this.name = model.getName();
		this.type = model.getType();
		this.ceilingLimit = model.getCeilingLimit();
		this.ip = model.getIp();
		if (model.getCreatedBy() != null) {
			this.createdBy = model.getCreatedBy().getId();
		}
		this.locationId = model.getLocationId();
	}
}
