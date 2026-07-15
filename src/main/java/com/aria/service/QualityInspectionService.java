package com.aria.service;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aria.dao.SupplierEvaluationReportRepository;
import com.aria.dao.SupplierEvaluationRepository;
import com.aria.dao.TRNIndexRepo;
import com.aria.dto.QualityInspectionScoreList;
import com.aria.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aria.dao.QualityInspectionScoreRepo;
import com.aria.dto.PageDataDto;
import com.aria.dto.QualityInspectionDTO;
import com.aria.utils.DateUtil;

@Service
public class QualityInspectionService {


    @Autowired
    QualityInspectionScoreRepo qualityInspectionScoreRepo;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    SupplierEvaluationReportRepository supplierEvaluationReportRepository;

    @Autowired
    SupplierEvaluationRepository supplierEvaluationRepository;


    public ResponseEntity<?> saveQualityInspections(List<QualityInspectionDTO> qualityInspectionDTOs, User loggedUser) {

        List<QualityInspectionScore> qualityInspectionScores = new ArrayList<>();

        Long billRefId = qualityInspectionDTOs.get(0).getBillRefId();
        Long partyId = qualityInspectionDTOs.get(0).getVendorRefId();
        Long poId = qualityInspectionDTOs.get(0).getPoId();
        Long branch = qualityInspectionDTOs.get(0).getBranch();
        Long company = qualityInspectionDTOs.get(0).getCompany();

        for (QualityInspectionDTO qualityInspectionDTO : qualityInspectionDTOs) {
            QualityInspectionScore qualityInspectionScore = new QualityInspectionScore(qualityInspectionDTO);
            qualityInspectionScore.setActiveFlag(1);
            qualityInspectionScore.setApproveById(loggedUser.getId());
            //qualityInspectionScore.setBranch(loggedUser.getBranch_id());
            //qualityInspectionScore.setCompany(loggedUser.getOrg_id());
            qualityInspectionScore.setCreatedById(loggedUser.getId());
            qualityInspectionScore.setDate(dateUtil.getISTDate());

            if (Objects.isNull(qualityInspectionScore.getLoyaltyScore())) {
                qualityInspectionScore.setLoyaltyScore(0D);
            }
            if (Objects.isNull(qualityInspectionScore.getDeliveryScore())) {
                qualityInspectionScore.setDeliveryScore(0D);
            }
            if (Objects.isNull(qualityInspectionScore.getQcScore())) {
                qualityInspectionScore.setQcScore(0D);
            }

            double totalScore = qualityInspectionScore.getLoyaltyScore() + qualityInspectionScore.getDeliveryScore() + qualityInspectionScore.getQcScore();

            qualityInspectionScore.setTotalScore(totalScore);

            // totalScore Already assigned by the above value
            // qualityInspectionScore.setTotalScore(null);
            qualityInspectionScores.add(qualityInspectionScore);
        }

        qualityInspectionScoreRepo.saveAll(qualityInspectionScores);
        updateSupplierEvolution(company, partyId, poId, branch, loggedUser);
        return new ResponseEntity<Status>(new Status("200", "Quality Inspections Saved successfully!!!"),
                HttpStatus.OK);
    }

    public ResponseEntity<?> listallQualityInspection(Long po_id, String billNumber, Long vendorRefid, String fromDate,
                                                      String toDate, User loggedUser, Pageable page) {

        Date fromDt = null;
        Date toDt = null;

        if (java.util.Objects.nonNull(fromDate)) {
            fromDt = dateUtil.getStringTODateDDMMYYYY(fromDate);
        }
        if (java.util.Objects.nonNull(toDate)) {
            toDt = dateUtil.getStringTODateDDMMYYYY(toDate);
        }

        List<String> billno = new ArrayList<String>();
        if (Objects.nonNull(billNumber)) {
            if (billNumber.contains("#")) {
                String[] billarray = billNumber.split("#");
                billno = Arrays.asList(billarray);

            } else {
                billno.add(billNumber);
            }
        }

        System.out.println(fromDt + " :::: " + toDt);
        Page<QualityInspectionScore> qualityinspection = qualityInspectionScoreRepo.getQualityInspection(billno,
                vendorRefid, fromDt, toDt, po_id, page);

        List<QualityInspectionDTO> qualitDataDto = qualityinspection.toList().stream()
                .map(val -> new QualityInspectionDTO(val)).collect(Collectors.toList());

        PageDataDto<QualityInspectionDTO> pageDataDto = new PageDataDto<>(qualitDataDto, page, qualityinspection);

        return new ResponseEntity<PageDataDto<QualityInspectionDTO>>(pageDataDto, HttpStatus.OK);
    }

