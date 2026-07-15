package com.aria.dto;

public class MyCellDto {

	private String content;

    public MyCellDto() {
    }

    public MyCellDto(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

	@Override
	public String toString() {
		return "MyCell [content=" + content + "]";
	}
}

