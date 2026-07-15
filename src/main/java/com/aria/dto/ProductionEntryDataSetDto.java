package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductionEntryDataSetDto {

    private Long colid;

    private String colName;

    private String colLot;

    private String dataType;

    private String coltable; // connectTable

    private String coltype; // typeOfMap
    private String bomIntakeType; //  waste or output

    private String total;

    private String avg;

    private Long dispUnit;

    private String unitName;

    private String value;

    private String considerAs;
    private String pType;


}
