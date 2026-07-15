package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "saleslocation")
public class SalesProductLocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="location")
    private String locationType;

    @Column(name="name")
    private String name;

    // unique refid is added for  find particular blending advice cas group completed status update
    @Column(name = "ceilinglimit")
    private String ceilingLimit;

    @Column
    private String ip;

    @Column(name = "codeword")
    private String codeWord;

    @Column(name = "createdby")
    private Long createdBy;


}
