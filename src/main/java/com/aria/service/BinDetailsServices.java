package com.aria.service;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.BlendingUtil;
import com.aria.utils.DateUtil;
import lombok.extern.flogger.Flogger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
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

import jakarta.transaction.Transactional;

@Service
@Slf4j
public class BinDetailsServices {


    public static final Logger logger = org.slf4j.LoggerFactory.getLogger(BinDetailsServices.class);

    @Autowired
    BinStatusRepository binStatusRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    GoDownRepository goDownRepository;

    @Autowired
    BinStockRepository binStockRepository;

    @Autowired
    BinRangeRepository binRangeRepository;

    @Autowired
    GoGroupRepository goGroupRepository;

    @Autowired
    BinLockRepository binLockRepository;

    @Autowired
    BlendingUtil blendingUtil;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    DepartmentDetailsRepository detailsRepository;

    @Autowired
    ProductMasterRepository productMasterRepository;

    @Autowired
    GeneratorRepository generatorRepository;

    @Autowired
    EBRepository ebRepository;

    @Autowired
    BinStockLogRepository binStockLogRepository;

    @Autowired
    BinstockAuditLogRepository binstockAuditLogRepository;

    @Autowired
    FinishedGooseBinsRepo finishedGooseBinsRepo;

    @Autowired
    MachineRepository machineRepository;

    @Autowired
    BillInwardRepository billInwardRepository;

    @Autowired
    GogroupPalletNumberMapRepository gogroupPalletNumberMapRepository;

    @Autowired
    ASRSPalletDetailsRepository asrsPalletDetailsRepository;


    @Autowired
    WcShiftsRepository wcShiftsRepository;

    // GoDown

