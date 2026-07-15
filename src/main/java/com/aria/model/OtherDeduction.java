package com.aria.model;

import com.aria.dao.OtherDeductionRepo;
import com.aria.dto.OtherDeductionEmpReqDto;
import com.aria.dto.OtherDeductionReqDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "otherdeduction")
public class OtherDeduction {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private double  amount;
    @Column
    private String description; 
    
    @Column
    private Long cid;

    @Column
    private String status;

    @Column
    @Temporal(TemporalType.DATE)
    private Date created_time;

    @Column
    private String name;

    @Column
    private Long conid;

    @Column
    private String contable;

    @Column
    private Long month;

    @Column
    private Long year;

    @Column
    private Long createdby;

    @Column
    private Long branch;

    @Column
    private Long dept;
    
    //Create a new column to check whether the bill has been generated for this entry or not
    @Column
    private Boolean generatebill;


    
    public OtherDeduction(OtherDeductionReqDTO otherDeductionReqDTO, OtherDeductionEmpReqDto empReqDto) {

        this.description = otherDeductionReqDTO.getType();
        this.branch = otherDeductionReqDTO.getBranch();
        this.dept = otherDeductionReqDTO.getDept();
        this.name = otherDeductionReqDTO.getType();
        this.contable = otherDeductionReqDTO.getConnectTable();

        this.conid = empReqDto.getEmpId();
        this.month = otherDeductionReqDTO.getMonth();
        this.year = otherDeductionReqDTO.getYear();
        this.amount = empReqDto.getAmount();
    }


}
