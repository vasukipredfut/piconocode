package com.aria.dto;



import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LotQtyDto {
		private String bag;
	    private Double value;
	    
	    public LotQtyDto(StockQtyWithLot dto) {
	    	this.bag=dto.getLot();
	    	this.value=dto.getQty();
	    }
}
