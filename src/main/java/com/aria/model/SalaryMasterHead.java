package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "salary_master_head")
public class SalaryMasterHead {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String structure_name;

    @Column
    private String factory_structure_name;

    @Column
    private String is_factory;

    @Column
    private String is_accounting;

    @Column
    private String is_deduction;

    @Column
    private long sorting;

    @Column
    private String tag;

    @Column
    private String percentage_of_salary;

    @Column
    private double min_amount;

    @Column
    private double max_amount;

    @Column
    private String epf_eligible;

    @Column
    private String esi_eligible;


    @Column
    private String voucher_notes;

    @Column
    private String status;

    @Column
    private String payroll_type;

    @Column
    private String mw;

    @Column
    private String dw;
    
}
