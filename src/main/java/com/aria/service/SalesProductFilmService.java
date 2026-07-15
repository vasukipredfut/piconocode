package com.aria.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aria.dao.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aria.dto.BinStockQuantity;
import com.aria.dto.BinStockQuantityDto;
import com.aria.dto.MaterialRequestDto;
import com.aria.dto.PageDataDto;
import com.aria.dto.RequestItemDto;
import com.aria.dto.SalesCeilingStockLimitDto;
import com.aria.dto.SalesFinalLocationRequest;
import com.aria.dto.SalesFinalProductRequest;
import com.aria.dto.SalesLocationDto;
import com.aria.dto.SalesProductFilmDto;
import com.aria.dto.SalesProductFilmOrderDto;
import com.aria.dto.SalesProductFilmPricesRequest;
import com.aria.dto.SalesProductFilmResponse;
import com.aria.dto.SalesProductNameDto;
import com.aria.model.Branch;
import com.aria.model.DepartmentDetails;
import com.aria.model.FilmProductValidity;
import com.aria.model.IssueItems;
import com.aria.model.MaterialIssue;
import com.aria.model.MaterialRequest;
import com.aria.model.ProductMaster;
import com.aria.model.RequestItem;
import com.aria.model.SalesCeilingStockLimit;
import com.aria.model.SalesProductBranchesEntity;
import com.aria.model.SalesProductCapacityEntity;
import com.aria.model.SalesProductFilm;
import com.aria.model.SalesProductFilmOrder;
import com.aria.model.SalesProductFilmPricesEntity;
import com.aria.model.SalesProductGroupEntity;
import com.aria.model.SalesProductLocationEntity;
import com.aria.model.SalesProductNameEntity;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.utils.DateUtil;

@Service
public class SalesProductFilmService {

    @Autowired
    private SalesProductGroupRepository salesProductGroupRepository;

    @Autowired
    private SalesProductBranchesRepository salesProductBranchesRepository;

    @Autowired
    private MaterialRequestRepository materialRequestRepository;

    @Autowired
    private RequestItemRepository requestItemRepository;

    @Autowired
    private ProductMasterRepository productMasterRepository;

    @Autowired
    private SalesProductCapacityRepository salesProductCapacityRepository;

    @Autowired
    private SalesProductNameRepository salesProductNameRepository;

    @Autowired
    private SaleProductFilmRepository saleProductFilmRepository;

    @Autowired
    private SalesProductFilmOrderRepository salesProductFilmOrderRepository;

    @Autowired
    private SalesCeilingStockLimitRepository salesCeilingStockLimitRepository;

    @Autowired
    private SalesProductionLocationRepository salesProductionLocationRepository;

    @Autowired
    private FilmProductValidityRepository filmProductValidityRepository;

    @Autowired
    private DateUtil dateUtil;

    @Autowired
    private BinStockRepository binStockRepository;

    @Autowired
    private MaterialIssueRepository materialIssueRepository;

    @Autowired
    private IssueItemsRepository issueItemsRepository;

