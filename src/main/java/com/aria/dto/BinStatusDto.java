package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.servlet.support.BindStatus;

import com.aria.model.Action;
import com.aria.model.BinStatus;
import com.aria.utils.CustomConfiguration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BinStatusDto {

	private Long binName;

	private Boolean locked;

	private String locktype;


	private Long updatedBy;


	private LocalDateTime updated_date;

	public BinStatusDto(BinStatus model) {
		this.binName = model.getBinName();
		this.locked=model.getLocked();
		this.locktype=model.getLocktype();
		this.updated_date=model.getUpdated_date();
		this.updatedBy=model.getUpdatedBy();
	}
}
