package com.aria.dto;

import com.aria.model.Organisation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrganizationDto {

    private Long idOrg;

    private String name;

    public OrganizationDto(Organisation organisation) {
        this.idOrg = organisation.getId();
        this.name = organisation.getName();
    }
}
