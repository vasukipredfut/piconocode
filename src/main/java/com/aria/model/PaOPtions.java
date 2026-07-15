package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "paoptions")
public class PaOPtions {

    @Id
    @Column(name = "primaryid")
    private Long primaryId;

    @Column
    private String name;

    @Column(name = "id")
    private Long id;

}
