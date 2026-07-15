package com.aria.dto;

import com.aria.model.FinishedGooseBins;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Getter
@Setter
@NoArgsConstructor
public class FinishedGooseBinsDto {

    private Long id;

   
    private Long refid;

   
    private Long binname;

   
    private Long bid;

   
    private String type;

   
    private String  name;

    private double  qty;

    public FinishedGooseBinsDto(FinishedGooseBins finishedGooseBins) {
        this.id = finishedGooseBins.getId();
        this.refid = finishedGooseBins.getRefid();
        this.binname = finishedGooseBins.getBinname();
        this.bid = finishedGooseBins.getBid();
        this.type = finishedGooseBins.getType();
        this.name = finishedGooseBins.getName();
    }
}
