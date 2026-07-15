package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "unitreportproductlist")
public class UnitReportProducts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long refid;
    @Column
    private String unit;
    @Column
    private Long yieldGroup;
    @Column
    private String input;
    @Column
    private String output;
    @Column
    private Long reportOrder;
}
