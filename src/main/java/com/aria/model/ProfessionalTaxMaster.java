package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "professional_tax_master")
public class ProfessionalTaxMaster {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column
    private double  salaryRange_From;
    @Column
    private double salaryRange_To;
    @Column
    private double ptAmount;
    @Column
    private String status;
    @Column
    private Long branch;

}
