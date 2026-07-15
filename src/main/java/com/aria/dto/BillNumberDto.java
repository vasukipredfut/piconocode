
package com.aria.dto;

import com.aria.model.BillNumber;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BillNumberDto {

	private Long id;

	private String billNo;

	public BillNumberDto(BillNumber billNumber) {
		this.id = billNumber.getId();

		this.billNo = billNumber.getBillNo();
	}
}
