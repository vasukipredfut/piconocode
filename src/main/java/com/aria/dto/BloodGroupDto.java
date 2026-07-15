package com.aria.dto;


import com.aria.model.BloodGroup;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BloodGroupDto {

    private Long id;

    private String name;

    public BloodGroupDto(BloodGroup bloodGroup) {
        this.id = bloodGroup.getId();
        this.name = bloodGroup.getName();
    }
}
