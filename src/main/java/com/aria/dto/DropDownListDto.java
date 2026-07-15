package com.aria.dto;

import com.aria.model.DropDownList;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DropDownListDto {

    private Long id;

    private String name;

    public DropDownListDto(DropDownList dropDown) {
        this.id = dropDown.getId();
        this.name = dropDown.getName();
    }

}
