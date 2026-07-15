package com.aria.dto;

import com.aria.model.ACwages;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ACwagesDTO {


    private Long id;

    private Long cwagesid;

    private String intra;

    private String interin;

    private String interout;

    private String stkin;
   
    private String stkout;

    private String ajournal;

    private String qcproce;

    private Long refid;

    private Long cby;

    private String extin;

    private String extout;

    public ACwagesDTO(ACwages aCwages ) {
        this.id = aCwages.getId();
        this.cwagesid = aCwages.getCwagesid();
        this.intra =aCwages.getIntra();
        this.interin = aCwages.getInterin();
        this.interout = aCwages.getInterout();
        this.stkin =aCwages.getStkin();
        this.stkout = aCwages.getStkout();
        this.ajournal = aCwages.getAjournal();
        this.qcproce = aCwages.getQcproce();
        this.refid = aCwages.getRefid();
        this.cby = aCwages.getCby();
        this.extin = aCwages.getExtin();
        this.extout = aCwages.getExtout();
    }
}
