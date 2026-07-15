package com.aria.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.BinStockUtil;
import com.aria.utils.DateUtil;
import com.aria.utils.DayEndUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MaterialIssueService {


    private static final DecimalFormat decfor = new DecimalFormat("0.0000");

    @Autowired
    MaterialIssueRepository materialIssueRepository;

    @Autowired
    IssueItemsRepository issueItemsRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    BinStockRepository binStockRepository;

    @Autowired
    WithInBranchReceiveItemsRepository receiveItemsRepository;

    @Autowired
    WithInBranchReceiveRepository withInBranchReceiveRepository;

    @Autowired
    WithInBranchDistributionRepository withInBranchDistributionRepository;

    @Autowired
    OtherBranchIssueRepository otherBranchIssueRepository;

    @Autowired
    MaterialRequestRepository materialRequestRepository;

    @Autowired
    BinStockUtil binStockUtil;

    @Autowired
    TRNIndexRepo trnIndexRepo;

    @Autowired
    OtherBranchReceiptRepository otherBranchReceiptRepository;

    @Autowired
    DeliteStockMoveRequestBinRepository deliteStockMoveRequestBinRepository;

    @Autowired
    SalesProductFilmOrderRepository salesProductFilmOrderRepository;

    @Autowired
    SalesProductLabelOrderRepository salesProductLabelOrderRepository;

    @Autowired
    RequestItemRepository requestItemRepository;

    @Autowired
    OtherBranchIssueItemsRepository otherBranchIssueItemsRepository;

    @Autowired
    IntraBranchRecRepository intraBranchRecRepository;

    @Autowired
    DayEndUtils dayEndUtils;

    public ResponseEntity<?> updateMaterialIssue(MaterialIssueDto materialIssueDto, User loggedUser) throws JsonProcessingException {

        Date acccountingDate =  dayEndUtils.getAccountingDate(loggedUser.getBranch_id(), dateUtil.getISTDate());
        Boolean accountingStatus =  dayEndUtils.checkAccountingStatus(materialIssueDto.getRequestFromBranchId(), acccountingDate);
        if(accountingStatus){
            return new ResponseEntity<Status>(new Status("400", "Receive branch is already account closed this accounting date : "+dateUtil.getDateTOStringWk(acccountingDate)+", Please close your branch account, and then continue material Issue process!!!"), HttpStatus.OK);
        }
        MaterialIssue  alreadyIssued = materialIssueRepository
                .findByrequestId(materialIssueDto.getRequestId());
        if(!("Pending".equalsIgnoreCase(alreadyIssued.getStatus()))) {
        	 return new ResponseEntity<Status>(new Status("400","Material already issued for this requestId: " + materialIssueDto.getRequestId() + " reload this page"), HttpStatus.OK);
        }
       

        MaterialIssue materialIssue = new MaterialIssue(materialIssueDto);
        materialIssue.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        materialIssue.setUpdatedBy(loggedUser.getId());
        materialIssue.setIssueBy(loggedUser);
        Branch branch = new Branch();
        branch.setId(loggedUser.getBranch_id());
        materialIssue.setIssueBranch(branch);
        materialIssue.setIssueDepartment(loggedUser.getDepartment());
        materialIssue.setIssueDate(dateUtil.getISTDate());
        materialIssue.setRequestDate(dateUtil.getStringTODateWk(materialIssueDto.getRequestDate()));
        materialIssue.setAccountingDate(acccountingDate);
        if (materialIssue.getTrnNo() == null || materialIssue.getTrnNo() == "") {
            String trnNo = getTrnNo();
            materialIssue.setTrnNo(trnNo);
        }

        MaterialIssue result = materialIssueRepository.save(materialIssue);
        for (IssueItemsDto issueItemsDto : materialIssueDto.getIssueItems()) {
            IssueItems issueItems = new IssueItems(issueItemsDto);
            issueItems.setUpdated_date(dateUtil.getISTDate());
            issueItems.setIssueItemId(result.getId());
            if (issueItemsDto.getBranchId() == null) {

                issueItems.setBranchId(loggedUser.getBranch_id());
            } else {
                issueItems.setBranchId(issueItemsDto.getBranchId());
            }
            if (issueItemsDto.getDeptId() == null) {
                issueItems.setDeptId(loggedUser.getDepartment().getId());
            } else {
                issueItems.setDeptId(issueItemsDto.getDeptId());
            }
            issueItems.setUpdatedBy(loggedUser.getId());
            IssueItems rs = issueItemsRepository.save(issueItems);
            Long binName = Long.valueOf(issueItemsDto.getIssuedFromBin());

            if (rs.getActive() == 0) {
                //BinStock binstock = binStockRepository.findByBatchNoAndBinName(issueItemsDto.getItemId(), issueItemsDto.getIssuedFromBatch(), binName, Double.valueOf(issueItemsDto.getSelectLot()), Double.valueOf(issueItemsDto.getStockQuantity()));
                Optional<BinStock> binStockOptional = binStockRepository.findById(issueItemsDto.getBinStockId());
                if (binStockOptional.isPresent()) {

                    BinStock binstock = binStockOptional.get();
                    BinStock newBinstock = new BinStock();
                    newBinstock.setBatchNo(issueItemsDto.getIssuedFromBatch());
                    newBinstock.setStkQty(Double.valueOf(issueItemsDto.getAllocatedQuantity()));
                    newBinstock.setLockStatus("Y");
                    newBinstock.setLockType("BU");
                    newBinstock.setBinName(Long.parseLong(issueItemsDto.getIssuedFromBin()));
                    newBinstock.setAvgCost(binstock.getAvgCost());
                    newBinstock.setBatchId(binstock.getBatchId());
                    newBinstock.setBinId(binstock.getBinId());
                    newBinstock.setInwarDate(binstock.getInwarDate());
                    newBinstock.setBinType(binstock.getBinType());
                    newBinstock.setBranch(materialIssueDto.getIssueFromBranchId());
                    newBinstock.setDept(materialIssueDto.getIssueFromDepartmentId());
                    newBinstock.setCategory(binstock.getCategory());
                    if (binstock.getAvgCost() != null) {
                        newBinstock.setCost(newBinstock.getStkQty() * binstock.getAvgCost());
                    }
                    newBinstock.setLot(binstock.getLot());
                    newBinstock.setLTrnNo(result.getTrnNo());
                    newBinstock.setMrnStatus("Y");
                    newBinstock.setCid(binstock.getCid());
                    newBinstock.setOilFFA(binstock.getOilFFA());
                    newBinstock.setRefId(binstock.getRefId());
                    newBinstock.setGid(binstock.getGid());
                    newBinstock.setCampName(binstock.getCampName());
                    newBinstock.setLocation(binstock.getLocation());
                    newBinstock.setSeedcolor(binstock.getSeedcolor());
                    newBinstock.setSeedMoisture(binstock.getSeedMoisture());
                    newBinstock.setAdate(dateUtil.getISTDate());
                    BinStock rsBin = binStockRepository.save(newBinstock);

                    // Reduce selected Stock
                    Double updatedStkQty = binstock.getStkQty() - Double.valueOf(issueItemsDto.getAllocatedQuantity());
                    binstock.setStkQty(Double.valueOf(decfor.format(updatedStkQty)));
                    binStockRepository.save(binstock);

                    binStockUtil.updateBinLogMaterialTransaction(result, rsBin, loggedUser, "I");
                    rs.setReceiveBinStockId(rsBin.getId());
                    issueItemsRepository.save(rs);

                } else {
                    return new ResponseEntity<Status>(new Status("204", "material issue  Not updated  successfully!!!"), HttpStatus.OK);
                }
            }
        }

        result.setStatus(materialIssueDto.getStatus());
        materialIssueRepository.save(result);
        if (result.getRequestBranch().getId().toString().equalsIgnoreCase(result.getIssueBranch().getId().toString())) {
            updateWithInBranchReceive(result.getId());
        } else {
            updatePlantReceipt(result.getId());
        }
        return new ResponseEntity<Status>(new Status("200", "material issue  updated  successfully!!!"), HttpStatus.OK);
    }


    public ResponseEntity<?> issuePendingRequest(MaterialIssueDto materialIssueDto, User loggedUser) throws JsonProcessingException {


        Date acccountingDate =  dayEndUtils.getAccountingDate(loggedUser.getBranch_id(), dateUtil.getISTDate());
        Boolean accountingStatus =  dayEndUtils.checkAccountingStatus(materialIssueDto.getRequestFromBranchId(), acccountingDate);
        if(accountingStatus){
            return new ResponseEntity<Status>(new Status("400", "Receive branch is already account closed this accounting date : "+dateUtil.getDateTOStringWk(acccountingDate)+", Please close your branch account, and then continue material Issue process!!!"), HttpStatus.OK);
        }
        MaterialIssue  alreadyIssued = materialIssueRepository
                .findByrequestId(materialIssueDto.getRequestId());

        if (alreadyIssued !=null && alreadyIssued.getStatus().equalsIgnoreCase("Issue") ) {
            return new ResponseEntity<>(
                    new Status("400", "Material already issued for this requestId: "
                            + materialIssueDto.getRequestId()),
                    HttpStatus.OK
            );
        }
            IssueQtyList qty = issueItemsRepository.getQtySummary(materialIssueDto.getRequestId());
        Double allocated = qty != null && qty.gettotal_allocated() != null
                ? qty.gettotal_allocated() : 0.0;
        Double requested = qty != null && qty.gettotal_requested() != null
                ? qty.gettotal_requested() : 0.0;
        Double pending = requested - allocated;
        Double incomingQty = 0.0;
        for (IssueItemsDto dto : materialIssueDto.getIssueItems()) {
            incomingQty += Double.valueOf(dto.getAllocatedQuantity());
        }
        if (incomingQty > pending) {
            return new ResponseEntity<>(
                    new Status("400", "Only pending qty allowed: " + pending),
                    HttpStatus.OK
            );
        }
        if (pending <= 0) {
            return new ResponseEntity<>(
                    new Status("400", "No pending quantity available"),
                    HttpStatus.OK
            );
        }

        MaterialIssue materialIssue = new MaterialIssue(materialIssueDto, true);
        materialIssue.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        materialIssue.setUpdatedBy(loggedUser.getId());
        materialIssue.setStatus(materialIssueDto.getStatus());
        materialIssue.setIssueBy(loggedUser);
        Branch branch = new Branch();
        branch.setId(loggedUser.getBranch_id());
        materialIssue.setIssueBranch(branch);
        materialIssue.setIssueDepartment(loggedUser.getDepartment());
        materialIssue.setIssueDate(dateUtil.getISTDate());
        materialIssue.setAccountingDate(acccountingDate);
        materialIssue.setRequestDate(dateUtil.getStringTODateWk(materialIssueDto.getRequestDate()));
        String trnNo = getTrnNo();
        materialIssue.setTrnNo(trnNo);
        MaterialIssue result = materialIssueRepository.save(materialIssue);

        for (IssueItemsDto issueItemsDto : materialIssueDto.getIssueItems()) {

            IssueItems issueItems = new IssueItems(issueItemsDto);

            issueItems.setUpdated_date(dateUtil.getISTDate());
            issueItems.setIssueItemId(result.getId());
            if (issueItemsDto.getBranchId() == null) {
                issueItems.setBranchId(loggedUser.getBranch_id());
            } else {
                issueItems.setBranchId(issueItemsDto.getBranchId());
            }
            if (issueItemsDto.getDeptId() == null) {
                issueItems.setDeptId(loggedUser.getDepartment().getId());
            } else {
                issueItems.setDeptId(issueItemsDto.getDeptId());
            }
            issueItems.setUpdatedBy(loggedUser.getId());
            IssueItems rs = issueItemsRepository.save(issueItems);
            Long binName = Long.valueOf(issueItemsDto.getIssuedFromBin());

            if (rs.getActive() == 0) {

                // BinStock binstock = binStockRepository.findByBatchNoAndBinName(issueItemsDto.getItemId(), issueItemsDto.getIssuedFromBatch(), binName, Double.valueOf(issueItemsDto.getSelectLot()), Double.valueOf(issueItemsDto.getStockQuantity()));
                Optional<BinStock> binStockOptional = binStockRepository.findById(issueItemsDto.getBinStockId());
                if (binStockOptional.isPresent()) {
                    BinStock binstock = binStockOptional.get();
                    BinStock newBinstock = new BinStock();
                    newBinstock.setBatchNo(issueItemsDto.getIssuedFromBatch());
                    newBinstock.setStkQty(Double.valueOf(issueItemsDto.getAllocatedQuantity()));
                    newBinstock.setLockStatus("Y");
                    newBinstock.setLockType("BU");
                    newBinstock.setBinName(Long.parseLong(issueItemsDto.getIssuedFromBin()));
                    newBinstock.setAvgCost(binstock.getAvgCost());
                    newBinstock.setBatchId(binstock.getBatchId());
                    newBinstock.setBinId(binstock.getBinId());
                    newBinstock.setInwarDate(binstock.getInwarDate());
                    newBinstock.setBinType(binstock.getBinType());
                    newBinstock.setBranch(materialIssueDto.getIssueFromBranchId());
                    newBinstock.setDept(materialIssueDto.getIssueFromDepartmentId());
                    newBinstock.setCategory(binstock.getCategory());
                    if (binstock.getAvgCost() != null) {
                        newBinstock.setCost(newBinstock.getStkQty() * binstock.getAvgCost());
                    }
                    newBinstock.setLot(binstock.getLot());
                    newBinstock.setLTrnNo(result.getTrnNo());
                    newBinstock.setMrnStatus("Y");
                    newBinstock.setCid(binstock.getCid());
                    newBinstock.setOilFFA(binstock.getOilFFA());
                    newBinstock.setRefId(binstock.getRefId());
                    newBinstock.setGid(binstock.getGid());
                    newBinstock.setCampName(binstock.getCampName());
                    newBinstock.setLocation(binstock.getLocation());
                    newBinstock.setSeedcolor(binstock.getSeedcolor());
                    newBinstock.setSeedMoisture(binstock.getSeedMoisture());
                    newBinstock.setAdate(dateUtil.getISTDate());
                    BinStock rsBin = binStockRepository.save(newBinstock);

                    // Reduce selected Stock
                    Double updatedStkQty = binstock.getStkQty() - Double.valueOf(issueItemsDto.getAllocatedQuantity());
                    binstock.setStkQty(Double.valueOf(decfor.format(updatedStkQty)));
                    binStockRepository.save(binstock);
                    binStockUtil.updateBinLogMaterialTransaction(result, rsBin, loggedUser, "I");

                    rs.setReceiveBinStockId(rsBin.getId());
                    issueItemsRepository.save(rs);

                } else {
                    return new ResponseEntity<Status>(new Status("204", "material issue  Not updated  successfully!!!"), HttpStatus.EXPECTATION_FAILED);
                }
            }
        }
        Double totalIssued = allocated + incomingQty;

        if (Math.abs(totalIssued - requested) < 0.0001) {
            result.setStatus("Issue");
        } else {
            result.setStatus("Pending_Quantity");
        }
//        result.setStatus(materialIssueDto.getStatus());
        materialIssueRepository.save(result);


        //updateOldIssue(materialIssueDto);

        Optional<MaterialIssue> updatePendingIssue = materialIssueRepository.findById(materialIssueDto.getId());
        if (updatePendingIssue.isPresent()) {
            MaterialIssue issue = updatePendingIssue.get();
            issue.setStatus("Issue");
            materialIssueRepository.save(issue);
        }

        if (result.getRequestBranch().getId().toString().equalsIgnoreCase(result.getIssueBranch().getId().toString())) {
            updateWithInBranchReceive(result.getId());
        } else {
            updatePlantReceipt(result.getId());
        }
        return new ResponseEntity<Status>(new Status("200", "material issue  updated  successfully!!!"), HttpStatus.OK);
    }

    public void updateOldIssue(MaterialIssueDto materialIssueDto) {
        Optional<MaterialIssue> updatePendingIssue = materialIssueRepository.findById(materialIssueDto.getId());
        if (updatePendingIssue.isPresent()) {
            MaterialIssue issue = updatePendingIssue.get();
            issue.setStatus("Issue");
            materialIssueRepository.save(issue);
            for (IssueItemsDto issueItemsDto : materialIssueDto.getIssueItems()) {
                List<IssueItems> issueItemsList = issueItemsRepository.findBy(materialIssueDto.getId(), issueItemsDto.getItemId());
                if (!issueItemsList.isEmpty()) {
                    for (IssueItems items : issueItemsList) {
                        items.setPendingQuantity("0");
                        issueItemsRepository.save(items);
                    }
                }
            }

            List<IssueItems> issueItems = issueItemsRepository.findAllByIssueItemId(issue.getId());
            String branchReceiptsItems = null;
            if (issueItems != null && issueItems.size() > 0) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    branchReceiptsItems = objectMapper.writeValueAsString(issueItems);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Optional<OtherBranchIssue> updateOtherBranchIssue = otherBranchIssueRepository.findBy(issue.getRequestId(), issue.getId(), issue.getTrnNo());

            if (updateOtherBranchIssue.isPresent()) {
                OtherBranchIssue otherBranchIssue = updateOtherBranchIssue.get();
                if (branchReceiptsItems != null) {
                    //otherBranchIssue.setOtherBranchIssueValue(branchReceiptsItems);
                    otherBranchIssueRepository.save(otherBranchIssue);
                }
            }

            Optional<WithInBranchDistribution> updateWithInBranchDistribution = withInBranchDistributionRepository.findBy(issue.getRequestId(), issue.getId(), issue.getTrnNo());
            if (updateWithInBranchDistribution.isPresent()) {
                WithInBranchDistribution withInBranchDistribution = updateWithInBranchDistribution.get();
                if (branchReceiptsItems != null) {
                    withInBranchDistribution.setWithinBranchDistributionValue(branchReceiptsItems);
                    withInBranchDistributionRepository.save(withInBranchDistribution);
                }
            }
            Optional<OtherBranchReceipt> updateOtherBranchReceipt = otherBranchReceiptRepository.findBy(issue.getRequestId(), issue.getId(), issue.getTrnNo());
            if (updateOtherBranchReceipt.isPresent()) {
                OtherBranchReceipt otherBranchReceipt = updateOtherBranchReceipt.get();
                if (branchReceiptsItems != null) {
                    //otherBranchReceipt.setOtherBranchReceiptValue(branchReceiptsItems);
                    otherBranchReceiptRepository.save(otherBranchReceipt);
                }
            }

        }
    }

    public String getTrnNo() {
        String trnNo = null;
        Optional<TRNIndex> trnIndex = trnIndexRepo.findByPrefix("MRN");
        if (trnIndex.isPresent()) {
            TRNIndex trn = trnIndex.get();
            Long trnlast = trn.getLastno();
            trnNo = trn.getPrefix() + "-" + trn.getLastno();
            Long fstrnlast = trnlast + 1;
            trn.setLastno(fstrnlast);
            TRNIndex rs = trnIndexRepo.saveAndFlush(trn);
            return trnNo;
        } else {
            return trnNo;
        }
    }


    public ResponseEntity<?> listAll(Pageable pageable) throws UsernameNotFoundException {
        Page<MaterialIssue> list = materialIssueRepository.findAll(pageable);
        if (!list.toList().isEmpty()) {
            List<MaterialIssueDto> result = list.toList().stream().map(data -> {
                MaterialIssueDto materialIssueDto = new MaterialIssueDto(data);
                materialIssueDto.setRequestDate(dateUtil.getStringTODateF1(data.getRequestDate()));
                materialIssueDto.setRequestDateString(dateUtil.getStringTODate(data.getRequestDate()));
                materialIssueDto.setIssueDate(dateUtil.getStringTODateF1(data.getRequestDate()));
                materialIssueDto.setIssueDateString(dateUtil.getStringTODate(data.getRequestDate()));
                return materialIssueDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<MaterialIssueDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<MaterialIssueDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> filterListAll(Long firmId, Long branchId, Long deptId, Long requestId, String itemName,
                                           String branchName, String deptName, String fromDate, String toDate, String status, Pageable pageable)
            throws UsernameNotFoundException {
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

        Page<MaterialIssueList> list = materialIssueRepository.materialIssueList(firmId, branchId, deptId, requestId, itemName,
                branchName, deptName, startDate, endDate, status, pageable);
        if (!list.toList().isEmpty()) {
            List<MaterialIssueListDto> result = list.toList().stream().map(data -> new MaterialIssueListDto(data))
                    .collect(Collectors.toList());
            PageDataDto<MaterialIssueListDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<MaterialIssueListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<MaterialIssueListDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getMaterialIssue(String id) throws UsernameNotFoundException {

        Optional<MaterialIssue> optionalMaterialIssue = materialIssueRepository.findById(Long.parseLong(id));

        MaterialIssueDto materialIssueDto = new MaterialIssueDto(optionalMaterialIssue.get());
        if (optionalMaterialIssue.get().getRequestDate() != null) {
            materialIssueDto.setRequestDate(dateUtil.getDateTOStringWk(optionalMaterialIssue.get().getRequestDate()));
            materialIssueDto.setRequestDateString(dateUtil.getDateTOStringWk(optionalMaterialIssue.get().getRequestDate()));
        }
        if (optionalMaterialIssue.get().getIssueDate() != null) {
            materialIssueDto.setIssueDate(dateUtil.getDateTOStringWk(optionalMaterialIssue.get().getIssueDate()));
            materialIssueDto.setIssueDateString(dateUtil.getDateTOStringWk(optionalMaterialIssue.get().getIssueDate()));
        }

        Optional<MaterialRequest> opMaterialIssue = materialRequestRepository.findById(materialIssueDto.getRequestId());
        if (opMaterialIssue.isPresent()) {

            MaterialRequest materialRequest = opMaterialIssue.get();

            materialIssueDto.setRequestFrom(materialRequest.getRequestBy().getEmployeename());

            materialIssueDto.setIssueBriefItems(getIssueItemBrief(materialRequest, Long.parseLong(id)));

            List<Long> binStockIds = deliteStockMoveRequestBinRepository.deliteBinStockIdList(materialRequest.getId());
            if (!binStockIds.isEmpty()) {
                materialIssueDto.setBinStockIds(binStockIds);
            }

            if (materialRequest.getRequestName().equalsIgnoreCase("Film Order")) {

                List<IssueItems> itemList = issueItemsRepository.findAllByIssueItemIdANDActive(materialIssueDto.getId());
                List<IssueItemsDto> items = itemList.stream().map(data -> {
                    IssueItemsDto itemDto = new IssueItemsDto(data);
                    IssueTrnCheck issueTrnCheck = materialIssueRepository.findBy(materialRequest.getId(), itemDto.getItemId(), itemDto.getSelectLot() );
                    if(issueTrnCheck != null ){
                        itemDto.setIssueStatus(false);
                    }else {
                        itemDto.setIssueStatus(true);
                    }
                    return itemDto;
                }).collect(Collectors.toList());
                materialIssueDto.setIssueItems(items);

            } else {
                List<IssueItems> issueItems = optionalMaterialIssue.get().getIssueItems();
                List<IssueItemsDto> items = issueItems.stream().map(data -> {
                    IssueItemsDto itemDto = new IssueItemsDto(data);
                    IssueTrnCheck issueTrnCheck = materialIssueRepository.findBy(materialRequest.getId(), itemDto.getItemId(), itemDto.getSelectLot() );
                    if(issueTrnCheck != null ){
                        itemDto.setIssueStatus(false);
                    }else {
                        itemDto.setIssueStatus(true);
                    }
                    return itemDto;
                }).collect(Collectors.toList());
                materialIssueDto.setIssueItems(items);
            }

            if(materialRequest.getRequestName().equalsIgnoreCase("Blending_Request")){
                List<IssueItems> allIssueItems = optionalMaterialIssue.get().getAllIssueItems();
                List<IssueItemsDto> items = allIssueItems.stream().map(data -> {
                    IssueItemsDto itemDto = new IssueItemsDto(data);
                    IssueTrnCheck issueTrnCheck = materialIssueRepository.findBy(materialRequest.getId(), itemDto.getItemId(), itemDto.getSelectLot() );
                    if(issueTrnCheck != null ){
                        itemDto.setIssueStatus(false);
                    }else {
                        itemDto.setIssueStatus(true);
                    }
                    return itemDto;
                }).collect(Collectors.toList());
                materialIssueDto.setAllIssueItems(items);
            }

            // Single product issue flow to updated by ALAGU

            List<RequestItem> requestItems = requestItemRepository.findBy(materialRequest.getId(), optionalMaterialIssue.get().getIssueBranch().getId(), optionalMaterialIssue.get().getIssueDepartment().getId());
            List<IssueItemsDto> itemsList = requestItems.stream().map(requestItem -> {
                IssueItemsDto issueItems = new IssueItemsDto();
                issueItems.setIssueItemId(materialIssueDto.getId());
                issueItems.setCreated_date(requestItem.getCreated_date());
                issueItems.setActive(0);
                issueItems.setSelectLot(requestItem.getSelectLot());
                issueItems.setBranchId(requestItem.getIssueBranch().getId());
                issueItems.setDeptId(requestItem.getIssueDepartment().getId());
                issueItems.setItemName(requestItem.getItemName());
                issueItems.setItemId(requestItem.getItemId());
                issueItems.setRequestedQuantity(requestItem.getStockRequired());
                IssueTrnCheck issueTrnCheck = materialIssueRepository.findBy(materialRequest.getId(), requestItem.getItemId(), requestItem.getSelectLot() );
                if(issueTrnCheck != null ){
                    issueItems.setIssueStatus(false);
                }else {
                    issueItems.setIssueStatus(true);
                }
                return issueItems;
            }).collect(Collectors.toList());
            materialIssueDto.setRequestItems(itemsList);






        }

        return new ResponseEntity<MaterialIssueDto>(materialIssueDto, HttpStatus.OK);
    }

    public List<IssueItemsBriefDto> getIssueItemBrief(MaterialRequest materialRequest, Long issueId) {

        Optional<MaterialIssue> optionalMaterialIssue = materialIssueRepository.findById(issueId);

        if (materialRequest.getRequestName().equalsIgnoreCase("Blending_Request")) {

            List<IssueItemsBriefDto> dtoList = new ArrayList<>();
            List<IssueItemsBrief> itemsBriefs = issueItemsRepository.findByBrief(materialRequest.getId());
            return itemsBriefs.stream().map(data -> new IssueItemsBriefDto(data)).collect(Collectors.toList());

        } /*else if (materialRequest.getRequestName().equalsIgnoreCase("Label Order")) {

            System.out.println("request Details == "+materialRequest.getId()
                    + " issue BranchId == " + optionalMaterialIssue.get().getIssueBranch().getId()
                    + " req Branch == " + materialRequest.getBranch().getId()
                    + " issue dept " + optionalMaterialIssue.get().getIssueDepartment().getId()
                    + " req dept " + materialRequest.getDepartment().getId());
            List<RequestItem> requestItems = requestItemRepository.findAllByRequestId(materialRequest.getId());
            System.out.println("requestItems == "+requestItems);
            List<IssueItemsBriefDto> dtoList = new ArrayList<>();

            Map<Long, Map<String, Double>> allocatedQuantitiesMap = new HashMap<>();

            for (RequestItem requestItem : requestItems) {

                IssueItemsBriefDto issueItemsBriefDto = new IssueItemsBriefDto();
                issueItemsBriefDto.setItemName(requestItem.getItemName());
                issueItemsBriefDto.setItemId(requestItem.getItemId());
                issueItemsBriefDto.setIssueItemId(issueId);
                Double pendingQuantity = 0.00;
                System.out.println(requestItem.getRequestId() + " - " + requestItem.getIssueBranch().getId() + " - " + requestItem.getIssueDepartment().getId() + " - " + requestItem.getItemId());
                Double requestedQuantity = Double.valueOf(requestItem.getStockRequired());
                Double allocatedQtyQuantity = issueItemsRepository.sumOfAllocatedQty(requestItem.getRequestId(), requestItem.getIssueBranch().getId(), requestItem.getIssueDepartment().getId(), requestItem.getItemId());
                issueItemsBriefDto.setLot(requestItem.getSelectLot());
                issueItemsBriefDto.setAllocatedQuantity(allocatedQtyQuantity);
                issueItemsBriefDto.setRequestedQuantity(requestedQuantity);
                issueItemsBriefDto.setPendingQuantity(pendingQuantity);
                dtoList.add(issueItemsBriefDto);
            }
            return dtoList;

        }*/ else {

            List<RequestItem> requestItems = requestItemRepository.findBy(materialRequest.getId(), optionalMaterialIssue.get().getIssueBranch().getId(), optionalMaterialIssue.get().getIssueDepartment().getId());

            List<IssueItemsBriefDto> dtoList = new ArrayList<>();

            Map<Long, Map<String, Double>> allocatedQuantitiesMap = new HashMap<>();

            for (RequestItem requestItem : requestItems) {

                IssueItemsBriefDto issueItemsBriefDto = new IssueItemsBriefDto();
                issueItemsBriefDto.setItemName(requestItem.getItemName());
                issueItemsBriefDto.setItemId(requestItem.getItemId());
                issueItemsBriefDto.setIssueItemId(issueId);

                //List<IssueItems> issueItems = issueItemsRepository.findAllByIssueItemIdAndItemAndLot(issueId, requestItem.getItemId(), requestItem.getSelectLot());
                Double pendingQuantity = 0.00;
                
                Double requestedQuantity = Double.valueOf(requestItem.getStockRequired());
                Double allocatedQtyQuantity = issueItemsRepository.sumOfAllocatedQty(requestItem.getRequestId(), requestItem.getIssueBranch().getId(), requestItem.getIssueDepartment().getId(), requestItem.getItemId(), requestItem.getSelectLot());
                issueItemsBriefDto.setLot(requestItem.getSelectLot());
                issueItemsBriefDto.setAllocatedQuantity(allocatedQtyQuantity);
                issueItemsBriefDto.setRequestedQuantity(requestedQuantity);
                issueItemsBriefDto.setPendingQuantity(pendingQuantity);
                dtoList.add(issueItemsBriefDto);
            }
            return dtoList;
        }
    }


    public ResponseEntity<?> deleteMaterialIssue(String id) throws UsernameNotFoundException {
        try {
            Optional<MaterialIssue> optionalMaterialIssue = materialIssueRepository.findById(Long.parseLong(id));
            if (optionalMaterialIssue.isPresent()) {
                MaterialIssue itemDetails = optionalMaterialIssue.get();
                materialIssueRepository.delete(itemDetails);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }


    public void updatePlantReceipt(Long id) throws JsonProcessingException {

        Optional<MaterialIssue> optionalMaterialIssue = materialIssueRepository.findById(id);

        if (optionalMaterialIssue.isPresent()) {
            MaterialIssue materialIssue = optionalMaterialIssue.get();
            OtherBranchIssue branchIssue = new OtherBranchIssue();
            branchIssue.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            branchIssue.setCreatedBy(materialIssue.getUpdatedBy());
            branchIssue.setRequestBranch(materialIssue.getRequestBranch());
            branchIssue.setOrganisation(materialIssue.getOrganisation());
            branchIssue.setRequestDepartment(materialIssue.getRequestDepartment());
            branchIssue.setRequestId(materialIssue.getRequestId());
            branchIssue.setIssueBranch(materialIssue.getIssueBranch());
            branchIssue.setIssueBy(materialIssue.getIssueBy());
            branchIssue.setIssueDepartment(materialIssue.getIssueDepartment());
            branchIssue.setIssueDepartment(materialIssue.getIssueBy().getDepartment());
            branchIssue.setIssueId(materialIssue.getId());
            branchIssue.setTrnNo(materialIssue.getTrnNo());
            branchIssue.setStatus("Pending");
            OtherBranchIssue rs = otherBranchIssueRepository.save(branchIssue);
            List<IssueItems> issueItems = issueItemsRepository.findAllByIssueItemIdANDActive(materialIssue.getId());
            if (issueItems != null && issueItems.size() > 0) {
                for (IssueItems items : issueItems) {
                    OtherBranchIssueItems issueItem = new OtherBranchIssueItems(items);
                    issueItem.setIssueItemId(rs.getId());
                    otherBranchIssueItemsRepository.save(issueItem);
                }
            }
        }
    }

    public void updateWithInBranchReceive(Long id) {
        Optional<MaterialIssue> optionalMaterialIssue = materialIssueRepository.findById(id);
        if (optionalMaterialIssue.isPresent()) {
            MaterialIssue materialIssue = optionalMaterialIssue.get();
            WithInBranchReceive withInBranchReceive = new WithInBranchReceive();
            withInBranchReceive.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            withInBranchReceive.setCreatedBy(materialIssue.getUpdatedBy());
            withInBranchReceive.setRequestBranch(materialIssue.getRequestBranch());
            withInBranchReceive.setRequestDepartment(materialIssue.getRequestDepartment());
            withInBranchReceive.setRequestDate(materialIssue.getRequestDate());
            withInBranchReceive.setRequestId(materialIssue.getRequestId());
            withInBranchReceive.setIssueBranch(materialIssue.getIssueBranch());
            withInBranchReceive.setIssueBy(materialIssue.getIssueBy());
            withInBranchReceive.setTrnNo(materialIssue.getTrnNo());
            /* Organisation organisation = new Organisation();
            organisation.setId(materialIssue.getIssueBy().getOrg_id());*/
            withInBranchReceive.setIssueOrg(optionalMaterialIssue.get().getOrganisation());
            withInBranchReceive.setRequestOrg(optionalMaterialIssue.get().getOrganisation());
            withInBranchReceive.setIssueDepartment(materialIssue.getIssueDepartment());
            withInBranchReceive.setIssueDate(materialIssue.getIssueDate());
            withInBranchReceive.setIssueId(materialIssue.getId());
            withInBranchReceive.setStatus("Pending");
            WithInBranchReceive result = withInBranchReceiveRepository.save(withInBranchReceive);
            List<IssueItems> issueItems = issueItemsRepository.findAllByIssueItemIdANDActive(materialIssue.getId());
            for (IssueItems issueItemValue : issueItems) {
                WithInBranchReceiveItems receiveItems = new WithInBranchReceiveItems(issueItemValue);
                receiveItems.setWithinId(result.getId());
                receiveItemsRepository.save(receiveItems);
            }
        }
    }

    public void rejectMaterialIssue(Long id) {
        Optional<MaterialIssue> optionalMaterialIssue = materialIssueRepository.findById(id);
        if (optionalMaterialIssue.isPresent()) {
            MaterialIssue materialIssue = optionalMaterialIssue.get();
            materialIssue.setStatus("REJECTED");
            materialIssueRepository.save(materialIssue);
            Optional<MaterialRequest> optionalMaterialRequest = materialRequestRepository.findById(materialIssue.getRequestId());
            if (optionalMaterialRequest.isPresent()) {
                MaterialRequest request = optionalMaterialRequest.get();
                List<Long> requestItems = requestItemRepository.findByIdList(request.getId(), materialIssue.getIssueBranch().getId(), materialIssue.getIssueDepartment().getId());
                if (!requestItems.isEmpty()) {
                    requestItemRepository.updateStatus(requestItems);
                }
                List<RequestItem> requestItemList = requestItemRepository.findByStatus(request.getId(), materialIssue.getIssueBranch().getId(), materialIssue.getIssueDepartment().getId());
                if (requestItemList.isEmpty()) {
                    request.setStatus("REJECTED");
                    materialRequestRepository.save(request);
                }
            }
        }
    }



    public ResponseEntity<?> mrnPendingList(Long requestBranchId, String requestBranchName, Long issueBranchId, String issueBranchName,
                                            String trnDate, String trnNo, String productName, Pageable pageable) throws UsernameNotFoundException {

        Page<MrnPendingList> list = materialIssueRepository.getMrnPendingList(requestBranchId, requestBranchName, issueBranchId, issueBranchName, dateUtil.getStringTODateWk(trnDate), trnNo, productName, pageable);
        if (!list.toList().isEmpty()) {
            List<MrnPendingDTO> result = list.toList().stream().map(data -> {

                System.out.println("data.getWithinBranchStatus() = " + data.getWithinBranchStatus());
                System.out.println("data.getIntraBranchStatus() = " + data.getIntraBranchStatus());
                System.out.println("data.getOtherBranchIssueStatus() = " + data.getOtherBranchIssueStatus());
                System.out.println("data.getIssueStatus() = " + data.getIssueStatus());

                MrnPendingDTO mrnPendingDTO = new MrnPendingDTO(data);
                mrnPendingDTO.setQty(issueItemsRepository.sumOfAllocatedQtyPending(data.getIssueId(), data.getBatchNo(), data.getItemId(), data.getLot()).toString());


                if ((data.getWithinBranchStatus() != null && !data.getWithinBranchStatus().equalsIgnoreCase("Pending")) && data.getWithinBranchDisbutionId() != null) {
                    mrnPendingDTO.setStatus(data.getWithinBranchStatus());
                    Optional<WithInBranchDistribution> withInBranchDistribution = withInBranchDistributionRepository.findById(data.getWithinBranchDisbutionId());
                    if ((withInBranchDistribution.isPresent())) {
                        if (withInBranchDistribution.get().getUpdatedBy() != null) {
                            Optional<User> userOptional = userRepository.findById(withInBranchDistribution.get().getUpdatedBy());
                            mrnPendingDTO.setPersonName(userOptional.get().getEmployeename());
                        }

                        if (withInBranchDistribution.get().getUpdated_date() != null) {
                            mrnPendingDTO.setDate(dateUtil.getLocalDateTimeToStringDDMMYYYY1(withInBranchDistribution.get().getUpdated_date()));
                            mrnPendingDTO.setDateTime(dateUtil.getLocalDateTimeToDateTimeString(withInBranchDistribution.get().getUpdated_date()));
                        }
                    }
                } else if ((data.getIntraBranchStatus() != null && !data.getIntraBranchStatus().equalsIgnoreCase("Pending")) && data.getIntraBranchRecId() != null) {
                    mrnPendingDTO.setStatus(data.getIntraBranchStatus());
                    Optional<IntraBranchReceiptEntity> intraBranchReceiptEntity = intraBranchRecRepository.findById(data.getIntraBranchRecId());
                    if ((intraBranchReceiptEntity.isPresent())) {

                        if (intraBranchReceiptEntity.get().getUpdatedBy() != null) {
                            Optional<User> userOptional = userRepository.findById(intraBranchReceiptEntity.get().getUpdatedBy());
                            mrnPendingDTO.setPersonName(userOptional.get().getEmployeename());
                        }

                        if (intraBranchReceiptEntity.get().getUpdated_date() != null) {
                            mrnPendingDTO.setDate(dateUtil.getLocalDateTimeToStringDDMMYYYY1(intraBranchReceiptEntity.get().getUpdated_date()));
                            mrnPendingDTO.setDateTime(dateUtil.getLocalDateTimeToDateTimeString(intraBranchReceiptEntity.get().getUpdated_date()));
                        }
                    }
                } else if ((data.getOtherBranchIssueStatus() != null && !data.getOtherBranchIssueStatus().equalsIgnoreCase("Pending")) && data.getOtherbranchIssueId() != null) {
                    mrnPendingDTO.setStatus(data.getOtherBranchIssueStatus());
                    Optional<OtherBranchIssue> otherBranchIssue = otherBranchIssueRepository.findById(data.getOtherbranchIssueId());
                    if ((otherBranchIssue.isPresent())) {
                        if (otherBranchIssue.get().getUpdatedBy() != null) {
                            Optional<User> userOptional = userRepository.findById(otherBranchIssue.get().getUpdatedBy());
                            mrnPendingDTO.setPersonName(userOptional.get().getEmployeename());
                        }

                        if (otherBranchIssue.get().getUpdated_date() != null) {
                            mrnPendingDTO.setDate(dateUtil.getLocalDateTimeToStringDDMMYYYY1(otherBranchIssue.get().getUpdated_date()));
                            mrnPendingDTO.setDateTime(dateUtil.getLocalDateTimeToDateTimeString(otherBranchIssue.get().getUpdated_date()));
                        }
                    }
                } else if (data.getIssueStatus() != null && data.getIssueId() != null) {
                    mrnPendingDTO.setStatus(data.getIssueStatus());
                    Optional<MaterialIssue> materialIssue = materialIssueRepository.findById(data.getIssueId());
                    if ((materialIssue.isPresent())) {
                        if (materialIssue.get().getIssueBy() != null) {
                            mrnPendingDTO.setPersonName(materialIssue.get().getIssueBy().getEmployeename());
                        }
                        if (materialIssue.get().getUpdated_date() != null) {
                            mrnPendingDTO.setDate(dateUtil.getLocalDateTimeToStringDDMMYYYY1(materialIssue.get().getUpdated_date()));
                            mrnPendingDTO.setDateTime(dateUtil.getLocalDateTimeToDateTimeString(materialIssue.get().getUpdated_date()));
                        }
                    }
                }

                return mrnPendingDTO;
            }).collect(Collectors.toList());
            PageDataDto<MrnPendingDTO> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<MrnPendingDTO>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<MrnPendingDTO>>(new PageDataDto(), HttpStatus.OK);
        }
    }



    public ResponseEntity<?> validateDayEndCloseReceiveBranch(Long receiveBranchId, User loggedUser) {

        Date acccountingDate =  dayEndUtils.getAccountingDate(loggedUser.getBranch_id(), dateUtil.getISTDate());
        Boolean accountingStatus =  dayEndUtils.checkAccountingStatus(receiveBranchId, acccountingDate);
        if(accountingStatus){
            return new ResponseEntity<Status>(new Status("400", "The receiving branch has already closed its accounts as of : "+dateUtil.getDateTOStringWk(acccountingDate)+", Please close your branch accounts before proceeding with the material issue process."), HttpStatus.OK);
        }
        return new ResponseEntity<Status>(new Status("200", "material issue Allow now"), HttpStatus.OK);
    }


}