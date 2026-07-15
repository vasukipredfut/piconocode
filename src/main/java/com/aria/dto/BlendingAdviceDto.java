package com.aria.dto;

import com.aria.model.BlendingAdvice;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class BlendingAdviceDto {

	private Long id;

	private Long journalId;

	private Long productId;

	private String fromDate;

	private String toDate;

	private String status;

	private Double perKalasalQty;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	private String previousCas;

	public BlendingAdviceDto(BlendingAdvice blendingAdvice) {
		DateUtil dateUtil = new DateUtil();
		this.id = blendingAdvice.getId();
		this.journalId = blendingAdvice.getJournalId();
		this.productId = blendingAdvice.getProductId();
		if (blendingAdvice.getFromDate() != null) {
			this.fromDate = dateUtil.getDateToStringDDMMYYYY(blendingAdvice.getFromDate());
		}
		if (blendingAdvice.getToDate() != null) {
			this.toDate = dateUtil.getDateToStringDDMMYYYY(blendingAdvice.getToDate());
		}
		this.status = blendingAdvice.getStatus();
		this.perKalasalQty = blendingAdvice.getPer_Kalasal_Qty();
		this.created_date = blendingAdvice.getCreated_date();
		this.updated_date = blendingAdvice.getUpdated_date();
		this.previousCas = blendingAdvice.getPrevious_cas();
	}
}
