package com.aria.dto;

import java.util.Date;

import org.apache.commons.text.StringEscapeUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VoucherEntryListDto {
	
	    private Long id;
	    private String vouNo;
	    private String vouType;
	    private Date vouDate;
	    private String status;
	    private String cVouName;
	    private String sVouName;
	    private String grpNo;
		public VoucherEntryListDto(VoucherEntryList VoucherEntryList) {
			
			this.id = VoucherEntryList.getId();
			this.vouNo = VoucherEntryList.getVouNo();
			this.vouType = VoucherEntryList.getVouType();
			this.vouDate = VoucherEntryList.getVouDate();
			this.status = VoucherEntryList.getStatus();
			this.cVouName = VoucherEntryList.getCVouName();
			this.sVouName =StringEscapeUtils.unescapeHtml4( VoucherEntryList.getSVouName());
			this.grpNo=VoucherEntryList.getGrpNo();
		}
	    
	    
}
