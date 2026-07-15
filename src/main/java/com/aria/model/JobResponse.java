package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "jobresponse")
public class JobResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String purpose;

    @Column
    private String workinst;

    @Column
    private Long clusterid;

    @Column
    private String deptid;

    @Column
    private String branchid;

    @Column
    private String userlevel;

    @Column(name = "final")
    private String finals;
}

