package com.aria.dto;



import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommonDropDownDto<T> {

	private Long id;
	private String name;

	public CommonDropDownDto(Object id, Object name) {
		String a = String.valueOf(id);
		Long b = Long.parseLong(a);
		this.id = b;
		this.name = name != null ? name.toString() : null;
	}

	public CommonDropDownDto(Long id, String name) {
		this.id = id;
		this.name = name;
	}
}
