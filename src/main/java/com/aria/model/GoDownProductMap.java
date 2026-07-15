package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "go_down_product_map")
public class GoDownProductMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String gd_name;

    @Column
    private Long refid;

    @Column
    private Long bid;

    @Column
    private Long year;

    @Column
    private String month;

    @Column
    private Long gid;
    
    @Column
    private Long  dispname;
}
