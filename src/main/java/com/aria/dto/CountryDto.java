package com.aria.dto;

import com.aria.model.Country;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class CountryDto {

    private Long id;

    private String name;
   
    private String currency;

    private String symbols;

    public CountryDto(Country country) {
        this.id = country.getId();
        this.name = country.getName();
        this.currency = country.getCurrency();
        this.symbols = country.getSymbols();
    }
}
