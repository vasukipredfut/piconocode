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
@Table(name = "salesproductname")
public class SalesProductNameEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "salesproductgroup")
	private Long salesProductGroup;

	@Column(name = "productname")
	private String productName;

	// unique refid is added for find particular blending advice cas group completed
	// status update
	@Column
	private String type;

	@Column
	private Long capacity;

	@Column(name = "createdby")
	private Long createdBy;

}
