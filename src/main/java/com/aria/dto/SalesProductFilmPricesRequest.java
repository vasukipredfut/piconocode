package com.aria.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;

@Getter
@Setter
public class SalesProductFilmPricesRequest {

    private Long salesProductId;
    private Long salesLocationId;
    private Long salesProductGroupId;
    private Double price;
    private Double permlprice;

}
