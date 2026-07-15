package com.aria.dto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.model.WelfareItems;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WelfareItemsDto {

	private Long id;

	private String welfareCategory;

	private String date;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	private Map<String, Object> welfareItemsValue;

	public WelfareItemsDto(WelfareItems welfareItems) {
		this.id = welfareItems.getId();
		this.welfareCategory = welfareItems.getWelfareCategory();
		this.createdBy = welfareItems.getCreatedBy();
		this.updatedBy = welfareItems.getUpdatedBy();
		this.created_date = welfareItems.getCreated_date();
		this.updated_date = welfareItems.getUpdated_date();
		if (welfareItems.getUpdated_date() != null) {
			this.updated_date = welfareItems.getUpdated_date();
		} else {
			this.updated_date = welfareItems.getCreated_date();
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			this.welfareItemsValue = mapper.readValue(welfareItems.getWelfareItemsValue(), HashMap.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
