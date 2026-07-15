package com.aria.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aria.utils.BinStockUtil;
import com.aria.utils.BlendingUtil;
import com.aria.utils.DateUtil;
import com.aria.utils.WorkFlowUtil;

@Service
public class InwardInspectionEntryService {

    @Autowired
    PatlogRepository patlogRepository;

    @Autowired
    KpiRepository kpiRepository;

    @Autowired
    AttributeFieldsProcessTypeOptionRepository processTypeRepository;

    @Autowired
    InwardCheckRepository inwardCheckRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    BinStockUtil binStockUtil;

    @Autowired
    BillInwardItemReceivedRepository billInwardItemReceivedRepository;

    @Autowired
    BillInwardRepository billInwardRepo;

    @Autowired
    DpEntryStoredProductRepository dpEntryStoredProductRepository;

    @Autowired
    CampRepository campRepository;

    @Autowired
    DpEntryStoredBinDetailsRepository dpEntryStoredBinDetailsRepository;

    @Autowired
    DirectPurchaseRepository directPurchaseRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    VendorRepository vendorRepository;

    @Autowired
    LotRepository lotRepository;

    @Autowired
    ProductMasterRepository productMasterRepository;

    @Autowired
    BatchMasterRepository batchMasterRepository;

    @Autowired
    InwardAuditRepo inwardAuditRepo;

    @Autowired
    InwardSkipLogRepository inwardSkipLogRepository;

    @Autowired
    KpiNameRepository kpiNameRepository;

    @Autowired
    ProreportdbRepository proreportdbRepository;

    @Autowired
    ActionRepository actionRepository;

    @Autowired
    BlendingUtil blendingUtil;

    @Autowired
    WorkFlowUtil workFlowUtil;


    @Autowired
    RestService restService;

    @Autowired
    HOConfigRESTLogRepository restLogRepository;


