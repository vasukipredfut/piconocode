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
@Table(name = "htacharges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HtaCharges {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "servid")
    private Long servid;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "vname", length = 10)
    private String vname;

    @Column(name = "ttype", length = 1)
    private String ttype;

    @Column(name = "tmode", length = 4)
    private String tmode;

    @Column(name = "cost")
    private Double cost;

    @Column(name = "trntype", length = 1)
    private String trntype;

    @Column(name = "tper")
    private Double tper;

}
