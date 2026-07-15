package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;

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
@Table(name = "inward_audit")
public class InwardAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String batch_no;

	@Column
	private String entry_type;

	@Column(name = "inward_date")
	private Date inwardDate;

	@Column(name = "product_id")
	private Long productId;

	@Column
	private String status;

	@Column
	private String batch_type;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_By")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	@Column
	private String roqcomment;
}
