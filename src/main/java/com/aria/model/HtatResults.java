package com.aria.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "htatresults")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HtatResults {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shiftdate")
    private LocalDate shiftdate;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "servid")
    private Long servid;

    @Column(name = "panelid")
    private Long panelid;

    @Column(name = "tsid")
    private Long tsid;

    @Column(name = "atid")
    private Long atid;

    @Column(name = "atname", length = 50)
    private String atname;

    @Column(name = "atresults", length = 25)
    private String atresults;

    @Column(name = "month", length = 2)
    private Long month;

    @Column(name = "year")
    private Integer year;

    @Column(name = "cby")
    private Long cby;

    @Column(name = "con", length = 50)
    private String con;

}
