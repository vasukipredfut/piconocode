package com.aria.model;

import com.aria.dto.SupplierGroupDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "suppliergroup")
public class SupplierGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column(name = "active_flag")
    private int activeFlag;

    public SupplierGroup(SupplierGroupDto supplierGroup) {
        this.id = supplierGroup.getId();
        this.name = supplierGroup.getName();
        this.activeFlag = supplierGroup.getActiveFlag();
    }

}
