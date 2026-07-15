package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WindmillPlanDto {

    private Long id;
    private String name;

    public WindmillPlanDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
