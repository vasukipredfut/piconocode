package com.aria.dto;

import com.aria.model.JournalAttributesOption;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Getter
@Setter
@NoArgsConstructor
public class JournalAttributesOptionDto {


    private Long id;

    private Long attributes_id;

    private Long attributes_map_id;

    private String optionvalue;

    public JournalAttributesOptionDto(JournalAttributesOption option) {
        this.id = option.getId();
        this.attributes_id = option.getAttributes_id();
        this.attributes_map_id = option.getAttributes_map_id();
        this.optionvalue = option.getOptionvalue();
    }
}
