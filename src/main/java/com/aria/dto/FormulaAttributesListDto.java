package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FormulaAttributesListDto {
    
    
    private Long id;

    private Long attId;
    private String attName;

    private String minvalue;
   
    private String maxValue;
   
    private String inputType;
   
    private String formula;

    public FormulaAttributesListDto(FormulaAttributesList formulaAttributesList) {
        this.id = formulaAttributesList.getId();
        this.attId = formulaAttributesList.getAttId();
        this.attName = formulaAttributesList.getAttName();
        this.minvalue = formulaAttributesList.getMinvalue();
        this.maxValue = formulaAttributesList.getMaxValue();
        this.inputType = formulaAttributesList.getInputType();
        this.formula = formulaAttributesList.getFormula();
    }
}
