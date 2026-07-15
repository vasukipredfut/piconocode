package com.aria.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

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
import com.aria.utils.DateUtil;
import com.aria.utils.GDCUtil;

@Service
public class GodownService {

    @Autowired
    GoDownExternalRepository goDownExternalRepository;

    @Autowired
    RequestGodownItemRepository requestGodownItemRepository;

    @Autowired
    BinStockRepository binStockRepository;

    @Autowired
    ProductMasterRepository productMasterRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GDCUtil gdcUtil;

    @Autowired
    TRNIndexRepo trnIndexRepo;

    @Autowired
    BinStockUtil binStockUtil;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    OrganisationRepository organisationRepository;


    @Autowired
    GoDownRepository goDownRepository;

    @Autowired
    MaterialReturnRepository materialReturnRepository;

    @Autowired
    MaterialReturnItemsRepository materialReturnItemsRepository;

    @Autowired
    GoGroupRepository goGroupRepository;

    @Autowired
    BlendingRequestAndPreprocessBinMapRepository blendingRequestAndPreprocessBinMapRepository;

    @Autowired
    BlendingRequestReceiveBinMapRepository blendingRequestReceiveBinMapRepository;

    @Autowired
    BinStockRepository binstockRepository;


    public ResponseEntity<?> saveExternalGoDown(GoDownExternalDto goDownExternalDto, User loggedUser) {

        GoDownExternal godown = new GoDownExternal(goDownExternalDto);
        godown.setCreatedDate(LocalDate.now(ZoneId.of("Asia/Kolkata")));
        godown.setCreatedBy(loggedUser.getId());
        Branch branch = new Branch();
        branch.setId(loggedUser.getBranch_id());
        godown.setFromBranch(branch);
        DepartmentDetails departmentDetails = new DepartmentDetails();
        departmentDetails.setId(4L);
        godown.setFromDepartment(departmentDetails);
        godown.setStatus("pending");
        if (godown.getTrnNo() == null || godown.getTrnNo() == "") {
            String trnNo = getTrnNo();
            godown.setTrnNo(trnNo);
        }
        if (godown.getFromName() != null && godown.getFromName().equalsIgnoreCase("branch")) {

            Optional<Branch> optionalBranch = branchRepository.findBy(godown.getFromName());
            if (optionalBranch.isPresent())
                godown.setFromId(optionalBranch.get().getId());

        } else if (godown.getFromName() != null && godown.getFromName().equalsIgnoreCase("godown")) {

            Optional<GoDown> optionalGoDown = goDownRepository.findByName(godown.getFromName());
            if (optionalGoDown.isPresent())
                godown.setFromId(optionalGoDown.get().getId());
        }

        GoDownExternal result = goDownExternalRepository.save(godown);
        for (RequestGodownItemDto requestItemDto : goDownExternalDto.getRequestItem()) {
            RequestGodownItem requestItem = new RequestGodownItem(requestItemDto);
            requestItem.setGoDownExternalId(result.getId());
            requestItem.setIssueBranch(branch);
            requestItem.setIssueDepartment(loggedUser.getDepartment());
            RequestGodownItem rs = requestGodownItemRepository.save(requestItem);
            for (BinStockDto binStockDto : requestItemDto.getBinStock()) {

                Long binName = Long.valueOf(binStockDto.getBinName());
                BinStock binStock = binStockRepository.findByBatchNoAndBinName(binStockDto.getId(), binStockDto.getProductId(),
                        binStockDto.getBatchNo(), binName, Double.valueOf(binStockDto.getLot()));
                System.out.println(binStock != null ? binStock.getId() : "BinStock not found");
                System.out.println(binStock.getId());
                if (binStock != null) {

                    BinStock newBinstock = new BinStock(binStockDto, binStock);
                    newBinstock.setLockStatus("Y");
                    if (binStock.getAvgCost() != null) {
                        newBinstock.setCost(newBinstock.getStkQty() * binStock.getAvgCost());
                    }
                    newBinstock.setLTrnNo(result.getTrnNo());
                    System.out.println(result.getTrnNo());
                    newBinstock.setMrnStatus("Y");
                    newBinstock.setLockType("BU");
                    newBinstock.setAdate(dateUtil.getISTDate());
                    BinStock rsBin = binStockRepository.save(newBinstock);
                    // Reduce selected Stock
                    Double updatedStkQty = binStock.getStkQty() - Double.valueOf(binStockDto.getTakenIssued());
                    if (updatedStkQty > 0) {
                        Double cost = updatedStkQty * binStock.getAvgCost();
                        binStock.setCost(cost);
                    }
                    binStock.setStkQty(updatedStkQty);
                    binStockRepository.save(binStock);
                    binStockUtil.updateBinStockLogExternal(rsBin, loggedUser, "I");

                } else {
                    return new ResponseEntity<Status>(new Status("404", "BinStock not updated!!!"), HttpStatus.NOT_FOUND);
                }
            }
        }
        if ((goDownExternalDto.getType() != null && goDownExternalDto.getType().equalsIgnoreCase("Blending_Request")) && goDownExternalDto.getOldTrnNo() != null) {
            saveBlendingExternalGoDownRequestPending(result, goDownExternalDto);
        }
        return new ResponseEntity<Status>(new Status("200", "GoDownExternal save successfully!!!"), HttpStatus.OK);

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

    public ResponseEntity<?> updateExternalGoDown(GoDownExternalDto goDownExternalDto, User updatedByUser) {


        boolean materialReturn = false;

        Optional<GoDownExternal> optionalGoDownExternal = goDownExternalRepository.findByTrnNo(goDownExternalDto.getTrnNo());

        if (optionalGoDownExternal.isPresent()) {

            GoDownExternal godown = optionalGoDownExternal.get();
            Branch branch = new Branch();
            branch.setId(updatedByUser.getBranch_id());
            godown.setIssueBy(updatedByUser);
            godown.setIssueBranch(branch);
            DepartmentDetails departmentDetails = new DepartmentDetails();
            departmentDetails.setId(4L);
            godown.setIssueDepartment(departmentDetails);
            godown.setDispatchOn(dateUtil.stringToDateTime(goDownExternalDto.getDispatchOn()));
            godown.setUpdatedBy(updatedByUser.getId());
            if (goDownExternalDto.getDeliveredBy() != null) {
                Optional<User> user = userRepository.findById(Long.valueOf(goDownExternalDto.getDeliveredBy()));
                if (user.isPresent()) {
                    godown.setDeliveredBy(user.get());
                }
            }

            godown.setFrom(goDownExternalDto.getFrom());
            godown.setTo(goDownExternalDto.getTo());
            godown.setToName(goDownExternalDto.getToName());

            if (godown.getToName() != null && godown.getToName().equalsIgnoreCase("branch")) {

                Optional<Branch> optionalBranch = branchRepository.findBy(godown.getToName());
                if (optionalBranch.isPresent())
                    godown.setToId(optionalBranch.get().getId());

            } else if (godown.getToName() != null && godown.getToName().equalsIgnoreCase("godown")) {

                Optional<GoDown> optionalGoDown = goDownRepository.findByName(godown.getToName());
                if (optionalGoDown.isPresent())
                    godown.setToId(optionalGoDown.get().getId());
            }

            godown.setIssueReference(goDownExternalDto.getIssueReference());
            godown.setIssueMode(goDownExternalDto.getIssueMode());
            godown.setTransmode(goDownExternalDto.getTransmode());
            if (goDownExternalDto.getVehicleType() != null) {
                godown.setVehicleType(goDownExternalDto.getVehicleType());
            }

            godown.setVehicleName(goDownExternalDto.getVehicleName());

            godown.setEwayBill(goDownExternalDto.getEwayBill());
            godown.setStatus("approved");
            String gdcNo = gdcUtil.getGDCNo(updatedByUser.getBranch_id(), updatedByUser.getOrg_id());
            godown.setGDCNo(gdcNo);
            GoDownExternal external = goDownExternalRepository.save(godown);
            if (!goDownExternalDto.getRequestItem().isEmpty()) {
                for (RequestGodownItemDto requestGodownItem : goDownExternalDto.getRequestItem()) {
                    Optional<RequestGodownItem> optional = requestGodownItemRepository.findById(requestGodownItem.getId());
                    RequestGodownItem requestItem = null;
                    if (optional.isPresent()) {
                        requestItem = new RequestGodownItem(optional.get(), requestGodownItem);
                    } else {
                        requestItem = new RequestGodownItem(requestGodownItem);
                    }
                    if (requestItem.getMaterial_return() != null && requestItem.getMaterial_return().equalsIgnoreCase("YES")) {
                        materialReturn = true;
                    }
                    RequestGodownItem rs = requestGodownItemRepository.save(requestItem);
                }
            }

            if (materialReturn) {
                updateMaterialReturn(external, updatedByUser);
            } else {
                deleteMaterialReturn(external, updatedByUser);
            }

            return new ResponseEntity<>(new Status("200", "Other GoDown issue updated successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("404", "not updated!!!"), HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity<?> updateBlendingExternalGoDown(GoDownExternalDto goDownExternalDto, User loggedUser) {

        Optional<GoDownExternal> optionalGoDownExternal = goDownExternalRepository.findByTrnNo(goDownExternalDto.getTrnNo());

        if (optionalGoDownExternal.isPresent()) {

            GoDownExternal godown = optionalGoDownExternal.get();
            Branch branch = new Branch();
            branch.setId(loggedUser.getBranch_id());
            godown.setIssueBy(loggedUser);
            godown.setIssueBranch(branch);
            DepartmentDetails departmentDetails = new DepartmentDetails();
            departmentDetails.setId(4L);
            godown.setIssueDepartment(departmentDetails);
            godown.setDispatchOn(dateUtil.stringToDateTime(goDownExternalDto.getDispatchOn()));
            godown.setUpdatedBy(loggedUser.getId());

            godown.setFrom(goDownExternalDto.getFrom());
            godown.setTo(goDownExternalDto.getTo());
            godown.setToName(goDownExternalDto.getToName());

            if (godown.getToName() != null && godown.getToName().equalsIgnoreCase("branch")) {

                Optional<Branch> optionalBranch = branchRepository.findBy(godown.getToName());
                if (optionalBranch.isPresent())
                    godown.setToId(optionalBranch.get().getId());

            } else if (godown.getToName() != null && godown.getToName().equalsIgnoreCase("godown")) {

                Optional<GoDown> optionalGoDown = goDownRepository.findByName(godown.getToName());
                if (optionalGoDown.isPresent())
                    godown.setToId(optionalGoDown.get().getId());
            }
            godown.setIssueReference(goDownExternalDto.getIssueReference());
            godown.setIssueMode(goDownExternalDto.getIssueMode());
            godown.setTransmode(goDownExternalDto.getTransmode());
            if (goDownExternalDto.getVehicleType() != null) {
                godown.setVehicleType(goDownExternalDto.getVehicleType());
            }

            godown.setVehicleName(goDownExternalDto.getVehicleName());

            godown.setEwayBill(goDownExternalDto.getEwayBill());
            godown.setStatus("approved");
            String gdcNo = gdcUtil.getGDCNo(loggedUser.getBranch_id(), loggedUser.getOrg_id());
            godown.setGDCNo(gdcNo);
            GoDownExternal result = goDownExternalRepository.save(godown);
            if (!goDownExternalDto.getRequestItem().isEmpty()) {
                for (RequestGodownItemDto requestItemDto : goDownExternalDto.getRequestItem()) {

                    System.out.println("check request item");

                    RequestGodownItem requestItem = new RequestGodownItem(requestItemDto);
                    requestItem.setGoDownExternalId(result.getId());
                    requestItem.setIssueBranch(branch);
                    requestItem.setIssueDepartment(loggedUser.getDepartment());
                    RequestGodownItem rs = requestGodownItemRepository.save(requestItem);
                    for (BinStockDto binStockDto : requestItemDto.getBinStock()) {

                        Long binName = Long.valueOf(binStockDto.getBinName());
                        BinStock binStock = binStockRepository.findByBatchNoAndBinName(binStockDto.getId(), binStockDto.getProductId(),
                                binStockDto.getBatchNo(), binName, Double.valueOf(binStockDto.getLot()));
                        System.out.println(binStock != null ? binStock.getId() : "BinStock not found");
                        System.out.println(binStock.getId());

                        if (binStock != null) {

                            BinStock newBinstock = new BinStock(binStockDto, binStock);
                            newBinstock.setLockStatus("Y");
                            System.out.println(binStock.getAvgCost());
                            System.out.println(newBinstock.getStkQty());
                            if (binStock.getAvgCost() != null) {
                                newBinstock.setCost(newBinstock.getStkQty() * binStock.getAvgCost());
                            }
                            newBinstock.setLTrnNo(result.getTrnNo());
                            System.out.println(result.getTrnNo());
                            newBinstock.setMrnStatus("Y");
                            newBinstock.setLockType("BU");
                            newBinstock.setAdate(dateUtil.getISTDate());
                            BinStock rsBin = binStockRepository.save(newBinstock);
                            // Reduce selected Stock
                            Double updatedStkQty = binStock.getStkQty() - Double.valueOf(binStockDto.getTakenIssued());
                            if (updatedStkQty > 0) {
                                if (binStock.getAvgCost() != null) {
                                    Double cost = updatedStkQty * binStock.getAvgCost();
                                    binStock.setCost(cost);
                                }
                            }
                            binStock.setStkQty(updatedStkQty);
                            binStockRepository.save(binStock);
                            binStockUtil.updateBinStockLogExternal(rsBin, loggedUser, "I");
                        } else {
                            return new ResponseEntity<Status>(new Status("404", "BinStock not updated!!!"), HttpStatus.NOT_FOUND);
                        }
                    }
                }
            } else {
                System.out.println("not check request item");
            }
        } else {
            return new ResponseEntity<Status>(new Status("404", "GoDownExternal NotFound!!!"), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<Status>(new Status("200", "GoDownExternal save successfully!!!"), HttpStatus.OK);

    }

    public ResponseEntity<?> updateGoDownRecipt(GoDownExternalDto goDownExternalDto, User loggedUser) {
        Optional<GoDownExternal> optionalGoDownExternal = goDownExternalRepository.findByTrnNo(goDownExternalDto.getTrnNo());
        if (optionalGoDownExternal.isPresent()) {
            GoDownExternal godown = optionalGoDownExternal.get();
            godown.setStatus("completed");

            for (RequestGodownItemDto requestItemDto : goDownExternalDto.getRequestItem()) {
                List<BinDetails> bin = requestItemDto.getBindetail();
                for (BinDetails binDetailDto : bin) {

                    BinStock binStock = binStockRepository.findByBatchNoAndTRNNo(godown.getTrnNo(),
                            binDetailDto.getBatchno(), requestItemDto.getItemId(),
                            Double.valueOf(requestItemDto.getSelectLot()), Double.valueOf(binDetailDto.getAllottedQuantity()), "BU");

                    if (binStock != null) {

                        binStock.setBinName(binDetailDto.getBinid());

                        Optional<GoGroup> goGroupOptional = goGroupRepository.findByBinNo(binDetailDto.getBinid());
                        if (goGroupOptional.isPresent()) {
                            binStock.setBinId(goGroupOptional.get().getGCode());
                            binStock.setDept(goGroupOptional.get().getDept());
                            binStock.setGid(goGroupOptional.get().getGid());
                            binStock.setBranch(goGroupOptional.get().getBranch());
                        }
                        binStock.setMrnStatus("N");
                        binStock.setLockStatus("N");
                        binStock.setLockType("");
                        BinStock rs = binStockRepository.save(binStock);
                        binStockUtil.updateBinStockLogExternal(rs, loggedUser, "R");
                    } else {
                        new ResponseEntity<>(new Status("404", "other GoDown recipt not updated successfully!!!"), HttpStatus.OK);
                    }
                }
            }
            goDownExternalRepository.save(godown);
            updateBlendingReceiveBin(godown);
            return new ResponseEntity<>(new Status("200", "other GoDown recipt updated successfully!!!"),
                    HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("404", "not updated!!!"), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> listAll(Long firmId, Long branch, String status, String trnNo, String fromDate, String toDate, Pageable pageable) {

        Date startDate = null;
        Date endDate = null;
        if(fromDate != null){
            startDate = dateUtil.getStringTODateWk(fromDate);
        }

        if(toDate != null){
            endDate = dateUtil.getStringTODateWk(toDate);
        }

        Page<GoDownExternal> goDownExternals;

        if (status.equalsIgnoreCase("approved") || status.equalsIgnoreCase("completed")) {
            List<String> statuses = Arrays.asList("approved", "completed");
            goDownExternals = goDownExternalRepository.findByStatusInOrderByIdDesc(statuses, branch ,  firmId , trnNo ,  startDate, endDate, pageable);
        } else if (status.equalsIgnoreCase("pending")) {
            goDownExternals = goDownExternalRepository.findByStatusOrderByIdDesc(status, branch , firmId , trnNo ,  startDate, endDate, pageable);
        } else {
            goDownExternals = goDownExternalRepository.findByAll(firmId, branch , trnNo ,  startDate, endDate, pageable);
        }

        if (goDownExternals.hasContent()) {
            List<GoDownExternalDto> dtos = goDownExternals.getContent().stream().map(data -> generateGoDownExternalDto(data, null))
                    .collect(Collectors.toList());
            PageDataDto<GoDownExternalDto> pageData = new PageDataDto<>(dtos, goDownExternals);
            return new ResponseEntity<PageDataDto<GoDownExternalDto>>(pageData, HttpStatus.OK);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    public ResponseEntity<?> getExternalGoDownById(Long id, User loggedUser) {
        Optional<GoDownExternal> godownOptional = goDownExternalRepository.findById(id);
        if (godownOptional.isPresent()) {
            GoDownExternal goDownExternal = godownOptional.get();
            GoDownExternalDto dto = generateGoDownExternalDto(goDownExternal, loggedUser);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } else {
            return ResponseEntity.badRequest().body("Godown not found with id: " + id);
        }
    }

    public GoDownExternalDto generateGoDownExternalDto(GoDownExternal goDownExternal, User loggedUser) {

        GoDownExternalDto dto = new GoDownExternalDto(goDownExternal);

        if (loggedUser != null) {

            CompanyList companyList = organisationRepository.findByFirm(loggedUser.getOrg_id());
            if (companyList != null) {
                dto.setFirmDetails(new Company(companyList));
            }

            if (dto.getFrom() != null) {
                if (dto.getFrom().equalsIgnoreCase("branch")) {
                    Optional<Branch> optional = branchRepository.findBy(dto.getFromName());
                    if (optional.isPresent()) {
                        BranchAddressList branchAddressList = branchRepository.getBranchByIdANDOrgId(optional.get().getId(), loggedUser.getOrg_id());
                        if (branchAddressList != null) {
                            dto.setFromAddress(new BranchAddress(branchAddressList));
                        } else {
                            BranchAddressList branchAddressList1 = branchRepository.getBranchId(optional.get().getId());
                            if (branchAddressList1 != null) {
                                dto.setFromAddress(new BranchAddress(branchAddressList1));
                            }
                        }
                    }
                } else {
                    Optional<GoDown> optional = goDownRepository.findByName(dto.getFromName());
                    if (optional.isPresent()) {
                        dto.setFromAddress(optional.get());
                    }
                }
            }

            if (dto.getTo() != null) {

                if (dto.getTo().equalsIgnoreCase("branch")) {
                    Optional<Branch> optional = branchRepository.findBy(dto.getToName());
                    if (optional.isPresent()) {
                        BranchAddressList branchAddressList = branchRepository.getBranchByIdANDOrgId(optional.get().getId(), loggedUser.getOrg_id());
                        if (branchAddressList != null) {
                            dto.setToAddress(new BranchAddress(branchAddressList));
                        } else {
                            BranchAddressList branchAddressList1 = branchRepository.getBranchId(optional.get().getId());
                            if (branchAddressList1 != null) {
                                dto.setToAddress(new BranchAddress(branchAddressList1));
                            }
                        }
                    }
                } else {
                    Optional<GoDown> optional = goDownRepository.findByName(dto.getToName());
                    if (optional.isPresent()) {
                        dto.setToAddress(optional.get());
                    }
                }
            }

        }

        List<RequestGodownItem> requestItems = requestGodownItemRepository.findByGoDownExternalId(goDownExternal.getId());

        List<RequestGodownItemDto> requestItemDtos = new ArrayList<>();

        for (RequestGodownItem requestItem : requestItems) {

            RequestGodownItemDto requestItemDto = new RequestGodownItemDto(requestItem);
            
	            if(requestItemDto.getItemId() != null) {
	
		            Optional<ProductMaster> optional = productMasterRepository.findById(requestItemDto.getItemId());
		            if (optional.isPresent()) {
		                if (optional.get().getHsnRefId() != null) {
		                    requestItemDto.setHsnNo(optional.get().getHsnRefId());
		                }
		
		                if (requestItemDto.getSelectLot() != null) {
		                    if (requestItemDto.getSelectLot().equalsIgnoreCase("1")) {
		                        requestItemDto.setUnit(optional.get().getUnit());
		                    } else {
		                        requestItemDto.setUnit(optional.get().getAltunit());
		                    }
		                }
		            }
	            }

            List<BinStock> binStocks = binStockRepository.findByLTrnNo(dto.getTrnNo(), requestItem.getItemId());

            List<BinStockDto> binStockDtos = binStocks.stream().map(BinStockDto::new).collect(Collectors.toList());

            requestItemDto.setBinStock(binStockDtos);

            requestItemDtos.add(requestItemDto);
        }

        dto.setRequestItem(requestItemDtos);

        return dto;
    }


    public void updateMaterialReturn(GoDownExternal goDownExternal, User loggedUser) {


        Boolean update = false;

        MaterialReturn materialReturn = null;

        Optional<MaterialReturn> materialReturnOptional = materialReturnRepository.findByGodownId(goDownExternal.getId());

        if (materialReturnOptional.isPresent()) {
            materialReturn = new MaterialReturn(goDownExternal);
            materialReturn.setId(materialReturnOptional.get().getId());
            materialReturn.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            materialReturn.setUpdatedBy(loggedUser.getId());
        } else {
            materialReturn = new MaterialReturn(goDownExternal);
            materialReturn.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            materialReturn.setCreatedBy(loggedUser.getId());
        }

        materialReturn.setType("GoDownExternal");
        //materialReturn.setStatus("Pending");
        Organisation organisation = new Organisation();
        organisation.setId(loggedUser.getOrg_id());
        materialReturn.setOrganisation(organisation);

        materialReturn.setFromDeptId(loggedUser.getDepartment().getId());
        materialReturn.setFromDeptName(loggedUser.getDepartment().getName());

        if (materialReturn.getFromName() != null && materialReturn.getFromName().equalsIgnoreCase("Branch")) {

            Optional<Branch> optionalBranch = branchRepository.findBy(goDownExternal.getFromName());
            if (optionalBranch.isPresent())
                materialReturn.setFromId(optionalBranch.get().getId());

        } else if (materialReturn.getFromName() != null && materialReturn.getFromName().equalsIgnoreCase("Godown")) {

            Optional<GoDown> optionalGoDown = goDownRepository.findByName(goDownExternal.getFromName());
            if (optionalGoDown.isPresent())
                materialReturn.setFromId(optionalGoDown.get().getId());
        }


        if (materialReturn.getToName() != null && materialReturn.getToName().equalsIgnoreCase("Branch")) {

            Optional<Branch> optionalBranch = branchRepository.findBy(goDownExternal.getToName());
            if (optionalBranch.isPresent())
                materialReturn.setToId(optionalBranch.get().getId());
            materialReturn.setToReachId(optionalBranch.get().getId());
            materialReturn.setToReachName(optionalBranch.get().getBranchName());

        } else if (materialReturn.getToName() != null && materialReturn.getToName().equalsIgnoreCase("Godown")) {

            Optional<GoDown> optionalGoDown = goDownRepository.findByName(goDownExternal.getToName());
            if (optionalGoDown.isPresent()) {
                materialReturn.setToId(optionalGoDown.get().getId());
                materialReturn.setToReachId(optionalGoDown.get().getId());
                materialReturn.setToReachName(optionalGoDown.get().getName());
            }
        }

        MaterialReturn result = materialReturnRepository.save(materialReturn);
        if (update) {
            List<MaterialReturnItems> materialReturnItems = materialReturnOptional.get().getReturnItems();
            if (!materialReturnItems.isEmpty()) {
                materialReturnItemsRepository.deleteAll(materialReturnItems);
            }
        }

        List<RequestGodownItem> otherBranchIssueItems = requestGodownItemRepository.findBy(goDownExternal.getId());
        for (RequestGodownItem itemsDto : otherBranchIssueItems) {

            if (update) {

                Optional<MaterialReturnItems> materialReturnItems = materialReturnItemsRepository.findBy(result.getId(), itemsDto.getItemId(), itemsDto.getItemName());
                if (!materialReturnItems.isPresent()) {
                    MaterialReturnItems materialReturnItems1 = new MaterialReturnItems(itemsDto);
                    materialReturnItems1.setReturnId(result.getId());
                    materialReturnItemsRepository.save(materialReturnItems1);
                }

            } else {
                MaterialReturnItems materialReturnItems = new MaterialReturnItems(itemsDto);
                materialReturnItems.setReturnId(result.getId());
                materialReturnItemsRepository.save(materialReturnItems);
            }
        }

    }


    public void deleteMaterialReturn(GoDownExternal sendExternalEntity, User loggedUser) {

        Optional<MaterialReturn> materialReturnOptional = materialReturnRepository.findByGodownId(sendExternalEntity.getId());

        if (materialReturnOptional.isPresent()) {
            MaterialReturn materialReturn = materialReturnOptional.get();

            List<MaterialReturnItems> materialReturnItems = materialReturn.getReturnItems();
            if (!materialReturnItems.isEmpty()) {
                materialReturnItemsRepository.deleteAll(materialReturnItems);
            }
            materialReturnRepository.delete(materialReturn);
        }

    }


    public void updateBlendingReceiveBin(GoDownExternal otherBranchReceipt) {

        if (otherBranchReceipt.getStatus().equalsIgnoreCase("completed")) {

            Optional<BlendingRequestAndPreprocessBinMap> optional = blendingRequestAndPreprocessBinMapRepository.findByExternalRequest(otherBranchReceipt.getId());
            if (optional.isPresent()) {

                List<BinStock> binStockList = binstockRepository.findByLTrnNo(otherBranchReceipt.getTrnNo());
                if (!binStockList.isEmpty()) {
                    for (BinStock binStock : binStockList) {

                        Optional<BlendingRequestReceiveBinMap> optionalBlendingRequestReceiveBinMap = blendingRequestReceiveBinMapRepository.findByReceive(otherBranchReceipt.getId(), otherBranchReceipt.getTrnNo(), binStock.getRefId(), binStock.getBinName(), binStock.getBatchNo(), binStock.getLot(), "Blending_External_Receive");
                        BlendingRequestReceiveBinMap blendingRequestReceiveBinMap = null;
                        if (optionalBlendingRequestReceiveBinMap.isPresent()) {
                            blendingRequestReceiveBinMap = optionalBlendingRequestReceiveBinMap.get();
                        } else {
                            blendingRequestReceiveBinMap = new BlendingRequestReceiveBinMap();
                        }

                        blendingRequestReceiveBinMap.setBlendingDetailId(optional.get().getBlendingDetailId());
                        blendingRequestReceiveBinMap.setCasgroup(optional.get().getCasgroup());
                        blendingRequestReceiveBinMap.setType("Blending_External_Receive");
                        blendingRequestReceiveBinMap.setTypeId(otherBranchReceipt.getId());
                        blendingRequestReceiveBinMap.setBinNo(binStock.getBinName());
                        blendingRequestReceiveBinMap.setQty(binStock.getStkQty());
                        blendingRequestReceiveBinMap.setTrnNo(binStock.getLTrnNo());
                        blendingRequestReceiveBinMap.setStatus("Updated");
                        blendingRequestReceiveBinMap.setBranch(binStock.getBranch());
                        blendingRequestReceiveBinMap.setBatchNo(binStock.getBatchNo());
                        blendingRequestReceiveBinMap.setDept(binStock.getDept());
                        blendingRequestReceiveBinMap.setItemId(binStock.getRefId());
                        Optional<ProductMaster> productMasterOptional = productMasterRepository.findById(binStock.getRefId());
                        if (productMasterOptional.isPresent()) {
                            blendingRequestReceiveBinMap.setItemName(productMasterOptional.get().getProductName());
                        }
                        blendingRequestReceiveBinMap.setLot(binStock.getLot());
                        blendingRequestReceiveBinMapRepository.save(blendingRequestReceiveBinMap);
                    }
                }

            }
        }
    }


    public void saveBlendingExternalGoDownRequestPending(GoDownExternal goDownExternal, GoDownExternalDto goDownExternalDto) {

        Optional<BlendingRequestAndPreprocessBinMap> optional = blendingRequestAndPreprocessBinMapRepository.findByExternalRequestWithTrnNo(goDownExternalDto.getOldTrnNo());
        if (optional.isPresent()) {
            BlendingRequestAndPreprocessBinMap blendingRequestAndPreprocessBinMap = new BlendingRequestAndPreprocessBinMap();
            blendingRequestAndPreprocessBinMap.setBlendingDetailId(optional.get().getBlendingDetailId());
            blendingRequestAndPreprocessBinMap.setCasgroup(optional.get().getCasgroup());
            blendingRequestAndPreprocessBinMap.setType(optional.get().getType());
            blendingRequestAndPreprocessBinMap.setTypeId(goDownExternal.getId());
            blendingRequestAndPreprocessBinMap.setTrnNo(goDownExternal.getTrnNo());
            blendingRequestAndPreprocessBinMapRepository.save(blendingRequestAndPreprocessBinMap);
        }

    }


}
