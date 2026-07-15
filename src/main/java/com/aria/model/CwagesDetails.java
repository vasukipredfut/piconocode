package com.aria.model;


import com.aria.dto.CwagesDetailsDTO;
import com.aria.dto.CwagesDetailsReqDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "cwagesdetail")
public class CwagesDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long trnid; 
    
    @Column
    private String trnno;
    @Column
    private String reftrnno;
    @Column
    private Long reftrnid;
    @Column
    private String reftrntable;

    @Column
    private Long  cwagesid;

    @Column
    private Double  urate;

    @Column
    private Double  qty;

    @Column
    private Double  amt;
    @Column
    private Long  luzone;
    @Column
    private Long  desigid;
    @Column
    private String  usecid;

    public CwagesDetails(CwagesDetailsDTO cwages) {
        this.id = cwages.getId();
        this.trnid = cwages.getTrnid();
        this.trnno = cwages.getTrnno();
        this.reftrnno = cwages.getReftrnno();
        this.reftrnid = cwages.getReftrnid();
        this.reftrntable = cwages.getReftrntable();
        this.cwagesid = cwages.getCwagesid();
        this.urate = cwages.getUrate();
        this.qty = cwages.getQty();
        this.amt = cwages.getAmt();
        this.luzone = cwages.getLuzone();
        this.desigid = cwages.getDesigid();
        this.usecid = cwages.getUsecid();
    }

    public CwagesDetails(CwagesSUM cwagesSUM, CwagesDetailsReqDTO cwages) {

        this.trnid = cwagesSUM.getId();
        this.trnno = cwagesSUM.getTrnno();
        this.reftrnno = cwagesSUM.getReftrnno();
        this.reftrnid = cwagesSUM.getReftrnid();
        this.reftrntable = cwagesSUM.getReftrntable();
        this.cwagesid = cwages.getCwagesId();
        this.urate = cwages.getUnitRate();
        this.qty = cwages.getQty();
        this.amt = cwages.getAmt();
        this.desigid = cwages.getSalStructId();
    }
}
