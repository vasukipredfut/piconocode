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
@Table(name = "qtysub2")
public class QtySub2 {
	
	 	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column
	    private Long gparent;

	    @Column
	    private Long parent;

	    @Column
	    private String name;

	    @Column
	    private String trnType;

	    @Column
	    private Long lineId;

	    @Column
	    private String ttable;

	    @Column
	    private String trn;

}
