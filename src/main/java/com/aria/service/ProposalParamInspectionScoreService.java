package com.aria.service;

import com.aria.dao.BillInwardRepository;
import com.aria.dao.DpEntryProposalParamInspectionScoreRepository;
import com.aria.dao.PurchaseOrderRepository;
import com.aria.dto.DpEntryProposalParamInspectionScoreDto;

import com.aria.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ProposalParamInspectionScoreService {


    @Autowired
    DpEntryProposalParamInspectionScoreRepository scoreRepository;

    @Autowired
    PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    BillInwardRepository billInwardRepository;


    public ResponseEntity<?> saveParamInspectionScore(List<DpEntryProposalParamInspectionScoreDto> paramInspectionScoreDtos, User loggedUser) {

        if (paramInspectionScoreDtos == null || paramInspectionScoreDtos.isEmpty()) {
            return ResponseEntity.badRequest().body("Parameter list is empty");
        }

        DpEntryProposalParamInspectionScoreDto firstDto = paramInspectionScoreDtos.get(0);
        Long billId = firstDto.getBillId();
        String poNo = firstDto.getPoNumber();
        Long poId = firstDto.getPoId();
        Long itemId = firstDto.getItemId();

        processScores(paramInspectionScoreDtos);

        Map<String, Object> scores = calculateScores(poNo, billId, itemId, 80, poId);

        return ResponseEntity.ok(scores);
    }


    public ResponseEntity<?> ListParamInspectionScore(String billId, String itemId, User loggedUser) {

        List<DpEntryProposalParamInspectionScore> scores = scoreRepository.getListParamScoreData(Long.parseLong(billId), Long.parseLong(itemId));


        return ResponseEntity.ok(scores);
    }

    public ResponseEntity<?> getInspectionScore(String poNo, String billId, String itemId, String poId, User loggedUser) {

        Map<String, Object> scores = calculateScores(poNo, Long.parseLong(billId), Long.parseLong(itemId), 80, Long.parseLong(poId));


        return ResponseEntity.ok(scores);
    }

    private void processScores(List<DpEntryProposalParamInspectionScoreDto> paramInspectionScoreDtos) {
        for (DpEntryProposalParamInspectionScoreDto dto : paramInspectionScoreDtos) {
            DpEntryProposalParamInspectionScore existingScore = scoreRepository.getListParamScoreduplicate(dto.getPoNumber(), dto.getBillId(), dto.getItemId(), String.valueOf(dto.getParamId()));
            DpEntryProposalParamInspectionScore newScore = new DpEntryProposalParamInspectionScore(dto);

            if (existingScore != null) {
                newScore.setId(existingScore.getId());
            }

            scoreRepository.save(newScore);
        }
    }

    private Map<String, Object> calculateScores(String poNo, Long billId, Long itemId, int qualityScore, Long poId) {
        Map<String, Object> scoreMap = new HashMap<>();

        List<DpEntryProposalParamInspectionScore> scores_all = scoreRepository.getListParamScorewithall(poNo, billId, itemId);

        List<DpEntryProposalParamInspectionScore> scores = scoreRepository.getListParamScore(poNo, billId, itemId, 0);

        float qualityValue = calculateQualityValue(scores.size(), scores_all.size(), qualityScore);

        scoreMap.put("qualityScore", qualityValue);

        Date inwardDate = getInwardDate(billId);
        Date dueDate = getDueDate(poId);
        Date poDate = getPoDate(poId);
        if (inwardDate != null && dueDate != null) {
            float deliveryValue = calculateDeliveryValue(inwardDate, dueDate, poDate, 20); // Assuming delivery score is 20
            scoreMap.put("deliveryScore", deliveryValue);
        }

        return scoreMap;
    }


    private float calculateQualityValue(int scoreSize, int totalfilled_nonfilled, int qualityScore) {

        float notfilledValue = scoreSize;

        if (totalfilled_nonfilled > 0) {
            notfilledValue = (totalfilled_nonfilled - notfilledValue) / totalfilled_nonfilled;
        }

        float valuePercentage = (notfilledValue * qualityScore);
        if (valuePercentage > 0) {
            valuePercentage = Float.parseFloat(String.format("%.2f", valuePercentage));
            return valuePercentage;

        } else {
            return 0;
        }
    }

    private Date getInwardDate(Long billId) {
        return billInwardRepository.findById(billId)
                .map(BillInward::getReceivedDate)
                .orElse(null);
    }

    private Date getDueDate(Long poId) {
        return purchaseOrderRepository.findById(poId)
                .map(PurchaseOrder::getDeliveryduedate)
                .orElse(null);
    }

    private Date getPoDate(Long poId) {
        return purchaseOrderRepository.findById(poId)
                .map(PurchaseOrder::getPoDate)
                .orElse(null);
    }

    private float calculateDeliveryValue(Date inwardDate, Date dueDate, Date poDate, int deliveryScore) {

        // Calculate the total allowed duration from PO date to due date
        long totalDurationDays = ChronoUnit.DAYS.between(poDate.toInstant(), dueDate.toInstant());

        // Calculate the actual delay (if any) from due date to inward date
        long delayDays = ChronoUnit.DAYS.between(dueDate.toInstant(), inwardDate.toInstant());

        // Check if there's a delay
        if (delayDays > 0) {
            // Calculate the delay ratio
            if (totalDurationDays > 0) {
                float delayRatio = (float) delayDays / totalDurationDays;

                // Reduce the delivery score based on the delay
                float reducedDeliveryScore = deliveryScore * (1 - delayRatio);

                // Ensure the delivery score doesn't go below zero
                return Math.max(reducedDeliveryScore, 0);
            } else {
                return 0;
            }

        } else {
            // If there's no delay, or the goods were delivered early, return the full delivery score
            return deliveryScore;
        }
    }
}
