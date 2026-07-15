package com.aria.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WindmillOtherChargestrndetails {
	private Long windmillid;
	private Double amount;
	private Long otherchargesid;
	private LocalDate trndate;
	public WindmillOtherChargestrndetails(WindmillOtherChargesProjection request) {
		this.windmillid = request.getWindmillid();
		this.amount = request.getAmount();
		this.otherchargesid = request.getOtherchargesid();
		this.trndate = request.getTrndate();
	}
	
}