    public ResponseEntity<?> getQualityInspectionbyId(Long id, User loggedUser) {

        Optional<QualityInspectionScore> qualityinspection = qualityInspectionScoreRepo.findById(id);

        if (qualityinspection.isPresent()) {
            QualityInspectionDTO qualityInspectionDTO = new QualityInspectionDTO(qualityinspection.get());

            return new ResponseEntity<QualityInspectionDTO>(qualityInspectionDTO, HttpStatus.OK);

        }

        return new ResponseEntity<QualityInspectionDTO>(new QualityInspectionDTO(), HttpStatus.OK);
    }

    public void updateSupplierEvolution(Long company, Long partyId, Long poId, Long branch, User loggedUser) {

        Date currentDate = dateUtil.getISTDate();

        int month = dateUtil.getMonth();
        int year = dateUtil.getYear();

        String fromDate = null;
        String toDate = null;

        String period = null;

        Date startDate = null;
        Date endDate = null;

        if (month > 6) {

            fromDate = dateUtil.getSetStartDate(7, year);
            toDate = dateUtil.getSetEndDate(12, year);
            period = "7to12";

        } else {
            fromDate = dateUtil.getSetStartDate(1, year);
            toDate = dateUtil.getSetEndDate(6, year);
            period = "1to6";
        }

        startDate = dateUtil.getStringTODateWk(fromDate);

        endDate = dateUtil.getStringTODateWk(toDate);

        String trnNo = null;

        Optional<SupplierEvaluationReport> checkEval = supplierEvaluationReportRepository.findBy(partyId, year, period);

        if (checkEval.isPresent()) {

            trnNo = checkEval.get().getTrnNo();

            Optional<SupplierEvaluationReport> optional = supplierEvaluationReportRepository.findBy(partyId, poId, branch, year, period);

            SupplierEvaluationReport supplierEvaluationReport = null;

            QualityInspectionScoreList qualityInspectionScore = qualityInspectionScoreRepo.findBy(partyId, poId, branch, company, startDate, endDate);


            if (optional.isPresent()) {

                supplierEvaluationReport = optional.get();

            } else {

                supplierEvaluationReport = new SupplierEvaluationReport();

            }

            supplierEvaluationReport.setTrnNo(trnNo);

            supplierEvaluationReport.setYear(year);
            supplierEvaluationReport.setPeriod(period);
            supplierEvaluationReport.setBranch(branch);
            supplierEvaluationReport.setCompany(company);
            supplierEvaluationReport.setLedgerId(partyId);
            supplierEvaluationReport.setPoId(poId);

            if (qualityInspectionScore != null) {

                supplierEvaluationReport.setBillId(qualityInspectionScore.getBillRefId());
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


        } else {

            SupplierEvaluation supplierEvaluation = new SupplierEvaluation();
            supplierEvaluation.setCid(company);
            supplierEvaluation.setEntry(dateUtil.getISTDate());
            supplierEvaluation.setCreated_date(dateUtil.getISTDate());
            supplierEvaluation.setStatus("Created");
            supplierEvaluation.setCreated("Y");
            supplierEvaluation.setCreated_by(loggedUser.getId());
            supplierEvaluation.setFromdate(startDate);
            supplierEvaluation.setTodate(endDate);
            SupplierEvaluation rs = supplierEvaluationRepository.save(supplierEvaluation);
            rs.setTrnno("SUPEVAL-" + rs.getId());
            trnNo = rs.getTrnno();
            SupplierEvaluation resu = supplierEvaluationRepository.save(rs);

            List<QualityInspectionScoreList> scoreLists = qualityInspectionScoreRepo.findBy(rs.getCid(), partyId, resu.getFromdate(), resu.getTodate());

            if (!scoreLists.isEmpty()) {

                for (QualityInspectionScoreList qualityInspectionScore : scoreLists) {

                    SupplierEvaluationReport supplierEvaluationReport = new SupplierEvaluationReport();

                    supplierEvaluationReport.setTrnNo(resu.getTrnno());
                    supplierEvaluationReport.setYear(year);
                    supplierEvaluationReport.setPeriod(period);
                    supplierEvaluationReport.setBranch(qualityInspectionScore.getBranchId());
                    supplierEvaluationReport.setCompany(resu.getCid());
                    supplierEvaluationReport.setLedgerId(qualityInspectionScore.getVendorId());
                    supplierEvaluationReport.setPoId(qualityInspectionScore.getPoId());

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

                    supplierEvaluationReportRepository.save(supplierEvaluationReport);
                }
            }
        }

    }

}