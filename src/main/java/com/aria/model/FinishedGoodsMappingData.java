package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "finished_goods_mapping_data")
public class FinishedGoodsMappingData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String ms;

    @Column
    private String ss;

    @Column
    private String name;

    @Column
    private Long refid;

    @Column
    private Long refid1;

    @Column
    private Long storder;

    @Column
    private Long itemorder;

    @Column
    private Long pds;

    @Column
    private Long apds;

    @Column
    private Long printOrder;

    @Column
    private Long created_by;

    @Column
    private String status;

    @Column
    private Long bid;
}
