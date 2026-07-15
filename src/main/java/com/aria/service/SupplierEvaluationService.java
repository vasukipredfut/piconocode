package com.aria.service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aria.utils.DateUtil;

@Service
public class SupplierEvaluationService {

    @Autowired
    SupplierEvaluationRepository supplierEvaluationRepository;

    @Autowired
    QualityInspectionScoreRepo qualityInspectionScoreRepo;

    @Autowired
    SupplierEvaluationReportRepository supplierEvaluationReportRepository;

    @Autowired
    SupplierEvaluationAttributesRepository supplierEvaluationAttributesRepository;


    @Autowired
    UserRepository userRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    BillInwardRepository billInwardRepository;


    @Autowired
    VendorRepository vendorRepository;

    @Autowired
    OrganisationRepository organisationRepository;


    // Scheduler Run


    public void saveSupplierEvaluation() {

        Date fromDate = new Date();
        Date toDate = new Date();


        List<QualityInspectionScoreList> scoreVendorLists = qualityInspectionScoreRepo.findBy(fromDate, toDate);
        if (!scoreVendorLists.isEmpty()) {

            for (QualityInspectionScoreList inspectionScoreVendor : scoreVendorLists) {


                Optional<SupplierEvaluation> optional = supplierEvaluationRepository.findBy(inspectionScoreVendor.getCompany(), inspectionScoreVendor.getCompany(), inspectionScoreVendor.getCompany(), fromDate, toDate);
                SupplierEvaluation supplierEvaluation = null;
                SupplierEvaluation rs = null;
                if (optional.isPresent()) {
                    supplierEvaluation = new SupplierEvaluation();
                    supplierEvaluation.setCid(inspectionScoreVendor.getCompany());
                    supplierEvaluation.setBranch(inspectionScoreVendor.getBranchId());
                    supplierEvaluation.setVid(inspectionScoreVendor.getVendorId());
                    supplierEvaluation.setStatus("Created");
                    supplierEvaluation.setCreated("Y");
                    supplierEvaluation.setCreated_date(dateUtil.getISTDate());
                    supplierEvaluation.setEntry(dateUtil.getISTDate());
                    SupplierEvaluation res = supplierEvaluationRepository.save(supplierEvaluation);
                    res.setTrnno("SUPEVAL-" + res.getId());
                    rs = supplierEvaluationRepository.save(res);
                } else {
                    supplierEvaluation = optional.get();
                    rs = supplierEvaluation;
                }


                int startMonth = dateUtil.getMonth(rs.getFromdate());
                int year = dateUtil.getYear(rs.getFromdate());
                int endMonth = dateUtil.getMonth(rs.getTodate());

                String period = startMonth + "to" + endMonth;

                Optional<SupplierEvaluationReport> optionalReport = supplierEvaluationReportRepository.findBy(rs.getVid(), year, period);
                if (optionalReport.isPresent()) {

                    //QualityInspectionScoreList qualityInspectionScore = qualityInspectionScoreRepo.findBy(partyId, poId, branch, company, startDate, endDate);



                } else {
                    List<QualityInspectionScoreList> scoreLists = qualityInspectionScoreRepo.findBy(rs.getCid(), rs.getVid(), rs.getFromdate(), rs.getTodate());

                    if (!scoreLists.isEmpty()) {

                        for (QualityInspectionScoreList qualityInspectionScore : scoreLists) {

                            SupplierEvaluationReport supplierEvaluationReport = new SupplierEvaluationReport();

                            supplierEvaluationReport.setTrnNo(rs.getTrnno());
                            supplierEvaluationReport.setYear(year);
                            supplierEvaluationReport.setPeriod(period);
                            supplierEvaluationReport.setBranch(qualityInspectionScore.getBranchId());
                            supplierEvaluationReport.setCompany(rs.getCid());
                            supplierEvaluationReport.setLedgerId(qualityInspectionScore.getVendorId());
                            supplierEvaluationReport.setPoId(qualityInspectionScore.getPoId());

                            if (qualityInspectionScore != null) {

                                supplierEvaluationReport.setBillId(qualityInspectionScore.getBillRefId());
                                supplierEvaluationReport.setBillNo(qualityInspectionScore.getBillNo());
                                supplierEvaluationReport.setInwardDate(qualityInspectionScore.getInwardDate());
                                supplierEvaluationReport.setDeliveryDueDate(qualityInspectionScore.getDeliveryDueDate());
                                supplierEvaluationReport.setLedgerId(qualityInspectionScore.getVendorId());

                                if (qualityInspectionScore.getDeliveryScore() != null && !qualityInspectionScore.getDeliveryScore().equalsIgnoreCase("0.0")) {
                                    supplierEvaluationReport.setDeliveryScore(Math.round(Float.valueOf(qualityInspectionScore.getDeliveryScore())));
                                } else {
                                    supplierEvaluationReport.setDeliveryScore(0);
                                }
                                if (qualityInspectionScore.getQcScore() != null && !qualityInspectionScore.getQcScore().equalsIgnoreCase("0.0")) {
                                    supplierEvaluationReport.setQualityScore(Math.round(Float.valueOf(qualityInspectionScore.getQcScore())));
                                } else {
                                    supplierEvaluationReport.setQualityScore(0);
                                }
                                supplierEvaluationReport.setLoyaltyScore(0);
                            }

                            supplierEvaluationReportRepository.save(supplierEvaluationReport);
                        }
                    }
                }
            }
        }
    }


