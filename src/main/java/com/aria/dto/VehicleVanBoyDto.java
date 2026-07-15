package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VehicleVanBoyDto {
    private Long id;
    private String employeeName;

    public VehicleVanBoyDto(Long id, String employeeName) {
        this.id = id;
        this.employeeName = employeeName;
    }
}
