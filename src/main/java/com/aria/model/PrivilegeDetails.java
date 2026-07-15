package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.PrivilegeDetailsDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "privilege_details")
public class PrivilegeDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long privilegeId;

	@Column(name = "privilege_name")
	private String privilegeName;

	public PrivilegeDetails(PrivilegeDetailsDto dto) {
		this.privilegeId = dto.getPrivilegeId();
		this.privilegeName = dto.getPrivilegeName();
	}

}