    public ResponseEntity<?> saveGoDownDetails(GoDownDto goDownDto, User loggedUser) {
        GoDown goDown = new GoDown(goDownDto);
        goDown.setCreatedBy(loggedUser.getId());
        goDown.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        goDownRepository.save(goDown);
        return new ResponseEntity<Status>(new Status("200", "GoDown save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateGoDownDetails(GoDownDto goDownDto, User loggedUser) {
        GoDown goDown = new GoDown(goDownDto);
        goDown.setUpdatedBy(loggedUser.getId());
        goDown.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        goDownRepository.save(goDown);
        return new ResponseEntity<Status>(new Status("200", "GoDown  updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllGoDown(String machineId) throws UsernameNotFoundException {
        log.debug("listAllGoDown called with machineId {}", machineId);
        List<GoDown> goDowns = new ArrayList<>();
        if (machineId != null) {
            log.debug("listAllGoDown: with for machineId");
            Optional<Machine> machineOptional = machineRepository.findById(Long.valueOf(machineId));
            machineOptional.map(Machine::getPlace).ifPresent(place -> {
                Optional<GoDown> goDownOpt = goDownRepository.findById(Long.valueOf(place));
                goDownOpt.ifPresent(goDowns::add);
            });
        } else {
            log.debug("listAllGoDown: without for machineId");
            List<GoDown> goDownsList = goDownRepository.findAll();
            goDowns.addAll(goDownsList);
        }
        log.debug("listAllGoDown: result for machineId count{}", goDowns.size());
        if (!goDowns.isEmpty()) {
            // updateGoDown(goDownDto); // It's impacting performance, for dropdown it's not
            // recommended.
            List<GoDownDto> result = goDowns.stream().map(GoDownDto::new).collect(Collectors.toList());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<GoDownDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> listAllGoDown(String goDownName, String goDownType, String branch, String status,
                                           Pageable pageable) throws UsernameNotFoundException {

        Page<GoDownList> list = goDownRepository.goDownFilterList(branch, goDownName, goDownType, status, pageable);
        if (!list.isEmpty()) {
            List<GoDownListDto> result = list.toList().stream().map(data -> new GoDownListDto(data))
                    .collect(Collectors.toList());
            PageDataDto<GoDownListDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<GoDownListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<GoDownListDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> listAllGoDown(String goDownName, String goDownType, String branch, String status,Long firmId)
            throws UsernameNotFoundException {

        List<GoDownList> list = goDownRepository.goDownFilterList(branch, goDownName, goDownType, status,firmId);
        if (!list.isEmpty()) {
            List<GoDownListDto> result = list.stream().map(data -> new GoDownListDto(data))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<GoDownListDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<GoDownListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getGoDownDetails(String id) throws UsernameNotFoundException {
        Optional<GoDown> optionalGoDown = goDownRepository.findById(Long.parseLong(id));
        GoDownDto goDownDto = new GoDownDto(optionalGoDown.get());
        updateGoDown(goDownDto);
        return new ResponseEntity<GoDownDto>(goDownDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteGoDownDetails(String id) throws UsernameNotFoundException {
        try {
            Optional<GoDown> optionalGoDown = goDownRepository.findById(Long.parseLong(id));
            if (optionalGoDown.isPresent()) {
                GoDown goDown = optionalGoDown.get();
                goDownRepository.delete(goDown);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public void updateGoDown(GoDownDto goDownDto) {
        if (goDownDto.getBranch() != null) {
            Optional<Branch> optionalBranch = branchRepository.findById(goDownDto.getBranch());
            if (optionalBranch.isPresent()) {
                goDownDto.setBranchName(optionalBranch.get().getBranchName());
            }
        }
        if (goDownDto.getGenSetId() != null) {
            Optional<Generator> generatorOptional = generatorRepository.findById(goDownDto.getGenSetId());
            if (generatorOptional.isPresent()) {
                goDownDto.setGenSetName(generatorOptional.get().getName());
            }
        }
        if (goDownDto.getEbService() != null) {
            Optional<EB> ebOptional = ebRepository.findById(goDownDto.getEbService());
            if (ebOptional.isPresent()) {
                goDownDto.setEbServiceName(ebOptional.get().getServiceName());
            }
        }
    }

    // BinRange

    public ResponseEntity<?> saveBinRangeDetails(BinRangeDto binRangeDto, User loggedUser) {
        // 19-08-2025 validation added by dhivya , discoverd by subhash sir
        boolean existsOverlappingRange = binRangeRepository.existsOverlappingRange(binRangeDto.getSlFm(), binRangeDto.getSlTo());
        if (existsOverlappingRange) {
            return new ResponseEntity<Status>(new Status("409", " This BinRange already exists!!!"), HttpStatus.CONFLICT);
        }
        BinRange binRange = new BinRange(binRangeDto);
        binRange.setBranch(loggedUser.getBranch_id());
        binRange.setCreatedBy(loggedUser.getId());
        binRange.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        binRangeRepository.save(binRange);
        return new ResponseEntity<Status>(new Status("200", "BinRange save successfully!!!"), HttpStatus.OK);

    }

    public ResponseEntity<?> updateBinRangeDetails(BinRangeDto binRangeDto, User loggedUser) {
        BinRange binRange = new BinRange(binRangeDto);
        binRange.setBranch(loggedUser.getBranch_id());
        binRange.setUpdatedBy(loggedUser.getId());
        binRange.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        binRangeRepository.save(binRange);
        return new ResponseEntity<Status>(new Status("200", "BinRange  updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllBinRange(String department, Long binNo, String goDownName, Pageable pageable)
            throws UsernameNotFoundException {

        Page<BinRangeList> list = binRangeRepository.binRangeFilterList(goDownName, department, binNo, pageable);
        if (!list.isEmpty()) {
            List<BinRangeListDto> result = list.toList().stream().map(data -> new BinRangeListDto(data))
                    .collect(Collectors.toList());
            PageDataDto<BinRangeListDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<BinRangeListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<BinRangeListDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getBinRangeDetails(String id) throws UsernameNotFoundException {
        Optional<BinRange> optionalBinRange = binRangeRepository.findById(Long.parseLong(id));
        BinRangeDto binRangeDto = new BinRangeDto(optionalBinRange.get());
        updateBinRange(binRangeDto);
        return new ResponseEntity<BinRangeDto>(binRangeDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteBinRangeDetails(String id) throws UsernameNotFoundException {
        try {
            Optional<BinRange> optionalBinRange = binRangeRepository.findById(Long.parseLong(id));
            if (optionalBinRange.isPresent()) {
                BinRange binRange = optionalBinRange.get();
                binRangeRepository.delete(binRange);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public void updateBinRange(BinRangeDto goDownDto) {
        if (goDownDto.getBranch() != null) {
            Optional<Branch> optionalBranch = branchRepository.findById(goDownDto.getBranch());
            if (optionalBranch.isPresent()) {
                goDownDto.setBranchName(optionalBranch.get().getBranchName());
            }
        }
        Optional<DepartmentDetails> departmentDetailsOptional = detailsRepository.findById(goDownDto.getDept());
        if (departmentDetailsOptional.isPresent()) {
            goDownDto.setDeptName(departmentDetailsOptional.get().getName());
        }
    }


    public ResponseEntity<?> findByBinFilterList(Long slfm, Long slto) throws UsernameNotFoundException {
        List<BinRange> list = binRangeRepository.findByBinFilterList(slfm, slto);
        if (!list.isEmpty()) {
            List<BinRangeDto> result = list.stream().map(data -> new BinRangeDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<BinRangeDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BinRangeDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    // GoGroup

    public ResponseEntity<?> listAllBinList(Long branch, Long dept, String goDownName, String firmName,
                                            Pageable pageable) throws UsernameNotFoundException {
        Page<BinList> list = binRangeRepository.binFilterList(branch, dept, goDownName, firmName, pageable);
        if (!list.toList().isEmpty()) {
            List<BinListDto> result = list.toList().stream().map(data -> {
                BinListDto binListDto = new BinListDto(data);
                Integer allocatedBinCount = goGroupRepository.findAllocatedBinCount(branch, dept,
                        Long.valueOf(binListDto.getFromBin()), Long.valueOf(binListDto.getToBin()));
                binListDto.setAllocatedBin(allocatedBinCount);
                Long totalCount = Long.valueOf(binListDto.getToBin()) - Long.valueOf(binListDto.getFromBin())+1;
                Long pendingCount = totalCount - allocatedBinCount;
                binListDto.setPendingBin(pendingCount);
                return binListDto;
            }).collect(Collectors.toList());
            PageDataDto<BinListDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<BinListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<BinListDto>>(new PageDataDto<>(), HttpStatus.OK);
        }
    }

    @Transactional
    public ResponseEntity<?> saveGoGroupDetails(List<GoGroupDto> goGroupDtoList, User loggedUser) {

        for (GoGroupDto goGroupDto : goGroupDtoList) {
            Optional<GoGroup> goGroupOptional = goGroupRepository.findByBinNo(goGroupDto.getGoName());
            if (goGroupOptional.isPresent()) {
            	 GoGroup existingBin = goGroupOptional.get();            	   
            	 Optional<GoDown> godown = goDownRepository.findById(existingBin.getGid());            	
            	 throw new RuntimeException("Error: bin no: " +existingBin.getGoName()+ " is already taken for dept: "+existingBin.getDept()+" and godown :  "+ godown.map(GoDown::getName).orElse(""));
            }
            String gcode = goGroupRepository.findByDescOne();
            gcode = gcode.replace("Zg", "");
            gcode = gcode.replace("zg", "");
            Long lgcode = Long.valueOf(gcode) + 1;
            String gCodeF = "zg" + lgcode;
            GoGroup goGroup = new GoGroup(goGroupDto);
            goGroup.setGCode(gCodeF);
            goGroup.setBranch(loggedUser.getBranch_id());
            goGroup.setDept(loggedUser.getDepartment().getId());
            savePalletMap(goGroupDto);
            goGroupRepository.save(goGroup);
        }
        return new ResponseEntity<Status>(new Status("200", "Bin created  successfully!!!"), HttpStatus.OK);

    }

    public void savePalletMap(GoGroupDto goGroupDto) {
        if (goGroupDto.getPalletDetails() != null && !goGroupDto.getPalletDetails().isEmpty()) {
//       LINSE COMMENDED BY DHIVYA (LOADING TIME) ALAGURAJA SIR SAID NEED SEPARATE API 
        	
//            for (PalletDto palletDto : goGroupDto.getPalletDetails()) {
//                boolean exists = gogroupPalletNumberMapRepository.existsByPalletNumber(palletDto.getPalletNumber());
//                if (exists) {
//                    throw new RuntimeException("Pallet Number '" + palletDto.getPalletNumber() + "' is already taken!");
//                }
//            }
            List<GogroupPalletNumberMap> palletEntities = goGroupDto.getPalletDetails().stream()
                    .map(dto -> {
                        GogroupPalletNumberMap entity = new GogroupPalletNumberMap(dto);
                        entity.setBinId(goGroupDto.getGoName());
                        return entity;
                    })
                    .collect(Collectors.toList());

            gogroupPalletNumberMapRepository.saveAll(palletEntities);
        }
    }


    public ResponseEntity<?> getPalletTye(String palletNumber, User loggedUser) {
        Optional<GogroupPalletNumberMap> palletNumberMap = gogroupPalletNumberMapRepository.findByPalletNumber(palletNumber, loggedUser.getDepartment().getId());
        if (palletNumberMap != null && palletNumberMap.isPresent()) {
            PalletDto palletDto = new PalletDto(palletNumberMap.get());
            return new ResponseEntity<>(palletDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("404", "Not Found PalletNumber Or Not Belong To User Dept"), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> getGoGroupDetails(Long godownId, Long firmId, Long branch, Long dept, Long slfm, Long slto)
            throws UsernameNotFoundException {
        Map<String, Object> stringObjectsMap = new HashMap<>();
        List<GoGroupList> list = goGroupRepository.getViewList(godownId, firmId, branch, dept, slfm, slto);
        if (!list.isEmpty()) {
            List<GoGroupListDto> result = list.stream().map(data -> {
                GoGroupListDto goGroupDto = new GoGroupListDto(data);
                return goGroupDto;
            }).collect(Collectors.toList());
            stringObjectsMap.put("allottedBin", result);
            Long r = slto - (slfm - 1);
            Long i = r - Long.valueOf(result.size());
            stringObjectsMap.put("pendingBinCount", i);
            return new ResponseEntity<Map<String, Object>>(stringObjectsMap, HttpStatus.OK);
        } else {
            stringObjectsMap.put("allottedBin", "");
            Long r = slto - (slfm - 1);
            stringObjectsMap.put("pendingBinCount", r);
            return new ResponseEntity<Map<String, Object>>(stringObjectsMap, HttpStatus.OK);
        }
    }

    public ResponseEntity<?> updateGoGroupDetails(GoGroupDto goGroupDto, User loggedUser) {
        GoGroup goGroup = new GoGroup(goGroupDto);
        goGroupRepository.save(goGroup);
        return new ResponseEntity<Status>(new Status("200", "GoGroup  updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> findBinNo(String binType, Long binNo, Long branch, Long dept, Long godownId) throws UsernameNotFoundException {
        List<Long> list = goGroupRepository.getFindBinList(binType, binNo, branch, dept, godownId);
        if (!list.isEmpty()) {
            return new ResponseEntity<List<Long>>(list, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<Long>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> listAllGoGroup() throws UsernameNotFoundException {

        List<GoGroup> list = goGroupRepository.findAll();
        if (!list.isEmpty()) {
            List<GoGroupDto> result = list.stream().map(data -> {
                GoGroupDto goGroupDto = new GoGroupDto(data);
                return goGroupDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<GoGroupDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BinRangeDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> deleteGoGroupDetails(String id) throws UsernameNotFoundException {
        try {
            Optional<GoGroup> optionalGoGroup = goGroupRepository.findById(Long.parseLong(id));
            if (optionalGoGroup.isPresent()) {
                GoGroup goGroup = optionalGoGroup.get();
                goGroupRepository.delete(goGroup);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    // BinStock

    public ResponseEntity<?> saveBinStockDetails(BinStockDto binStockDto, User loggedUser) {
        BinStock binStock = new BinStock(binStockDto);
        binStockRepository.save(binStock);
        return new ResponseEntity<Status>(new Status("200", "BinStock save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateBinStockDetails(BinStockDto goGroupDto, User loggedUser) {
        BinStock binStock = new BinStock(goGroupDto);
        binStockRepository.save(binStock);
        return new ResponseEntity<Status>(new Status("200", "BinStock  updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllBinStock() throws UsernameNotFoundException {

        List<BinStock> list = binStockRepository.findAll();
        if (!list.isEmpty()) {
            List<BinStockDto> result = list.stream().map(data -> {
                BinStockDto binStockDto = new BinStockDto(data);
                updateBinStock(binStockDto);
                return binStockDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<BinStockDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BinStockDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getBinStockDetails(String id) throws UsernameNotFoundException {
        Optional<BinStock> optionalBinStock = binStockRepository.findById(Long.parseLong(id));
        BinStockDto binStockDto = new BinStockDto(optionalBinStock.get());
        updateBinStock(binStockDto);
        return new ResponseEntity<BinStockDto>(binStockDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteBinStockDetails(String id) throws UsernameNotFoundException {
        try {
            Optional<BinStock> optionalBinStock = binStockRepository.findById(Long.parseLong(id));
            if (optionalBinStock.isPresent()) {
                BinStock binStock = optionalBinStock.get();
                binStockRepository.delete(binStock);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }


    public ResponseEntity<?> listAllBinStock(Long binStockId, Long firmId, Long branchId, Long deptId, Long productId, Long batchId,
                                             String batchNo, Double lot, Long binName) throws UsernameNotFoundException {

        List<BinStock> list = binStockRepository.filterReqBinStockList(binStockId, firmId, branchId, deptId, productId, batchId, batchNo, lot, binName);
        if (!list.isEmpty()) {
            List<BinStockDto> result = list.stream().map(data -> {
                BinStockDto binStockDto = new BinStockDto(data);
                updateBinStock(binStockDto);
                return binStockDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<BinStockDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BinStockDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> listAllBinStockSearch(Long branchId, Long deptId, Long productId, Long batchId,
                                                   String batchNo, Double lot) throws UsernameNotFoundException {

        List<BinStock> list = binStockRepository.filterBillBinStockList(branchId, deptId, productId, batchId, batchNo, lot);
        if (!list.isEmpty()) {
            List<BinStockDto> result = list.stream().map(data -> {
                BinStockDto binStockDto = new BinStockDto(data);
                updateBinStock(binStockDto);
                return binStockDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<BinStockDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BinStockDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> getBinNoList(Long branchId, Long deptId, Long productId, String binNo, Double lot) throws UsernameNotFoundException {

        List<Long> list = binStockRepository.filterBinNoList(branchId, deptId, productId, binNo, lot);
        if (!list.isEmpty()) {
            return new ResponseEntity<List<Long>>(list, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<Long>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> listAllBinStockSearchGroupByBranchDept(Long firmId, Long branchId, Long deptId, Long productId,
                                                                    Long batchId, String batchNo, Double lot) throws UsernameNotFoundException {
        List<BinStockQuantity> list = binStockRepository.filterBillBinStockListGroupByBranchDept(firmId, branchId, deptId, productId, batchId, batchNo, lot);
        if (!list.isEmpty()) {
            List<BinStockQuantityDto> result = list.stream().map(data -> {
                BinStockQuantityDto binStockDto = new BinStockQuantityDto();
                binStockDto.setQuantity(data.getQuantity());
                binStockDto.setDept(data.getDept());
                binStockDto.setBranch(data.getBranch());
                updateBinStock(binStockDto, productId);
                return binStockDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<BinStockQuantityDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BinStockQuantityDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> listAllBinStocklist(Long branchId, Long deptId, Long productId) throws UsernameNotFoundException {

        List<BinStockListDto> list = binStockRepository.filterBinStockList(branchId, deptId, productId);
        if (!list.isEmpty()) {
            List<BinStockFilterListDto> result = list.stream().map(data -> {
                BinStockFilterListDto binStockDto = new BinStockFilterListDto(data);
                return binStockDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<BinStockFilterListDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BinStockFilterListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> filterBinStockWithPagination(Long branchId, Long deptId, Long productId, Long batchId,
                                                          String batchNo, Pageable pageable) throws UsernameNotFoundException {
        Page<BinStock> list = binStockRepository.filterBillBinStockList(branchId, deptId, productId, batchId, batchNo, pageable);
        if (!list.isEmpty()) {
            List<BinStockDto> result = list.stream().map(data -> {
                BinStockDto binStockDto = new BinStockDto(data);
                updateBinStock(binStockDto);
                return binStockDto;
            }).collect(Collectors.toList());
            PageDataDto<BinStockDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<BinStockDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<BinStockDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> listAllQCBinList(Long branchId, Long binNo) throws UsernameNotFoundException {

        List<Long> list = binStockRepository.filterBillBinList(branchId, String.valueOf(binNo));
        if (!list.isEmpty()) {
            return new ResponseEntity<List<Long>>(list, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<Long>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> listAllDeptWiseBinList(Long branchId, Long deptId, String type) throws UsernameNotFoundException {

        List<String> list = binStockRepository.filterBillBinListDeptWise(branchId, deptId, type);
        if (!list.isEmpty()) {
            return new ResponseEntity<List<String>>(list, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<String>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> asrsListAllDeptWiseBinList(boolean asrs, String trnNo, Long branchId, Long deptId, String type) throws UsernameNotFoundException {
        Map<String, Object> response = new HashMap<>();
        try {
            double dominoCount = 0.0;
            com.aria.model.Wcshift wcshift = wcShiftsRepository.findByTrnNo(trnNo).orElse(null);
            if (wcshift != null) {
                Long domino = wcshift.getDominoCount();
                dominoCount = Double.valueOf(domino);
            }
            double asrsQty = asrsPalletDetailsRepository.asrsAllocatedQty(trnNo);

            response.put("dominoCount", dominoCount);
            response.put("asrsQty", asrsQty);
            if (dominoCount > asrsQty) {
                response.put("message", "ASRS allocated quantity is less than Domino count");
                response.put("success", false);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else if (dominoCount < asrsQty) {
                response.put("message", "ASRS allocated quantity is greater than Domino count");
                response.put("success", false);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else if (dominoCount == asrsQty) {
                // proceed
                List<AsrsBinList> list = asrsPalletDetailsRepository.asrsAllocatedBins(branchId, deptId, type, trnNo);

                if (!list.isEmpty()) {
                    List<Map<String, Object>> finalList = new ArrayList<>();
                    for (AsrsBinList asrsBin : list) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("binNo", asrsBin.getBinNo());
                        map.put("qty", asrsBin.getQty());
                        finalList.add(map);
                    }
                    response.put("data", finalList);
                    response.put("message", "ASRS allocated quantity matches Domino count");
                    response.put("success", true);
                    return new ResponseEntity<>(response, HttpStatus.OK);
                } else {
                    response.put("data", new ArrayList<>());
                    response.put("message", "No ASRS allocated bins found");
                    response.put("success", false);
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }

            } else {
                response.put("message", "Unable to compare quantities");
                response.put("success", false);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        } catch (Exception e) {
            logger.error("Error in asrsListAllDeptWiseBinList: ", e);
            response.put("message", "An error occurred while processing the request");
            response.put("success", false);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public ResponseEntity<?> listAllDeptWiseBinWithStockList(Long branchId, Long deptId, String type) throws UsernameNotFoundException {
        List<String> list = binStockRepository.listAllDeptWiseBinWithStockList(branchId, deptId, type);
        if (!list.isEmpty()) {
            return new ResponseEntity<List<String>>(list, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<String>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getBinDetailsForBinNo(String binId) throws UsernameNotFoundException {
        BinGoGroupList goGroup = binStockRepository.getBinStkDetailsForBinNo(Long.valueOf(binId));
        if (Objects.nonNull(goGroup)) {
            BinGoGroupDto binGoGroupDto = new BinGoGroupDto(goGroup);
            List<BinStock> list = binStockRepository.findBybinNumber(Long.valueOf(binId));
            for (BinStock binStock : list) {
                BinProduct binProduct = new BinProduct();
                binProduct.setQuantity(binStock.getStkQty());
                binProduct.setBatchNo(binStock.getBatchNo());
                binProduct.setProductName(binStock.getBatchNo());
                binProduct.setInwardDate(
                        binStock.getInwarDate() != null ? dateUtil.getStringTODateF1(binStock.getInwarDate()) : null);
                Optional<ProductMaster> optionalProductMaster = productMasterRepository.findById(binStock.getRefId());
                if (optionalProductMaster.isPresent()) {
                    binProduct.setProductName(optionalProductMaster.get().getProductName());
                }
                binGoGroupDto.getProductList().add(binProduct);
            }
            return new ResponseEntity<BinGoGroupDto>(binGoGroupDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<BinGoGroupDto>(new BinGoGroupDto(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> dpBinANDStock(Long branchId) throws UsernameNotFoundException {

        List<FindBinDetails> list = binStockRepository.dpBinANDStock(branchId);
        if (!list.isEmpty()) {
            List<FindBinDetailsDTO> result = list.stream().map(data -> new FindBinDetailsDTO(data))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<FindBinDetailsDTO>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<FindBinDetailsDTO>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public void updateBinStock(BinStockDto binStockDto) {
        Optional<ProductMaster> optionalProductMaster = productMasterRepository.findById(binStockDto.getProductId());
        if (optionalProductMaster.isPresent()) {
            binStockDto.setProductName(optionalProductMaster.get().getProductName());
        }
        if (binStockDto.getBranch() != null && binStockDto.getBranch() > 0) {
            Optional<Branch> optionalBranch = branchRepository.findById(binStockDto.getBranch());
            if (optionalBranch.isPresent()) {
                binStockDto.setBranchName(optionalBranch.get().getBranchName());
            }
        }
        if (binStockDto.getDept() != null && binStockDto.getDept() > 0) {
            Optional<DepartmentDetails> departmentDetailsOptional = detailsRepository.findById(binStockDto.getDept());
            if (departmentDetailsOptional.isPresent()) {
                binStockDto.setDeptName(departmentDetailsOptional.get().getName());
            }
        }

        List<GogroupPalletNumberMap> gogroupPalletNumberMaps = gogroupPalletNumberMapRepository.findByBinId(binStockDto.getBinName(), binStockDto.getProductId());
        if (!gogroupPalletNumberMaps.isEmpty()) {
            binStockDto.setPalletNumbers(gogroupPalletNumberMaps);
        }
    }

    public void updateBinStock(BinStockQuantityDto binStockDto, Long productId) {
        Optional<ProductMaster> optionalProductMaster = productMasterRepository.findById(productId);
        if (optionalProductMaster.isPresent()) {
            binStockDto.setProductName(optionalProductMaster.get().getProductName());
        }
        Optional<Branch> optionalBranch = branchRepository.findById(binStockDto.getBranch());
        if (optionalBranch.isPresent()) {
            binStockDto.setBranchName(optionalBranch.get().getBranchName());
        }
        if (binStockDto.getDept() != null) {
            Optional<DepartmentDetails> departmentDetailsOptional = detailsRepository.findById(binStockDto.getDept());
            if (departmentDetailsOptional.isPresent()) {
                binStockDto.setDeptName(departmentDetailsOptional.get().getName());
            }
        }
    }

    // binLock

    public ResponseEntity<?> saveBinLock(BinLockRequestDto binLockDto, User loggedUser) {

        BinLock rs = new BinLock(binLockDto);
        rs.setLockedEmployee(loggedUser);
        if (binLockDto.getLocked()) {
            rs.setLockStatus("Lock");
        } else {
            rs.setLockStatus("UnLock");
        }
        if (binLockDto.getLockDate() != null) {
            rs.setLockDate(dateUtil.getStringTODateWk(binLockDto.getLockDate()));
        } else {
            rs.setLockDate(dateUtil.getISTDate());
        }
        rs.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        rs.setCreatedBy(loggedUser.getId());
        BinLock binLock = binLockRepository.save(rs);

        if (binLock.getLockStatus().equals("Lock")) {
            binStockRepository.updateLockStatus(Long.parseLong(binLockDto.getBinNo()), "Y", binLock.getLockType());
            binStockRepository.updateWaitingForResultStatus(Long.parseLong(binLockDto.getBinNo()), "Y", "WR");
        } else {
            binStockRepository.updateLockStatus(Long.parseLong(binLockDto.getBinNo()), "N", null);
            binStockRepository.updateWaitingForResultStatus(Long.parseLong(binLockDto.getBinNo()), "Y", "WR");
        }
        /*
        List<BinStock> updateLockStk = new ArrayList<BinStock>();
        List<BinLock> updateLock = new ArrayList<BinLock>();
        List<BinStock> binlist = binStockRepository.findBybinNumber(Long.parseLong(binLockDto.getBinNo()));
        for (BinStock binStk : binlist) {

             updateBinLock(binLock, binStk);
            if (binLock.getLockStatus().equals("Lock")) {
                binStk.setLockStatus("Y");
                String category = binStk.getCategory();

                if (Objects.nonNull(binLock.getLockType())) {
                    if (category != null && category.equalsIgnoreCase("WaitingForResult")) {
                        binStk.setLockType("WR");
                    } else {
                        binStk.setLockType(binLock.getLockType());
                    }
                } else {
                    binStk.setLockType("");
                }
            } else {
                String category = binStk.getCategory();
                if (category != null && category.equalsIgnoreCase("WaitingForResult")) {
                    binStk.setLockStatus("Y");
                    binStk.setLockType("WR");
                } else {
                    binStk.setLockStatus("N");
                    binStk.setLockType("");
                }
            }
            binStockRepository.save(binStk);
            updateLockStk.add(binStk);

        }
        binStockRepository.saveAll(updateLockStk);
        binLockRepository.saveAll(updateLock);
        */


        BinStatus binStatus = new BinStatus();
        binStatus.setBinName(Long.parseLong(binLockDto.getBinNo()));
        binStatus.setLocked(binLockDto.getLocked());
        binStatus.setLocktype(binLockDto.getLockType());
        binStatus.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        binStatus.setUpdatedBy(loggedUser.getId());

        binStatusRepository.save(binStatus);

        if (binLockDto.getLocked()) {
            return new ResponseEntity<Status>(new Status("200", "bin locked  successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("200", "bin unlocked  successfully!!!"), HttpStatus.OK);
        }
    }

    private void updateBinLock(BinLock binLock, BinStock binLockRequestBinDetailDto) {

        if (binLockRequestBinDetailDto.getRefId() != null) {
            ProductMaster product = new ProductMaster();
            product.setId(binLockRequestBinDetailDto.getRefId());
            binLock.setProduct(product);
        }

        binLock.setBatchNo(binLockRequestBinDetailDto.getBatchNo());
        binLock.setBatchId(binLockRequestBinDetailDto.getBatchId());
        binLock.setInwardDate(binLockRequestBinDetailDto.getInwarDate());

        binLock.setState(binLockRequestBinDetailDto.getLocation());
        binLock.setQuantity(binLockRequestBinDetailDto.getStkQty());
    }

    public ResponseEntity<?> listBinLockAll() throws UsernameNotFoundException {

        List<BinLock> list = binLockRepository.findAll();
        if (!list.isEmpty()) {
            List<BinLockDto> result = list.stream().map(data -> {
                BinLockDto binLockDto = new BinLockDto(data);
                return binLockDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<BinLockDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BinLockDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getBinLockDetails(String id) throws UsernameNotFoundException {
        Optional<BinLock> optionalBinLock = binLockRepository.findById(Long.parseLong(id));
        BinLockDto binLockDto = new BinLockDto(optionalBinLock.get());
        return new ResponseEntity<>(binLockDto, HttpStatus.OK);
    }


    public ResponseEntity<?> filterBinLock(Long productId, String binNo, String batchNo, String lockType, String status,
                                           String dateFrom, String dateTo, Pageable pageable) throws UsernameNotFoundException {

        Page<BinLock> list = binLockRepository.filterBillBinLockList(productId, binNo, batchNo, lockType, status,
                pageable);
        if (list != null && !list.getContent().isEmpty()) {
            List<BinLockDto> result = list.getContent().stream().map(data -> {
                BinLockDto binStockDto = new BinLockDto(data);
                return binStockDto;
            }).collect(Collectors.toList());
            PageDataDto<BinLockDto> pageData = new PageDataDto<>(result, pageable, list);
            return new ResponseEntity<PageDataDto<BinLockDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BinStockDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getStockByBranch(Long firmId, Long productId, String lockType, String status, Double lot)
            throws UsernameNotFoundException {

        List<StockByBranch> list = binStockRepository.getStockByBranch(firmId, productId, lockType, status, lot);

        List<StockByBranchDTO> stockByBranchDTOs = list.stream().map(data -> new StockByBranchDTO(data))
                .collect(Collectors.toList());

        return new ResponseEntity<List<StockByBranchDTO>>(stockByBranchDTOs, HttpStatus.OK);

    }


    public ResponseEntity<?> filterPeekOfStock(Long branchId, Long deptId, Long productId, String type) throws UsernameNotFoundException {

        Long product_id = productId;

        Map<String, Object> stringObjectMap = new HashMap<>();

        Map<String, Object> OverAllObjectMap = new HashMap<>();


        try {

            Double currentStock = binStockRepository.getCurrentSock(product_id);
            OverAllObjectMap.put("currentStock", currentStock);

            List<PlantWiseStock> currentPlantWiseStock = binStockRepository.getBranchWithDeptCurrentStock(product_id);
            OverAllObjectMap.put("currentPlantWiseStock", getData(currentPlantWiseStock));

            Double conceptionStock = binStockLogRepository.getConceptionStock(product_id);
            OverAllObjectMap.put("conceptionStock", conceptionStock);

            List<PlantWiseStock> conceptionPlantWiseStock = binStockLogRepository.getConceptionStock(product_id, "I");
            OverAllObjectMap.put("conceptionPlantWiseStock", getData(conceptionPlantWiseStock));


            stringObjectMap.put("OverAllStock", OverAllObjectMap);

            if (type != null && type.equalsIgnoreCase("Month")) {
                // Last three months Stock
                Map<String, String> lastThreeMonthDate = dateUtil.getMonthOfStartAndEndDate(3);
                Map<String, Object> last3MonthStringObjectMap = new HashMap<>();
                for (String monthName : lastThreeMonthDate.keySet()) {

                    Map<String, Object> monthStockValue = new HashMap<>();
                    String dateValue = lastThreeMonthDate.get(monthName);

                    List<String> dates = Arrays.asList(dateValue.split("/"));

                    Date startDate = dateUtil.getStringTODateYYYYMMDD(dates.get(0));
                    Date endDate = dateUtil.getStringTODateYYYYMMDD(dates.get(1));
                    List<PlantWiseStock> monthStock = binStockLogRepository.getMonthConceptionOrInwardStock(product_id, "R", startDate, endDate);
                    monthStockValue.put(monthName, monthStock);
                    List<PlantWiseStock> monthConceptionStock = binStockLogRepository.getMonthConceptionOrInwardStock(product_id, "I", startDate, endDate);
                    monthStockValue.put(monthName + "-ConceptionStock", getData(monthConceptionStock));
                    last3MonthStringObjectMap.put(monthName, monthStockValue);
                }

                stringObjectMap.put("lastThreeMonthStock", last3MonthStringObjectMap);

            } else if (type != null && type.equalsIgnoreCase("Year")) {

                // Last One year Stock
                Map<String, String> last1YearDate = dateUtil.getMonthOfStartAndEndDate(12);

                Map<String, Double> last1YearStringIntegerHashMap = new HashMap<>();

                for (String monthName : last1YearDate.keySet()) {

                    String dateValue = last1YearDate.get(monthName);

                    List<String> dates = Arrays.asList(dateValue.split("/"));

                    Date startDate = dateUtil.getStringTODateYYYYMMDD(dates.get(0));
                    Date endDate = dateUtil.getStringTODateYYYYMMDD(dates.get(1));
                    Double monthStock = binStockLogRepository.getMonthSock(productId, startDate, endDate);
                    last1YearStringIntegerHashMap.put(monthName, monthStock);
                }

                // Last One year Stock top three months

                if (!last1YearStringIntegerHashMap.isEmpty()) {
                    Map<String, Object> last1YearTopThreeMonthsStringObjectMap = new HashMap<>();
                    int j = 3;
                    List<String> keys = last1YearStringIntegerHashMap.entrySet().stream()
                            .sorted(Map.Entry.<String, Double>comparingByValue().reversed()).limit(3).map(Map.Entry::getKey)
                            .collect(Collectors.toList());

                    for (String monthName : keys) {
                        Map<String, Object> monthStockValue = new HashMap<>();
                        String dateValue = last1YearDate.get(monthName);
                        List<String> dates = Arrays.asList(dateValue.split("/"));

                        Date startDate = dateUtil.getStringTODateYYYYMMDD(dates.get(0));
                        Date endDate = dateUtil.getStringTODateYYYYMMDD(dates.get(1));
                        List<PlantWiseStock> monthStock = binStockLogRepository.getMonthConceptionOrInwardStock(productId,
                                "R", startDate, endDate);
                        monthStockValue.put(monthName, monthStock);
                        List<PlantWiseStock> monthConceptionStock = binStockLogRepository
                                .getMonthConceptionOrInwardStock(productId, "I", startDate, endDate);

                        monthStockValue.put(monthName + "-ConceptionStock", getData(monthConceptionStock));
                        last1YearTopThreeMonthsStringObjectMap.put(monthName, monthStockValue);
                    }

                    stringObjectMap.put("last1YearTopThreeMonths", last1YearTopThreeMonthsStringObjectMap);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            stringObjectMap.put("OverAllStock", OverAllObjectMap);
        }

        return new ResponseEntity<Map<String, Object>>(stringObjectMap, HttpStatus.OK);
    }


    public List<PlantWiseStockDto> getData(List<PlantWiseStock> plantWiseStocks) {

        List<PlantWiseStockDto> list = new ArrayList<>();

        if (plantWiseStocks != null && !plantWiseStocks.isEmpty()) {
            list = plantWiseStocks.stream()
                    .map(data -> new PlantWiseStockDto(data)).collect(Collectors.toList());
        }

        return list;
    }

    public ResponseEntity<?> finishedGooseBinsList(Long binId, Long branchId, Long productId, String type, String name) throws UsernameNotFoundException {
        List<FinishedGooseBins> list = finishedGooseBinsRepo.finishedGooseBinsList(binId, branchId, productId, type, name);
        List<FinishedGooseBinsDto> result = list.stream().map(data -> {
            FinishedGooseBinsDto stockLedgerDto = new FinishedGooseBinsDto(data);
            return stockLedgerDto;
        }).collect(Collectors.toList());
        return new ResponseEntity<List<FinishedGooseBinsDto>>(result, HttpStatus.OK);
    }

    public ResponseEntity<?> finishedGooseBinsList(boolean asrs, String trnNo, Long binId, Long branchId, Long productId, String type, String name) throws UsernameNotFoundException {

        logger.info("Inside finishedGooseBinsList Asrs with params - asrs: {}, trnNo: {}, binId: {}, branchId: {}, productId: {}, type: {}, name: {}",
                asrs, trnNo, binId, branchId, productId, type, name);
        try {
            if (asrs && trnNo != null) {
                List<FinishedGooseBins> list = finishedGooseBinsRepo.asrsFinishedGooseBinsList(binId, branchId, productId, type, name);
                List<FinishedGooseBinsDto> result = list.stream().map(data -> {
                    FinishedGooseBinsDto stockLedgerDto = new FinishedGooseBinsDto(data);
                    if (asrs && trnNo != null) {
                        Double qty = asrsPalletDetailsRepository.allocatedQty(trnNo, data.getBinname());
                        if (qty != null) {
                            stockLedgerDto.setQty(qty);
                        }
                    }
                    return stockLedgerDto;
                }).collect(Collectors.toList());
                return new ResponseEntity<>(result, HttpStatus.OK);
            } else {
                logger.info("Fetching finished goose bins without ASRS allocation.");
                List<FinishedGooseBins> list = finishedGooseBinsRepo.finishedGooseBinsList(binId, branchId, productId, type, name);
                List<FinishedGooseBinsDto> result = list.stream().map(data -> {
                    FinishedGooseBinsDto stockLedgerDto = new FinishedGooseBinsDto(data);
                    return stockLedgerDto;
                }).collect(Collectors.toList());
                return new ResponseEntity<>(result, HttpStatus.OK);
            }

        } catch (Exception e) {
            logger.error("Error in finishedGooseBinsList Asrs:", e);
            return new ResponseEntity<>(new Status("500", "Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ResponseEntity<?> getBinLotWiseStock(Long productId, Long binNo, Double lot)
            throws UsernameNotFoundException {
        List<BinLotWiseStock> list = binStockRepository.getBinLotWiseStock(productId, binNo, lot);
        List<BinLotWiseStockDto> result = list.stream().map(data -> {
            BinLotWiseStockDto stockLedgerDto = new BinLotWiseStockDto(data);
            return stockLedgerDto;
        }).collect(Collectors.toList());
        return new ResponseEntity<List<BinLotWiseStockDto>>(result, HttpStatus.OK);
    }

    public ResponseEntity<?> getBinWiseLot(Long productId, Long binNo, Double lot, Long branch, Long dept)
            throws UsernameNotFoundException {
        List<BinLotWiseStock> list = binStockRepository.getBinWiseLot(productId, binNo, lot, branch, dept);
        List<BinLotWiseStockDto> result = list.stream().map(data -> {
            BinLotWiseStockDto stockLedgerDto = new BinLotWiseStockDto(data);
            return stockLedgerDto;
        }).collect(Collectors.toList());
        return new ResponseEntity<List<BinLotWiseStockDto>>(result, HttpStatus.OK);
    }

    public ResponseEntity<?> getBinWiseSummary(Long branchId, Long deptId, Long productId, String productName,
                                               String productCategory, String binNo, Double lot, Pageable pageable) throws UsernameNotFoundException {

        Page<BinWiseStockList> list = binStockRepository.findBy(branchId, deptId, productId, productName, productCategory, binNo, lot, pageable);
        if (!list.isEmpty()) {
            List<BinWiseStockListDto> result = list.stream().map(data -> {
                BinWiseStockListDto binStockDto = new BinWiseStockListDto(data);
                return binStockDto;
            }).collect(Collectors.toList());
            PageDataDto<BinWiseStockListDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<BinWiseStockListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<BinWiseStockListDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> stockItemWiseLotList(Long productId) throws UsernameNotFoundException {

        List<ProductLotWiseStockList> list = binStockRepository.findBy(productId);
        if (!list.isEmpty()) {
            List<ProductLotWiseStockListDto> result = list.stream().map(data -> new ProductLotWiseStockListDto(data))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<ProductLotWiseStockListDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ProductLotWiseStockListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> listAllBinStockByProductId(Long productId) {

        List<BinStockListDto> binList = binStockRepository.filterBinStockByProductId(productId);
        List<BinStockFilterListDto> list = binList.stream().map(data -> new BinStockFilterListDto(data))
                .collect(Collectors.toList());
        return new ResponseEntity<List<BinStockFilterListDto>>(list, HttpStatus.OK);
    }

    public ResponseEntity<?> listAllBinStockData(Long branchId, Long deptId, Long productId, Long batchId,
                                                 String batchNo, Double lot, Long gid, Long binNo) {
        List<BinStock> list = binStockRepository.filterBinStockData(branchId, deptId, productId, batchId, batchNo, lot, gid, binNo);
        if (!list.isEmpty()) {
            List<BinStockDto> result = list.stream().map(data -> {
                BinStockDto binStockDto = new BinStockDto(data);
                updateBinStock(binStockDto);
                return binStockDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<BinStockDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BinStockDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> filterBinStockOnlyBill(Long branchId, Long deptId, Long productId, Long batchId,
                                                    String batchNo, Double lot) throws UsernameNotFoundException {

        List<BinStock> list = binStockRepository.filterBinStockOnlyBill(branchId, deptId, productId, batchId, batchNo, lot);
        if (!list.isEmpty()) {
            List<BinStockDto> result = list.stream().map(data -> {
                BinStockDto binStockDto = new BinStockDto(data);
                updateBinStock(binStockDto);
                return binStockDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<BinStockDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BinStockDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> getSplitBinLotWiseStock(Long productId, Long binNo, Double lot, String palletNo) throws UsernameNotFoundException {
        logger.info("Fetching split bin lot wise stock for Product ID: {}, Bin No: {}, Lot: {}, Pallet No: {}",
                productId, binNo, lot, palletNo);
        List<SplitBinLotWiseStock> list = null;
        try {
            if (palletNo != null && !palletNo.isEmpty()) {
                list = binStockRepository.getBinLotWiseStockSplitWithPalletNo(productId, binNo, palletNo, lot);
            } else {
                list = binStockRepository.getBinLotWiseStockSplit(productId, binNo, lot);
            }
            List<SplitBinLotWiseStockDTO> result = list.stream().map(data -> {
                SplitBinLotWiseStockDTO stockLedgerDto = new SplitBinLotWiseStockDTO(data);
                return stockLedgerDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<SplitBinLotWiseStockDTO>>(result, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    public ResponseEntity<?> filterBinStockOnlyDelite(String binStockIds, Long branchId, Long deptId, Long productId, Long batchId,
                                                      String batchNo, Double lot) throws UsernameNotFoundException {

        List<Long> binStockId = Arrays.asList(binStockIds.split("\\,")).stream().map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
        List<BinStock> list = binStockRepository.filterBinStockOnlyDelite(binStockId, branchId, deptId, productId, batchId, batchNo, lot);
        if (!list.isEmpty()) {
            List<BinStockDto> result = list.stream().map(data -> {
                BinStockDto binStockDto = new BinStockDto(data);
                updateBinStock(binStockDto);
                return binStockDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<BinStockDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BinStockDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> filterFindBy(Long binName) throws UsernameNotFoundException {

        List<BinLockBiNoDetails> list = binStockRepository.filterFindBy(binName);
        if (!list.isEmpty()) {
            List<BinLockBiNoDetailList> result = list.stream().map(data -> {
                BinLockBiNoDetailList binStockDto = new BinLockBiNoDetailList(data);
                return binStockDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<BinLockBiNoDetailList>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BinLockBiNoDetailList>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> findBinNoWithLockType(String binType, Long binNo, Long branch, Long dept) throws UsernameNotFoundException {
        List<BinNoWithLockTypeList> list = goGroupRepository.findBinNoWithLockType(binType, binNo, branch, dept);
        if (!list.isEmpty()) {
            List<BinNoWithLockType> result = list.stream().map(data -> {
                BinNoWithLockType binStockDto = new BinNoWithLockType(data);
                return binStockDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<BinNoWithLockType>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BinNoWithLockType>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> getBinStockWiseSummary(Long branchId, Long deptId, Long productId, String productName,
                                                    String productCategory, Long binNo, Double lot, Pageable pageable) throws UsernameNotFoundException {

        List<BinQtyWiseStockList> list = binStockRepository.findByBinQtyWiseStock(branchId, deptId, productId, productName, productCategory, binNo, lot);
        if (!list.isEmpty()) {
            List<BinQtyWiseStockDTO> result = list.stream().map(data -> {
                BinQtyWiseStockDTO binStockDto = new BinQtyWiseStockDTO(data);
                Optional<BillInward> billInward = billInwardRepository.findByStockLorrySerialNo(data.getLorryDate(), data.getBatchId(), data.getBatchNo());
                if (billInward.isPresent()) {
                    binStockDto.setLorrySerialNo(billInward.get().getLorrySerialNo());
                }
                return binStockDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<BinQtyWiseStockDTO>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BinQtyWiseStockDTO>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> filterOfCurrentStock(Long branchId, Long deptId, Long productId, String type)
            throws UsernameNotFoundException {

        Long product_id = productId;
        Map<String, Object> stringObjectMap = new HashMap<>();
        Map<String, Object> OverAllObjectMap = new HashMap<>();

        try {

            Double currentStock = binStockRepository.getCurrentSock(product_id);
            OverAllObjectMap.put("currentStock", currentStock);

            List<PlantWiseStock> currentPlantWiseStock = binStockRepository.getBranchWithDeptCurrentStock(product_id);
            OverAllObjectMap.put("currentPlantWiseStock", getData(currentPlantWiseStock));
            stringObjectMap.put("OverAllStock", OverAllObjectMap);

        } catch (Exception e) {
            e.printStackTrace();
            stringObjectMap.put("OverAllStock", OverAllObjectMap);
        }

        return new ResponseEntity<Map<String, Object>>(stringObjectMap, HttpStatus.OK);
    }

    public ResponseEntity<?> getAsrsPalletType(String palletNumber, User loggedUser) {
        Optional<GogroupPalletNumberMap> palletNumberMap = gogroupPalletNumberMapRepository.findByAsrsPalletNumber(palletNumber, loggedUser.getDepartment().getId());
        if (palletNumberMap != null && palletNumberMap.isPresent()) {
            PalletDto palletDto = new PalletDto(palletNumberMap.get());
            return new ResponseEntity<>(palletDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("404", "Not Found PalletNumber Or Not Belong To User Dept"), HttpStatus.NOT_FOUND);
        }
    }

	public ResponseEntity<?> palletNoValidation(List<PalletDto> palletDto, User user) {
		List<String> existingPalletNumbers = new ArrayList<>();
		for (PalletDto dto : palletDto) {
             Optional<GogroupPalletNumberMap> exists = gogroupPalletNumberMapRepository.findByPalletNumber(dto.getPalletNumber());
             if (exists.isPresent()) {   
            	 GogroupPalletNumberMap pallet = exists.get();

            	 existingPalletNumbers.add(
                         dto.getPalletNumber() +
                         " (Bin: " + pallet.getBinId() + ")"
                 );
             }
         }
		if (!existingPalletNumbers.isEmpty()) {

	        String message = "Pallet Number(s) already taken: "
	                + String.join(", ", existingPalletNumbers);

	        return ResponseEntity.badRequest()
	                .body(new Status("400", message));
	    }
		 return new ResponseEntity<>(palletDto, HttpStatus.OK);
		
	}

	public ResponseEntity<?> palletNoUpdate(List<PalletDto> palletDto, User user) {
		  List<GogroupPalletNumberMap> palletEntities = palletDto.stream()
                  .map(dto -> {
                      GogroupPalletNumberMap entity = new GogroupPalletNumberMap(dto);  
                      entity.setBinId(dto.getBinId());
                      return entity;
                  })
                  .collect(Collectors.toList());

          gogroupPalletNumberMapRepository.saveAll(palletEntities);
      

          return new ResponseEntity<Status>(new Status("200", "PalletNo updated  successfully!!!"), HttpStatus.OK);

	}

	public ResponseEntity<?> findPalletDetails(Long binNo, User user) {
		  List<GogroupPalletNumberMap> palletNumberMap = gogroupPalletNumberMapRepository.findByPalletDetails(binNo);
	        if (palletNumberMap != null && !palletNumberMap.isEmpty()) {
	           
	        	List<PalletDto> palletDto = palletNumberMap.stream().map(data->{
	        		 PalletDto dto = new PalletDto(data);
	        		 return dto;
	        	}).collect(Collectors.toList());

	            return new ResponseEntity<>(palletDto, HttpStatus.OK);
	        } else {
	            return new ResponseEntity<Status>(new Status("404", "Not Found PalletNumber Or Not Belong To User Dept"), HttpStatus.NOT_FOUND);
	        }
	}
}
