package com.aria.model;

import com.aria.dto.CreditNoteCategoryDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "creditnote_category")
public class CreditNoteCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    public CreditNoteCategory(CreditNoteCategoryDto creditNoteCategory) {
        this.id = creditNoteCategory.getId();
        this.name = creditNoteCategory.getName();
    }
}
