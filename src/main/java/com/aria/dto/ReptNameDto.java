package com.aria.dto;

import jakarta.persistence.Column;

import com.aria.model.ReptName;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReptNameDto {
	private Long id;   
    private String name;    
    private Long activeFlag;
    
    public ReptNameDto (ReptName model)
    {
    	this.id=model.getId();
    	this.name=model.getName();
    	this.activeFlag=model.getActiveFlag();
    			
    }

}
