package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProductionEntryForm {

    private List<ProductionEntryFormDto> productionEntry;

    private ProductionEntryFormDto journalEntry;

    private String entryIsOn;

}
