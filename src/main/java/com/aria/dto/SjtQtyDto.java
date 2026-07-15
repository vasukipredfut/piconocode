package com.aria.dto;


import com.aria.model.SjtQty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SjtQtyDto {


    private Long sjid;

    
    private String sjname;
    
    private Long ina;
    
    private Long inb;
    
    private Long inc;
    
    private Long ind;
    
    private Long outa;
    
    private Long outb;
    
    private Long outc;
    
    private Long outd;
    
    private Long oute;
    
    private Long outf;
    
    private Long outg;
    
    private Long outh;
    
    private Long outi;
    
    private Long outj;
    
    private Long outk;
    
    private Long outl;
    
    private Long outm;
    
    private Long outn;
    
    private Long outo;
    
    private Long outp;
    
    private Long outq;
    
    private Long outr;
    
    private Long outs;
    
    private Double conversion;
    
    private Long rid;
    
    private Double conversion_old;
    
    private Long outt;
    
    private Long outu;
    
    private Long outv;
    
    private Long ine;
    
    private Long inf;
    
    private Long ing;
    
    private Long inh;
    
    private Long ini;
    
    private Long inj;
    
    private Long ink;

    public SjtQtyDto(SjtQty sjtQty) {
        this.sjid = sjtQty.getSjid();
        this.sjname = sjtQty.getSjname();
        this.ina = sjtQty.getIna();
        this.inb = sjtQty.getInb();
        this.inc = sjtQty.getInc();
        this.ind = sjtQty.getInd();
        this.outa = sjtQty.getOuta();
        this.outb = sjtQty.getOutb();
        this.outc = sjtQty.getOutc();
        this.outd = sjtQty.getOutd();
        this.oute = sjtQty.getOute();
        this.outf = sjtQty.getOutf();
        this.outg = sjtQty.getOutg();
        this.outh = sjtQty.getOuth();
        this.outi = sjtQty.getOuti();
        this.outj = sjtQty.getOutj();
        this.outk = sjtQty.getOutk();
        this.outl = sjtQty.getOutl();
        this.outm = sjtQty.getOutm();
        this.outn = sjtQty.getOutn();
        this.outo = sjtQty.getOuto();
        this.outp = sjtQty.getOutp();
        this.outq = sjtQty.getOutq();
        this.outr = sjtQty.getOutr();
        this.outs = sjtQty.getOuts();
        this.conversion = sjtQty.getConversion();
        this.rid = sjtQty.getRid();
        this.conversion_old = sjtQty.getConversion_old();
        this.outt = sjtQty.getOutt();
        this.outu = sjtQty.getOutu();
        this.outv = sjtQty.getOutv();
        this.ine = sjtQty.getIne();
        this.inf = sjtQty.getInf();
        this.ing = sjtQty.getIng();
        this.inh = sjtQty.getInh();
        this.ini = sjtQty.getIni();
        this.inj = sjtQty.getInj();
        this.ink = sjtQty.getInk();
    }
}