    public ResponseEntity<?> saveSupplierEvaluation(SupplierEvaluationDTO supplierEvaluationDTO, User loggedUser) {


        SupplierEvaluation supplierEvaluation = new SupplierEvaluation(supplierEvaluationDTO);

        supplierEvaluation.setStatus("Created");
        supplierEvaluation.setCreated("Y");
        supplierEvaluation.setCreated_by(loggedUser.getId());
        supplierEvaluation.setCreated_date(dateUtil.getISTDate());
        supplierEvaluation.setEntry(dateUtil.getISTDate());
        SupplierEvaluation rs = supplierEvaluationRepository.save(supplierEvaluation);
        rs.setTrnno("SUPEVAL-" + rs.getId());
        supplierEvaluationRepository.save(rs);

        int startMonth = dateUtil.getMonth(rs.getFromdate());
        int year = dateUtil.getYear(rs.getFromdate());
        int endMonth = dateUtil.getMonth(rs.getTodate());

        String period = startMonth + "to" + endMonth;


        List<QualityInspectionScoreList> scoreLists = qualityInspectionScoreRepo.findBy(rs.getCid(), rs.getVid(), rs.getFromdate(), rs.getTodate());

        if (!scoreLists.isEmpty()) {

            for (QualityInspectionScoreList qualityInspectionScore : scoreLists) {

                SupplierEvaluationReport supplierEvaluationReport = new SupplierEvaluationReport();

                supplierEvaluationReport.setTrnNo(rs.getTrnno());
                supplierEvaluationReport.setYear(year);
                supplierEvaluationReport.setPeriod(period);
                supplierEvaluationReport.setBranch(qualityInspectionScore.getBranchId());
                supplierEvaluationReport.setCompany(rs.getCid());
                supplierEvaluationReport.setLedgerId(qualityInspectionScore.getVendorId());
                supplierEvaluationReport.setPoId(qualityInspectionScore.getPoId());

                if (qualityInspectionScore != null) {

                    supplierEvaluationReport.setBillId(qualityInspectionScore.getBillRefId());
                    supplierEvaluationReport.setBillNo(qualityInspectionScore.getBillNo());
                    supplierEvaluationReport.setInwardDate(qualityInspectionScore.getInwardDate());
                    supplierEvaluationReport.setDeliveryDueDate(qualityInspectionScore.getDeliveryDueDate());
                    supplierEvaluationReport.setLedgerId(qualityInspectionScore.getVendorId());

                    if (qualityInspectionScore.getDeliveryScore() != null && !qualityInspectionScore.getDeliveryScore().equalsIgnoreCase("0.0")) {
                        supplierEvaluationReport.setDeliveryScore(Math.round(Float.valueOf(qualityInspectionScore.getDeliveryScore())));
                    } else {
                        supplierEvaluationReport.setDeliveryScore(0);
                    }
                    if (qualityInspectionScore.getQcScore() != null && !qualityInspectionScore.getQcScore().equalsIgnoreCase("0.0")) {
                        supplierEvaluationReport.setQualityScore(Math.round(Float.valueOf(qualityInspectionScore.getQcScore())));
                    } else {
                        supplierEvaluationReport.setQualityScore(0);
                    }
                    supplierEvaluationReport.setLoyaltyScore(0);
                }

                supplierEvaluationReportRepository.save(supplierEvaluationReport);
            }
        }
        return new ResponseEntity<Status>(new Status("200", "Supplier Evaluation Created Successfully!!!!"), HttpStatus.OK);

    }


