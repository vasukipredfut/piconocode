package com.aria.service;

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

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class BlendingAdviceService {

    private static final DecimalFormat decfor = new DecimalFormat("0.00");


    @Autowired
    BlendingAdviceRepository blendingAdviceRepository;

    @Autowired
    BlendingStockDetailsRepostiory blendingStockDetailsRepostiory;

    @Autowired
    BlendingAdviceBinDetailsRepository blendingAdviceBinDetailsRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    GoGroupRepository goGroupRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    BlendingAdviceDetailsRepository blendingAdviceDetailsRepository;

    @Autowired
    BlendingAdviceStockDetailsRepository blendingAdviceStockDetailsRepository;

    @Autowired
    BlendingAdvicePreProcessRepository blendingAdvicePreProcessRepository;

    @Autowired
    MaterialRequestRepository materialRequestRepository;

    @Autowired
    JournalRepository journalRepository;

    @Autowired
    RequestItemRepository requestItemRepository;

    @Autowired
    MaterialIssueRepository materialIssueRepository;

    @Autowired
    IssueItemsRepository issueItemsRepository;

    @Autowired
    ProductMasterRepository productMasterRepository;

    @Autowired
    ActionConfigurationRepository actionConfigurationRepository;

    @Autowired
    ActionRepository actionRepository;

    @Autowired
    BinStockRepository binStockRepository;

    @Autowired
    BlendingSchedulerRepo blendingSchedulerRepo;

    @Autowired
    BlendingAdvicePreProcessBindetailsRepository blendingAdvicePreProcessBindetailsRepository;

    @Autowired
    ISORepository isoRepository;

    @Autowired
    TRNIndexRepo trnIndexRepo;

    @Autowired
    GoDownExternalRepository goDownExternalRepository;

    @Autowired
    RequestGodownItemRepository requestGodownItemRepository;

    @Autowired
    ExternalGodownBinDetailsRepository externalGodownBinDetailsRepository;

    @Autowired
    BlendingRequestAndPreprocessBinMapRepository blendingRequestAndPreprocessBinMapRepository;


    @Autowired
    BlendingPreprocessRequestAndReceiveBinMapRepository blendingPreprocessRequestAndReceiveBinMapRepository;


    @Autowired
    BinDetailsServices binDetailsServices;


    @Autowired
    BinLockRepository binLockRepository;

    // BlendingAdvice

    public ResponseEntity<?> checkBlending(Long journalId, User loggedUser) {
        Optional<BlendingAdvice> checkBlending = blendingAdviceRepository.checkBlending(journalId);
        if (checkBlending.isPresent()) {
            List<BlendingAdviceDetails> blendingAdviceDetails = blendingAdviceDetailsRepository.findBy(checkBlending.get().getId());
            if (blendingAdviceDetails.isEmpty()) {
                return new ResponseEntity<BlendingAdviceDto>(new BlendingAdviceDto(checkBlending.get()), HttpStatus.OK);
            } else {
                return new ResponseEntity<BlendingAdviceDto>(new BlendingAdviceDto(checkBlending.get()), HttpStatus.CREATED);
            }
        } else {
            return new ResponseEntity<BlendingAdviceDto>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> saveBlendingAdvice(BlendingAdviceDto blendingAdviceDto, User loggedUser) {
        BlendingAdvice blendingAdvice = new BlendingAdvice(blendingAdviceDto);
        blendingAdvice.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        blendingAdvice.setStatus("P");
        Optional<Journal> journal = journalRepository.findById(blendingAdviceDto.getJournalId());
        if (journal.isPresent()) {
            if (journal.get().getBatchProxId() != null) {
                blendingAdvice.setProductId(journal.get().getBatchProxId());
            } else {
                blendingAdvice.setProductId(0L);
            }
        }
        BlendingAdvice result = blendingAdviceRepository.save(blendingAdvice);
        return new ResponseEntity<BlendingAdviceDto>(new BlendingAdviceDto(result), HttpStatus.OK);
    }

    public ResponseEntity<?> updateBlendingAdvice(BlendingAdviceDto blendingAdviceDto, User loggedUser) {
        BlendingAdvice blendingAdvice = new BlendingAdvice(blendingAdviceDto);
        blendingAdvice.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        blendingAdviceRepository.save(blendingAdvice);
        return new ResponseEntity<Status>(new Status("200", "Blending Advice updated Successfully!!!"), HttpStatus.OK);
    }

    public void updateBlendingAdvice(Long id) {
        Optional<BlendingAdvice> checkBlending = blendingAdviceRepository.findById(id);
        if (checkBlending.isPresent()) {
            BlendingAdvice blendingAdvice = checkBlending.get();
            blendingAdvice.setBlending_detail_type("Created");
            blendingAdvice.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            blendingAdviceRepository.save(blendingAdvice);
        }
    }


    public ResponseEntity<?> deleteBlending(Long id) {
        Optional<BlendingAdvice> checkBlending = blendingAdviceRepository.findById(id);
        if (checkBlending.isPresent()) {
            blendingAdviceRepository.delete(checkBlending.get());
            return new ResponseEntity<Status>(new Status("200", "deleted Successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<BlendingAdviceDto>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> getBlendingStockDetails(Long sjid, User loggedUser) {
        Map<String, Object> resultObjectMap = new LinkedHashMap<>();
        List<Branch> branchList = branchRepository.findBy();
        Optional<Journal> journal = journalRepository.findById(sjid);
        Map<String, Object> lockStockDetailsObjectMap = new LinkedHashMap<>();
        Map<String, Object> stockDetailsObjectMap = new LinkedHashMap<>();
        Map<String, Object> categoryStockDetailsObjectMap = new LinkedHashMap<>();
        Long productId = 0L;
        if (journal.isPresent()) {
            productId = journal.get().getBatchProxId();
        }

        List<BranchStockSum> branchStockSums = blendingStockDetailsRepostiory.findByOverAllQty(productId);
        List<BlendingStock> blendingStocks = blendingStockDetailsRepostiory.findByLOCK(productId);
        List<BlendingStock> stateWiseStocks = blendingStockDetailsRepostiory.findByStateWiseLOCK(productId);
        List<BranchStockSum> oilffa_15_20 = blendingStockDetailsRepostiory.findByOilFFQty(productId, "Oil_FFA_1.5_to_2.0");
        List<BranchStockSum> oilffa_20 = blendingStockDetailsRepostiory.findByOilFFQty(productId, "Oil_FFA_2.0");
        List<BranchStockSum> wRCount = blendingStockDetailsRepostiory.findByLockTypeCount(productId, "WR");
        List<BranchStockSum> qICount = blendingStockDetailsRepostiory.findByLockTypeCount(productId, "QI");

        for (Branch branch : branchList) {
            Map<String, Object> lockStockMap = new HashMap<>();
            Long overall = branchStockSums.stream().filter(val -> val.getBranchId() == branch.getId()).mapToLong(BranchStockSum::getQtySum).sum();
            lockStockMap.put("OverALL", overall);
            for (BlendingStock blendingStock : blendingStocks) {
                if (blendingStock.getBranch().equals(branch.getId())) {
                    lockStockMap.put(blendingStock.getLockType(), blendingStock.getQty());
                }
            }
            Long sumOFWRCount = wRCount.stream().filter(val -> val.getBranchId() == branch.getId()).mapToLong(BranchStockSum::getSumOfCount).sum();
            lockStockMap.put("WRCOUNT", sumOFWRCount);
            Long sumOFQICount = qICount.stream().filter(val -> val.getBranchId() == branch.getId()).mapToLong(BranchStockSum::getSumOfCount).sum();
            lockStockMap.put("QICOUNT", sumOFQICount);

            lockStockDetailsObjectMap.put(branch.getBranchName(), lockStockMap);

            Map<String, Object> stockDetailsStateMap = new HashMap<>();
            for (BlendingStock blendingStock : stateWiseStocks) {
                if (blendingStock.getBranch().equals(branch.getId())) {
                    stockDetailsStateMap.put(blendingStock.getCategory(), blendingStock.getQty());
                }
            }

            stockDetailsObjectMap.put(branch.getBranchName(), stockDetailsStateMap);

            Map<String, Object> categoryStockStateMap = new HashMap<>();
            Long oilffa1520_long = oilffa_15_20.stream().filter(val -> val.getBranchId() == branch.getId()).mapToLong(BranchStockSum::getQtySum).sum();
            Long oilffa20_long = oilffa_20.stream().filter(val -> val.getBranchId() == branch.getId()).mapToLong(BranchStockSum::getQtySum).sum();

            categoryStockStateMap.put("Oil_FFA_1.5_to_2.0", oilffa1520_long);
            categoryStockStateMap.put("Oil_FFA_2.0", oilffa20_long);
            categoryStockDetailsObjectMap.put(branch.getBranchName(), categoryStockStateMap);

        }

        resultObjectMap.put("LockStockDetails", lockStockDetailsObjectMap);
        resultObjectMap.put("StockDetails", stockDetailsObjectMap);
        resultObjectMap.put("CategoryStockDetails", categoryStockDetailsObjectMap);

        return new ResponseEntity<Map<String, Object>>(resultObjectMap, HttpStatus.OK);
    }


    public ResponseEntity<?> blendingAllStockDetails(Long sjid, Long branch, String category, Double ffa, String status, Long blendingAdviceId, String lockType) throws UsernameNotFoundException {

        Optional<Journal> journal = journalRepository.findById(sjid);
        Long productId = 0L;
        if (journal.isPresent()) {
            productId = journal.get().getBatchProxId();
        }

        boolean previousCas;
        if (blendingAdviceId != null) {
            Optional<BlendingAdvice> optional = blendingAdviceRepository.findById(blendingAdviceId);
            if (optional.isPresent()) {
                if (optional.get().getPrevious_cas() != null && optional.get().getPrevious_cas().equalsIgnoreCase("Y")) {
                    previousCas = false;
                } else {
                    previousCas = true;
                }
            } else {
                previousCas = true;
            }
        } else {
            previousCas = true;
        }

        List<CasBinWiseStockList> tuples = binStockRepository.getBinWithAvgFFA(productId);
        Map<Long, Double> stringMap = tuples.stream().collect(Collectors.toMap(tuple -> tuple.getBinNo(), tuple -> tuple.getAvgFFA()));

        List<BinLockList> lockBinstoctList = binStockRepository.getBinLockList(productId);
        Map<Long, String> lockBinstoctLockMap = lockBinstoctList.stream().collect(Collectors.toMap(tuple -> tuple.getBinNo(), tuple -> tuple.getLock()));

        Map<Long, String> blendingLockMap;
        if (previousCas) {
            List<BinLockList> lockTuples = binStockRepository.getBlendingLockList(productId);
            blendingLockMap = lockTuples.stream().collect(Collectors.toMap(tuple -> tuple.getBinNo(), tuple -> {
                String val = "N";
                if (tuple.getBlendingLock()) {
                    val = "Y";
                }
                return val;
            }));
        } else {
            blendingLockMap = new HashMap<>();
        }


        List<BlendingStock> blendingStocks = blendingStockDetailsRepostiory.findBinStockDetails(productId, category, ffa, branch, lockType, status);

        if (!blendingStocks.isEmpty()) {

            List<BlendingStockDTO> result = blendingStocks.stream().map(data -> {

                BlendingStockDTO blendingStockDTO = new BlendingStockDTO(data);

                // xylo bins only convertion

               /* if (blendingStockDTO.getBinName() != null && (blendingStockDTO.getBinName() == 2323 || blendingStockDTO.getBinName() == 2324)) {
                    if (blendingStockDTO.getLot() == 1) {
                        Double qty = blendingStockDTO.getQty() / 74.0;
                        blendingStockDTO.setQty(qty);
                        blendingStockDTO.setLot(74.0);
                    }
                }*/

                if (blendingStockDTO.getLot() != null) {
                    blendingStockDTO.setMainlot(blendingStockDTO.getLot());
                    if (blendingStockDTO.getLot() == 1) {
                        Double qty = blendingStockDTO.getQty() / 74.0;
                        blendingStockDTO.setQty(qty);
                        blendingStockDTO.setLot(74.0);
                    }
                }

                Long binName = blendingStockDTO.getBinName();
                if (stringMap.containsKey(binName)) {
                    blendingStockDTO.setAvgFFA(decfor.format(stringMap.get(binName)));
                }

                if (previousCas) {

                    if (blendingLockMap.containsKey(binName)) {
                        String lock = blendingLockMap.get(binName);
                        if (lock.equalsIgnoreCase("Y")) {
                            blendingStockDTO.setBlendingLock(true);
                        } else {
                            blendingStockDTO.setBlendingLock(false);
                        }
                    } else {
                        blendingStockDTO.setBlendingLock(false);
                    }
                } else {

                    blendingStockDTO.setBlendingLock(false);
                }

                if (lockBinstoctLockMap.containsKey(binName)) {
                    String lock = lockBinstoctLockMap.get(binName);
                    if (lock.equalsIgnoreCase("Y")) {
                        blendingStockDTO.setLock(true);
                    } else {
                        blendingStockDTO.setLock(false);
                    }
                } else {
                    blendingStockDTO.setLock(false);
                }

                return blendingStockDTO;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<BlendingStockDTO>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BlendingStockDTO>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> printBlendingStockDetails(PrintBlendingStock printBlendingStock) throws UsernameNotFoundException {

        List<CasBinWiseStockList> tuples = binStockRepository.getBinWithAvgFFA(printBlendingStock.getProductId());
        Map<Long, Double> stringMap = tuples.stream().collect(Collectors.toMap(tuple -> tuple.getBinNo(), tuple -> tuple.getAvgFFA()));

        List<BlendingStock> blendingStocks = blendingStockDetailsRepostiory.findBinStockDetails(printBlendingStock.getProductId(), printBlendingStock.getBinList(), printBlendingStock.getBatchNoList());
        if (!blendingStocks.isEmpty()) {
            List<BlendingStockDTO> result = blendingStocks.stream().map(data -> {
                BlendingStockDTO blendingStockDTO = new BlendingStockDTO(data);
                Long binName = blendingStockDTO.getBinName();
                if (stringMap.containsKey(binName)) {
                    blendingStockDTO.setAvgFFA(decfor.format(stringMap.get(binName)));
                }
                return blendingStockDTO;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<BlendingStockDTO>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BlendingStockDTO>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> getCasBinWiseLotANDStock(Long blendingStockId, Long productId, Long branch, String casgroup) throws UsernameNotFoundException {


        List<CasBinWiseStockListDto> result = new ArrayList<>();

        String stockType = null;

        /*
        Optional<Journal> journal = journalRepository.findByBlendingStockId(blendingStockId);

        if (journal.isPresent() && journal.get().getStockType() != null) {
            stockType = journal.get().getStockType();
        }

        if (stockType != null && stockType.equalsIgnoreCase("Delite Stock")) {*//*
            List<CasBinWiseStockList> blendingStocks = blendingAdviceStockDetailsRepository.findBy(blendingStockId, productId, branch, "DT");
            if (!blendingStocks.isEmpty()) {
                result = blendingStocks.stream().map(data -> new CasBinWiseStockListDto(data, "BlendingBin")).collect(Collectors.toList());
            }
            result.addAll(preProcessOrReqList(blendingStockId, productId, branch, casgroup));
        } else {*/
            List<CasBinWiseStockList> blendingStocks = blendingAdviceStockDetailsRepository.findBy(blendingStockId, productId, branch);
            if (!blendingStocks.isEmpty()) {
                result = blendingStocks.stream().map(data -> new CasBinWiseStockListDto(data, "BlendingBin")).collect(Collectors.toList());
            }
            result.addAll(preProcessOrReqList(blendingStockId, productId, branch, casgroup));
        //}


        if (!result.isEmpty()) {
            return new ResponseEntity<List<CasBinWiseStockListDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<CasBinWiseStockListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public List<CasBinWiseStockListDto> preProcessOrReqList(Long blendingStockId, Long productId, Long branch, String casgroup) {

        Set<CasBinWiseStockListDto> list = new HashSet<>();

        List<CasBinWiseStockList> requestReceiveBin = blendingAdviceStockDetailsRepository.findByRequestReceiveBin(blendingStockId, productId, branch, casgroup);
        if (!requestReceiveBin.isEmpty()) {
            list.addAll(requestReceiveBin.stream().map(data -> new CasBinWiseStockListDto(data, "ExternalReceiveBin")).collect(Collectors.toList()));
        }

        List<CasBinWiseStockList> preprocessReceiveBin = blendingAdviceStockDetailsRepository.findByPreprocessReceiveBin(blendingStockId, productId, branch, casgroup);
        if (!preprocessReceiveBin.isEmpty()) {
            list.addAll(preprocessReceiveBin.stream().map(data -> new CasBinWiseStockListDto(data, "PreProcessBin")).collect(Collectors.toList()));
        }

        List<CasBinWiseStockListDto> casBinWiseStockListDtos = new ArrayList<>(list);

        return casBinWiseStockListDtos;
    }

    // BlendingAdviceDetails

    public ResponseEntity<?> saveBlendingAdviceDetails(List<BlendingAdviceDetailsDto> blendingAdviceDetailsDtoList, User loggedUser) {

        if (blendingAdviceDetailsDtoList != null && !blendingAdviceDetailsDtoList.isEmpty()) {

            Long id = null;
            for (BlendingAdviceDetailsDto blendingAdviceDto : blendingAdviceDetailsDtoList) {

                id = blendingAdviceDto.getBlendingAdviceId();

                BlendingAdviceDetails blendingAdviceDetails = new BlendingAdviceDetails(blendingAdviceDto);
                blendingAdviceDetails.setCasTransaction("CAS-" + blendingAdviceDto.getBlendingAdviceId());
                blendingAdviceDetails.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                blendingAdviceDetails.setStatus("P");
                Optional<Journal> journal = journalRepository.findById(blendingAdviceDto.getJournalId());
                if (journal.isPresent()) {
                    if (journal.get().getBatchProxId() != null) {
                        blendingAdviceDetails.setProductId(journal.get().getBatchProxId());
                        blendingAdviceDto.setProductId(journal.get().getBatchProxId());
                    } else {
                        blendingAdviceDetails.setProductId(0L);
                        blendingAdviceDto.setProductId(0L);
                    }
                }

                BlendingAdviceDetails result = blendingAdviceDetailsRepository.save(blendingAdviceDetails);

                //Find whether any config related to Bill Inward
                Optional<ActionConfiguration> getByTaskName = actionConfigurationRepository.findByTaskName("Blending Advice");
                if (getByTaskName.isPresent()) {

                    ActionConfigurationDto rec = new ActionConfigurationDto(getByTaskName.get());
                    // Actions table insertion
                    if (result != null) {
                        ActionDto dto = new ActionDto();
                        dto.setActionConfigId(rec.getId());
                        dto.setTaskDescription("Blending Advice Activation");
                        dto.setUniqueRef(result.getJournalId().toString());
                        dto.setOwnerId(loggedUser.getId());
                        dto.setRefType("Blending Advice Journal Id");
                        dto.setStatus("Pending");
                        //qty - total no of kalasal * no of bags per kalasal
                        int qty = 0;
                        if (!blendingAdviceDto.getStockDetails().isEmpty()) {
                            qty = (int) blendingAdviceDto.getStockDetails().stream().mapToDouble(e -> e.getNoOfBags()).sum();
                        }
                        dto.setQuantity((long) qty);
                        dto.setBranchId(blendingAdviceDetails.getBranchId());
                        dto.setDeptID(1L);
                        dto.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                        dto.setCreatedBy(loggedUser.getId());
                        Action model = new Action(dto);
                        actionRepository.save(model);
                    }
                }
                saveBlendingAdviceStockDetailsUpdate(result, blendingAdviceDto, loggedUser);
            }
            updateBlendingAdvice(id);
        }
        return new ResponseEntity<Status>(new Status("200", "Blending Advice Details saved Successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateBlendingAdviceDetails(BlendingAdviceDetailsDto blendingAdviceDto, User loggedUser) {
        BlendingAdviceDetails blendingAdviceDetails = new BlendingAdviceDetails(blendingAdviceDto);
        blendingAdviceDetails.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        blendingAdviceDetailsRepository.save(blendingAdviceDetails);
        return new ResponseEntity<Status>(new Status("200", "Blending Advice Details updated Successfully!!!"), HttpStatus.OK);
    }

    public void saveBlendingAdviceStockDetailsUpdate(BlendingAdviceDetails blendingAdviceDetails, BlendingAdviceDetailsDto blendingAdviceDto, User loggedUser) {

        if (!blendingAdviceDto.getStockDetails().isEmpty()) {
            for (BlendingAdviceStockDetailsDto blendingAdviceStockDetailsDto : blendingAdviceDto.getStockDetails()) {
                BlendingAdviceStockDetails blendingAdviceStockDetails = new BlendingAdviceStockDetails(blendingAdviceStockDetailsDto);
                blendingAdviceStockDetails.setBlendingAdviceDetailsId(blendingAdviceDetails.getId());
                blendingAdviceStockDetails.setProductId(blendingAdviceDetails.getProductId());
                blendingAdviceStockDetails.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                BlendingAdviceStockDetails result = blendingAdviceStockDetailsRepository.save(blendingAdviceStockDetails);

                if (!blendingAdviceStockDetailsDto.getBinDetails().isEmpty()) {
                    for (BlendingAdviceBinDetailsDto binDetailsDto : blendingAdviceStockDetailsDto.getBinDetails()) {

                        BlendingAdviceBinDetails binDetails = new BlendingAdviceBinDetails(binDetailsDto);
                        binDetails.setBlendingAdviceStockId(result.getId());
                        binDetails.setProductId(result.getProductId());
                        BlendingAdviceBinDetails res = blendingAdviceBinDetailsRepository.save(binDetails);
                        updateBlendingStockLock(res);

                    }
                }
            }
        }

        if (!blendingAdviceDto.getPreProcesses().isEmpty()) {

            for (BlendingAdvicePreProcessDto blendingAdvicePreProcessDto : blendingAdviceDto.getPreProcesses()) {

                BlendingAdvicePreProcess blendingAdvicePreProcess = new BlendingAdvicePreProcess(blendingAdvicePreProcessDto);
                blendingAdvicePreProcess.setBlendingAdviceDetailsId(blendingAdviceDetails.getId());
                Optional<BlendingAdviceStockDetails> stockDetails = blendingAdviceStockDetailsRepository.findBy(blendingAdviceDetails.getId(), blendingAdvicePreProcessDto.getCasGroup());
                if (stockDetails.isPresent()) {
                    blendingAdvicePreProcess.setBlendingAdviceStockId(stockDetails.get().getId());
                }
                BlendingAdvicePreProcess result = blendingAdvicePreProcessRepository.save(blendingAdvicePreProcess);

                if (!blendingAdvicePreProcessDto.getBinDetails().isEmpty()) {
                    for (BlendingPreProcessBinDetailsDto binDetailsDto : blendingAdvicePreProcessDto.getBinDetails()) {
                        BlendingPreProcessBinDetails binDetails = new BlendingPreProcessBinDetails(binDetailsDto);
                        binDetails.setBlendingAdvicePreprocessId(result.getId());
                        BlendingPreProcessBinDetails res = blendingAdvicePreProcessBindetailsRepository.save(binDetails);
                        updateBlendingPreProcessStockLock(res);
                    }
                }

                //Find whether any config related to Bill Inward

                Optional<ActionConfiguration> getByTaskName = actionConfigurationRepository.findByTaskName("Blending Advice");
                if (getByTaskName.isPresent()) {

                    ActionConfigurationDto rec = new ActionConfigurationDto(getByTaskName.get());
                    // Actions table insertion
                    if (result != null) {
                        ActionDto dto = new ActionDto();
                        dto.setActionConfigId(rec.getId());
                        dto.setTaskDescription("Blending Advice for " + blendingAdvicePreProcess.getCasGroup());
                        dto.setUniqueRef(blendingAdvicePreProcess.getCasGroup());
                        dto.setOwnerId(loggedUser.getId());
                        dto.setRefType("Process");
                        dto.setStatus("Pending");
                        dto.setBranchId(1L);
                        dto.setDeptID(1L);
                        dto.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                        dto.setCreatedBy(loggedUser.getId());
                        Action model = new Action(dto);
                        actionRepository.save(model);
                    }
                }
            }
        }
    }


    public void updateBlendingStockLock(BlendingAdviceBinDetails binDetails) {

        Optional<BinStock> optionalBinStock = binStockRepository.findByProductAndBatchAndBinId(binDetails.getProductId(), binDetails.getBatchNo(), binDetails.getBinNo(), binDetails.getMainlot());
        if (optionalBinStock.isPresent()) {
            BinStock binStock = optionalBinStock.get();
            binStock.setBlendingLock(true);
            binStockRepository.save(binStock);
        }
    }


    public void updateBlendingPreProcessStockLock(BlendingPreProcessBinDetails binDetails) {

        List<BinStock> optionalBinStock = binStockRepository.findByBatchAndBinId(binDetails.getBatchId(), binDetails.getBatchNo(), binDetails.getBinNo());
        if (!optionalBinStock.isEmpty()) {
            for (BinStock binStock1 : optionalBinStock) {
                BinStock binStock = binStock1;
                binStock.setBlendingLock(true);
                binStockRepository.save(binStock);
            }
        }
    }


    public ResponseEntity<?> blendingList(Long branchId, Pageable pageable) throws UsernameNotFoundException {

        Page<BlendingAdviceDetails> blendingAdviceDetailsList = blendingAdviceDetailsRepository.findByBranchId(branchId, pageable);
        if (!blendingAdviceDetailsList.isEmpty()) {
            List<BlendingAdviceDetailsDto> result = blendingAdviceDetailsList.stream().map(data -> {
                BlendingAdviceDetailsDto blendingAdviceDetailsDto = new BlendingAdviceDetailsDto(data);

                List<BlendingAdviceStockDetailsDto> StockDetailsDtos = data.getStockDetails() != null
                        ? data.getStockDetails().stream().map(stock -> {

                    BlendingAdviceStockDetailsDto blendingAdviceStockDetailsDto = new BlendingAdviceStockDetailsDto(stock);

                    List<BlendingAdviceBinDetailsDto> binDetailsDtos = stock.getBinDetails() != null
                            ? stock.getBinDetails().stream().map(bin -> {
                        BlendingAdviceBinDetailsDto binDetailsDto = new BlendingAdviceBinDetailsDto(bin);
                        Double avgFFA = binStockRepository.getAvgFFA(binDetailsDto.getBinNo(), binDetailsDto.getProductId());
                        if (avgFFA != null) {
                            binDetailsDto.setAvgFFA(decfor.format(avgFFA));
                        }
                        return binDetailsDto;

                    }).collect(Collectors.toList()) : null;

                    blendingAdviceStockDetailsDto.setBinDetails(binDetailsDtos);

                    return blendingAdviceStockDetailsDto;

                }).collect(Collectors.toList()) : null;

                blendingAdviceDetailsDto.setStockDetails(StockDetailsDtos);

                return blendingAdviceDetailsDto;

            }).collect(Collectors.toList());

            PageDataDto<BlendingAdviceDetailsDto> pageData = new PageDataDto<>(result, blendingAdviceDetailsList);
            return new ResponseEntity<PageDataDto<BlendingAdviceDetailsDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<BlendingAdviceDetailsDto>>(new PageDataDto<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> blendingAdviceDetailsById(Long id) throws UsernameNotFoundException {

        Optional<BlendingAdviceDetails> blendingAdviceDetails = blendingAdviceDetailsRepository.findById(id);

        if (blendingAdviceDetails.isPresent()) {

            BlendingAdviceDetailsDto blendingAdviceDetailsDto = new BlendingAdviceDetailsDto(blendingAdviceDetails.get());

            blendingAdviceDetailsDto.setIsoNo(isoRepository.getISONo("BLENDING"));

            List<BlendingAdviceStockDetailsDto> StockDetailsDtos = blendingAdviceDetails.get().getStockDetails() != null
                    ? blendingAdviceDetails.get().getStockDetails().stream().map(stock -> {

                BlendingAdviceStockDetailsDto blendingAdviceStockDetailsDto = new BlendingAdviceStockDetailsDto(stock);

                List<BlendingAdviceBinDetailsDto> binDetailsDtos = stock.getBinDetails() != null
                        ? stock.getBinDetails().stream().map(bin -> {

                    BlendingAdviceBinDetailsDto binDetailsDto = new BlendingAdviceBinDetailsDto(bin);

                    Double avgFFA = binStockRepository.getAvgFFA(binDetailsDto.getBinNo(), binDetailsDto.getProductId());
                    if (avgFFA != null) {
                        binDetailsDto.setAvgFFA(decfor.format(avgFFA));
                    }
                    BinStock binStock = null;
                    binStock = binStockRepository.findBy(binDetailsDto.getBranchId(), binDetailsDto.getProductId(), binDetailsDto.getBatchNo(), binDetailsDto.getBinNo(), binDetailsDto.getLot());
                    if (binStock != null) {
                        binDetailsDto.setBinStockFFA(binStock.getOilFFA());
                        binDetailsDto.setBinId(binStock.getBinId());
                        binDetailsDto.setLocation(binStock.getLocation());
                        binDetailsDto.setState(binStock.getLocation());
                        binDetailsDto.setSeedColour(binStock.getSeedcolor());
                        binDetailsDto.setCategory(binStock.getCategory());
                        binDetailsDto.setSeedMoisture(binStock.getSeedMoisture());
                        binDetailsDto.setInwardDate(dateUtil.getDateTOStringWk(binStock.getInwarDate()));
                        Optional<Branch> branchOptional = branchRepository.findById(binDetailsDto.getBranchId());
                        if (branchOptional.isPresent()) {
                            binDetailsDto.setBranchName(branchOptional.get().getBranchName());
                        }
                    } else {
                        binStock = binStockRepository.findBy(binDetailsDto.getBranchId(), binDetailsDto.getProductId(), binDetailsDto.getBatchNo(), binDetailsDto.getBinNo());
                        if (binStock != null) {
                            binDetailsDto.setBinStockFFA(binStock.getOilFFA());
                            binDetailsDto.setBinId(binStock.getBinId());
                            binDetailsDto.setLocation(binStock.getLocation());
                            binDetailsDto.setState(binStock.getLocation());
                            binDetailsDto.setSeedColour(binStock.getSeedcolor());
                            binDetailsDto.setCategory(binStock.getCategory());
                            binDetailsDto.setSeedMoisture(binStock.getSeedMoisture());
                            binDetailsDto.setInwardDate(dateUtil.getDateTOStringWk(binStock.getInwarDate()));
                            Optional<Branch> branchOptional = branchRepository.findById(binDetailsDto.getBranchId());
                            if (branchOptional.isPresent()) {
                                binDetailsDto.setBranchName(branchOptional.get().getBranchName());
                            }
                        }
                    }

                    return binDetailsDto;

                }).collect(Collectors.toList()) : null;

                blendingAdviceStockDetailsDto.setBinDetails(binDetailsDtos);

                return blendingAdviceStockDetailsDto;

            }).collect(Collectors.toList()) : null;

            blendingAdviceDetailsDto.setStockDetails(StockDetailsDtos);

            Optional<BlendingAdvice> blendingAdvice = blendingAdviceRepository.findById(blendingAdviceDetailsDto.getBlendingAdviceId());
            if (blendingAdvice.isPresent()) {
                String ksw = dateUtil.getDateTOStringWk(blendingAdvice.get().getFromDate()) + "/" + dateUtil.getDateTOStringWk(blendingAdvice.get().getToDate());
                blendingAdviceDetailsDto.setKalasalWeek(ksw);
            }
            Optional<Journal> journal = journalRepository.findById(blendingAdviceDetailsDto.getJournalId());
            if (journal.isPresent()) {
                if (journal.get().getPrimaryProductId() != null) {
                    Optional<ProductMaster> optional = productMasterRepository.findById(journal.get().getPrimaryProductId());
                    if (optional.isPresent()) {
                        blendingAdviceDetailsDto.setPrimaryProductId(optional.get().getId());
                        blendingAdviceDetailsDto.setPrimaryProductName(optional.get().getProductName());
                    }
                }
            }
            return new ResponseEntity<BlendingAdviceDetailsDto>(blendingAdviceDetailsDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<BlendingAdviceDetailsDto>(HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity<?> printBlendingList(Long journalId, String kalasalWeek, String status, Long branch, Pageable pageable) throws UsernameNotFoundException {


        Date fromDate = null;
        Date toDate = null;
        String dateFrom = null;
        String dateTo = null;
        if (Objects.nonNull(kalasalWeek)) {
            List<String> list = Arrays.asList(kalasalWeek.split("/"));
            if (!list.isEmpty()) {
                dateFrom = list.get(0);
                dateTo = list.get(1);
            }
        }

        if (dateFrom != null) {
            fromDate = dateUtil.getStringTODateWk(dateFrom);
        }

        if (dateTo != null) {
            toDate = dateUtil.getStringTODateWk(dateTo);
        }
        Page<BlendingAdvicePrintList> listPage = null;
        if (status != null) {
            listPage = blendingAdviceRepository.findBy(journalId, fromDate, toDate, status, branch, pageable);
        } else {
            listPage = blendingAdviceRepository.findBy(journalId, fromDate, toDate, branch, pageable);
        }

        if (!listPage.toList().isEmpty()) {
            List<BlendingAdvicePrintListDto> result = listPage.toList().stream().map(data -> new BlendingAdvicePrintListDto(data)).collect(Collectors.toList());
            PageDataDto<BlendingAdvicePrintListDto> pageData = new PageDataDto<>(result, pageable, listPage);
            return new ResponseEntity<PageDataDto<BlendingAdvicePrintListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<BlendingAdvicePrintListDto>>(new PageDataDto<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> activateBlendingAdvice(Long adviceId, Long journalId, Long branchId, User loggedUser) throws UsernameNotFoundException {

        List<BlendingAdvice> blendingAdviceList = blendingAdviceRepository.findBy(adviceId, journalId);
        if (!blendingAdviceList.isEmpty()) {
            updateExpirePreviousCas(blendingAdviceList, branchId);
        }

        Optional<BlendingAdvice> optional = blendingAdviceRepository.findById(adviceId);
        if (optional.isPresent()) {
            BlendingAdvice blendingAdvice = optional.get();
            blendingAdvice.setStatus("A");
            blendingAdviceRepository.save(blendingAdvice);
            List<BlendingAdviceDetails> blendingAdviceDetailsList = blendingAdviceDetailsRepository.findByList(adviceId, branchId);
            for (BlendingAdviceDetails blendingAdviceDetails : blendingAdviceDetailsList) {
                blendingAdviceDetails.setStatus("A");
                blendingAdviceDetails.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                BlendingAdviceDetails result = blendingAdviceDetailsRepository.save(blendingAdviceDetails);
                releaseDeliteLock(result, loggedUser);
                sentExternalRequest(result, loggedUser);
                sentExternalGodown(result, loggedUser);
                updatePreprocess(result, loggedUser);
            }
            return new ResponseEntity<Status>(new Status("200", "Blending Advice Activated Successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(HttpStatus.NOT_FOUND);
        }

    }


    public void updateExpirePreviousCas(List<BlendingAdvice> blendingAdviceList, Long branchId) {

        if (!blendingAdviceList.isEmpty()) {

            for (BlendingAdvice blendingAdvice : blendingAdviceList) {

                List<BlendingAdviceDetails> blendingAdviceDetailsList = blendingAdviceDetailsRepository.findByList(blendingAdvice.getId(), branchId);
                if (!blendingAdviceDetailsList.isEmpty()) {
                    for (BlendingAdviceDetails blendingAdviceDetails : blendingAdviceDetailsList) {
                        updateBlendingStockDetails(blendingAdviceDetails, "UpdatePreviousCas");
                        blendingAdviceDetails.setStatus("E");
                        blendingAdviceDetailsRepository.save(blendingAdviceDetails);
                        saveSchedulerDetailsByAdviceDetails(blendingAdviceDetails, "UpdatePreviousCas");
                        updateMaterialRequestExpire(blendingAdviceDetails);
                    }
                }

                List<BlendingAdviceDetails> adviceDetailsList = blendingAdviceDetailsRepository.findByNotIn(blendingAdvice.getId());
                if (adviceDetailsList.isEmpty()) {
                    blendingAdvice.setStatus("E");
                    blendingAdviceRepository.save(blendingAdvice);
                    saveSchedulerDetailsAdvice(blendingAdvice, "UpdatePreviousCas");
                }
            }
        }
    }


    public void sentExternalRequest(BlendingAdviceDetails blendingAdvice, User loggedUser) {

        List<BlendingRequestList> blendingRequestLists = blendingAdviceStockDetailsRepository.findByRequestItem(blendingAdvice.getId());

        if (!blendingRequestLists.isEmpty()) {

            MaterialRequest materialRequest = new MaterialRequest();

            Branch requestbranch = new Branch();
            requestbranch.setId(blendingAdvice.getBranchId());
            materialRequest.setBranch(requestbranch);

            DepartmentDetails department = new DepartmentDetails();
            department.setId(4L);
            materialRequest.setDepartment(department);

            materialRequest.setRequestBy(loggedUser);
            Organisation organisation = new Organisation();
            organisation.setId(loggedUser.getOrg_id());
            materialRequest.setOrganisation(organisation);
            materialRequest.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            materialRequest.setRequestDate(dateUtil.getISTDate());
            materialRequest.setActive(0);
            materialRequest.setRequestName("Blending_Request");
            materialRequest.setRequestDescription("Blending_Request");
            materialRequest.setStatus("Requested Sent");
            materialRequest.setCreatedBy(loggedUser.getId());
            MaterialRequest request = materialRequestRepository.save(materialRequest);

            for (BlendingRequestList blendingRequestList : blendingRequestLists) {

                Branch issuebranch = new Branch();
                issuebranch.setId(blendingRequestList.getIssueBranch());

                DepartmentDetails issueDepartment = new DepartmentDetails();
                issueDepartment.setId(blendingRequestList.getIssueDeptId());

                RequestItem requestItem = new RequestItem();
                requestItem.setRequestId(request.getId());
                requestItem.setCreated_date(dateUtil.getISTDate());

                requestItem.setIssueBranch(issuebranch);
                requestItem.setIssueDepartment(issueDepartment);


                requestItem.setSelectLot(String.valueOf(blendingRequestList.getMainLot()));
                requestItem.setCreatedBy(loggedUser.getId());

                Optional<ProductMaster> optionalProductMaster = productMasterRepository.findById(blendingRequestList.getProductId());
                if (optionalProductMaster.isPresent()) {
                    requestItem.setItemName(optionalProductMaster.get().getProductName());
                }

                requestItem.setItemId(blendingRequestList.getProductId());

                Double qty = 0.0;
                if (blendingRequestList.getMainLot() != null) {
                    if (blendingRequestList.getMainLot() == blendingRequestList.getLot()) {
                        qty = blendingRequestList.getQty();
                    } else {
                        qty = blendingRequestList.getQty() * blendingRequestList.getLot();
                    }
                    requestItem.setSelectLot(blendingRequestList.getMainLot().toString());
                }
                requestItem.setStockRequired(qty.toString());
                requestItem.setStockQuantity(qty.toString());


                requestItem = requestItemRepository.save(requestItem);

            }
            updateMaterialIssue(request, blendingAdvice);
            updateBlendingRequestBinAndPreprocessBin(blendingAdvice, request, blendingRequestLists);
        }

    }


    public void updateMaterialIssue(MaterialRequest materialRequest, BlendingAdviceDetails blendingAdvice) {

        List<BlendingRequestList> blendingRequestLists = blendingAdviceStockDetailsRepository.findByIssueBranchAndDept(blendingAdvice.getId());

        for (BlendingRequestList blendingRequestList : blendingRequestLists) {

            Branch issueBranch = new Branch();
            issueBranch.setId(blendingRequestList.getIssueBranch());

            DepartmentDetails issueDepartment = new DepartmentDetails();
            issueDepartment.setId(blendingRequestList.getIssueDeptId());

            MaterialIssue materialIssue = new MaterialIssue();
            materialIssue.setRequestDate(materialRequest.getRequestDate());
            materialIssue.setRequestBranch(materialRequest.getBranch());
            materialIssue.setRequestDepartment(materialRequest.getDepartment());
            materialIssue.setOrganisation(materialRequest.getOrganisation());
            materialIssue.setRequestDescription(materialRequest.getRequestDescription());
            materialIssue.setRequestId(materialRequest.getId());
            materialIssue.setRequestName(materialRequest.getRequestName());
            materialIssue.setCreated_date(materialRequest.getCreated_date());
            materialIssue.setCreatedBy(materialRequest.getCreatedBy());
            materialIssue.setStatus("Pending");

            materialIssue.setIssueBranch(issueBranch);

            materialIssue.setIssueDepartment(issueDepartment);

            MaterialIssue result = materialIssueRepository.save(materialIssue);


            List<BlendingRequestList> binDetails = blendingAdviceStockDetailsRepository.findByRequestItem(blendingAdvice.getId(),
                    blendingRequestList.getProductId(), blendingRequestList.getIssueBranch(), blendingRequestList.getIssueDeptId());
            List<BlendingRequestList> pendingBinDetails = new ArrayList<>();
            for (BlendingRequestList requestItem : binDetails) {

                /*System.out.println("product id = " + requestItem.getProductId());
                System.out.println("lot = " + requestItem.getLot());
                System.out.println("batch no = " + requestItem.getBatchNo());
                System.out.println("branch= " + requestItem.getIssueBranch());
                System.out.println("qaty = " + requestItem.getQty());
                System.out.println("mainlot = " + requestItem.getMainLot());*/
                Double qty = 0.0;
                if (requestItem.getMainLot() != null) {
                    if (requestItem.getMainLot() == requestItem.getLot()) {
                        qty = requestItem.getQty();
                    } else {
                        qty = requestItem.getQty() * requestItem.getLot();
                    }
                }

                BinStock binstock = binStockRepository.findByBinStockObject(requestItem.getProductId(), requestItem.getBatchNo(), requestItem.getBinNo(), requestItem.getMainLot(), qty);
                if (binstock != null) {

                    System.out.println("binstock id = " + binstock.getId());


                    IssueItems issueItems = new IssueItems();
                    issueItems.setIssueItemId(result.getId());
                    issueItems.setCreated_date(dateUtil.getISTDate());
                    issueItems.setActive(0);
                    issueItems.setBranchId(requestItem.getIssueBranch());
                    issueItems.setDeptId(requestItem.getIssueDeptId());
                    issueItems.setItemId(requestItem.getProductId());
                    Optional<ProductMaster> optionalProductMaster = productMasterRepository.findById(requestItem.getProductId());
                    if (optionalProductMaster.isPresent()) {
                        issueItems.setItemName(optionalProductMaster.get().getProductName());
                    }
                    issueItems.setIssuedFromBatch(requestItem.getBatchNo());
                    issueItems.setIssuedFromBin(requestItem.getBinNo().toString());

                    issueItems.setSelectLot(requestItem.getMainLot().toString());

                    issueItems.setRequestedQuantity(qty.toString());

                    issueItems.setBinStockId(binstock.getId());

                    issueItems.setStockQuantity(binstock.getStkQty().toString());

                    updateBinDetails(issueItems, requestItem);
                    issueItemsRepository.save(issueItems);
                } else {
                    pendingBinDetails.add(requestItem);
                }
            }

            if (!pendingBinDetails.isEmpty()) {
                for (BlendingRequestList requestItem : pendingBinDetails) {

                    /*System.out.println("product id = " + requestItem.getProductId());
                    System.out.println("lot = " + requestItem.getLot());
                    System.out.println("batch no = " + requestItem.getBatchNo());
                    System.out.println("branch= " + requestItem.getIssueBranch());
                    System.out.println("qaty = " + requestItem.getQty());
                    System.out.println("mainlot = " + requestItem.getMainLot());*/

                    Double qty = 0.0;
                    if (requestItem.getMainLot() != null) {
                        if (requestItem.getMainLot() == requestItem.getMainLot()) {
                            qty = requestItem.getQty();
                        } else {
                            qty = requestItem.getQty() * requestItem.getLot();
                        }
                    }
                    Double finalReqQty = qty;
                    Double finalqty = qty;
                    Double reqQty = 0.0;
                    Double stkQty = 0.0;

                    List<BinStock> binStockList = binStockRepository.findByBinStockObject(requestItem.getProductId(), requestItem.getBatchNo(), requestItem.getBinNo(), requestItem.getMainLot());
                    if (!binStockList.isEmpty()) {
                        for (BinStock binStock : binStockList) {
                            System.out.println("binstock id 1== " + binStock.getId());
                            if (finalqty > 0) {

                                stkQty = binStock.getStkQty();

                                finalqty = finalReqQty - stkQty;

                                if (finalReqQty > stkQty) {
                                    reqQty = stkQty;
                                } else {
                                    reqQty = finalReqQty;
                                }

                                IssueItems issueItems = new IssueItems();
                                issueItems.setIssueItemId(result.getId());
                                issueItems.setCreated_date(dateUtil.getISTDate());
                                issueItems.setActive(0);
                                issueItems.setBranchId(requestItem.getIssueBranch());
                                issueItems.setDeptId(requestItem.getIssueDeptId());
                                issueItems.setItemId(requestItem.getProductId());
                                Optional<ProductMaster> optionalProductMaster = productMasterRepository.findById(requestItem.getProductId());
                                if (optionalProductMaster.isPresent()) {
                                    issueItems.setItemName(optionalProductMaster.get().getProductName());
                                }
                                issueItems.setIssuedFromBatch(requestItem.getBatchNo());
                                issueItems.setIssuedFromBin(requestItem.getBinNo().toString());

                                issueItems.setSelectLot(requestItem.getMainLot().toString());

                                issueItems.setRequestedQuantity(reqQty.toString());

                                issueItems.setStockQuantity(stkQty.toString());

                                issueItems.setBinStockId(binStock.getId());

                                updateBinDetails(issueItems, requestItem);
                                issueItemsRepository.save(issueItems);
                            }
                        }
                    }
                }
            }
        }

    }


    public void updateBinDetails(IssueItems issueItems, BlendingRequestList binDetails) {
        List<BinDetails> binDetailsList = new ArrayList<>();
        BinDetails binDetails1 = new BinDetails();
        binDetails1.setAllottedQuantity(binDetails.getQty());
        binDetails1.setBinid(binDetails.getBinNo());
        binDetails1.setBatchno(binDetails.getBatchNo());
        binDetailsList.add(binDetails1);
        issueItems.setBinDetails(binDetailsList);

    }

    public void updateBlendingRequestBinAndPreprocessBin(BlendingAdviceDetails blendingAdvice, MaterialRequest request, List<BlendingRequestList> blendingRequestList) {

        BlendingRequestAndPreprocessBinMap blendingBinMap = new BlendingRequestAndPreprocessBinMap();
        blendingBinMap.setBlendingDetailId(blendingAdvice.getId());
        blendingBinMap.setCasgroup(blendingAdvice.getCasTransaction());
        blendingBinMap.setTypeId(request.getId());
        blendingBinMap.setType("Blending_Material_Request");
        blendingRequestAndPreprocessBinMapRepository.save(blendingBinMap);
    }


    public void updateBlendingRequestBinAndPreprocessBin(BlendingAdviceDetails blendingAdvice, GoDownExternal goDownExternal) {

        BlendingRequestAndPreprocessBinMap blendingBinMap = new BlendingRequestAndPreprocessBinMap();
        blendingBinMap.setBlendingDetailId(blendingAdvice.getId());
        blendingBinMap.setCasgroup(blendingAdvice.getCasTransaction());
        blendingBinMap.setTypeId(goDownExternal.getId());
        blendingBinMap.setTrnNo(goDownExternal.getTrnNo());
        blendingBinMap.setType("Blending_External_Request");
        blendingRequestAndPreprocessBinMapRepository.save(blendingBinMap);
    }


    public void checkBatchDetails() {

        Date date = dateUtil.getISTDate();

        List<BlendingAdvice> blendingAdviceList = blendingAdviceRepository.findByDate(date);
        if (!blendingAdviceList.isEmpty() && blendingAdviceList != null) {
            for (BlendingAdvice blendingAdvice : blendingAdviceList) {
                boolean status = updateBlendingDetails(blendingAdvice);
                blendingAdvice.setStatus("E");
                BlendingAdvice rd = blendingAdviceRepository.save(blendingAdvice);
                saveSchedulerDetailsAdvice(rd, "scheduler");
            }
        }
        updatedBatchDetails(date);
    }


    public void updatedBatchDetails(Date date) {

        List<BlendingAdvice> blendingAdviceList = blendingAdviceRepository.findByExpired(date);
        if (!blendingAdviceList.isEmpty() && blendingAdviceList != null) {
            for (BlendingAdvice blendingAdvice : blendingAdviceList) {
                boolean status = updateBlendingDetails(blendingAdvice);
                blendingAdvice.setStatus("E");
                BlendingAdvice rd = blendingAdviceRepository.save(blendingAdvice);
                saveSchedulerDetailsAdvice(rd, "scheduler");
            }
        }
    }

    public void saveSchedulerDetailsAdvice(BlendingAdvice blendingAdvices, String type) {

        BlendingScheduler scheduler = new BlendingScheduler();
        scheduler.setADate(dateUtil.getISTDate());
        scheduler.setAdatetime(dateUtil.getISTDate());
        scheduler.setBlending_id(blendingAdvices.getId());
        scheduler.setBlending_table("BlendingAdvice");
        scheduler.setFromDate(blendingAdvices.getFromDate());
        scheduler.setToDate(blendingAdvices.getToDate());
        scheduler.setBlending_type(type);
        blendingSchedulerRepo.save(scheduler);
    }


    public void saveSchedulerDetailsByAdviceDetails(BlendingAdviceDetails blendingAdvices, String type) {

        BlendingScheduler scheduler = new BlendingScheduler();
        scheduler.setADate(dateUtil.getISTDate());
        scheduler.setAdatetime(dateUtil.getISTDate());
        scheduler.setBlending_id(blendingAdvices.getId());
        scheduler.setBlending_table("BlendingAdviceDetails");
        scheduler.setBlending_type(type);
        blendingSchedulerRepo.save(scheduler);
    }

    public void saveSchedulerDetailsByAdviceStockDetails(BlendingAdviceStockDetails blendingAdvices, String type) {

        BlendingScheduler scheduler = new BlendingScheduler();
        scheduler.setADate(dateUtil.getISTDate());
        scheduler.setAdatetime(dateUtil.getISTDate());
        scheduler.setBlending_id(blendingAdvices.getId());
        scheduler.setBlending_table("BlendingAdviceStockDetails");
        scheduler.setBlending_type(type);
        blendingSchedulerRepo.save(scheduler);
    }


    public boolean updateBlendingDetails(BlendingAdvice blendingAdvice) {
        boolean status = true;
        try {
            List<BlendingAdviceDetails> blendingAdviceDetailsList = blendingAdviceDetailsRepository.findByAdviceId(blendingAdvice.getId());
            if (!blendingAdviceDetailsList.isEmpty() && blendingAdviceDetailsList != null) {
                for (BlendingAdviceDetails blendingAdviceDetails : blendingAdviceDetailsList) {
                    boolean chStatus = updateBlendingStockDetails(blendingAdviceDetails, "scheduler");
                    blendingAdviceDetails.setStatus("E");
                    BlendingAdviceDetails rd = blendingAdviceDetailsRepository.save(blendingAdviceDetails);
                    saveSchedulerDetailsByAdviceDetails(rd, "scheduler");
                    updateMaterialRequestExpire(blendingAdviceDetails);
                }
            }
        } catch (Exception e) {
            status = false;
        }
        return status;
    }


    public boolean updateBlendingStockDetails(BlendingAdviceDetails blendingAdviceDetails, String type) {

        boolean status = true;
        try {
            List<BlendingAdviceStockDetails> blendingAdviceStockDetailsList = blendingAdviceStockDetailsRepository.findBy(blendingAdviceDetails.getId());
            if (!blendingAdviceStockDetailsList.isEmpty()) {
                for (BlendingAdviceStockDetails blendingAdviceStockDetails : blendingAdviceStockDetailsList) {
                    List<BlendingAdviceBinDetails> binDetailsList = blendingAdviceBinDetailsRepository.findBy(blendingAdviceStockDetails.getId());
                    if (!binDetailsList.isEmpty()) {
                        for (BlendingAdviceBinDetails binDetails : binDetailsList) {
                            List<BinStock> binStockList = binStockRepository.findByProductAndBatchAndBinIdANDLockTrue(binDetails.getProductId(), binDetails.getBatchNo(), binDetails.getBinNo());
                            if (!binStockList.isEmpty()) {
                                for (BinStock optionalBinStock : binStockList) {
                                    BinStock binStock = optionalBinStock;
                                    binStock.setBlendingLock(false);
                                    binStockRepository.save(binStock);
                                }
                            }
                        }
                    }
                    blendingAdviceStockDetails.setStatus("E");
                    BlendingAdviceStockDetails rd = blendingAdviceStockDetailsRepository.save(blendingAdviceStockDetails);
                    saveSchedulerDetailsByAdviceStockDetails(rd, type);
                }

            } else {
                status = false;
            }
        } catch (Exception e) {
            status = false;
        }
        return status;
    }


    public ResponseEntity<?> casAvgFFA(Long productId, List<Long> binName) throws UsernameNotFoundException {

        Double avgFFA = binStockRepository.getAvgCasFFA(binName, productId);
        if (avgFFA != null) {
            return new ResponseEntity<String>(decfor.format(avgFFA), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> categoryList(String category) throws UsernameNotFoundException {

        List<String> blendingStocks = binStockRepository.findByCategory(category);
        if (!blendingStocks.isEmpty()) {
            return new ResponseEntity<List<String>>(blendingStocks, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<String>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public void sentExternalGodown(BlendingAdviceDetails blendingAdvice, User loggedUser) {

        List<BlendingRequestList> blendingRequestLists = blendingAdviceStockDetailsRepository.findByRequestGodown(blendingAdvice.getId());

        if (!blendingRequestLists.isEmpty()) {

            for (BlendingRequestList blendingRequestGodown : blendingRequestLists) {

                GoDownExternal goDownExternal = new GoDownExternal();

                goDownExternal.setType("Blending_Request");
                goDownExternal.setFromName(blendingRequestGodown.getGodownName());
                goDownExternal.setFrom("godown");
                goDownExternal.setFromId(blendingRequestGodown.getGodownId());

                Optional<Branch> branchOptional = branchRepository.findById(blendingAdvice.getBranchId());
                if (branchOptional.isPresent()) {
                    goDownExternal.setToName(branchOptional.get().getBranchName());
                    goDownExternal.setTo("branch");
                    /*goDownExternal.setIssueBranch(branchOptional.get());
                    goDownExternal.setIssueDepartment(branchOptional.get().getId());
                    DepartmentDetails fromDept = new DepartmentDetails();
                    fromDept.setId(4L);
                    goDownExternal.setIssueDepartment(fromDept);*/
                }

                Branch fromBranch = new Branch();
                fromBranch.setId(blendingRequestGodown.getIssueBranch());
                goDownExternal.setFromBranch(fromBranch);

                DepartmentDetails detailsOptional = new DepartmentDetails();
                detailsOptional.setId(blendingRequestGodown.getIssueDeptId());
                goDownExternal.setFromDepartment(detailsOptional);

                Organisation organisation = new Organisation();
                organisation.setId(loggedUser.getOrg_id());
                goDownExternal.setStatus("pending");
                goDownExternal.setTrnNo(getTrnNo());
                goDownExternal.setCreatedBy(loggedUser.getId());
                goDownExternal.setCreatedDate(LocalDate.now(ZoneId.of("Asia/Kolkata")));
                goDownExternal.setActive(0);

                GoDownExternal result = goDownExternalRepository.save(goDownExternal);

                List<BlendingRequestList> godownItemLists = blendingAdviceStockDetailsRepository.findByRequestGoDownItems(blendingAdvice.getId(), blendingRequestGodown.getGodownId(), blendingRequestGodown.getIssueBranch(), blendingRequestGodown.getIssueDeptId());

                for (BlendingRequestList blendingRequestList : godownItemLists) {

                    /*
                    RequestGodownItem requestGodownItem = new RequestGodownItem();
                    
                    requestGodownItem.setGoDownExternalId(result.getId());
                    
                    Branch issuebranch = new Branch();
                    issuebranch.setId(blendingRequestList.getIssueBranch());

                    DepartmentDetails issueDepartment = new DepartmentDetails();
                    issueDepartment.setId(blendingRequestList.getIssueDeptId());

                    requestGodownItem.setIssueBranch(issuebranch);
                    requestGodownItem.setIssueDepartment(issueDepartment);

                    requestGodownItem.setStockQuantity(String.valueOf(blendingRequestList.getQty()));
                    requestGodownItem.setSelectLot(String.valueOf(blendingRequestList.getLot()));

                    Optional<ProductMaster> optionalProductMaster = productMasterRepository.findById(blendingRequestList.getProductId());
                    if (optionalProductMaster.isPresent()) {
                        requestGodownItem.setItemName(optionalProductMaster.get().getProductName());
                    }
                    requestGodownItem.setItemId(blendingRequestList.getProductId());
                    requestGodownItem.setStockQuantity(blendingRequestList.getQty().toString());
                    if (blendingRequestList.getLot() != null) {
                        requestGodownItem.setSelectLot(blendingRequestList.getLot().toString());
                    }

                    RequestGodownItem req = requestGodownItemRepository.save(requestGodownItem);
                    */

                    List<BinStock> binStockList = binStockRepository.findByGodownStock(blendingRequestList.getProductId(),
                            blendingRequestList.getBatchNo(), blendingRequestList.getBinNo(),
                            blendingRequestList.getGodownId(), blendingRequestList.getLot(), blendingRequestList.getIssueBranch());

                    if (binStockList != null) {

                        ExternalGodownBinDetails binDetails = new ExternalGodownBinDetails();
                        binDetails.setBatchNo(blendingRequestList.getBatchNo());
                        binDetails.setBinNo(blendingRequestList.getBinNo());
                        binDetails.setItemId(blendingRequestList.getProductId());
                        Optional<ProductMaster> optionalProductMaster = productMasterRepository.findById(blendingRequestList.getProductId());
                        if (optionalProductMaster.isPresent()) {
                            binDetails.setItemName(optionalProductMaster.get().getProductName());
                        }
                        binDetails.setGodownId(blendingRequestList.getGodownId());
                        binDetails.setQty(blendingRequestList.getQty());
                        binDetails.setLot(blendingRequestList.getLot());
                        binDetails.setGoDownExternalId(result.getId());
                        binDetails.setBranch(blendingRequestList.getIssueBranch());
                        binDetails.setDept(blendingRequestList.getIssueDeptId());
                        externalGodownBinDetailsRepository.save(binDetails);

                    }
                }

                updateBlendingRequestBinAndPreprocessBin(blendingAdvice, goDownExternal);
            }
        }
    }


    private String getTrnNo() {
        String trnNo = null;
        Optional<TRNIndex> trnIndex = trnIndexRepo.findByPrefix("GMRN");
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


    public void updatePreprocess(BlendingAdviceDetails blendingAdvice, User loggedUser) {


        List<BlendingPreprocessRequestList> blendingRequestLists = blendingAdvicePreProcessBindetailsRepository.findByDetailsId(blendingAdvice.getId());

        if (!blendingRequestLists.isEmpty()) {

            for (BlendingPreprocessRequestList blendingRequestBin : blendingRequestLists) {
                BlendingPreprocessRequestAndReceiveBinMap requestAndReceiveBinMap = new BlendingPreprocessRequestAndReceiveBinMap(blendingRequestBin);
                blendingPreprocessRequestAndReceiveBinMapRepository.save(requestAndReceiveBinMap);
            }
        }
    }

    public void releaseDeliteLock(BlendingAdviceDetails blendingAdviceDetails, User loggedUser) {

        Optional<Journal> journalOptional = journalRepository.findById(blendingAdviceDetails.getJournalId());
        if (journalOptional.isPresent() && journalOptional.get().getBlending()) {
            if (journalOptional.get().getStockType() != null && journalOptional.get().getStockType().equalsIgnoreCase("Delite Stock")) {

                List<Long> binNoList = blendingAdviceBinDetailsRepository.findByBinNo(blendingAdviceDetails.getId());

                for (Long binno : binNoList){
                    Optional<BinLock> binLockOptional = binLockRepository.findByDTBinLock(binno.toString());
                    if(binLockOptional.isPresent()) {
                        BinLockRequestDto binLockRequestDto = new BinLockRequestDto();
                        binLockRequestDto.setId(binLockOptional.get().getId());
                        binLockRequestDto.setLockDate(dateUtil.getDateTOStringWk(binLockOptional.get().getLockDate()));
                        binLockRequestDto.setLocked(false);
                        binLockRequestDto.setLockedEmployeeId(loggedUser.getId());
                        binLockRequestDto.setLockType(null);
                        binLockRequestDto.setLockStatus("UnLock");
                        binLockRequestDto.setBinNo(binno.toString());
                        binDetailsServices.saveBinLock(binLockRequestDto, loggedUser);
                    }
                }
            }
        }
    }



    public void updateMaterialRequestExpire(BlendingAdviceDetails blendingAdviceDetails){

        materialIssueRepository.updateExpiredBlendingRequest(blendingAdviceDetails.getId());
    }

}
