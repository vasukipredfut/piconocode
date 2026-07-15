package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.SubModulesDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "sub_modules")
public class SubModules {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name")
	private String submoduleName;

	@OneToOne
	@JoinColumn(name = "module_id", referencedColumnName = "id")
	private Modules moduleId;

	public SubModules(SubModulesDto dto) {
		this.id = dto.getId();
		if (dto.getModuleId() != null) {
			Modules modules = new Modules();
			modules.setId(dto.getModuleId());
			this.moduleId = modules;
		}
		this.submoduleName = dto.getName();
	}

}
