package com.aria.dto;


import com.aria.model.SalaryTransaction;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class SalaryTransactionDto {


    private Long id;

    private String trnyear;

    private int trnmonth;

    private Long trnweekid;

    private String trndate;

    private Long trnid;

    private String trnno;

    private Long empId;

    private String empName;

    private String contable;

    private String scode;

    private String designation;

    private String jdate;

    private String epfno;

    private String esino;

    private Long deptId;

    private Long branch;

    private Long company;


    private String fmperiod;


    private String toperiod;

    private String mnodays;

    private double nowdays;

    private double tds;

    private double total;

    private double eepf1;

    private double eepf2;

    private double eepftotal;

    private double eesi;

    private double depf;

    private double desi;

    private double tsalary;

    private double tdeduction;

    private double netsalary;

    private double advance;

    private double balance;

    private Long createdBy;

    private String createdByName;

   /* private String cst;

    private Long vby;

    private String vst;

    private Long aby;

    private String ast;*/

    private String bankacno;

    private String bank;

    private String sex;

    private double gwa; // c1


    private double allowance; // c1  for daily wages

    private double conveyance; //c2

    private double hra; // c3

    private double basic; //c4

    private double da; //c5

    private double wa; //c6

    /*private double C7; //c7

    private double C8;

    private double C9;

    private double C10;*/

    private double leaveHolidayBasic;

    private double leaveHolidayDA;

    //private double nowday1;

    private String vtype;

    private double oeepf1;

    private double oeepf2;

    private double oeepftotal;

    private double oeesi;

    private double odepf;


    private double odesi;


    private double diffeepf1;


    private double diffeepf2;


    private double diffeepftotal;


    private double diffeesi;


    private double diffdepf;


    private double diffdesi;


    private double otherDeduction;


    private double uniformReimbursment;


    private double uniformReimbursement;


   /* private double CL_availed;


    private double SL_availed;


    private double EL_availed;


    private double M_availed;


    private double CL_atCredit;


    private double SL_atCredit;


    private double EL_atCredit;


    private double M_atCredit;


    private double refNoWithDate;*/


    private double ptAmount;


    private double add_sal;
    
    private String category;

    private Long dacatid;


    public SalaryTransactionDto(SalaryTransaction salaryTransaction) {

    	
        DateUtil dateUtil = new DateUtil();

//        this.id = salaryTransaction.getId();
        this.trnyear = salaryTransaction.getTrnyear();
        this.trnmonth = salaryTransaction.getTrnmonth();
        this.trnweekid = salaryTransaction.getTrnweekid();
        this.trndate = dateUtil.getDateTOStringWk(salaryTransaction.getTrndate());
        this.trnid = salaryTransaction.getTrnid();
        this.trnno = salaryTransaction.getTrnno();
        this.empId = salaryTransaction.getConid();
        this.contable = salaryTransaction.getContable();
        this.scode = salaryTransaction.getScode();
        this.designation = salaryTransaction.getDesignation();
        this.jdate = dateUtil.getDateTOStringWk(salaryTransaction.getJdate());
        this.epfno = salaryTransaction.getEpfno();
        this.esino = salaryTransaction.getEsino();
        this.deptId = salaryTransaction.getDid();
        this.branch = salaryTransaction.getBid();
        this.company = salaryTransaction.getCid();
        this.fmperiod = dateUtil.getDateTOStringWk(salaryTransaction.getFmperiod());
        this.toperiod = dateUtil.getDateTOStringWk(salaryTransaction.getToperiod());
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
        this.createdBy = salaryTransaction.getCby();
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
