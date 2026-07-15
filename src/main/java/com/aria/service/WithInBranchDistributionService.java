package com.aria.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.aria.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;

@Service
public class WithInBranchDistributionService {


    @Autowired
    WithInBranchDistributionRepository withInBranchDistributionRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    OtherBranchReceiptRepository otherBranchReceiptRepository;

    @Autowired
    MaterialIssueRepository materialIssueRepository;

    @Autowired
    MaterialRequestRepository materialRequestRepository;
    
    @Autowired
    MaterialIssueService materialIssueService;


    public ResponseEntity<?> updateWithInBranchDistribution(WithInBranchDistributionDto withInBranchDistributionDto, User loggedUser) {
        Optional<WithInBranchDistribution> optional = withInBranchDistributionRepository
                .findById(withInBranchDistributionDto.getId());
        if (optional.isPresent()) {
            WithInBranchDistribution withInBranchDistribution = optional.get();
            if ("Issued".equalsIgnoreCase(withInBranchDistribution.getStatus())) {
                return new ResponseEntity<>(
                    new Status("400", "Already Completed! Cannot process again."),
                    HttpStatus.BAD_REQUEST
                );
            }
            withInBranchDistribution.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            withInBranchDistribution.setUpdatedBy(loggedUser.getId());
            withInBranchDistribution.setStatus("Issued");
            withInBranchDistribution.setReceivedDate(dateUtil.getISTDate());
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                withInBranchDistribution.setWithinBranchDistributionValue(objectMapper.writeValueAsString(withInBranchDistributionDto.getWithInBranchDistributionValue()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            WithInBranchDistribution branchDistribution = withInBranchDistributionRepository.save(withInBranchDistribution);
            updateOtherBranchReceipt(branchDistribution.getId());
            return new ResponseEntity<Status>(new Status("200", "with in branch distribution updated  successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("404", "with in branch distribution not updated  successfully!!!"), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> filterListAll(Long firmId, Long userBranchId, Long dept , String hupId, String dktNo, String requestDeptName, String issueBranchName,
                                           String fromDate, String toDate, String status, Pageable pageable) throws UsernameNotFoundException {

        Date startDate = null;
        Date endDate = null;

        if (fromDate != null && toDate != null) {
            startDate = dateUtil.getStringTODateWk(fromDate);
            endDate = dateUtil.getStringTODateWk(toDate);
        } else if (fromDate != null && toDate == null) {
            startDate = dateUtil.getStringTODateWk(fromDate);
        } else if (fromDate == null && toDate != null) {
            endDate = dateUtil.getStringTODateWk(toDate);
        }
        /*Long id = null;
        if (mrnNo != null && mrnNo.contains("-")) {
            List<String> splitId = new ArrayList<>(Arrays.asList(mrnNo.split("-")));
            id = Long.valueOf(splitId.get(1).trim());
        }
        Long dktId = null;
        if (dktNo != null && dktNo.contains("-")) {
            List<String> splitId = new ArrayList<>(Arrays.asList(dktNo.split("-")));
            dktId = Long.valueOf(splitId.get(1).trim());
        }*/
        Page<IntraBranchReceiptList> list = withInBranchDistributionRepository.withInBranchDistributionList(firmId , userBranchId, dept , hupId, dktNo, requestDeptName, issueBranchName, startDate, endDate, status, pageable);
        if (!list.toList().isEmpty()) {
            List<IntraBranchReceiptListDto> result = list.toList().stream().map(data -> new IntraBranchReceiptListDto(data)).collect(Collectors.toList());
            PageDataDto<IntraBranchReceiptListDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<IntraBranchReceiptListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<IntraBranchReceiptListDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> listAll(Pageable pageable) throws UsernameNotFoundException {
        Page<WithInBranchDistribution> list = withInBranchDistributionRepository.findAll(pageable);
        if (!list.toList().isEmpty()) {
            List<WithInBranchDistributionDto> result = list.toList().stream().map(data -> {
                WithInBranchDistributionDto branchDistributionDto = new WithInBranchDistributionDto(data);
                        if (data.getReceivedDate() != null) {
                            branchDistributionDto.setReceivedOnDate(dateUtil.getStringTODateF1(data.getReceivedDate()));
                            branchDistributionDto.setReceivedOnDateString(dateUtil.getStringTODate(data.getReceivedDate()));
                        }
                        if (data.getIssueDate() != null) {
                            branchDistributionDto.setIssueDate(dateUtil.getStringTODateF1(data.getIssueDate()));
                            branchDistributionDto.setIssueDateString(dateUtil.getStringTODate(data.getIssueDate()));
                        }




                        return branchDistributionDto;
                    }
            ).collect(Collectors.toList());
            return new ResponseEntity<List<WithInBranchDistributionDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<WithInBranchDistributionDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getWithInBranchDistribution(String id) throws UsernameNotFoundException {
        Optional<WithInBranchDistribution> optional = withInBranchDistributionRepository
                .findById(Long.parseLong(id));
        WithInBranchDistributionDto branchDistributionDto = new WithInBranchDistributionDto(optional.get());
        if (optional.get().getReceivedDate() != null) {
            branchDistributionDto.setReceivedOnDate(dateUtil.getStringTODateF1(optional.get().getReceivedDate()));
            branchDistributionDto.setReceivedOnDateString(dateUtil.getStringTODate(optional.get().getReceivedDate()));
        }
        if (optional.get().getIssueDate() != null) {
            branchDistributionDto.setIssueDate(dateUtil.getStringTODateF1(optional.get().getIssueDate()));
            branchDistributionDto.setIssueDateString(dateUtil.getStringTODate(optional.get().getIssueDate()));
        }
        Optional<MaterialIssue> request = materialIssueRepository.findById(branchDistributionDto.getIssueId());
		if (request.isPresent()) {
			Optional<MaterialRequest> optionalMaterialRequest = materialRequestRepository
					.findById(optional.get().getRequestId());				
            if (optionalMaterialRequest.isPresent())
            	branchDistributionDto.setWithInBranchIssueBriefValue(materialIssueService
						.getIssueItemBrief(optionalMaterialRequest.get(), request.get().getId()));
		}
        return new ResponseEntity<WithInBranchDistributionDto>(branchDistributionDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteWithInBranchDistribution(String id) throws UsernameNotFoundException {
        try {
            Optional<WithInBranchDistribution> optional = withInBranchDistributionRepository
                    .findById(Long.parseLong(id));
            if (optional.isPresent()) {
                WithInBranchDistribution branchDistribution = optional.get();
                withInBranchDistributionRepository.delete(branchDistribution);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public void updateOtherBranchReceipt(Long id) {
        Optional<WithInBranchDistribution> optional = withInBranchDistributionRepository.findById(id);
        if (optional.isPresent()) {
            WithInBranchDistribution branchDistribution = optional.get();
            OtherBranchReceipt otherBranchReceipt = new OtherBranchReceipt();
            otherBranchReceipt.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            otherBranchReceipt.setCreatedBy(branchDistribution.getUpdatedBy());
            otherBranchReceipt.setHupId(branchDistribution.getHupId());
            otherBranchReceipt.setDocketNo(branchDistribution.getDocketNo());
            otherBranchReceipt.setRequestBranch(branchDistribution.getRequestBranch());
            otherBranchReceipt.setRequestDepartment(branchDistribution.getRequestDepartment());
            otherBranchReceipt.setRequestId(branchDistribution.getRequestId());
            otherBranchReceipt.setIssueBranch(branchDistribution.getIssueBranch());
            otherBranchReceipt.setIssueBy(branchDistribution.getIssueBy());
            otherBranchReceipt.setIssueDate(branchDistribution.getIssueDate());
            otherBranchReceipt.setReceivedDate(branchDistribution.getReceivedDate());
            otherBranchReceipt.setReceivedBy(branchDistribution.getReceivedBy());
            otherBranchReceipt.setIssueDepartment(branchDistribution.getIssueDepartment());
            otherBranchReceipt.setIssueOrg(branchDistribution.getIssueOrg());
            otherBranchReceipt.setRequestOrg(branchDistribution.getRequestOrg());
            otherBranchReceipt.setIssueId(branchDistribution.getIssueId());
            otherBranchReceipt.setStatus("notIssue");
            otherBranchReceipt.setTrnNo(branchDistribution.getTrnNo());
            otherBranchReceipt.setOtherBranchReceiptValue(branchDistribution.getWithinBranchDistributionValue());
            otherBranchReceiptRepository.save(otherBranchReceipt);
        }
    }
}
