package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.aria.model.Attendance;
import com.aria.model.MachineDropDown;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MachineDropDownDto {

	private Long id;

	private String name;

	public MachineDropDownDto(MachineDropDown machineDropDown) {
		this.id = machineDropDown.getId();
		this.name=machineDropDown.getName();
	
	}

}
