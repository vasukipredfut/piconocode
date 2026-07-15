package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.AttributeFieldsProcessTypeOptionBasedDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "options")
public class AttributeFieldsProcessTypeOptionBased {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private Long attributes_id;

	@Column
	private String options;


	public AttributeFieldsProcessTypeOptionBased(AttributeFieldsProcessTypeOptionBasedDto field) {
		this.id = field.getId();
		this.options = field.getOptions();
	}

}
