package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import com.aria.model.Action;
import com.aria.model.No2Products;
import com.aria.model.No2ProductsPartyMapping;
import com.aria.model.No2Ratehistory;
import com.aria.model.RatehistoryGrouping;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class No2RatehistoryListDto {

	    private Long partyid;
	    
	    private String partyName;
	    
	    private String productName;
	    
	    private Long productid;
	    
	    private Long increasedecrease;
	    
	    private Double percentage;

	    private String created_date;

	    private Long created_by;
	    
	    private Double newamount;
	    
	    private Double oldamount;

	    private Long lot;




	public No2RatehistoryListDto(RatehistoryGrouping model) {
		DateUtil dateUtil = new DateUtil();
		this.partyid=model.getPartyId();
		this.partyName=model.getPartyName();
		this.productid=model.getProductId();
		this.productName=model.getProductName();
		this.newamount=model.getAmount();
		this.oldamount=model.getOldAmount();
		this.lot=model.getLot();
		this.increasedecrease = model.getIncDec();
		this.created_date = dateUtil.getLocalDateTimeToDateString(model.getcreatedDate());
	
	}
}
