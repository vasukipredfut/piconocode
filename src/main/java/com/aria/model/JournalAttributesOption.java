package com.aria.model;

import com.aria.dto.JournalAttributesOptionDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "journal_attributes_option")
public class JournalAttributesOption {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long attributes_id;

    @Column
    private Long attributes_map_id;

    @Column
    private String optionvalue;


    public JournalAttributesOption(JournalAttributesOptionDto option) {
        this.id = option.getId();
        this.attributes_id = option.getAttributes_id();
        this.attributes_map_id = option.getAttributes_map_id();
        this.optionvalue = option.getOptionvalue();
    }
}
