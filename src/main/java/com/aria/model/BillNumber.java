
package com.aria.model;

import com.aria.dto.BillNumberDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "bill_number")
public class BillNumber {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String billNo;

	public BillNumber(BillNumberDto billNumber) {
		this.id = billNumber.getId();
		this.billNo = billNumber.getBillNo();
	}
}
