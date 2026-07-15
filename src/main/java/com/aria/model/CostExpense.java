package com.aria.model;

import com.aria.dto.CostExpenseDto;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;


@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "cost_expense")
public class CostExpense {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String trnno;

    @Column
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column
    private String cost_desc;
    @Column
    private Double amount;

    @Column
    private String cost_type;

    @Column
    private Long conid;

    @Column
    private String contype;

    @Column
    private Long logid;

    @Column
    private String logtable;

    @Column
    private Long created_by;

    @Column
    private String cendt;

    @Column
    private String cluster;

    @Column
    private Long branch_id;

    @Column
    private Long dept_id;
    
    @Column
    private Long sconid;

    @Column
    private String scontype;

    @Column
    private String narration;

    @Column
    private String usecid;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;


    public CostExpense(CostExpenseDto costExpense) {
        DateUtil dateUtil = new DateUtil();
        this.id = costExpense.getId();
        this.trnno = costExpense.getTrnno();
        this.date = dateUtil.getStringTODateWk(costExpense.getDate());
        this.cost_desc = costExpense.getCost_desc();
        this.amount = costExpense.getAmount();
        this.cost_type = costExpense.getCost_type();
        this.conid = costExpense.getConid();
        this.contype = costExpense.getContype();
        this.logid = costExpense.getLogid();
        this.logtable = costExpense.getLogtable();
        this.created_by = costExpense.getCreatedBy();
        this.cendt = costExpense.getCendt();
        this.cluster = costExpense.getCluster();
        this.branch_id = costExpense.getBranchId();
        this.dept_id = costExpense.getDeptId();
        this.sconid = costExpense.getSconid();
        this.scontype = costExpense.getScontype();
        this.narration = costExpense.getNarration();
        this.usecid = costExpense.getUsecid();
    }
}