    public ResponseEntity<?> updateEvaluationStatus(String status, String trnNo, String type, User loggedUser) {


        Optional<SupplierEvaluation> optional = supplierEvaluationRepository.findBy(trnNo);

        if (optional.isPresent()) {

            SupplierEvaluation supplierEvaluation = optional.get();

            if (status != null && status.equalsIgnoreCase("Approved")) {
                supplierEvaluation.setApproval("Y");
                supplierEvaluation.setApproved_date(dateUtil.getISTDate());
                supplierEvaluation.setApproval_staff(loggedUser.getId());
            } else if (status != null && status.equalsIgnoreCase("Verified")) {
                supplierEvaluation.setVerify("Y");
                supplierEvaluation.setVerify_date(dateUtil.getISTDate());
                supplierEvaluation.setVerify_staff(loggedUser.getId());
            } else if (status != null && status.equalsIgnoreCase("Authorised")) {
                supplierEvaluation.setAuthenty("Y");
                supplierEvaluation.setAuthorised_date(dateUtil.getISTDate());
                supplierEvaluation.setAuthorise_staff(loggedUser.getId());
            }
            supplierEvaluation.setStatus(status);
            supplierEvaluationRepository.save(supplierEvaluation);
        } else {
            return new ResponseEntity<Status>(new Status("400", "Supplier Evaluation Not Updated!!!!"), HttpStatus.OK);
        }

        return new ResponseEntity<Status>(new Status("200", "Supplier Evaluation Updated Successfully!!!!"), HttpStatus.OK);

    }


    public ResponseEntity<?> listAllSupplierEvaluation(Long vendorId, Long poNumber, Integer fromyear, Integer toyear,
                                                       String timePeriod, Pageable page) {

        Page<SupplierEvaluationReportList> suppPage = supplierEvaluationReportRepository.findbySupplierEvaluation(poNumber,
                vendorId, fromyear, toyear, timePeriod, page);

        if (!suppPage.isEmpty()) {
            List<SupplierEvaluationReportListDTO> ans = suppPage.toList().stream().map(val -> {
                SupplierEvaluationReportListDTO supp = new SupplierEvaluationReportListDTO(val);
                return supp;
            }).collect(Collectors.toList());
            PageDataDto<SupplierEvaluationReportListDTO> result = new PageDataDto<>(ans, page, suppPage);
            return new ResponseEntity<Object>(result, HttpStatus.OK);
        }
        return new ResponseEntity<Object>(new PageDataDto<>(), HttpStatus.OK);

    }

    public ResponseEntity<?> listAllSupplierEvaluationattributes() {

        List<SupplierEvaluationAttributes> suppPage = supplierEvaluationAttributesRepository
                .findListSupplierEvaluationAttributes();

        if (!suppPage.isEmpty()) {
            List<SupplierEvaluationAttributesDTO> ans = suppPage.stream().map(val -> {
                SupplierEvaluationAttributesDTO supp = new SupplierEvaluationAttributesDTO(val);
                return supp;
            }).collect(Collectors.toList());
            return new ResponseEntity<Object>(ans, HttpStatus.OK);
        }
        return new ResponseEntity<Object>(new ArrayList<SupplierEvaluationAttributesDTO>(), HttpStatus.OK);

    }

