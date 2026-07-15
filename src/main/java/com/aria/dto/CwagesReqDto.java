package com.aria.dto;


import com.aria.model.Cwages;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StringEscapeUtils;

@Getter
@Setter
@NoArgsConstructor
public class CwagesReqDto {


    private Long id;

    private String codeno;

    private String sno;

    private Long salstruid;

    private Long trnyear;

    private String name;

    private Double amount;

    private String journal;

    private String mvmt;

    private String tport;

    private String usecid;

    private Long type;

    private Long slab;

    private String activity;

    public CwagesReqDto(Cwages cwages) {
        this.id = cwages.getId();
        this.codeno = cwages.getCodeno();
        this.sno = cwages.getSno();
        this.salstruid = cwages.getSalstruid();
        this.trnyear = cwages.getTrnyear();
        this.name = StringEscapeUtils.unescapeHtml4(cwages.getName());
        this.amount = cwages.getAmount();
        this.journal = cwages.getJournal();
        this.mvmt = cwages.getMvmt();
        this.tport = cwages.getTport();
        this.usecid = cwages.getUsecid();
        this.type = cwages.getType();
        this.slab = cwages.getSlab();
        this.activity = cwages.getActivity();
    }
}
