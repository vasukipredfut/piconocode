package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BinListDto {

    private Long godownId;
    private String godownName;
    private String fromBin;
    private String toBin;
    private String firmName;
    private Long firmId;
    private Object allocatedBin;
    private Object pendingBin;

    public BinListDto(BinList binList) {
        this.godownId = binList.getGodownId();
        this.godownName = binList.getGodownName();
        this.fromBin = binList.getFromBin();
        this.toBin = binList.getToBin();
        this.firmId = binList.getFirmId();
        this.firmName = binList.getFirmName();
    }

}
