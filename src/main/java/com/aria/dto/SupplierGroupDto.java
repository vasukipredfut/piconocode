package com.aria.dto;

import com.aria.model.SupplierGroup;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;

@Getter
@Setter
@NoArgsConstructor
public class SupplierGroupDto {

    private Long id;
    private String name;
    private int activeFlag;

    public SupplierGroupDto(SupplierGroup supplierGroup) {
        this.id = supplierGroup.getId();
        this.name = supplierGroup.getName();
        this.activeFlag = supplierGroup.getActiveFlag();
    }
}
