package com.aria.dto;

import com.aria.model.VoucherSharedMemberDetails;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VoucherSharedMemberDetailsDto {	
	    private Long id;
	    private String trnno;	  
	    private Long bid;	   
	    private String batchno;	    
	    private Double urate; 	      
	    private Long conid;
	    private String contype;
	    private String conname;
	    private Long condept;
	    private Long conbranch;
		public VoucherSharedMemberDetailsDto(VoucherSharedMemberDetails voucherSharedMemberDetails) {
			
			this.id = voucherSharedMemberDetails.getId();
			this.trnno = voucherSharedMemberDetails.getTrnno();
			this.bid = voucherSharedMemberDetails.getBid();
			this.batchno = voucherSharedMemberDetails.getBatchno();
			this.urate = voucherSharedMemberDetails.getUrate();
			this.conid = voucherSharedMemberDetails.getConid();
			this.contype = voucherSharedMemberDetails.getContype();
			this.conname = voucherSharedMemberDetails.getConname();
			this.condept = voucherSharedMemberDetails.getCondept();
			this.conbranch = voucherSharedMemberDetails.getConbranch();
		}
	  
	    
	


}
