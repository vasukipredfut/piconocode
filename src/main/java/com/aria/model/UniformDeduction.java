package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "uniform_deduction")
public class UniformDeduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long empcode;
    @Column
    private Long month;
    @Column
    private Long year;
    @Column
    private Long amount;
    @Column
    private Long createdby;


    @Column
    @Temporal(TemporalType.DATE)
    private Date created_time;


    @Column
    private Long contable;
    @Column
    private Long billing_month;
}
