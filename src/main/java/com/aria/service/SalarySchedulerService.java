package com.aria.service;


import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

@Service
public class SalarySchedulerService {


    @Autowired
    InstruLogRepository instruLogRepository;

    @Autowired
    LeaveDetailsRepository leaveDetailsRepository;

    @Autowired
    UserLeaveCountRepo leaveCountRepo;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmastDailyRepository emastDailyRepository;


    @Autowired
    TRNIndexRepo trnIndexRepo;

    @Autowired
    SalaryMasterHeadRepository salaryAllowanceMapRepository;

    @Autowired
    EpayMasterNewRepository epayMasterNewRepository;

    @Autowired
    SalaryTransactionRepo salaryTransactionRepo;

    @Autowired
    AssignDutyRepository assignDutyRepository;


    @Autowired
    ProfessionalTaxMasterRepository professionalTaxMasterRepository;

    @Autowired
    OtherDeductionRepo otherDeductionRepo;

    @Autowired
    UniformDeductionRepo uniformDeductionRepo;

    @Autowired
    LeaveSummaryRepository leaveSummaryRepository;

    @Autowired
    HolidayDetailsRepository holidayDetailsRepository;

    @Autowired
    DaySalaryRepo daySalaryRepo;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    DepartmentDetailsRepository departmentDetailsRepository;


    public ResponseEntity<?> listAll(String fromDate, String toDate, Long company, Long branch, String pageFlag, Pageable pageable) throws UsernameNotFoundException {

        Date start = null;
        Date end = null;
        if (fromDate != null) {
            start = dateUtil.getStringTODateWk(fromDate);
        }

        if (toDate != null) {
            end = dateUtil.getStringTODateWk(toDate);
        }

        if (pageFlag != null && pageFlag.equalsIgnoreCase("Y")) {

            Page<SalaryTransaction> list = salaryTransactionRepo.findAll(start, end, branch, company, pageable);
            if (!list.isEmpty()) {
                List<SalaryTransactionDto> result = list.toList().stream().map(data -> {
                    SalaryTransactionDto salaryDto = new SalaryTransactionDto(data);
                    return salaryDto;
                }).collect(Collectors.toList());
                PageDataDto<SalaryTransactionDto> pageData = new PageDataDto<>(result, list);
                return new ResponseEntity<PageDataDto<SalaryTransactionDto>>(pageData, HttpStatus.OK);
            } else {
                return new ResponseEntity<PageDataDto<SalaryTransactionDto>>(new PageDataDto(), HttpStatus.OK);
            }
        } else {
            List<SalaryTransaction> list = salaryTransactionRepo.findAll(start, end, branch, company);
            if (!list.isEmpty()) {
                List<SalaryTransactionDto> result = list.stream().map(data -> {
                    SalaryTransactionDto salaryDto = new SalaryTransactionDto(data);
                    return salaryDto;
                }).collect(Collectors.toList());
                return new ResponseEntity<List<SalaryTransactionDto>>(result, HttpStatus.OK);
            } else {
                return new ResponseEntity<List<SalaryTransactionDto>>(new ArrayList<>(), HttpStatus.OK);
            }
        }
    }


    // PrePare Staff Salary

