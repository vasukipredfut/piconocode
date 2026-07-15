package com.aria.model;

import com.aria.dto.JaggeryInspectionScoreDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "jagginspscore")
public class JaggeryInspectionScore {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String uniqno;

    @Column
    private Double chocol;

    @Column
    private Double brown;

    @Column
    private Double ltblack;

    @Column
    private Double black;

    @Column
    private Double white;

    @Column
    private Double moist1;

    @Column
    private Double moist2;

    @Column
    private Double moist3;

    @Column
    private Double moistavg;

    @Column
    private Double  moistmark;

    @Column
    private Double insol1;

    @Column
    private Double insol2;

    @Column
    private Double insol3;

    @Column
    private Double insolavg;

    @Column
    private Double insolmark;
    
    @Column
    private Long insoluble;
    
    @Column
    private Long thuls;
    
    @Column
    private Long blacks;
    
    @Column
    private Long vision;

    @Column
    private Long moistures;
    
    @Column
    private Long leakage;
    
    @Column
    private Long lime;
    
    @Column
    private Long wtless;
    
    @Column
    private Long total;
    
    @Column
    private Long score;
    
    @Column
    private String total_ash;
    
    @Column
    private String total_sugar;
    
    @Column
    private String sucrose;
    
    @Column
    private String acid_insoluble_ash;


    public JaggeryInspectionScore(JaggeryInspectionScoreDto inspectionScore) {
        this.id = inspectionScore.getId();
        this.uniqno = inspectionScore.getUniqno();
        this.chocol = inspectionScore.getChocol();
        this.brown = inspectionScore.getBrown();
        this.ltblack = inspectionScore.getLtblack();
        this.black = inspectionScore.getBlack();
        this.white = inspectionScore.getWhite();
        this.moist1 = inspectionScore.getMoist1();
        this.moist2 = inspectionScore.getMoist2();
        this.moist3 =inspectionScore.getMoist3();
        this.moistavg = inspectionScore.getMoistavg();
        this.moistmark = inspectionScore.getMoistmark();
        this.insol1 = inspectionScore.getInsol1();
        this.insol2 = inspectionScore.getInsol2();
        this.insol3 = inspectionScore.getInsol3();
        this.insolavg = inspectionScore.getInsolavg();
        this.insolmark = inspectionScore.getInsolmark();
        this.insoluble = inspectionScore.getInsoluble();
        this.thuls = inspectionScore.getThuls();
        this.blacks = inspectionScore.getBlacks();
        this.vision = inspectionScore.getVision();
        this.moistures = inspectionScore.getMoistures();
        this.leakage = inspectionScore.getLeakage();
        this.lime = inspectionScore.getLime();
        this.wtless = inspectionScore.getWtless();
        this.total = inspectionScore.getTotal();
        this.score = inspectionScore.getScore();
        this.total_ash = inspectionScore.getTotal_ash();
        this.total_sugar = inspectionScore.getTotal_sugar();
        this.sucrose = inspectionScore.getSucrose();
        this.acid_insoluble_ash = inspectionScore.getAcid_insoluble_ash();
    }
}
