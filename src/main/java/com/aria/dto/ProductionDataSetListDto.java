package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StringEscapeUtils;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProductionDataSetListDto {


    private Long colid;

    private String colName;
    private String dispColName;

    private String dataType;

    private String linkWith;
    private String typeOfMap;
    private String connectTable;
    private Long dispUnit;
    private String unitName;
    private Long awithId;
    private String awith;
    private String minValue;
    private String maxValue;
    private String considerAs;
    private Boolean appCalStatus;
    private String Excess;

    List<CommonDropDownDto> dropDownDtoList;
    private String bomIntakeType;


    public ProductionDataSetListDto(ProductionDataSetList productionDataSetList) {
        this.colid = productionDataSetList.getColid();
        this.colName = StringEscapeUtils.unescapeHtml4(productionDataSetList.getColName());
        this.dataType = productionDataSetList.getDataType();
        this.linkWith = productionDataSetList.getLinkWith();
        this.typeOfMap = productionDataSetList.getTypeOfMap();
        this.dispUnit = productionDataSetList.getDispUnit();
        this.unitName = productionDataSetList.getUnitName();
        this.awithId = productionDataSetList.getAwithId();
        this.awith = productionDataSetList.getAwith();
        this.connectTable = productionDataSetList.getConnectTable();
        this.minValue = productionDataSetList.getMinValue();
        this.maxValue = productionDataSetList.getMaxValue();
        this.considerAs = productionDataSetList.getConsiderAs();
        this.appCalStatus = productionDataSetList.getAppCalStatus();
        this.bomIntakeType = productionDataSetList.getBomIntakeType();
    }


}
