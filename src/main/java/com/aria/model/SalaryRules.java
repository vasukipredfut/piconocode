package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "salary_rules")
public class SalaryRules {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String sal_head_name;

    @Column
    private String head_id;

    @Column
    private String contribution_by_employee;

    @Column
    private String contribution_by_employer;

    @Column
    private String minimum_amount;

    @Column
    private String maximum_amount;

    @Column
    private double percentage;
    

}
