package com.aria.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VoucherGrpListDto {
	 private Long id;
	    private String grpNo;
	    private LocalDate fmDate;
	    private LocalDate toDate;
	    private String status;
	    private String bName;
	    private String dName;
	    private String cName;
		public VoucherGrpListDto(VoucherGrpList voucherGrpList) {
		
			this.id = voucherGrpList.getId();
			this.grpNo = voucherGrpList.getGrpNo();
			this.fmDate = voucherGrpList.getFmDate();
			this.toDate = voucherGrpList.getToDate();
			this.status = voucherGrpList.getStatus();
			this.bName = voucherGrpList.getBName();
			this.dName = voucherGrpList.getDName();
			this.cName = voucherGrpList.getCName();
		}
	    
	    

}
