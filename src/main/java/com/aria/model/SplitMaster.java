package com.aria.model;

import com.aria.dto.SplitMasterDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "split_master")
public class SplitMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long pcode;

    @Column
    private double qty;

    @Column
    private double lot;

    @Column
    private Long spcode;

    @Column
    private double sqty;

    @Column
    private double slot;

    @Column
    private String type;

    @Column
    private String stype;

    @Column
    private Long splitid;

    @Column
    private double sqty_old;


    public SplitMaster(SplitMasterDto splitMaster) {
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
