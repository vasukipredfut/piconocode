package com.aria.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HttfgrpDto {

    private Long id;
    private String name;
    private Double urate;
    private String ttype;
    private Double aper;
    private Double dper;
    private String status;
    private String mceiling;
    private Double climit;
}
