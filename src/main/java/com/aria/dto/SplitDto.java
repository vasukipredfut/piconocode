package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class SplitDto {


    private Long id;


    private String trndate;


    private String   trnno;


    private Long  splitid;


    private Long  refid;


    private double  qty;


    private double  lot;


    private Long  binname;



    private Long  dept;


    private Long  branch;


    private String  usecid;

    private String  status;

    List<SplitProductDto> splitProducts;

}
