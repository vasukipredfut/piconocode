package com.aria.dto;

import jakarta.persistence.Entity;

import org.apache.commons.text.StringEscapeUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class RoqCommentDTO {

	private Long id;
	private String name;

	public RoqCommentDTO(RoqCommentList list) {
		this.id = list.getId();
		this.name = StringEscapeUtils.unescapeHtml4(list.getName());

	}

}
