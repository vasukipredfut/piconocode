package com.aria.dto;

import java.time.LocalDateTime;

public interface ExpenseCategoryListDto {

	Long getId();
	String getName();
	String getDescription();
	Boolean getStatus();
	Long getCreatedBy();
	Long getUpdatedBy();
	LocalDateTime getCreatedDate();
	LocalDateTime getUpdatedDate();
}
