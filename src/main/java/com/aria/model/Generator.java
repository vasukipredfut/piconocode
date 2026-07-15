package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.GeneratorDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "generator")
public class Generator {
	
	//needs to update sts which comes from machine master for that geneset id

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String name;
	
	@Column
	private String capacity;
	
	@Column(name = "machine_id")
	private String machineId;
	
	@Column
	private String status;
	

	@Column
	private Long cluster;
	
	@Column
	private Long branch;
	
	@Column
	private Long company;
	
	@Column
	private Long mf;
	
	@OneToOne
	@JoinColumn(name = "created_by", updatable = false, referencedColumnName = "id")
	private User createdBy;
	
	@OneToOne
	@JoinColumn(name = "updated_by", referencedColumnName = "id")
	private User updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	public Generator(GeneratorDto gen) {
		this.id = gen.getId();

		this.name = gen.getName();

		this.capacity = gen.getCapacity();
		this.machineId = gen.getMachineId();
		this.status=gen.getStatus();
		if (gen.getCreatedBy() != null) {
			User ref= new User();
			ref.setId(gen.getCreatedBy());
			this.createdBy = ref;
		}
		if (gen.getUpdatedBy() != null) {
			User ref= new User();
			ref.setId(gen.getUpdatedBy());
			this.updatedBy = ref;
		}

		this.created_date = gen.getCreated_date();
		this.updated_date = gen.getUpdated_date();
	}
}
