package com.aria.model;

import com.aria.dto.UnitsDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "units")
public class Units {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "eway_bill_Flag")
    private String ewayBillFlag;

    public Units(UnitsDto units) {
        this.id = units.getId();
        this.name = units.getName();
        this.displayName = units.getDisplayName();
        this.ewayBillFlag = units.getEwayBillFlag();
    }
}
