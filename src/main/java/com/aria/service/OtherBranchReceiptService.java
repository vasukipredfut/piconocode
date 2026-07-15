
package com.aria.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aria.dao.*;
import com.aria.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.dto.OtherBranchReceiptDto;
import com.aria.dto.OtherBranchReceiptList;
import com.aria.dto.OtherBranchReceiptListDto;
import com.aria.dto.PageDataDto;
import com.aria.utils.BinStockUtil;
import com.aria.utils.DateUtil;

@Service
public class OtherBranchReceiptService {

    @Autowired
    OtherBranchReceiptRepository otherBranchReceiptRepository;

    @Autowired
    BinStockRepository binstockRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    GoGroupRepository goGroupRepository;

    @Autowired
    MaterialRequestRepository materialRequestRepository;

    @Autowired
    MaterialIssueService materialIssueService;

    @Autowired
    MaterialIssueRepository materialIssueRepository;

    @Autowired
    BinStockUtil binStockUtil;

    @Autowired
    BinDetailsRepository binDetailsRepository;

    @Autowired
    IssueItemsRepository issueItemsRepository;

    @Autowired
    BlendingRequestAndPreprocessBinMapRepository blendingRequestAndPreprocessBinMapRepository;

    @Autowired
    BlendingRequestReceiveBinMapRepository blendingRequestReceiveBinMapRepository;


    @Autowired
    ProductMasterRepository productMasterRepository;


    @Autowired
    BlendingAdviceStockDetailsRepository blendingAdviceStockDetailsRepository;


