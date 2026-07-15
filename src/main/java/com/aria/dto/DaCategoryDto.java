package com.aria.dto;

import com.aria.model.DaCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class DaCategoryDto {

    private Long id;

   
    private String name;

   
    private String dw;

    private String mw;

   
    private String colid;

    public DaCategoryDto(DaCategory daCategory) {
        this.id = daCategory.getId();
        this.name = daCategory.getName();
        this.dw = daCategory.getDw();
        this.mw = daCategory.getMw();
        this.colid = daCategory.getColid();
    }
}
