package com.aria.model;

import com.aria.dto.SalaryTransactionDto;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "salary_transaction")
public class SalaryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String trnyear;
    @Column
    private int trnmonth;
    @Column
    private Long trnweekid;
    @Column
    @Temporal(TemporalType.DATE)
    private Date trndate;
    @Column
    private Long trnid;
    @Column
    private String trnno;
    @Column
    private Long conid; // empId OR EmastId
    @Column
    private String contable; // employee table or East_daily table
    @Column
    private String scode;
    @Column
    private String designation;
    @Column
    @Temporal(TemporalType.DATE)
    private Date jdate;
    @Column
    private String epfno;
    @Column
    private String esino;
    @Column
    private Long did;
    @Column
    private Long bid;
    @Column
    private Long cid;
    @Column
    @Temporal(TemporalType.DATE)
    private Date fmperiod;
    @Column
    @Temporal(TemporalType.DATE)
    private Date toperiod;
    @Column
    private String mnodays;
    @Column
    private double nowdays;
    /* @Column
     private double basic;
     @Column
     private double da;
     @Column
     private double hra;*/
    @Column
    private double tds;
    @Column
    private double total;
    @Column
    private double eepf1;
    @Column
    private double eepf2;
    @Column
    private double eepftotal;
    @Column
    private double eesi;
    @Column
    private double depf;
    @Column
    private double desi;
    @Column
    private double tsalary;
    @Column
    private double tdeduction;
    @Column
    private double netsalary;
    @Column
    private double advance;
    @Column
    private double balance;
    @Column
    private Long cby;
    @Column
    private String cst;
    @Column
    private Long vby;
    @Column
    private String vst;
    @Column
    private Long aby;
    @Column
    private String ast;
    @Column
    private String bankacno;
    @Column
    private String bank;
    @Column
    private String sex;
    @Column
    private double gwa; // c1

    @Column
    private double allowance; // c1  for dailywages
    @Column
    private double conveyance; //c2
    @Column
    private double hra; // c3
    @Column
    private double basic; //c4
    @Column
    private double da; //c5
    @Column
    private double wa; //c6
    @Column
    private double C7; //c7
    @Column
    private double C8;
    @Column
    private double C9;
    @Column
    private double C10;
    @Column
    private double leaveHolidayBasic;
    @Column
    private double leaveHolidayDA;
    @Column
    private double nowday1;
    @Column
    private String vtype;
    @Column
    private double oeepf1;
    @Column
    private double oeepf2;
    @Column
    private double oeepftotal;
    @Column
    private double oeesi;
    @Column
    private double odepf;

    @Column
    private double odesi;

    @Column
    private double diffeepf1;

    @Column
    private double diffeepf2;

    @Column
    private double diffeepftotal;

    @Column
    private double diffeesi;

    @Column
    private double diffdepf;

    @Column
    private double diffdesi;

    @Column
    private double otherDeduction;

    @Column
    private double uniformReimbursment;

    @Column
    private double uniformReimbursement;

    @Column
    private double CL_availed;

    @Column
    private double SL_availed;

    @Column
    private double EL_availed;

    @Column
    private double M_availed;

    @Column
    private double CL_atCredit;

    @Column
    private double SL_atCredit;

    @Column
    private double EL_atCredit;

    @Column
    private double M_atCredit;

    @Column
    private String refNoWithDate;

    @Column
    private double ptAmount;

    @Column
    private double add_sal;

    @Column
    private double mess_bill;

    @Column
    private Long dacatid;

    @Column
    private String salarytype;
    
    @Column
    private String status;
    
    @Column
    private Long auby;
    @Column
    private String aust;
    
    @Column(name = "approved_on")
    private Date approvedOn;
    @Column(name = "authorized_on")
    private Date authorizedOn;
    @Column(name = "verified_on")
    private Date verifiedOn;
    

    public SalaryTransaction(EpayMasterNew epay) {

        this.conid = epay.getEmpcode();
        this.contable = epay.getConnect_table();
        this.designation = epay.getDesignation();
        this.jdate = epay.getJdate();
        this.epfno = epay.getEpfno();
        this.esino = epay.getEsino().toString();
        this.did = epay.getDid();
        this.cid = epay.getCompany();
        this.tds = epay.getTds();
        this.eepf1 = epay.getEpf1();
        this.eepf2 = epay.getEpf2();
        this.eesi = epay.getEesi();
        this.desi = epay.getDesiper();
        this.bankacno = epay.getBankacno().toString();
        this.bank = epay.getBankcode();
        this.sex = epay.getSex();
        this.dacatid = epay.getDacatid();

    }


    public SalaryTransaction(DepayMaster epay) {
        DateUtil dateUtil = new DateUtil();
        this.conid = epay.getEmpcode();
        this.contable = "emastdaily";
        if (epay.getDesignation() != null && epay.getDesignation() != "") {
            this.designation = epay.getDesignation();
        } else {
            this.designation = epay.getNatWork();
        }
        this.jdate = epay.getJdate();
        this.epfno = epay.getEpfno();
        this.esino = epay.getEsino().toString();
        this.did = epay.getDid();
        this.bid = epay.getBid();
        this.cid = epay.getCid();
        this.basic = epay.getBasic();
        this.da = epay.getDa();
        this.tds = epay.getTds();
        this.eepf1 = epay.getEepf1per();
        this.eepf2 = epay.getEepf2per();
        this.eesi = epay.getEsiceil();
        this.depf = epay.getDepfper();
        this.desi = epay.getDesiper();
        this.bankacno = epay.getBankacno().toString();
        this.bank = epay.getBank();
        this.sex = epay.getSex();


    }
    
 public SalaryTransaction(SalaryTransactionDto salaryTransaction) {

    	
        DateUtil dateUtil = new DateUtil();

      
        this.trnyear = salaryTransaction.getTrnyear();
        this.trnmonth = salaryTransaction.getTrnmonth();
        this.trnweekid = salaryTransaction.getTrnweekid();
        this.trndate = dateUtil.getStringTODateWk(salaryTransaction.getTrndate());
        this.trnid = salaryTransaction.getTrnid();
       
        this.conid = salaryTransaction.getEmpId();
        this.contable = salaryTransaction.getContable();
        this.scode = salaryTransaction.getScode();
        this.designation = salaryTransaction.getDesignation();
        this.jdate = dateUtil.getStringTODateWk(salaryTransaction.getJdate());
        this.epfno = salaryTransaction.getEpfno();
        this.esino = salaryTransaction.getEsino();
        this.did = salaryTransaction.getDeptId();
        this.bid = salaryTransaction.getBranch();
        this.cid = salaryTransaction.getCompany();
        this.fmperiod = dateUtil.getStringTODateWk(salaryTransaction.getFmperiod());
        this.toperiod = dateUtil.getStringTODateWk(salaryTransaction.getToperiod());
        this.mnodays = salaryTransaction.getMnodays();
        this.nowdays = salaryTransaction.getNowdays();
        this.tds = salaryTransaction.getTds();
        this.total = salaryTransaction.getTotal();
        this.eepf1 = salaryTransaction.getEepf1();
        this.eepf2 = salaryTransaction.getEepf2();
        this.eepftotal = salaryTransaction.getEepftotal();
        this.eesi = salaryTransaction.getEesi();
        this.depf = salaryTransaction.getDepf();
        this.desi = salaryTransaction.getDesi();
        this.tsalary = salaryTransaction.getTsalary();
        this.tdeduction = salaryTransaction.getTdeduction();
        this.netsalary = salaryTransaction.getNetsalary();
        this.advance = salaryTransaction.getAdvance();
        this.balance = salaryTransaction.getBalance();
        // this.cby = salaryTransaction.getCreatedBy();
        //this.cst = salaryTransaction.getCst();
        //this.vby = salaryTransaction.getVby();
        //this.vst = salaryTransaction.getVst();
        //this.aby = salaryTransaction.getAby();
        //this.ast = salaryTransaction.getAst();
        this.bankacno = salaryTransaction.getBankacno();
        this.bank = salaryTransaction.getBank();
        this.sex = salaryTransaction.getSex();
        this.gwa = salaryTransaction.getGwa();
        this.allowance = salaryTransaction.getAllowance();
        this.conveyance = salaryTransaction.getConveyance();
        this.hra = salaryTransaction.getHra();
        this.basic = salaryTransaction.getBasic();
        this.da = salaryTransaction.getDa();
        this.wa = salaryTransaction.getWa();
        //C7 = c7;
        //C8 = c8;
        //C9 = c9;
        //C10 = c10;
        this.leaveHolidayBasic = salaryTransaction.getLeaveHolidayBasic();
        this.leaveHolidayDA = salaryTransaction.getLeaveHolidayDA();
        //this.nowday1 = salaryTransaction.getNowday1();
        this.vtype = salaryTransaction.getVtype();
        this.oeepf1 = salaryTransaction.getOeepf1();
        this.oeepf2 = salaryTransaction.getOeepf2();
        this.oeepftotal = salaryTransaction.getOeepftotal();
        this.oeesi = salaryTransaction.getOeesi();
        this.odepf = salaryTransaction.getDepf();
        this.odesi = salaryTransaction.getOdesi();
        this.diffeepf1 = salaryTransaction.getDiffeepf1();
        this.diffeepf2 = salaryTransaction.getDiffeepf2();
        this.diffeepftotal = salaryTransaction.getDiffeepftotal();
        this.diffeesi = salaryTransaction.getDiffeesi();
        this.diffdepf = salaryTransaction.getDiffdepf();
        this.diffdesi = salaryTransaction.getDiffdesi();
        this.otherDeduction = salaryTransaction.getOtherDeduction();
        this.uniformReimbursment = salaryTransaction.getUniformReimbursment();
        this.uniformReimbursement = salaryTransaction.getUniformReimbursement();
        /*this.CL_availed = salaryTransaction.getId();
        this.SL_availed = salaryTransaction.getId();
        this.EL_availed = salaryTransaction.getId();
        M_availed =salaryTransaction.getId();
        this.CL_atCredit = salaryTransaction.getId();
        this.SL_atCredit = salaryTransaction.getId();
        this.EL_atCredit = salaryTransaction.getId();
         M_atCredit =salaryTransaction.getId();
        this.refNoWithDate = salaryTransaction.getId();*/
      
//        this.add_sal = salaryTransaction.getId();
        this.ptAmount = salaryTransaction.getPtAmount();
        this.dacatid=salaryTransaction.getDacatid();
    }
}
