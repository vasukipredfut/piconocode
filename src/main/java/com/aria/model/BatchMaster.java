package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "batchmaster")
public class BatchMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long  id;  

    @Column(name = "productid")
    public Long productId;

    @Column
    private String batchno;

    @Column
    private String btype;

    @Column
    private Long mcid;

    @Column
    private Long createdby;

    @Column
    private Long dept;

    @Column
    private Long branch;

    @Column
    private String lockstatus;  
    
    @Column
    private Long unlockby;

    @Column
    private String sj;

    @Column
    private String si;

    @Column
    private String track;

    @Column
    private Long shiftid;

    @Column
    private Date shiftdt;

    @Column
    private Double tgt;

    @Column
    private Double stkqty;

    @Column
    private Double pot;

    @Column
    private String location;

    @Column
    private String del;

    @Column
    private Long delby;

    @Column
    private Date deldt;

    @Column
    private String x;

    @Column
    private Long vid;

    @Column
    private Date inwdt;

    @Column
    private Date delydt;

    @Column
    private String score;

    @Column
    private String pshift;
}
