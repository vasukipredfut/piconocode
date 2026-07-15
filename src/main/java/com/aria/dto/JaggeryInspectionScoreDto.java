package com.aria.dto;

import com.aria.model.JaggeryInspectionScore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class JaggeryInspectionScoreDto {

    private Long id;

    
    private String uniqno;

    
    private Double chocol;

    
    private Double brown;

    
    private Double ltblack;

    
    private Double black;

    
    private Double white;

    
    private Double moist1;

    
    private Double moist2;

    
    private Double moist3;

    
    private Double moistavg;

    
    private Double  moistmark;

    
    private Double insol1;

    
    private Double insol2;

    
    private Double insol3;

    
    private Double insolavg;

    
    private Double insolmark;

    
    private Long insoluble;

    
    private Long thuls;

    
    private Long blacks;

    
    private Long vision;

    
    private Long moistures;

    
    private Long leakage;

    
    private Long lime;

    
    private Long wtless;

    
    private Long total;

    
    private Long score;

    
    private String total_ash;

    
    private String total_sugar;

    
    private String sucrose;

    
    private String acid_insoluble_ash;

    public JaggeryInspectionScoreDto(JaggeryInspectionScore inspectionScore) {
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