    @Transactional
    public ResponseEntity<?> updateOtherBranchReceipt(OtherBranchReceiptDto otherBranchReceiptDto, User loggedUser) {

        Optional<OtherBranchReceipt> optional = otherBranchReceiptRepository.findById(otherBranchReceiptDto.getId());
        boolean deliteLock = false;
        if (optional.isPresent()) {

            OtherBranchReceipt otherBranchReceipt = optional.get();
            if ("Issued".equalsIgnoreCase(otherBranchReceipt.getStatus())) {
                return new ResponseEntity<>(
                    new Status("400", "Already Completed! Cannot process again."),
                    HttpStatus.BAD_REQUEST
                );
            }
            Optional<MaterialIssue> materialIssue = materialIssueRepository.findById(otherBranchReceipt.getIssueId());

            Optional<MaterialRequest> materialRequest = materialRequestRepository.findById(otherBranchReceipt.getRequestId());
            if (materialRequest.isPresent()) {
                if (materialRequest.get().getRequestName().equalsIgnoreCase("Delite Bag Transfer")) {
                    deliteLock = true;
                }
            }

            otherBranchReceipt.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            otherBranchReceipt.setUpdatedBy(loggedUser.getId());
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                List<OtherBranchIssueItems> otherBranchIssueItems = otherBranchReceiptDto.getOtherBranchReceiptValue();

                //System.out.println("------###########------");

                for (OtherBranchIssueItems issueItems : otherBranchIssueItems) {

                    /*System.out.println("------###########------");
                    System.out.println("Batch == " + issueItems.getIssuedFromBatch()
                            + " binName == " + Long.parseLong(issueItems.getIssuedFromBin()) +
                            " deptId == " + issueItems.getDeptId() + " branchIs == " + issueItems.getBranchId() + " alloQty == " +
                            Double.parseDouble(issueItems.getAllocatedQuantity()) + " lot === " + issueItems.getSelectLot());
                    System.out.println("------###########------");*/
                    /*Optional<BinStock> optionalRecord = binstockRepository
                            .findByBatchNoAndBinIdAndLockStatusAndLockTypeAndDeptAndBranchAndIssuedQtyANDTrnNo( materialIssue.get().getTrnNo() ,
                                    issueItems.getIssuedFromBatch(), Long.parseLong(issueItems.getIssuedFromBin()), "Y",
                                    "BU", issueItems.getDeptId(), issueItems.getBranchId(),
                                    Double.parseDouble(issueItems.getAllocatedQuantity()),
                                    Double.parseDouble(issueItems.getSelectLot()));*/
                    Optional<BinStock> optionalRecord = binstockRepository.findById(issueItems.getBinStockId());
                    /*System.out.println("------###########------");
                    System.out.println(" bindetails == " + issueItems.getBinDetails());
                    System.out.println("------###########------");*/

                    if (optionalRecord.isPresent() && !issueItems.getBinDetails().isEmpty()) {

                        for (BinDetails binObj : issueItems.getBinDetails()) {

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
                            if (otherBranchReceipt.getRequestDepartment() != null) {
                                newBinstock.setDept(otherBranchReceipt.getRequestDepartment().getId());
                            }
                            if (otherBranchReceipt.getRequestBranch() != null) {
                                newBinstock.setBranch(otherBranchReceipt.getRequestBranch().getId());
                            }
                            newBinstock.setInwarDate(optionalRecord.get().getInwarDate());
                            newBinstock.setLot(optionalRecord.get().getLot());
                            newBinstock.setOilFFA(optionalRecord.get().getOilFFA());
                            newBinstock.setLTrnNo(optionalRecord.get().getLTrnNo());
                            newBinstock.setMrnStatus("N");
                            Optional<GoGroup> goGroupOptional = goGroupRepository.findBygoName(binObj.getBinid());
                            if (goGroupOptional.isPresent()) {
                                newBinstock.setBinName(goGroupOptional.get().getGoName());
                                newBinstock.setBinId(goGroupOptional.get().getGCode());
                                newBinstock.setGid(goGroupOptional.get().getGid());
                            }
                            newBinstock.setStkQty(binObj.getAllottedQuantity());
                            newBinstock.setRefId(optionalRecord.get().getRefId());
                            newBinstock.setSeedcolor(optionalRecord.get().getSeedcolor());
                            newBinstock.setSeedMoisture(optionalRecord.get().getSeedMoisture());
                            newBinstock.setLocation(optionalRecord.get().getLocation());
                            newBinstock.setCampName(optionalRecord.get().getCampName());
                            if (deliteLock) {
                                newBinstock.setLockStatus("Y");
                                newBinstock.setLockType("DT");
                            } else {
                                newBinstock.setLockStatus("N");
                            }
                            newBinstock.setAdate(dateUtil.getISTDate());
                            BinStock rs = binstockRepository.save(newBinstock);
                            updateReceiveBinDetails(rs, loggedUser , optionalRecord.get());
                            binStockUtil.updateBinLogMaterialTransaction(materialIssue.get(), rs, loggedUser, "R");
                        }
                        binstockRepository.delete(optionalRecord.get());
                    } else {
                        throw new RuntimeException(" Item not issued please check Issue Branch!!!");
                    }
                }
                otherBranchReceipt.setOtherBranchReceiptValue(objectMapper.writeValueAsString(otherBranchReceiptDto.getOtherBranchReceiptValue()));

            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<Status>(new Status("500", "Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            otherBranchReceipt.setStatus("Issued");
            otherBranchReceiptRepository.save(otherBranchReceipt);
            updateBlendingReceiveBin(otherBranchReceipt);
            return new ResponseEntity<Status>(new Status("200", "Other branch receipt updated  successfully!!!"),
                    HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("404", "Other branch receipt not updated  successfully!!!"),
                    HttpStatus.OK);
        }
    }

    public ResponseEntity<?> listAll(Pageable pageable) throws UsernameNotFoundException {
        Page<OtherBranchReceipt> list = otherBranchReceiptRepository.findAll(pageable);
        if (!list.toList().isEmpty()) {
            List<OtherBranchReceiptDto> result = list.toList().stream().map(data -> {
                OtherBranchReceiptDto otherBranchReceiptDto = new OtherBranchReceiptDto(data);
                if (data.getReceivedDate() != null) {
                    otherBranchReceiptDto.setReceivedOnDate(dateUtil.getStringTODateF1(data.getReceivedDate()));
                    otherBranchReceiptDto.setReceivedOnDateString(dateUtil.getStringTODate(data.getReceivedDate()));
                }
                if (data.getIssueDate() != null) {
                    otherBranchReceiptDto.setIssueDate(dateUtil.getStringTODateF1(data.getIssueDate()));
                    otherBranchReceiptDto.setIssueDateString(dateUtil.getStringTODate(data.getIssueDate()));
                }
                return otherBranchReceiptDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<OtherBranchReceiptDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<OtherBranchReceiptDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> filterListAll( Long firmId , Long branchId, Long deptId, Long requestId, String hupId, String dktNo,
                                           String issueDeptName, String issueBranchName, String fromDate, String toDate, String status,
                                           Pageable pageable) throws UsernameNotFoundException {

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
        Page<OtherBranchReceiptList> list = otherBranchReceiptRepository.otherBranchReceiptList(firmId , branchId, deptId, hupId,
                dktNo, issueDeptName, issueBranchName, startDate, endDate, status, pageable);
        if (!list.toList().isEmpty()) {
            List<OtherBranchReceiptListDto> result = list.toList().stream()
                    .map(data -> new OtherBranchReceiptListDto(data)).collect(Collectors.toList());
            PageDataDto<OtherBranchReceiptListDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<OtherBranchReceiptListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<OtherBranchReceiptListDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getOtherBranchReceipt(String id) throws UsernameNotFoundException {

        Optional<OtherBranchReceipt> optional = otherBranchReceiptRepository.findById(Long.parseLong(id));
        OtherBranchReceiptDto otherBranchReceiptDto = new OtherBranchReceiptDto(optional.get());
        if (optional.get().getReceivedDate() != null) {
            otherBranchReceiptDto.setReceivedOnDate(dateUtil.getStringTODateF1(optional.get().getReceivedDate()));
            otherBranchReceiptDto.setReceivedOnDateString(dateUtil.getStringTODate(optional.get().getReceivedDate()));
        }
        if (optional.get().getIssueDate() != null) {
            otherBranchReceiptDto.setIssueDate(dateUtil.getStringTODateF1(optional.get().getIssueDate()));
            otherBranchReceiptDto.setIssueDateString(dateUtil.getStringTODate(optional.get().getIssueDate()));
        }
        Optional<MaterialIssue> request = materialIssueRepository.findById(otherBranchReceiptDto.getIssueId());
        if (request.isPresent()) {
            otherBranchReceiptDto.setRequestName(request.get().getRequestName());
            Optional<MaterialRequest> optionalMaterialRequest = materialRequestRepository.findById(optional.get().getRequestId());
            if (optionalMaterialRequest.isPresent())
                otherBranchReceiptDto.setOtherBranchReceiptBriefValue(materialIssueService.getIssueItemBrief(optionalMaterialRequest.get(), request.get().getId()));

        }
        return new ResponseEntity<OtherBranchReceiptDto>(otherBranchReceiptDto, HttpStatus.OK);

    }

    public ResponseEntity<?> deleteOtherBranchReceipt(String id) throws UsernameNotFoundException {
        try {
            Optional<OtherBranchReceipt> optionalOtherPlantIssue = otherBranchReceiptRepository
                    .findById(Long.parseLong(id));
            if (optionalOtherPlantIssue.isPresent()) {
                OtherBranchReceipt otherBranchReceipt = optionalOtherPlantIssue.get();
                otherBranchReceiptRepository.delete(otherBranchReceipt);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public boolean isValidRequestForUser(Long branchId, Long deptId) {
        List<MaterialRequest> materialRequest = materialRequestRepository.findByRequestById(branchId, deptId);
        return !materialRequest.isEmpty();
    }

    public void updateReceiveBinDetails(BinStock binStock, User loggedUser , BinStock lockBinStock) {
        BinDetails binDetails = new BinDetails();
        binDetails.setBatchno(binStock.getBatchNo());
        binDetails.setBinid(binStock.getBinName());
        binDetails.setTrn_no(binStock.getLTrnNo());
        binDetails.setAllottedQuantity(binStock.getStkQty());
        binDetails.setReceiveBinStockId(binStock.getId());
        binDetails.setBinStockId(lockBinStock.getId());
        binDetails.setUpdatedBy(loggedUser.getId());
        binDetails.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        binDetailsRepository.save(binDetails);
    }

    public void updateBlendingReceiveBin(OtherBranchReceipt otherBranchReceipt) {

        if(otherBranchReceipt.getStatus().equalsIgnoreCase("Issued")) {

            Optional<BlendingRequestAndPreprocessBinMap> optional = blendingRequestAndPreprocessBinMapRepository.findByMaterialRequest(otherBranchReceipt.getRequestId());

            if (optional.isPresent()) {

                List<BinStock> binStockList = binstockRepository.findByLTrnNo(otherBranchReceipt.getTrnNo());

                if (!binStockList.isEmpty()) {

                    for (BinStock binStock : binStockList) {

                        Optional<BlendingAdviceStockDetails> blendingAdviceStockDetails = blendingAdviceStockDetailsRepository.findBy(optional.get().getBlendingDetailId(),  binStock.getRefId(), binStock.getBatchNo() );
                        Optional<BlendingRequestReceiveBinMap> optionalBlendingRequestReceiveBinMap = blendingRequestReceiveBinMapRepository.findByReceive(otherBranchReceipt.getIssueId(), otherBranchReceipt.getTrnNo(), binStock.getRefId(), binStock.getBinName() , binStock.getBatchNo(),  binStock.getLot(),"Blending_Material_Receive" );

                        BlendingRequestReceiveBinMap blendingRequestReceiveBinMap = null;
                        if(optionalBlendingRequestReceiveBinMap.isPresent()) {
                            blendingRequestReceiveBinMap = optionalBlendingRequestReceiveBinMap.get();
                        }else {
                            blendingRequestReceiveBinMap = new BlendingRequestReceiveBinMap();
                        }

                        blendingRequestReceiveBinMap.setBlendingDetailId(optional.get().getBlendingDetailId());
                        if(blendingAdviceStockDetails.isPresent()) {
                            blendingRequestReceiveBinMap.setCasgroup(blendingAdviceStockDetails.get().getCasGroup());
                        }
                        blendingRequestReceiveBinMap.setType("Blending_Material_Receive");
                        blendingRequestReceiveBinMap.setTypeId(otherBranchReceipt.getIssueId());
                        blendingRequestReceiveBinMap.setBinNo(binStock.getBinName());
                        blendingRequestReceiveBinMap.setQty(binStock.getStkQty());
                        blendingRequestReceiveBinMap.setTrnNo(binStock.getLTrnNo());
                        blendingRequestReceiveBinMap.setStatus("Updated");
                        blendingRequestReceiveBinMap.setBatchNo(binStock.getBatchNo());
                        blendingRequestReceiveBinMap.setBranch(binStock.getBranch());
                        blendingRequestReceiveBinMap.setDept(binStock.getDept());
                        blendingRequestReceiveBinMap.setItemId(binStock.getRefId());
                        Optional<ProductMaster> productMasterOptional = productMasterRepository.findById(binStock.getRefId());
                        if(productMasterOptional.isPresent()) {
                            blendingRequestReceiveBinMap.setItemName(productMasterOptional.get().getProductName());
                        }
                        blendingRequestReceiveBinMap.setLot(binStock.getLot());
                        blendingRequestReceiveBinMapRepository.save(blendingRequestReceiveBinMap);
                    }
                }

            }
        }
    }

}
