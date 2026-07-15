package com.aria.dto;

import com.aria.model.QtyMain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QtyMainDto {
	
	private Long id;
	private String name;
	
	public QtyMainDto(QtyMain model) {		
		this.id = model.getId();
		this.name = model.getName();
	}
	
	

}
