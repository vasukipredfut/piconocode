package com.aria.dto;

import com.aria.model.SplitMaster;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SplitMasterDto {

    private Long id;

    
    private Long pcode;

    
    private double qty;

    
    private double lot;

    
    private Long spcode;

    
    private double sqty;

    
    private double slot;

    
    private String type;

    
    private String stype;

    
    private Long splitid;

    
    private double sqty_old;

    public SplitMasterDto(SplitMaster splitMaster) {
        this.id = splitMaster.getId();
        this.pcode = splitMaster.getPcode();
        this.qty = splitMaster.getQty();
        this.lot = splitMaster.getLot();
        this.spcode = splitMaster.getSpcode();
        this.sqty = splitMaster.getSqty();
        this.slot = splitMaster.getSlot();
        this.type = splitMaster.getType();
        this.stype = splitMaster.getStype();
        this.splitid = splitMaster.getSplitid();
        this.sqty_old = splitMaster.getSqty_old();
    }
}
