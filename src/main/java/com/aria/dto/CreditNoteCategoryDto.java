package com.aria.dto;

import com.aria.model.CreditNoteCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@NoArgsConstructor
@Getter
@Setter
public class CreditNoteCategoryDto {

    private Long id;


    private String name;

    public CreditNoteCategoryDto(CreditNoteCategory creditNoteCategory) {
        this.id = creditNoteCategory.getId();
        this.name = creditNoteCategory.getName();
    }
}
