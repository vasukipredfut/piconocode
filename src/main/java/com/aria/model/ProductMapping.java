package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@NoArgsConstructor
@Table(name = "product_mapping")
@Getter
@Setter
@ToString
public class ProductMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long product_mapping_id;
	
	@OneToOne
	@JoinColumn(name = "product_ref_id", referencedColumnName = "id")
	private ProductMaster product_ref_id;
	
	@Column
	private Long product_category_ref_id;
}
