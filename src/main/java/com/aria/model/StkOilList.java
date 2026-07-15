package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "stkoillist")
public class StkOilList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long  id; 
            
    @Column
    private String name;

    @Column
    private Long parentid;

    @Column
    private Long refid;

    @Column
    private double lot;

    @Column
    private double conversion;

    @Column
    private String cword;

    @Column
    private double pconversion;

    @Column
    private double conversion_old;

    @Column
    private int oil_Consumption;

    @Column
    private double createdby;

    @Column
    private String group;

    @Column
    private String subGroup1;

    @Column
    private double displayOrder1;

    @Column
    private double displayOrder2;

}
