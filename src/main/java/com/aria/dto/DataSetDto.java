package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DataSetDto {

    private Long attributesId;

    private String attributesName;

    private int dispOrder;



    public DataSetDto(DataSetList dataSetList) {
        this.attributesId = dataSetList.getAttributesId();
        this.attributesName = dataSetList.getAttributesName();
        this.dispOrder = dataSetList.getDispOrder();
    }
}
