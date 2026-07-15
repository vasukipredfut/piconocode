package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "process_report_product_group")
public class ProcessReportProductGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private Long prid;

    @Column
    private Long prigrpid;

    @Column
    private Long createdby;

    @Column
    private Long activeFlag;

}
