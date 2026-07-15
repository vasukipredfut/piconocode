package com.aria.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.aria.dto.BinStatusDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "binstatus")
public class BinStatus {

	@Id
	@Column(name = "bin_name")
	private Long binName;

	@Column
	private Boolean locked;

	@Column
	private String locktype;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column
	private LocalDateTime updated_date;

	public BinStatus(BinStatusDto model) {

		this.binName = model.getBinName();
		this.locked=model.getLocked();
		this.locktype=model.getLocktype();
		this.updated_date=model.getUpdated_date();
		this.updatedBy=model.getUpdatedBy();
	}
}
