package com.aria.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConsumablesDto {

    private Integer id;

    private String binName;

    private String batchNo;

    private Integer quantity;

    public ConsumablesDto(Object id, Object binName, Object batchNo, Object quantity) {
        this.id = (Integer) id;
        this.binName = binName.toString();
        this.batchNo = batchNo.toString();
        this.quantity = (Integer) quantity;
       
    }
}
