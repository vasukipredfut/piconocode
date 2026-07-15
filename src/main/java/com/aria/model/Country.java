package com.aria.model;

import com.aria.dto.CountryDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "country")
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String currency;

    @Column
    private String symbols;

    public Country(CountryDto country) {
        this.id = country.getId();
        this.name = country.getName();
        this.currency = country.getCurrency();
        this.symbols = country.getSymbols();
    }
}
