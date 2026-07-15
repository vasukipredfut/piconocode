package com.aria.service;


import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.BinStockUtil;
import com.aria.utils.DateUtil;
import com.aria.utils.DayEndUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ProductionEntryService {

    public final Logger logger = LoggerFactory.getLogger(ProductionEntryService.class);

    private static final DecimalFormat decfor = new DecimalFormat("0.0000");


    @Autowired
    ProductionEntryRepository productionEntryRepository;

    @Autowired
    JournalRepository journalRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    BlendingAdviceDetailsRepository blendingAdviceDetailsRepository;

    @Autowired
    BlendingAdviceRepository blendingAdviceRepository;

    @Autowired
    BlendingAdviceStockDetailsRepository blendingAdviceStockDetailsRepository;

    @Autowired
    GoGroupRepository goGroupRepository;

    @Autowired
    JournalEntryRepository journalEntryRepository;

    @Autowired
    ActionRepository actionRepository;

    @Autowired
    BinStockRepository binStockRepository;

    @Autowired
    BinStockUtil binStockUtil;

    @Autowired
    BatchMasterRepository batchMasterRepository;

    @Autowired
    JournalProductMappingRepository linkSjRepository;

    @Autowired
    ActionConfigurationRepository actionConfigurationRepository;

    @Autowired
    BlendingAdviceBinDetailsRepository binDetailsRepository;

    @Autowired
    WcShiftsRepository wcShiftsRepository;

    @Autowired
    TRNIndexRepo trnIndexRepo;

    @Autowired
    PShiftsRepository pShiftsRepository;

    @Autowired
    SjtQtyRepo sjtQtyRepo;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MachineRepository machineRepository;

    @Autowired
    ProductMasterRepository productMasterRepository;

    @Autowired
    GoDownRepository goDownRepository;

    @Autowired
    DwClusterRepository dwClusterRepository;

    @Autowired
    JournalProductMappingRepository journalProductMappingRepository;

    @Autowired
    MachinePanelReadingMappingRepository panelReadingMappingRepository;


    @Autowired
    BinStockLogRepository binStockLogRepository;

    @Autowired
    BinstockAuditLogRepository binstockAuditLogRepository;

    @Autowired
    BlendingRequestReceiveBinMapRepository blendingRequestReceiveBinMapRepository;
    @Autowired
    BlendingPreprocessRequestAndReceiveBinMapRepository blendingPreprocessRequestAndReceiveBinMapRepository;

    @Autowired
    DayEndUtils dayEndUtils;

    @Autowired
    BlendingStockDetailsRepostiory blendingStockDetailsRepostiory;

    @Autowired
    ASRSProductionDetailsRepository asrsProductionDetailsRepository;


    public ResponseEntity<?> saveProductionEntry(ProductionEntryForm productionEntryForm, User loggedUser) {
        logger.info("Entering saveProductionEntry with productionEntryForm: {} and loggedUser: {}", productionEntryForm, loggedUser);
        try {

            if (productionEntryForm.getEntryIsOn() != null && productionEntryForm.getEntryIsOn().equalsIgnoreCase("Ongoingentry")) {
                String batchNo = null;
                String trnNo = null;
                for (ProductionEntryFormDto productionEntryDto : productionEntryForm.getProductionEntry()) {
                    if (productionEntryDto.getDataSetList() != null && !productionEntryDto.getDataSetList().isEmpty()) {
                        Long productId = null;
                        Optional<Journal> journalOptional = journalRepository.findById(productionEntryDto.getJournalId());
                        if (journalOptional.isPresent()) {
                            productId = journalOptional.get().getPrimaryProductId();
                        }
                        if (productionEntryDto.getBatchNo() == null || productionEntryDto.getBatchNo() == "") {
                            if (batchNo == null) {
                                BatchMaster batchMaster = getBatchNo(productionEntryDto, productId, loggedUser);
                                batchNo = batchMaster.getBatchno();
                                productionEntryDto.setBatchNo(batchMaster.getBatchno());
                                productionEntryDto.setBatchId(batchMaster.getId());
                            } else {
                                BatchMaster batchMaster = findBatch(batchNo);
                                productionEntryDto.setBatchNo(batchMaster.getBatchno());
                                productionEntryDto.setBatchId(batchMaster.getId());
                            }
                        } else {
                            BatchMaster batchMaster = findBatch(productionEntryDto.getBatchNo());
                            productionEntryDto.setBatchNo(batchMaster.getBatchno());
                            productionEntryDto.setBatchId(batchMaster.getId());
                        }
                        if (trnNo == null) {
                            if (productionEntryDto.getTrnNo() == null) {
                                Optional<TRNIndex> trnIndex = trnIndexRepo.findByPrefix("PRODN");
                                if (trnIndex.isPresent()) {
                                    TRNIndex trn = trnIndex.get();
                                    Long trnlast = trn.getLastno();
                                    trnNo = trn.getPrefix() + "-" + trn.getLastno();
                                    productionEntryDto.setTrnNo(trnNo);
                                    Long fstrnlast = trnlast + 1;
                                    trn.setLastno(fstrnlast);
                                    TRNIndex rs = trnIndexRepo.saveAndFlush(trn);
                                }
                            } else {
                                trnNo = productionEntryDto.getTrnNo();
                            }
                        } else {
                            productionEntryDto.setTrnNo(trnNo);
                        }

                        saveWcShift(productionEntryDto, loggedUser, productId);
                        for (ProductionEntryDataSetDto dataSetDto : productionEntryDto.getDataSetList()) {
                            ProductionEntry productionEntry = new ProductionEntry(productionEntryDto);
                            productionEntry.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                            productionEntry.setCreatedBy(loggedUser.getId());
                            productionEntry.setShiftDate(dateUtil.getStringTODateWk(productionEntryDto.getShiftDate()));
                            productionEntry.setColid(dataSetDto.getColid());
                            productionEntry.setColName(dataSetDto.getColName());
                            productionEntry.setColType(dataSetDto.getColtype());
                            productionEntry.setColTable(dataSetDto.getColtable());
                            productionEntry.setColCate(dataSetDto.getConsiderAs());
                            productionEntry.setDataType(dataSetDto.getDataType());
                            productionEntry.setDispUnit(dataSetDto.getDispUnit());
                            productionEntry.setTotal(dataSetDto.getTotal());
                            productionEntry.setAvg(dataSetDto.getAvg());
                            productionEntry.setDataResValues(dataSetDto.getValue());
                            productionEntry.setConsiderAs(dataSetDto.getConsiderAs());
                            productionEntry.setCol_lot(dataSetDto.getColLot());
                            productionEntry.setPType(dataSetDto.getPType());
                            productionEntry.setBomIntakeType(dataSetDto.getBomIntakeType());
                            productionEntry.setAccounting_date(dayEndUtils.getAccountingDate(productionEntryDto.getBranch(), dateUtil.getISTDate()));
                            ProductionEntry peResult = productionEntryRepository.save(productionEntry);
                            if (peResult.getColTable() != null && peResult.getColTable().equalsIgnoreCase("panelmaster")) {
                                updateEnergyMeter(peResult);
                            }
                        }
                    }
                }
                return new ResponseEntity<Status>(new Status("200", "Production entry save successfully!!!"), HttpStatus.OK);
            } else if (productionEntryForm.getEntryIsOn() != null && productionEntryForm.getEntryIsOn().equalsIgnoreCase("endProcess")) {

                String batchNo = null;
                String trnNo = null;
                for (ProductionEntryFormDto productionEntryDto : productionEntryForm.getProductionEntry()) {

                    if (productionEntryDto.getDataSetList() != null && !productionEntryDto.getDataSetList().isEmpty()) {
                        Long productId = null;
                        Optional<Journal> journalOptional = journalRepository.findById(productionEntryDto.getJournalId());
                        if (journalOptional.isPresent()) {
                            productId = journalOptional.get().getPrimaryProductId();
                        }
                        if (productionEntryDto.getBatchNo() == null || productionEntryDto.getBatchNo() == "") {
                            if (batchNo == null) {
                                BatchMaster batchMaster = getBatchNo(productionEntryDto, productId, loggedUser);
                                batchNo = batchMaster.getBatchno();
                                productionEntryDto.setBatchNo(batchMaster.getBatchno());
                                productionEntryDto.setBatchId(batchMaster.getId());
                            } else {
                                BatchMaster batchMaster = findBatch(batchNo);
                                productionEntryDto.setBatchNo(batchMaster.getBatchno());
                                productionEntryDto.setBatchId(batchMaster.getId());
                            }
                        } else {
                            BatchMaster batchMaster = findBatch(productionEntryDto.getBatchNo());
                            productionEntryDto.setBatchNo(batchMaster.getBatchno());
                            productionEntryDto.setBatchId(batchMaster.getId());
                        }

                        if (trnNo == null) {
                            if (productionEntryDto.getTrnNo() == null) {
                                Optional<TRNIndex> trnIndex = trnIndexRepo.findByPrefix("PRODN");
                                if (trnIndex.isPresent()) {
                                    TRNIndex trn = trnIndex.get();
                                    Long trnlast = trn.getLastno();
                                    trnNo = trn.getPrefix() + "-" + trn.getLastno();
                                    productionEntryDto.setTrnNo(trnNo);
                                    Long fstrnlast = trnlast + 1;
                                    trn.setLastno(fstrnlast);
                                    TRNIndex rs = trnIndexRepo.saveAndFlush(trn);
                                }
                            } else {
                                trnNo = productionEntryDto.getTrnNo();
                            }
                        } else {
                            productionEntryDto.setTrnNo(trnNo);
                        }

                        saveWcShift(productionEntryDto, loggedUser, productId);
                        for (ProductionEntryDataSetDto dataSetDto : productionEntryDto.getDataSetList()) {
                            ProductionEntry productionEntry = new ProductionEntry(productionEntryDto);
                            productionEntry.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                            productionEntry.setCreatedBy(loggedUser.getId());
                            productionEntry.setShiftDate(dateUtil.getStringTODateWk(productionEntryDto.getShiftDate()));
                            productionEntry.setColid(dataSetDto.getColid());
                            productionEntry.setColName(dataSetDto.getColName());
                            productionEntry.setColType(dataSetDto.getColtype());
                            productionEntry.setColTable(dataSetDto.getColtable());
                            productionEntry.setColCate(dataSetDto.getConsiderAs());
                            productionEntry.setDataType(dataSetDto.getDataType());
                            productionEntry.setDispUnit(dataSetDto.getDispUnit());
                            productionEntry.setTotal(dataSetDto.getTotal());
                            productionEntry.setAvg(dataSetDto.getAvg());
                            productionEntry.setDataResValues(dataSetDto.getValue());
                            productionEntry.setConsiderAs(dataSetDto.getConsiderAs());
                            productionEntry.setCol_lot(dataSetDto.getColLot());
                            productionEntry.setPType(dataSetDto.getPType());
                            productionEntry.setBomIntakeType(dataSetDto.getBomIntakeType());
                            productionEntry.setAccounting_date(dayEndUtils.getAccountingDate(productionEntryDto.getBranch(), dateUtil.getISTDate()));
                            ProductionEntry peResult = productionEntryRepository.save(productionEntry);
                            if (peResult.getColTable() != null && peResult.getColTable().equalsIgnoreCase("panelmaster")) {
                                updateEnergyMeter(peResult);
                            }
                        }
                    }
                }
                logger.info("Updating Journal Entry for endProcess with batchNo: {} and trnNo: {}", batchNo, trnNo);
                ProductionEntryFormDto journalEntryDto = productionEntryForm.getJournalEntry();
                if (journalEntryDto.getBatchNo() != null && journalEntryDto.getBatchNo() != "") {
                    System.out.println("journalEntryDto.getBatchNo() == " + journalEntryDto.getBatchNo());
                    if (journalEntryDto.getBatchId() == null) {
                        BatchMaster batchMaster = findBatch(journalEntryDto.getBatchNo());
                        journalEntryDto.setBatchNo(batchMaster.getBatchno());
                        journalEntryDto.setBatchId(batchMaster.getId());
                    }
                } else {
                    logger.info("Finding batch for endProcess with batchNo: {}", batchNo);
                    BatchMaster batchMaster = findBatch(batchNo);
                    journalEntryDto.setBatchNo(batchMaster.getBatchno());
                    journalEntryDto.setBatchId(batchMaster.getId());
                }
                if (journalEntryDto.getTrnNo() == null || journalEntryDto.getTrnNo().isEmpty()) {
                    journalEntryDto.setTrnNo(trnNo);
                }
                Long productId = null;
                Optional<Journal> journalOptional = journalRepository.findById(journalEntryDto.getJournalId());
                if (journalOptional.isPresent()) {
                    productId = journalOptional.get().getPrimaryProductId();
                }
                updateWcShift(journalEntryDto, loggedUser, productId);
                updateJournalEntry(journalEntryDto, loggedUser);

                return new ResponseEntity<Status>(new Status("200", "Production entry save successfully!!!"), HttpStatus.OK);
            } else {
                return new ResponseEntity<Status>(new Status("400", "Production entry not saved!!!"), HttpStatus.OK);
            }
        } catch (Exception e) {
            logger.error("Error in saveProductionEntry: ", e);
            return new ResponseEntity<Status>(new Status("500", "Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    public BatchMaster checkBatch(String batchNo, ProductionEntryFormDto entryDto, User loggedUser) {

        System.out.println("check batch = " + batchNo);

        Optional<BatchMaster> optionalBatchMaster = batchMasterRepository.findByBatchno(batchNo);
        if (!optionalBatchMaster.isPresent()) {
            BatchMaster batchMaster = new BatchMaster();
            batchMaster.setBatchno(batchNo);
            batchMaster.setDept(loggedUser.getDepartment().getId());
            batchMaster.setBranch(loggedUser.getBranch_id());
            //batchMaster.setProductId(entryDto.ge());
            batchMaster.setShiftdt(dateUtil.getStringTODateWk(entryDto.getShiftDate()));
            batchMaster.setShiftid(entryDto.getShiftId());
            batchMaster.setCreatedby(loggedUser.getId());
            batchMaster.setBtype("PRODUCTION");
            BatchMaster result = batchMasterRepository.save(batchMaster);
            return result;
        } else {
            return optionalBatchMaster.get();
        }
    }

    public void updateDataSet(ProductionEntry productionEntry, ProductionEntryFormDto productionEntryFormDto, ProductionEntryDataSetDto dataSetDto) throws UsernameNotFoundException {
        productionEntry.setColid(dataSetDto.getColid());
        productionEntry.setColType(dataSetDto.getColtype());
        productionEntry.setColTable(dataSetDto.getColtable());
        productionEntry.setDataType(dataSetDto.getDataType());
        productionEntry.setDispUnit(dataSetDto.getDispUnit());
        productionEntry.setTotal(dataSetDto.getTotal());
        productionEntry.setAvg(dataSetDto.getAvg());
        productionEntry.setDataResValues(dataSetDto.getValue());

        /*if (productionEntryFormDto.getTsId() == 1 || productionEntryFormDto.getTsId() == 9 || productionEntryFormDto.getTsId() == 15) {
            productionEntry.setC1(dataSetDto.getValue());
        } else if (productionEntryFormDto.getTsId() == 2 || productionEntryFormDto.getTsId() == 10 || productionEntryFormDto.getTsId() == 16) {
            productionEntry.setC2(dataSetDto.getValue());
        } else if (productionEntryFormDto.getTsId() == 3 || productionEntryFormDto.getTsId() == 11 || productionEntryFormDto.getTsId() == 17) {
            productionEntry.setC3(dataSetDto.getValue());
        } else if (productionEntryFormDto.getTsId() == 4 || productionEntryFormDto.getTsId() == 12 || productionEntryFormDto.getTsId() == 18) {
            productionEntry.setC4(dataSetDto.getValue());
        } else if (productionEntryFormDto.getTsId() == 5 || productionEntryFormDto.getTsId() == 13 || productionEntryFormDto.getTsId() == 19) {
            productionEntry.setC5(dataSetDto.getValue());
        } else if (productionEntryFormDto.getTsId() == 6 || productionEntryFormDto.getTsId() == 14 || productionEntryFormDto.getTsId() == 20) {
            productionEntry.setC6(dataSetDto.getValue());
        } else if (productionEntryFormDto.getTsId() == 7 || productionEntryFormDto.getTsId() == 21) {
            productionEntry.setC7(dataSetDto.getValue());
        } else if (productionEntryFormDto.getTsId() == 8 || productionEntryFormDto.getTsId() == 22) {
            productionEntry.setC8(dataSetDto.getValue());
        } else if (productionEntryFormDto.getTsId() == 23) {
            productionEntry.setC9(dataSetDto.getValue());
        } else if (productionEntryFormDto.getTsId() == 24) {
            productionEntry.setC10(dataSetDto.getValue());
        } else if (productionEntryFormDto.getTsId() == 25) {
            productionEntry.setCTOTAL(Double.valueOf(dataSetDto.getValue()));
        } else if (productionEntryFormDto.getTsId() == 26) {
            productionEntry.setCAVG(Double.valueOf(dataSetDto.getValue()));
        }*/
    }

    public ResponseEntity<?> updateProductionEntry(ProductionEntryDto productionEntryDto, User loggedUser) {
        ProductionEntry productionEntry = new ProductionEntry(productionEntryDto);
        productionEntry.setShiftDate(dateUtil.getStringTODateF1(productionEntryDto.getShiftDate()));
        productionEntry.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        productionEntry.setUpdatedBy(loggedUser.getId());
        productionEntryRepository.save(productionEntry);
        return new ResponseEntity<Status>(new Status("200", "Production entry updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAll(Pageable pageable) throws UsernameNotFoundException {

        Page<ProductionEntry> list = productionEntryRepository.findAll(pageable);

        if (!list.toList().isEmpty()) {
            List<ProductionEntryDto> result = list.toList().stream().map(data -> {
                        ProductionEntryDto productionEntryDto = new ProductionEntryDto(data);
                        productionEntryDto.setShiftDate(dateUtil.getStringTODateF1(data.getShiftDate()));
                        return productionEntryDto;
                    })
                    .collect(Collectors.toList());
            PageDataDto<AssignDutyFilterListResponseDTO> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<AssignDutyFilterListResponseDTO>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<AssignDutyFilterListResponseDTO>>(new PageDataDto(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> getProductionEntryFilterList(Long branchId, Long deptId, Long journalId, String journalName, String status, String fromDate, String toDate, Pageable pageable) throws UsernameNotFoundException {

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

        if (status == null) {
            status = "N";
        }

        Page<ProductionEntryList> list = productionEntryRepository.getProductionEntryFilterList(branchId, deptId, journalId, journalName, status, startDate, endDate, pageable);

        if (!list.toList().isEmpty()) {
            List<ProductionEntryListDto> result = list.toList().stream().map(data -> {
                ProductionEntryListDto pEntry = new ProductionEntryListDto(data);
                if (pEntry.getMachineId() != null) {

                    if (pEntry.getMmctable() != null && pEntry.getMmctable().equalsIgnoreCase("machine")) {
                        Optional<Machine> optionalMachine = machineRepository.findById(pEntry.getMachineId());
                        if (optionalMachine.isPresent()) {
                            pEntry.setMachineName(optionalMachine.get().getName());
                        }
                    } else {
                        Optional<DwCluster> optionalMachine = dwClusterRepository.findById(pEntry.getMachineId());
                        if (optionalMachine.isPresent()) {
                            pEntry.setMachineName(optionalMachine.get().getName());
                        }
                    }
                }
                ProductionEntry productionEntry = productionEntryRepository.findBy(pEntry.getJournalId(), pEntry.getBatchId(), pEntry.getBatchNo(), pEntry.getTrnNo());
                if (productionEntry != null) {
                    pEntry.setTsId(productionEntry.getTsId());
                    pEntry.setTsName(productionEntry.getTsName());
                }

                boolean asrsProductionDetails = asrsProductionDetailsRepository.existsByTrnNoAndJournalId(pEntry.getTrnNo(), pEntry.getJournalId());
                if (asrsProductionDetails) {
                    pEntry.setAsrs(true);
                } else {
                    pEntry.setAsrs(false);
                }

                return pEntry;
            }).collect(Collectors.toList());

            PageDataDto<ProductionEntryListDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<ProductionEntryListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<ProductionEntryListDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> getJournalEntryFilterList(Long journalId, String journalName, String fromDate, String toDate, Pageable pageable) throws UsernameNotFoundException {
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
        Page<JournalEntryList> list = journalEntryRepository.getJournalEntryFilterList(journalId, journalName, startDate, endDate, pageable);

        if (!list.toList().isEmpty()) {
            List<JournalEntryListDto> result = list.toList().stream().map(data -> new JournalEntryListDto(data)).collect(Collectors.toList());
            PageDataDto<JournalEntryListDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<JournalEntryListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<JournalEntryListDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getProductionEntry(String id) throws UsernameNotFoundException {
        Optional<ProductionEntry> optionalProductionEntry = productionEntryRepository
                .findById(Long.parseLong(id));
        ProductionEntryDto productionEntryDto = new ProductionEntryDto(optionalProductionEntry.get());

        return new ResponseEntity<ProductionEntryDto>(productionEntryDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteProductionEntry(String id) throws UsernameNotFoundException {
        try {
            Optional<ProductionEntry> optionalProductionEntry = productionEntryRepository
                    .findById(Long.parseLong(id));
            if (optionalProductionEntry.isPresent()) {
                ProductionEntry batchtype = optionalProductionEntry.get();
                productionEntryRepository.delete(batchtype);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }


    public ResponseEntity<?> listAllBom(Long journalId, Long shiftId, String cate, String shiftDate, Long branch) throws UsernameNotFoundException {

        Date convertDate = null;
        if (shiftDate != null) {
            convertDate = dateUtil.getStringTODateWk(shiftDate);
        } else {
            convertDate = dateUtil.getISTDate();
        }

        Optional<BlendingAdvice> blendingAdvice = blendingAdviceRepository.findByDate(convertDate, journalId, branch);

        List<BlendingCasGroupListDto> blendingCasGroupList = new ArrayList<>();
        if (blendingAdvice.isPresent()) {

            Optional<BlendingAdviceDetails> blendingAdviceDetails = blendingAdviceDetailsRepository.findBy(journalId, blendingAdvice.get().getId(), branch);

            if (blendingAdviceDetails.isPresent()) {

                List<BlendingAdviceStockDetails> blendingAdviceStockDetails = blendingAdviceStockDetailsRepository.findBy(blendingAdviceDetails.get().getId());

                if (!blendingAdviceStockDetails.isEmpty()) {

                    for (BlendingAdviceStockDetails resultBlending : blendingAdviceStockDetails) {

                        BlendingCasGroupListDto blendingCasGroupListDto = new BlendingCasGroupListDto();
                        blendingCasGroupListDto.setCasGroup(resultBlending.getCasGroup());
                        List<LinkSjList> list = linkSjRepository.findByRawMaterialList(journalId, cate);
                        if (!list.isEmpty()) {

                            List<LinkSjListDto> result = list.stream().map(data -> {

                                LinkSjListDto linkSj = new LinkSjListDto(data);

                                List<BlendingBinList> seaSameList = binDetailsRepository.findByGroupList(resultBlending.getId(), linkSj.getProductId(), branch);

                                List<BlendingAdviceBinDetailsDto> rsbinList = seaSameList.stream().map(val -> new BlendingAdviceBinDetailsDto(val)).collect(Collectors.toList());

                                updateMaterialRequestBinANDPreprocessBinList(resultBlending, branch, rsbinList);

                                if (!rsbinList.isEmpty()) {
                                    linkSj.setBinDetailsList(rsbinList);
                                }
                                return linkSj;

                            }).collect(Collectors.toList());

                            blendingCasGroupListDto.setProductList(result);
                        }
                        blendingCasGroupList.add(blendingCasGroupListDto);

                    }
                    return new ResponseEntity<List<BlendingCasGroupListDto>>(blendingCasGroupList, HttpStatus.OK);
                }
            }
        } else {
            BlendingCasGroupListDto blendingCasGroupListDto = new BlendingCasGroupListDto();

            List<LinkSjList> list = linkSjRepository.findByRawMaterialList(journalId, cate);
            if (!list.isEmpty()) {
                List<LinkSjListDto> result = list.stream().map(data -> {
                    LinkSjListDto linkSj = new LinkSjListDto(data);
                    return linkSj;
                }).collect(Collectors.toList());
                blendingCasGroupListDto.setProductList(result);
            }
            blendingCasGroupList.add(blendingCasGroupListDto);
            return new ResponseEntity<List<BlendingCasGroupListDto>>(blendingCasGroupList, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


    public void updateMaterialRequestBinANDPreprocessBinList(BlendingAdviceStockDetails blendingAdviceStockDetails, Long branch, List<BlendingAdviceBinDetailsDto> rsbinList) {


        List<BlendingBinList> blendingRequestlist = blendingRequestReceiveBinMapRepository.findBy(blendingAdviceStockDetails.getBlendingAdviceDetailsId());
        if (!blendingRequestlist.isEmpty()) {
            List<BlendingAdviceBinDetailsDto> rsList = blendingRequestlist.stream().map(val -> {
                BlendingAdviceBinDetailsDto binDetailsDto = new BlendingAdviceBinDetailsDto(val);
                binDetailsDto.setBlendingAdviceStockId(blendingAdviceStockDetails.getId());
                return binDetailsDto;
            }).collect(Collectors.toList());
            rsbinList.addAll(rsList);
        }

        List<BlendingBinList> blendingPreProcesslist = blendingPreprocessRequestAndReceiveBinMapRepository.findBy(blendingAdviceStockDetails.getBlendingAdviceDetailsId());
        if (!blendingPreProcesslist.isEmpty()) {
            List<BlendingAdviceBinDetailsDto> rsList = blendingPreProcesslist.stream().map(val -> {
                BlendingAdviceBinDetailsDto binDetailsDto = new BlendingAdviceBinDetailsDto(val);
                binDetailsDto.setBlendingAdviceStockId(blendingAdviceStockDetails.getId());
                binDetailsDto.setType("PreProcessReceive");
                return binDetailsDto;
            }).collect(Collectors.toList());
            rsbinList.addAll(rsList);
        }
    }


    public ResponseEntity<?> listAllBye(Long journalId, Long shiftId, String cate, String shiftDate, Long branch) throws UsernameNotFoundException {

        List<LinkSjList> list = linkSjRepository.findByRawMaterialList(journalId, cate);
        if (!list.isEmpty()) {
            List<LinkSjListDto> result = list.stream().map(data -> {
                LinkSjListDto linkSj = new LinkSjListDto(data);
                return linkSj;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<LinkSjListDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<LinkSjListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public void updateJournalEntry(ProductionEntryFormDto productionEntryFormDto, User loggedUser) {


        MathDto finalQty = new MathDto();
        HashMap<String, Double> stringDoubleHashMap = new HashMap<>();
        Long productId = null;
        String cate = null;
        List<String> binNoList = new ArrayList<>();
        List<Long> journalIdList = new ArrayList<>();
        Boolean blending = false;
        String stockType = null;
        Optional<Journal> journalOptional = journalRepository.findById(productionEntryFormDto.getJournalId());
        if (journalOptional.isPresent()) {
            productId = journalOptional.get().getPrimaryProductId();
            if (journalOptional.get().getProductionCategory() != null) {
                cate = journalOptional.get().getProductionCategory();
            }
            if (journalOptional.get().getBlending() == true) {
                blending = true;
            }
            /*if (journalOptional.get().getStockType() != null && journalOptional.get().getStockType().equalsIgnoreCase("Delite Stock")) {
                stockType = "DT";
            }*/
        }

        for (ProductionEntryClosingDataDto productionEntryClosingDataDto : productionEntryFormDto.getBomDataList()) {

            if(productionEntryClosingDataDto.getBomIntakeType() != null && (productionEntryClosingDataDto.getBomIntakeType().equalsIgnoreCase("Process Waste") && productionEntryClosingDataDto.getBinNo() == null)) {
                JournalEntry journalEntry = new JournalEntry();
                updateJournalEntryProduct(journalEntry, productionEntryClosingDataDto, loggedUser);
                journalEntry.setAtrndate(dateUtil.getStringTODateWk(productionEntryFormDto.getShiftDate()));
                journalEntry.setAdate(dateUtil.getISTDate());
                journalEntry.setTrndate(dateUtil.getISTDate());
                journalEntry.setAby(loggedUser.getId());
                journalEntry.setWcid(productionEntryFormDto.getMmcId());
                journalEntry.setWctable(productionEntryFormDto.getMmcTable());
                journalEntry.setEby(loggedUser.getId());
                BatchMaster batch = findBatch(productionEntryFormDto, productId, loggedUser);
                journalEntry.setBatchid(batch.getId());
                journalEntry.setBatchno(batch.getBatchno());
                journalEntry.setBid(productionEntryFormDto.getBranch());
                journalEntry.setDid(loggedUser.getDepartment().getId());
                journalEntry.setFirmId(productionEntryFormDto.getFirmId());
                if(productionEntryFormDto.getGodownId() != null) {
                    journalEntry.setGodown(productionEntryFormDto.getGodownId());
                }
                journalEntry.setLot(Double.valueOf(productionEntryClosingDataDto.getLot()));
                journalEntry.setTrnno(productionEntryFormDto.getTrnNo());
                journalEntry.setSjid(productionEntryFormDto.getJournalId());
                journalEntry.setPshiftid(productionEntryFormDto.getShiftId());
                journalEntry.setBomIntakeType(productionEntryClosingDataDto.getBomIntakeType());
                journalEntry.setAccounting_date(dayEndUtils.getAccountingDate(loggedUser.getBranch_id(), dateUtil.getISTDate()));

                updateWasteReduceBinStock(journalEntry, finalQty, loggedUser, stringDoubleHashMap, productionEntryClosingDataDto);

                if (productionEntryClosingDataDto.getTotalInputQuantity() != null && productionEntryClosingDataDto.getTotalInputQuantity() != "") {
                    journalEntry.setTotalInputQty(Double.valueOf(productionEntryClosingDataDto.getTotalInputQuantity()));
                }
                journalEntry.setSrce("sjtrn");
                journalEntry.setAstatus("A");
                journalEntry.setTrntype("I");
                journalEntry.setType("bom");
                JournalEntry result = journalEntryRepository.save(journalEntry);
                binNoList.add(result.getBinname() + "$" + result.getBatchno() + "$" + result.getLot());
            } else {
                JournalEntry journalEntry = new JournalEntry();
                updateJournalEntryProduct(journalEntry, productionEntryClosingDataDto, loggedUser);
                journalEntry.setAtrndate(dateUtil.getStringTODateWk(productionEntryFormDto.getShiftDate()));
                journalEntry.setAdate(dateUtil.getISTDate());
                journalEntry.setTrndate(dateUtil.getISTDate());
                journalEntry.setAby(loggedUser.getId());
                journalEntry.setWcid(productionEntryFormDto.getMmcId());
                journalEntry.setWctable(productionEntryFormDto.getMmcTable());
                journalEntry.setEby(loggedUser.getId());
                BatchMaster batch = findBatch(productionEntryFormDto, productId, loggedUser);
                journalEntry.setBatchid(batch.getId());
                journalEntry.setBatchno(batch.getBatchno());
                journalEntry.setBid(productionEntryFormDto.getBranch());
                journalEntry.setDid(loggedUser.getDepartment().getId());
                journalEntry.setFirmId(productionEntryFormDto.getFirmId());
                journalEntry.setGodown(productionEntryFormDto.getGodownId());
                journalEntry.setLot(Double.valueOf(productionEntryClosingDataDto.getLot()));
                journalEntry.setTrnno(productionEntryFormDto.getTrnNo());
                journalEntry.setSjid(productionEntryFormDto.getJournalId());
                journalEntry.setPshiftid(productionEntryFormDto.getShiftId());
                journalEntry.setBomIntakeType(productionEntryClosingDataDto.getBomIntakeType());
                journalEntry.setAccounting_date(dayEndUtils.getAccountingDate(loggedUser.getBranch_id(), dateUtil.getISTDate()));

                updateCostReduceBinStock(journalEntry, finalQty, loggedUser, stringDoubleHashMap, blending, stockType, productionEntryClosingDataDto);

                if (productionEntryClosingDataDto.getTotalInputQuantity() != null && productionEntryClosingDataDto.getTotalInputQuantity() != "") {
                    journalEntry.setTotalInputQty(Double.valueOf(productionEntryClosingDataDto.getTotalInputQuantity()));
                }
                journalEntry.setSrce("sjtrn");
                journalEntry.setAstatus("A");
                journalEntry.setTrntype("I");
                journalEntry.setType("bom");
                JournalEntry result = journalEntryRepository.save(journalEntry);
                binNoList.add(result.getBinname() + "$" + result.getBatchno() + "$" + result.getLot());
                //binStockUtil.updateBinLogProduction(result, loggedUser, "PRODUCTION", "I");
            }
        }

        for (ProductionEntryClosingDataDto productionEntryClosingDataDto : productionEntryFormDto.getByeDataList()) {
            JournalEntry journalEntry = new JournalEntry();
            journalEntry.setAtrndate(dateUtil.getStringTODateWk(productionEntryFormDto.getShiftDate()));
            journalEntry.setAdate(dateUtil.getISTDate());
            journalEntry.setTrndate(dateUtil.getISTDate());
            journalEntry.setAby(loggedUser.getId());
            journalEntry.setEby(loggedUser.getId());
            journalEntry.setWcid(productionEntryFormDto.getMmcId());
            journalEntry.setWctable(productionEntryFormDto.getMmcTable());
            updateJournalEntryProduct(journalEntry, productionEntryClosingDataDto, loggedUser);
            BatchMaster batch = findBatch(productionEntryFormDto, productId, loggedUser);
            journalEntry.setBatchid(batch.getId());
            journalEntry.setBatchno(batch.getBatchno());
            journalEntry.setLot(Double.valueOf(productionEntryClosingDataDto.getLot()));
            updateCostCreateBinStock(journalEntry, finalQty);
            journalEntry.setSjid(productionEntryFormDto.getJournalId());
            journalEntry.setPshiftid(productionEntryFormDto.getShiftId());
            journalEntry.setSrce("sjtrn");
            journalEntry.setAstatus("A");
            journalEntry.setTrntype("R");
            journalEntry.setType("bye");
            journalEntry.setBid(productionEntryFormDto.getBranch());
            journalEntry.setDid(loggedUser.getDepartment().getId());
            journalEntry.setFirmId(productionEntryFormDto.getFirmId());
            journalEntry.setGodown(productionEntryFormDto.getGodownId());
            journalEntry.setTrnno(productionEntryFormDto.getTrnNo());
            journalEntry.setBomIntakeType(productionEntryClosingDataDto.getBomIntakeType());
            if (productionEntryClosingDataDto.getTotalOutputQuantity() != null && productionEntryClosingDataDto.getTotalOutputQuantity() != "") {
                journalEntry.setTotalOutputQty(Double.valueOf(productionEntryClosingDataDto.getTotalOutputQuantity()));
            }
            journalEntry.setAccounting_date(dayEndUtils.getAccountingDate(loggedUser.getBranch_id(), dateUtil.getISTDate()));
            JournalEntry result = journalEntryRepository.save(journalEntry);
            binStockUtil.updateAriaGradeSeedStockOROil(result, loggedUser, stringDoubleHashMap, cate);
            binStockUtil.updateBinLogProduction(result, loggedUser, "PRODUCTION", "R");

            if (cate.equalsIgnoreCase("pre") || cate.equalsIgnoreCase("PRE")) {
                journalIdList.add(result.getId());
            }

        }

        if (cate.equalsIgnoreCase("pre") || cate.equalsIgnoreCase("PRE")) {
            updatePreProcessBinMap(journalIdList, binNoList);
        }
    }


    public void updateJournalEntryProduct(JournalEntry journalEntry, ProductionEntryClosingDataDto productionEntryClosingDataDto, User loggedUser) {

        journalEntry.setProid(productionEntryClosingDataDto.getProductId());
        journalEntry.setCasgrp(productionEntryClosingDataDto.getCasgroup());
        if ((productionEntryClosingDataDto.getCasgroup() != null) || (productionEntryClosingDataDto.getBlendingStockId() != null)) {
            Optional<BlendingAdviceStockDetails> blendingAdviceStockDetails = null;
            if (productionEntryClosingDataDto.getBlendingStockId() != null) {
                blendingAdviceStockDetails = blendingAdviceStockDetailsRepository.findById(productionEntryClosingDataDto.getBlendingStockId());
            } else {
                blendingAdviceStockDetails = blendingAdviceStockDetailsRepository.findBy(productionEntryClosingDataDto.getCasgroup(),
                        journalEntry.getBid(),
                        productionEntryClosingDataDto.getProductId(),
                        journalEntry.getSjid());
            }
            if (blendingAdviceStockDetails.isPresent()) {
                journalEntry.setCasweekid(blendingAdviceStockDetails.get().getBlendingAdviceDetailsId());
                updateCasAction(blendingAdviceStockDetails.get(), loggedUser);
            }
        }
        if(productionEntryClosingDataDto.getBinNo() != null) {
            Optional<GoGroup> groupOptional = goGroupRepository.findByBinNo(Long.valueOf(productionEntryClosingDataDto.getBinNo()));
            if (groupOptional.isPresent()) {
                journalEntry.setBinname(groupOptional.get().getGoName());
                journalEntry.setBinid(groupOptional.get().getGCode());
            }
        }
        if (productionEntryClosingDataDto.getLot() != null) {
            journalEntry.setLot(Double.valueOf(productionEntryClosingDataDto.getLot()));
        }
        journalEntry.setStkqty(Double.valueOf(productionEntryClosingDataDto.getQuantity()));
    }

    public void updateCostReduceBinStock(JournalEntry journalEntry,
                                         MathDto finalQty,
                                         User loggedUser,
                                         HashMap<String, Double> stringDoubleHashMap,
                                         Boolean blending, String stockType,
                                         ProductionEntryClosingDataDto productionEntryClosingDataDto) {

        List<BinStock> binStocks = null;

        System.out.println(" BlendingStockId " + productionEntryClosingDataDto.getBlendingStockId());
        System.out.println(" Bid " + journalEntry.getBid());
        System.out.println(" ProductId " + productionEntryClosingDataDto.getProductId());
        System.out.println(" BinNo " + productionEntryClosingDataDto.getBinNo());
        System.out.println(" Lot " + productionEntryClosingDataDto.getLot());
        System.out.println(" Casgroup " + productionEntryClosingDataDto.getCasgroup());
        System.out.println(" journal id " + journalEntry.getSjid());
        System.out.println(" stockType " + stockType);
        System.out.println(" BlendingStockType " + productionEntryClosingDataDto.getBlendingStockType());

        String blendingStockType = productionEntryClosingDataDto.getBlendingStockType();

        if (journalEntry.getBatchno().contains("Prdn")) {

            if (blending == true) {

                /*if (stockType != null) {

                    if ((productionEntryClosingDataDto.getCasgroup() != null) && (productionEntryClosingDataDto.getBlendingStockId() != null)) {

                        logger.info(" getCasgroup & blending stock id ");

                        if (productionEntryClosingDataDto.getBlendingStockId() != null) {
                            System.out.println("  blending stock id ");
                            binStocks = binStockRepository.findByBlendingStockIdWithDelite(productionEntryClosingDataDto.getBlendingStockId(),
                                    journalEntry.getBid(), productionEntryClosingDataDto.getProductId(),
                                    Long.valueOf(productionEntryClosingDataDto.getBinNo()), journalEntry.getLot());
                            System.out.println(" blending delite stock  == " + binStocks);
                        } else {
                            System.out.println(" getCasgroup  ");

                            binStocks = binStockRepository.findByBlendingCasgrpWithDelite(productionEntryClosingDataDto.getCasgroup(),
                                    journalEntry.getSjid(), journalEntry.getBid(), productionEntryClosingDataDto.getProductId(),
                                    Long.valueOf(productionEntryClosingDataDto.getBinNo()), journalEntry.getLot());

                            System.out.println(" getCasgroup binstock delite == " + binStocks);
                        }
                    } else {
                        System.out.println(" blending 1 == no cas group ");
                        binStocks = binStockRepository.findByBinStocksDelite(journalEntry.getProid(),
                                Long.valueOf(journalEntry.getBinname()), journalEntry.getBinid(), journalEntry.getLot());
                        System.out.println(" blending 1 == " + binStocks);
                    }

                } else {*/

                System.out.println(" blending == " + blending);

                if ((productionEntryClosingDataDto.getCasgroup() != null) && (productionEntryClosingDataDto.getBlendingStockId() != null)) {

                    if (blendingStockType.equalsIgnoreCase("BlendingBin")) {

                        binStocks = binStockRepository.findByBlendingStockId(productionEntryClosingDataDto.getBlendingStockId(),
                                journalEntry.getBid(), productionEntryClosingDataDto.getProductId(),
                                Long.valueOf(productionEntryClosingDataDto.getBinNo()), journalEntry.getLot(), productionEntryClosingDataDto.getCasgroup());

                    } else if (blendingStockType.equalsIgnoreCase("PreProcessBin")) {

                        binStocks = binStockRepository.findByBlendingStockId1(productionEntryClosingDataDto.getBlendingStockId(),
                                journalEntry.getBid(), productionEntryClosingDataDto.getProductId(),
                                Long.valueOf(productionEntryClosingDataDto.getBinNo()), journalEntry.getLot(), productionEntryClosingDataDto.getCasgroup());

                    } else if (blendingStockType.equalsIgnoreCase("ExternalReceiveBin")) {

                        binStocks = binStockRepository.findByBlendingStockId2(productionEntryClosingDataDto.getBlendingStockId(),
                                journalEntry.getBid(), productionEntryClosingDataDto.getProductId(),
                                Long.valueOf(productionEntryClosingDataDto.getBinNo()), journalEntry.getLot(), productionEntryClosingDataDto.getCasgroup());

                    }

                } else {
                    System.out.println(" blending 1 == no cas group ");
                    binStocks = binStockRepository.findByBinStocks(journalEntry.getProid(),
                            Long.valueOf(journalEntry.getBinname()), journalEntry.getBinid(), journalEntry.getLot());
                    System.out.println(" blending 1 == " + binStocks);
                }
                // }

            } else {
                System.out.println(" noooo blending  == ");

                /*if (stockType != null && stockType.equalsIgnoreCase("DT")) {
                    binStocks = binStockRepository.findByBinStocksWithDeliteOnly(journalEntry.getProid(),
                            Long.valueOf(journalEntry.getBinname()), journalEntry.getBinid(), journalEntry.getLot());
                    System.out.println(" noooo blending delite binStock list   == "+binStocks);
                } else {*/
                binStocks = binStockRepository.findByBinStocks(journalEntry.getProid(),
                        Long.valueOf(journalEntry.getBinname()), journalEntry.getBinid(), journalEntry.getLot());
                System.out.println(" noooo blending binStock list == " + binStocks);
                //}

            }
        } else {
            System.out.println(" DirectPurchase  Batch == ");
           /* if (stockType != null && stockType.equalsIgnoreCase("DT")) {
                binStocks = binStockRepository.findByBinStockByBinNoANdBinIdDeliteOnly(journalEntry.getProid(), Long.valueOf(journalEntry.getBinname()), journalEntry.getBinid(), journalEntry.getLot(), journalEntry.getBatchno());
                System.out.println(" DirectPurchase batch delite binStock list  == " + binStocks);
            }else {*/
            binStocks = binStockRepository.findByBinStockByBinNoANdBinId(journalEntry.getProid(), Long.valueOf(journalEntry.getBinname()), journalEntry.getBinid(), journalEntry.getLot(), journalEntry.getBatchno());
            System.out.println(" DirectPurchase batch  binStock list   == " + binStocks);
            // }
        }

        System.out.println("######## ALG-BOM #########");
        System.out.println("binstockList ==== " + binStocks);

        if (binStocks != null && !binStocks.isEmpty()) {

            Double validQty = journalEntry.getStkqty();
            Double getStkQty = journalEntry.getStkqty();
            Double remaining = Double.valueOf(0);
            Double finalAvgCost = Double.valueOf(0);
            Double finalCost = Double.valueOf(0);
            System.out.println(" validQty ==== " + validQty);
            String batchNo = null;

            for (BinStock binStock : binStocks) {

                stringDoubleHashMap.put(binStock.getBatchNo(), binStock.getOilFFA());

                if (batchNo == null) {
                    batchNo = binStock.getBatchNo();
                } else {
                    batchNo = batchNo + " | " + binStock.getBatchNo();
                }

                System.out.println("batchNo ===== " + batchNo);

                Double avgCost = 0.0;
                Double cost = 0.0;

                if (binStock.getAvgCost() != null) {
                    avgCost = binStock.getAvgCost();
                }

                Double stkQty = binStock.getStkQty();
                Double bStkQty = binStock.getStkQty();
                System.out.println(" stkQty ==== " + stkQty);
                System.out.println(" getStkQty ==== " + getStkQty);
                remaining = getStkQty - stkQty;
                System.out.println(" remaining ==== " + remaining);
                if (remaining.toString().contains("-")) {

                    cost = avgCost * getStkQty;
                    finalCost = finalCost + cost;
                    Double finalRemaining = stkQty - getStkQty;

                    binStock.setStkQty(Double.valueOf(decfor.format(finalRemaining)));
                    binStock.setAdate(dateUtil.getISTDate());
                    BinStock rs = binStockRepository.save(binStock);

                    binStockUtil.updateJournalEntryStockAuditLog(journalEntry, getStkQty, rs, loggedUser);
                    binStockUtil.updateReduceBinLogProduction(journalEntry, rs, getStkQty, loggedUser, "PRODUCTION", "I");

                    break;

                } else {

                    cost = avgCost * bStkQty;
                    finalCost = finalCost + cost;

                    binStock.setStkQty(0.00);
                    binStock.setAdate(dateUtil.getISTDate());
                    BinStock rs = binStockRepository.save(binStock);

                    binStockUtil.updateJournalEntryStockAuditLog(journalEntry, bStkQty, rs, loggedUser);
                    binStockUtil.updateReduceBinLogProduction(journalEntry, rs, bStkQty, loggedUser, "PRODUCTION", "I");

                    getStkQty = remaining;

                    int round = Math.round(remaining.intValue());

                    if (round == 0) {
                        break;
                    }

                }
            }

            finalAvgCost = finalCost / validQty;
            journalEntry.setCost(finalCost);

            journalEntry.setReduce_batchno(batchNo);

            //update final
            if (finalQty.getTotalCost() != null) {
                Double finalQtyCost = finalQty.getTotalCost() + finalCost;
                finalQty.setTotalCost(finalQtyCost);
            } else {
                finalQty.setTotalCost(finalCost);
            }
            if (finalQty.getTotalQuantity() != null) {
                Double finalTotalQty = finalQty.getTotalCost() + validQty;
                finalQty.setTotalCost(finalTotalQty);
            } else {
                finalQty.setTotalQuantity(validQty);
            }

        }

    }



    public void updateWasteReduceBinStock(JournalEntry journalEntry,
                                         MathDto finalQty,
                                         User loggedUser,
                                         HashMap<String, Double> stringDoubleHashMap,
                                         ProductionEntryClosingDataDto productionEntryClosingDataDto) {

        List<BinStock>  binStocks = binStockRepository.findByBinStocks(journalEntry.getProid(), journalEntry.getLot());

        if (binStocks != null && !binStocks.isEmpty()) {

            Double validQty = journalEntry.getStkqty();
            Double getStkQty = journalEntry.getStkqty();
            Double remaining = Double.valueOf(0);
            Double finalAvgCost = Double.valueOf(0);
            Double finalCost = Double.valueOf(0);
            System.out.println(" validQty ==== " + validQty);
            String batchNo = null;

            for (BinStock binStock : binStocks) {

                stringDoubleHashMap.put(binStock.getBatchNo(), binStock.getOilFFA());

                if (batchNo == null) {
                    batchNo = binStock.getBatchNo();
                } else {
                    batchNo = batchNo + " | " + binStock.getBatchNo();
                }

                System.out.println("batchNo ===== " + batchNo);

                Double avgCost = 0.0;
                Double cost = 0.0;

                if (binStock.getAvgCost() != null) {
                    avgCost = binStock.getAvgCost();
                }

                Double stkQty = binStock.getStkQty();
                Double bStkQty = binStock.getStkQty();
                System.out.println(" stkQty ==== " + stkQty);
                System.out.println(" getStkQty ==== " + getStkQty);
                remaining = getStkQty - stkQty;
                System.out.println(" remaining ==== " + remaining);
                if (remaining.toString().contains("-")) {

                    cost = avgCost * getStkQty;
                    finalCost = finalCost + cost;
                    Double finalRemaining = stkQty - getStkQty;

                    binStock.setStkQty(Double.valueOf(decfor.format(finalRemaining)));
                    binStock.setAdate(dateUtil.getISTDate());
                    BinStock rs = binStockRepository.save(binStock);

                    binStockUtil.updateJournalEntryStockAuditLog(journalEntry, getStkQty, rs, loggedUser);
                    binStockUtil.updateReduceBinLogProduction(journalEntry, rs, getStkQty, loggedUser, "PRODUCTION", "I");

                    break;

                } else {

                    cost = avgCost * bStkQty;
                    finalCost = finalCost + cost;

                    binStock.setStkQty(0.00);
                    binStock.setAdate(dateUtil.getISTDate());
                    BinStock rs = binStockRepository.save(binStock);

                    binStockUtil.updateJournalEntryStockAuditLog(journalEntry, bStkQty, rs, loggedUser);
                    binStockUtil.updateReduceBinLogProduction(journalEntry, rs, bStkQty, loggedUser, "PRODUCTION", "I");

                    getStkQty = remaining;

                    int round = Math.round(remaining.intValue());

                    if (round == 0) {
                        break;
                    }

                }
            }

            finalAvgCost = finalCost / validQty;
            journalEntry.setCost(finalCost);

            journalEntry.setReduce_batchno(batchNo);

            //update final
            if (finalQty.getTotalCost() != null) {
                Double finalQtyCost = finalQty.getTotalCost() + finalCost;
                finalQty.setTotalCost(finalQtyCost);
            } else {
                finalQty.setTotalCost(finalCost);
            }
            if (finalQty.getTotalQuantity() != null) {
                Double finalTotalQty = finalQty.getTotalCost() + validQty;
                finalQty.setTotalCost(finalTotalQty);
            } else {
                finalQty.setTotalQuantity(validQty);
            }

        }

    }
















    public void updateCostCreateBinStock(JournalEntry journalEntry, MathDto finalQty) {

        try {

            Double cost = finalQty.getTotalCost();
            Double avgcost = finalQty.getTotalCost() / journalEntry.getStkqty();

            journalEntry.setCost(cost);
            journalEntry.setAvgcost(avgcost);

        } catch (Exception e) {
            journalEntry.setCost(0.0);
            journalEntry.setAvgcost(0.0);
        }


    }

    public void updateCasAction(BlendingAdviceStockDetails result, User loggedUser) {

        //Find whether any config related to Blending Advice
        Optional<ActionConfiguration> getByTaskName = actionConfigurationRepository.findByTaskName("Blending Advice");
        if (getByTaskName.isPresent()) {
            System.out.println("action configuration found");
            ActionConfigurationDto rec = new ActionConfigurationDto(getByTaskName.get());
            // Actions table insertion
            if (result != null) {
                ActionDto dto = new ActionDto();
                dto.setActionConfigId(rec.getId());
                dto.setTaskDescription("Blending Advice CasGroup Activation");
                Optional<BlendingAdviceDetails> blendingAdviceDetails = blendingAdviceDetailsRepository.findById(result.getBlendingAdviceDetailsId());
                if (blendingAdviceDetails.isPresent()) {
                    dto.setUniqueRef(blendingAdviceDetails.get().getJournalId().toString());
                    // BlendingAdviceWeekId # journalId # BlendingAdviceDetailId # casgroup
                    String refTypeDescription = blendingAdviceDetails.get().getBlendingAdviceId() + "#" + blendingAdviceDetails.get().getJournalId() + "#" + blendingAdviceDetails.get().getId() + "#" + result.getCasGroup();
                    System.out.println("refTypeDescription  = " + refTypeDescription);
                    dto.setUniqueRefDescription(refTypeDescription);
                }

                dto.setOwnerId(loggedUser.getId());
                dto.setRefType("Blending Advice Journal Id");
                dto.setStatus("Completed");
                //qty - total no of bag per cas group
                dto.setQuantity(result.getNoOfBags().longValue());
                dto.setBranchId(loggedUser.getBranch_id());
                dto.setDeptID(loggedUser.getDepartment().getId());
                dto.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                dto.setCreatedBy(loggedUser.getId());
                Action model = new Action(dto);
                actionRepository.save(model);
            }
        }

    }

    public void saveWcShift(ProductionEntryFormDto productionEntry, User loggedUser, Long productId) {
        Optional<Wcshift> optionalWcshift = wcShiftsRepository.findBy(productionEntry.getJournalId(), productionEntry.getShiftId(), productId, productionEntry.getDept(), productionEntry.getBranch(), dateUtil.getStringTODateWk(productionEntry.getShiftDate()), productionEntry.getBatchNo());
        if (!optionalWcshift.isPresent()) {
            Wcshift wcshift = new Wcshift();
            wcshift.setProId(productId);
            wcshift.setJournalId(productionEntry.getJournalId());
            wcshift.setWcId(productionEntry.getMmcId());
            wcshift.setWcTable(productionEntry.getMmcTable());
            wcshift.setDept(productionEntry.getDept());
            wcshift.setBranch(productionEntry.getBranch());
            wcshift.setShiftDate(dateUtil.getStringTODateWk(productionEntry.getShiftDate()));
            Optional<PShifts> pShifts = pShiftsRepository.findById(productionEntry.getShiftId());
            wcshift.setPShift(pShifts.get().getName());
            wcshift.setPShiftId(pShifts.get().getId());
            wcshift.setBatchId(productionEntry.getBatchId());
            wcshift.setBatchNo(productionEntry.getBatchNo());
            wcshift.setBby(loggedUser.getId());
            wcshift.setBon(loggedUser.getEmployeename());
            wcshift.setBStatus("Y");
            wcshift.setEStatus("N");
            wcshift.setBegin(pShifts.get().getInTime().toString());
            wcshift.setEEnd(pShifts.get().getOutTime().toString());
            wcshift.setShrs(pShifts.get().getHours().toString());
            wcshift.setStrbegin(pShifts.get().getInTime().toString());
            wcshift.setStrend(pShifts.get().getOutTime().toString());
            wcshift.setTrnNo(productionEntry.getTrnNo());
            wcShiftsRepository.save(wcshift);
        }
    }


    public void updateWcShift(ProductionEntryFormDto productionEntry, User loggedUser, Long productId) {

        Optional<Wcshift> optionalWcshift = wcShiftsRepository.findBy(productionEntry.getJournalId(), productionEntry.getShiftId(), productId, productionEntry.getDept(), productionEntry.getBranch(), dateUtil.getStringTODateWk(productionEntry.getShiftDate()), productionEntry.getBatchNo());
        if (optionalWcshift.isPresent()) {
            Wcshift wcshift = optionalWcshift.get();
            wcshift.setEby(loggedUser.getId());
            wcshift.setEon(loggedUser.getEmployeename());
            wcshift.setJEntryBy(loggedUser.getId());
            wcshift.setJEntry(loggedUser.getEmployeename());
            wcshift.setJEntryOn(LocalDateTime.now(ZoneId.of("Asia/Kolkata")).toString());
            wcshift.setEStatus("Y");
            wcShiftsRepository.save(wcshift);
        }
    }


    public ResponseEntity<?> getJournalEntryView(Long journalId, String shiftDate, String batchNo,
                                                 String trnNo, Long shiftId) throws UsernameNotFoundException {

        Date shiftDateC = null;

        if (shiftDate != null) {
            shiftDateC = dateUtil.getStringTODateWk(shiftDate);
        }

        JournalEntryView journalEntryView = journalEntryRepository.getJournalEntryView(journalId, shiftDateC, batchNo, trnNo, shiftId);

        if (journalEntryView != null) {
            JournalEntryViewDto result = new JournalEntryViewDto(journalEntryView);
            if (result.getAby() != null) {
                Optional<User> optionalUser = userRepository.findById(result.getAby());
                if (optionalUser.isPresent()) {
                    result.setAccountingByName(optionalUser.get().getEmployeename());
                }
            }

            if (result.getEby() != null) {
                Optional<User> optionalUser = userRepository.findById(result.getEby());
                if (optionalUser.isPresent()) {
                    result.setEntryByName(optionalUser.get().getEmployeename());
                }
            }

            if (result.getWcid() != null) {

                if (result.getWcTable() != null && result.getWcTable().equalsIgnoreCase("machine")) {
                    Optional<Machine> optionalMachine = machineRepository.findById(result.getWcid());
                    if (optionalMachine.isPresent()) {
                        result.setWorkCenterName(optionalMachine.get().getName());
                    }
                } else {
                    Optional<DwCluster> optionalMachine = dwClusterRepository.findById(result.getWcid());
                    if (optionalMachine.isPresent()) {
                        result.setWorkCenterName(optionalMachine.get().getName());
                    }
                }
            }

            if (result.getGodownId() != null) {
                Optional<GoDown> optional = goDownRepository.findById(result.getGodownId());
                if (optional.isPresent()) {
                    result.setGodownName(optional.get().getName());
                }
            }

            updateJournalEntryView(result, journalId, shiftDateC, batchNo, trnNo, shiftId);
            return new ResponseEntity<JournalEntryViewDto>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<JournalEntryViewDto>(HttpStatus.NOT_FOUND);
        }
    }

    public void updateJournalEntryView(JournalEntryViewDto journalEntryViewDto, Long journalId, Date shiftDate, String batchNo,
                                       String trnNo, Long shiftId) {


        List<JournalEntryDto> outputProducts = new ArrayList<>();

        List<JournalEntry> list = journalEntryRepository.getJournalEntryViewByeList(journalEntryViewDto.getJournalId(), shiftDate, batchNo, journalEntryViewDto.getTrnNo(), shiftId);
        if (!list.isEmpty()) {

            List<JournalEntryDto> result = list.stream().map(data -> {
                JournalEntryDto j = new JournalEntryDto(data);
                if (j.getProid() != null) {

                    Optional<ProductMaster> optional = productMasterRepository.findById(j.getProid());
                    if (optional.isPresent()) {
                        j.setProductName(optional.get().getProductName());
                        if (j.getLot() == 1) {
                            j.setUnitName(optional.get().getUnit());
                        } else {
                            j.setUnitName(optional.get().getAltunit());
                        }
                    }

                    /*Optional<JournalProductMapping> journalProductMappingOptional = journalProductMappingRepository.findByJournalProductMapping(journalId, j.getProid(), j.getType());
                    if (journalProductMappingOptional.isPresent()) {
                        j.setBomTakeType(journalProductMappingOptional.get().getBomTakeType());
                    }*/

                    Optional<GoDown> goDownOptional = goDownRepository.findByGoName(j.getBinname());
                    if (goDownOptional.isPresent()) {
                        j.setGodownName(goDownOptional.get().getName());
                        j.setGodownId(goDownOptional.get().getId());
                    }

                }
                return j;
            }).collect(Collectors.toList());

            outputProducts.addAll(result);
        }

        List<JournalEntryDataList> bomlist = journalEntryRepository.getJournalEntryViewBomList(journalEntryViewDto.getJournalId(), shiftDate, batchNo, journalEntryViewDto.getTrnNo(), shiftId);
        if (!bomlist.isEmpty()) {

            for (JournalEntryDataList data : bomlist) {

                List<BinStockLogList> logList = binStockLogRepository.getBinStockLogList(data.getSjid(), data.getProid(), "I", data.getTrnno());

                if (!logList.isEmpty()) {

                    for (BinStockLogList binStockLog : logList) {

                        JournalEntryDto j = new JournalEntryDto(data);

                        j.setProductName(binStockLog.getProductName());
                        Optional<ProductMaster> optional = productMasterRepository.findById(j.getProid());
                        if (optional.isPresent()) {
                            if (j.getLot() == 1) {
                                j.setUnitName(optional.get().getUnit());
                            } else {
                                j.setUnitName(optional.get().getAltunit());
                            }
                        }

                       /* if (j.getProid() != null) {
                            Optional<JournalProductMapping> journalProductMappingOptional = journalProductMappingRepository.findByJournalProductMapping(journalId, j.getProid(), j.getType());
                            if (journalProductMappingOptional.isPresent()) {
                                j.setBomTakeType(journalProductMappingOptional.get().getBomTakeType());
                            }
                        }*/

                        // stbin details
                        j.setGodownName(binStockLog.getGodownName());
                        j.setGodownId(binStockLog.getGodownId());
                        j.setBatchid(binStockLog.getBatchid());
                        j.setBatchno(binStockLog.getBatchno());
                        j.setBinid(binStockLog.getBinid());
                        j.setBinname(binStockLog.getBinname());
                        j.setStkqty(binStockLog.getStkqty());
                        j.setCost(binStockLog.getCost());
                        j.setAvgcost(binStockLog.getAvgcost());
                        j.setGodown(binStockLog.getGodownId());
                        j.setLot(binStockLog.getLot());


                        outputProducts.add(j);
                    }


                }

            }
        }

        journalEntryViewDto.setOutputProducts(outputProducts);
    }


    public ResponseEntity<?> getProductionEntrySlapDetails(ProductionEntryFormDto entryFormDto, User loggedUser) throws
            UsernameNotFoundException {


        Date shiftDateC = null;

        if (entryFormDto.getShiftDate() != null) {
            shiftDateC = dateUtil.getStringTODateWk(entryFormDto.getShiftDate());
        }

        List<ProductionEntry> list = null;
        if (entryFormDto.getDcType().equalsIgnoreCase("dcbased")) {
            list = productionEntryRepository.findBy(entryFormDto.getJournalId(), entryFormDto.getShiftId(), shiftDateC, entryFormDto.getBatchNo(), entryFormDto.getDcType(), "bsLab");
        } else {
            list = productionEntryRepository.findBy(entryFormDto.getJournalId(), entryFormDto.getShiftId(), shiftDateC, entryFormDto.getBatchNo(), entryFormDto.getDcType(), "tsLab");
        }

        if (list != null) {
            List<ProductionEntryDto> result = list.stream().map(data -> new ProductionEntryDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<ProductionEntryDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(HttpStatus.NOT_FOUND);
        }
    }

    public BatchMaster getBatchNo(ProductionEntryFormDto entryDto, Long productId, User user) {

        String startPre = "Prdn";
        String dept = null;
        String branch = "P" + user.getBranch_id();
        String orgName = "VVV";
        if (user.getOrg_id() == 1) {
            orgName = "VVV";
        } else {
            orgName = "VRA";
        }
        if (user.getDepartment() != null) {
            dept = user.getDepartment().getTrnprefix();
        }

        String preFix = orgName + "/" + branch + "/" + dept;
        String batchNo = null;
        Optional<TRNIndex> optional = trnIndexRepo.findByPrefix(preFix);
        if (optional.isPresent()) {
            TRNIndex trnIndex = optional.get();
            Long lastNo = trnIndex.getLastno();
            batchNo = startPre + "-" + preFix + "-" + lastNo + "-" + productId + "-" + entryDto.getShiftId() + "-" + entryDto.getMmcId();
            trnIndex.setLastno(lastNo + 1);
            trnIndexRepo.save(trnIndex);
        } else {
            TRNIndex trnIndexVal = findPreFix(preFix, user);
            batchNo = startPre + "-" + preFix + "-" + trnIndexVal.getLastno() + "-" + productId + "-" + entryDto.getShiftId() + "-" + entryDto.getMmcId();
        }

        Optional<BatchMaster> optionalBatchMaster = batchMasterRepository.findByBatchno(batchNo);
        if (!optionalBatchMaster.isPresent()) {
            BatchMaster batchMaster = new BatchMaster();
            batchMaster.setBatchno(batchNo);
            batchMaster.setDept(user.getDepartment().getId());
            batchMaster.setBranch(user.getBranch_id());
            batchMaster.setProductId(productId);
            batchMaster.setShiftdt(dateUtil.getStringTODateWk(entryDto.getShiftDate()));
            batchMaster.setShiftid(entryDto.getShiftId());
            batchMaster.setCreatedby(user.getId());
            batchMaster.setBtype("PRODUCTION");
            BatchMaster result = batchMasterRepository.save(batchMaster);
            return result;
        } else {
            return optionalBatchMaster.get();
        }
    }

    public BatchMaster findBatch(String batchNo) {
        Optional<BatchMaster> optionalBatchMaster = batchMasterRepository.findByBatchno(batchNo);
        if (!optionalBatchMaster.isPresent()) {
            return null;
        } else {
            return optionalBatchMaster.get();
        }
    }

   /* public BatchMaster findBatch(Long batchId, String batchNo) {
        Optional<BatchMaster> optionalBatchMaster = batchMasterRepository.findByBatchno(batchNo);
        if (!optionalBatchMaster.isPresent()) {
            return null;
        } else {
            return optionalBatchMaster.get();
        }
    }*/


    public BatchMaster findBatch(ProductionEntryFormDto entryFormDto, Long productId, User user) {
        String batchNo = null;
        if (entryFormDto.getBatchNo() == null) {
            String startPre = "Prdn";
            String dept = null;
            String branch = "P" + user.getBranch_id();
            /*Optional<Organisation> organisation = organisationRepository.findById(user.getOrg_id());
            if (organisation.isPresent()) {
                orgName = organisation.get().getName();
            }*/
            String orgName = "VVV";
            if (user.getOrg_id() == 1) {
                orgName = "VVV";
            } else {
                orgName = "VRA";
            }
            if (user.getDepartment() != null) {
                dept = user.getDepartment().getTrnprefix();
            }

            String preFix = orgName + "/" + branch + "/" + dept;

            Optional<TRNIndex> trnIndex = trnIndexRepo.findByPrefix(preFix);

            batchNo = startPre + "-" + preFix + "-" + trnIndex.get().getLastno() + "-" + productId + "-" + entryFormDto.getShiftId() + "-" + entryFormDto.getMmcId();
            /*String batchNo = "Prdn-"+preFix+"-"+entryDto.getShiftId()+"-"+entryDto.getMmcId()+"-"+entryDto.get;*/

        } else {
            batchNo = entryFormDto.getBatchNo();
        }


        Optional<BatchMaster> optionalBatchMaster = batchMasterRepository.findByBatchno(batchNo);
        if (!optionalBatchMaster.isPresent()) {
            return null;
        } else {
            return optionalBatchMaster.get();
        }
    }


    public TRNIndex findPreFix(String preFix, User user) {
        TRNIndex trnIndex = new TRNIndex();
        trnIndex.setBatchno(Long.valueOf(1));
        trnIndex.setLastno(Long.valueOf(1));
        trnIndex.setPrefix(preFix);
        TRNIndex rs = trnIndexRepo.save(trnIndex);
        return rs;
    }


    public ResponseEntity<?> checkProductionEntry(ProductionEntryFormDto productionEntry, User loggedUser) throws
            UsernameNotFoundException {

        Long productId = null;
        Optional<Journal> journalOptional = journalRepository.findById(productionEntry.getJournalId());
        if (journalOptional.isPresent()) {
            productId = journalOptional.get().getPrimaryProductId();
        }
        Date date = null;
        if (productionEntry.getShiftDate() != null) {
            date = dateUtil.getStringTODateWk(productionEntry.getShiftDate());
        }
        Optional<Wcshift> optionalWcShift = wcShiftsRepository.findBy(productionEntry.getJournalId(), productionEntry.getShiftId(), productId, productionEntry.getDept(), loggedUser.getBranch_id(), date, productionEntry.getBatchNo());
        if (optionalWcShift.isPresent()) {
            return new ResponseEntity<String>("AlReady Exists!!!", HttpStatus.OK);
        } else {
            return new ResponseEntity<String>("Not updated!!!", HttpStatus.OK);
        }

    }


    public ResponseEntity<?> getSjtQty(Long journalId) throws UsernameNotFoundException {

        Optional<SjtQty> sjtQty = sjtQtyRepo.findBySjId(journalId);
        if (sjtQty.isPresent()) {
            return new ResponseEntity<SjtQtyDto>(new SjtQtyDto(sjtQty.get()), HttpStatus.OK);
        } else {
            return new ResponseEntity<SjtQtyDto>(HttpStatus.NOT_FOUND);
        }

    }


    public ResponseEntity<?> getProductionEntrySlapWiseDetails(ProductionEntryFormDto entryFormDto, User loggedUser) throws
            UsernameNotFoundException {

        Date shiftDateC = null;

        if (entryFormDto.getShiftDate() != null) {
            shiftDateC = dateUtil.getStringTODateWk(entryFormDto.getShiftDate());
        }

        List<ProductionEntryGrouping> list = productionEntryRepository.findBy(entryFormDto.getJournalId(), entryFormDto.getShiftId(), shiftDateC, entryFormDto.getBatchNo());

        if (list != null) {
            List<ProductionEntryGroupingDto> result = list.stream().map(data -> {
                ProductionEntryGroupingDto productionEntryGrouping = new ProductionEntryGroupingDto(data);
                List<ProductionEntryGroupingList> groupingList = productionEntryRepository.findBy(productionEntryGrouping.getJournalId(), productionEntryGrouping.getBatchId(), productionEntryGrouping.getBatchNo(), productionEntryGrouping.getTsId(), productionEntryGrouping.getMachineId(), productionEntryGrouping.getAuxMachineId(), productionEntryGrouping.getShiftId(), productionEntryGrouping.getShiftDate());
                List<ProductionEntryGroupingListDto> resultGroupingListDto = groupingList.stream().map(datares -> new ProductionEntryGroupingListDto(datares)).collect(Collectors.toList());
                productionEntryGrouping.setSlabDetails(resultGroupingListDto);
                return productionEntryGrouping;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<ProductionEntryGroupingDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ProductionEntryGroupingDto>>(HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity<?> getProductionEntryMachineDetails(ProductionEntryFormDto entryFormDto, User loggedUser) throws
            UsernameNotFoundException {

        Date shiftDateC = null;

        if (entryFormDto.getShiftDate() != null) {
            shiftDateC = dateUtil.getStringTODateWk(entryFormDto.getShiftDate());
        }

        List<ProductionEntryMachineGrouping> list = productionEntryRepository.findByMachineDetails(entryFormDto.getJournalId(), entryFormDto.getShiftId(), entryFormDto.getBatchNo());

        if (list != null) {
            List<ProductionEntryMachineGroupingDto> result = list.stream().map(data -> new ProductionEntryMachineGroupingDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<ProductionEntryMachineGroupingDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ProductionEntryMachineGroupingDto>>(HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity<?> getProductionBatchList(Long journalId, Long branchId, String inwardDate, User loggedUser) throws UsernameNotFoundException {

        Long productId = null;
        Optional<Journal> journalOptional = journalRepository.findById(journalId);
        if (journalOptional.isPresent()) {
            if (journalOptional.get().getBatchProxId() != null) {
                productId = journalOptional.get().getBatchProxId();
            } else {
                return new ResponseEntity<List<ProductionBatchListDto>>(HttpStatus.NOT_FOUND);
            }
        }
        Date date = null;
        if (inwardDate != null) {
            date = dateUtil.getStringTODateWk(inwardDate);
        }

        if (productId != null) {

            List<ProductionBatchList> list = binStockRepository.findBy(productId, branchId, date);

            if (list != null) {
                List<ProductionBatchListDto> result = list.stream().map(data -> new ProductionBatchListDto(data)).collect(Collectors.toList());
                return new ResponseEntity<List<ProductionBatchListDto>>(result, HttpStatus.OK);
            } else {
                return new ResponseEntity<List<ProductionBatchListDto>>(HttpStatus.NOT_FOUND);
            }

        } else {
            return new ResponseEntity<List<ProductionBatchListDto>>(HttpStatus.NOT_FOUND);
        }
    }


    public void updateEnergyMeter(ProductionEntry peResult) {
        Optional<MachinePanelReadingMapping> optional = panelReadingMappingRepository.findByReading(peResult.getMmcId(), peResult.getColid());
        if (optional.isPresent()) {

            MachinePanelReadingMapping rs = optional.get();
            MachinePanelReadingMapping machinePanelReadingMapping = optional.get();
            machinePanelReadingMapping.setMeterOpenReading(rs.getMeterClosingReading());
            // Long sumValue = Long.valueOf(rs.getMeterClosingReading()) + Long.valueOf(peResult.getDataResValues());
            machinePanelReadingMapping.setMeterClosingReading(peResult.getDataResValues());
            panelReadingMappingRepository.save(machinePanelReadingMapping);
        }

    }


    public ResponseEntity<?> deleteProductionEntrySlapDetails(ProductionEntryFormDto entryFormDto, User loggedUser) throws UsernameNotFoundException {


        Date shiftDateC = null;

        if (entryFormDto.getShiftDate() != null) {
            shiftDateC = dateUtil.getStringTODateWk(entryFormDto.getShiftDate());
        }

        List<ProductionEntry> list = null;
        list = productionEntryRepository.deleteProductionEntry(entryFormDto.getJournalId(), entryFormDto.getShiftId(), shiftDateC, entryFormDto.getBatchNo(), entryFormDto.getTrnNo());

        if (list != null) {
            productionEntryRepository.deleteAll(list);
            Optional<Wcshift> optionalWcshift = wcShiftsRepository.findByTrnNo(entryFormDto.getTrnNo());
            if (optionalWcshift.isPresent()) {
                wcShiftsRepository.delete(optionalWcshift.get());
            }
            return new ResponseEntity<Status>(new Status("200", "Production entry " + entryFormDto.getTsName() + " slap deleted successfully!!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "Production entry " + entryFormDto.getTsName() + " couldn't deleted!!!!"), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> revertJournalEntry(ProductionEntryFormDto entryFormDto, User loggedUser) throws UsernameNotFoundException {

        Date shiftDateC = null;

        if (entryFormDto.getShiftDate() != null) {
            shiftDateC = dateUtil.getStringTODateWk(entryFormDto.getShiftDate());
        }

        List<JournalEntry> journalEntryList = null;

        List<ProductionEntry> list = null;

        List<BinstockAuditLog> auditLogList = null;

        List<BlendingPreprocessRequestAndReceiveBinMap> binMapList = null;
        if (entryFormDto.getDcType().equalsIgnoreCase("dcbased")) {
            list = productionEntryRepository.deleteProductionEntrySlapDetails(entryFormDto.getJournalId(), entryFormDto.getShiftId(), shiftDateC, entryFormDto.getBatchNo(), "bsLab", entryFormDto.getTsName(), entryFormDto.getTsId(), entryFormDto.getTrnNo());
        } else {
            list = productionEntryRepository.deleteProductionEntrySlapDetails(entryFormDto.getJournalId(), entryFormDto.getShiftId(), shiftDateC, entryFormDto.getBatchNo(), "tsLab", entryFormDto.getTsName(), entryFormDto.getTsId(), entryFormDto.getTrnNo());
        }

        journalEntryList = journalEntryRepository.deleteJournalEntryList(entryFormDto.getJournalId(), entryFormDto.getBatchNo(), entryFormDto.getTrnNo());

        auditLogList = binstockAuditLogRepository.findByTrnNo(entryFormDto.getTrnNo());

        binMapList = blendingPreprocessRequestAndReceiveBinMapRepository.findBy(entryFormDto.getTrnNo(), entryFormDto.getBatchNo());


        if (journalEntryList != null) {

            binStockUtil.revertProduction(entryFormDto);

            productionEntryRepository.deleteAll(list);

            binstockAuditLogRepository.deleteAll(auditLogList);

            journalEntryRepository.deleteAll(journalEntryList);

            blendingPreprocessRequestAndReceiveBinMapRepository.deleteAll(binMapList);

            Optional<Wcshift> optionalWcshift = wcShiftsRepository.findByTrnNo(entryFormDto.getTrnNo());

            if (optionalWcshift.isPresent()) {
                Wcshift wcshift = optionalWcshift.get();
                wcshift.setEStatus("N");
                wcShiftsRepository.save(wcshift);
            }

            return new ResponseEntity<Status>(new Status("200", "Journal entry " + entryFormDto.getJournalId() + " was Revert successfully!!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "Journal entry " + entryFormDto.getJournalId() + " couldn't Reverted!!!!"), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> deleteJournalEntry(ProductionEntryFormDto entryFormDto, User loggedUser) throws UsernameNotFoundException {

        Date shiftDateC = null;

        if (entryFormDto.getShiftDate() != null) {
            shiftDateC = dateUtil.getStringTODateWk(entryFormDto.getShiftDate());
        }

        List<ProductionEntry> productionEntryList = null;

        List<JournalEntry> journalEntryList = null;

        List<BinstockAuditLog> auditLogList = null;

        List<BlendingPreprocessRequestAndReceiveBinMap> binMapList = null;

        productionEntryList = productionEntryRepository.deleteProductionEntry(entryFormDto.getJournalId(), entryFormDto.getShiftId(), shiftDateC, entryFormDto.getBatchNo(), entryFormDto.getTrnNo());

        journalEntryList = journalEntryRepository.deleteJournalEntryList(entryFormDto.getJournalId(), entryFormDto.getBatchNo(), entryFormDto.getTrnNo());

        Optional<Wcshift> optionalWcshift = wcShiftsRepository.findByTrnNo(entryFormDto.getTrnNo());

        auditLogList = binstockAuditLogRepository.findByTrnNo(entryFormDto.getTrnNo());

        binMapList = blendingPreprocessRequestAndReceiveBinMapRepository.findBy(entryFormDto.getTrnNo(), entryFormDto.getBatchNo());


        if (journalEntryList != null) {

            binStockUtil.revertProduction(entryFormDto);

            productionEntryRepository.deleteAll(productionEntryList);

            binstockAuditLogRepository.deleteAll(auditLogList);

            journalEntryRepository.deleteAll(journalEntryList);

            blendingPreprocessRequestAndReceiveBinMapRepository.deleteAll(binMapList);

            if (optionalWcshift.isPresent()) {
                wcShiftsRepository.delete(optionalWcshift.get());
            }

            return new ResponseEntity<Status>(new Status("200", "Journal entry " + entryFormDto.getJournalId() + " was deleted successfully!!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "Journal entry " + entryFormDto.getJournalId() + " couldn't deleted!!!!"), HttpStatus.OK);
        }
    }


    public void updatePreProcessBinMap(List<Long> journalIds, List<String> binNoList) {


        if (!binNoList.isEmpty()) {

            for (String binNoWithBatch : binNoList) {

                List<String> list = new ArrayList<>(Arrays.asList(binNoWithBatch.split("\\$")));

                String binNo = list.get(0);
                String batchNo = list.get(1);
                String lot = list.get(2);

                if (!journalIds.isEmpty()) {
                    List<JournalEntry> journalEntryList = journalEntryRepository.getList(journalIds, batchNo);
                    if (!journalEntryList.isEmpty()) {
                        for (JournalEntry journalEntry : journalEntryList) {

                            BlendingPreprocessRequestAndReceiveBinMap requestAndReceiveBinMap = null;

                            Optional<BlendingPreprocessRequestAndReceiveBinMap> binMapOp = blendingPreprocessRequestAndReceiveBinMapRepository.findByPreProcessBin(journalEntry.getBid(), Long.valueOf(binNo), batchNo, journalEntry.getBatchid(), journalEntry.getProid(), Double.valueOf(lot));
                            if (binMapOp.isPresent()) {
                                requestAndReceiveBinMap = binMapOp.get();
                            } else {
                                Optional<BlendingPreprocessRequestAndReceiveBinMap> binMapOptional = blendingPreprocessRequestAndReceiveBinMapRepository.findByPreProcessBinWithStatus(journalEntry.getBid(), Long.valueOf(binNo), batchNo, journalEntry.getBatchid(), journalEntry.getProid(), Double.valueOf(lot));
                                if (binMapOptional.isPresent()) {
                                    requestAndReceiveBinMap = binMapOptional.get();
                                } else {
                                    Optional<BlendingPreprocessRequestAndReceiveBinMap> binMapOptionalbi = blendingPreprocessRequestAndReceiveBinMapRepository.findByPreProcessBinWithOutStatus(journalEntry.getBid(), Long.valueOf(binNo), batchNo, journalEntry.getBatchid(), journalEntry.getProid(), Double.valueOf(lot));
                                    if (binMapOptionalbi.isPresent()) {
                                        requestAndReceiveBinMap = binMapOptionalbi.get();
                                    } else {
                                        Optional<BlendingPreprocessRequestAndReceiveBinMap> binMapOptionalBatch = blendingPreprocessRequestAndReceiveBinMapRepository.findByPreProcessWithOutBin(journalEntry.getBid(), batchNo, journalEntry.getProid(), Double.valueOf(lot));
                                        if (binMapOptionalBatch.isPresent()) {
                                            requestAndReceiveBinMap = binMapOptionalBatch.get();
                                        } else {
                                            Optional<BlendingPreprocessRequestAndReceiveBinMap> binMapOptionalLot = blendingPreprocessRequestAndReceiveBinMapRepository.findByPreProcessWithOutLot(journalEntry.getBid(), batchNo, journalEntry.getProid());
                                            if (binMapOptionalLot.isPresent()) {
                                                requestAndReceiveBinMap = binMapOptionalLot.get();
                                            }
                                        }
                                    }
                                }
                            }

                            BlendingPreprocessRequestAndReceiveBinMap binMap = new BlendingPreprocessRequestAndReceiveBinMap();

                            if (requestAndReceiveBinMap != null) {
                                binMap.setLot(journalEntry.getLot());
                                binMap.setBlendingDetailId(requestAndReceiveBinMap.getBlendingDetailId());
                                binMap.setCasgroup(requestAndReceiveBinMap.getCasgroup());
                                binMap.setBatchId(requestAndReceiveBinMap.getBatchId());
                                binMap.setBatchNo(requestAndReceiveBinMap.getBatchNo());
                                binMap.setBlendingAdviceStockId(requestAndReceiveBinMap.getBlendingAdviceStockId());
                                binMap.setBranch(journalEntry.getBid());
                                binMap.setDept(journalEntry.getDid());
                                binMap.setItemId(journalEntry.getProid());
                                Optional<ProductMaster> productMasterOptional = productMasterRepository.findById(journalEntry.getProid());
                                if (productMasterOptional.isPresent()) {
                                    binMap.setItemName(productMasterOptional.get().getProductName());
                                }
                                binMap.setTrnNo(journalEntry.getTrnno());
                                binMap.setQty(journalEntry.getStkqty());
                                binMap.setBinNo(journalEntry.getBinname());
                                binMap.setStatus("updated");
                                blendingPreprocessRequestAndReceiveBinMapRepository.save(binMap);
                                //blendingPreprocessRequestAndReceiveBinMapRepository.delete(requestAndReceiveBinMap);
                            } else {
                                binMap.setLot(journalEntry.getLot());
                                binMap.setBranch(journalEntry.getBid());
                                binMap.setDept(journalEntry.getDid());
                                binMap.setItemId(journalEntry.getProid());
                                Optional<ProductMaster> productMasterOptional = productMasterRepository.findById(journalEntry.getProid());
                                if (productMasterOptional.isPresent()) {
                                    binMap.setItemName(productMasterOptional.get().getProductName());
                                }
                                binMap.setTrnNo(journalEntry.getTrnno());
                                binMap.setQty(journalEntry.getStkqty());
                                binMap.setBinNo(journalEntry.getBinname());
                                binMap.setBatchId(journalEntry.getBatchid());
                                binMap.setBatchNo(journalEntry.getBatchno());
                                binMap.setStatus("updated");
                                blendingPreprocessRequestAndReceiveBinMapRepository.save(binMap);
                            }
                        }
                    }
                }
            }
        }
    }


    public ResponseEntity<?> validateStockQty(ProductionEntryFormDto entryFormDto, User loggedUser) throws UsernameNotFoundException {

        Boolean blending = false;
        Optional<Journal> journalOptional = journalRepository.findById(entryFormDto.getJournalId());
        if (journalOptional.isPresent()) {
            if (journalOptional.get().getBlending() == true) {
                blending = true;
            }
        }

        List<ProductionEntryClosingDataDto> productionEntryClosingDataDtos = new ArrayList<>();

        Boolean status = true;
        if (!entryFormDto.getBomDataList().isEmpty()) {

            for (ProductionEntryClosingDataDto productionEntryClosingDataDto : entryFormDto.getBomDataList()) {

                List<BinStock> binStocks = null;

                Double lot = Double.valueOf(productionEntryClosingDataDto.getLot());
                Double stkQty = Double.valueOf(productionEntryClosingDataDto.getQuantity());
                Long blendingStockId = productionEntryClosingDataDto.getBlendingStockId();
                Long branch = entryFormDto.getBranch();
                Long bomProductId = productionEntryClosingDataDto.getProductId();
                Long binNo = Long.valueOf(productionEntryClosingDataDto.getBinNo());
                Long journalId = entryFormDto.getJournalId();
                String casgroup = productionEntryClosingDataDto.getCasgroup();
                String batchNo = entryFormDto.getBatchNo();
                String binId = null;


                Optional<GoGroup> groupOptional = goGroupRepository.findByBinNo(binNo);
                if (groupOptional.isPresent()) {
                    binId = groupOptional.get().getGCode();
                }

                if ((entryFormDto.getBatchNo() == null) || (entryFormDto.getBatchNo() != null && entryFormDto.getBatchNo().contains("Prdn"))) {

                    if (blending == true) {

                        if ((productionEntryClosingDataDto.getCasgroup() != null) && (productionEntryClosingDataDto.getBlendingStockId() != null)) {
                            System.out.println(" getCasgroup & blending stock id ");
                            if (productionEntryClosingDataDto.getBlendingStockId() != null) {
                                System.out.println("  blending stock id ");
                                binStocks = binStockRepository.findByBlendingStockId(blendingStockId, branch, bomProductId, binNo, lot, casgroup);
                                if (binStocks.isEmpty()) {
                                    binStocks = binStockRepository.findByBlendingStockId1(blendingStockId, branch, bomProductId, binNo, lot, casgroup);
                                }
                                System.out.println(" blending stock id == " + binStocks);
                            } else {
                                System.out.println(" getCasgroup  ");
                                binStocks = binStockRepository.findByBlendingCasgrp(casgroup, journalId, branch, bomProductId, binNo, lot);
                                if (binStocks.isEmpty()) {
                                    binStocks = binStockRepository.findByBlendingCasgrp1(casgroup, journalId, branch, bomProductId, binNo, lot);
                                }
                                System.out.println(" getCasgroup binsytock == " + binStocks);
                            }
                        } else {
                            System.out.println(" blending 1 == no cas group ");
                            binStocks = binStockRepository.findByBinStocks(bomProductId, binNo, binId, lot);
                            System.out.println(" blending 1 == " + binStocks);
                        }
                    } else {
                        System.out.println(" noooo blending  == ");
                        binStocks = binStockRepository.findByBinStocks(bomProductId, binNo, binId, lot);
                        System.out.println(" noooo blending  == " + binStocks);
                    }
                } else {
                    System.out.println(" batch not match  == ");
                    binStocks = binStockRepository.findByBinStockByBinNoANdBinId(bomProductId, binNo, binId, lot, batchNo);
                    System.out.println(" batch not match   == " + binStocks);
                }

                if (!binStocks.isEmpty()) {

                    Double qty = binStocks.stream().mapToDouble(data -> data.getStkQty()).sum();
                    System.out.println("qty == " + qty);
                    if (qty > stkQty) {
                        logger.info("In Stock Qty is Greater than Required Qty");
                        if (status) {
                            status = true;
                        }
                        productionEntryClosingDataDto.setStatus("Valid");
                        productionEntryClosingDataDto.setMessage("Success");
                        productionEntryClosingDataDtos.add(productionEntryClosingDataDto);
                    } else if (stkQty == qty) {
                        if (status) {
                            status = true;
                        }
                        productionEntryClosingDataDto.setStatus("Valid");
                        productionEntryClosingDataDto.setMessage("Success");
                        productionEntryClosingDataDtos.add(productionEntryClosingDataDto);
                    } else {
                        status = false;
                        productionEntryClosingDataDto.setStatus("NotValid");
                        productionEntryClosingDataDto.setMessage("Qty Issue");
                        productionEntryClosingDataDtos.add(productionEntryClosingDataDto);
                    }
                } else {
                    status = false;
                    productionEntryClosingDataDto.setStatus("NotValid");
                    productionEntryClosingDataDto.setMessage("Not IN Stock");
                    productionEntryClosingDataDtos.add(productionEntryClosingDataDto);
                }

            }
        }

        Map<String, Object> response = new HashMap<>();
        if (status) {
            response.put("status", 200);
            response.put("success", true);
            response.put("message", "Valid Bom Details");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("status", 200);
            response.put("success", false);
            response.put("message", "NotValid Bom Details");
            response.put("data", productionEntryClosingDataDtos);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

    }


}

