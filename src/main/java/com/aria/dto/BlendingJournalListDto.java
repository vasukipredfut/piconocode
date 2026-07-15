package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BlendingJournalListDto {

    private Long id;
    private String name;
    private Long productId;
    private String stockType;

    public BlendingJournalListDto(JournalList journalList) {
        this.id = journalList.getId();
        this.name = journalList.getName();
        this.productId = journalList.getBatchProId();
        this.stockType = journalList.getStockType();
    }
}
