package com.aria.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "cluster")
public class Cluster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @OneToOne
    @JoinColumn(name = "dept_id", referencedColumnName = "id")
    private DepartmentDetails department;

    @OneToOne
    @JoinColumn(name = "org_id", referencedColumnName = "id")
    private Organisation organisation;

    @OneToOne
    @JoinColumn(name = "branch_id", referencedColumnName = "id")
    private Branch branch;

    @Column
    private String purpose;

    @Column
    private String workinst;

    @Column
    private String production;

    @Column
    private String utilisation;

    @Column
    private String responsibility;

    @Column(name = "final")
    private String finals;

    @Column
    private String oldname;

    @Column
    private String trnprefix;

    @Column
    private String cprefix;

    @Column
    private String pv;


}