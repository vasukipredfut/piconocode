package com.aria.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HtaChargesDto {

    private Long id;
    private Long servid;
    private String name;
    private String vname;
    private String ttype;
    private String tmode;
    private Double cost;
    private String trntype;
    private Double tper;

}