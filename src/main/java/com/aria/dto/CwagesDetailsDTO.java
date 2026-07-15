package com.aria.dto;

import com.aria.model.CwagesDetails;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;

@Getter
@Setter
@NoArgsConstructor
public class CwagesDetailsDTO {


    private Long id;

   
    private Long trnid;

   
    private String trnno;
   
    private String reftrnno;
   
    private Long reftrnid;
   
    private String reftrntable;

   
    private Long  cwagesid;

   
    private Double  urate;

   
    private Double  qty;

   
    private Double  amt;
   
    private Long  luzone;
   
    private Long  desigid;
   
    private String  usecid;

    public CwagesDetailsDTO(CwagesDetails cwages) {
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

}