    @Transactional
    public ResponseEntity<?> saveInwardInspectionEntry(List<PatlogDto> reqDtos, User user) {

        String bathNo = null;
        String oilFFa = null;
        String seedMoisture = null;
        Long productId = null;
        try {
            if (!reqDtos.isEmpty()) {
                int s = reqDtos.size() - 1;
                updatedInwardCheck(reqDtos.get(s), user);
            }
            List<Patlog> patList = new ArrayList<>();
            for (PatlogDto reqDto : reqDtos) {
                Patlog model = new Patlog(reqDto);
                if (reqDto.getTno() == null) {
                    model.setTno("Fresh Report");
                } else {
                    model.setTno(reqDto.getTno());
                }
                model.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                model.setCreatedBy(user.getId());
                model.setEntry(dateUtil.getStringTODateWk(reqDto.getEntry()));
                if ((reqDto.getAtid() != null && reqDto.getAtid() == 2140)
                        || (reqDto.getAtid() != null && reqDto.getAtid() == 2109)
                        || (reqDto.getAtid() != null && reqDto.getAtid() == 10285)
                        || (reqDto.getAtid() != null && reqDto.getAtid() == 8115)
                        || (reqDto.getAtid() != null && reqDto.getAtid() == 14850)) {
                    if (reqDto.getResval() != null) {
                        bathNo = reqDto.getBatchno();
                        oilFFa = reqDto.getResval();
                        productId = reqDto.getProductMasterId();
                    }
                }
                if ( (reqDto.getAtid() != null && reqDto.getAtid() == 2104)
                     ||  (reqDto.getAtid() != null && reqDto.getAtid() == 2157) ) {
                    if (reqDto.getResval() != null) {
                        bathNo = reqDto.getBatchno();
                        seedMoisture = reqDto.getResval();
                        productId = reqDto.getProductMasterId();
                    }
                }
                patList.add(model);
            }
            patlogRepository.saveAll(patList);
            if (bathNo != null && seedMoisture != null && productId != null) {
                binStockUtil.updateBinStockBySeedMoisture(bathNo, seedMoisture, productId);
            }
            if (bathNo != null && oilFFa != null && productId != null) {
                binStockUtil.updateBinStockByFFA(bathNo, oilFFa, productId, user);
            }
            return new ResponseEntity<Status>(new Status("200", "Inward save successfully!!!"), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Status>(new Status("400", "Inward details value not created"), HttpStatus.OK);
    }

    public void updatedInwardCheck(PatlogDto patlogDto, User loggeduser) {

        if (patlogDto != null) {

            Optional<InwardCheck> optional = inwardCheckRepository.findByInwardCheck(patlogDto.getBatchno());
            if (optional.isPresent()) {
                InwardCheck inwardCheck = optional.get();
                inwardCheck.setCreatedBy(loggeduser.getId());
                inwardCheck.setUpdatedBy(loggeduser.getId());
                inwardCheck.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                inwardCheck.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                inwardCheck.setBatchNo(patlogDto.getBatchno());
                inwardCheck.setBag(patlogDto.getNoOfBag());
                inwardCheck.setProductId(patlogDto.getProductMasterId());
                if (patlogDto.getTno() == null) {
                    inwardCheck.setRecheckLog("Fresh Report");
                } else {
                    inwardCheck.setRecheckLog(patlogDto.getTno());
                }
                inwardCheck.setInwardDate(dateUtil.getStringTODateWk(patlogDto.getEntry()));
                inwardCheck.setStatus("InProgress");
                if (patlogDto.getStepId() != null) {
                    inwardCheck.setCompletedstep(patlogDto.getStepId().toString());
                }
                InwardCheck rs = inwardCheckRepository.save(inwardCheck);
                updateInwardAudit(rs);
            } else {
                InwardCheck inwardCheck = new InwardCheck();
                inwardCheck.setCreatedBy(loggeduser.getId());
                inwardCheck.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

                inwardCheck.setBatchNo(patlogDto.getBatchno());
                inwardCheck.setProductId(patlogDto.getProductMasterId());
                if (patlogDto.getTno() == null) {
                    inwardCheck.setRecheckLog("Fresh Report");
                } else {
                    inwardCheck.setRecheckLog(patlogDto.getTno());
                }
                inwardCheck.setInwardDate(dateUtil.getStringTODateWk(patlogDto.getEntry()));
                inwardCheck.setStatus("InProgress");
                if (patlogDto.getStepId() != null) {
                    inwardCheck.setCompletedstep(patlogDto.getStepId().toString());
                }
                InwardCheck rs = inwardCheckRepository.save(inwardCheck);
                updateInwardAudit(rs);
            }
        }
    }

    @Transactional
    public ResponseEntity<?> updateInwardInspectionEntry(List<PatlogDto> reqDtos, User user) {
        String bathNo = null;
        String oilFFa = null;
        Long productId = null;
        try {
            List<Patlog> patList = new ArrayList<>();
            for (PatlogDto reqDto : reqDtos) {
                Patlog model = new Patlog(reqDto);
                model.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                model.setUpdatedBy(user.getId());
                model.setEntry(dateUtil.getStringTODateWk(reqDto.getEntry()));
                if ((reqDto.getStepId() != null && reqDto.getStepId() == 5)
                        && (reqDto.getAtid() != null && reqDto.getAtid() == 2109)) {
                    if (reqDto.getResval() != null) {
                        bathNo = reqDto.getBatchno();
                        oilFFa = reqDto.getResval();
                        productId = reqDto.getProductMasterId();
                    }
                }
                patList.add(model);
            }
            patlogRepository.saveAll(patList);
            if (bathNo != null && oilFFa != null && productId != null) {
                binStockUtil.updateBinStockByFFA(bathNo, oilFFa, productId, user);
            }
            return new ResponseEntity<Status>(new Status("200", "Inward updated successfully!!!"), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Status>(new Status("400", "Inward details not updated"), HttpStatus.OK);
    }

    public ResponseEntity<?> getAttributesInwardInspectionDataPrint(InspectionEntryRequestList requestList) {
        List<InspectionEntryPrintResponse> inspectionEntryPrintResponseList = new ArrayList<>();
        for (InspectionEntryRequest request : requestList.getRequestList()) {

            InspectionEntryPrintResponse inspectionEntryPrintResponse = new InspectionEntryPrintResponse();
            List<Attribute> list = getAttributesInwardInspectionDataList(request.getEntryType(), request.getBatchNo());
            inspectionEntryPrintResponse.setAttributesList(list);
            inspectionEntryPrintResponse.setReportType(request.getEntryType());
            inspectionEntryPrintResponse.setKpiName(request.getKpiName());
            inspectionEntryPrintResponse.setISO(kpiNameRepository.findByISO(request.getKpiName()));
            List<BillInwardItemReceived> itemReceivedList = billInwardItemReceivedRepository.listBillInwardBatchNo(request.getBatchNo());
            if (itemReceivedList != null && itemReceivedList.size() > 0) {
                BillInwardItemReceived billInwardItemReceived = itemReceivedList.get(0);
                inspectionEntryPrintResponse.setProductName(billInwardItemReceived.getDescription());
                inspectionEntryPrintResponse.setStock(billInwardItemReceived.getReceivedQuantity());
                inspectionEntryPrintResponse.setKgs(billInwardItemReceived.getLot());
                Optional<BillInward> opBillInward = billInwardRepo.findById(billInwardItemReceived.getInwardId());
                if (opBillInward.isPresent()) {
                    BillInward billInward = opBillInward.get();
                    inspectionEntryPrintResponse.setBillNo(billInward.getBillNo());
                    inspectionEntryPrintResponse.setLorrySNo(billInward.getLorrySerialNo());
                    inspectionEntryPrintResponse.setBillDate(dateUtil.getDateTOStringYYYYMMDD(billInward.getBillDate()));
                    inspectionEntryPrintResponse.setLorryDate(dateUtil.getDateTOStringYYYYMMDD(billInward.getLorryDate()));
                    inspectionEntryPrintResponse.setInwardDate(dateUtil.getDateTOStringYYYYMMDD(billInward.getReceivedDate()));
                }

                DpEntryStoredProduct storedProduct = dpEntryStoredProductRepository
                        .findByProductBatchNo(request.getBatchNo());
                if (storedProduct != null) {

                    List<String> dpEntryStoredBinDetails = dpEntryStoredBinDetailsRepository
                            .findByProduct(storedProduct.getId());
                    if (dpEntryStoredBinDetails != null) {
                        inspectionEntryPrintResponse.setStoredBins(dpEntryStoredBinDetails);
                    }

                    Optional<DirectPurchase> opDirectPurchase = directPurchaseRepository
                            .findById(storedProduct.getEntryId());
                    if (opDirectPurchase.isPresent()) {
                        DirectPurchase directPurchase = opDirectPurchase.get();
                        Optional<Vendor> opVendor = vendorRepository.findById(directPurchase.getVendor_id());
                        if (opVendor.isPresent()) {
                            inspectionEntryPrintResponse.setVendorPlace(opVendor.get().getName());
                        }
                        if (directPurchase.getBranch() != null)
                            inspectionEntryPrintResponse.setPlant(directPurchase.getBranch().getBranchName());

                        Camp camp = directPurchase.getCamp();
                        if (camp != null)
                            inspectionEntryPrintResponse.setCamp(camp.getCamp());
                    }
                }
            }
            inspectionEntryPrintResponseList.add(inspectionEntryPrintResponse);
        }
        return new ResponseEntity<List<InspectionEntryPrintResponse>>(inspectionEntryPrintResponseList, HttpStatus.OK);
    }

    public ResponseEntity<?> getAttributesInwardInspectionDataPrintRot(List<InspectionEntryRequest> requests) {
        List<InspectionEntryPrintRotResponse> responseList = new ArrayList<>();
        for (InspectionEntryRequest request : requests) {
            InspectionEntryPrintRotResponse inspectionEntryPrintResponse = new InspectionEntryPrintRotResponse();
            inspectionEntryPrintResponse
                    .setAttributes(getAttributesInwardInspectionDataMap(request.getEntryType(), request.getBatchNo()));

            Optional<InwardAudit> inwardAuditOptional = inwardAuditRepo.findroqComment(request.getBatchNo(),
                    request.getEntryType());
            if (inwardAuditOptional.isPresent()) {
                InwardAudit inwardAudit = inwardAuditOptional.get();
                inspectionEntryPrintResponse.setRoqCommentList(inwardAudit.getRoqcomment());
            }

            inspectionEntryPrintResponse.setReportType(request.getEntryType());
            inspectionEntryPrintResponse.setKpiName(request.getKpiName());

            Long campId = 0L;
            List<BillInwardItemReceived> itemReceivedList = billInwardItemReceivedRepository.listBillInwardBatchNo(request.getBatchNo());

            if (itemReceivedList != null && itemReceivedList.size() > 0) {
                BillInwardItemReceived billInwardItemReceived = itemReceivedList.get(0);
                inspectionEntryPrintResponse.setProductName(billInwardItemReceived.getDescription());
                inspectionEntryPrintResponse.setStock(billInwardItemReceived.getReceivedQuantity());
                inspectionEntryPrintResponse.setKgs(billInwardItemReceived.getLot());
                Optional<BillInward> opBillInward = billInwardRepo.findById(billInwardItemReceived.getInwardId());
                if (opBillInward.isPresent()) {
                    BillInward billInward = opBillInward.get();
                    inspectionEntryPrintResponse.setLorrySNo(billInward.getLorrySerialNo());
                    inspectionEntryPrintResponse.setBillNo(billInward.getBillNo());
                    inspectionEntryPrintResponse
                            .setBillDate(dateUtil.getDateTOStringYYYYMMDD(billInward.getBillDate()));
                    inspectionEntryPrintResponse
                            .setLorryDate(dateUtil.getDateTOStringYYYYMMDD(billInward.getLorryDate()));
                }

                DpEntryStoredProduct storedProduct = dpEntryStoredProductRepository
                        .findByProductBatchNo(request.getBatchNo());
                if (storedProduct != null) {

                    List<String> dpEntryStoredBinDetails = dpEntryStoredBinDetailsRepository
                            .findByProduct(storedProduct.getId());
                    if (dpEntryStoredBinDetails != null) {
                        inspectionEntryPrintResponse.setStoredBins(dpEntryStoredBinDetails);
                    }

                    Optional<DirectPurchase> opDirectPurchase = directPurchaseRepository
                            .findById(storedProduct.getEntryId());
                    if (opDirectPurchase.isPresent()) {
                        DirectPurchase directPurchase = opDirectPurchase.get();
                        Optional<Vendor> opVendor = vendorRepository.findById(directPurchase.getVendor_id());
                        if (opVendor.isPresent()) {
                            inspectionEntryPrintResponse.setVendorPlace(opVendor.get().getName());
                        }
                        if (directPurchase.getBranch() != null)
                            inspectionEntryPrintResponse.setPlant(directPurchase.getBranch().getBranchName());

                        Camp camp = directPurchase.getCamp();
                        if (camp != null) {
                            inspectionEntryPrintResponse.setCamp(camp.getCamp());
                            campId = camp.getId();
                        }
                    }

                    inspectionEntryPrintResponse.setComparisonResponseList(
                            getComparisonDate(campId, request.getBatchNo(), storedProduct.getItemId()));
                }
                responseList.add(inspectionEntryPrintResponse);
            }
        }
        return new ResponseEntity(responseList, HttpStatus.OK);

    }

    public InspectionEntryPrintRotComparisonResponse getComparisonData(String entryType, String batchNo) {
        InspectionEntryPrintRotComparisonResponse inspectionEntryPrintResponse = new InspectionEntryPrintRotComparisonResponse();
        inspectionEntryPrintResponse.setAttributes(getAttributesInwardInspectionDataMap(entryType, batchNo));
        inspectionEntryPrintResponse.setReportType(entryType);
        List<BillInwardItemReceived> itemReceivedList = billInwardItemReceivedRepository.listBillInwardBatchNo(batchNo);
        if (itemReceivedList != null && itemReceivedList.size() > 0) {
            BillInwardItemReceived billInwardItemReceived = itemReceivedList.get(0);
            inspectionEntryPrintResponse.setProductName(billInwardItemReceived.getDescription());
            inspectionEntryPrintResponse.setStock(billInwardItemReceived.getReceivedQuantity());
            inspectionEntryPrintResponse.setKgs(billInwardItemReceived.getLot());
            Optional<BillInward> opBillInward = billInwardRepo.findById(billInwardItemReceived.getInwardId());
            if (opBillInward.isPresent()) {
                BillInward billInward = opBillInward.get();
                inspectionEntryPrintResponse.setLorrySNo(billInward.getLorrySerialNo());
                inspectionEntryPrintResponse.setBillNo(billInward.getBillNo());
                inspectionEntryPrintResponse.setBillDate(dateUtil.getDateTOStringYYYYMMDD(billInward.getBillDate()));
                inspectionEntryPrintResponse.setLorryDate(dateUtil.getDateTOStringYYYYMMDD(billInward.getLorryDate()));
            }

            DpEntryStoredProduct storedProduct = dpEntryStoredProductRepository.findByProductBatchNo(batchNo);
            if (storedProduct != null) {

                List<String> dpEntryStoredBinDetails = dpEntryStoredBinDetailsRepository
                        .findByProduct(storedProduct.getId());
                if (dpEntryStoredBinDetails != null) {
                    inspectionEntryPrintResponse.setStoredBins(dpEntryStoredBinDetails);
                }

                Optional<DirectPurchase> opDirectPurchase = directPurchaseRepository
                        .findById(storedProduct.getEntryId());
                if (opDirectPurchase.isPresent()) {
                    DirectPurchase directPurchase = opDirectPurchase.get();
                    Optional<Vendor> opVendor = vendorRepository.findById(directPurchase.getVendor_id());
                    if (opVendor.isPresent()) {
                        inspectionEntryPrintResponse.setVendorPlace(opVendor.get().getName());
                    }
                    if (directPurchase.getBranch() != null)
                        inspectionEntryPrintResponse.setPlant(directPurchase.getBranch().getBranchName());

                    Camp camp = directPurchase.getCamp();
                    if (camp != null)
                        inspectionEntryPrintResponse.setCamp(camp.getCamp());
                }
            }

        }
        return inspectionEntryPrintResponse;
    }

    public ResponseEntity<?> getAttributesInwardInspectionData(String entryType, Long kpiNameId, String inwardDate,
                                                               String batchNo) {

        List<KpiList> stepList = kpiRepository.getKpiNameStepsDetails(kpiNameId);
        List<KpiListDto> result = new ArrayList<>();
        if (!stepList.isEmpty()) {
            result = stepList.stream().map(stepData -> {
                KpiListDto step = new KpiListDto(stepData);
                Optional<InwardSkipLog> inwardSkipLog = inwardSkipLogRepository.findBy(step.getKpiNameId(),
                        step.getTaskGroupMapId(), batchNo, entryType);
                if (inwardSkipLog.isPresent()) {
                    step.setSkipflag(inwardSkipLog.get().getSkipflag());
                }
                updatesDetails(step, entryType, batchNo, inwardDate);

                return step;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<KpiListDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<KpiListDto>>(result, HttpStatus.OK);
        }

    }

    public List<Attribute> getAttributesInwardInspectionDataList(String entryType, String batchNo) {

        List<AttributesNameValueList> attributeList = patlogRepository.getAttributesAndValues(batchNo, entryType);
        List<Attribute> result = new ArrayList<>();
        if (!attributeList.isEmpty()) {
            result = attributeList.stream().map(stepData -> {
                Attribute attribute = new Attribute();
                attribute.setAttributeName(stepData.getName());
                attribute.setAttributeValue(stepData.getAttributesResValue());
                return attribute;
            }).collect(Collectors.toList());

        }
        return result;

    }

    public List<InspectionEntryPrintRotComparisonResponse> getComparisonDate(Long campId, String batchNo,
                                                                             Long productId) {
        List<InspectionEntryPrintRotComparisonResponse> list = new ArrayList<>();
        Optional<InwardCheck> inwardCheck = inwardCheckRepository.findByInwardCheck(batchNo);
        if (inwardCheck.isPresent()) {
            List<ComparisonInspectionData> inspectionData = inwardCheckRepository.findByInwardCheckCampId(campId,
                    productId);
            for (ComparisonInspectionData comparisonInspectionData : inspectionData) {
                list.add(getComparisonData(comparisonInspectionData.getEntryType(),
                        comparisonInspectionData.getBatchNo()));
            }
        }
        return list;
    }

    public LinkedHashMap<String, String> getAttributesInwardInspectionDataMap(String entryType, String batchNo) {
        List<AttributesNameValueList> attributeList = patlogRepository.getAttributesAndValues(batchNo, entryType);
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        for (AttributesNameValueList stepData : attributeList) {
            result.put(stepData.getName(), stepData.getAttributesResValue());
        }
        return result;
    }

    public void updatesDetails(KpiListDto step, String entryType, String batchNo, String inwardDate) {

        Date inwardDate1 = null;
        if (inwardDate != null) {
            inwardDate1 = dateUtil.getStringTODateWk(inwardDate);
        }

        if (Objects.isNull(entryType)) {
            entryType = "Fresh Report";
        }

        List<AttributesList> attributesLists = patlogRepository.getAttributesAndValues(step.getTaskGroupMapId(),
                step.getKpiNameId(), step.getProductMasterId(), batchNo, entryType);
        if (!attributesLists.isEmpty()) {
            List<AttributesListDto> reslt = attributesLists.stream().map(kpi -> {
                AttributesListDto fieldDto = new AttributesListDto(kpi);
                if (fieldDto.getAttributesResValue() == null) {
                    List<AttributeFieldsProcessTypeOptionBased> optionBaseds = processTypeRepository
                            .findBy(fieldDto.getId());
                    if (!optionBaseds.isEmpty()) {
                        List<AttributeFieldsProcessTypeOptionBasedDto> te = optionBaseds.stream()
                                .map(optionData -> new AttributeFieldsProcessTypeOptionBasedDto(optionData))
                                .collect(Collectors.toList());
                        fieldDto.setOptions(te);
                    }
                }
                return fieldDto;
            }).collect(Collectors.toList());
            step.setAttributeFieldsProcessType(reslt);
        }
    }

    public ResponseEntity<?> getAllInwardInspectionDetails(String entryType, String batchNo, Long kpiNameId,
                                                           String inwardDate, String status, Pageable pageable) {

        Date date = null;
        if (Objects.nonNull(inwardDate)) {
            date = dateUtil.getStringTODateWk(inwardDate);
        }

        Page<PatlogList> res = patlogRepository.getInspectionDetailsList(kpiNameId, entryType, batchNo, date, status,
                pageable);
        if (!res.toList().isEmpty()) {
            Random random = new Random();
            List<PatlogListDto> result = res.toList().stream().map(data -> new PatlogListDto(data, random))
                    .collect(Collectors.toList());
            PageDataDto<PatlogListDto> pageData = new PageDataDto<>(result, pageable, res);
            return new ResponseEntity<PageDataDto<PatlogListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<PatlogListDto>>(new PageDataDto<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> closeInwardInspection(PatlogDto patlogDto, User loggedUser) {

        Optional<InwardCheck> inwardCheck = inwardCheckRepository.findByInwardCheck(patlogDto.getBatchno());
        if (inwardCheck.isPresent()) {
            InwardCheck inwardCheck1 = inwardCheck.get();
            inwardCheck1.setStatus("Completed");
            inwardCheck1.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            inwardCheck1.setUpdatedBy(loggedUser.getId());
            InwardCheck rs = inwardCheckRepository.save(inwardCheck1);
            updateInwardAudit(rs);
            if (rs.getRecheckLog() != null && rs.getRecheckLog().equalsIgnoreCase("Fresh Report")) {
                binStockUtil.updateBinStockLockRelease(rs.getBatchNo(), rs.getProductId());
            }
            closeBatchNo(patlogDto.getBatchno());
        }
        return new ResponseEntity<Status>(new Status("200", "Inward closed successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> createRecheckInwardInspection(PatlogDto patlogDto, User loggedUser) {

        Optional<InwardCheck> inwardCheck = inwardCheckRepository.findByInwardCheck(patlogDto.getBatchno());
        if (inwardCheck.isPresent()) {
            InwardCheck ic = inwardCheck.get();
            InwardCheck inwardCheck1 = inwardCheck.get();
            inwardCheck1.setStatus("InProgress");
            inwardCheck1.setRecheckLog(patlogDto.getEntryType());
            inwardCheck1.setRecheckdesc(patlogDto.getEntry());
            inwardCheck1.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            inwardCheck1.setUpdatedBy(loggedUser.getId());
            InwardCheck rs = inwardCheckRepository.save(inwardCheck1);
            closeInwardAudit(ic, patlogDto);
            updateInwardAudit(rs);
            openBatchNo(patlogDto.getBatchno());
        }
        return new ResponseEntity<Status>(new Status("200", "Recheck created successfully!!!"), HttpStatus.OK);
    }

    public void closeBatchNo(String batchNo) {
        if (batchNo != null) {
            // Retrieve all BatchMaster entries with the given batchNo
            List<BatchMaster> batchMasters = batchMasterRepository.findAllByBatchno(batchNo);

            if (!batchMasters.isEmpty()) {
                for (BatchMaster batchMaster : batchMasters) {
                    // Find all BillInwardItemReceived entries related to the current BatchMaster
                    List<BillInwardItemReceived> list = billInwardItemReceivedRepository
                            .findByBillInwardProductsByBatchNo(batchNo, batchMaster.getId());

                    if (!list.isEmpty()) {
                        for (BillInwardItemReceived billInwardItemReceived : list) {
                            // Set inspection_status to "Closed"
                            billInwardItemReceived.setInspection_status("Closed");
                            billInwardItemReceivedRepository.save(billInwardItemReceived);
                        }
                    }
                }
            }
        }
    }


    public void openBatchNo(String batchNo) {
        if (batchNo != null) {
            List<BatchMaster> batchMasters = batchMasterRepository.findAllByBatchno(batchNo);
            if (!batchMasters.isEmpty()) {
                for (BatchMaster batchMaster : batchMasters) {
                	List<BillInwardItemReceived> list = billInwardItemReceivedRepository.findByBillInwardProductsByBatchNo(batchNo, batchMaster.getId());
                    if (!list.isEmpty()) {
                        for (BillInwardItemReceived billInwardItemReceived : list) {
                            billInwardItemReceived.setInspection_status("Open");
                            billInwardItemReceivedRepository.save(billInwardItemReceived);
                        }
                    }
                }
                
            }
        }
    }

    public void updateInwardAudit(InwardCheck inwardCheck) {
        if (inwardCheck != null) {
            Optional<InwardAudit> inwardAuditOptional = inwardAuditRepo.findByInwardCheck(inwardCheck.getBatchNo(),
                    inwardCheck.getRecheckLog(), inwardCheck.getProductId());
            if (inwardAuditOptional.isPresent()) {
                InwardAudit inwardAudit = inwardAuditOptional.get();
                inwardAudit.setStatus(inwardCheck.getStatus());
                inwardAudit.setBatch_type("Open");
                if (inwardCheck.getProductId() != null) {
                    inwardAudit.setProductId(inwardCheck.getProductId());
                }
                inwardAudit.setInwardDate(inwardCheck.getInwardDate());
                inwardAudit.setUpdatedBy(inwardCheck.getUpdatedBy());
                inwardAudit.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                inwardAuditRepo.save(inwardAudit);
            } else {
                InwardAudit inwardAudit = new InwardAudit();
                inwardAudit.setBatch_no(inwardCheck.getBatchNo());
                inwardAudit.setEntry_type(inwardCheck.getRecheckLog());
                inwardAudit.setInwardDate(inwardCheck.getInwardDate());
                inwardAudit.setProductId(inwardCheck.getProductId());
                inwardAudit.setCreatedBy(inwardCheck.getCreatedBy());
                inwardAudit.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                inwardAudit.setBatch_type("Open");
                inwardAudit.setStatus(inwardCheck.getStatus());
                inwardAudit.setUpdatedBy(inwardCheck.getUpdatedBy());
                inwardAuditRepo.save(inwardAudit);
            }
        }
    }

    public void closeInwardAudit(InwardCheck inwardCheck, PatlogDto patlogDto) {
        if (inwardCheck != null) {
            Optional<InwardAudit> inwardAuditOptional = inwardAuditRepo.findByInwardCheck(inwardCheck.getBatchNo(),
                    patlogDto.getOldEntryType(), inwardCheck.getProductId());
            if (inwardAuditOptional.isPresent()) {
                InwardAudit inwardAudit = inwardAuditOptional.get();
                inwardAudit.setStatus("Completed");
                inwardAudit.setBatch_type("Close");
                inwardAudit.setInwardDate(inwardCheck.getInwardDate());
                inwardAudit.setUpdatedBy(inwardCheck.getUpdatedBy());
                inwardAudit.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                inwardAuditRepo.save(inwardAudit);
            }
        }
    }

    public ResponseEntity<?> viewInwardInspectionData(String entryType, Long kpiNameId, String inwardDate,
                                                      String batchNo) {

        List<KpiList> stepList = kpiRepository.getAllKpiNameStepsDetails(kpiNameId);
        List<KpiListDto> result = new ArrayList<>();
        if (!stepList.isEmpty()) {
            result = stepList.stream().map(stepData -> {
                KpiListDto step = new KpiListDto(stepData);
                updatesDetails(step, entryType, batchNo, inwardDate);
                return step;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<KpiListDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<KpiListDto>>(result, HttpStatus.OK);
        }

    }

    @Transactional
    public ResponseEntity<?> saveInwardInspectionSkipLog(List<PatlogDto> reqDtos, User user) {
        try {
            List<InwardSkipLog> patList = new ArrayList<>();
            for (PatlogDto reqDto : reqDtos) {
                InwardSkipLog model = new InwardSkipLog(reqDto);
                if (reqDto.getTno() == null) {
                    model.setTno("Fresh Report");
                } else {
                    model.setTno(reqDto.getTno());
                }
                model.setEntry(dateUtil.getStringTODateWk(reqDto.getEntry()));
                patList.add(model);
            }
            inwardSkipLogRepository.saveAll(patList);
            return new ResponseEntity<Status>(new Status("200", "Inward skip Log successfully!!!"), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Status>(new Status("400", "Inward skip Log not saved"), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> updateOilTaste(UpdateOilTasteDTO oilTasteDTO, User user) {
        String bathNo = null;
        String oilFFa = null;
        Long productId = null;
        try {

            return new ResponseEntity<Status>(new Status("200", "Inward updated successfully!!!"), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Status>(new Status("400", "Inward details not updated"), HttpStatus.OK);
    }

    public ResponseEntity<?> listRoqComment() {
        List<RoqCommentList> list = inwardCheckRepository.findRoqComment();
        List<RoqCommentDTO> dto = list.stream().map(data -> new RoqCommentDTO(data)).collect(Collectors.toList());
        return new ResponseEntity<List<RoqCommentDTO>>(dto, HttpStatus.OK);
    }

    public ResponseEntity<?> updateInwardAudit(InwardAuditDTO dto, User loggedUser) {

        Optional<InwardAudit> inwardAuditOptional = inwardAuditRepo.findByInwardCheck(dto.getBatchNo(), dto.getEntryType(), dto.getProductId());

        if (inwardAuditOptional.isPresent() && inwardAuditOptional.get().getRoqcomment() == null) {

            InwardAudit inwardAudit = inwardAuditOptional.get();
            inwardAudit.setRoqcomment(dto.getRoqComment());
            InwardAudit result = inwardAuditRepo.save(inwardAudit);

            HoInwardInspectionDto hoInwardInspectionDto = getHoInwardInspectionData(result);
            hoInwardInspectionDto.setTRANSFER_DATE(dateUtil.getDateTOStringWk(dateUtil.getISTDate()));
            hoInwardInspectionDto.setTRANSFER_BY(loggedUser.getEmployeename());
            RestResponse response = restService.saveHoInwardInspection(hoInwardInspectionDto);

            String responseData = getHoData(response);

            updateLog("InwardInspection", responseData, hoInwardInspectionDto);

            return new ResponseEntity<Status>(new Status("200", "Inward taste added successfully!!!"), HttpStatus.OK);

        } else {
            return new ResponseEntity<Status>(new Status("400", "Inward taste Already Exists"), HttpStatus.OK);
        }

    }


    public HoInwardInspectionDto getHoInwardInspectionData(InwardAudit inwardAudit) {

        HoInwardInspectionDto hoInwardInspectionDto = new HoInwardInspectionDto();

        hoInwardInspectionDto.setINSPECTION_TYPE(inwardAudit.getEntry_type());
        hoInwardInspectionDto.setCOMMENT_ON_TASTE(inwardAudit.getRoqcomment());
        Optional<BillInward> billInward = billInwardRepo.findByInspectionDetails(inwardAudit.getInwardDate(), inwardAudit.getProductId(), inwardAudit.getBatch_no());

        if (billInward.isPresent()) {

            hoInwardInspectionDto.setBILL_NO(billInward.get().getBillNo());
            hoInwardInspectionDto.setBILL_DATE(dateUtil.getDateTOStringWk(billInward.get().getBillDate()));
            hoInwardInspectionDto.setLORRY_DATE(dateUtil.getDateTOStringWk(billInward.get().getReceivedDate()));
            hoInwardInspectionDto.setLORRY_NO(billInward.get().getLorrySerialNo());
            hoInwardInspectionDto.setBILL_ID(billInward.get().getHoBillId());
            BillInwardItemReceived billInwardItemReceived = billInward.get().getItemReceived().get(0);
            Double qty = billInwardItemReceivedRepository.sumOfReceivedQty(billInward.get().getId(), inwardAudit.getProductId());
            String receivedQty = qty + " " + billInwardItemReceived.getLot() + "  (" + billInwardItemReceived.getConv_package_quantity_value() + " Kg ) ";

            hoInwardInspectionDto.setRECEIVED_QTY(receivedQty);
        }

        LinkedHashMap<String, String> linkedHashMap = getAttributesInwardInspectionDataMap(inwardAudit.getEntry_type(), inwardAudit.getBatch_no());

        if (linkedHashMap.containsKey("Oil color category")) {
            hoInwardInspectionDto.setOIL_COLOR_CATEGORY(linkedHashMap.get("Oil color category"));
        }

        if (linkedHashMap.containsKey("Oil taste category")) {
            hoInwardInspectionDto.setOIL_TASTE_CATEGORY(linkedHashMap.get("Oil taste category"));
        }

        if (linkedHashMap.containsKey("Oil FFA")) {
            hoInwardInspectionDto.setOIL_FFA(linkedHashMap.get("Oil FFA"));
        }

        if (linkedHashMap.containsKey("Oil Peroxide value")) {
            hoInwardInspectionDto.setPEROXIDE_VALUE(linkedHashMap.get("Oil Peroxide value"));
        }

        if (linkedHashMap.containsKey("Oil Iodine value")) {
            hoInwardInspectionDto.setIODINE_VALUE(linkedHashMap.get("Oil Iodine value"));
        }


        if (inwardAudit.getProductId() == 773L) {

            if (linkedHashMap.containsKey("Dryage Weight for 3 Bags")) {
                hoInwardInspectionDto.setAVERAGE_WASTE(linkedHashMap.get("Dryage Weight for 3 Bags"));
            }

            if (linkedHashMap.containsKey("Before Moisture")) {
                hoInwardInspectionDto.setMOISTURE(linkedHashMap.get("Before Moisture"));
            }

            if (linkedHashMap.containsKey("Total oil outtern")) {
                hoInwardInspectionDto.setTOTAL_OIL_OUTERN(linkedHashMap.get("Total oil outtern"));
            }

        } else if (inwardAudit.getProductId() == 774L) {


            if (linkedHashMap.containsKey("dryage for three bags in kg")) {
                hoInwardInspectionDto.setAVERAGE_WASTE(linkedHashMap.get("dryage for three bags in kg"));
            }

            if (linkedHashMap.containsKey("Seed Moisture")) {
                hoInwardInspectionDto.setMOISTURE(linkedHashMap.get("Seed Moisture"));
            }

            if (linkedHashMap.containsKey("Total oil Outturn")) {
                hoInwardInspectionDto.setTOTAL_OIL_OUTERN(linkedHashMap.get("Total oil Outturn"));
            }

        }

        return hoInwardInspectionDto;

    }


    public void updateLog(String type, String response, HoInwardInspectionDto jsonData) {
        String status = response;
        Long typeId = jsonData.getBILL_ID();
        String responseData = response;
        String jsonData1 = getHoData(jsonData);
        updateRestLog(type, typeId, status, responseData, jsonData1);
    }

    public void updateRestLog(String type, Long typeId, String status, String response, String jsonData) {

        HOConfigRESTLog hoConfigRESTLog = new HOConfigRESTLog();
        hoConfigRESTLog.setType(type);
        hoConfigRESTLog.setFrom_office("FT");
        hoConfigRESTLog.setTypeId(typeId);
        hoConfigRESTLog.setStatus(status);
        hoConfigRESTLog.setResponse(response);
        hoConfigRESTLog.setJson_data(jsonData);
        hoConfigRESTLog.setCreated_time(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        restLogRepository.save(hoConfigRESTLog);
    }

    public String getHoData(HoInwardInspectionDto hoInwardInspectionDto) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> map = objectMapper.convertValue(hoInwardInspectionDto, new TypeReference<Map<String, Object>>() {
            });
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getHoData(RestResponse hoInwardInspectionDto) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> map = objectMapper.convertValue(hoInwardInspectionDto, new TypeReference<Map<String, Object>>() {
            });
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
