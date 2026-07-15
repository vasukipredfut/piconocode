package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class LeaveSalaryVoucherDto {


    private Long id;

   
    private String trnno;

   
   
    private String trndate;

   
    private Long year;

   
    private Long cid;

   
    private Long bid;

   
    private Long did;

   
    private String pay;

   
    private double amt;

   
    private Long cby;

   
    private String cst;

   
    private String vtype;

   
    private String  verify;

   
    private Long vstaff;

   
    private String vendt;

   
    private Long approval;

   
    private Long apstaff;

   
    private String  apendt;

   
    private String authenty;

   
    private Long  astaff;

   
    private String  aendt;

   
   
    private String vdate;

   
   
    private String adate;

   
   
    private String  apdate;

   
    private String  paytype;

   
    private String paydesc;

   
    private int month;


}
