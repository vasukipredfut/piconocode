package com.aria.model;

import com.aria.dto.ACwagesDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "acwages")
public class ACwages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long cwagesid;

    @Column
    private String intra;

    @Column
    private String interin;

    @Column
    private String interout;

    @Column
    private String stkin;

    @Column
    private String stkout;

    @Column
    private String ajournal;

    @Column
    private String qcproce;

    @Column
    private Long refid;

    @Column
    private Long cby;

    @Column
    private String extin;

    @Column
    private String extout;

    public ACwages(ACwagesDTO aCwages ) {
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
