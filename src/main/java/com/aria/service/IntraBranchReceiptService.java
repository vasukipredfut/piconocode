
package com.aria.service;

import com.fasterxml.jackson.databind.ObjectMapper;


import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;

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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IntraBranchReceiptService {

    @Autowired
    IntraBranchReceiptRepository intraBranchReceiptRepository;

    @Autowired
    IntraBranchRecRepository intraBranchRecRepository;

    @Autowired
    IntraBranchReceiptMappingRepository intraBranchReceiptMappingRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    OtherBranchIssueService otherBranchIssueService;

    @Autowired
    WithInBranchDistributionRepository withInBranchDistributionRepository;

    @Autowired
    OtherBranchIssueRepository otherBranchIssueRepository;

    @Autowired
    UserRepository userRepository;

    public ResponseEntity<?> updateIntraBranchReceipt(IntraBranchReceiptDto intraBranchReceiptDto, User loggedUser) {

        Optional<IntraBranchReceiptEntity> optional = intraBranchRecRepository.findById(intraBranchReceiptDto.getId());
        if (optional.isPresent()) {
            IntraBranchReceiptEntity intraBranchReceipt = optional.get();
            if ("VERIFIED".equalsIgnoreCase(intraBranchReceipt.getStatus())) {
                return new ResponseEntity<>(
                    new Status("400", "Already Completed! Cannot process again."),
                    HttpStatus.BAD_REQUEST
                );
            }
            intraBranchReceipt.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            intraBranchReceipt.setUpdatedBy(loggedUser.getId());
            intraBranchReceipt.setDocketNo(intraBranchReceiptDto.getDocketNo());
            intraBranchReceipt.setStatus("VERIFIED");
            //ObjectMapper objectMapper = new ObjectMapper();
            IntraBranchReceiptEntity branchReceipt = intraBranchRecRepository.save(intraBranchReceipt);
            updateWithInBranchDistribution(branchReceipt.getId());
            return new ResponseEntity<Status>(new Status("200", "intra plant updated  successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("404", "intra  plat not updated  successfully!!!"), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> listAll(Pageable pageable) throws UsernameNotFoundException {
        Page<IntraBranchReceipt> list = intraBranchReceiptRepository.findAll(pageable);
        if (!list.toList().isEmpty()) {
            List<IntraBranchReceiptDto> result = list.toList().stream().map(data -> {
                        IntraBranchReceiptDto branchReceiptDto = new IntraBranchReceiptDto(data);
                        if (data.getEntryDate() != null) {
                            branchReceiptDto.setEntryOnDate(dateUtil.getStringTODateF1(data.getEntryDate()));
                            branchReceiptDto.setEntryOnDateString(dateUtil.getStringTODate(data.getEntryDate()));
                        }
                        if (data.getReceivedDate() != null) {
                            branchReceiptDto.setReceivedOnDate(dateUtil.getStringTODateF1(data.getReceivedDate()));
                            branchReceiptDto.setReceivedOnDateString(dateUtil.getStringTODate(data.getReceivedDate()));
                        }
                        if (data.getIssueDate() != null) {
                            branchReceiptDto.setIssueDate(dateUtil.getStringTODateF1(data.getIssueDate()));
                            branchReceiptDto.setIssueDateString(dateUtil.getStringTODate(data.getIssueDate()));
                        }
                        return branchReceiptDto;
                    }
            ).collect(Collectors.toList());
            return new ResponseEntity<List<IntraBranchReceiptDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<IntraBranchReceiptDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> filterListAll(Long firmId,   Long userBranchId, Long deptId,  String hupId, String dktNo, String issueBranchName, String dispatchOn,
                                           String status, Pageable pageable, String from, String to) throws UsernameNotFoundException {


        Date fromDate = null;
        if (from != null) {
            fromDate = dateUtil.getStringTODateWk(from);
        }
        Date toDate = null;
        if (to != null) {
            toDate = dateUtil.getStringTODateWk(to);
        }


        Page<IntraBranchReceiptEntity> list = intraBranchRecRepository.intraBranchReceiptList(firmId , userBranchId, deptId , hupId, issueBranchName, fromDate, toDate, status, pageable);
        List<IntraBranchRecDto> recDtos = new ArrayList<>();
        for (IntraBranchReceiptEntity intraBranchReceiptEntity : list) {
            IntraBranchRecDto dto = new IntraBranchRecDto();
            dto.setId(intraBranchReceiptEntity.getId());
            dto.setDispatchBy(intraBranchReceiptEntity.getDispatchBy());
            dto.setDispatchOn(dateUtil.getDateTOStringWk(intraBranchReceiptEntity.getDispatchOn()));
            dto.setCreated_date(intraBranchReceiptEntity.getCreated_date());
            dto.setCreatedBy(intraBranchReceiptEntity.getCreatedBy());
            dto.setUpdated_date(intraBranchReceiptEntity.getUpdated_date());
            dto.setUpdatedBy(intraBranchReceiptEntity.getUpdatedBy());
            dto.setStatus(intraBranchReceiptEntity.getStatus());
            dto.setDocketNo(intraBranchReceiptEntity.getDocketNo());
            dto.setHupId(intraBranchReceiptEntity.getHupId());
            dto.setIssueBranch(intraBranchReceiptEntity.getIssueBranch().getBranchName());
            Optional<User> user = userRepository.findById(intraBranchReceiptEntity.getDispatchBy());
            if (user.isPresent()) {
                dto.setDispatchByName(user.get().getEmployeename());
            }
            if (intraBranchReceiptEntity.getDeliveryByStaff() != null) {
                Optional<User> user1 = userRepository.findById(intraBranchReceiptEntity.getDeliveryByStaff());
                if (user1.isPresent()) {
                    dto.setDeliveryStaffName(user1.get().getEmployeename());
                }
            }
            recDtos.add(dto);
        }
        if (!list.toList().isEmpty()) {
            PageDataDto<IntraBranchRecDto> pageData = new PageDataDto(recDtos, list);
            return new ResponseEntity<PageDataDto<IntraBranchRecDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<IntraBranchRecDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> getIntraBranchReceipt(Long id) throws UsernameNotFoundException {


        Optional<IntraBranchReceiptEntity> optional = intraBranchRecRepository
                .findById(id);
        if (optional.isPresent()) {
            List<OtherBranchIssueDto> result = new ArrayList<>();
            List<IntraBranchReceiptMapping> list = intraBranchReceiptMappingRepository.findByIntraBranchReceiptId(id);
            for (IntraBranchReceiptMapping intraBranchReceiptMapping : list) {
                OtherBranchIssueDto issueDto = otherBranchIssueService.getOtherBranchIssue(String.valueOf(intraBranchReceiptMapping.getOtherBranchIssueId())).getBody();
                issueDto.setHubNo(optional.get().getHupId());
                result.add(issueDto);
            }
            return new ResponseEntity<List<OtherBranchIssueDto>>(result, HttpStatus.OK);
        }
        return new ResponseEntity<List<OtherBranchIssueDto>>(new ArrayList<>(), HttpStatus.OK);
    }

    public ResponseEntity<?> deleteIntraBranchReceipt(String id) throws UsernameNotFoundException {
        try {
            Optional<IntraBranchReceipt> optional = intraBranchReceiptRepository
                    .findById(Long.parseLong(id));
            if (optional.isPresent()) {
                IntraBranchReceipt intraBranchReceipt = optional.get();
                intraBranchReceiptRepository.delete(intraBranchReceipt);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public void updateWithInBranchDistribution(Long id) {
        List<IntraBranchReceiptMapping> list = intraBranchReceiptMappingRepository.findByIntraBranchReceiptId(id);
        for (IntraBranchReceiptMapping intraBranchReceiptMapping : list) {
            Optional<OtherBranchIssue> optional = otherBranchIssueRepository.findById(intraBranchReceiptMapping.getOtherBranchIssueId());
            Optional<IntraBranchReceiptEntity> optionalIntra = intraBranchRecRepository.findById(intraBranchReceiptMapping.getIntraBranchReceiptId());
            IntraBranchReceiptEntity intraBranchReceipt = optionalIntra.get();
            OtherBranchIssue otherBranchIssue = optional.get();
            WithInBranchDistribution withInBranchDistribution = new WithInBranchDistribution();
            withInBranchDistribution.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            withInBranchDistribution.setCreatedBy(intraBranchReceipt.getUpdatedBy());
            withInBranchDistribution.setTrnNo(otherBranchIssue.getTrnNo());
            withInBranchDistribution.setHupId(intraBranchReceipt.getHupId());
            withInBranchDistribution.setDocketNo("DKT-" + otherBranchIssue.getId());
            withInBranchDistribution.setRequestBranch(otherBranchIssue.getRequestBranch());
            withInBranchDistribution.setRequestDepartment(otherBranchIssue.getRequestDepartment());
            withInBranchDistribution.setRequestId(otherBranchIssue.getRequestId());
            withInBranchDistribution.setIssueBranch(intraBranchReceipt.getIssueBranch());
            withInBranchDistribution.setIssueBy(otherBranchIssue.getIssueBy());
            withInBranchDistribution.setIssueDate(Date.from(otherBranchIssue.getUpdated_date().atZone(ZoneId.systemDefault()).toInstant()));
            withInBranchDistribution.setIssueDepartment(otherBranchIssue.getIssueDepartment());
            withInBranchDistribution.setIssueOrg(otherBranchIssue.getOrganisation());
            withInBranchDistribution.setRequestOrg(otherBranchIssue.getOrganisation());
            withInBranchDistribution.setIssueId(otherBranchIssue.getIssueId());
            withInBranchDistribution.setStatus("notIssue");
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                String branchReceiptsItems = objectMapper.writeValueAsString(otherBranchIssue.getOtherBranchIssueItems());
                withInBranchDistribution.setWithinBranchDistributionValue(branchReceiptsItems);
            }catch (Exception e){
                e.printStackTrace();
            }
            withInBranchDistributionRepository.save(withInBranchDistribution);
        }
    }

}
