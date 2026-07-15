package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.LabelLocationDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "labelLocation")
public class LabelLocation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "type")
	private String type;

	@Column(name = "ceilinglimit")
	private String ceilingLimit;

	@Column(name = "ip")
	private String ip;

	@Column(name = "locationid")
	private Long locationId;

	@OneToOne
	@JoinColumn(name = "createdby", referencedColumnName = "id")
	private User createdBy;

	public LabelLocation(LabelLocationDto dto) {
		this.id = dto.getId();
		this.name = dto.getName();
		this.type = dto.getType();
		this.ceilingLimit = dto.getCeilingLimit();
		this.ip = dto.getIp();
		this.locationId = dto.getLocationId();
	}

}
