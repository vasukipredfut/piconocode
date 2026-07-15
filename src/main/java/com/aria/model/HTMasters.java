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
@Table(name = "htmasters", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HTMasters {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 50)
    private String name;

    private Long bid;
    private Long panelid;
    private Long cid;
    private Long resbid;

    private Integer bcycle;

    private Double dq;
    private Double md;
    private Double dmandcost;
    private Double mrent;

    @Column(length = 10)
    private String tfname;

    private Double eq;
    private Double pmd;

    private Long tfid;
    private Long expenseid;
    private Long vid;
    private Long mf;
    private Long branch;
}
