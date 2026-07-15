package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StringEscapeUtils;

@Getter
@Setter
@NoArgsConstructor
public class CWagesListDTO {


    private Long id;

    private String name;
    private String designation;
    private Long trnYear;
    private String materialMovement;
    private String codeNo;
    private Double cost;
    private String journal;
    private String activityBased;

    public CWagesListDTO(CWagesDTOList cWagesDTOList) {
        this.id = cWagesDTOList.getId();
        this.name = StringEscapeUtils.unescapeHtml4(cWagesDTOList.getName());
        this.designation = cWagesDTOList.getDesignation();
        this.trnYear = cWagesDTOList.getTrnYear();
        this.materialMovement =cWagesDTOList.getMaterialMovement();
        this.codeNo = cWagesDTOList.getCodeNo();
        this.cost = cWagesDTOList.getCost();
        this.journal = cWagesDTOList.getJournal();
        this.activityBased = cWagesDTOList.getActivityBased();
    }
}
