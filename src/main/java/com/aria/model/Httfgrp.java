package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "httfgrp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Httfgrp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "primaryid")
    private Long primaryId;

    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 60)
    private String name;

    @Column(name = "urate")
    private Double urate;

    @Column(name = "ttype", length = 5)
    private String ttype;

    @Column(name = "aper")
    private Double aper;

    @Column(name = "dper")
    private Double dper;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "mceiling", length = 1)
    private String mceiling;

    @Column(name = "climit")
    private Double climit;
}
