package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "vehicle_break_down_log")
public class VehicleBreakDownLog {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private Long  mcid;
    @Column
    private String  mcname;

    @Column
    private String   mciso;
    
    @Column
    private String   problem;

    @Column
    private String   action;
    
    @Column
    private String   rcause;
    @Column
    private String   paction;

    @Column
    @Temporal(TemporalType.DATE)
    private Date stdate;
    @Column
    @Temporal(TemporalType.TIME)
    private Date  sttime;
    
    @Column
    @Temporal(TemporalType.DATE)
    private Date  endate;
    
    @Column
    @Temporal(TemporalType.TIME)
    private Date  entime;
    
    @Column
    private Double  difftime;
    
    @Column
    private String   topic;
    
    @Column
    private String   entposition;
    
    @Column
    private LocalDateTime modified;
    
    @Column
    private Long   cby;
    
    @Column
    private String   cendt;
    @Column
    private Long   aby;
    
    @Column
    private String  aendt;
    @Column
    private Long   vby;
    @Column
    private String   vendt;
    @Column
    private Long   cluster;
    @Column
    private Long  dept;
    
    @Column
    private Long   branch;
    
    @Column
    private String   status;
    
    @Column
    private LocalDateTime   varrivedon;
    
    @Column
    private String   astrvarr;
    @Column
    private String   jcardno;
    @Column
    private Date   jcarddt;
    
    @Column
    private String   iono;
    
    @Column
    private String   iopageno;
    
    @Column
    private Long   trialby;
    
    @Column
    private String   strst;
    
    @Column
    private String   stren;
    
    @Column
    private String   apendt;
    
    @Column
    private Long  apby;
    
    @Column
    @Temporal(TemporalType.DATE)
    private Date   datepro;
    
    @Column
    private Long   inkm;
    
    @Column
    private Long   outkm;

}
