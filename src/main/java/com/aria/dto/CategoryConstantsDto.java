package com.aria.dto;

import com.aria.model.CategoryConstants;
import com.aria.model.Color;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@Getter
@Setter
public class CategoryConstantsDto {

    private Long id;

    private String name;

    private Long type;

    public CategoryConstantsDto(CategoryConstants constants) {
        this.id = constants.getId();
        this.name = constants.getName();
        this.type = constants.getType();
    }
}