    public ResponseEntity<?> listvendorSCore(Long vendorId, User user) {

        LocalDate currentDate = LocalDate.now();
        Integer year = currentDate.getYear();
        int month = currentDate.getMonthValue();

        String period = "1to6";

        if (month < 7) {
            year = year - 1;
        }

        SupplierEvaluationReportVendorScore suppPage = supplierEvaluationReportRepository.findVendorScore(vendorId);

        if (suppPage != null) {
            SupplierEvaluationVendorScoreDTO vendorScoreDTO = new SupplierEvaluationVendorScoreDTO(suppPage);

            return new ResponseEntity<Object>(vendorScoreDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<Object>(new SupplierEvaluationVendorScoreDTO(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> supplierEvaluationList(Long firmId ,Long vendorId, String partyName, Integer fromyear, Integer toyear, String timePeriod, String status, Pageable page) {

        Page<EvaluationReportList> suppPage = supplierEvaluationReportRepository.findBySupplierEvaluation(firmId , vendorId, partyName, fromyear, toyear, timePeriod, status, page);

        if (!suppPage.isEmpty()) {
            List<EvaluationReportListDto> ans = suppPage.toList().stream().map(val -> new EvaluationReportListDto(val)).collect(Collectors.toList());
            PageDataDto<EvaluationReportListDto> result = new PageDataDto<>(ans, page, suppPage);
            return new ResponseEntity<Object>(result, HttpStatus.OK);
        }
        return new ResponseEntity<Object>(new PageDataDto<>(), HttpStatus.OK);

    }


    public ResponseEntity<?> supplierEvaluationListByBranchWise(Long vendorId, String partyName, Long firmId, Integer fromyear, Integer toyear, String timePeriod, String trnNo) {

        List<EvaluationReportList> suppPage = supplierEvaluationReportRepository.findByEvaluationReportList(firmId, vendorId, partyName, fromyear, toyear, timePeriod, trnNo);

        if (!suppPage.isEmpty()) {
            List<EvaluationReportListDto> result = suppPage.stream().map(val -> {
                EvaluationReportListDto supp = new EvaluationReportListDto(val);
                Optional<SupplierEvaluation> supplierEvaluationOptional = supplierEvaluationRepository.findBy(supp.getTrnNo());
                if(supplierEvaluationOptional.isPresent()){
                    if (supplierEvaluationOptional.get().getVerify_staff() != null) {
                        Optional<User> user = userRepository.findById(supplierEvaluationOptional.get().getVerify_staff());
                        if (user.isPresent()) {
                            supp.setVerifiedByName(user.get().getEmployeename());
                        }
                        if (supplierEvaluationOptional.get().getVerify_date() != null) {
                            supp.setVerifiedDate(dateUtil.getDateTOStringWk(supplierEvaluationOptional.get().getVerify_date()));
                            supp.setVerifiedDateTime(dateUtil.getDateTOStringDATETIME(supplierEvaluationOptional.get().getVerify_date()));
                        }
                    }
                    if (supplierEvaluationOptional.get().getApproval_staff() != null) {
                        Optional<User> user = userRepository.findById(supplierEvaluationOptional.get().getApproval_staff());
                        if (user.isPresent()) {
                            supp.setApprovedByName(user.get().getEmployeename());
                        }
                        if (supplierEvaluationOptional.get().getApproved_date() != null) {
                            supp.setApprovedDate(dateUtil.getDateTOStringWk(supplierEvaluationOptional.get().getApproved_date()));
                            supp.setApprovedDateTime(dateUtil.getDateTOStringDATETIME(supplierEvaluationOptional.get().getApproved_date()));
                        }
                    }

                    if (supplierEvaluationOptional.get().getAuthorise_staff() != null) {
                        Optional<User> user = userRepository.findById(supplierEvaluationOptional.get().getAuthorise_staff());
                        if (user.isPresent()) {
                            supp.setAuthorisedByName(user.get().getEmployeename());
                        }
                        if (supplierEvaluationOptional.get().getAuthorised_date() != null) {
                            supp.setAuthorisedDate(dateUtil.getDateTOStringWk(supplierEvaluationOptional.get().getAuthorised_date()));
                            supp.setAuthorisedDateTime(dateUtil.getDateTOStringDATETIME(supplierEvaluationOptional.get().getAuthorised_date()));
                        }
                    }
                }

                return supp;
            }).collect(Collectors.toList());

            return new ResponseEntity<List<EvaluationReportListDto>>(result, HttpStatus.OK);
        }
        return new ResponseEntity<List<EvaluationReportListDto>>(new ArrayList<>(), HttpStatus.OK);

    }


    public ResponseEntity<?> periodBillListByVendor(Long firmId, Long branch, Long vendorId, String period, Integer year, User user) {


        List<EvaluationBillList> evaluationBillList = supplierEvaluationReportRepository.findBySupplierEvaluationBillList(firmId, branch, vendorId, period, year);

        if (!evaluationBillList.isEmpty()) {
            List<EvaluationBillListDTO> result = evaluationBillList.stream().map(val -> {
                EvaluationBillListDTO supp = new EvaluationBillListDTO(val);

                String billIds = supp.getBillRefId().replaceAll("#", ",");
                supp.setBillRefId(billIds);

                if (supp.getBillNo() == null) {

                    List<Long> ids = new ArrayList<>(Arrays.asList(billIds.split("\\,"))).stream().map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());

                    String billNo = billInwardRepository.findByBillNo(ids);
                    if (billNo != null) {
                        supp.setBillNo(billNo);
                    }
                }
                return supp;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<EvaluationBillListDTO>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<EvaluationBillListDTO>>(new ArrayList<>(), HttpStatus.OK);
        }

    }


    public ResponseEntity<?> printSupplierEvaluationList(Long firmId, Long vendorId, String vendorName, Integer fromYear, Integer toYear, String timePeriod, String trnNo) {


        PrintSupplierEvaluationReportResult result = new PrintSupplierEvaluationReportResult();
        if (firmId != null) {
            CompanyList companyList = organisationRepository.findByFirm(firmId);
            if (companyList != null)
                result.setCompanyDetails(new Company(companyList));
        }

        if (trnNo != null) {
            SupplierEvalValidUsers supplierEvalValidUsers = supplierEvaluationRepository.findByUsers(trnNo);
            if (supplierEvalValidUsers != null) {
                result.setValidateUserDetails(new SupplierEvalValidUsersDTO(supplierEvalValidUsers));
            }
        }

        if (vendorId != null) {
            PartyList party = vendorRepository.findByVendor(vendorId);
            if (party != null)
                result.setVendorDetails(new PartyDetails(party));
        }

        List<EvaluationReportList> branchWiseList = supplierEvaluationReportRepository.findByEvaluationReportList(firmId, vendorId, vendorName, fromYear, toYear, timePeriod, trnNo);

        if (!branchWiseList.isEmpty()) {

            List<EvaluationReportListDto> branchWiseResult = branchWiseList.stream().map(val -> {
                EvaluationReportListDto supp = new EvaluationReportListDto(val);
                return supp;
            }).collect(Collectors.toList());

            result.setBranchWiseScoreResult(branchWiseResult);

            Map<String, List<EvaluationBillListDTO>> branchANDPoWiseBill = new HashMap<>();

            for (EvaluationReportList evaluationReportList : branchWiseList) {

                List<EvaluationBillList> evaluationBillList = supplierEvaluationReportRepository.findBySupplierEvaluationBillList(firmId, evaluationReportList.getBranchId(), vendorId, timePeriod, evaluationReportList.getYear());

                List<EvaluationBillListDTO> billList = null;

                if (!evaluationBillList.isEmpty()) {
                    billList = evaluationBillList.stream().map(val -> {
                        EvaluationBillListDTO billDTO = new EvaluationBillListDTO(val);
                        String billIds = billDTO.getBillRefId().replaceAll("#", ",");
                        billDTO.setBillRefId(billIds);
                        if (billDTO.getBillNo() == null) {
                            List<Long> ids = new ArrayList<>(Arrays.asList(billIds.split("\\,"))).stream().map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
                            String billNo = billInwardRepository.findByBillNo(ids);
                            if (billNo != null) {
                                billDTO.setBillNo(billNo);
                            }
                        }
                        return billDTO;
                    }).collect(Collectors.toList());
                }

                branchANDPoWiseBill.put(evaluationReportList.getBranchName(), billList);
            }

            result.setPoAndBillWiseScoreResult(branchANDPoWiseBill);

        }

        List<SupplierEvaluationAttributes> commentList = supplierEvaluationAttributesRepository.findListSupplierEvaluationAttributes();

        if (!commentList.isEmpty()) {
            List<SupplierEvaluationAttributesDTO> ans = commentList.stream().map(val -> new SupplierEvaluationAttributesDTO(val)).collect(Collectors.toList());
            result.setCommentAndDescription(ans);
        }

        return new ResponseEntity<PrintSupplierEvaluationReportResult>(result, HttpStatus.OK);

    }












    public void updateSupplierEvaluation() {

        Date fromDate = new Date();
        Date toDate = new Date();


        List<QualityInspectionScoreList> scoreVendorLists = qualityInspectionScoreRepo.findBy(fromDate, toDate);
        if (!scoreVendorLists.isEmpty()) {

            for (QualityInspectionScoreList inspectionScoreVendor : scoreVendorLists) {


                Optional<SupplierEvaluation> optional = supplierEvaluationRepository.findBy(inspectionScoreVendor.getCompany(), inspectionScoreVendor.getCompany(), inspectionScoreVendor.getCompany(), fromDate, toDate);
                SupplierEvaluation supplierEvaluation = null;
                SupplierEvaluation rs = null;
                if (optional.isPresent()) {
                    supplierEvaluation = new SupplierEvaluation();
                    supplierEvaluation.setCid(inspectionScoreVendor.getCompany());
                    supplierEvaluation.setBranch(inspectionScoreVendor.getBranchId());
                    supplierEvaluation.setVid(inspectionScoreVendor.getVendorId());
                    supplierEvaluation.setStatus("Created");
                    supplierEvaluation.setCreated("Y");
                    supplierEvaluation.setCreated_date(dateUtil.getISTDate());
                    supplierEvaluation.setEntry(dateUtil.getISTDate());
                    SupplierEvaluation res = supplierEvaluationRepository.save(supplierEvaluation);
                    res.setTrnno("SUPEVAL-" + res.getId());
                    rs = supplierEvaluationRepository.save(res);
                } else {
                    supplierEvaluation = optional.get();
                    rs = supplierEvaluation;
                }


                int startMonth = dateUtil.getMonth(rs.getFromdate());
                int year = dateUtil.getYear(rs.getFromdate());
                int endMonth = dateUtil.getMonth(rs.getTodate());

                String period = startMonth + "to" + endMonth;

                Optional<SupplierEvaluationReport> optionalReport = supplierEvaluationReportRepository.findBy(rs.getVid(), year, period);
                if (optionalReport.isPresent()) {

                    //QualityInspectionScoreList qualityInspectionScore = qualityInspectionScoreRepo.findBy(partyId, poId, branch, company, startDate, endDate);



                } else {
                    List<QualityInspectionScoreList> scoreLists = qualityInspectionScoreRepo.findBy(rs.getCid(), rs.getVid(), rs.getFromdate(), rs.getTodate());

                    if (!scoreLists.isEmpty()) {

                        for (QualityInspectionScoreList qualityInspectionScore : scoreLists) {

                            SupplierEvaluationReport supplierEvaluationReport = new SupplierEvaluationReport();

                            supplierEvaluationReport.setTrnNo(rs.getTrnno());
                            supplierEvaluationReport.setYear(year);
                            supplierEvaluationReport.setPeriod(period);
                            supplierEvaluationReport.setBranch(qualityInspectionScore.getBranchId());
                            supplierEvaluationReport.setCompany(rs.getCid());
                            supplierEvaluationReport.setLedgerId(qualityInspectionScore.getVendorId());
                            supplierEvaluationReport.setPoId(qualityInspectionScore.getPoId());

                            if (qualityInspectionScore != null) {

                                supplierEvaluationReport.setBillId(qualityInspectionScore.getBillRefId());
                                supplierEvaluationReport.setBillNo(qualityInspectionScore.getBillNo());
                                supplierEvaluationReport.setInwardDate(qualityInspectionScore.getInwardDate());
                                supplierEvaluationReport.setDeliveryDueDate(qualityInspectionScore.getDeliveryDueDate());
                                supplierEvaluationReport.setLedgerId(qualityInspectionScore.getVendorId());

                                if (qualityInspectionScore.getDeliveryScore() != null && !qualityInspectionScore.getDeliveryScore().equalsIgnoreCase("0.0")) {
                                    supplierEvaluationReport.setDeliveryScore(Math.round(Float.valueOf(qualityInspectionScore.getDeliveryScore())));
                                } else {
                                    supplierEvaluationReport.setDeliveryScore(0);
                                }
                                if (qualityInspectionScore.getQcScore() != null && !qualityInspectionScore.getQcScore().equalsIgnoreCase("0.0")) {
                                    supplierEvaluationReport.setQualityScore(Math.round(Float.valueOf(qualityInspectionScore.getQcScore())));
                                } else {
                                    supplierEvaluationReport.setQualityScore(0);
                                }
                                supplierEvaluationReport.setLoyaltyScore(0);
                            }

                            supplierEvaluationReportRepository.save(supplierEvaluationReport);
                        }
                    }
                }
            }
        }
    }



}
