package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "lot")
public class Lot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "dispname")
    private String dispname;

    @Column(name = "lot_str")
    private String lotString;

    @Column(name = "lot")
    private Double lot;

    @Column(name = "refid")
    private Long productId;


}
