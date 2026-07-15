package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "fgmdata")
public class FgmData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ms", nullable = false)
    private String ms;

    @Column(name = "ss", nullable = false)
    private String ss;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "refid", nullable = false)
    private Long refid;

    @Column(name = "refid1")
    private Long refid1;

    @Column(name = "storder")
    private Integer storder;

    @Column(name = "itemorder")
    private Integer itemorder;

    @Column(name = "pds")
    private Integer pds;

    @Column(name = "apds")
    private Integer apds;

    @Column(name = "printOrder", nullable = false)
    private Integer printOrder;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "status")
    private Character status;

    @Column(name = "bid")
    private Integer bid;
}
