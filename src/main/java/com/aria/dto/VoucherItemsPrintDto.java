package com.aria.dto;

import org.apache.commons.text.StringEscapeUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VoucherItemsPrintDto {
	   private Double unitRate;
	    private Double lineTotal;
	    private Long quantity;
	    private String secName;
	    private Long refId;
		public VoucherItemsPrintDto(VoucherItemsPrint voucherItemsPrint) {
			
			this.unitRate = voucherItemsPrint.getUnitRate();
			this.lineTotal = voucherItemsPrint.getLineTotal();
			this.quantity = voucherItemsPrint.getQuantity();
			this.secName = StringEscapeUtils.unescapeHtml4(voucherItemsPrint.getSecName());
			this.refId=voucherItemsPrint.getRefId();
		}
	    
	    
}
