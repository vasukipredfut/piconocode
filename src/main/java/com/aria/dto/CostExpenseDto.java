package com.aria.dto;


import com.aria.model.CostExpense;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CostExpenseDto {

    private Long id;

    private String trnno;

    private String date;


    private String cost_desc;

    private Double amount;


    private String cost_type;


    private Long conid;


    private String contype;


    private Long logid;


    private String logtable;


    private Long createdBy;


    private String cendt;


    private String cluster;


    private Long branchId;


    private Long deptId;


    private Long sconid;


    private String scontype;


    private String narration;


    private String usecid;
    private Long binNo;
    private String batchNo;

    public CostExpenseDto(CostExpense costExpense) {
        DateUtil dateUtil = new DateUtil();
        this.id = costExpense.getId();
        this.trnno = costExpense.getTrnno();
        this.date = dateUtil.getDateTOStringWk(costExpense.getDate());
        this.cost_desc = costExpense.getCost_desc();
        this.amount = costExpense.getAmount();
        this.cost_type = costExpense.getCost_type();
        this.conid = costExpense.getConid();
        this.contype = costExpense.getContype();
        this.logid = costExpense.getLogid();
        this.logtable = costExpense.getLogtable();
        this.createdBy = costExpense.getCreated_by();
        this.cendt = costExpense.getCendt();
        this.cluster = costExpense.getCluster();
        this.branchId = costExpense.getBranch_id();
        this.deptId = costExpense.getDept_id();
        this.sconid = costExpense.getSconid();
        this.scontype = costExpense.getScontype();
        this.narration = costExpense.getNarration();
        this.usecid = costExpense.getUsecid();
    }
}
