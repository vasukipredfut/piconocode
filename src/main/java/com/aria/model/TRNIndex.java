package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "trnindex")
public class TRNIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long advno;

    @Column
    private Long batchno;

    @Column
    private Long cluster;

    @Column
    private Long compcode;

    @Column
    private Long forwardno;

    @Column
    private Long formid;

    @Column
    private Long lastno;

    @Column
    private Long receivedno;

    @Column
    private String prefix;

    @Column
    private Long vouno;

}
