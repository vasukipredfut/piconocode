package com.aria.dto;

import com.aria.model.LabelBranches;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LabelBranchesDto {

	private Long id;

	private String name;

	private Long branchId;

	public LabelBranchesDto(LabelBranches model) {
		this.id = model.getId();
		this.name = model.getName();
		this.branchId = model.getBranchId();
	}

}
