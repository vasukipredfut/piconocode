package com.aria.dto;

import com.aria.model.EmastDaily;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmastSingleUserResponse {
	private EmastDaily emast;
	private String blood;
	private String branch;
}
