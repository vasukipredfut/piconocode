package com.aria.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessVoucherListDto {

	private Long id;
	private Long voucherno;
	private Long month;
	private Long year;
	private String branch;
	private String company;
	private String status;
	public MessVoucherListDto(MessVoucherList requet) {
		super();
		this.id=requet.getId();
		this.voucherno = requet.getVoucherno();
		this.month = requet.getMonth();
		this.year = requet.getYear();
		this.branch = requet.getBranch();
		this.company = requet.getCompany();
		this.status = requet.getStatus();
	}

}
