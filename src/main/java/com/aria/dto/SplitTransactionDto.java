package com.aria.dto;

import com.aria.model.SplitProductDetails;
import com.aria.model.SplitTransaction;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class SplitTransactionDto {

    private Long id;

   
    private String trndate;

   
    private String   trnno;

   
    private Long  splitid;

   
    private Long  refid;
    private String  productName;

   
    private double  qty;

   
    private double  lot;

   
    private Long  binname;

   
    private Long  createdBy;
    private String  createdByName;

   
    private String  con;

   
    private Long  dept;
    private String  deptName;

   
    private Long  branch;
    private String  branchName;

   
    private String  usecid;

    List<SplitProductDetailsDTO> productDetailsDTOList;

    public SplitTransactionDto(SplitTransaction splitTransaction) {
        DateUtil dateUtil = new DateUtil();
        this.id = splitTransaction.getId();
        this.trndate = dateUtil.getDateTOStringWk(splitTransaction.getTrndate());
        this.trnno = splitTransaction.getTrnno();
        this.splitid = splitTransaction.getSplitid();
        this.refid = splitTransaction.getRefid();
        this.qty = splitTransaction.getQty();
        this.lot = splitTransaction.getLot();
        this.binname = splitTransaction.getBinname();
        this.createdBy = splitTransaction.getCby();
        this.con = splitTransaction.getCon();
        this.dept = splitTransaction.getDid();
        this.branch = splitTransaction.getBid();
        this.usecid = splitTransaction.getUsecid();
    }
}
