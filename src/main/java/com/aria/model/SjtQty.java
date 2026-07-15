package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "sjtqty")
public class SjtQty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rid;


    @Column
    private Long sjid;

    @Column
    private String sjname;
    @Column
    private Long ina;
    @Column
    private Long inb;
    @Column
    private Long inc;
    @Column
    private Long ind;
    @Column
    private Long outa;
    @Column
    private Long outb;
    @Column
    private Long outc;
    @Column
    private Long outd;
    @Column
    private Long oute;
    @Column
    private Long outf;
    @Column
    private Long outg;
    @Column
    private Long outh;
    @Column
    private Long outi;
    @Column
    private Long outj;
    @Column
    private Long outk;
    @Column
    private Long outl;
    @Column
    private Long outm;
    @Column
    private Long outn;
    @Column
    private Long outo;
    @Column
    private Long outp;
    @Column
    private Long outq;
    @Column
    private Long outr;
    @Column
    private Long outs;
    @Column
    private Double conversion;

    @Column
    private Double conversion_old;
    @Column
    private Long outt;
    @Column
    private Long outu;
    @Column
    private Long outv;
    @Column
    private Long ine;
    @Column
    private Long inf;
    @Column
    private Long ing;
    @Column
    private Long inh;
    @Column
    private Long ini;
    @Column
    private Long inj;
    @Column
    private Long ink;
}
