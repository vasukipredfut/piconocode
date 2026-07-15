package com.aria.dto;


import java.util.List;

import com.aria.model.Permissions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JwtResponse {
	private String accessToken;
	private String tokenType = "Bearer";
	private String refreshToken;
	private Long id;
	private String username;
	private List<String> roles;
	private List<Permissions> permissions;

	public JwtResponse(String accessToken, String refreshToken, Long id, String username, List<String> roles,List<Permissions> permissions) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.id = id;
		this.username = username;
		this.roles = roles;
		this.permissions = permissions;
	}
	

}