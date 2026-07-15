package com.aria.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimingReadingDto {

    private Long timingId;
    private String timingName;
    
    private Long exportAttributeid;
    private Double exportOpening;
    private Double exportClosing;

   
    private Long importAttributeid;
    private Double importOpening;
    private Double importClosing;
    
    private Long tdLossAttributeid;
    private Double tdLoss;

    private Long netGenerationAttributeid;
    private Double netGeneration;
    
    private Long generationPowerAttributeid;
    private Double generationPowerOpening;
    private Double generationPowerClosing;
    
    private Long exportPowerAttributeid;
    private Double exportPowerOpening;
    private Double exportPowerClosing;
    
    private Long netexportAttributeid;
    private Double netexport;
   
    private Long consumedunitAttributeid;
    private Double consumedunit;
//    

}

