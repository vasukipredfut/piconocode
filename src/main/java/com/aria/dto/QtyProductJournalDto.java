package com.aria.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QtyProductJournalDto {

	List<Long>journalId;
	List<Long>productId;
	private boolean pfKey;
	
}
