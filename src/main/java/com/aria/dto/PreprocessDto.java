package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;

import com.aria.model.Preprocess;
import com.aria.model.Product;

@Getter
@Setter
@NoArgsConstructor
public class PreprocessDto {

	private Long priid;

	private String name;

	private Long sjid;

	private Long productId;

	private Long id;

	

	public PreprocessDto(Preprocess preprocess) {
		
		this.id=preprocess.getId();
        this.name = StringEscapeUtils.unescapeHtml4(preprocess.getName());
		this.sjid=preprocess.getSjid();
		this.productId=preprocess.getRefid();
		this.priid=preprocess.getPriid();
		
		
	}

}
