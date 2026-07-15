package com.aria.dto;

import com.aria.model.MyTest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MyTestDto {

	private Long id;

	private String imagePath;

	public MyTestDto(MyTest myTest) {
		this.id = myTest.getId();
		this.imagePath = myTest.getImagePath();
	}
}
