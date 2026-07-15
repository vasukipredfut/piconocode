package com.aria.dto;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PageDataDto<T> {
	
	
	private Integer totalPages;
	
	private Long totalRows;
	
	private Integer currentPage;
	
	private List<T> pageData;
	
	
	public PageDataDto (List<T> pageDto, Page pageData)
	{
		this.totalPages = pageData.getTotalPages();
		this.totalRows = pageData.getTotalElements();
		this.currentPage = pageData.getNumber();
		this.pageData = pageDto;
	}
	
	public PageDataDto (List<T> pageDto, Pageable pageData, Page pageData1)
	{
		this.totalPages = pageData1.getTotalPages();
		this.totalRows = (long) pageDto.size();
		this.currentPage = pageData.getPageNumber();
		this.pageData = pageDto;
	}

}
