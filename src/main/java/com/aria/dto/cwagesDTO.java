package com.aria.dto;

import org.apache.commons.text.StringEscapeUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class cwagesDTO {

	private Long cwages_id;
    private String cwages_name;
    private String codeNo;
    private Double amount;
    private Long salStrucId;
    private String designationName;
    
    

	public Long getCwages_id() {
		return cwages_id;
	}



	public String getCwages_name() {
		return cwages_name;
	}



	public String getCodeNo() {
		return codeNo;
	}



	public Double getAmount() {
		return amount;
	}


	public Long getSalStrucId() {
		return salStrucId;
	} 
	

	public String getDesignationName() {
		return designationName;
	}



	public cwagesDTO(cwagesList cwages) {
        this.cwages_id = cwages.getCwagesId();
        this.cwages_name = StringEscapeUtils.unescapeHtml4(cwages.getCwagesName());
        this.codeNo = cwages.getCodeNo();
        this.amount = cwages.getAmount();
        this.salStrucId = cwages.getSalStrucId();
        this.designationName =cwages.getDesignationName();
    }	
    
}
