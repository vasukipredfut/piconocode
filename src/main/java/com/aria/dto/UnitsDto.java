package com.aria.dto;

import com.aria.model.Units;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UnitsDto {

    private Long id;

    
    private String name;

    
    private String displayName;

    
    private String ewayBillFlag;


    public UnitsDto(Units units) {
        this.id = units.getId();
        this.name = units.getName();
        this.displayName = units.getDisplayName();
        this.ewayBillFlag = units.getEwayBillFlag();
    }
}