    @Autowired
    private SalesProductFilmPricesRepository salesProductFilmPricesRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<?> saveSalesProductFilm(SalesProductFilmDto dto, User user) {
        if (dto.getProductId() != null) {
            Optional<SalesProductFilm> optional = saleProductFilmRepository.findByProductIdAndBranchId(dto.getProductId(), dto.getBranchID());
            if (optional.isPresent()) {
                return new ResponseEntity<Status>(new Status("400", "Sales Product Film Already Exists"), HttpStatus.OK);
            }
        }
        SalesProductFilm film = new SalesProductFilm(dto);
        film.setCreatedDate(LocalDate.now());
        film.setUpdatedBy(user);
        saleProductFilmRepository.save(film);
        return new ResponseEntity<Status>(new Status("200", "Sales Product Film saved successfully!!!"), HttpStatus.OK);
    }
    public ResponseEntity<?> updateSalesProductFilm(SalesProductFilmDto dto, User user) {
        SalesProductFilm film = new SalesProductFilm(dto);
        film.setUpdatedBy(user);
        saleProductFilmRepository.save(film);
        return new ResponseEntity<Status>(new Status("200", "Sales Product Film updated successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> saveSalesFinalProduct(SalesFinalProductRequest salesFinalProductRequest, User user) {
        if (salesFinalProductRequest.getProductGroupID() == null
                || salesFinalProductRequest.getFinalProductName() == null
                || salesFinalProductRequest.getFinalProductName().isEmpty()
                || salesFinalProductRequest.getProductGroupID() == 0L) {
            return new ResponseEntity<Status>(new Status("400", "Bad Request"), HttpStatus.BAD_REQUEST);
        }
        SalesProductNameEntity salesProductNameEntity = new SalesProductNameEntity();
        salesProductNameEntity.setSalesProductGroup(salesFinalProductRequest.getProductGroupID());
        salesProductNameEntity.setProductName(salesFinalProductRequest.getFinalProductName());
        salesProductNameEntity.setType("");
        salesProductNameEntity.setCapacity(0L);
        salesProductNameEntity.setCreatedBy(user.getId());
        salesProductNameRepository.save(salesProductNameEntity);
        return new ResponseEntity<Status>(new Status("200", "Sales Product Group saved successfully!!!"),
                HttpStatus.OK);
    }

    public ResponseEntity<?> deleteSalesProductFilm(Long id) {
        saleProductFilmRepository.deleteById(id);
        return new ResponseEntity<Status>(new Status("200", "Sales Product Deleted successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> saveSalesLocation(SalesFinalLocationRequest salesFinalLocationRequest, User user) {
        if (salesFinalLocationRequest.getLocationName() == null || salesFinalLocationRequest.getLocationType() == null
                || salesFinalLocationRequest.getLocationType().isEmpty()
                || salesFinalLocationRequest.getLocationName().isEmpty()) {
            return new ResponseEntity<Status>(new Status("400", "Bad Request"), HttpStatus.BAD_REQUEST);
        }
        SalesProductLocationEntity salesProductLocationEntity = new SalesProductLocationEntity();
        salesProductLocationEntity.setLocationType(salesFinalLocationRequest.getLocationType());
        salesProductLocationEntity.setName(salesFinalLocationRequest.getLocationName());
        salesProductLocationEntity.setCreatedBy(user.getId());
        salesProductLocationEntity.setCeilingLimit("N");
        salesProductLocationEntity.setCodeWord("");
        salesProductLocationEntity.setIp("");
        salesProductionLocationRepository.save(salesProductLocationEntity);
        return new ResponseEntity<Status>(new Status("200", "Sales Location saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> saveSalesCapacity(String capacity) {
        if (capacity == null || capacity.isEmpty()) {
            return new ResponseEntity<Status>(new Status("400", "Bad Request"), HttpStatus.BAD_REQUEST);
        }
        SalesProductCapacityEntity salesProductCapacityEntity = new SalesProductCapacityEntity();
        salesProductCapacityEntity.setCapacity(capacity);
        salesProductCapacityRepository.save(salesProductCapacityEntity);
        return new ResponseEntity<Status>(new Status("200", "Capacity saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> saveSalesBranches(Long branchId) {
        Optional<Branch> branchOp = branchRepository.findById(branchId);
        if (branchOp.isPresent()) {
            Branch branch = branchOp.get();
            SalesProductBranchesEntity salesProductBranchesEntity = salesProductBranchesRepository
                    .findByBranchId(branchId);
            if (salesProductBranchesEntity == null) {
                salesProductBranchesEntity = new SalesProductBranchesEntity();
            }
            salesProductBranchesEntity.setBranchId(branchId);
            salesProductBranchesEntity.setName(branch.getSearch());
            salesProductBranchesRepository.save(salesProductBranchesEntity);
            return new ResponseEntity<Status>(new Status("200", "Plant saved successfully!!!"), HttpStatus.OK);
        }
        return new ResponseEntity<Status>(new Status("404", "Branch Not found!!!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> getSalesProductFilmGroup() {
        List<SalesProductGroupEntity> list = salesProductGroupRepository.findAll();
        if (!list.isEmpty()) {
            return new ResponseEntity<List<SalesProductGroupEntity>>(list, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<SalesProductGroupEntity>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getPrices(String locationType, Long productGroupId) {
        List<SalesProductLocationEntity> lists = salesProductionLocationRepository.findByLocationType(locationType);
        List<Long> locationIds = new ArrayList<>();
        for (SalesProductLocationEntity entity : lists) {
            locationIds.add(entity.getId());
        }
        List<SalesProductFilmPricesEntity> list = salesProductFilmPricesRepository
                .findBySalesLocationIdInAndSalesProductGroupId(locationIds, productGroupId);
        if (!list.isEmpty()) {
            return new ResponseEntity<List<SalesProductFilmPricesEntity>>(list, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<SalesProductFilmPricesEntity>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getSalesProductFilmCapacity() {
        List<SalesProductCapacityEntity> list = salesProductCapacityRepository.findAll();
        if (!list.isEmpty()) {
            return new ResponseEntity<List<SalesProductCapacityEntity>>(list, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<SalesProductCapacityEntity>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getSalesProductFilmBranches() {
        List<SalesProductBranchesEntity> list = salesProductBranchesRepository.findAll();
        if (!list.isEmpty()) {
            return new ResponseEntity<List<SalesProductBranchesEntity>>(list, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<SalesProductBranchesEntity>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getSalesProductName(Long productGroup) {
        List<SalesProductNameEntity> list = salesProductNameRepository.findBySalesProductGroup(productGroup);
        if (!list.isEmpty()) {
            List<SalesProductNameDto> result = list.stream()
                    .map(data -> new SalesProductNameDto(data.getId(), data.getProductName()))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<SalesProductNameDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<SalesProductNameDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getSalesLocation(String locationType) {
        List<SalesProductLocationEntity> list = salesProductionLocationRepository.findByLocationType(locationType);
        if (!list.isEmpty()) {
            List<SalesLocationDto> result = list.stream()
                    .map(data -> new SalesLocationDto(data.getId(), data.getName())).collect(Collectors.toList());
            return new ResponseEntity<List<SalesLocationDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<SalesLocationDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getSalesProductFilm(Long productId, String productName, String productGroup, String branch,
                                                 String finalProductName, String dateFrom, String dateTo, Pageable pageable) {
        HashMap<String, LocalDateTime> dateHashMap = dateDynamic(dateFrom, dateTo);
        System.out.println("came here");
        Page<SalesProductFilm> list = saleProductFilmRepository.getSalesProductFilmList(productId, productName,
                productGroup, branch, finalProductName, dateHashMap.get("dateFrom"), dateHashMap.get("dateTo"),
                pageable);
        System.out.println(list.getSize());
        List<SalesProductFilmResponse> salesProductFilmResponses = list.getContent().stream()
                .map(val -> entityToDto(val)).collect(Collectors.toList());
        PageDataDto<SalesProductFilmResponse> pageData = new PageDataDto<>(salesProductFilmResponses, list);
        return new ResponseEntity<PageDataDto<SalesProductFilmResponse>>(pageData, HttpStatus.OK);
    }

    private SalesProductFilmResponse entityToDto(SalesProductFilm salesProductFilm) {
        SalesProductFilmResponse salesProductFilmResponse = new SalesProductFilmResponse();
        salesProductFilmResponse.setId(salesProductFilm.getId());
        salesProductFilmResponse.setProductId(salesProductFilm.getProductId());
        salesProductFilmResponse.setProductName(salesProductFilm.getName());
        salesProductFilmResponse.setProductGroupID(salesProductFilm.getProductGroupID());

        Optional<SalesProductGroupEntity> salesProductGroupEntityOp = salesProductGroupRepository.findById(salesProductFilm.getProductGroupID());
        if (salesProductGroupEntityOp.isPresent()) {
            salesProductFilmResponse.setProductGroupName(salesProductGroupEntityOp.get().getName());
        }

        salesProductFilmResponse.setCapacityId(salesProductFilm.getCapacity());
        Optional<SalesProductCapacityEntity> salesProductCapacityEntityOp = salesProductCapacityRepository.findById(salesProductFilm.getCapacity());
        if (salesProductCapacityEntityOp.isPresent())
            salesProductFilmResponse.setCapacity(salesProductCapacityEntityOp.get().getCapacity());

        salesProductFilmResponse.setBranchID(salesProductFilm.getBranchID().getBranchId());
        salesProductFilmResponse.setBranchName(salesProductFilm.getBranchID().getName());
        salesProductFilmResponse.setFinalProductId(salesProductFilm.getFinalProductID());
        Optional<SalesProductNameEntity> salesProductNameEntityOp = salesProductNameRepository.findById(salesProductFilm.getFinalProductID());
        if (salesProductNameEntityOp.isPresent()) {
            salesProductFilmResponse.setFinalProductName(salesProductNameEntityOp.get().getProductName());
        }
        salesProductFilmResponse.setCreatedDate(salesProductFilm.getCreatedDate());

        Optional<ProductMaster> optionalProductMaster = productMasterRepository.findById(salesProductFilm.getProductId());
        if (optionalProductMaster.isPresent()) {
            salesProductFilmResponse.setAltValue(optionalProductMaster.get().getWhere2());
        }

        return salesProductFilmResponse;
    }

    public ResponseEntity<?> savePrices(List<SalesProductFilmPricesRequest> requestList, User user) {
        List<SalesProductFilmPricesEntity> list = new ArrayList<>();
        for (SalesProductFilmPricesRequest req : requestList) {
            SalesProductFilmPricesEntity salesProductFilmPricesEntity = salesProductFilmPricesRepository
                    .findBySalesProductIdAndSalesLocationIdAndSalesProductGroupId(req.getSalesProductId(), req.getSalesLocationId(), req.getSalesProductGroupId());
            if (salesProductFilmPricesEntity != null) {
                salesProductFilmPricesEntity.setPrice(req.getPrice());
                salesProductFilmPricesEntity.setPermlprice(req.getPermlprice());
                salesProductFilmPricesEntity.setUpdatedBy(user.getId());
                salesProductFilmPricesEntity.setFromDate(dateUtil.getISTDate());
                salesProductFilmPricesEntity.setEnteredDate(dateUtil.getISTDate());
            } else {
                salesProductFilmPricesEntity = new SalesProductFilmPricesEntity();
                salesProductFilmPricesEntity.setSalesLocationId(req.getSalesLocationId());
                salesProductFilmPricesEntity.setSalesProductId(req.getSalesProductId());
                salesProductFilmPricesEntity.setSalesProductGroupId(req.getSalesProductGroupId());
                salesProductFilmPricesEntity.setPrice(req.getPrice());
                salesProductFilmPricesEntity.setPermlprice(req.getPermlprice());
                salesProductFilmPricesEntity.setUpdatedBy(user.getId());
                salesProductFilmPricesEntity.setFromDate(dateUtil.getISTDate());
                salesProductFilmPricesEntity.setEnteredDate(dateUtil.getISTDate());
            }
            list.add(salesProductFilmPricesEntity);
        }
        salesProductFilmPricesRepository.saveAll(list);
        return new ResponseEntity<Status>(new Status("200", "Prices saved successfully!!!"), HttpStatus.OK);
    }

    private HashMap<String, LocalDateTime> dateDynamic(String dateFrom, String dateTo) {
        LocalDateTime dateFromLocalDate = null, dateToLocalDate = null;
        if ((dateFrom != null && !dateFrom.isEmpty()) && (dateTo != null && !dateTo.isEmpty())) {
            dateFromLocalDate = dateUtil.getDateToLocalDateTime(dateUtil.getStringTODateWk(dateFrom));
            dateToLocalDate = dateUtil.getDateToLocalDateTime(dateUtil.getStringTODateWk(dateTo));
        } else if ((dateFrom == null || dateFrom.isEmpty()) && ((dateTo != null && !dateTo.isEmpty()))) {
            dateToLocalDate = dateUtil.getDateToLocalDateTime(dateUtil.getStringTODateWk(dateTo));
        } else if ((dateFrom != null && !dateFrom.isEmpty()) && (dateTo == null || dateTo.isEmpty())) {
            dateFromLocalDate = dateUtil.getDateToLocalDateTime(dateUtil.getStringTODateWk(dateFrom));
        }
        HashMap<String, LocalDateTime> dateHashMap = new HashMap<>();
        dateHashMap.put("dateFrom", dateFromLocalDate);
        dateHashMap.put("dateTo", dateToLocalDate);
        return dateHashMap;
    }

    /* ---------- Ceiling Stock ---------- */

    public ResponseEntity<?> saveCeilingStockLimit(SalesCeilingStockLimitDto dto, User user) {
        SalesCeilingStockLimit ceilingStock = salesCeilingStockLimitRepository
                .findByBranchIdAndCapacityAndProductGroup(dto.getPlantId(), dto.getCapacity(), dto.getProductGroupId());
        if (ceilingStock != null) {
            return new ResponseEntity<Status>(new Status("400", "Ceiling Stock limit already Exists!!!"),
                    HttpStatus.BAD_REQUEST);
        }
        SalesCeilingStockLimit model = new SalesCeilingStockLimit(dto);
        model.setUpdatedBy(user);
        model.setCreatedDate(LocalDate.now());
        salesCeilingStockLimitRepository.save(model);
        return new ResponseEntity<Status>(new Status("200", "Ceiling Stock limit saved successfully!!!"),
                HttpStatus.OK);
    }

    public ResponseEntity<?> updateCeilingStockLimit(SalesCeilingStockLimitDto dto, User user) {
        SalesCeilingStockLimit ceilingStock = salesCeilingStockLimitRepository
                .findByBranchIdAndCapacityAndProductGroup(dto.getPlantId(), dto.getCapacity(), dto.getProductGroupId());
        if (ceilingStock == null) {
            return new ResponseEntity<Status>(new Status("404", "Ceiling Stock limit doesn't Exists!!!"),
                    HttpStatus.NOT_FOUND);
        }
        ceilingStock.setCeilingLimit(dto.getCeilingLimit());
        ceilingStock.setUpdatedBy(user);
        salesCeilingStockLimitRepository.save(ceilingStock);
        return new ResponseEntity<Status>(new Status("200", "Ceiling Stock limit Updated successfully!!!"),
                HttpStatus.OK);
    }

    public ResponseEntity<?> getSalesCeilingStockLimitByFilter(String capacity, String productGroup, String branch,
                                                               Pageable pageable) {
        Page<SalesCeilingStockLimit> list = salesCeilingStockLimitRepository.getSalesProductFilmList(capacity,
                productGroup, branch, pageable);
        if (list.isEmpty()) {
            return new ResponseEntity<>(list.toList(), HttpStatus.OK);
        }
        List<SalesCeilingStockLimitDto> result = list.getContent().stream().map(val -> {
            SalesCeilingStockLimitDto dto = new SalesCeilingStockLimitDto(val);
            if (val.getBranchId() != null) {
                Optional<SalesProductBranchesEntity> bran = salesProductBranchesRepository.findById(val.getBranchId());
                bran.ifPresent(data -> dto.setPlantName(data.getName()));
            }
            if (val.getProductGroup() != null) {
                Optional<SalesProductGroupEntity> group = salesProductGroupRepository.findById(val.getProductGroup());
                group.ifPresent(data -> dto.setProductGroupName(data.getName()));
            }
            return dto;
        }).collect(Collectors.toList());

        PageDataDto<SalesCeilingStockLimitDto> pageData = new PageDataDto<>(result, list);
        return new ResponseEntity<>(pageData, HttpStatus.OK);
    }

    public ResponseEntity<?> getSalesCeilingStockLimit(Long productGroupId, Long plantId, String capacity) {
        SalesCeilingStockLimit stockLimit = salesCeilingStockLimitRepository
                .findByBranchIdAndCapacityAndProductGroup(plantId, capacity, productGroupId);
        if (stockLimit != null) {
            SalesCeilingStockLimitDto dto = new SalesCeilingStockLimitDto(stockLimit);
            if (stockLimit.getBranchId() != null) {
                Optional<SalesProductBranchesEntity> branch = salesProductBranchesRepository
                        .findById(stockLimit.getBranchId());
                branch.ifPresent(data -> dto.setPlantName(data.getName()));
            }
            if (stockLimit.getProductGroup() != null) {
                Optional<SalesProductGroupEntity> productGroup = salesProductGroupRepository
                        .findById(stockLimit.getProductGroup());
                productGroup.ifPresent(data -> dto.setProductGroupName(data.getName()));
            }
            return new ResponseEntity<SalesCeilingStockLimitDto>(dto, HttpStatus.OK);
        }
        return ResponseEntity.badRequest().body("Ceiling stock not found");
    }

    public ResponseEntity<?> getSalesCeilingStockLimitById(Long id) {
        Optional<SalesCeilingStockLimit> stockLimit = salesCeilingStockLimitRepository.findById(id);
        if (stockLimit.isPresent()) {
            SalesCeilingStockLimitDto dto = new SalesCeilingStockLimitDto(stockLimit.get());
            if (stockLimit.get().getBranchId() != null) {
                Optional<SalesProductBranchesEntity> branch = salesProductBranchesRepository
                        .findById(stockLimit.get().getBranchId());
                branch.ifPresent(data -> dto.setPlantName(data.getName()));
            }
            if (stockLimit.get().getProductGroup() != null) {
                Optional<SalesProductGroupEntity> productGroup = salesProductGroupRepository
                        .findById(stockLimit.get().getProductGroup());
                productGroup.ifPresent(data -> dto.setProductGroupName(data.getName()));
            }
            return new ResponseEntity<SalesCeilingStockLimitDto>(dto, HttpStatus.OK);
        }
        return ResponseEntity.badRequest().body("Ceiling stock not found");
    }

    public ResponseEntity<?> deleteSalesCeiling(Long id) {
        Optional<SalesCeilingStockLimit> model = salesCeilingStockLimitRepository.findById(id);
        if (model.isPresent()) {
            salesCeilingStockLimitRepository.deleteById(id);
            return new ResponseEntity<Status>(new Status("200", "Deleted successfully!!!"), HttpStatus.OK);
        }
        return new ResponseEntity<Status>(new Status("404", "Ceiling stock limit doesn't Exists!!!"),
                HttpStatus.NOT_FOUND);
    }

    /* --------------Film Order-------------- */

    public ResponseEntity<?> saveFilmOrders(List<SalesProductFilmOrderDto> dtos, User user) {

        List<SalesProductFilmOrder> filmOrders = new ArrayList<>();

        for (SalesProductFilmOrderDto dto : dtos) {
            SalesProductFilmOrder filmOrder = new SalesProductFilmOrder(dto);
            filmOrder.setOrderDate(dateUtil.getISTDate());
            filmOrder.setOrderedBy(user.getId());
            filmOrder.setOrderBranch(user.getBranch_id());
            Optional<SalesProductFilm> optional = saleProductFilmRepository.findById(dto.getFilmId());
            FilmProductValidity validity = filmProductValidityRepository
                    .findByProductGroupIdAndLocationType(optional.get().getProductGroupID(), dto.getLocation());
            if (validity != null) {
                LocalDate bestBefore = LocalDate.now().plusMonths(validity.getMonth());
                System.out.println(bestBefore.with(TemporalAdjusters.lastDayOfMonth()));
                filmOrder.setBestBefore(bestBefore.with(TemporalAdjusters.lastDayOfMonth()));
            }
            if (dto.getCompletedQty() != null) {
                filmOrder.setBalance(dto.getOrderQtyKg() - dto.getCompletedQty());
            }
            filmOrder.setMrnStatus("P");
            saveMaterialFlowRequest(filmOrder, user);
            filmOrders.add(filmOrder);
        }

        salesProductFilmOrderRepository.saveAll(filmOrders);

        return new ResponseEntity<Status>(new Status("200", "Film orders saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updatePendingQty(Long mrnNo, Double completedQty) {
        Optional<SalesProductFilmOrder> optional = salesProductFilmOrderRepository.findByMrnNo(mrnNo);
        if (optional != null) {
            SalesProductFilmOrder film = optional.get();
            film.setPrintedQty(film.getPrintedQty() + completedQty);
            Double pendingQty = film.getBalance() - completedQty;
            film.setBalance(pendingQty);
            if (pendingQty == 0) {
                film.setMrnStatus("C");
            }
            salesProductFilmOrderRepository.save(film);
            return new ResponseEntity<Status>(new Status("200", "Film orders pendingQty updated successfully!!!"),
                    HttpStatus.OK);

        }
        return new ResponseEntity<Status>(new Status("404", "RequestId doesn't Exists!!!"), HttpStatus.NOT_FOUND);
    }

    public void saveMaterialFlowRequest(SalesProductFilmOrder filmOrder, User loggedUser) {
        MaterialRequest materialRequest = createMaterialRequest(filmOrder, loggedUser);
        filmOrder.setMrnNo(materialRequest.getId());
        salesProductFilmOrderRepository.save(filmOrder);
        createRequestItem(materialRequest, filmOrder);
        updateMaterialIssue(materialRequest.getId());
    }

    private MaterialRequest createMaterialRequest(SalesProductFilmOrder res, User loggedUser) {
        MaterialRequestDto materialRequestDto = new MaterialRequestDto();
        materialRequestDto.setRequestFromFirmId(loggedUser.getOrg_id());
        materialRequestDto.setRequestName("Film Order");
        materialRequestDto.setRequestDescription("Film Order");
        materialRequestDto.setRequestById(res.getOrderedBy());
        materialRequestDto.setRequestFromBranchId(res.getOrderBranch());
        materialRequestDto.setRequestFromDepartmentId(loggedUser.getDepartment().getId());
        materialRequestDto.setCreated_date(dateUtil.getDateToLocalDateTime(res.getOrderDate()));
        materialRequestDto.setCreatedBy(res.getOrderedBy());
        materialRequestDto.setStatus("Request Sent");
        MaterialRequest materialRequest = new MaterialRequest(materialRequestDto);
        materialRequest.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        //materialRequest.setRequestDate(Date.from(res.getOrderDate().atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
        materialRequest.setRequestDate(res.getOrderDate());
        return materialRequestRepository.save(materialRequest);
    }

    public void updateMaterialIssue(Long materialRequestId) {

        Optional<MaterialRequest> optionalMaterialFlowRequest = materialRequestRepository.findById(materialRequestId);

        if (optionalMaterialFlowRequest.isPresent()) {
            MaterialRequest materialRequest = optionalMaterialFlowRequest.get();
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
            Branch branch = new Branch();
            branch.setId(3L);
            materialIssue.setIssueBranch(branch);
            DepartmentDetails dept = new DepartmentDetails();
            dept.setId(32L);
            materialIssue.setIssueDepartment(dept);
            List<RequestItem> requestItems = requestItemRepository.findAllByRequestId(materialRequest.getId());

            MaterialIssue result = materialIssueRepository.save(materialIssue);

            for (RequestItem requestItem : requestItems) {
                IssueItems issueItems = new IssueItems();
                issueItems.setIssueItemId(result.getId());
                issueItems.setCreated_date(requestItem.getCreated_date());
                issueItems.setActive(0);
                issueItems.setSelectLot(requestItem.getSelectLot());
                issueItems.setBranchId(requestItem.getIssueBranch().getId());
                issueItems.setDeptId(requestItem.getIssueDepartment().getId());
                issueItems.setItemName(requestItem.getItemName());
                issueItems.setItemId(requestItem.getItemId());
                issueItems.setRequestedQuantity(requestItem.getStockRequired());
                issueItemsRepository.save(issueItems);
            }

        }

    }

    private void createRequestItem(MaterialRequest materialRequest, SalesProductFilmOrder filmOrder) {

        RequestItemDto requestItemDto = new RequestItemDto();
        requestItemDto.setRequestId(materialRequest.getId());
        requestItemDto.setIssueBranchId(3L);
        requestItemDto.setIssueDepartmentId(32L);
        requestItemDto.setSelectLot(String.valueOf(filmOrder.getLot()));
        requestItemDto.setStockQuantity(String.valueOf(filmOrder.getOrderQtyKg()));
        requestItemDto.setStockRequired(String.valueOf(filmOrder.getOrderQtyKg()));
        requestItemDto.setActive(materialRequest.getActive());
        requestItemDto.setCreatedBy(materialRequest.getCreatedBy());
        requestItemDto.setCreated_date(materialRequest.getRequestDate());
        requestItemDto.setUpdatedBy(materialRequest.getUpdatedBy());
        requestItemDto.setUpdated_date(materialRequest.getRequestDate());
        RequestItem requestItem = new RequestItem(requestItemDto);

        Optional<SalesProductFilm> film = saleProductFilmRepository.findById(filmOrder.getFilmId().getId());
        Optional<ProductMaster> product = productMasterRepository.findById(film.get().getProductId());
        product.ifPresent(productMaster -> {
            requestItem.setItemId(productMaster.getId());
            requestItem.setItemName(productMaster.getProductName());
        });
        requestItemRepository.save(requestItem);
    }

    public ResponseEntity<?> getAll(Pageable pageable) {
        Page<SalesProductFilmOrder> film = salesProductFilmOrderRepository.findAll(pageable);
        if (film.isEmpty()) {
            return new ResponseEntity<>(film.toList(), HttpStatus.OK);
        }

        List<SalesProductFilmOrderDto> result = film.getContent().stream().map(data -> {
            SalesProductFilmOrderDto dto = new SalesProductFilmOrderDto(data);
            if (dto.getOrderedBy() != null) {
                Optional<User> userOptional = userRepository.findById(dto.getOrderedBy());
                if (userOptional.get().getEmployeename() != null) {
                    dto.setOrderedByName(userOptional.get().getEmployeename());
                }
            }
            if (data.getSubLocationId() != null) {
                Optional<SalesProductLocationEntity> loc = salesProductionLocationRepository
                        .findById(data.getSubLocationId());
                loc.ifPresent(location -> dto.setSubLocationName(location.getName()));
            }
            return dto;
        }).collect(Collectors.toList());
        PageDataDto<SalesProductFilmOrderDto> pageData = new PageDataDto<>(result, film);
        return new ResponseEntity<>(pageData, HttpStatus.OK);

    }

    public ResponseEntity<?> getFilmOrderById(Long id) {
        Optional<SalesProductFilmOrder> film = salesProductFilmOrderRepository.findById(id);
        if (film.isPresent()) {
            SalesProductFilmOrderDto dto = new SalesProductFilmOrderDto(film.get());
            if (dto.getOrderedBy() != null) {
                Optional<User> userOptional = userRepository.findById(dto.getOrderedBy());
                if (userOptional.get().getEmployeename() != null) {
                    dto.setOrderedByName(userOptional.get().getEmployeename());
                }
            }
            if (film.get().getSubLocationId() != null) {
                Optional<SalesProductLocationEntity> loc = salesProductionLocationRepository
                        .findById(film.get().getSubLocationId());
                loc.ifPresent(location -> dto.setSubLocationName(location.getName()));
            }
            return new ResponseEntity<SalesProductFilmOrderDto>(dto, HttpStatus.OK);
        }
        return ResponseEntity.badRequest().body("film not found");
    }

    public ResponseEntity<?> getMrpDetails(long locationId, Long productGroupId, Long productId) {
        SalesProductFilmPricesEntity film = salesProductFilmPricesRepository
                .findBySalesProductIdAndSalesLocationIdAndSalesProductGroupId(productId, locationId, productGroupId);
        if (film != null) {
            return new ResponseEntity<SalesProductFilmPricesEntity>(film, HttpStatus.OK);
        } else {
            return ResponseEntity.badRequest().body("Item not found");
        }
    }

    public ResponseEntity<?> getStockInPouch(Long branch, Long dept, Long productId, Long lot) {
        List<BinStockQuantity> list = binStockRepository.getStockInPouch(branch, dept, productId, lot);
        if (!list.isEmpty()) {
            List<BinStockQuantityDto> result = list.stream().map(data -> {
                BinStockQuantityDto binStockDto = new BinStockQuantityDto();
                binStockDto.setQuantity(data.getQuantity());
                binStockDto.setDept(data.getDept());
                binStockDto.setBranch(data.getBranch());
                return binStockDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<BinStockQuantityDto>>(result, HttpStatus.OK);
        }
        return new ResponseEntity<List<BinStockQuantityDto>>(new ArrayList<>(), HttpStatus.OK);
    }

    public ResponseEntity<?> getPendingStock(String location, Long subLocationId, Long filmId, Long branch, Long lot) {
        SalesProductFilm film = new SalesProductFilm();
        film.setId(filmId);
        List<SalesProductFilmOrder> list = salesProductFilmOrderRepository
                .findByFilmIdAndLocationAndSubLocationIdAndOrderBranchAndLot(film, location, subLocationId, branch,
                        lot);
        if (!list.isEmpty()) {
            List<SalesProductFilmOrderDto> result = list.stream().map(data -> new SalesProductFilmOrderDto(data))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<SalesProductFilmOrderDto>>(result, HttpStatus.OK);
        } else
            return new ResponseEntity<List<SalesProductFilmOrderDto>>(new ArrayList<>(), HttpStatus.OK);

    }

    public ResponseEntity<?> getFilmOrderByFilter(String status, String orderDate, String pageFlag, Pageable pageable) {

        Date oDate = null;
        if (java.util.Objects.nonNull(orderDate)) {
            oDate = dateUtil.getFilterDate(orderDate);
        }

        if (pageFlag != null && pageFlag.equalsIgnoreCase("N")) {
            List<SalesProductFilmOrder> list = salesProductFilmOrderRepository.findByMrnStatusAndOrderDate(status, oDate);
            List<SalesProductFilmOrderDto> result = list.stream().map(data -> {
                SalesProductFilmOrderDto d = new SalesProductFilmOrderDto(data);
                if (d.getOrderedBy() != null) {
                    Optional<User> userOptional = userRepository.findById(d.getOrderedBy());
                    if (userOptional.get().getEmployeename() != null) {
                        d.setOrderedByName(userOptional.get().getEmployeename());
                    }
                }
                Optional<Branch> branch = branchRepository.findById(data.getOrderBranch());
                d.setBranchName(branch.get().getBranchName());
                updateAltValue(d);
                return d;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<SalesProductFilmOrderDto>>(result, HttpStatus.OK);
        }

        Page<SalesProductFilmOrder> pages = salesProductFilmOrderRepository.findByFilter(status, oDate, pageable);
        if (!pages.isEmpty()) {
            List<SalesProductFilmOrderDto> result = pages.toList().stream().map(data -> {
                SalesProductFilmOrderDto dto = new SalesProductFilmOrderDto(data);
                if (dto.getOrderedBy() != null) {
                    Optional<User> userOptional = userRepository.findById(dto.getOrderedBy());
                    if (userOptional.get().getEmployeename() != null) {
                        dto.setOrderedByName(userOptional.get().getEmployeename());
                    }
                }
                Optional<Branch> branch = branchRepository.findById(data.getOrderBranch());
                dto.setBranchName(branch.get().getBranchName());
                updateAltValue(dto);
                return dto;
            }).collect(Collectors.toList());
            PageDataDto<SalesProductFilmOrderDto> pageData = new PageDataDto<>(result, pages);
            return new ResponseEntity<>(pageData, HttpStatus.OK);
        }

        return new ResponseEntity<>(pages.toList(), HttpStatus.OK);
    }


    public void updateAltValue(SalesProductFilmOrderDto dto) {

        if (dto.getFilmId() != null) {
            Optional<SalesProductFilm> salesProductFilm = saleProductFilmRepository.findById(dto.getFilmId());
            if (salesProductFilm.isPresent()) {
                Optional<ProductMaster> optionalProductMaster = productMasterRepository.findById(salesProductFilm.get().getProductId());
                if (optionalProductMaster.isPresent()) {
                    dto.setAltValue(optionalProductMaster.get().getWhere2());
                }
            }
        }
    }

}
