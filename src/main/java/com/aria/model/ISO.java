package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "iso")
public class ISO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iso_no")
    private String isoNo;

    @Column(name = "module_type")
    private String moduleType;

    @Column(name = "active_flag")
    private Boolean active_flag;


}
