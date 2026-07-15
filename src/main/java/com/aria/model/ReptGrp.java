package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "reptgrp")
public class ReptGrp {

	
	 	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column
	    private Long rid;

	    @Column
	    private Long rsid;

	    @Column
	    private Long refid;

	    @Column
	    private Double dlot;

	    @Column
	    private String dunit;

	    @Column
	    private Double cfr;

	    @Column
	    private Long display_order;
}
