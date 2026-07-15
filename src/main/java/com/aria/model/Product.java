package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import org.hibernate.annotations.Where;

import com.aria.dto.ProductDto;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "product")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;


	@Column
	private String productName;
	
	
	@Column
	private String lot;

	@Column
	private String totalQuantity;

	@Column
	private int active;


	@Column(updatable = false)
	private Long createdBy;

	@Column
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	@OneToMany(mappedBy = "productId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Where(clause = "active=0")
	private List<SalesProduct> salesProduct;

	public Product(ProductDto product) {
		this.id = product.getId();
		this.productName = product.getProductName();
		
		this.lot = product.getLot();
		this.totalQuantity = product.getTotalQuantity();
		this.active = product.getActive();
        this.createdBy = product.getCreatedBy();
		this.updatedBy = product.getUpdatedBy();
		this.created_date = product.getCreated_date();
		this.updated_date = product.getUpdated_date();

	}

	
}
