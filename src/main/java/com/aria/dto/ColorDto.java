package com.aria.dto;

import com.aria.model.Color;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@Getter
@Setter
public class ColorDto {

    private Long id;

    private String name;

    private String value;

    public ColorDto(Color color) {
        this.id = color.getId();
        this.name = color.getName();
        this.value = color.getValue();
    }
}
