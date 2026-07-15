package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StringEscapeUtils;

@Getter
@Setter
@NoArgsConstructor
public class CwagesStructureListDTO {


    private Long id;

    private String codeNo;
    private String designation;

    private Long salStructId;

    private Long trnYear;

    private String name;

    private Double amount;

    private String journal;

    private String materialMovement;

    private String transport;

    private String userId;

    private Long type;

    private Long slab;

    private String activity;

    public CwagesStructureListDTO(CwagesStructureList cwagesStructureList) {
        this.id = cwagesStructureList.getId();
        this.codeNo = cwagesStructureList.getCodeNo();
        this.designation = cwagesStructureList.getDesignation();
        this.salStructId = cwagesStructureList.getSalStructId();
        this.trnYear = cwagesStructureList.getTrnYear();
        this.name = StringEscapeUtils.unescapeHtml4(cwagesStructureList.getName());
        this.amount = cwagesStructureList.getAmount();
        this.journal = cwagesStructureList.getJournal();
        this.materialMovement = cwagesStructureList.getMaterialMovement();
        this.transport = cwagesStructureList.getTransport();
        this.userId = cwagesStructureList.getUserId();
        this.type = cwagesStructureList.getType();
        this.slab = cwagesStructureList.getSlab();
        this.activity = cwagesStructureList.getActivity();
    }
}
