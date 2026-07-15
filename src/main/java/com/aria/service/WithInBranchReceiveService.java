package com.aria.service;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.BinStockUtil;
import org.springframework.transaction.annotation.Transactional;

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
public class WithInBranchReceiveService {


    @Autowired
    WithInBranchReceiveRepository withInBranchReceiveRepository;

    @Autowired
    MaterialIssueRepository materialIssueRepository;

    @Autowired
    MaterialRequestRepository materialRequestRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    BinStockRepository binstockRepository;

    @Autowired
    GoGroupRepository goGroupRepository;

    @Autowired
    MaterialWithinBranchReceiptStoredBinDetailsRepository materialWithinBranchReceiptStoredBinDetailsRepository;

    @Autowired
    WithInBranchReceiveItemsRepository withInBranchReceiveItemsRepository;

    @Autowired
    IssueItemsRepository issueItemsRepository;

    @Autowired
    BinStockUtil binStockUtil;


    @Transactional
    public ResponseEntity<?> updateWithInBranchReceive(WithInBranchReceiveDto receiveDto, User loggedUser) {
        Optional<WithInBranchReceive> optional = withInBranchReceiveRepository.findById(receiveDto.getId());
        if (optional.isPresent()) {
            WithInBranchReceive withInBranchReceive = optional.get();
            withInBranchReceive.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            withInBranchReceive.setUpdatedBy(loggedUser.getId());
            withInBranchReceive.setEntryDate(dateUtil.getISTDate());
            withInBranchReceive.setReceivedDate(dateUtil.getISTDate());
            User user = new User();
            user.setId(receiveDto.getReceiveUserId());
            withInBranchReceive.setReceivedBy(user);
            WithInBranchReceive result = withInBranchReceiveRepository.save(withInBranchReceive);

            Optional<MaterialIssue> materialIssue = materialIssueRepository.findById(result.getIssueId());

            try {
                List<WithInBranchReceiveItemDto> issueItemsList = receiveDto.getWithInBranchReceiveItems();

                for (WithInBranchReceiveItemDto issueItems : issueItemsList) {

                    WithInBranchReceiveItems receiveItems = new WithInBranchReceiveItems(issueItems);
                    receiveItems.setWithinId(result.getId());
                    if (receiveItems.getReceiveBranchId() == null) {
                        receiveItems.setReceiveBranchId(result.getRequestBranch().getId());
                    }
                    if (receiveItems.getReceiveDeptId() == null) {
                        receiveItems.setReceiveDeptId(result.getRequestDepartment().getId());
                    }
                    WithInBranchReceiveItems receiveItemsResult = withInBranchReceiveItemsRepository.save(receiveItems);

                    System.out.println("issue batch = " + receiveItemsResult.getIssuedFromBatch() + " issue bin = " + Long.parseLong(receiveItemsResult.getIssuedFromBin()) + " issue dept = " +
                            " issue dept = " + receiveItemsResult.getIssueDeptId() + " issue branch = " + receiveItemsResult.getIssueBranchId() + " issue quantity = " +
                            receiveItemsResult.getQuantity() + " lot = " + Double.parseDouble(receiveItemsResult.getSelectLot()));
                    Optional<BinStock> optionalRecord = null;
                    if(receiveItems.getBinStockId() != null){

                        optionalRecord = binstockRepository.findById(receiveItems.getBinStockId());
                    } else {
                        optionalRecord = binstockRepository.findByBatchNoAndBinIdAndLockStatusAndLockTypeAndDeptAndBranchAndIssuedQty(
                                issueItems.getIssuedFromBatch(), Long.parseLong(issueItems.getIssuedFromBin()), "Y", "BU",
                                issueItems.getDeptId(), issueItems.getBranchId(), Double.parseDouble(issueItems.getQuantity()), Double.parseDouble(receiveItemsResult.getSelectLot()));
                    }

                    if (optionalRecord.isPresent()) {

                        if (!issueItems.getBinDetails().isEmpty()) {

                            for (WithInBranchRecieveBinDetailsDto binObj : issueItems.getBinDetails()) {

                                binStockUtil.updateMaterialTransActionONIssueBinStockAuditLog(optionalRecord.get(), loggedUser, "MaterialIssue", "I");

                                MaterialWithinBranchReceiptStoredBinDetails binDetails = new MaterialWithinBranchReceiptStoredBinDetails(binObj);
                                binDetails.setWithinItemId(receiveItemsResult.getId());
                                binDetails.setItemId(receiveItemsResult.getItemId());
                                Optional<GoGroup> goGroupOptional = goGroupRepository.findBygoName(binDetails.getBinId());
                                GoGroup goGroup = null;
                                if (goGroupOptional.isPresent()) {
                                    goGroup = goGroupOptional.get();
                                    binDetails.setBinNo(goGroupOptional.get().getGCode());
                                } else {
                                    return new ResponseEntity<Status>(new Status("204", "BinId not Present"), HttpStatus.OK);
                                }
                                MaterialWithinBranchReceiptStoredBinDetails resultBinDetails = materialWithinBranchReceiptStoredBinDetailsRepository.save(binDetails);

                                BinStock newBinstock = new BinStock();
                                double avgCost = 0.0;
                                if (optionalRecord.get().getAvgCost() != null) {
                                    avgCost = optionalRecord.get().getAvgCost();
                                }
                                newBinstock.setAvgCost(avgCost);
                                newBinstock.setBinType(optionalRecord.get().getBinType());
                                newBinstock.setBatchNo(optionalRecord.get().getBatchNo());
                                newBinstock.setBatchId(optionalRecord.get().getBatchId());
                                newBinstock.setCategory(optionalRecord.get().getCategory());
                                newBinstock.setCid(optionalRecord.get().getCid());
                                newBinstock.setCost(avgCost * binObj.getAllottedQuantity());
                                if (result.getRequestDepartment() != null) {
                                    newBinstock.setDept(result.getRequestDepartment().getId());
                                }
                                if (result.getRequestBranch() != null) {
                                    newBinstock.setBranch(result.getRequestBranch().getId());
                                }
                                if (goGroup != null) {
                                    newBinstock.setBinName(resultBinDetails.getBinId());
                                    newBinstock.setBinId(resultBinDetails.getBinNo());
                                    newBinstock.setGid(goGroup.getGid());
                                }
                                newBinstock.setLot(optionalRecord.get().getLot());
                                newBinstock.setOilFFA(optionalRecord.get().getOilFFA());
                                newBinstock.setLTrnNo(optionalRecord.get().getLTrnNo());
                                newBinstock.setInwarDate(optionalRecord.get().getInwarDate());
                                newBinstock.setStkQty(binObj.getAllottedQuantity());
                                newBinstock.setRefId(optionalRecord.get().getRefId());
                                newBinstock.setSeedcolor(optionalRecord.get().getSeedcolor());
                                newBinstock.setSeedMoisture(optionalRecord.get().getSeedMoisture());
                                newBinstock.setLocation(optionalRecord.get().getLocation());
                                newBinstock.setCampName(optionalRecord.get().getCampName());
                                newBinstock.setRefId(optionalRecord.get().getRefId());
                                newBinstock.setLockStatus("N");
                                newBinstock.setAdate(dateUtil.getISTDate());
                                BinStock rs = binstockRepository.save(newBinstock);
                                binStockUtil.updateBinLogMaterialTransaction(materialIssue.get(), rs, loggedUser, "R");
                                binStockUtil.updateMaterialTransONReceiveActionBinStockAuditLog(optionalRecord.get(), loggedUser, "MaterialReceive", "R");

                            }
                            binstockRepository.delete(optionalRecord.get());

                        } else {
                            System.out.println("issue batch = " + 1);
                            return new ResponseEntity<Status>(new Status("204", "With In Branch receive not updated successfully!!!"), HttpStatus.OK);
                        }

                    } else {
                        return new ResponseEntity<Status>(new Status("204", "not Issued this product!!!"), HttpStatus.OK);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("issue batch = " + 2);
                return new ResponseEntity<Status>(new Status("204", "With In Branch Receive not updated successfully!!!"), HttpStatus.OK);
            }
            result.setStatus("Completed");
            withInBranchReceiveRepository.save(result);
            return new ResponseEntity<Status>(new Status("200", "With in branch receive updated successfully!!!"), HttpStatus.OK);
        } else {
            System.out.println("issue batch = " + 3);
            return new ResponseEntity<Status>(new Status("204", "With in branch receive not updated successfully!!!"), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> listAll(Pageable pageable) throws UsernameNotFoundException {
        Page<WithInBranchReceive> list = withInBranchReceiveRepository.findAll(pageable);
        if (!list.toList().isEmpty()) {
            List<WithInBranchReceiveDto> result = list.toList().stream().map(data -> {
                        WithInBranchReceiveDto withInBranchReceiveDto = new WithInBranchReceiveDto(data);
                        if (data.getRequestDate() != null) {
                            withInBranchReceiveDto.setRequestDate(dateUtil.getStringTODateF1(data.getRequestDate()));
                            withInBranchReceiveDto.setReceivedOnDateString(dateUtil.getStringTODate(data.getRequestDate()));
                        }
                        if (data.getEntryDate() != null) {
                            withInBranchReceiveDto.setEntryOnDate(dateUtil.getStringTODateF1(data.getEntryDate()));
                            withInBranchReceiveDto.setEntryOnDateString(dateUtil.getStringTODate(data.getEntryDate()));
                        }
                        if (data.getReceivedDate() != null) {
                            withInBranchReceiveDto.setReceivedOnDate(dateUtil.getStringTODateF1(data.getReceivedDate()));
                            withInBranchReceiveDto.setReceivedOnDateString(dateUtil.getStringTODate(data.getReceivedDate()));
                        }
                        if (data.getIssueDate() != null) {
                            withInBranchReceiveDto.setIssueDate(dateUtil.getStringTODateF1(data.getIssueDate()));
                            withInBranchReceiveDto.setIssueDateString(dateUtil.getStringTODate(data.getIssueDate()));
                        }
                        return withInBranchReceiveDto;
                    })
                    .collect(Collectors.toList());
            return new ResponseEntity<List<WithInBranchReceiveDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<WithInBranchReceiveDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> filterListAll(Long firmId, Long branch , Long deptId , String mrnNo, String itemName, String requestDeptName, String issueDeptName,
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

        Page<WithInBranchReceiveList> list = withInBranchReceiveRepository.withInBranchReceiveList( firmId ,  branch, deptId, mrnNo, itemName, requestDeptName, issueDeptName, startDate, endDate, status, pageable);
        if (!list.toList().isEmpty()) {
            List<WithInBranchReceiveListDto> result = list.toList().stream().map(data -> new WithInBranchReceiveListDto(data)).collect(Collectors.toList());
            PageDataDto<WithInBranchReceiveListDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<WithInBranchReceiveListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<WithInBranchReceiveListDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getWithInBranchReceive(String id) throws UsernameNotFoundException {
        Optional<WithInBranchReceive> optional = withInBranchReceiveRepository.findById(Long.parseLong(id));
        if (optional.isPresent()) {
            WithInBranchReceive withInBranchReceive = optional.get();
            System.out.println("withInBranchReceive.getIssueId() = "+withInBranchReceive.getIssueId());
            List<IssueItemsValues> issueItemsList = issueItemsRepository.findBy(withInBranchReceive.getIssueId());
            List<IssueItemsValuesDto> valuesDtoList = issueItemsList.stream().map(sa -> new IssueItemsValuesDto(sa)).collect(Collectors.toList());
            WithInBranchReceiveDto receiveDto = new WithInBranchReceiveDto(optional.get(), valuesDtoList);

            Optional<MaterialRequest> optionalMaterialRequest = materialRequestRepository.findById(withInBranchReceive.getRequestId());
            if (optionalMaterialRequest.isPresent()) {
                receiveDto.setRequestName(optionalMaterialRequest.get().getRequestName());
            }
            List<WithInBranchReceiveItems> receiveItemsList = withInBranchReceive.getWithInBranchReceiveItems();
            List<WithInBranchReceiveItemDto> receiveItemsDtoList = new ArrayList<>();
            for (WithInBranchReceiveItems receiveItems : receiveItemsList) {
                WithInBranchReceiveItemDto receiveItemDto = new WithInBranchReceiveItemDto(receiveItems);
                List<MaterialWithinBranchReceiptStoredBinDetails> binDetailsList = materialWithinBranchReceiptStoredBinDetailsRepository
                        .findByWithinItemId(receiveItems.getId());
                List<WithInBranchRecieveBinDetailsDto> binDetailsDtoList = binDetailsList.stream()
                        .map(WithInBranchRecieveBinDetailsDto::new).collect(Collectors.toList());
                receiveItemDto.setBinDetails(binDetailsDtoList);
                receiveItemsDtoList.add(receiveItemDto);
            }
            receiveDto.setWithInBranchReceiveItems(receiveItemsDtoList);

            if (withInBranchReceive.getRequestDate() != null) {
                receiveDto.setRequestDate(dateUtil.getStringTODateF1(withInBranchReceive.getRequestDate()));
                receiveDto.setReceivedOnDateString(dateUtil.getStringTODate(withInBranchReceive.getRequestDate()));
            }
            if (withInBranchReceive.getEntryDate() != null) {
                receiveDto.setEntryOnDate(dateUtil.getStringTODateF1(withInBranchReceive.getEntryDate()));
                receiveDto.setEntryOnDateString(dateUtil.getStringTODate(withInBranchReceive.getEntryDate()));
            }
            if (withInBranchReceive.getReceivedDate() != null) {
                receiveDto.setReceivedOnDate(dateUtil.getStringTODateF1(withInBranchReceive.getReceivedDate()));
                receiveDto.setReceivedOnDateString(dateUtil.getStringTODate(withInBranchReceive.getReceivedDate()));
            }
            if (withInBranchReceive.getIssueDate() != null) {
                receiveDto.setIssueDate(dateUtil.getStringTODateF1(withInBranchReceive.getIssueDate()));
                receiveDto.setIssueDateString(dateUtil.getStringTODate(withInBranchReceive.getIssueDate()));
            }
            return new ResponseEntity<WithInBranchReceiveDto>(receiveDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> deleteWithInBranchReceive(String id) throws UsernameNotFoundException {
        try {
            Optional<WithInBranchReceive> optional = withInBranchReceiveRepository
                    .findById(Long.parseLong(id));
            if (optional.isPresent()) {
                WithInBranchReceive receiveDto = optional.get();
                withInBranchReceiveRepository.delete(receiveDto);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public ResponseEntity<?> getMRNDetails(String id) throws UsernameNotFoundException {
        Optional<WithInBranchReceive> optional = withInBranchReceiveRepository.findById(Long.parseLong(id));
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
        Optional<MaterialIssue> materialIssueOptional = materialIssueRepository.findById(withInBranchReceiptDto.getIssueId());
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
        withInBranchReceiptDto.setStringObjectMapValue(mapList);
        return new ResponseEntity<MRNDetailsDto>(withInBranchReceiptDto, HttpStatus.OK);
    }


}