    public ResponseEntity<?> prepareStaffSalary(String year, Long month, Long company, Long branch, String fmDate, String toDate, User loggedUser) {


        System.out.println("###################");

        Date date = dateUtil.getISTDate();

        System.out.println(" IST DATE == " + date);
        Long createdBy = null;

        if (loggedUser != null) {
            createdBy = loggedUser.getId();
        }

        System.out.println("###################");
        System.out.println("###################");

        Date start = null;
        Date end = null;
        String yearValue =null;
        String monthValue;
        int mValue=0;
        if (fmDate != null && toDate != null) {
            // ✅ Custom Date Range
            start = dateUtil.getStringTODateWk(fmDate);
            end = dateUtil.getStringTODateWk(toDate);
            LocalDate localStart = start.toInstant()
          	        .atZone(ZoneId.systemDefault())
          	        .toLocalDate();

          	 yearValue = String.valueOf(localStart.getYear());
          	 mValue = localStart.getMonthValue();
          	 if (mValue < 10) {
                 monthValue = "0" + mValue;
             } else {
                 monthValue = String.valueOf(mValue);
             }
        } else {
            // ✅ Year & Month Logic
            String monthStr = (month < 10) ? "0" + month : String.valueOf(month);

            String from = "01-" + monthStr + "-" + year;

            start = dateUtil.getStringTODateWk(from);
            end = dateUtil.getCalendarEndMonthDate(start);
            yearValue=year;
            if (month < 10) {
                monthValue = "0" + month;
            } else {
                monthValue = String.valueOf(month);
            }
        }
        System.out.println("endDate === " + end);
        System.out.println("year === " + year);
        System.out.println("month === " + month);
        
        
        
       

        Long days;
        int daysInMonth;

        if (fmDate != null && toDate != null) {
        	days = dateUtil.getDaysBetween(start, end); 
        	 daysInMonth = (days != null) ? days.intValue() : 0;
        } else {
        	daysInMonth = dateUtil.lastDayOfMonth(Integer.valueOf(year), month.intValue());
        }
       
        System.out.println("daysInMonth === " + daysInMonth);

        List<SalaryTransaction> list = new ArrayList<>();
      

        try {

            Optional<SalaryTransaction> salaryTransaction = salaryTransactionRepo.findBy(branch, company, start, end, Long.valueOf(1), "emaster");

            if (!salaryTransaction.isPresent()) {
                List<User> usersList = userRepository.findAllStaffs(company, branch);
                //List<User> usersList = userRepository.findAllStaffsTesting(company, branch);


                for (User user : usersList) {

                    double attendanceDays = 0.0;
                    double lossOfPayDays = 0.0;
                    double grossCalculationDays = 0.0;

                    

                    Optional<LeaveSummary> leaveSummary = leaveSummaryRepository.findBy(user.getId(), yearValue, monthValue);
                    if (leaveSummary.isPresent()) {
                        attendanceDays = leaveSummary.get().getWorkedDays();
                        lossOfPayDays = leaveSummary.get().getLossOfPayDays();
                        grossCalculationDays = 26.0 - lossOfPayDays;
                    }

                    Integer leaveDays = leaveDetailsRepository.getLeaveCountByUserIdAndLeaveType(user.getId(), start, end);

                    System.out.println("user Id === " + user.getId());

                    EpayMasterNew epayMaster = epayMasterNewRepository.findByStaff(user.getId());


                    double basic = 0.0;
                    double da = 0.0;
                    double wa = 0.0;
                    double hra = 0.0;
                    double gwa = 0.0;
                    double finalAllowance = 0.0;
                    double tds = 0.0;
                    double perdayBasic = 0.0;
                    double perDayDa = 0.0;
                    double addHolidayBasic = 0.0;
                    double addHolidayDa = 0.0;
                    double holidayBasic = 0.0;
                    double holidayDa = 0.0;
                    double leaveBasic = 0.0;
                    double leaveDa = 0.0;

                    double finalBasic = 0.0;
                    double finalDa = 0.0;
                    double finalWa = 0.0;
                    double finalHra = 0.0;
                    double finalGWA = 0.0;
                    double finalLeaveWagesBasic = 0.0;
                    double finalLeaveWagesDa = 0.0;

                    double addSal = 0.0;


                    if (epayMaster != null) {


                        double totalSalary = getPerDaySal(epayMaster);

                        double perDaySalary = totalSalary / 26;

                        double grossSalary = perDaySalary * grossCalculationDays;


                        perdayBasic = epayMaster.getBasic() / 26;

                        perDayDa = epayMaster.getDa() / 26;

                        gwa = epayMaster.getGwa(); // goodwork allowance
                        hra = epayMaster.getHra(); // hra
                        wa = epayMaster.getWa(); // wa


                        if (leaveSummary.isPresent()) {


                            if (leaveSummary.get().getLeaveHolidaySalaryDays() > 0) {

                                double holidaySalDays = 0.0;
                                double checkHolidaySalDays = 0.0;

                                if (leaveSummary.get().getLeaveHolidayAttendedDays() != null && leaveSummary.get().getLeaveHolidayAttendedDays() > 0) {


                                    holidaySalDays = leaveSummary.get().getLeaveHolidaySalaryDays() - leaveSummary.get().getLeaveHolidayAttendedDays();

                                    checkHolidaySalDays = leaveSummary.get().getLeaveHolidaySalaryDays() - leaveSummary.get().getLeaves();

                                    if (checkHolidaySalDays > 0) {

                                        addHolidayBasic = perdayBasic * checkHolidaySalDays;

                                        addHolidayDa = perDayDa * checkHolidaySalDays;
                                    }
                                }


                                if (holidaySalDays > 0) {

                                    holidayBasic = perdayBasic * holidaySalDays;

                                    holidayDa = perDayDa * holidaySalDays;

                                } else {

                                    holidaySalDays = leaveSummary.get().getLeaveHolidaySalaryDays();

                                    holidayBasic = perdayBasic * holidaySalDays;

                                    holidayDa = perDayDa * holidaySalDays;

                                }

                            }


                            double totalLeaveSaldays = 0.0;

                            if (leaveSummary.get().getLeaves() > 0.0) {
                                totalLeaveSaldays = leaveSummary.get().getEarnedLeaveDays();
                            }

                            if (totalLeaveSaldays > 0) {

                                leaveBasic = perdayBasic * totalLeaveSaldays;

                                leaveDa = perDayDa * totalLeaveSaldays;

                            }

                        }


                        basic = perdayBasic * attendanceDays;
                        da = perDayDa * attendanceDays;

                        if (addHolidayBasic > 0) {
                            finalBasic = basic + addHolidayBasic;
                        } else {
                            finalBasic = basic;
                        }

                        if (addHolidayDa > 0) {
                            finalDa = da + addHolidayDa;
                        } else {
                            finalDa = da;
                        }


                        finalLeaveWagesBasic = holidayBasic + leaveBasic;

                        finalLeaveWagesDa = holidayDa + leaveDa;


                        double finalLeaveWagesSal = finalLeaveWagesBasic + finalLeaveWagesDa;

                        double grossSalMinusBasic = 0.0;

                        if (grossSalary > finalDa) {
                            grossSalMinusBasic = grossSalary - basic;
                        } else {
                            finalBasic = grossSalary;
                        }

                        double grossSalMinusDa = 0.0;

                        if (grossSalMinusBasic > finalDa) {
                            grossSalMinusDa = grossSalMinusBasic - da;
                        } else {
                            finalDa = grossSalMinusBasic;
                        }


                        double grossSalMinusLeaveSal = 0.0;

                        if (grossSalMinusDa > finalLeaveWagesSal) {
                            grossSalMinusLeaveSal = grossSalMinusDa - finalLeaveWagesSal;
                        } else {
                            finalLeaveWagesSal = grossSalMinusDa;
                        }


                        double grossSalMinusWa = 0.0;

                        double grossSalMinusHRA = 0.0;

                        if (grossSalMinusLeaveSal > wa) {
                            grossSalMinusWa = grossSalMinusLeaveSal - wa;
                            finalWa = wa;
                        } else if (grossSalMinusLeaveSal > 0) {
                            finalGWA = grossSalMinusLeaveSal;
                        }

                        if (grossSalMinusWa > hra) {
                            grossSalMinusHRA = grossSalMinusWa - hra;
                            finalHra = hra;
                        } else if (grossSalMinusLeaveSal > 0) {
                            finalGWA = grossSalMinusWa;
                        }

                        if (grossSalMinusHRA > 0) {
                            finalGWA = grossSalMinusHRA;
                        }


                        tds = epayMaster.getTds();


                        SalaryTransaction transaction = new SalaryTransaction(epayMaster);

                        transaction.setSalarytype("STAFF");
                        transaction.setBid(user.getBranch_id());
                        transaction.setCid(user.getOrg_id());
                        transaction.setDacatid(user.getDacatid());

//                        transaction.setTrnno(trnNo);

                        transaction.setMnodays(String.valueOf(daysInMonth));

                        transaction.setNowdays(attendanceDays);
                        transaction.setFmperiod(start);
                        transaction.setToperiod(end);
                        transaction.setTrndate(date);
//                        transaction.setTrnmonth(mValue);
//                        transaction.setTrnyear(yearValue);

                        transaction.setCby(createdBy);

                        transaction.setBasic(Math.round(finalBasic));

                        transaction.setDa(Math.round(finalDa));

                       
                        transaction.setHra(Math.round(finalHra));

                        transaction.setWa(Math.round(finalWa));

                        transaction.setGwa(Math.round(finalGWA));

                        transaction.setLeaveHolidayBasic(Math.round(finalLeaveWagesBasic));

                        transaction.setLeaveHolidayDA(Math.round(finalLeaveWagesDa));

                        double epTotal = transaction.getEepf1() + transaction.getEepf2();

                        transaction.setEepftotal(Math.round(epTotal));


                        double totalSal = getTotalSalary(transaction);

                        transaction.setTotal(Math.floor(totalSal));
                        
                        double totalNetSal = totalSal+ transaction.getLeaveHolidayBasic() + transaction.getLeaveHolidayDA();

                        transaction.setTsalary(Math.floor(totalNetSal));

                        //deduction

                        double esi = 0.0;
                        double epf = 0.0;
                        double mess = 0.0;
                        double uniform = 0.0;
                        double otherDeduction = 0.0;
                        double professionalTax = 0.0;


                        if (mValue == 2 || mValue == 8) {

                            Optional<ProfessionalTaxMaster> professionalTaxMaster = professionalTaxMasterRepository.findBy(epayMaster.getTotal());
                            if (professionalTaxMaster.isPresent()) {
                                professionalTax = professionalTaxMaster.get().getPtAmount();
                                transaction.setPtAmount(professionalTax);
                            }
                        }
                        boolean isAprToSep = (mValue >= 4 && mValue <= 9);
                        int periodStartMonth = isAprToSep ? 4 : 10;

                        Optional<SalaryTransaction> esiCheck;

                        if (periodStartMonth == 4) {
                            esiCheck = salaryTransactionRepo.findEsiDone(
                                    epayMaster.getId(), yearValue, 4, 9,"emaster");
                        } else {                     	
                        	int yearCal = Integer.parseInt(yearValue);
                            Optional<SalaryTransaction> p1;
                            Optional<SalaryTransaction> p2;
                            if (mValue >= 1 && mValue <= 3) {
                                // Jan–Mar → previous year Oct–Dec
                                p1 = salaryTransactionRepo.findEsiDone(epayMaster.getId(), String.valueOf(yearCal - 1), 10, 12, "emaster");
                                p2 = salaryTransactionRepo.findEsiDone(epayMaster.getId(), yearValue, 1, 3, "emaster");
                            } else {
                                // Oct–Dec → same year
                                p1 = salaryTransactionRepo.findEsiDone(epayMaster.getId(), yearValue, 10, 12, "emaster");
                                p2 = salaryTransactionRepo.findEsiDone(epayMaster.getId(), String.valueOf(yearCal + 1), 1, 3, "emaster");
                            }
                            esiCheck = p1.isPresent() ? p1 : p2;
                        }

                        boolean isEligible = false;
                        double esiSalary = transaction.getTsalary() - transaction.getWa();
                        double esiBase = (esiSalary / attendanceDays) * 26;
                        if (esiCheck.isPresent()) {
                            isEligible = true;
                        } else if (esiBase <= 21000) {
                            isEligible = true;
                        }

                        double esiEmp = 0.0;
                        double esiComp = 0.0;

                        if (isEligible  && attendanceDays > 0) {
                           double c = esiSalary / 100;

                            esiEmp = Math.ceil(c * epayMaster.getDesiper());
                            esiComp = Math.ceil(c * epayMaster.getEesiper());
                        }

                        transaction.setDesi(esiEmp);
                        transaction.setEesi(esiComp);

                        transaction.setDepf(0.0);

                        double finalEpfAmount = getWagesEpf(transaction);

                        double epfSalary = transaction.getTsalary() - transaction.getHra();

                         double employeeEpf = 0.0;
                         double employerPf = 0.0;
                         double employerPension = 0.0;
                         int age = 0;
                         Optional<User> findById = userRepository.findById(user.getId());
                         if(findById.isPresent()){
                         Date dob =findById.get().getDateofbirth();                        
                         if (dob != null) {

                             LocalDate birthDate;

                             // Java 8 safe handling
                             if (dob instanceof java.sql.Date) {
                                 birthDate = ((java.sql.Date) dob).toLocalDate();
                             } else {
                                 birthDate = dob.toInstant()
                                         .atZone(ZoneId.systemDefault())
                                         .toLocalDate();
                             }

                             LocalDate today = LocalDate.now();
                             age = Period.between(birthDate, today).getYears();

                         }
                         }
                         // Age > 58
                         if ( age!= 0 && age > 58) {
                         	transaction.setEepf1(0.0);
                         	transaction.setEepf2(0.0);
                         	transaction.setDepf(0.0);
                         } else {

                         	boolean isExempt = "Y".equalsIgnoreCase(epayMaster.getEpfexem());

                         	if (epfSalary <= 15000) {
                         		isExempt = false;
                         	}

                         	if (!isExempt) {

                         		double epfBaseSalary = epfSalary > 15000 ? 15000 : epfSalary;

                         		employerPension = Math.round(epfBaseSalary * epayMaster.getEepf1per() / 100.0);
                         		employerPf = Math.round(epfBaseSalary * epayMaster.getEepf2per() / 100.0);
                         		employeeEpf = Math.round(epfSalary * epayMaster.getDepfper() / 100.0);
                         	}

                         	transaction.setEepf1(employerPension);
                         	transaction.setEepf2(employerPf);
                         	transaction.setDepf(employeeEpf);

                         	double total = employerPension + employerPf;
                         	transaction.setEepftotal(Math.round(total));
                         }

                        transaction.setTds(tds);


                        OtherDeductionData otherDeductionData = otherDeductionRepo.findBy(epayMaster.getId(), "emaster", start, end);
                        if (otherDeductionData != null) {
                            otherDeduction = otherDeductionData.getAmt();
                        }
                        transaction.setOtherDeduction(otherDeduction);

                        OtherDeductionData uniformDeduction = uniformDeductionRepo.findBy(epayMaster.getId(), "emaster", start, end);
                        if (uniformDeduction != null) {
                            uniform = uniformDeduction.getAmt();
                        }
                        transaction.setUniformReimbursement(uniform);


                        double deduction = getTotalDeduction(transaction, epayMaster);


                        transaction.setTdeduction(deduction);
                        double finalNetSal = totalNetSal - deduction;

                        transaction.setNetsalary(Math.floor(finalNetSal));
                       
//                        salaryTransactionRepo.save(transaction);
                        list.add(transaction);
                    }

                }

//                list = salaryTransactionRepo.findBy(start, end, branch, company, year, month, 1L, "emaster");
           

            } else {
            	return new ResponseEntity<>(
                        new Status("403", "salary already prepared for this month and year"),
                        HttpStatus.FORBIDDEN);
            }

            System.out.println("###################");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!list.isEmpty()) {
            List<SalaryTransactionDto> result = list.stream().map(data -> {
                SalaryTransactionDto salaryDto = new SalaryTransactionDto(data);
                Optional<User> employee = userRepository.findById(salaryDto.getEmpId());
                if (employee.isPresent()) {
                    salaryDto.setEmpName(employee.get().getEmployeename());
                }
                return salaryDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<SalaryTransactionDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<SalaryTransactionDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    // PrePare Staff Apprentice Salary

    public ResponseEntity<?> prepareStaffApprenticeSalary(String year, Long month, Long company, Long branch, String fmDate, String toDate, User loggedUser) {


        System.out.println("###################");

        Date date = dateUtil.getISTDate();

        System.out.println(" IST DATE == " + date);
        Long createdBy = null;

        if (loggedUser != null) {
            createdBy = loggedUser.getId();
        }

        System.out.println("###################");
        System.out.println("###################");

        Date start = null;
        Date end = null;
        String yearValue =null;
        String monthValue;
        int mValue=0;
        if (fmDate != null && toDate != null) {
            // ✅ Custom Date Range
            start = dateUtil.getStringTODateWk(fmDate);
            end = dateUtil.getStringTODateWk(toDate);
            LocalDate localStart = start.toInstant()
          	        .atZone(ZoneId.systemDefault())
          	        .toLocalDate();

          	 yearValue = String.valueOf(localStart.getYear());
          	 mValue = localStart.getMonthValue();
          	 if (mValue < 10) {
                 monthValue = "0" + month;
             } else {
                 monthValue = String.valueOf(month);
             }

        } else {
            // ✅ Year & Month Logic
            String monthStr = (month < 10) ? "0" + month : String.valueOf(month);

            String from = "01-" + monthStr + "-" + year;

            start = dateUtil.getStringTODateWk(from);
            end = dateUtil.getCalendarEndMonthDate(start);
            yearValue=year;
            if (month < 10) {
                monthValue = "0" + month;
            } else {
                monthValue = String.valueOf(month);
            }
        }
        System.out.println("endDate === " + end);
        System.out.println("year === " + year);
        System.out.println("month === " + month);

        Long days;
        int daysInMonth;

        if (fmDate != null && toDate != null) {
        	days = dateUtil.getDaysBetween(start, end); 
        	 daysInMonth = (days != null) ? days.intValue() : 0;
        } else {
        	daysInMonth = dateUtil.lastDayOfMonth(Integer.valueOf(year), month.intValue());
        }
       
        System.out.println("daysInMonth === " + daysInMonth);

        List<SalaryTransaction> list = new ArrayList<>();

        try {

            Optional<SalaryTransaction> salaryTransaction = salaryTransactionRepo.findBy(branch, company, start, end, Long.valueOf(6), "emaster");

            if (!salaryTransaction.isPresent()) {

                List<User> usersList = userRepository.findAllStaffApprentice(company, branch);


                for (User user : usersList) {

                    double attendanceDays = 0.0;                   

                    Optional<LeaveSummary> leaveSummary = leaveSummaryRepository.findBy(user.getId(), year, monthValue);
                    if (leaveSummary.isPresent()) {
                        attendanceDays = leaveSummary.get().getTsaldays();
                    }

                    System.out.println("user Id === " + user.getId());

                    EpayMasterNew epayMaster = epayMasterNewRepository.findByStaff(user.getId());


                    double basic = 0.0;
                    double da = 0.0;
                    double wa = 0.0;
                    double hra = 0.0;
                    double gwa = 0.0;
                    double finalAllowance = 0.0;
                    double tds = 0.0;
                    double finalLeaveWagesBasic = 0.0;
                    double finalLeaveWagesDa = 0.0;

                    double addSal = 0.0;


                    if (epayMaster != null) {


                        double perMonthSalary = epayMaster.getBasic();

                        double perDaySalary = perMonthSalary / 26;

                        double grossSalary = perDaySalary * attendanceDays;


                        SalaryTransaction transaction = new SalaryTransaction(epayMaster);

                        transaction.setSalarytype("STAFF_STIPEND");
                        transaction.setBid(user.getBranch_id());
                        transaction.setCid(user.getOrg_id());
                        transaction.setDacatid(user.getDacatid());

//                        transaction.setTrnno(trnNo);

                        transaction.setMnodays(String.valueOf(daysInMonth));

                        transaction.setNowdays(attendanceDays);
                        transaction.setFmperiod(start);
                        transaction.setToperiod(end);
                        transaction.setTrndate(date);
//                        transaction.setTrnmonth(month.intValue());
//                        transaction.setTrnyear(year);

                        transaction.setCby(createdBy);

                        transaction.setBasic(Math.round(grossSalary));

                        transaction.setDa(Math.round(da));

                        transaction.setCst("Created");
                        transaction.setStatus("Pending");
                        transaction.setHra(Math.round(hra));

                        transaction.setWa(Math.round(wa));

                        transaction.setGwa(Math.round(gwa));

                        transaction.setLeaveHolidayBasic(Math.round(finalLeaveWagesBasic));

                        transaction.setLeaveHolidayDA(Math.round(finalLeaveWagesDa));

                        double epTotal = transaction.getEepf1() + transaction.getEepf2();

                        transaction.setEepftotal(Math.round(epTotal));


                        double totalSal = getTotalSalary(transaction);

                        transaction.setTotal(Math.floor(totalSal));
                        double totalNetSal = totalSal + transaction.getLeaveHolidayBasic() + transaction.getLeaveHolidayDA();

                        transaction.setTsalary(Math.floor(totalNetSal));


                        //deduction

                        double esi = 0.0;
                        double epf = 0.0;
                        double mess = 0.0;
                        double uniform = 0.0;
                        double otherDeduction = 0.0;
                        double professionalTax = 0.0;


                        if (mValue == 2 || mValue == 8) {

                            Optional<ProfessionalTaxMaster> professionalTaxMaster = professionalTaxMasterRepository.findBy(epayMaster.getTotal());
                            if (professionalTaxMaster.isPresent()) {
                                professionalTax = professionalTaxMaster.get().getPtAmount();
                                transaction.setPtAmount(professionalTax);
                            }
                        }

                        boolean isAprToSep = (mValue >= 4 && mValue <= 9);
                        int periodStartMonth = isAprToSep ? 4 : 10;

                        Optional<SalaryTransaction> esiCheck;

                        if (periodStartMonth == 4) {
                            esiCheck = salaryTransactionRepo.findEsiDone(
                                    epayMaster.getId(), yearValue, 4, 9,"emaster");
                        } else {                     	
                        	int yearCal = Integer.parseInt(yearValue);
                            Optional<SalaryTransaction> p1;
                            Optional<SalaryTransaction> p2;
                            if (mValue >= 1 && mValue <= 3) {
                                // Jan–Mar → previous year Oct–Dec
                                p1 = salaryTransactionRepo.findEsiDone(epayMaster.getId(), String.valueOf(yearCal - 1), 10, 12, "emaster");
                                p2 = salaryTransactionRepo.findEsiDone(epayMaster.getId(), yearValue, 1, 3, "emaster");
                            } else {
                                // Oct–Dec → same year
                                p1 = salaryTransactionRepo.findEsiDone(epayMaster.getId(), yearValue, 10, 12, "emaster");
                                p2 = salaryTransactionRepo.findEsiDone(epayMaster.getId(), String.valueOf(yearCal + 1), 1, 3, "emaster");
                            }
                            esiCheck = p1.isPresent() ? p1 : p2;
                        }

                        boolean isEligible = false;
                        double esiSalary = transaction.getTsalary() - transaction.getWa();
                        double esiBase = (esiSalary / attendanceDays) * 26;
                        if (esiCheck.isPresent()) {
                            isEligible = true;
                        } else if (esiBase <= 21000) {
                            isEligible = true;
                        }

                        double esiEmp = 0.0;
                        double esiComp = 0.0;

                        if (isEligible  && attendanceDays > 0) {
                        	 double c = esiSalary / 100;

                             esiEmp = Math.ceil(c * epayMaster.getDesiper());
                             esiComp = Math.ceil(c * epayMaster.getEesiper());
                        }

                        transaction.setDesi(esiEmp);
                        transaction.setEesi(esiComp);

                        transaction.setDepf(0.0);
                        double finalEpfAmount = getWagesEpf(transaction);

                        double epfSalary = transaction.getTsalary() - transaction.getHra();

                         double employeeEpf = 0.0;
                         double employerPf = 0.0;
                         double employerPension = 0.0;
                         int age = 0;
                         Optional<User> findById = userRepository.findById(user.getId());
                         if(findById.isPresent()){
                         Date dob =findById.get().getDateofbirth();                        

                         if (dob != null) {

                             LocalDate birthDate;

                             // Java 8 safe handling
                             if (dob instanceof java.sql.Date) {
                                 birthDate = ((java.sql.Date) dob).toLocalDate();
                             } else {
                                 birthDate = dob.toInstant()
                                         .atZone(ZoneId.systemDefault())
                                         .toLocalDate();
                             }

                             LocalDate today = LocalDate.now();
                             age = Period.between(birthDate, today).getYears();

                         }
                         }
                         // Age > 58
                         if ( age!= 0 && age > 58) {
                         	transaction.setEepf1(0.0);
                         	transaction.setEepf2(0.0);
                         	transaction.setDepf(0.0);
                         } else {

                         	boolean isExempt = "Y".equalsIgnoreCase(epayMaster.getEpfexem());

                         	if (epfSalary <= 15000) {
                         		isExempt = false;
                         	}

                         	if (!isExempt) {

                         		double epfBaseSalary = epfSalary > 15000 ? 15000 : epfSalary;

                         		employerPension = Math.round(epfBaseSalary * epayMaster.getEepf1per() / 100.0);
                         		employerPf = Math.round(epfBaseSalary * epayMaster.getEepf2per() / 100.0);
                         		employeeEpf = Math.round(epfSalary * epayMaster.getDepfper() / 100.0);
                         	}

                         	transaction.setEepf1(employerPension);
                         	transaction.setEepf2(employerPf);
                         	transaction.setDepf(employeeEpf);

                         	double total = employerPension + employerPf;
                         	transaction.setEepftotal(Math.round(total));                         }


                        transaction.setTds(tds);


                        OtherDeductionData otherDeductionData = otherDeductionRepo.findBy(epayMaster.getId(), "emaster", start, end);
                        if (otherDeductionData != null) {
                            otherDeduction = otherDeductionData.getAmt();
                        }
                        transaction.setOtherDeduction(otherDeduction);

                        OtherDeductionData uniformDeduction = uniformDeductionRepo.findBy(epayMaster.getId(), "emaster", start, end);
                        if (uniformDeduction != null) {
                            uniform = uniformDeduction.getAmt();
                        }
                        transaction.setUniformReimbursement(uniform);


                        double deduction = getTotalDeduction(transaction, epayMaster);


                        transaction.setTdeduction(deduction);

                        double finalNetSal = totalNetSal - deduction;

                        transaction.setNetsalary(Math.floor(finalNetSal));

                        salaryTransactionRepo.save(transaction);
                    }

                }

                list = salaryTransactionRepo.findBy(start, end, branch, company, year, month, 6L, "emaster");


            } else {
            	return new ResponseEntity<>(
                        new Status("403", "salary already prepared for this month and year"),
                        HttpStatus.FORBIDDEN);
            }

            System.out.println("###################");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!list.isEmpty()) {
            List<SalaryTransactionDto> result = list.stream().map(data -> {
                SalaryTransactionDto salaryDto = new SalaryTransactionDto(data);
                Optional<User> employee = userRepository.findById(salaryDto.getEmpId());
                if (employee.isPresent()) {
                    salaryDto.setEmpName(employee.get().getEmployeename());
                }
                return salaryDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<SalaryTransactionDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<SalaryTransactionDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    // prePare Labour Salary

    public ResponseEntity<?> prepareLabourSalary(String year, Long month, Long company, Long branch, Long dept, String fmDate, String toDate, User loggedUser) {


        System.out.println("###################");

        Date date = dateUtil.getISTDate();

        System.out.println(" IST DATE == " + date);
        Long createdBy = null;

        if (loggedUser != null) {
            createdBy = loggedUser.getId();
        }

        System.out.println("###################");
        System.out.println("###################");

        Date start = null;
        Date end = null;
        String yearValue = null;
        int mValue=0;
        if (fmDate != null && toDate != null) {
            // ✅ Custom Date Range
            start = dateUtil.getStringTODateWk(fmDate);
            end = dateUtil.getStringTODateWk(toDate);
            LocalDate localStart = start.toInstant()
          	        .atZone(ZoneId.systemDefault())
          	        .toLocalDate();

          	 yearValue = String.valueOf(localStart.getYear());
          	 mValue = localStart.getMonthValue();
        } else {
            // ✅ Year & Month Logic
            String monthStr = (month < 10) ? "0" + month : String.valueOf(month);

            String from = "01-" + monthStr + "-" + year;

            start = dateUtil.getStringTODateWk(from);
            end = dateUtil.getCalendarEndMonthDate(start);
            yearValue=year;
            mValue=month.intValue();
        }
        System.out.println("endDate === " + end);
        System.out.println("year === " + year);
        System.out.println("month === " + month);

        Long days;
        int daysInMonth;

        if (fmDate != null && toDate != null) {
        	days = dateUtil.getDaysBetween(start, end); 
        	 daysInMonth = (days != null) ? days.intValue() : 0;
        } else {
        	daysInMonth = dateUtil.lastDayOfMonth(Integer.valueOf(year), month.intValue());
        }
       

        System.out.println("daysInMonth === " + daysInMonth);

        List<SalaryTransaction> list = new ArrayList<>();

        try {

            Optional<SalaryTransaction> salaryTransaction = salaryTransactionRepo.findBy(branch, company, start, end, 1L, "emastdaily");

            if (!salaryTransaction.isPresent()) {

                // Daily wages Salary or Contract wages

                List<AssignUserListForSal> emastDailyList = assignDutyRepository.findByAssignUserListForFactAct(start, end, branch, dept);


                if (emastDailyList != null && !emastDailyList.isEmpty()) {

                    for (AssignUserListForSal user : emastDailyList) {

                        System.out.println(user.getUserId());

                        EpayMasterNew epayMaster = epayMasterNewRepository.findByWages(user.getUserId());

                        DailyWagesSumSalary wagesSumSalary = daySalaryRepo.findBy(user.getUserId(), start, end);

                        Integer attendanceDays = assignDutyRepository.findByAssignUserListCount(user.getUserId(), start, end);

                        //SalaryMasterHead salaryMasterHead = salaryAllowanceMapRepository.findByDw("Y");

                        double basic = 0.0;
                        double da = 0.0;
                        double wa = 0.0;
                        double allowance = 0.0;
                        double gwa = 0.0;
                        double hra = 0.0;


                        if (epayMaster != null) {

                            int noOfLeaveSal = getHolidayDetails(epayMaster.getId(), 1, Integer.valueOf(yearValue), start, end);

                            attendanceDays = attendanceDays;

                            if (wagesSumSalary != null) {

                                basic = wagesSumSalary.getBasic();
                                da = wagesSumSalary.getDa();
                                allowance = wagesSumSalary.getAllowance();

                                if (allowance > 750) {
                                    gwa = allowance - 750;
                                    wa = 750.00;
                                } else {
                                    gwa = allowance;
                                }


                                SalaryTransaction transaction = new SalaryTransaction(epayMaster);

                                transaction.setSalarytype("WAGES");
                                transaction.setDid(dept);
                                if (user.getBranch() != null) {
                                    transaction.setBid(user.getBranch());
                                } else {
                                    transaction.setBid(branch);
                                }
                                if (user.getOrgId() != null) {
                                    transaction.setCid(user.getOrgId());
                                } else {
                                    transaction.setCid(company);
                                }


                                if (transaction.getDacatid() == 0) {
                                    transaction.setDacatid(1L);
                                }
//                                transaction.setTrnno(trnNo);

                                transaction.setBasic(Math.round(basic));
                                transaction.setDa(Math.round(da));
                                transaction.setWa(Math.round(wa));
                                transaction.setGwa(Math.round(gwa));

                                transaction.setHra(Math.round(hra));


                                /*double leaveBasic = getDWLeaveBasic(epayMaster.getId(), 1, Integer.valueOf(year), start, end);

                                transaction.setLeaveHolidayBasic(Math.round(leaveBasic));
                                double finalBasic = transaction.getBasic() + leaveBasic;
                                transaction.setBasic(Math.floor(finalBasic));

                                double leaveDa = getDWLeaveDa(epayMaster.getId(), 1, Integer.valueOf(year), start, end);

                                transaction.setLeaveHolidayDA(Math.floor(leaveDa));
                                double finalDa = transaction.getDa() + leaveDa;
                                transaction.setDa(Math.floor(finalDa));*/


                                double totalSal = getTotalSalary(transaction);

                                transaction.setTotal(Math.floor(totalSal));

                                transaction.setMnodays(String.valueOf(daysInMonth));
                                transaction.setNowdays(attendanceDays);
                                transaction.setFmperiod(start);
                                transaction.setToperiod(end);
                                transaction.setTrndate(date);
//                                transaction.setTrnmonth(month.intValue());
//                                transaction.setTrnyear(year);

                                transaction.setCby(createdBy);
                               
                                double epTotal = transaction.getEepf1() + transaction.getEepf2();

                                transaction.setEepftotal(Math.floor(epTotal));

                                double totalNetSalary = totalSal;

                                transaction.setTsalary(Math.floor(totalNetSalary));


                                //deduction

                                double esi = 0.0;
                                double epf = 0.0;
                                double mess = 0.0;
                                double uniform = 0.0;
                                double otherDeduction = 0.0;
                                double professionalTax = 0.0;
                                double tds = 0.0;


                                if (mValue == 2 || mValue == 8) {

                                    Optional<ProfessionalTaxMaster> professionalTaxMaster = professionalTaxMasterRepository.findBy(epayMaster.getTotal());
                                    if (professionalTaxMaster.isPresent()) {
                                        professionalTax = professionalTaxMaster.get().getPtAmount();
                                        transaction.setPtAmount(professionalTax);
                                    }
                                }
                                boolean isAprToSep = (mValue >= 4 && mValue <= 9);
                                int periodStartMonth = isAprToSep ? 4 : 10;

                                Optional<SalaryTransaction> esiCheck;

                                if (periodStartMonth == 4) {
                                    esiCheck = salaryTransactionRepo.findEsiDone(
                                            epayMaster.getId(), yearValue, 4, 9,"emastdaily");
                                } else {                     	
                                	int yearCal = Integer.parseInt(yearValue);
                                    Optional<SalaryTransaction> p1;
                                    Optional<SalaryTransaction> p2;
                                    if (mValue >= 1 && mValue <= 3) {
                                        // Jan–Mar → previous year Oct–Dec
                                        p1 = salaryTransactionRepo.findEsiDone(epayMaster.getId(), String.valueOf(yearCal - 1), 10, 12, "emaster");
                                        p2 = salaryTransactionRepo.findEsiDone(epayMaster.getId(), yearValue, 1, 3, "emaster");
                                    } else {
                                        // Oct–Dec → same year
                                        p1 = salaryTransactionRepo.findEsiDone(epayMaster.getId(), yearValue, 10, 12, "emaster");
                                        p2 = salaryTransactionRepo.findEsiDone(epayMaster.getId(), String.valueOf(yearCal + 1), 1, 3, "emaster");
                                    }
                                    esiCheck = p1.isPresent() ? p1 : p2;
                                }
                                boolean isEligible = false;
                                double esiSalary = transaction.getTsalary() - transaction.getWa();
                                double esiBase = (esiSalary / attendanceDays) * 26;
                                if (esiCheck.isPresent()) {
                                    isEligible = true;
                                } else if (esiBase <= 21000) {
                                    isEligible = true;
                                }

                                double esiEmp = 0.0;
                                double esiComp = 0.0;

                                if (isEligible && attendanceDays != null && attendanceDays > 0) {                                    

                                    double c = esiSalary / 100;

                                    esiEmp = Math.ceil(c * epayMaster.getDesiper());
                                    esiComp = Math.ceil(c * epayMaster.getEesiper());
                                }

                                transaction.setDesi(esiEmp);
                                transaction.setEesi(esiComp);

                                transaction.setDepf(0.0);

                                double finalEpfAmount = getWagesEpf(transaction);

                               double epfSalary = transaction.getTsalary() - transaction.getHra();

                                double employeeEpf = 0.0;
                                double employerPf = 0.0;
                                double employerPension = 0.0;
                                int age = 0;
                                Optional<User> findById = userRepository.findById(user.getUserId());
//                                if(findById.isPresent()){
//                                Date dob =findById.get().getDateofbirth();                        

                                if (findById.isPresent()) {

                                    Date dob = findById.get().getDateofbirth();

                                    if (dob != null) {

                                        LocalDate birthDate;

                                        // Java 8 safe handling
                                        if (dob instanceof java.sql.Date) {
                                            birthDate = ((java.sql.Date) dob).toLocalDate();
                                        } else {
                                            birthDate = dob.toInstant()
                                                    .atZone(ZoneId.systemDefault())
                                                    .toLocalDate();
                                        }

                                        LocalDate today = LocalDate.now();
                                        age = Period.between(birthDate, today).getYears();

                                    }
                                }
                                // Age > 58
                                if ( age!= 0 && age > 58) {
                                	transaction.setEepf1(0.0);
                                	transaction.setEepf2(0.0);
                                	transaction.setDepf(0.0);
                                } else {

                                	boolean isExempt = "Y".equalsIgnoreCase(epayMaster.getEpfexem());

                                	if (epfSalary <= 15000) {
                                		isExempt = false;
                                	}

                                	if (!isExempt) {

                                		double epfBaseSalary = epfSalary > 15000 ? 15000 : epfSalary;

                                		employerPension = Math.round(epfBaseSalary * epayMaster.getEepf1per() / 100.0);
                                 		employerPf = Math.round(epfBaseSalary * epayMaster.getEepf2per() / 100.0);
                                 		employeeEpf = Math.round(epfSalary * epayMaster.getDepfper() / 100.0);
                                 	}

                                 	transaction.setEepf1(employerPension);
                                 	transaction.setEepf2(employerPf);
                                 	transaction.setDepf(employeeEpf);

                                 	double total = employerPension + employerPf;
                                 	transaction.setEepftotal(Math.round(total));
                                }

                                tds = epayMaster.getTds();

                                transaction.setTds(tds);

                                OtherDeductionData otherDeductionData = otherDeductionRepo.findBy(epayMaster.getId(), "emastdaily", start, end);
                                if (otherDeductionData != null) {
                                    otherDeduction = otherDeductionData.getAmt();
                                }
                                transaction.setOtherDeduction(otherDeduction);

                                OtherDeductionData uniformDeduction = uniformDeductionRepo.findBy(epayMaster.getId(), "emastdaily", start, end);
                                if (uniformDeduction != null) {
                                    uniform = uniformDeduction.getAmt();
                                }
                                transaction.setUniformReimbursement(uniform);

                                double deduction = professionalTax + esiEmp + employeeEpf + tds + otherDeduction + uniform;

                                double finalNetSal = transaction.getTsalary() - deduction;

                                transaction.setNetsalary(finalNetSal);
                                transaction.setTdeduction(deduction);
//                                salaryTransactionRepo.save(transaction);
                                list.add(transaction);
                            }
                        }
                    }
                }
//                list = salaryTransactionRepo.findBy(start, end, branch, company, year, month, 1L, "emastdaily");


            } else {
            	return new ResponseEntity<>(
                        new Status("403", "salary already prepared for this month and year"),
                        HttpStatus.FORBIDDEN);
            }

            System.out.println("###################");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!list.isEmpty()) {
            List<SalaryTransactionDto> result = list.stream().map(data -> {
                SalaryTransactionDto salaryDto = new SalaryTransactionDto(data);
                Optional<EmastDaily> employee = emastDailyRepository.findById(salaryDto.getEmpId());
                if (employee.isPresent()) {
                    salaryDto.setEmpName(employee.get().getName());
                }
                return salaryDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<SalaryTransactionDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<SalaryTransactionDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    // prePare Labour Salary Apprentice

    public ResponseEntity<?> prepareLabourApprenticeSalary(String year, Long month, Long company, Long branch, Long dept, String fmDate, String toDate, User loggedUser) {


        System.out.println("###################");

        Date date = dateUtil.getISTDate();

        System.out.println(" IST DATE == " + date);
        Long createdBy = null;

        if (loggedUser != null) {
            createdBy = loggedUser.getId();
        }

        System.out.println("###################");
        System.out.println("###################");

        Date start = null;
        Date end = null;
        String yearValue=null;
        int mValue =0;
        if (fmDate != null && toDate != null) {
            // ✅ Custom Date Range
            start = dateUtil.getStringTODateWk(fmDate);
            end = dateUtil.getStringTODateWk(toDate);
            LocalDate localStart = start.toInstant()
          	        .atZone(ZoneId.systemDefault())
          	        .toLocalDate();

          	 yearValue = String.valueOf(localStart.getYear());
          	 mValue = localStart.getMonthValue();
        } else {
            // ✅ Year & Month Logic
            String monthStr = (month < 10) ? "0" + month : String.valueOf(month);

            String from = "01-" + monthStr + "-" + year;

            start = dateUtil.getStringTODateWk(from);
            end = dateUtil.getCalendarEndMonthDate(start);
            yearValue=year;
            mValue=month.intValue();
        }
        System.out.println("endDate === " + end);
        System.out.println("year === " + year);
        System.out.println("month === " + month);

        Long days;
        int daysInMonth;

        if (fmDate != null && toDate != null) {
        	days = dateUtil.getDaysBetween(start, end); 
        	 daysInMonth = (days != null) ? days.intValue() : 0;
        } else {
        	daysInMonth = dateUtil.lastDayOfMonth(Integer.valueOf(year), month.intValue());
        }
       

        System.out.println("daysInMonth === " + daysInMonth);

        List<SalaryTransaction> list = new ArrayList<>();

        try {

            Optional<SalaryTransaction> salaryTransaction = salaryTransactionRepo.findBy(branch, company, start, end, 6L, "emastdaily");

            if (!salaryTransaction.isPresent()) {
//
//                String trnNo = null;
//                Optional<TRNIndex> trnIndex = trnIndexRepo.findByPrefix("STIPEND");
//                if (trnIndex.isPresent()) {
//                    TRNIndex trnIndex1 = trnIndex.get();
//                    trnNo = trnIndex1.getPrefix() + "-" + trnIndex1.getVouno();
//                    Long vouno = trnIndex1.getVouno() + 1;
//                    trnIndex1.setVouno(vouno);
//                    trnIndexRepo.save(trnIndex1);
//                }

                // Daily wages Salary or Contract wages

                List<AssignUserListForSal> emastDailyList = assignDutyRepository.findByAssignUserListForApprenticeAct(start, end, branch, dept);


                if (emastDailyList != null && !emastDailyList.isEmpty())

                    for (AssignUserListForSal user : emastDailyList) {

                        System.out.println(user.getUserId());

                        EpayMasterNew epayMaster = epayMasterNewRepository.findByWages(user.getUserId());

                        DailyWagesSumSalary wagesSumSalary = daySalaryRepo.findBy(user.getUserId(), start, end);

                        Integer attendanceDays = assignDutyRepository.findByAssignUserListCount(user.getUserId(), start, end);


                        double basic = 0.0;
                        double da = 0.0;
                        double wa = 0.0;
                        double allowance = 0.0;
                        double gwa = 0.0;
                        double hra = 0.0;


                        if (epayMaster != null) {

                            int noOfLeaveSal = getHolidayDetails(epayMaster.getId(), 1, Integer.valueOf(yearValue), start, end);

                            attendanceDays = attendanceDays;

                            if (wagesSumSalary != null) {

                                basic = wagesSumSalary.getBasic();
                                da = wagesSumSalary.getDa();
                                allowance = wagesSumSalary.getAllowance();

                                if (allowance > 750) {
                                    gwa = allowance - 750;
                                    wa = 750.00;
                                } else {
                                    gwa = allowance;
                                }


                                SalaryTransaction transaction = new SalaryTransaction(epayMaster);

                                transaction.setSalarytype("WAGES_STIPEND");
                                if (user.getBranch() != null) {
                                    transaction.setBid(user.getBranch());
                                } else {
                                    transaction.setBid(branch);
                                }
                                if (user.getOrgId() != null) {
                                    transaction.setCid(user.getOrgId());
                                } else {
                                    transaction.setCid(company);
                                }
                                transaction.setDid(dept);
                                if (transaction.getDacatid() == 0) {
                                    transaction.setDacatid(6L);
                                }
//                                transaction.setTrnno(trnNo);

                                transaction.setBasic(Math.round(basic));
                                transaction.setDa(Math.round(da));
                                transaction.setWa(Math.round(wa));
                                transaction.setGwa(Math.round(gwa));

                                transaction.setHra(Math.round(hra));


                                /*double leaveBasic = getDWLeaveBasic(epayMaster.getId(), 1, Integer.valueOf(year), start, end);

                                transaction.setLeaveHolidayBasic(Math.floor(leaveBasic));
                                double finalBasic = transaction.getBasic() + leaveBasic;
                                transaction.setBasic(Math.floor(finalBasic));

                                double leaveDa = getDWLeaveDa(epayMaster.getId(), 1, Integer.valueOf(year), start, end);

                                transaction.setLeaveHolidayDA(Math.floor(leaveDa));
                                double finalDa = transaction.getDa() + leaveDa;
                                transaction.setDa(Math.floor(finalDa));*/


                                double totalSal = getTotalSalary(transaction);

                                transaction.setTotal(Math.round(totalSal));

                                transaction.setMnodays(String.valueOf(daysInMonth));
                                transaction.setNowdays(attendanceDays);
                                transaction.setFmperiod(start);
                                transaction.setToperiod(end);
                                transaction.setTrndate(date);
//                                transaction.setTrnmonth(month.intValue());
//                                transaction.setTrnyear(year);

                                transaction.setCby(createdBy);
                               
                                double epTotal = transaction.getEepf1() + transaction.getEepf2();

                                transaction.setEepftotal(Math.round(epTotal));

                                transaction.setTsalary(Math.round(totalSal));

                                //deduction

                                //deduction

                                double esi = 0.0;
                                double epf = 0.0;
                                double mess = 0.0;
                                double uniform = 0.0;
                                double otherDeduction = 0.0;
                                double professionalTax = 0.0;
                                double tds = 0.0;
                                double depf = 0.0;


                                if (mValue == 2 || mValue == 8) {

                                    Optional<ProfessionalTaxMaster> professionalTaxMaster = professionalTaxMasterRepository.findBy(epayMaster.getTotal());
                                    if (professionalTaxMaster.isPresent()) {
                                        professionalTax = professionalTaxMaster.get().getPtAmount();
                                        transaction.setPtAmount(professionalTax);
                                    }
                                }

                                boolean isAprToSep = (mValue >= 4 && mValue <= 9);
                                int periodStartMonth = isAprToSep ? 4 : 10;

                                Optional<SalaryTransaction> esiCheck;

                                if (periodStartMonth == 4) {
                                    esiCheck = salaryTransactionRepo.findEsiDone(
                                            epayMaster.getId(), yearValue, 4, 9,"emastdaily");
                                } else {                     	
                                	int yearCal = Integer.parseInt(yearValue);
                                    Optional<SalaryTransaction> p1;
                                    Optional<SalaryTransaction> p2;
                                    if (mValue >= 1 && mValue <= 3) {
                                        // Jan–Mar → previous year Oct–Dec
                                        p1 = salaryTransactionRepo.findEsiDone(epayMaster.getId(), String.valueOf(yearCal - 1), 10, 12, "emaster");
                                        p2 = salaryTransactionRepo.findEsiDone(epayMaster.getId(), yearValue, 1, 3, "emaster");
                                    } else {
                                        // Oct–Dec → same year
                                        p1 = salaryTransactionRepo.findEsiDone(epayMaster.getId(), yearValue, 10, 12, "emaster");
                                        p2 = salaryTransactionRepo.findEsiDone(epayMaster.getId(), String.valueOf(yearCal + 1), 1, 3, "emaster");
                                    }
                                    esiCheck = p1.isPresent() ? p1 : p2;
                                }

                                boolean isEligible = false;
                                double esiSalary = transaction.getTsalary() - transaction.getWa();
                                double esiBase = (esiSalary / attendanceDays) * 26;
                                if (esiCheck.isPresent()) {
                                    isEligible = true;
                                } else if (esiBase <= 21000) {
                                    isEligible = true;
                                }

                                double esiEmp = 0.0;
                                double esiComp = 0.0;

                                if (isEligible && attendanceDays != null && attendanceDays > 0) {
                                    double c =  esiSalary/ 100;
                                    esiEmp = Math.ceil(c * epayMaster.getDesiper());
                                    esiComp = Math.ceil(c * epayMaster.getEesiper());
                                }

                                transaction.setDesi(esiEmp);
                                transaction.setEesi(esiComp);

                                transaction.setDepf(0.0);

                                double finalEpfAmount = getWagesEpf(transaction);

                                double epfSalary = transaction.getTsalary() - transaction.getHra();

                                 double employeeEpf = 0.0;
                                 double employerPf = 0.0;
                                 double employerPension = 0.0;
                                 int age = 0;
                                 Optional<User> findById = userRepository.findById(user.getUserId());
                                 if(findById.isPresent()){
                                 Date dob =findById.get().getDateofbirth();                        

                                 if (dob != null) {

                                     LocalDate birthDate;

                                     // Java 8 safe handling
                                     if (dob instanceof java.sql.Date) {
                                         birthDate = ((java.sql.Date) dob).toLocalDate();
                                     } else {
                                         birthDate = dob.toInstant()
                                                 .atZone(ZoneId.systemDefault())
                                                 .toLocalDate();
                                     }

                                     LocalDate today = LocalDate.now();
                                     age = Period.between(birthDate, today).getYears();

                                 }
                                 }
                                 // Age > 58
                                 if ( age!= 0 && age > 58) {
                                 	transaction.setEepf1(0.0);
                                 	transaction.setEepf2(0.0);
                                 	transaction.setDepf(0.0);
                                 } else {

                                 	boolean isExempt = "Y".equalsIgnoreCase(epayMaster.getEpfexem());

                                 	if (epfSalary <= 15000) {
                                 		isExempt = false;
                                 	}

                                 	if (!isExempt) {

                                 		double epfBaseSalary = epfSalary > 15000 ? 15000 : epfSalary;

                                 		employerPension = Math.round(epfBaseSalary * epayMaster.getEepf1per() / 100.0);
                                 		employerPf = Math.round(epfBaseSalary * epayMaster.getEepf2per() / 100.0);
                                 		employeeEpf = Math.round(epfSalary * epayMaster.getDepfper() / 100.0);
                                 	}

                                 	transaction.setEepf1(employerPension);
                                 	transaction.setEepf2(employerPf);
                                 	transaction.setDepf(employeeEpf);

                                 	double total = employerPension + employerPf;
                                 	transaction.setEepftotal(Math.round(total));
                                 }

                                tds = epayMaster.getTds();
                                transaction.setTds(tds);

                                OtherDeductionData otherDeductionData = otherDeductionRepo.findBy(epayMaster.getId(), "emastdaily", start, end);
                                if (otherDeductionData != null) {
                                    otherDeduction = otherDeductionData.getAmt();
                                }
                                transaction.setOtherDeduction(otherDeduction);

                                OtherDeductionData uniformDeduction = uniformDeductionRepo.findBy(epayMaster.getId(), "emastdaily", start, end);
                                if (uniformDeduction != null) {
                                    uniform = uniformDeduction.getAmt();
                                }
                                transaction.setUniformReimbursement(uniform);

                                double deduction = professionalTax + esiEmp + employeeEpf + tds + otherDeduction + uniform;

                                double finalNetSal = transaction.getTsalary() - deduction;

                                transaction.setNetsalary(finalNetSal);
                                transaction.setTdeduction(deduction);
//                                salaryTransactionRepo.save(transaction);
                                list.add(transaction);

                            }
                        }
                    }

//                list = salaryTransactionRepo.findBy(start, end, branch, company, year, month, 6L, "emastdaily");


            } else {
            	return new ResponseEntity<>(
                        new Status("403", "salary already prepared for this month and year"),
                        HttpStatus.FORBIDDEN);
            }

            System.out.println("###################");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!list.isEmpty()) {
            List<SalaryTransactionDto> result = list.stream().map(data -> {
                SalaryTransactionDto salaryDto = new SalaryTransactionDto(data);
                Optional<EmastDaily> employee = emastDailyRepository.findById(salaryDto.getEmpId());
                if (employee.isPresent()) {
                    salaryDto.setEmpName(employee.get().getName());
                }
                return salaryDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<SalaryTransactionDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<SalaryTransactionDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    // prePare Labour Building maintenance Salary Apprentice

    public ResponseEntity<?> prepareBuildingMaintenanceLabourSalary(String year, Long month, Long company, Long branch, Long dept, String fromDate, String toDate, User loggedUser) {


        System.out.println("###################");

        Date date = dateUtil.getISTDate();

        System.out.println(" IST DATE == " + date);
        Long createdBy = null;

        if (loggedUser != null) {
            createdBy = loggedUser.getId();
        }

        System.out.println("###################");
        System.out.println("###################");

        System.out.println("fromDate === " + fromDate);
        System.out.println("toDate === " + toDate);

        Date start = dateUtil.getStringTODateWk(fromDate);
        Date end = dateUtil.getStringTODateWk(toDate);

        System.out.println("year === " + year);
        System.out.println("month === " + month);

        Long daysInMonth = null;
        Map<String, Long> daysInMonthValue = dateUtil.findDifference(start, end);
        if (daysInMonthValue.containsKey("D")) {
            daysInMonth = daysInMonthValue.get("D");
        }
        System.out.println("daysInMonth === " + daysInMonth);

        List<SalaryTransaction> list = new ArrayList<>();

        try {

            Optional<SalaryTransaction> salaryTransaction = salaryTransactionRepo.findBy(branch, company, year, month, "emastdaily", start, end);

            if (!salaryTransaction.isPresent()) {

                String trnNo = null;
                Optional<TRNIndex> trnIndex = trnIndexRepo.findByPrefix("STIPEND");
                if (trnIndex.isPresent()) {
                    TRNIndex trnIndex1 = trnIndex.get();
                    trnNo = trnIndex1.getPrefix() + "-" + trnIndex1.getVouno();
                    Long vouno = trnIndex1.getVouno() + 1;
                    trnIndex1.setVouno(vouno);
                    trnIndexRepo.save(trnIndex1);
                }

                // Daily wages Salary or Contract wages

                List<AssignUserListForSal> emastDailyList = assignDutyRepository.findByAssignUserListForApprenticeAct(start, end, branch, dept);


                if (emastDailyList != null && !emastDailyList.isEmpty())

                    for (AssignUserListForSal user : emastDailyList) {

                        System.out.println(user.getUserId());

                        EpayMasterNew epayMaster = epayMasterNewRepository.findByWages(user.getUserId());

                        DailyWagesSumSalary wagesSumSalary = daySalaryRepo.findBy(user.getUserId(), start, end);

                        Integer attendanceDays = assignDutyRepository.findByAssignUserListCount(user.getUserId(), start, end);


                        double basic = 0.0;
                        double da = 0.0;
                        double wa = 0.0;
                        double allowance = 0.0;
                        double gwa = 0.0;
                        double hra = 0.0;


                        if (epayMaster != null) {

                            int noOfLeaveSal = getHolidayDetails(epayMaster.getId(), 1, Integer.valueOf(year), start, end);

                            attendanceDays = attendanceDays;

                            if (wagesSumSalary != null) {

                                basic = wagesSumSalary.getBasic();
                                da = wagesSumSalary.getDa();
                                allowance = wagesSumSalary.getAllowance();

                                if (allowance > 750) {
                                    gwa = allowance - 750;
                                    wa = 750.00;
                                } else {
                                    gwa = allowance;
                                }


                                SalaryTransaction transaction = new SalaryTransaction(epayMaster);

                                transaction.setSalarytype("WAGES_BUILDING_MAINTENANCE");

                                if (user.getBranch() != null) {
                                    transaction.setBid(user.getBranch());
                                } else {
                                    transaction.setBid(branch);
                                }
                                if (user.getOrgId() != null) {
                                    transaction.setCid(user.getOrgId());
                                } else {
                                    transaction.setCid(company);
                                }

                                if (transaction.getDacatid() == 0) {
                                    transaction.setDacatid(6L);
                                }
                                transaction.setTrnno(trnNo);

                                transaction.setBasic(Math.round(basic));
                                transaction.setDa(Math.round(da));
                                transaction.setWa(Math.round(wa));
                                transaction.setGwa(Math.round(gwa));

                                transaction.setHra(Math.round(hra));


                                double totalSal = getTotalSalary(transaction);

                                transaction.setTotal(Math.round(totalSal));

                                transaction.setMnodays(String.valueOf(daysInMonth));
                                transaction.setNowdays(attendanceDays);
                                transaction.setFmperiod(start);
                                transaction.setToperiod(end);
                                transaction.setTrndate(date);
                                transaction.setTrnmonth(month.intValue());
                                transaction.setTrnyear(year);

                                transaction.setCby(createdBy);

                                double epTotal = transaction.getEepf1() + transaction.getEepf2();

                                transaction.setEepftotal(Math.round(epTotal));

                                transaction.setTsalary(Math.round(totalSal));

                                //deduction

                                double esi = 0.0;
                                double epf = 0.0;
                                double mess = 0.0;
                                double uniform = 0.0;
                                double otherDeduction = 0.0;
                                double professionalTax = 0.0;
                                double tds = 0.0;
                                double depf = 0.0;
                                transaction.setDesi(0.0);
                                /*

                                if (month == 2 || month == 8) {

                                    Optional<ProfessionalTaxMaster> professionalTaxMaster = professionalTaxMasterRepository.findBy(epayMaster.getTotal());
                                    if (professionalTaxMaster.isPresent()) {
                                        professionalTax = professionalTaxMaster.get().getPtAmount();
                                        transaction.setPtAmount(professionalTax);
                                    }
                                }


                                if (!epayMaster.getEsiexem().equalsIgnoreCase("Y") || epayMaster.getEsiexem().equalsIgnoreCase("")) {

                                    if (transaction.getNetsalary() <= 21000) {
                                        double finalEsiAmount = getWagesEsi(transaction);
                                        double cEsi = finalEsiAmount / 100;
                                        double esi1 = cEsi * epayMaster.getEesiper();
                                        double esi2 = cEsi * epayMaster.getDesiper();
                                        esi = esi1 + esi2;

                                        transaction.setDesi(Math.floor(esi));
                                        transaction.setEesi(Math.floor(esi2));
                                    }

                                }
                                */

                                transaction.setDepf(0.0);

                                /*double finalEpfAmount = getWagesEpf(transaction);

                                if (!epayMaster.getEpfexem().equalsIgnoreCase("Y") || epayMaster.getEpfexem().equalsIgnoreCase("")) {

                                    if (transaction.getTsalary() >= 15000) {

                                        double cEPF = finalEpfAmount / 100;
                                        double epf1 = cEPF * epayMaster.getEepf1per();
                                        double epf2 = cEPF * epayMaster.getEepf2per();
                                        double depf = cEPF * epayMaster.getDepfper();
                                        epf = depf;
                                        transaction.setDepf(depf);

                                    }
                                }*/


                                tds = epayMaster.getTds();
                                transaction.setTds(tds);

                                /*OtherDeductionData otherDeductionData = otherDeductionRepo.findBy(epayMaster.getId(), "emastdaily", start, end);
                                if (otherDeductionData != null) {
                                    otherDeduction = otherDeductionData.getAmt();
                                }
                                transaction.setOtherDeduction(otherDeduction);

                                OtherDeductionData uniformDeduction = uniformDeductionRepo.findBy(epayMaster.getId(), "emastdaily", start, end);
                                if (uniformDeduction != null) {
                                    uniform = uniformDeduction.getAmt();
                                }*/
                                transaction.setUniformReimbursement(uniform);

                                double deduction = professionalTax + esi + epf + tds + otherDeduction + uniform;

                                double finalNetSal = transaction.getTsalary() - deduction;

                                transaction.setNetsalary(finalNetSal);

                                salaryTransactionRepo.save(transaction);

                            }
                        }
                    }

                list = salaryTransactionRepo.findBy(start, end, branch, company, year, month, 6L, "emastdaily");


            } else {
                list = salaryTransactionRepo.findBy(start, end, branch, company, year, month, 6L, "emastdaily");
            }

            System.out.println("###################");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!list.isEmpty()) {
            List<SalaryTransactionDto> result = list.stream().map(data -> {
                SalaryTransactionDto salaryDto = new SalaryTransactionDto(data);
                Optional<EmastDaily> employee = emastDailyRepository.findById(salaryDto.getEmpId());
                if (employee.isPresent()) {
                    salaryDto.setEmpName(employee.get().getName());
                }
                return salaryDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<SalaryTransactionDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<SalaryTransactionDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    // prePare Labour Building maintenance Salary Apprentice

    public ResponseEntity<?> buildingMaintenanceLabourSalaryLastDate(Long company, Long branch, Long dept, User loggedUser) {


        try {

            Optional<SalaryTransaction> salaryTransaction = salaryTransactionRepo.findBy(company, branch, dept, "emastdaily", "WAGES_BUILDING_MAINTENANCE");
            if (salaryTransaction.isPresent()) {
                SalaryTransactionDto salaryTransactionDto = new SalaryTransactionDto(salaryTransaction.get());
                return new ResponseEntity<SalaryTransactionDto>(salaryTransactionDto, HttpStatus.OK);
            }
            return new ResponseEntity<SalaryTransactionDto>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<SalaryTransactionDto>(HttpStatus.NOT_FOUND);

    }


    public double getPerDaySal(EpayMasterNew transaction) {

        double basic = transaction.getBasic();
        double da = transaction.getDa();
        double wa = transaction.getWa();
        double gwa = transaction.getGwa();
        double ca = transaction.getCa();
        double hra = transaction.getHra();
        double allow = transaction.getAllowance();

        double total = basic + da + wa + gwa + ca + hra + allow;

        return total;
    }


    public double getPerDaySalForApprentice(EpayMasterNew transaction) {

        double basic = transaction.getBasic();

        return basic;


    }

    public double getStaffEsi(SalaryTransaction transaction) {

        double basic = 0.0;
        double da = 0.0;
        double wa = 0.0;
        double gwa = 0.0;
        double ca = 0.0;
        double hra = 0.0;
        double allow = 0.0;
        double total = 0.0;


        List<SalaryMasterHead> list = salaryAllowanceMapRepository.findAllByMwESIEligible();

        for (SalaryMasterHead salaryMasterHead : list) {

            if (salaryMasterHead.getName().equalsIgnoreCase("BASIC")) {

                basic = transaction.getBasic();
            } else if (salaryMasterHead.getName().equalsIgnoreCase("DA")) {
                da = transaction.getDa();
            } else if (salaryMasterHead.getName().equalsIgnoreCase("HRA")) {
                hra = transaction.getHra();
            } else if (salaryMasterHead.getName().equalsIgnoreCase("ALLOW")) {
                allow = transaction.getAllowance();
            } else if (salaryMasterHead.getName().equalsIgnoreCase("GWA")) {
                gwa = transaction.getGwa();
            }
        }


        total = basic + da + allow + hra + gwa;

        return total;


    }


    public double getWagesEsi(SalaryTransaction transaction) {

        double basic = 0.0;
        double da = 0.0;
        double wa = 0.0;
        double gwa = 0.0;
        double ca = 0.0;
        double hra = 0.0;
        double allow = 0.0;
        double total = 0.0;


        List<SalaryMasterHead> list = salaryAllowanceMapRepository.findAllByDwESIEligible();

        for (SalaryMasterHead salaryMasterHead : list) {

            if (salaryMasterHead.getName().equalsIgnoreCase("BASIC")) {

                basic = transaction.getBasic();
            } else if (salaryMasterHead.getName().equalsIgnoreCase("DA")) {
                da = transaction.getDa();
            } else if (salaryMasterHead.getName().equalsIgnoreCase("HRA")) {
                hra = transaction.getHra();
            } else if (salaryMasterHead.getName().equalsIgnoreCase("ALLOW")) {
                allow = transaction.getAllowance();
            } else if (salaryMasterHead.getName().equalsIgnoreCase("GWA")) {
                gwa = transaction.getGwa();
            }
        }


        total = basic + da + allow + hra + gwa;

        return total;


    }


    public double getStaffEpf(SalaryTransaction transaction) {

        double basic = 0.0;
        double da = 0.0;
        double wa = 0.0;
        double gwa = 0.0;
        double allow = 0.0;
        double total = 0.0;
        double leaveBasicDa = transaction.getLeaveHolidayBasic() + transaction.getLeaveHolidayDA();


        List<SalaryMasterHead> list = salaryAllowanceMapRepository.findAllByMwEPIEligible();

        if (!list.isEmpty()) {

            for (SalaryMasterHead salaryMasterHead : list) {

                if (salaryMasterHead.getName().equalsIgnoreCase("BASIC")) {

                    basic = transaction.getBasic();

                } else if (salaryMasterHead.getName().equalsIgnoreCase("DA")) {

                    da = transaction.getDa();

                } else if (salaryMasterHead.getName().equalsIgnoreCase("WA")) {

                    wa = transaction.getWa();

                } else if (salaryMasterHead.getName().equalsIgnoreCase("GWA")) {

                    gwa = transaction.getGwa();

                }
            }
        }


        total = basic + da + allow + wa + gwa + leaveBasicDa;

        return total;


    }


    public double getWagesEpf(SalaryTransaction transaction) {

        double basic = 0.0;
        double da = 0.0;
        double wa = 0.0;
        double gwa = 0.0;
        double ca = 0.0;
        double hra = 0.0;
        double allow = 0.0;
        double total = 0.0;


        List<SalaryMasterHead> list = salaryAllowanceMapRepository.findAllByMwEPIEligible();

        for (SalaryMasterHead salaryMasterHead : list) {

            if (salaryMasterHead.getName().equalsIgnoreCase("BASIC")) {

                basic = transaction.getBasic();

            } else if (salaryMasterHead.getName().equalsIgnoreCase("DA")) {

                da = transaction.getDa();

            } else if (salaryMasterHead.getName().equalsIgnoreCase("WA")) {

                wa = transaction.getWa();

            } else if (salaryMasterHead.getName().equalsIgnoreCase("GWA")) {

                gwa = transaction.getGwa();

            }
        }


        total = basic + da + allow + wa + gwa;

        return total;


    }


    public double getTDS(EpayMasterNew epayMaster, SalaryTransaction transaction) {

        double tdsPer = 0.0;

        double totalSal = epayMaster.getTotal() * 12;

        double checkTds = totalSal / 10000;


        if (checkTds >= 150) { // 15,00,000
            tdsPer = 30;
        } else if (checkTds >= 125) { // 12,50,000
            tdsPer = 25;
        } else if (checkTds >= 100) { // 10,00,000
            tdsPer = 20;
        } else if (checkTds >= 75) { // 7,50,000
            tdsPer = 15;
        } else if (checkTds >= 50) { // 5,00,000
            tdsPer = 10;
        } else if (checkTds >= 25) { // 2,50,000
            tdsPer = 5;
        }

        double tds = transaction.getNetsalary() * tdsPer;
        return tds;


    }


    public double getTotalSalary(SalaryTransaction transaction) {

        double totalSal = transaction.getBasic() + transaction.getDa() + transaction.getWa() +
                transaction.getGwa() + transaction.getConveyance() + transaction.getHra();
        System.out.println("totalSalary == " + totalSal);
        return totalSal;

    }

    public double getTotalDeduction(SalaryTransaction transaction, EpayMasterNew epayMasterNew) {

        double esi = 0.0;
        double epf = 0.0;
        double tds = 0.0;

        if (epayMasterNew.getEsiexem() != null && !epayMasterNew.getEsiexem().equalsIgnoreCase("Y")) {
            esi = transaction.getDesi();
        } else if (epayMasterNew.getEsiexem() == null) {
            esi = transaction.getDesi();
        }

        if (epayMasterNew.getEpfexem() != null && !epayMasterNew.getEpfexem().equalsIgnoreCase("Y")) {
            epf = transaction.getEepf1();
        } else if (epayMasterNew.getEpfexem() == null) {
            epf = transaction.getEepf1();
        }

        tds = transaction.getTds();

        double totalDeduction = esi + epf + tds + transaction.getPtAmount() + transaction.getOtherDeduction() + transaction.getUniformReimbursement();

        return totalDeduction;


    }

    public int getHolidayDetails(Long userId, int empType, int year, Date start, Date end) {

        int i = 0;
        List<Date> dateList = holidayDetailsRepository.findBy(String.valueOf(year), start, end);
        if (!dateList.isEmpty()) {
            for (Date date : dateList) {
                List<AssignDuty> assignDuties = assignDutyRepository.findBy(userId, empType, date);
                if (!assignDuties.isEmpty()) {
                    i = i + assignDuties.size();
                }
            }
        }
        return i;
    }


    public double getDWLeaveBasic(Long userId, int empType, int year, Date start, Date end) {

        double basic = 0.0;
        List<Date> dateList = holidayDetailsRepository.findBy(String.valueOf(year), start, end);
        if (!dateList.isEmpty()) {
            basic = daySalaryRepo.findByLeaveBasic(userId, dateList);
        }
        return basic;
    }

    public double getDWLeaveDa(Long userId, int empType, int year, Date start, Date end) {

        double da = 0.0;
        List<Date> dateList = holidayDetailsRepository.findBy(String.valueOf(year), start, end);
        if (!dateList.isEmpty()) {
            da = daySalaryRepo.findByLeaveDa(userId, dateList);
        }
        return da;
    }


    public ResponseEntity<?> salaryPreparationList(String category, String year, Long month, Long company, Long branch, String status, Pageable pageable) throws UsernameNotFoundException {

        String connectTable = null;
        Long daCatId = null;

        if (category != null) {
            if (category.equalsIgnoreCase("Staff")) {
                connectTable = "emaster";
                daCatId = 1L;
            } else if (category.equalsIgnoreCase("StaffApprentice")) {
                connectTable = "emaster";
                daCatId = 6L;
            } else if (category.equalsIgnoreCase("Wages")) {
                connectTable = "emastdaily";
                daCatId = 1L;
            } else if (category.equalsIgnoreCase("WagesApprentice")) {
                connectTable = "emastdaily";
                daCatId = 6L;
            }
        }

        Page<SalaryTransactionList> list = salaryTransactionRepo.findByList(branch, company, year, month, daCatId, connectTable,status, pageable);
        if (!list.isEmpty()) {
            List<SalaryTransactionListDTO> result = list.toList().stream().map(data -> {
                SalaryTransactionListDTO salaryDto = new SalaryTransactionListDTO(data);
                if(salaryDto.getConnectTable().equalsIgnoreCase("emastdaily")) {
                    Optional<DepartmentDetails> departmentDetails = departmentDetailsRepository.findById(salaryDto.getDeptId());
                    if (departmentDetails.isPresent()) {
                        salaryDto.setDeptName(departmentDetails.get().getName());
                    }
                }
                return salaryDto;
            }).collect(Collectors.toList());
            PageDataDto<SalaryTransactionListDTO> pageData = new PageDataDto<>(result, list);
            return new ResponseEntity<PageDataDto<SalaryTransactionListDTO>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<SalaryTransactionListDTO>>(new PageDataDto(), HttpStatus.OK);
        }

    }


    public ResponseEntity<?> viewPrepareSalaryDetailsList(String category, String year, Long month, Long company, Long branch, String trnNo, Pageable pageable) throws UsernameNotFoundException {

        String connectTable = null;
        Long daCatId = null;

        if (category != null) {
            if (category.equalsIgnoreCase("Staff")) {
                connectTable = "emaster";
                daCatId = 1L;
            } else if (category.equalsIgnoreCase("StaffApprentice")) {
                connectTable = "emaster";
                daCatId = 6L;
            } else if (category.equalsIgnoreCase("Wages")) {
                connectTable = "emastdaily";
                daCatId = 1L;
            } else if (category.equalsIgnoreCase("WagesApprentice")) {
                connectTable = "emastdaily";
                daCatId = 6L;
            }
        }
        Page<SalaryTransactionDetailList> list = null;

        if (connectTable.equalsIgnoreCase("emaster")) {
            list = salaryTransactionRepo.findByStaffDetailList(branch, company, year, month, daCatId, connectTable, trnNo, pageable);
        } else if (connectTable.equalsIgnoreCase("emastdaily")) {
            list = salaryTransactionRepo.findByWageDetailList(branch, company, year, month, daCatId, connectTable, trnNo, pageable);
        }
        if (!list.isEmpty()) {
            List<SalaryTransactionDetailListDTO> result = list.toList().stream().map(data -> new SalaryTransactionDetailListDTO(data)).collect(Collectors.toList());
            PageDataDto<SalaryTransactionDetailListDTO> pageData = new PageDataDto<>(result, list);
            return new ResponseEntity<PageDataDto<SalaryTransactionDetailListDTO>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<SalaryTransactionDetailListDTO>>(new PageDataDto(), HttpStatus.OK);
        }

    }


    public ResponseEntity<?> rejectPrepareSalaryList(String category, String year, Long month, Long company, Long branch, String trnNo) throws UsernameNotFoundException {

        String connectTable = null;
        Long daCatId = null;

        if (category != null) {
            if (category.equalsIgnoreCase("Staff")) {
                connectTable = "emaster";
                daCatId = 1L;
            } else if (category.equalsIgnoreCase("StaffApprentice")) {
                connectTable = "emaster";
                daCatId = 6L;
            } else if (category.equalsIgnoreCase("Wages")) {
                connectTable = "emastdaily";
                daCatId = 1L;
            } else if (category.equalsIgnoreCase("WagesApprentice")) {
                connectTable = "emastdaily";
                daCatId = 6L;
            }
        }

        List<SalaryTransaction> list = salaryTransactionRepo.findByRejectList(branch, company, year, month, daCatId, connectTable, trnNo);
        if (!list.isEmpty()) {
            salaryTransactionRepo.deleteAll(list);
            return new ResponseEntity<Status>(new Status("200", "Salary Rejected Successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "Salary Rejected Not Successfully!!!"), HttpStatus.OK);
        }

    }


    public ResponseEntity<?> printPrepareSalaryDetailsList(String category, String year, Long month, Long company, Long branch, String trnNo) throws UsernameNotFoundException {

        String connectTable = null;
        Long daCatId = null;

        if (category != null) {
            if (category.equalsIgnoreCase("Staff")) {
                connectTable = "emaster";
                daCatId = 1L;
            } else if (category.equalsIgnoreCase("StaffApprentice")) {
                connectTable = "emaster";
                daCatId = 6L;
            } else if (category.equalsIgnoreCase("Wages")) {
                connectTable = "emastdaily";
                daCatId = 1L;
            } else if (category.equalsIgnoreCase("WagesApprentice")) {
                connectTable = "emastdaily";
                daCatId = 6L;
            }
        }

        PrintSalaryTransactionDetails printSalaryTransactionDetails = new PrintSalaryTransactionDetails();

        if (branch != null) {
            Optional<Branch> optionalBranch = branchRepository.findById(branch);
            if (optionalBranch.isPresent()) {
                printSalaryTransactionDetails.setBranchId(optionalBranch.get().getId());
                printSalaryTransactionDetails.setBranchName(optionalBranch.get().getBranchName());
                printSalaryTransactionDetails.setBranchAddress(optionalBranch.get().getAddress());
                printSalaryTransactionDetails.setBranchManager(optionalBranch.get().getManager());
                printSalaryTransactionDetails.setRegNo(optionalBranch.get().getRegistrationno());
            }
        }
        if (company != null) {
            Optional<Organisation> optionalCompany = organisationRepository.findById(company);
            if (optionalCompany.isPresent()) {
                printSalaryTransactionDetails.setCompanyId(optionalCompany.get().getId());
                printSalaryTransactionDetails.setCompanyName(optionalCompany.get().getName());
                printSalaryTransactionDetails.setCompanyAddress(optionalCompany.get().getAddress());
            }
        }
        if (year != null) {
            printSalaryTransactionDetails.setYear(year);
        }
        if (month != null) {
            printSalaryTransactionDetails.setMonth(month);
        }

        int menCount = 0;
        int womenCount = 0;

        List<SalaryTransactionDetailListDTO> result = null;

        List<SalaryTransactionDetailList> list = new ArrayList<>();

        if (connectTable.equalsIgnoreCase("emaster")) {
            list = salaryTransactionRepo.findByStaffDetailList(branch, company, year, month, daCatId, connectTable, trnNo);
            menCount = salaryTransactionRepo.countValue(branch, company, year, month, daCatId, connectTable, "M", trnNo);
            womenCount = salaryTransactionRepo.countValue(branch, company, year, month, daCatId, connectTable, "F", trnNo);
        } else if (connectTable.equalsIgnoreCase("emastdaily")) {
            list = salaryTransactionRepo.findByWageDetailList(branch, company, year, month, daCatId, connectTable, trnNo);
            menCount = salaryTransactionRepo.wagesCountValue(branch, company, year, month, daCatId, connectTable, "M", trnNo);
            womenCount = salaryTransactionRepo.wagesCountValue(branch, company, year, month, daCatId, connectTable, "F", trnNo);

        }
        if (!list.isEmpty()) {

            result = list.stream().map(data -> new SalaryTransactionDetailListDTO(data)).collect(Collectors.toList());
        } else {
            result = new ArrayList<>();
        }

        printSalaryTransactionDetails.setSalaryTransactionDetailList(result);
        printSalaryTransactionDetails.setTotalEmployees(result.size());
        printSalaryTransactionDetails.setTotalMens(menCount);
        printSalaryTransactionDetails.setTotalWomens(womenCount);

        return new ResponseEntity<PrintSalaryTransactionDetails>(printSalaryTransactionDetails, HttpStatus.OK);

    }







    /*

    public ResponseEntity<?> saveSalaryScheduler(String fromDate, String toDate, User loggedUser) {

        System.out.println("###################");

        Date date = dateUtil.getISTDate();

        System.out.println(" IST DATE == " + date);
        Long createdBy = null;

        if (loggedUser != null) {
            createdBy = loggedUser.getId();
        }

        System.out.println("###################");
        System.out.println("###################");
        Date start = null;
        Date end = null;
        if (fromDate == null) {
            start = dateUtil.getStartMonthDay();
        } else {
            start = dateUtil.getStringTODateWk(fromDate);
        }

        if (toDate == null) {
            end = dateUtil.getEndMonthDay();
        } else {
            end = dateUtil.getStringTODateWk(toDate);
        }

        int daysInMonth, month, year;

        try {

            if (fromDate == null) {

                LocalDate currentDate = LocalDate.now();
                YearMonth currentMonth = YearMonth.from(currentDate);
                daysInMonth = currentMonth.lengthOfMonth();
                month = currentMonth.getMonthValue();
                year = currentMonth.getYear();

            } else {

                LocalDate currentDate = LocalDate.now();
                YearMonth currentMonth = YearMonth.from(currentDate);
                daysInMonth = currentMonth.lengthOfMonth();
                month = currentMonth.getMonthValue();
                year = currentMonth.getYear();
            }


            String trnNo = null;
            Optional<TRNIndex> trnIndex = trnIndexRepo.findByPrefix("WAGES");
            if (trnIndex.isPresent()) {
                TRNIndex trnIndex1 = trnIndex.get();
                trnNo = trnIndex1.getPrefix() + "-" + trnIndex1.getVouno();
                Long vouno = trnIndex1.getVouno() + 1;
                trnIndex1.setVouno(vouno);
                trnIndexRepo.save(trnIndex1);
            }


            List<User> usersList = userRepository.findAlls();


            for (User user : usersList) {

                double attendanceDays = 0.0;

                Optional<LeaveSummary> leaveSummary = leaveSummaryRepository.findBy(user.getId(), String.valueOf(year), String.valueOf(month));
                if (leaveSummary.isPresent()) {
                    attendanceDays = leaveSummary.get().getWorkedDays();
                }

                //Integer attendanceDays = instruLogRepository.getAttendanceDays(user.getBioid(), start, end);

                Integer leaveDays = leaveDetailsRepository.getLeaveCountByUserIdAndLeaveType(user.getId(), start, end);

                EpayMasterNew epayMaster = epayMasterNewRepository.findByStaff(user.getId());

                //SalaryMasterHead salaryMasterHead = salaryAllowanceMapRepository.findByMw("Y");


                double basic = 0.0;
                double da = 0.0;
                double wa = 0.0;
                double hra = 0.0;
                double gwa = 0.0;
                double finalAllowance = 0.0;
                double tds = 0.0;
                double perdayBasic = 0.0;
                double perDayDa = 0.0;

                if (epayMaster != null) {


                    hra = epayMaster.getHra(); // hra
                    wa = epayMaster.getWa(); // wa


                    if (attendanceDays >= 26) {

                        perdayBasic = epayMaster.getBasic() / 26;
                        perDayDa = epayMaster.getDa() / 26;

                        gwa = epayMaster.getGwa();  // goodWorkAllowance
                        basic = epayMaster.getBasic(); // basic
                        da = epayMaster.getDa();  // da


                    } else if (attendanceDays < 26) {

                        attendanceDays = attendanceDays;

                        perdayBasic = epayMaster.getBasic() / 26;
                        perDayDa = epayMaster.getDa() / 26;

                        double cGWA = epayMaster.getGwa();
                        double cWA = epayMaster.getWa();
                        double cHRA = epayMaster.getHra();

                        double cTotAll = cGWA + cWA + cHRA;

                        double perDayAllowance = cTotAll / 26;

                        tds = epayMaster.getTds();


                        finalAllowance = perDayAllowance * attendanceDays;

                        basic = perdayBasic * attendanceDays;
                        da = perDayDa * attendanceDays;

                        gwa = finalAllowance - (hra + wa);

                    }


                    SalaryTransaction transaction = new SalaryTransaction(epayMaster);

                    transaction.setTrnno(trnNo);

                    transaction.setMnodays(String.valueOf(daysInMonth));

                    transaction.setNowdays(attendanceDays);
                    transaction.setFmperiod(start);
                    transaction.setToperiod(end);
                    transaction.setTrndate(date);
                    transaction.setTrnmonth(month);
                    transaction.setTrnyear(String.valueOf(year));

                    transaction.setCby(createdBy);


                    transaction.setBasic(Math.floor(basic));

                    transaction.setDa(Math.floor(da));

                    transaction.setHra(Math.floor(hra));

                    transaction.setWa(Math.floor(wa));

                    transaction.setGwa(Math.floor(gwa));

                    double epTotal = transaction.getEepf1() + transaction.getEepf2();

                    transaction.setEepftotal(Math.floor(epTotal));


                    if (leaveSummary.isPresent()) {

                        if (leaveSummary.get().getLeaveHolidayAttendedDays() != null && leaveSummary.get().getLeaveHolidayAttendedDays() > 0) {

                            double leavebasic = perdayBasic * leaveSummary.get().getLeaveHolidayAttendedDays();
                            transaction.setLeaveHolidayBasic(leavebasic);

                            double finalBasic = transaction.getBasic() + leavebasic;
                            transaction.setBasic(Math.floor(finalBasic));


                            double leaveDa = perDayDa * leaveSummary.get().getLeaveHolidayAttendedDays();
                            transaction.setLeaveHolidayDA(leaveDa);
                            double finalDa = transaction.getDa() + leaveDa;
                            transaction.setDa(Math.floor(finalDa));

                            double detuctionGwa = leavebasic + leaveDa;

                            double fgwa = transaction.getGwa() - detuctionGwa;
                            if (fgwa < 0) {
                                transaction.setAdd_sal(fgwa);
                                transaction.setGwa(0.0);
                            } else {
                                transaction.setGwa(Math.round(fgwa));
                            }
                        }
                    }


                    double totalSal = getTotalSalary(transaction);

                    transaction.setTotal(Math.floor(totalSal));

                    transaction.setTsalary(Math.floor(totalSal));

                    */
    /* double totalNetSalary = totalSal + epTotal;

     *//*


                    //deduction

                    double esi = 0.0;
                    double epf = 0.0;
                    double mess = 0.0;
                    double uniform = 0.0;
                    double otherDeduction = 0.0;
                    double professionalTax = 0.0;


                    if (month == 2 || month == 8) {

                        Optional<ProfessionalTaxMaster> professionalTaxMaster = professionalTaxMasterRepository.findBy(epayMaster.getTotal());
                        if (professionalTaxMaster.isPresent()) {
                            professionalTax = professionalTaxMaster.get().getPtAmount();
                            transaction.setPtAmount(professionalTax);
                        }
                    }


                    if (!epayMaster.getEsiexem().equalsIgnoreCase("Y")) {

                        if (transaction.getNetsalary() <= 21000) {
                            double finalEsiAmount = getStaffEsi(transaction);
                            double cEsi = finalEsiAmount / 100;
                            double esi1 = cEsi * epayMaster.getEesiper();
                            double esi2 = cEsi * epayMaster.getDesiper();
                            esi = esi1 + esi2;
                            transaction.setDesi(Math.round(esi));
                        }

                    }
                    transaction.setDepf(0.0);
                    double finalEpfAmount = getStaffEpf(transaction);

                    if (!epayMaster.getEpfexem().equalsIgnoreCase("Y")) {

                        if (transaction.getNetsalary() >= 15000) {

                            double cEPF = finalEpfAmount / 100;
                            double epf1 = cEPF * epayMaster.getEepf1per();
                            double epf2 = cEPF * epayMaster.getEepf2per();

                            double depf = cEPF * epayMaster.getDepfper();
                            epf = depf;
                            transaction.setDepf(Math.round(depf));
                        }
                    }

                    if (transaction.getDepf() == 0.0) {
                        double cEPF = finalEpfAmount / 100;
                        double epf1 = cEPF * epayMaster.getEepf1per();
                        double epf2 = cEPF * epayMaster.getEepf2per();
                        double depf = cEPF * epayMaster.getDepfper();
                        epf = depf;
                        transaction.setDepf(Math.round(depf));
                    }


                    transaction.setTds(tds);


                    OtherDeductionData otherDeductionData = otherDeductionRepo.findBy(epayMaster.getId(), "emaster", start, end);
                    if (otherDeductionData != null) {
                        otherDeduction = otherDeductionData.getAmt();
                    }
                    transaction.setOtherDeduction(otherDeduction);

                    OtherDeductionData uniformDeduction = uniformDeductionRepo.findBy(epayMaster.getId(), "emaster", start, end);
                    if (uniformDeduction != null) {
                        uniform = uniformDeduction.getAmt();
                    }
                    transaction.setUniformReimbursement(uniform);


                    double deduction = getTotalDeduction(transaction, epayMaster);


                    transaction.setTdeduction(deduction);

                    double totalNetSal = transaction.getTotal() + transaction.getLeaveHolidayBasic() + transaction.getLeaveHolidayDA();

                    transaction.setTsalary(Math.floor(totalNetSal));


                    double finalNetSal = transaction.getTsalary() - deduction;

                    transaction.setNetsalary(finalNetSal);


                    salaryTransactionRepo.save(transaction);
                }

            }


            // Daily wages Salary or Contract wages

            List<AssignUserListForSal> emastDailyList = assignDutyRepository.findByAssignUserListForSal(start, end);


            for (AssignUserListForSal user : emastDailyList) {


                EpayMasterNew epayMaster = epayMasterNewRepository.findByWages(user.getUserId());

                DailyWagesSumSalary wagesSumSalary = daySalaryRepo.findBy(user.getUserId(), start, end);

                Integer attendanceDays = assignDutyRepository.findByAssignUserListCount(user.getUserId(), start, end);

                SalaryMasterHead salaryMasterHead = salaryAllowanceMapRepository.findByDw("Y");

                double basic = 0.0;
                double da = 0.0;
                double wa = 0.0;
                double allowance = 0.0;
                double gwa = 0.0;
                double hra = 0.0;
                double finalAllowance = 0.0;
                double perDayBasic = 0.0;
                double perDayDa = 0.0;
                double perDayAllowance = 0.0;


                if (epayMaster != null) {

                    int noOfLeaveSal = getHolidayDetails(epayMaster.getId(), 1, year, start, end);

                    attendanceDays = attendanceDays;

                    if (wagesSumSalary != null) {

                        basic = wagesSumSalary.getBasic();
                        da = wagesSumSalary.getDa();
                        allowance = wagesSumSalary.getAllowance();

                        if (allowance > 750) {
                            gwa = allowance - 750;
                            wa = 750.00;
                        } else {
                            gwa = allowance;
                        }


                        SalaryTransaction transaction = new SalaryTransaction(epayMaster);

                        transaction.setTrnno(trnNo);

                        transaction.setBasic(Math.floor(basic));
                        transaction.setDa(Math.floor(da));
                        transaction.setWa(Math.floor(wa));
                        transaction.setGwa(Math.floor(gwa));

                        transaction.setHra(Math.floor(hra));


                        double leaveBasic = getDWLeaveBasic(epayMaster.getId(), 1, year, start, end);

                        transaction.setLeaveHolidayBasic(Math.floor(leaveBasic));
                        double finalBasic = transaction.getBasic() + leaveBasic;
                        transaction.setBasic(Math.floor(finalBasic));

                        double leaveDa = getDWLeaveDa(epayMaster.getId(), 1, year, start, end);

                        transaction.setLeaveHolidayDA(Math.floor(leaveDa));
                        double finalDa = transaction.getDa() + leaveDa;
                        transaction.setDa(Math.floor(finalDa));


                        double totalSal = getTotalSalary(transaction);

                        transaction.setTotal(Math.floor(totalSal));

                        transaction.setMnodays(String.valueOf(daysInMonth));
                        transaction.setNowdays(attendanceDays);
                        transaction.setFmperiod(start);
                        transaction.setToperiod(end);
                        transaction.setTrndate(date);
                        transaction.setTrnmonth(month);
                        transaction.setTrnyear(String.valueOf(year));

                        transaction.setCby(createdBy);

                        double epTotal = transaction.getEepf1() + transaction.getEepf2();

                        transaction.setEepftotal(Math.floor(epTotal));

                        double totalNetSalary = totalSal;

                        transaction.setTsalary(Math.floor(totalNetSalary));

                        //deduction

                        //deduction

                        double esi = 0.0;
                        double epf = 0.0;
                        double mess = 0.0;
                        double uniform = 0.0;
                        double otherDeduction = 0.0;
                        double professionalTax = 0.0;
                        double tds = 0.0;


                        if (month == 2 || month == 8) {

                            Optional<ProfessionalTaxMaster> professionalTaxMaster = professionalTaxMasterRepository.findBy(epayMaster.getTotal());
                            if (professionalTaxMaster.isPresent()) {
                                professionalTax = professionalTaxMaster.get().getPtAmount();
                                transaction.setPtAmount(professionalTax);
                            }
                        }


                        if (!epayMaster.getEsiexem().equalsIgnoreCase("Y") || epayMaster.getEsiexem().equalsIgnoreCase("")) {

                            if (transaction.getNetsalary() <= 21000) {
                                double finalEsiAmount = getWagesEsi(transaction);
                                double cEsi = finalEsiAmount / 100;
                                double esi1 = cEsi * epayMaster.getEesiper();
                                double esi2 = cEsi * epayMaster.getDesiper();
                                esi = esi1 + esi2;

                                transaction.setDesi(Math.floor(esi));
                            }

                        }

                        double finalEpfAmount = getWagesEpf(transaction);

                        if (!epayMaster.getEpfexem().equalsIgnoreCase("Y") || epayMaster.getEpfexem().equalsIgnoreCase("")) {

                            if (transaction.getNetsalary() >= 15000) {

                                double cEPF = finalEpfAmount / 100;
                                double epf1 = cEPF * epayMaster.getEepf1per();
                                double epf2 = cEPF * epayMaster.getEepf2per();
                                double depf = cEPF * epayMaster.getDepfper();
                                epf = depf;
                                transaction.setDepf(depf);

                            }
                        }

                        if (transaction.getDepf() == 0.0) {
                            double cEPF = finalEpfAmount / 100;
                            double epf1 = cEPF * epayMaster.getEepf1per();
                            double epf2 = cEPF * epayMaster.getEepf2per();
                            double depf = cEPF * epayMaster.getDepfper();
                            epf = depf;
                            transaction.setDepf(Math.round(depf));
                        }

                        tds = epayMaster.getTds();
                        transaction.setTds(tds);

                        OtherDeductionData otherDeductionData = otherDeductionRepo.findBy(epayMaster.getId(), "emastdaily", start, end);
                        if (otherDeductionData != null) {
                            otherDeduction = otherDeductionData.getAmt();
                        }
                        transaction.setOtherDeduction(otherDeduction);

                        OtherDeductionData uniformDeduction = uniformDeductionRepo.findBy(epayMaster.getId(), "emastdaily", start, end);
                        if (uniformDeduction != null) {
                            uniform = uniformDeduction.getAmt();
                        }
                        transaction.setUniformReimbursement(uniform);

                        double deduction = professionalTax + esi + epf + tds + otherDeduction + uniform;

                        double finalNetSal = transaction.getNetsalary() - deduction;

                        transaction.setNetsalary(finalNetSal);

                        salaryTransactionRepo.save(transaction);

                    }
                }
            }


            */
/*//*
/ contract wages Salary

            List<AssignUserListForSal> contractUserList = assignDutyRepository.findByAssignUserListForContractSal(start, end);


            for (AssignUserListForSal user : contractUserList) {

                //Integer attendanceDays = instruLogRepository.getAttendanceDays(user.getBioid(), start, end);

                AssignUserListForSal salarySum = cwagesTRNRepository.findUserContractSalCount(user.getUserId(), start, end);

                EpayMasterNew epayMaster = epayMasterNewRepository.findByWages(user.getUserId());

                SalaryMasterHead salaryMasterHead = salaryAllowanceMapRepository.findByDw("Y");

                double basic = 0.0;
                double da = 0.0;
                double wa = 0.0;
                double allowance = 0.0;
                double gwa = 0.0;
                double hra = 0.0;
                double finalAllowance = 0.0;
                double attendanceDays = 0.0;
                double epTotal = 0.0;
                double totalSal = 0.0;

                if (epayMaster != null) {


                    //basic = epayMaster.getBasic(); // basic
                    // da = epayMaster.getDa();  // da
                    // allowance = epayMaster.getAllowance();  // Allowance


                    attendanceDays = salarySum.getCount();

                    //double preBasic = basic * attendanceDays;
                    //double preDa = da * attendanceDays;
                    //double preAllowance = da * attendanceDays;

                    //double cTotAll = preBasic + preDa + preAllowance;

                    //double perDayAllowance = cTotAll / attendanceDays;

                    //double tds = epayMaster.getTds() / 26;

                    //double tds = epayMaster.getTds() / 26;


                    SalaryTransaction transaction = new SalaryTransaction(epayMaster);

                    transaction.setTrnno(trnNo);

                    transaction.setBasic(Math.floor(basic));
                    transaction.setDa(Math.floor(da));

                    transaction.setHra(Math.floor(hra));
                    transaction.setWa(Math.floor(wa));

                    transaction.setGwa(Math.floor(gwa));

                    totalSal = salarySum.getAmount();

                    transaction.setTotal(Math.floor(totalSal));

                    transaction.setMnodays(String.valueOf(daysInMonth));
                    transaction.setNowdays(attendanceDays);
                    transaction.setFmperiod(start);
                    transaction.setToperiod(end);
                    transaction.setTrndate(date);
                    transaction.setTrnmonth(month);
                    transaction.setTrnyear(String.valueOf(year));

                    transaction.setCby(createdBy);

                    // epTotal = transaction.getEepf1() + transaction.getEepf2();

                    transaction.setEepftotal(Math.floor(epTotal));

                    double totalNetSalary = totalSal;

                    transaction.setTsalary(Math.floor(totalNetSalary));

                    //deduction


                    salaryTransactionRepo.save(transaction);
                }
           } *//*


            System.out.println("###################");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ResponseEntity<Status>(new Status("200", "salaryUpdated"), HttpStatus.OK);
    }
*/




/*



    // PrePare Staff Salary

    public ResponseEntity<?> prepareStaffSalary(String year, Long month, Long company, Long branch, User loggedUser) {


        System.out.println("###################");

        Date date = dateUtil.getISTDate();

        System.out.println(" IST DATE == " + date);
        Long createdBy = null;

        if (loggedUser != null) {
            createdBy = loggedUser.getId();
        }

        System.out.println("###################");
        System.out.println("###################");

        String fromDate = null;
        if (month > 9) {
            fromDate = "01-0" + month + "-" + year;
        } else {
            fromDate = "01-" + month + "-" + year;
        }

        Date start = dateUtil.getStringTODateWk(fromDate);

        System.out.println("fromDate === " + start);

        Date end = dateUtil.getCalendarEndMonthDate(start);

        System.out.println("endDate === " + end);
        System.out.println("year === " + year);
        System.out.println("month === " + month);

        int daysInMonth = dateUtil.lastDayOfMonth(Integer.valueOf(year), month.intValue());

        System.out.println("daysInMonth === " + daysInMonth);

        List<SalaryTransaction> list = new ArrayList<>();

        try {

            Optional<SalaryTransaction> salaryTransaction = salaryTransactionRepo.findBy(branch, company, year, month.intValue(), Long.valueOf(1), "emaster");

            if (!salaryTransaction.isPresent()) {

                String trnNo = null;
                Optional<TRNIndex> trnIndex = trnIndexRepo.findByPrefix("WAGES");
                if (trnIndex.isPresent()) {
                    TRNIndex trnIndex1 = trnIndex.get();
                    trnNo = trnIndex1.getPrefix() + "-" + trnIndex1.getVouno();
                    Long vouno = trnIndex1.getVouno() + 1;
                    trnIndex1.setVouno(vouno);
                    trnIndexRepo.save(trnIndex1);
                }

                List<User> usersList = new ArrayList<>();


                //usersList = userRepository.findAllStaffs(company, branch);
                usersList = userRepository.findAllStaffsTesting(company, branch);
                */
/*} else {
                    usersList = userRepository.findAllStaffApprentice(company, branch);
                }*//*



                for (User user : usersList) {

                    double attendanceDays = 0.0;
                    double lossOfPayDays = 0.0;
                    double grossCalculationDays = 0.0;

                    String monthValue = null;
                    if (month < 10) {
                        monthValue = "0" + month;
                    } else {
                        monthValue = String.valueOf(month);
                    }

                    Optional<LeaveSummary> leaveSummary = leaveSummaryRepository.findBy(user.getId(), year, monthValue);
                    if (leaveSummary.isPresent()) {
                        attendanceDays = leaveSummary.get().getWorkedDays();
                        lossOfPayDays = leaveSummary.get().getLossOfPayDays();
                        grossCalculationDays = 26.0 - lossOfPayDays;
                    }

                    Integer leaveDays = leaveDetailsRepository.getLeaveCountByUserIdAndLeaveType(user.getId(), start, end);

                    System.out.println("user Id === " + user.getId());

                    EpayMasterNew epayMaster = epayMasterNewRepository.findByStaff(user.getId());


                    double basic = 0.0;
                    double da = 0.0;
                    double wa = 0.0;
                    double hra = 0.0;
                    double gwa = 0.0;
                    double finalAllowance = 0.0;
                    double tds = 0.0;
                    double perdayBasic = 0.0;
                    double perDayDa = 0.0;
                    double addHolidayBasic = 0.0;
                    double addHolidayDa = 0.0;
                    double holidayBasic = 0.0;
                    double holidayDa = 0.0;
                    double leaveBasic = 0.0;
                    double leaveDa = 0.0;

                    double finalBasic = 0.0;
                    double finalDa = 0.0;
                    double finalWa = 0.0;
                    double finalHra = 0.0;
                    double finalGWA = 0.0;
                    double finalLeaveWagesBasic = 0.0;
                    double finalLeaveWagesDa = 0.0;

                    double addSal = 0.0;


                    if (epayMaster != null) {


                        double perDaySalary = getPerDaySal(epayMaster);

                        double grossSalary = perDaySalary * grossCalculationDays;


                        perdayBasic = epayMaster.getBasic() / 26;

                        perDayDa = epayMaster.getDa() / 26;

                        gwa = epayMaster.getGwa(); // goodwork allowance
                        hra = epayMaster.getHra(); // hra
                        wa = epayMaster.getWa(); // wa

                        if (attendanceDays >= 26) {


                            basic = epayMaster.getBasic(); // basic
                            da = epayMaster.getDa();  // da


                            if (leaveSummary.isPresent()) {

                                if (leaveSummary.get().getLeaveHolidaySalaryDays() > 0) {

                                    double holidaySalDays = 0.0;

                                    if (leaveSummary.get().getLeaveHolidayAttendedDays() != null && leaveSummary.get().getLeaveHolidayAttendedDays() > 0) {


                                        holidaySalDays = leaveSummary.get().getLeaveHolidaySalaryDays() - leaveSummary.get().getLeaveHolidayAttendedDays();

                                        double checkholidaySalDays = leaveSummary.get().getLeaveHolidayAttendedDays() - leaveSummary.get().getLeaves();

                                        if (checkholidaySalDays > 0) {

                                            */
/*addHolidayBasic = perdayBasic * leaveSummary.get().getLeaveHolidayAttendedDays();

                                            addHolidayDa = perDayDa * leaveSummary.get().getLeaveHolidayAttendedDays();*//*


                                            addHolidayBasic = perdayBasic * checkholidaySalDays;

                                            addHolidayDa = perDayDa * checkholidaySalDays;

                                            holidayBasic = perdayBasic * checkholidaySalDays;

                                            holidayDa = perDayDa * checkholidaySalDays;

                                        }


                                    }

                                    if (holidaySalDays > 0) {

                                        holidayBasic = perdayBasic * holidaySalDays;

                                        holidayDa = perDayDa * holidaySalDays;


                                    } else {

                                        holidaySalDays = leaveSummary.get().getLeaveHolidaySalaryDays();

                                        holidayBasic = perdayBasic * holidaySalDays;

                                        holidayDa = perDayDa * holidaySalDays;

                                    }

                                }


                                double totalLeaveSaldays = 0.0;

                                if (leaveSummary.get().getLeaves() > 0.0) {
                                    totalLeaveSaldays = leaveSummary.get().getLeaves();
                                }

                                if (totalLeaveSaldays > 0) {

                                    leaveBasic = perdayBasic * totalLeaveSaldays;

                                    leaveDa = perDayDa * totalLeaveSaldays;

                                }

                            }


                            if (addHolidayBasic > 0) {
                                finalBasic = basic + addHolidayBasic;
                            } else {
                                finalBasic = basic;
                            }

                            if (addHolidayDa > 0) {
                                finalDa = da + addHolidayDa;
                            } else {
                                finalDa = da;
                            }

                            finalHra = hra;
                            finalWa = wa;

                            double deductionBasic = 0.0;
                            double deductionDa = 0.0;

                            if (holidayBasic > 0) {
                                deductionBasic = holidayBasic;
                            }
                            if (holidayBasic > 0) {
                                deductionDa = holidayDa;
                            }

                            finalLeaveWagesBasic = deductionBasic + leaveBasic;
                            finalLeaveWagesDa = deductionDa + leaveDa;

                            double deductionGWA = deductionBasic + deductionDa;

                            double GWA = gwa - deductionGWA;
                            if (GWA > 0) {
                                finalGWA = GWA;
                            } else {
                                addSal = GWA;
                            }


                        } else if (attendanceDays < 26) {


                            attendanceDays = attendanceDays;


                            if (leaveSummary.isPresent()) {

                                if (leaveSummary.get().getLeaveHolidaySalaryDays() > 0) {

                                    double holidaySalDays = 0.0;

                                    if (leaveSummary.get().getLeaveHolidayAttendedDays() != null && leaveSummary.get().getLeaveHolidayAttendedDays() > 0) {

                                        holidaySalDays = leaveSummary.get().getLeaveHolidaySalaryDays() - leaveSummary.get().getLeaveHolidayAttendedDays();

                                        addHolidayBasic = perdayBasic * leaveSummary.get().getLeaveHolidayAttendedDays();


                                        addHolidayDa = perDayDa * leaveSummary.get().getLeaveHolidayAttendedDays();


                                    }

                                    if (holidaySalDays > 0) {

                                        holidayBasic = perdayBasic * holidaySalDays;

                                        holidayDa = perDayDa * holidaySalDays;

                                    } else {

                                        holidaySalDays = leaveSummary.get().getLeaveHolidaySalaryDays();

                                        holidayBasic = perdayBasic * holidaySalDays;

                                        holidayDa = perDayDa * holidaySalDays;

                                    }

                                }

                                double totalLeaveSaldays = 0.0;

                                if (leaveSummary.get().getLeaves() > 0.0) {
                                    totalLeaveSaldays = leaveSummary.get().getLeaves();
                                }

                                if (totalLeaveSaldays > 0) {

                                    leaveBasic = perdayBasic * totalLeaveSaldays;

                                    leaveDa = perDayDa * totalLeaveSaldays;

                                }
                            }


                            if (lossOfPayDays > 0) {

                                basic = perdayBasic * attendanceDays;
                                da = perDayDa * attendanceDays;

                                if (addHolidayBasic > 0) {
                                    finalBasic = basic + addHolidayBasic;
                                } else {
                                    finalBasic = basic;
                                }

                                if (addHolidayDa > 0) {
                                    finalDa = da + addHolidayDa;
                                } else {
                                    finalDa = da;
                                }

                                finalLeaveWagesBasic = holidayBasic + leaveBasic;
                                finalLeaveWagesDa = holidayDa + leaveDa;

                                double finalLeaveWagesSal = finalLeaveWagesBasic + finalLeaveWagesDa;

                                double minusBasic = grossSalary - basic;

                                double minusDa = minusBasic - da;

                                double minusLeaveSal = minusDa - finalLeaveWagesSal;
                                double minusWa = 0.0;
                                double minusHRA = 0.0;

                                if (minusLeaveSal > 750) {
                                    minusWa = minusLeaveSal - 750.0;
                                    finalWa = 750.0;
                                } else if (minusLeaveSal > 0) {
                                    finalGWA = minusLeaveSal;
                                }

                                if (minusWa > hra) {
                                    minusHRA = minusWa - hra;
                                    finalHra = hra;
                                } else if (minusLeaveSal > 0) {
                                    finalGWA = minusWa;
                                }

                                finalGWA = minusHRA;

                            } else {

                                gwa = epayMaster.getGwa(); // goodwork allowance
                                hra = epayMaster.getHra(); // hra
                                wa = epayMaster.getWa(); // wa

                                basic = perdayBasic * attendanceDays;
                                da = perDayDa * attendanceDays;

                                if (addHolidayBasic > 0) {
                                    finalBasic = basic + addHolidayBasic;
                                } else {
                                    finalBasic = basic;
                                }

                                if (addHolidayDa > 0) {
                                    finalDa = da + addHolidayDa;
                                } else {
                                    finalDa = da;
                                }

                                finalHra = hra;
                                finalWa = wa;

                                double deductionBasic = 0.0;
                                double deductionDa = 0.0;

                                if (holidayBasic > 0) {
                                    deductionBasic = holidayBasic;
                                }
                                if (holidayBasic > 0) {
                                    deductionDa = holidayDa;
                                }

                                finalLeaveWagesBasic = holidayBasic + leaveBasic;
                                finalLeaveWagesDa = holidayDa + leaveDa;

                                double finalLeaveWagesSal = finalLeaveWagesBasic + finalLeaveWagesDa;

                                double deductionGWA = deductionBasic + deductionDa;

                                double GWA = gwa - deductionGWA;
                                if (GWA > 0) {
                                    finalGWA = GWA;
                                } else {
                                    addSal = GWA;
                                }
                            }
                        }


                        tds = epayMaster.getTds();


                        SalaryTransaction transaction = new SalaryTransaction(epayMaster);

                        transaction.setBid(branch);

                        transaction.setTrnno(trnNo);

                        transaction.setMnodays(String.valueOf(daysInMonth));

                        transaction.setNowdays(attendanceDays);
                        transaction.setFmperiod(start);
                        transaction.setToperiod(end);
                        transaction.setTrndate(date);
                        transaction.setTrnmonth(month.intValue());
                        transaction.setTrnyear(year);

                        transaction.setCby(createdBy);

                        transaction.setBasic(Math.round(finalBasic));

                        transaction.setDa(Math.round(finalDa));


                        transaction.setHra(Math.round(finalHra));

                        transaction.setWa(Math.round(finalWa));

                        transaction.setGwa(Math.round(finalGWA));

                        transaction.setLeaveHolidayBasic(Math.round(finalLeaveWagesBasic));

                        transaction.setLeaveHolidayDA(Math.round(finalLeaveWagesDa));

                        double epTotal = transaction.getEepf1() + transaction.getEepf2();

                        transaction.setEepftotal(Math.round(epTotal));


                        double totalSal = getTotalSalary(transaction);

                        transaction.setTotal(Math.floor(totalSal));

                        //deduction

                        double esi = 0.0;
                        double epf = 0.0;
                        double mess = 0.0;
                        double uniform = 0.0;
                        double otherDeduction = 0.0;
                        double professionalTax = 0.0;


                        if (month == 2 || month == 8) {

                            Optional<ProfessionalTaxMaster> professionalTaxMaster = professionalTaxMasterRepository.findBy(epayMaster.getTotal());
                            if (professionalTaxMaster.isPresent()) {
                                professionalTax = professionalTaxMaster.get().getPtAmount();
                                transaction.setPtAmount(professionalTax);
                            }
                        }


                        if (!epayMaster.getEsiexem().equalsIgnoreCase("Y")) {

                            if (transaction.getNetsalary() <= 21000) {
                                double finalEsiAmount = getStaffEsi(transaction);
                                double cEsi = finalEsiAmount / 100;
                                double esi1 = cEsi * epayMaster.getEesiper();
                                double esi2 = cEsi * epayMaster.getDesiper();
                                esi = esi1 + esi2;
                                transaction.setDesi(Math.round(esi));
                            }

                        }
                        transaction.setDepf(0.0);

                        double finalEpfAmount = getStaffEpf(transaction);

                        if (!epayMaster.getEpfexem().equalsIgnoreCase("Y")) {

                            if (transaction.getNetsalary() >= 15000) {

                                double cEPF = finalEpfAmount / 100;
                                double epf1 = cEPF * epayMaster.getEepf1per();
                                double epf2 = cEPF * epayMaster.getEepf2per();

                                double depf = cEPF * epayMaster.getDepfper();
                                epf = depf;
                                transaction.setDepf(Math.round(depf));
                            }
                        }

                        if (transaction.getDepf() == 0.0) {
                            double cEPF = finalEpfAmount / 100;
                            double epf1 = cEPF * epayMaster.getEepf1per();
                            double epf2 = cEPF * epayMaster.getEepf2per();
                            double depf = cEPF * epayMaster.getDepfper();
                            epf = depf;
                            transaction.setDepf(Math.round(depf));
                        }


                        transaction.setTds(tds);


                        OtherDeductionData otherDeductionData = otherDeductionRepo.findBy(epayMaster.getId(), "emaster", start, end);
                        if (otherDeductionData != null) {
                            otherDeduction = otherDeductionData.getAmt();
                        }
                        transaction.setOtherDeduction(otherDeduction);

                        OtherDeductionData uniformDeduction = uniformDeductionRepo.findBy(epayMaster.getId(), "emaster", start, end);
                        if (uniformDeduction != null) {
                            uniform = uniformDeduction.getAmt();
                        }
                        transaction.setUniformReimbursement(uniform);


                        double deduction = getTotalDeduction(transaction, epayMaster);


                        transaction.setTdeduction(deduction);

                        double totalNetSal = getTotalSalary(transaction) + transaction.getLeaveHolidayBasic() + transaction.getLeaveHolidayDA();

                        transaction.setTsalary(Math.floor(totalNetSal));


                        double finalNetSal = totalNetSal - deduction;

                        transaction.setNetsalary(Math.floor(finalNetSal));

                        salaryTransactionRepo.save(transaction);
                    }

                }

                list = salaryTransactionRepo.findBy(start, end, branch, company, year, month.intValue(), "emaster");


            } else {
                list = salaryTransactionRepo.findBy(start, end, branch, company, year, month.intValue(), "emaster");
            }

            System.out.println("###################");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!list.isEmpty()) {
            List<SalaryTransactionDto> result = list.stream().map(data -> {
                SalaryTransactionDto salaryDto = new SalaryTransactionDto(data);
                return salaryDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<SalaryTransactionDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<SalaryTransactionDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }
*/

    public ResponseEntity<?> verifyApproveAuthorizePrepareSalaryList(String category, String year, Long month, Long company, Long branch, String trnNo, String status, User user) throws UsernameNotFoundException {

        String connectTable = null;
        Long daCatId = null;

        if (category != null) {
            if (category.equalsIgnoreCase("Staff")) {
                connectTable = "emaster";
                daCatId = 1L;
            } else if (category.equalsIgnoreCase("StaffApprentice")) {
                connectTable = "emaster";
                daCatId = 6L;
            } else if (category.equalsIgnoreCase("Wages")) {
                connectTable = "emastdaily";
                daCatId = 1L;
            } else if (category.equalsIgnoreCase("WagesApprentice")) {
                connectTable = "emastdaily";
                daCatId = 6L;
            }
        }

        List<SalaryTransaction> list = salaryTransactionRepo.findByRejectList(branch, company, year, month, daCatId, connectTable, trnNo);
        if (!list.isEmpty()) {
        	if(status.equalsIgnoreCase("Verify")) {
        	  for (SalaryTransaction transaction : list) {
        		  if(transaction.getStatus().equalsIgnoreCase("Pending")) {
                  transaction.setStatus("Verified");
                  transaction.setVby(user.getId());
                  transaction.setVerifiedOn(dateUtil.getISTDate());
                  transaction.setVst("Verified");
              }else {
          		return new ResponseEntity<>(new Status("400", "Salary  Preparation is already Verified!"), HttpStatus.BAD_REQUEST);
          	} 
        		  }
        	  }
        	if(status.equalsIgnoreCase("Approve")) {
          	  for (SalaryTransaction transaction : list) {
          		  if(transaction.getStatus().equalsIgnoreCase("Verified")) {
                    transaction.setStatus("Approved");
                    transaction.setAby(user.getId());
                    transaction.setApprovedOn(dateUtil.getISTDate());
                    transaction.setAst("Approved");
                }else {
              		return new ResponseEntity<>(new Status("400", "Salary  Preparation is already Approved!"), HttpStatus.BAD_REQUEST);
              	} 
          		  }}
        	if(status.equalsIgnoreCase("Authorize")) {
            	  for (SalaryTransaction transaction : list) {
            		  if(transaction.getStatus().equalsIgnoreCase("Approved")) {
                      transaction.setStatus("Authorized");
                      transaction.setAuby(user.getId());
                      transaction.setAuthorizedOn(dateUtil.getISTDate());
                      transaction.setAust("Authorized");
                  }
            		  else {
                    		return new ResponseEntity<>(new Status("400", "Salary  Preparation is already Authorized!"), HttpStatus.BAD_REQUEST);
                    	}}}
        	salaryTransactionRepo.saveAll(list);
            return new ResponseEntity<Status>(new Status("200", "Salary Status Updated Successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "Salary Status Not Updated Successfully!!!"), HttpStatus.OK);
        }

    }
    
    @Transactional
    public ResponseEntity<?> saveStaffSalary(List<SalaryTransactionDto> dtoList, User user) {
    	 if (dtoList == null || dtoList.isEmpty()) {
    	        return new ResponseEntity<>(new Status("400", "No data to save"), HttpStatus.BAD_REQUEST);
    	    }
    	SalaryTransactionDto salaryTransactionDto = dtoList.get(0);
    	
    	Long month = Long.valueOf(salaryTransactionDto.getTrnmonth());
    	
    	Date date = dateUtil.getISTDate();
    	
    	String fmperiod = salaryTransactionDto.getFmperiod();
    	String toperiod = salaryTransactionDto.getToperiod();
    	
    	 Date start = dateUtil.getStringTODateWk(fmperiod);
         Date end = dateUtil.getStringTODateWk(toperiod);
//         get month and year
         LocalDate localStart = start.toInstant()
        	        .atZone(ZoneId.systemDefault())
        	        .toLocalDate();

        	String yearValue = String.valueOf(localStart.getYear());
        	int monthValue = localStart.getMonthValue();

    	Optional<SalaryTransaction> salaryTransaction = salaryTransactionRepo.findBy(salaryTransactionDto.getBranch(), salaryTransactionDto.getCompany(), start,end , Long.valueOf(1), "emaster");

        if (!salaryTransaction.isPresent()) {

            String trnNo = null;
            Long trnId = null;
            Optional<TRNIndex> trnIndex = trnIndexRepo.findByPrefix("WAGES");
            if (trnIndex.isPresent()) {
                TRNIndex trnIndex1 = trnIndex.get();
                trnNo = trnIndex1.getPrefix() + "-" + trnIndex1.getVouno();
                trnId = trnIndex1.getVouno();
                Long vouno = trnIndex1.getVouno() + 1;
                trnIndex1.setVouno(vouno);
                trnIndexRepo.save(trnIndex1);
            }

        for (SalaryTransactionDto dto : dtoList) {
            SalaryTransaction entity = new SalaryTransaction(dto);
            entity.setSalarytype("STAFF");
            entity.setCby(user.getId());   
            entity.setStatus("Pending");
            entity.setCst("Created");
            entity.setTrnno(trnNo);
            entity.setTrnyear(yearValue);
            entity.setTrnmonth(monthValue);
            entity.setFmperiod(start);
            entity.setToperiod(end);
            entity.setTrndate(date);
            salaryTransactionRepo.save(entity);
        }

        return new ResponseEntity<Object>(new Status("200", "Salary Prepared Sucessfully ..."), HttpStatus.OK);
    }else {
    	return new ResponseEntity<>(
                new Status("403", "salary already prepared for this month and year"),
                HttpStatus.FORBIDDEN);
    }

}

    @Transactional
	public ResponseEntity<?> saveStaffApprenticeSalary(List<SalaryTransactionDto> dtoList, User user) {
		 if (dtoList == null || dtoList.isEmpty()) {
		        return new ResponseEntity<>(new Status("400", "No data to save"), HttpStatus.BAD_REQUEST);
		    }
		SalaryTransactionDto salaryTransactionDto = dtoList.get(0);
		
    	Long month = Long.valueOf(salaryTransactionDto.getTrnmonth());
    	
    	Date date = dateUtil.getISTDate();
    	
    	String fmperiod = salaryTransactionDto.getFmperiod();
    	String toperiod = salaryTransactionDto.getToperiod();
    	 Date start = dateUtil.getStringTODateWk(fmperiod);
         Date end = dateUtil.getStringTODateWk(toperiod);
         
//       get month and year
       LocalDate localStart = start.toInstant()
      	        .atZone(ZoneId.systemDefault())
      	        .toLocalDate();

      	String yearValue = String.valueOf(localStart.getYear());
      	int monthValue = localStart.getMonthValue();
      	
    	Optional<SalaryTransaction> salaryTransaction = salaryTransactionRepo.findBy(salaryTransactionDto.getBranch(), salaryTransactionDto.getCompany(), start,end , Long.valueOf(1), "emaster");

        if (!salaryTransaction.isPresent()) {

            String trnNo = null;
            Optional<TRNIndex> trnIndex = trnIndexRepo.findByPrefix("STIPEND");
            if (trnIndex.isPresent()) {
                TRNIndex trnIndex1 = trnIndex.get();
                trnNo = trnIndex1.getPrefix() + "-" + trnIndex1.getVouno();
                Long vouno = trnIndex1.getVouno() + 1;
                trnIndex1.setVouno(vouno);
                trnIndexRepo.save(trnIndex1);
            }
            for (SalaryTransactionDto dto : dtoList) {
                SalaryTransaction entity = new SalaryTransaction(dto);
                entity.setSalarytype("STAFF_STIPEND");
                entity.setCby(user.getId());
              
                entity.setStatus("Pending");
                entity.setCst("Created");
                entity.setTrnno(trnNo);
                entity.setTrnyear(yearValue);
                entity.setTrnmonth(monthValue);
                entity.setFmperiod(start);
                entity.setToperiod(end);
                entity.setTrndate(date);
                salaryTransactionRepo.save(entity);
            }

	 return new ResponseEntity<Object>(new Status("200", "Salary Prepared Sucessfully ..."), HttpStatus.OK);
    }else {
    	return new ResponseEntity<>(
                new Status("403", "salary already prepared for this month and year"),
                HttpStatus.FORBIDDEN);
    }
	}

	@Transactional
	public ResponseEntity<?> saveLabourSalary(List<SalaryTransactionDto> dtoList, User user) {
		 if (dtoList == null || dtoList.isEmpty()) {
		        return new ResponseEntity<>(new Status("400", "No data to save"), HttpStatus.BAD_REQUEST);
		    }
		SalaryTransactionDto salaryTransactionDto = dtoList.get(0);
		
    	Long month = Long.valueOf(salaryTransactionDto.getTrnmonth());
    	
    	Date date = dateUtil.getISTDate();
    	
    	String fmperiod = salaryTransactionDto.getFmperiod();
    	String toperiod = salaryTransactionDto.getToperiod();
    	 Date start = dateUtil.getStringTODateWk(fmperiod);
         Date end = dateUtil.getStringTODateWk(toperiod);
         
//       get month and year
       LocalDate localStart = start.toInstant()
      	        .atZone(ZoneId.systemDefault())
      	        .toLocalDate();

      	String yearValue = String.valueOf(localStart.getYear());
      	int monthValue = localStart.getMonthValue();
         
    	Optional<SalaryTransaction> salaryTransaction = salaryTransactionRepo.findBy(salaryTransactionDto.getBranch(), salaryTransactionDto.getCompany(), start,end , Long.valueOf(1), "emaster");

         if (!salaryTransaction.isPresent()) {

             String trnNo = null;
             Optional<TRNIndex> trnIndex = trnIndexRepo.findByPrefix("WAGES");
             if (trnIndex.isPresent()) {
                 TRNIndex trnIndex1 = trnIndex.get();
                 trnNo = trnIndex1.getPrefix() + "-" + trnIndex1.getVouno();
                 Long vouno = trnIndex1.getVouno() + 1;
                 trnIndex1.setVouno(vouno);
                 trnIndexRepo.save(trnIndex1);
             }
             for (SalaryTransactionDto dto : dtoList) {
                 SalaryTransaction entity = new SalaryTransaction(dto);
                 entity.setSalarytype("WAGES");
                 entity.setCby(user.getId());
                
                 entity.setStatus("Pending");
                 entity.setCst("Created");
                 entity.setTrnno(trnNo);
                 entity.setTrnyear(yearValue);
                 entity.setTrnmonth(monthValue);
                 entity.setFmperiod(start);
                 entity.setToperiod(end);
                 entity.setTrndate(date);
                 salaryTransactionRepo.save(entity);
             }

 	 return new ResponseEntity<Object>(new Status("200", "Salary Prepared Sucessfully ..."), HttpStatus.OK);
     }else {
     	return new ResponseEntity<>(
                 new Status("403", "salary already prepared for this month and year"),
                 HttpStatus.FORBIDDEN);
     }
			}

	@Transactional
	public ResponseEntity<?> saveLabourApprenticeSalary(List<SalaryTransactionDto> dtoList, User user) {
		 if (dtoList == null || dtoList.isEmpty()) {
		        return new ResponseEntity<>(new Status("400", "No data to save"), HttpStatus.BAD_REQUEST);
		    }
		SalaryTransactionDto salaryTransactionDto = dtoList.get(0);
		
    	Long month = Long.valueOf(salaryTransactionDto.getTrnmonth());
    	Date date = dateUtil.getISTDate();
    	
    	String fmperiod = salaryTransactionDto.getFmperiod();
    	String toperiod = salaryTransactionDto.getToperiod();
    	 Date start = dateUtil.getStringTODateWk(fmperiod);
         Date end = dateUtil.getStringTODateWk(toperiod);
         
//       get month and year
       LocalDate localStart = start.toInstant()
      	        .atZone(ZoneId.systemDefault())
      	        .toLocalDate();

      	String yearValue = String.valueOf(localStart.getYear());
      	int monthValue = localStart.getMonthValue();
      	
    	Optional<SalaryTransaction> salaryTransaction = salaryTransactionRepo.findBy(salaryTransactionDto.getBranch(), salaryTransactionDto.getCompany(), start,end , Long.valueOf(1), "emaster");

          if (!salaryTransaction.isPresent()) {

              String trnNo = null;
              Optional<TRNIndex> trnIndex = trnIndexRepo.findByPrefix("STIPEND");
              if (trnIndex.isPresent()) {
                  TRNIndex trnIndex1 = trnIndex.get();
                  trnNo = trnIndex1.getPrefix() + "-" + trnIndex1.getVouno();
                  Long vouno = trnIndex1.getVouno() + 1;
                  trnIndex1.setVouno(vouno);
                  trnIndexRepo.save(trnIndex1);
              }
              for (SalaryTransactionDto dto : dtoList) {
                  SalaryTransaction entity = new SalaryTransaction(dto);
                  entity.setSalarytype("WAGES_STIPEND");
                  entity.setCby(user.getId());
                  entity.setDacatid(user.getDacatid());
                  entity.setStatus("Pending");
                  entity.setCst("Created");
                  entity.setTrnno(trnNo);
                  entity.setTrnyear(yearValue);
                  entity.setTrnmonth(monthValue);
                  entity.setFmperiod(start);
                  entity.setToperiod(end);
                  entity.setTrndate(date);
                  salaryTransactionRepo.save(entity);
              }

  	 return new ResponseEntity<Object>(new Status("200", "Salary Prepared Sucessfully ..."), HttpStatus.OK);
      }else {
      	return new ResponseEntity<>(
                  new Status("403", "salary already prepared for this month and year"),
                  HttpStatus.FORBIDDEN);
      }
	}
}