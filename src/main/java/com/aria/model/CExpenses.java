package com.aria.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.CExpensesDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "cexpense")
public class CExpenses {
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    private String trnno;

	    private Date date;

	    private String cdesc;

	    private Double amount;

	    private String ctype;

	    private Long conid;

	    private String contype;

	    private Long logid;

	    private String logtable;

	    private Long cby;

	    private String cendt;

	    private Long vby;

	    private String vendt;

	    private Long aby;

	    private String aendt;

	    private Long cluster;

	    private Long branch;

	    private Long dept;

	    private Long branch1;

	    private Long sconid;

	    private String scontype;

	    private String narration;

	    private String usecid;

	    private LocalDateTime cdate;

		public CExpenses(CExpensesDto CExpensesDto) {
			this.id = CExpensesDto.getId();
			this.trnno = CExpensesDto.getTrnno();
			this.date = CExpensesDto.getDate();
			this.cdesc = CExpensesDto.getCdesc();
			this.amount = CExpensesDto.getAmount();
			this.ctype = CExpensesDto.getCtype();
			this.conid = CExpensesDto.getConid();
			this.contype = CExpensesDto.getContype();
			this.logid = CExpensesDto.getLogid();
			this.logtable = CExpensesDto.getLogtable();
			this.cby = CExpensesDto.getCby();
			this.cendt = CExpensesDto.getCendt();
			this.vby = CExpensesDto.getVby();
			this.vendt = CExpensesDto.getVendt();
			this.aby = CExpensesDto.getAby();
			this.aendt = CExpensesDto.getAendt();
			this.cluster = CExpensesDto.getCluster();
			this.branch = CExpensesDto.getBranch();
			this.dept = CExpensesDto.getDept();
			this.branch1 = CExpensesDto.getBranch1();
			this.sconid = CExpensesDto.getSconid();
			this.scontype = CExpensesDto.getScontype();
			this.narration = CExpensesDto.getNarration();
			this.usecid = CExpensesDto.getUsecid();
			this.cdate = CExpensesDto.getCdate();
		}
	    

}
