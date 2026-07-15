package com.aria.service;


import com.fasterxml.jackson.core.JsonProcessingException;
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
public class MaterialWithInBranchReceiptService {

    @Autowired
    MaterialWithInBranchReceiptRepository materialWithInBranchReceiptRepository;

    @Autowired
    MaterialRequestRepository materialRequestRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    OtherBranchIssueRepository otherBranchIssueRepository;

    @Autowired
    MaterialIssueRepository materialIssueRepository;

    @Autowired
    MaterialWithInBranchReceiptItemRepository materialWithInBranchReceiptItemRepository;

    public ResponseEntity<?> updateMaterialWithInBranchReceipt(MaterialWithInBranchReceiptDto materialWithInBranchReceiptDto, User loggedUser) throws JsonProcessingException {

        Optional<MaterialWithInBranchReceipt> optional = materialWithInBranchReceiptRepository.findById(materialWithInBranchReceiptDto.getId());
        if (optional.isPresent()) {
            MaterialWithInBranchReceipt branchReceipt = optional.get();
            branchReceipt.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            branchReceipt.setUpdatedBy(loggedUser.getId());
            branchReceipt.setStatus(materialWithInBranchReceiptDto.getStatus());
            branchReceipt.setReceivedBy(loggedUser);
            branchReceipt.setReceivedDate(dateUtil.getISTDate());
            branchReceipt.setEntryDate(dateUtil.getISTDate());
            MaterialWithInBranchReceipt result = materialWithInBranchReceiptRepository.save(branchReceipt);
            updateMaterialWithInBranchReceiptupdateItems(materialWithInBranchReceiptDto.getWithInBranchReceiptItems(), result);
            updatePlantReceipt(result.getId());
            return new ResponseEntity<Status>(new Status("200", "material with in branch receipt updated successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("404", "not updated!!!"), HttpStatus.NOT_FOUND);
        }

    }

    void updateMaterialWithInBranchReceiptupdateItems(List<MaterialWithInBranchReceiptItemDto> withInBranchReceiptItems, MaterialWithInBranchReceipt result) {
        if (!withInBranchReceiptItems.isEmpty()) {
            for (MaterialWithInBranchReceiptItemDto receiptItemDto : withInBranchReceiptItems) {
                Optional<MaterialWithInBranchReceiptItem> materialWithInBranchReceiptItem = materialWithInBranchReceiptItemRepository.findMaterialItem(result.getId(),receiptItemDto.getItemId());
                if (materialWithInBranchReceiptItem.isPresent()) {
                    MaterialWithInBranchReceiptItem item = materialWithInBranchReceiptItem.get();
                    item.setPackageQuantity(receiptItemDto.getPackageQuantity());
                    item.setPackageType(receiptItemDto.getPackageType());
                    materialWithInBranchReceiptItemRepository.save(item);
                }
            }
        }
    }


    public ResponseEntity<?> listAll(User loggedUser, Pageable pageable) throws UsernameNotFoundException {
        Page<MaterialWithInBranchReceipt> list = materialWithInBranchReceiptRepository.findAll(pageable);
        if (!list.toList().isEmpty()) {
            List<MaterialWithInBranchReceiptDto> result = list.toList().stream().map(data -> {
                MaterialWithInBranchReceiptDto materialWithInBranchReceiptDto = new MaterialWithInBranchReceiptDto(data);
                if (data.getEntryDate() != null) {
                    materialWithInBranchReceiptDto.setEntryOnDate(dateUtil.getStringTODateF1(data.getEntryDate()));
                    materialWithInBranchReceiptDto.setEntryOnDateString(dateUtil.getStringTODate(data.getEntryDate()));
                }
                if (data.getReceivedDate() != null) {
                    materialWithInBranchReceiptDto.setReceivedOnDate(dateUtil.getStringTODateF1(data.getReceivedDate()));
                    materialWithInBranchReceiptDto.setReceivedOnDateString(dateUtil.getStringTODate(data.getEntryDate()));
                }
                materialWithInBranchReceiptDto.setIssueDate(dateUtil.getStringTODateF1(data.getIssueDate()));
                materialWithInBranchReceiptDto.setIssueDateString(dateUtil.getStringTODate(data.getIssueDate()));
                materialWithInBranchReceiptDto.setRequestDate(dateUtil.getStringTODateF1(data.getRequestDate()));
                materialWithInBranchReceiptDto.setRequestDateString(dateUtil.getStringTODate(data.getRequestDate()));
                return materialWithInBranchReceiptDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<MaterialWithInBranchReceiptDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<IntraBranchReceiptDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> filterListAll(String mrnNo, String itemName, String requestBranchName, String issueBranchName,
                                           String fromDate, String toDate, Pageable pageable) throws UsernameNotFoundException {

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


        Page<WithInBranchReceiptList> list = materialWithInBranchReceiptRepository.withInBranchReceiveList(mrnNo, itemName, requestBranchName, issueBranchName, startDate, endDate, pageable);
        if (!list.toList().isEmpty()) {
            List<WithInBranchReceiptListDto> result = list.toList().stream().map(data -> new WithInBranchReceiptListDto(data)).collect(Collectors.toList());
            PageDataDto<WithInBranchReceiptListDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<WithInBranchReceiptListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<WithInBranchReceiptListDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> getMaterialWithInBranchReceipt(String id) throws UsernameNotFoundException {
        Optional<MaterialWithInBranchReceipt> optional = materialWithInBranchReceiptRepository
                .findById(Long.parseLong(id));
        MaterialWithInBranchReceiptDto withInBranchReceiptDto = new MaterialWithInBranchReceiptDto(optional.get());
        if (optional.get().getEntryDate() != null) {
            withInBranchReceiptDto.setEntryOnDate(dateUtil.getStringTODateF1(optional.get().getEntryDate()));
            withInBranchReceiptDto.setEntryOnDateString(dateUtil.getStringTODate(optional.get().getEntryDate()));
        }
        if (optional.get().getReceivedDate() != null) {
            withInBranchReceiptDto.setReceivedOnDate(dateUtil.getStringTODateF1(optional.get().getReceivedDate()));
            withInBranchReceiptDto.setReceivedOnDateString(dateUtil.getStringTODate(optional.get().getReceivedDate()));
        }

        withInBranchReceiptDto.setIssueDate(dateUtil.getStringTODateF1(optional.get().getIssueDate()));
        withInBranchReceiptDto.setIssueDateString(dateUtil.getStringTODate(optional.get().getIssueDate()));
        withInBranchReceiptDto.setRequestDate(dateUtil.getStringTODateF1(optional.get().getRequestDate()));
        withInBranchReceiptDto.setRequestDateString(dateUtil.getStringTODate(optional.get().getRequestDate()));
        return new ResponseEntity<MaterialWithInBranchReceiptDto>(withInBranchReceiptDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteWithInPlantReceipt(String id) throws UsernameNotFoundException {
        try {
            Optional<MaterialWithInBranchReceipt> optional = materialWithInBranchReceiptRepository
                    .findById(Long.parseLong(id));
            if (optional.isPresent()) {
                MaterialWithInBranchReceipt materialWithInBranchReceipt = optional.get();
                materialWithInBranchReceiptRepository.delete(materialWithInBranchReceipt);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }


    public void updatePlantReceipt(Long id) throws JsonProcessingException {
        Optional<MaterialWithInBranchReceipt> optional = materialWithInBranchReceiptRepository.findById(id);
        if (optional.isPresent()) {
            MaterialWithInBranchReceipt branchReceipt = optional.get();
            OtherBranchIssue branchIssue = new OtherBranchIssue();
            branchIssue.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            branchIssue.setCreatedBy(branchReceipt.getUpdatedBy());
            branchIssue.setMaterialWithInBranchId(branchReceipt.getId());
            branchIssue.setRequestBranch(branchReceipt.getRequestBranch());
            branchIssue.setRequestDepartment(branchReceipt.getRequestDepartment());
            branchIssue.setRequestId(branchReceipt.getRequestId());
            branchIssue.setIssueBranch(branchReceipt.getIssueBranch());
            branchIssue.setIssueBy(branchReceipt.getIssueBy());
            branchIssue.setIssueDepartment(branchReceipt.getIssueDepartment());
            branchIssue.setIssueDepartment(branchReceipt.getIssueBy().getDepartment());
            branchIssue.setIssueId(branchReceipt.getId());
            branchIssue.setTrnNo(branchReceipt.getTrnNo());
            branchIssue.setStatus("Pending");
            OtherBranchIssue otherBranchIssue = otherBranchIssueRepository.save(branchIssue);
            if(Objects.nonNull(branchReceipt.getWithInBranchReceiptItems()) && branchReceipt.getWithInBranchReceiptItems().size() >0)
            {
                    /*for(IssueItems items : issueItems){
                        OtherBranchIssueItems issueItem = new OtherBranchIssueItems(items);
                        issueItem.setIssueItemId(rs.getId());
                        otherBranchIssueItemsRepository.save(issueItem);
                    }*/

            	 /*ObjectMapper objectMapper = new ObjectMapper();
                 String branchReceiptsItems = objectMapper.writeValueAsString(branchReceipt.getWithInBranchReceiptItems());
                 branchIssue.setOtherBranchIssueValue(branchReceiptsItems);*/

            }
            //branchIssue.setOtherBranchIssueValue(branchReceipt.getWithinBranchReceiptValue());

        }

    }

    public ResponseEntity<?> getMRNDetails(String id) throws UsernameNotFoundException {
        Optional<MaterialWithInBranchReceipt> optional = materialWithInBranchReceiptRepository.findById(Long.parseLong(id));
        MRNDetailsDto withInBranchReceiptDto = new MRNDetailsDto(optional.get());
        withInBranchReceiptDto.setIssueDate(dateUtil.getStringTODateF1(optional.get().getIssueDate()));
        withInBranchReceiptDto.setIssueDateString(dateUtil.getStringTODate(optional.get().getIssueDate()));
        withInBranchReceiptDto.setRequestDate(dateUtil.getStringTODateF1(optional.get().getRequestDate()));
        withInBranchReceiptDto.setRequestDateString(dateUtil.getStringTODate(optional.get().getRequestDate()));
        Optional<MaterialRequest> materialRequestOptional = materialRequestRepository.findById(withInBranchReceiptDto.getRequestId());
        if (materialRequestOptional.isPresent()) {
            withInBranchReceiptDto.setRequestName(materialRequestOptional.get().getRequestName());
            withInBranchReceiptDto.setRequestPerson(materialRequestOptional.get().getRequestBy().getId());
            withInBranchReceiptDto.setRequestPersonName(materialRequestOptional.get().getRequestBy().getEmployeename());
        }
        /*Optional<MaterialIssue> materialIssueOptional = materialIssueRepository.findById(withInBranchReceiptDto.getIssueId());
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (materialIssueOptional.isPresent()) {
            if (!materialIssueOptional.get().getIssueItems().isEmpty()) {
                for (IssueItems issueItems : materialIssueOptional.get().getIssueItems()) {
                    Map<String, Object> stringObjectMap = new HashMap<>();
                    stringObjectMap.put("itemName", issueItems.getItemName());
                    stringObjectMap.put("requestedQuantity", issueItems.getRequestedQuantity());
                    stringObjectMap.put("issueQuantity", issueItems.getAllocatedQuantity());
                    stringObjectMap.put("issueFromBin", issueItems.getIssuedFromBin());
                    stringObjectMap.put("issueFromBatch", issueItems.getIssuedFromBatch());
                    stringObjectMap.put("pendingQuantity", issueItems.getPendingQuantity());
                    mapList.add(stringObjectMap);
                }
            }
        }
        withInBranchReceiptDto.setStringObjectMapValue(mapList);*/
        return new ResponseEntity<MRNDetailsDto>(withInBranchReceiptDto, HttpStatus.OK);
    }

}
