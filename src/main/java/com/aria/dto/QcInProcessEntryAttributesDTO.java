package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QcInProcessEntryAttributesDTO {

    private Long attributeId;

    private String attributeName;
    private String attributeResValue;
    private String unit;


    public QcInProcessEntryAttributesDTO(QcInProcessEntryAttributes attributes) {
        this.attributeId = attributes.getAttributeId();
        this.attributeName = attributes.getAttributeName();
    }
}
