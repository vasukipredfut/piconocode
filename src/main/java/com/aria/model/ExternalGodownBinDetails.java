package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "external_godown_bindetail")
public class ExternalGodownBinDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "godown_external_id")
    private Long goDownExternalId;


    @Column(name = "batch_No")
    private String batchNo;

    @Column(name = "item_id")
    private Long itemId;


    @Column(name = "item_Name")
    private String itemName;


    @Column(name = "qty")
    private Double qty;

    @Column(name = "godown_id")
    private Long godownId;

    @Column(name = "bin_no")
    private Long binNo;

    @Column(name = "branch")
    private Long branch;

    @Column(name = "dept")
    private Long dept;
    @Column(name = "lot")
    private double lot;

}
