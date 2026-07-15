package com.aria.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.BillUtil;
import com.aria.utils.BinStockUtil;
import com.aria.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.Tuple;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BillService {

    private static final Logger logger = LoggerFactory.getLogger(BillService.class);

    private static final DecimalFormat decfor = new DecimalFormat("0.00");
    private static final DecimalFormat decfor1 = new DecimalFormat("0");

    @Autowired
    BillInwardRepository billInwardRepository;

    @Autowired
    BillForwardRepository billForwardRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    BillReceiveRepository billReceiveRepository;

    @Autowired
    BillExchangeRepository billExchangeRepository;

    @Autowired
    DirectPurchaseRepository directPurchaseRepository;

    @Autowired
    BillInwardItemReceivedRepository billInwardItemReceivedRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BillUtil billUtil;

    @Autowired
    BillInwardLorryDetailsRepository billInwardLorryDetailsRepository;

    @Autowired
    BillReceiveProductRepository billReceiveProductRepository;

    @Autowired
    BillForwardProductRepository billForwardProductRepository;

    @Autowired
    BillExchangeProductRepository billExchangeProductRepository;

    @Autowired
    BatchMasterRepository batchMasterRepository;

    @Autowired
    DpEntryStoredBinDetailsRepository storedBinDetailsRepository;

    @Autowired
    DpEntryStoredProductRepository storedProductRepository;

    @Autowired
    BinStockLogRepository binStockLogRepository;

    @Autowired
    BinStockUtil binStockUtil;

    @Autowired
    ActionConfigurationRepository actionConfigurationRepository;

    @Autowired
    ActionRepository actionRepository;

    @Autowired
    KpiNameRepository kpiNameRepo;

    @Autowired
    PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    PurchaseOrderItemsRepository orderItemsRepository;

    @Autowired
    RestService restService;

    @Autowired
    HOConfigRESTLogRepository restLogRepository;

    @Autowired
    ProductExcessStorageRepository excessStorageRepository;

    @Autowired
    ExcessShortageProductsRepository excessShortageProductsRepository;

    @Autowired
    QualityInspectionScoreRepo qualityInspectionScoreRepo;

    @Autowired
    ViewAccountsLedgerRepository viewAccountsLedgerRepository;

    @Autowired
    ProductMasterRepository productMasterRepository;

    @Autowired
    SupplierEvaluationReportRepository supplierEvaluationReportRepository;

    @Autowired
    TRNIndexRepo trnIndexRepo;

    @Autowired
    PurchaseOrderItemsRepository purchaseOrderItemsRepository;

    @Autowired
    PatlogRepository patlogRepository;

    @Autowired
    ConsumptionDetailsRepository consumptionDetailsRepository;


    // bill inward

    public ResponseEntity<?> saveBill(BillInwardDto billInwardDto, User loggedUser) {
        BillInward billInward = new BillInward(billInwardDto, true);
        billInward.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        billInward.setCreatedBy(loggedUser.getId());
        billInward.setReceivedDate(dateUtil.getStringTODateWk(billInwardDto.getReceivedDate()));
        billInward.setBillDate(dateUtil.getStringTODateWk(billInwardDto.getBillDate()));
        billInward.setLorryDate(dateUtil.getStringTODateWk(billInwardDto.getLorryDate()));
        billInward.setStatus("billSaved");
        billInwardRepository.save(billInward);
        return new ResponseEntity<Status>(new Status("200", "billInward saved successfully!!!"), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> saveBillInward(BillInwardDto billInwardDto, User loggedUser) {

        BillInward billInward = new BillInward(billInwardDto);

        Optional<BillInward> billwardduplicate = billInwardRepository.findByBillNoAndVendorIdBillDate(billInwardDto.getBillNo(), billInwardDto.getVendorId(), dateUtil.getStringTODateWk(billInwardDto.getBillDate()));

        if (billwardduplicate.isPresent()) {
            return new ResponseEntity<Status>(new Status("400", "Bill Already inwarded!!!"), HttpStatus.BAD_REQUEST);
        }

        billInward.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        billInward.setCreatedBy(loggedUser.getId());
        billInward.setReceivedDate(dateUtil.getStringTODateWk(billInwardDto.getReceivedDate()));
        billInward.setBillDate(dateUtil.getStringTODateWk(billInwardDto.getBillDate()));
        billInward.setLorryDate(dateUtil.getStringTODateWk(billInwardDto.getLorryDate()));
        String billAmt = decfor.format(billInwardDto.getBillAmount());
        System.out.println("billAmout  == " + billAmt);
        billInward.setBillAmount(Double.valueOf(billAmt));
        billInward.setStatus("Inwarded");
        // comment by ALagu purpose of subash need one product at different po-no
        /*
         * if (!billInwardDto.getItemReceives().isEmpty()) { for
         * (BillInwardItemReceivedDto billInwardItemReceivedDto :
         * billInwardDto.getItemReceives()) { String inwardDate =
         * dateUtil.getDateTOStringWk(dateUtil.getISTDate()); BatchMaster batchMaster =
         * checkBatch(billInwardItemReceivedDto, billInwardDto.getBillNo(),
         * billInwardDto.getVendorId(), inwardDate, loggedUser); if (batchMaster ==
         * null) { throw new RuntimeException("BatchId already present in DB"); } } }
         */
        BillInward result = billInwardRepository.save(billInward);

        List<BillInwardItemReceivedDto> billInwardItemReceivedDtos = new ArrayList<BillInwardItemReceivedDto>();
        if (!billInwardDto.getItemReceives().isEmpty()) {
            billInwardItemReceivedDtos = billInwardDto.getItemReceives();
            for (BillInwardItemReceivedDto billInwardProductDto : billInwardDto.getItemReceives()) {

                Date currentDate = dateUtil.getISTDate();
                String inwardDate = dateUtil.getDateTOStringWk(currentDate);

                /*
                 * BatchMaster batchMaster =
                 * getBatch(billInwardProductDto,billInwardDto.getBillNo(),
                 * billInwardDto.getVendorId(), inwardDate,loggedUser);
                 */

                BatchMaster batchMaster = checkBatch(billInwardDto, billInwardProductDto, billInwardDto.getBillNo(), billInwardDto.getVendorId(), inwardDate, loggedUser);
                if (batchMaster != null) {
                    billInwardProductDto.setBatchNo(batchMaster.getBatchno());
                    billInwardProductDto.setBatchId(batchMaster.getId());
                }
                BillInwardItemReceived billInwardProduct = new BillInwardItemReceived(billInwardProductDto);
                billInwardProduct.setCheck_lorry_serial_date(dateUtil.getISTDate());
                billInwardProduct.setInwardId(result.getId());
                billInwardItemReceivedRepository.save(billInwardProduct);


                if (Objects.isNull(billInwardProduct.getItem_type()) || billInwardProduct.getItem_type() == 28) {
                    Optional<Tuple> checkWhetherInspectOrNOt = kpiNameRepo.findByTypeAndId("I", billInwardProduct.getItem_id());
                    if (checkWhetherInspectOrNOt.isPresent()) {
                        // Find whether any config realted to Bill Inward
                        Optional<ActionConfiguration> getByTaskName = actionConfigurationRepository.findByTaskName("Inward Inspection");
                        if (getByTaskName.isPresent()) {
                            System.out.println("action configuration found");
                            ActionConfigurationDto rec = new ActionConfigurationDto(getByTaskName.get());
                            // Actions table insertion
                            if (result != null) {
                                ActionDto dto = new ActionDto();
                                dto.setActionConfigId(rec.getId());
                                dto.setTaskDescription("Inward Inspection Entry for " + billInwardProduct.getBatchNo());
                                dto.setUniqueRef(billInwardProduct.getBatchNo());
                                dto.setOwnerId(loggedUser.getId());
                                dto.setRefType("BatchNo");
                                dto.setStatus("Pending");
                                dto.setQuantity(Long.parseLong(billInwardProduct.getReceivedQuantity().toString()));
                                dto.setBranchId(result.getInwardBranch().getId());
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
        }
        // Close or Open po
        closePurchaseOrder(result.getId());

        if (billInwardDto.getLorryDetails() != null && !billInwardDto.getLorryDetails().isEmpty()) {
            for (BillInwardLorryDto billDetailsDto : billInwardDto.getLorryDetails()) {
                BillInwardLorryDetails billInwardBillDetails = new BillInwardLorryDetails(billDetailsDto);
                billInwardBillDetails.setInwardId(result.getId());
                billInwardLorryDetailsRepository.save(billInwardBillDetails);
            }
        }

        if (result.getProductCategory().equalsIgnoreCase("RawMaterial")) {

            // billUtil.updateBillForward(result, billInwardItemReceivedDtos);
            billUtil.updateDpEntry(result, billInwardDto, billInwardItemReceivedDtos);

        } else {
            billUtil.updateBillExchange(result, billInwardItemReceivedDtos);
            // below to be verified later with arun
            // verifyUtil.updateInwardInspectionEntry(result);
        }
        return new ResponseEntity<Status>(new Status("200", "billInward saved successfully!!!"), HttpStatus.OK);
    }

    public void closePurchaseOrder(Long inwardId) {

        List<BillInwardItemReceived> billInwardItemReceiveds = billInwardItemReceivedRepository.listBillInwardProducts(inwardId);

        if (!billInwardItemReceiveds.isEmpty())

            for (BillInwardItemReceived itemReceived : billInwardItemReceiveds) {

                Optional<PurchaseOrderItems> optional = purchaseOrderItemsRepository.findBy(itemReceived.getPo_id(), itemReceived.getItem_id());
                Double receivedQty = billInwardItemReceivedRepository.sumOfPoQty(itemReceived.getPo_id(), itemReceived.getItem_id());
                System.out.println("receivedQty = " + receivedQty);
                Double poQty = orderItemsRepository.sumOfPoQty(itemReceived.getPo_id(), itemReceived.getItem_id());
                System.out.println("poQty = " + poQty);
                System.out.println("checkQuantity = " + poQty + "  -  " + receivedQty);
                PurchaseOrderItems po = optional.get();
                if (Double.compare(receivedQty, poQty) == 0) {
                    po.setPo_inward_status("Closed");
                } else {
                    po.setPo_inward_status("Open");
                }
                purchaseOrderItemsRepository.save(po);
            }
    }

    public ResponseEntity<?> updateBillInward(BillInwardDto billInwardDto, User loggedUser) {
        BillInward billInward = new BillInward(billInwardDto);
        billInward.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        billInward.setUpdatedBy(loggedUser.getId());
        billInward.setCreatedBy(loggedUser.getId());
        billInward.setReceivedDate(dateUtil.getStringTODateWk(billInwardDto.getReceivedDate()));
        billInward.setBillDate(dateUtil.getStringTODateWk(billInwardDto.getBillDate()));
        billInward.setLorryDate(dateUtil.getStringTODateWk(billInwardDto.getLorryDate()));
        String billAmt = decfor.format(billInwardDto.getBillAmount());
        System.out.println("billAmout  == " + billAmt);
        billInward.setBillAmount(Double.valueOf(billAmt));
        BillInward result = billInwardRepository.save(billInward);
        List<BillInwardItemReceivedDto> billInwardItemReceivedDtos = new ArrayList<BillInwardItemReceivedDto>();
        if (!billInwardDto.getItemReceives().isEmpty()) {
            billInwardItemReceivedDtos = billInwardDto.getItemReceives();
            for (BillInwardItemReceivedDto billInwardProductDto : billInwardDto.getItemReceives()) {
                Date currentDate = dateUtil.getISTDate();
                String inwardDate = dateUtil.getDateTOStringWk(currentDate);
                if (billInwardProductDto.getBatchNo() == null) {
                    BatchMaster batchMaster = checkBatch(billInwardDto, billInwardProductDto, billInwardDto.getBillNo(), billInwardDto.getVendorId(), inwardDate, loggedUser);
                    if (batchMaster != null) {
                        billInwardProductDto.setBatchNo(batchMaster.getBatchno());
                        billInwardProductDto.setBatchId(batchMaster.getId());
                    }
                }
                BillInwardItemReceived billInwardProduct = new BillInwardItemReceived(billInwardProductDto);
                billInwardProduct.setInwardId(result.getId());
                billInwardItemReceivedRepository.save(billInwardProduct);
            }
        }
        if (billInwardDto.getLorryDetails() != null && !billInwardDto.getLorryDetails().isEmpty()) {
            for (BillInwardLorryDto billDetailsDto : billInwardDto.getLorryDetails()) {
                BillInwardLorryDetails billInwardBillDetails = null;
                Optional<BillInwardLorryDetails> billInwardLorryDetails = billInwardLorryDetailsRepository.findBy(result.getId());
                if (billInwardLorryDetails.isPresent()) {
                    billInwardBillDetails = new BillInwardLorryDetails(billDetailsDto);
                    billInwardBillDetails.setId(billInwardLorryDetails.get().getId());
                } else {
                    billInwardBillDetails = new BillInwardLorryDetails(billDetailsDto);
                }
                billInwardBillDetails.setInwardId(result.getId());
                billInwardLorryDetailsRepository.save(billInwardBillDetails);
            }
        }
        if (result.getProductCategory().equalsIgnoreCase("RawMaterial")) {

            Optional<DirectPurchase> optional = directPurchaseRepository.findBy(result.getBillNo(), result.getId());
            if (optional.isPresent()) {
                if (result.getLorry_without_Weight() != null && optional.get().getStock_status().equalsIgnoreCase("")) {
                    billUtil.updateBillForward(result, billInwardItemReceivedDtos);
                }
            } else {
                billUtil.updateDpEntry(result, billInwardDto, billInwardItemReceivedDtos);
            }
        }
        return new ResponseEntity<Status>(new Status("200", "billInward updated successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllBillInward(Pageable pageable) throws UsernameNotFoundException {
        Page<BillInward> billInwardList = billInwardRepository.findAll(pageable);
        List list = billInwardList.toList().stream().map(user -> {
            BillInwardDto billInwardDto = new BillInwardDto(user);
            billInwardDto.setReceivedDate(dateUtil.getDateTOStringWk(user.getReceivedDate()));
            billInwardDto.setBillDate(dateUtil.getDateTOStringWk(user.getBillDate()));
            billInwardDto.setLorryDate(dateUtil.getDateTOStringWk(user.getLorryDate()));
            billInwardDto.setBillAmountStringValue(new BigDecimal(user.getBillAmount()).toPlainString());
            return billInwardDto;
        }).collect(Collectors.toList());
        PageDataDto<BillInwardDto> pageData = new PageDataDto<>(list, billInwardList);
        return new ResponseEntity<PageDataDto<BillInwardDto>>(pageData, HttpStatus.OK);
    }

    public ResponseEntity<?> getTransactionType(String itemType, String lorryNo, String inwardDate) throws UsernameNotFoundException {
        List<Long> itemIds = new ArrayList<>();
        if (itemType.equalsIgnoreCase("S") || itemType.contains("S")) {
            itemIds = productMasterRepository.findBYSesameProducts();
        } else if (itemType.equalsIgnoreCase("G") || itemType.contains("G")) {
            itemIds = productMasterRepository.findBYGroundnutProducts();
        } else if (itemType.equalsIgnoreCase("J") || itemType.contains("J")) {
            itemIds = productMasterRepository.findBYJaggerProducts();
        }
        System.out.println("itemId" + itemIds);

        Date conInwardDate = null;
        if (inwardDate == null) {
            conInwardDate = dateUtil.getDateValue(dateUtil.getISTDate());
            System.out.println("dateUtil.getISTDate() " + dateUtil.getISTDate());
        } else {
            conInwardDate = dateUtil.getStringTODateWk(inwardDate);
            System.out.println("conInwardDate " + conInwardDate);
        }

        Optional<BillInward> optional = billInwardRepository.findByLorrySerialNoOnly(conInwardDate, itemIds);
        List<BillInward> billInwardList = billInwardRepository.findByLorrySerialNo(conInwardDate, itemIds, lorryNo);
        if (optional.isPresent()) {
            LorrySerialNoDto billInwardDto = new LorrySerialNoDto(optional.get());
            billInwardDto.setLorryNoWithCount(billInwardList.size());
            return new ResponseEntity<LorrySerialNoDto>(billInwardDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<LorrySerialNoDto>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> checkEwayBill(Long vendorId, String ewayBillNo) throws UsernameNotFoundException {

        Optional<BillInward> billInward = billInwardRepository.findByEwayBillNo(vendorId, ewayBillNo);
        if (billInward.isPresent()) {
            return new ResponseEntity<Status>(new Status("200", "This EwayBill No Already Exists is This Party!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> getBillInwardDetails(String id) throws UsernameNotFoundException {
        Optional<BillInward> optionalBillInward = billInwardRepository.findById(Long.parseLong(id));
        BillInwardDto billInwardDto = new BillInwardDto(optionalBillInward.get());
        if (optionalBillInward.get().getReceivedDate() != null) {
            billInwardDto.setReceivedDate(dateUtil.getDateTOStringWk(optionalBillInward.get().getReceivedDate()));
        }
        billInwardDto.setBillDate(dateUtil.getDateTOStringWk(optionalBillInward.get().getBillDate()));
        if (optionalBillInward.get().getReceivedDate() != null) {
            billInwardDto.setLorryDate(dateUtil.getDateTOStringWk(optionalBillInward.get().getLorryDate()));
        }
        if (optionalBillInward.get().getBillAmount() != null) {
            billInwardDto.setBillAmountStringValue(new BigDecimal(optionalBillInward.get().getBillAmount()).toPlainString());
        }
        Long inwardBy = null;
        if (optionalBillInward.get().getUpdatedBy() != null) {
            inwardBy = optionalBillInward.get().getUpdatedBy();
        } else if (optionalBillInward.get().getCreatedBy() != null) {
            inwardBy = optionalBillInward.get().getCreatedBy();
        }
        if (inwardBy != null) {
            Optional<User> optional = userRepository.findById(inwardBy);
            if (optional.isPresent()) {
                billInwardDto.setInwardByName(optional.get().getEmployeename());
            }
        }
        for (BillInwardItemReceivedDto itemReceived : billInwardDto.getItemReceives()) {
            Optional<ProductMaster> optional = productMasterRepository.findById(itemReceived.getItemId());
            if (optional.isPresent()) {
                itemReceived.setProductCategoryType(optional.get().getProduct_category_type());
            }
        }
        return new ResponseEntity<BillInwardDto>(billInwardDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteBillInward(String id) throws UsernameNotFoundException {
        try {
            Optional<BillInward> billInwardOptional = billInwardRepository.findById(Long.parseLong(id));
            if (billInwardOptional.isPresent()) {
                BillInward billInward = billInwardOptional.get();
                List<BillInwardItemReceived> itemReceives = billInwardItemReceivedRepository.findBy(billInward.getId());
                if (!itemReceives.isEmpty()) {
                    if (!billInward.getProductCategory().equalsIgnoreCase("RawMaterial")) {
                        for (BillInwardItemReceived billInwardItem : itemReceives) {
                            BillInwardItemReceived billInwardItemReceived = billInwardItem;
                            openPurchaseOrder(billInwardItemReceived);
                        }
                    }
                    billInwardItemReceivedRepository.deleteAll(itemReceives);
                }
                List<BillInwardLorryDetails> lorryDetails = billInwardLorryDetailsRepository.findByList(billInward.getId());
                if (!lorryDetails.isEmpty()) {
                    billInwardLorryDetailsRepository.deleteAll(lorryDetails);
                }

                deleteBillInwardDetailsWithDirectPurchase(id);

                billInwardRepository.delete(billInward);

            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public void deleteBillInwardDetailsWithDirectPurchase(String id) {
        Optional<DirectPurchase> directPurchaseOptional = directPurchaseRepository.findBy(Long.parseLong(id));
        if (directPurchaseOptional.isPresent()) {
            DirectPurchase directPurchase = directPurchaseOptional.get();
            List<DpEntryStoredProduct> dpEntryStoredProducts = storedProductRepository.findBy(directPurchase.getId());
            if (!dpEntryStoredProducts.isEmpty()) {
                for (DpEntryStoredProduct dpEntryStoredProduct : dpEntryStoredProducts) {
                    List<DpEntryStoredBinDetails> binDetails = storedBinDetailsRepository.findByProductId(dpEntryStoredProduct.getId());
                    if (!binDetails.isEmpty()) {
                        storedBinDetailsRepository.deleteAll(binDetails);
                    }
                    storedProductRepository.delete(dpEntryStoredProduct);
                }
            }
            directPurchaseRepository.delete(directPurchase);
        }
    }


    public ResponseEntity<?> filterBillInwardList(Long firmId, String firmName, String referenceNumber, Long vendorId, String vendorName, Long inwardBranchId, String status, String dateFrom, String dateTo, String inwardBranchName, Pageable pageable) throws UsernameNotFoundException {

        HashMap<String, Date> dateHashMap = dateDynamic(dateFrom, dateTo);

        Page<BillInward> billInwardList = billInwardRepository.filterBillInwardList(firmId, firmName, referenceNumber, vendorId, vendorName, inwardBranchId, inwardBranchName, status, dateHashMap.get("dateFrom"), dateHashMap.get("dateTo"), pageable);
        List<BillInwardDto> list = billInwardList.getContent().stream().map(user -> {
            BillInwardDto billInwardDto = new BillInwardDto(user);
            System.out.println(user.getId());
            System.out.println(user.getProductCategory());
            if (user.getReceivedDate() != null) {
                billInwardDto.setReceivedDate(dateUtil.getDateTOStringWk(user.getReceivedDate()));
            }
            if (user.getBillDate() != null) {
                billInwardDto.setBillDate(dateUtil.getDateTOStringWk(user.getBillDate()));
            }
            if (user.getLorryDate() != null) {
                billInwardDto.setLorryDate(dateUtil.getDateTOStringWk(user.getLorryDate()));
            }
            if (user.getBillAmount() != null) {
                billInwardDto.setBillAmountStringValue(new BigDecimal(user.getBillAmount()).toPlainString());
            }
            Optional<DirectPurchase> optionalDirectPurchase = directPurchaseRepository.findBy(billInwardDto.getId());
            if (optionalDirectPurchase.isPresent()) {
                if (!optionalDirectPurchase.get().getStock_status().equalsIgnoreCase("Completed")) {
                    billInwardDto.setAmendmentStatus("Y");
                } else {
                    billInwardDto.setAmendmentStatus("N");
                }

                if (!billInwardDto.getProductCategory().equalsIgnoreCase("RawMaterial")) {
                    if (optionalDirectPurchase.get().getStock_status().equalsIgnoreCase("Pending")) {
                        billInwardDto.setDeleteStatus("Y");
                    } else {
                        billInwardDto.setDeleteStatus("N");
                    }
                } else {
                    billInwardDto.setDeleteStatus("N");
                }

            } else {
                billInwardDto.setAmendmentStatus("Y");
                if (!billInwardDto.getProductCategory().equalsIgnoreCase("RawMaterial")) {
                    billInwardDto.setDeleteStatus("Y");
                } else {
                    billInwardDto.setDeleteStatus("N");
                }
            }
            return billInwardDto;
        }).collect(Collectors.toList());
        PageDataDto<BillInwardDto> pageData = new PageDataDto<>(list, billInwardList);
        return new ResponseEntity<PageDataDto<BillInwardDto>>(pageData, HttpStatus.OK);

    }

    public BatchMaster checkBatch(BillInwardDto billInwardDto, BillInwardItemReceivedDto billInwardItemReceivedDto, String billNo, Long vendorId, String inwardDate, User loggedUser) {
        String batchNo = null;
        if (billInwardItemReceivedDto.getBatchNo() != null) {
            batchNo = billInwardItemReceivedDto.getBatchNo();
        } else {
            if (billInwardDto.getProductCategory().equalsIgnoreCase("RawMaterial")) {
                batchNo = inwardDate + "-" + billInwardDto.getLorrySerialNo() + "#" + billNo + "#" + vendorId + "#" + billInwardItemReceivedDto.getItemId();
                System.out.println("check batch = " + batchNo);
            } else {
                batchNo = inwardDate + "#" + billNo + "#" + vendorId + "#" + billInwardItemReceivedDto.getItemId();
                System.out.println("check batch = " + batchNo);
            }
        }

        Optional<BatchMaster> optionalBatchMaster = batchMasterRepository.findByBatchno(batchNo);
        if (!optionalBatchMaster.isPresent()) {
            BatchMaster batchMaster = new BatchMaster();
            batchMaster.setBatchno(batchNo);
            batchMaster.setDept(loggedUser.getDepartment().getId());
            batchMaster.setBranch(loggedUser.getBranch_id());
            batchMaster.setProductId(billInwardItemReceivedDto.getItemId());
            batchMaster.setCreatedby(loggedUser.getId());
            batchMaster.setBtype("INWARD");
            BatchMaster result = batchMasterRepository.save(batchMaster);
            return result;
        } else {
            return optionalBatchMaster.get();
        }
    }

    public BatchMaster findBatch(BillInwardItemReceivedDto billInwardItemReceivedDto, User loggedUser) {

        Optional<BatchMaster> optionalBatchMaster = batchMasterRepository.findByBatchno(billInwardItemReceivedDto.getBatchNo());
        if (!optionalBatchMaster.isPresent()) {
            BatchMaster batchMaster = new BatchMaster();
            batchMaster.setBatchno(billInwardItemReceivedDto.getBatchNo());
            batchMaster.setDept(loggedUser.getDepartment().getId());
            batchMaster.setBranch(loggedUser.getBranch_id());
            batchMaster.setProductId(billInwardItemReceivedDto.getItemId());
            batchMaster.setCreatedby(loggedUser.getId());
            batchMaster.setBtype("INWARD");
            BatchMaster result = batchMasterRepository.save(batchMaster);
            return result;
        } else {
            return optionalBatchMaster.get();
        }
    }

    /*
     * public BatchMaster getBatch(BillInwardItemReceivedDto
     * billInwardItemReceivedDto, String billNo, Long vendorId, String inwardDate,
     * User loggedUser) { String batchNo = inwardDate + "-" +
     * billInwardItemReceivedDto.getLorrySerialNo() + "#" + billNo + "#" + vendorId
     * + "#" + billInwardItemReceivedDto.getItemId();
     * System.out.println("get batch = " + batchNo); Optional<BatchMaster>
     * optionalBatchMaster = batchMasterRepository.findByBatchno(batchNo); if
     * (optionalBatchMaster.isPresent()) { return optionalBatchMaster.get(); } else
     * { return null; } }
     */

    // bill forward

    public ResponseEntity<?> updateForward(BillForwardDto billForwardDto, User loggedUser) {
        BillForward billForward = new BillForward(billForwardDto);
        billForward.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        billForward.setUpdatedBy(loggedUser.getId());
        billForward.setBillDate(dateUtil.getStringTODateWk(billForwardDto.getBillDate()));
        billForward.setBillInwardDate(dateUtil.getStringTODateWk(billForwardDto.getBillInwardDate()));
        billForward.setForwardDate(dateUtil.getStringTODateWk(billForwardDto.getForwardDate()));
        billForward.setStatus("BillForwarded");
        BillForward result = billForwardRepository.save(billForward);
        if (!billForwardDto.getBillDetails().isEmpty()) {
            for (BillForwardProductDto billForwardProductDto : billForwardDto.getBillDetails()) {
                BillForwardProduct billForwardProduct = new BillForwardProduct(billForwardProductDto);
                billForwardProduct.setForwardId(result.getId());
                billForwardProductRepository.save(billForwardProduct);
            }
        }
        updateReceived(result.getId(), billForwardDto, loggedUser);
        return new ResponseEntity<Status>(new Status("200", "billForward updated successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateForward(List<BillForwardDto> billForwardDtoList, User loggedUser) {
        List<Long> forwardIdList = new ArrayList<>();
        if (billForwardDtoList != null && !billForwardDtoList.isEmpty()) {
            for (BillForwardDto billForwardDto : billForwardDtoList) {
                BillForward billForward = new BillForward(billForwardDto);
                billForward.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                billForward.setUpdatedBy(loggedUser.getId());
                billForward.setBillDate(dateUtil.getStringTODateWk(billForwardDto.getBillDate()));
                billForward.setBillInwardDate(dateUtil.getStringTODateWk(billForwardDto.getBillInwardDate()));
                billForward.setForwardDate(dateUtil.getStringTODateWk(billForwardDto.getForwardDate()));
                billForward.setStatus("BillForwarded");
                BillForward result = billForwardRepository.save(billForward);
                if (!billForwardDto.getBillDetails().isEmpty()) {
                    for (BillForwardProductDto billForwardProductDto : billForwardDto.getBillDetails()) {
                        BillForwardProduct billForwardProduct = new BillForwardProduct(billForwardProductDto);
                        billForwardProduct.setForwardId(result.getId());
                        billForwardProductRepository.save(billForwardProduct);
                    }
                }
                updateRestService(result.getId(), loggedUser);
                forwardIdList.add(result.getId());
                // updateReceived(result.getId(), billForwardDto, loggedUser);
            }
            if (forwardIdList.size() > 0) {
                List<BillForwardDto> result = getForwardList(forwardIdList);
                if (result.isEmpty()) {
                    return new ResponseEntity<Status>(new Status("400", "Some Field Value Missing Please fill & Forward!!!"), HttpStatus.OK);
                } else {
                    return new ResponseEntity<List<BillForwardDto>>(result, HttpStatus.OK);
                }
            }
            {
                return new ResponseEntity<Status>(new Status("400", "No data FOUND"), HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<Status>(new Status("400", "No data FOUND"), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> listAllBillForward(Pageable pageable) throws UsernameNotFoundException {
        Page<BillForward> billForwards = billForwardRepository.findAll(pageable);
        List<BillForwardDto> list = billForwards.toList().stream().map(user -> {
            BillForwardDto billForwardDto = new BillForwardDto(user);
            billForwardDto.setBillDate(dateUtil.getDateTOStringWk(user.getBillDate()));
            billForwardDto.setBillInwardDate(dateUtil.getDateTOStringWk(user.getBillInwardDate()));
            if (user.getForwardDate() != null) {
                billForwardDto.setForwardDate(dateUtil.getDateTOStringWk(user.getForwardDate()));
            }
            return billForwardDto;
        }).collect(Collectors.toList());

        PageDataDto<BillForwardDto> pageData = new PageDataDto<>(list, billForwards);
        return new ResponseEntity<PageDataDto<BillForwardDto>>(pageData, HttpStatus.OK);
    }

    public ResponseEntity<?> filterBillForwardList(Long firmId, String firmName, String billNumber, Long fromBranchId, String fromBranchName, Long toBranchId, String toBranchName, String status, String dateFrom, String dateTo, Pageable pageable) throws UsernameNotFoundException {

        HashMap<String, Date> dateHashMap = dateDynamic(dateFrom, dateTo);

        Page<BillForward> billForwards = billForwardRepository.filterBillForwardList(firmId, firmName, billNumber, fromBranchId, fromBranchName, toBranchId, toBranchName, status, dateHashMap.get("dateFrom"), dateHashMap.get("dateTo"), pageable);
        List<BillForwardDto> list = billForwards.getContent().stream().map(user -> {
            BillForwardDto billForwardDto = new BillForwardDto(user);
            billForwardDto.setBillDate(dateUtil.getDateTOStringWk(user.getBillDate()));
            billForwardDto.setBillInwardDate(dateUtil.getDateTOStringWk(user.getBillInwardDate()));
            if (user.getForwardDate() != null) {
                billForwardDto.setForwardDate(dateUtil.getDateTOStringWk(user.getForwardDate()));
            }
            Optional<BillInward> optionalBillInward = billInwardRepository.findById(user.getBill_ref_id());
            if (optionalBillInward.isPresent()) {
                if (optionalBillInward.get().getBillAmount() != null) {
                    billForwardDto.setBillAmount(optionalBillInward.get().getBillAmount());
                    billForwardDto.setBillAmountStringValue(new BigDecimal(optionalBillInward.get().getBillAmount()).toPlainString());
                }
                Optional<DirectPurchase> directPurchaseOptional = directPurchaseRepository.findBy(optionalBillInward.get().getId());
                if (directPurchaseOptional.isPresent()) {
                    if (directPurchaseOptional.get().getExcessOrShortage() != null && directPurchaseOptional.get().getExcessOrShortage() == true) {
                        Optional<ProductExcessStorage> productExcessStorage = excessStorageRepository.findBy(optionalBillInward.get().getVendor().getId(), optionalBillInward.get().getBillNo());
                        if (productExcessStorage.isPresent()) {
                            if (productExcessStorage.get().getStatus() != null && productExcessStorage.get().getStatus().equalsIgnoreCase("Verified")) {
                                billForwardDto.setShexStatus("C");
                            } else {
                                billForwardDto.setShexStatus("P");
                            }
                        } else {
                            billForwardDto.setShexStatus("P");
                        }
                    } else {
                        billForwardDto.setShexStatus("C");
                    }
                }
            }
            return billForwardDto;
        }).collect(Collectors.toList());
        PageDataDto<BillForwardDto> pageData = new PageDataDto<>(list, billForwards);
        return new ResponseEntity<PageDataDto<BillForwardDto>>(pageData, HttpStatus.OK);
    }

    public ResponseEntity<?> getBillForwardDetails(String id) throws UsernameNotFoundException {
        Optional<BillForward> optionalBillForward = billForwardRepository.findById(Long.parseLong(id));
        BillForwardDto billForwardDto = new BillForwardDto(optionalBillForward.get());
        billForwardDto.setBillDate(dateUtil.getDateTOStringWk(optionalBillForward.get().getBillDate()));
        billForwardDto.setBillInwardDate(dateUtil.getDateTOStringWk(optionalBillForward.get().getBillInwardDate()));
        if (optionalBillForward.get().getForwardDate() != null) {
            billForwardDto.setForwardDate(dateUtil.getDateTOStringWk(optionalBillForward.get().getForwardDate()));
        }
        Optional<BillInward> optional = billInwardRepository.findById(billForwardDto.getBillInwardId());
        if (optional.isPresent()) {
            billForwardDto.setLorryWithWeight(optional.get().getLorry_with_Weight());
            billForwardDto.setLorryWithoutWeight(optional.get().getLorry_without_Weight());
            billForwardDto.setLorrySerialNo(optional.get().getLorrySerialNo());
            billForwardDto.setProductCategory(optional.get().getProductCategory());
        }

        return new ResponseEntity<BillForwardDto>(billForwardDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteBillForward(String id) throws UsernameNotFoundException {
        try {
            Optional<BillForward> billInwardOptional = billForwardRepository.findById(Long.parseLong(id));
            if (billInwardOptional.isPresent()) {
                BillForward billInward = billInwardOptional.get();
                billForwardRepository.delete(billInward);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    // bill receive

    public void updateReceived(Long id, BillForwardDto billForwardDto, User loggedUser) {
        Optional<BillForward> optional = billForwardRepository.findById(id);
        if (optional.isPresent()) {
            BillForward billForward = optional.get();
            BillReceive billReceive = new BillReceive(billForward);
            billReceive.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            billReceive.setCreatedBy(loggedUser.getId());
            billReceive.setStatus("Pending");
            BillReceive result = billReceiveRepository.save(billReceive);
            updateBillReceiveProduct(result, billForwardDto);
        }
    }

    public void updateBillReceiveProduct(BillReceive billReceive, BillForwardDto billForward) {
        for (BillForwardProductDto billInwardProduct : billForward.getBillDetails()) {
            BillReceiveProduct billForwardProduct = new BillReceiveProduct(billInwardProduct);
            billForwardProduct.setReceiveId(billReceive.getId());
            billReceiveProductRepository.save(billForwardProduct);
        }
    }

    public void exchangeUpdateReceived(Long id, BillExchangeDto billExchangeDto, User loggedUser) {
        Optional<BillExchange> optional = billExchangeRepository.findById(id);
        if (optional.isPresent()) {
            BillExchange billExchange = optional.get();
            BillReceive billReceive = new BillReceive(billExchange);
            billReceive.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            billReceive.setCreatedBy(loggedUser.getId());
            billReceive.setStatus("Pending");
            BillReceive result = billReceiveRepository.save(billReceive);
            updateBillReceiveProduct(result, billExchangeDto);
        }
    }

    public void updateBillReceiveProduct(BillReceive billReceive, BillExchangeDto billForward) {
        for (BillExchangeProductDto billInwardProduct : billForward.getBillDetails()) {
            BillReceiveProduct billForwardProduct = new BillReceiveProduct(billInwardProduct);
            billForwardProduct.setReceiveId(billReceive.getId());
            billReceiveProductRepository.save(billForwardProduct);
        }
    }

    public ResponseEntity<?> saveReceive(BillReceiveDto billReceiveDto, User loggedUser) {
        BillReceive billReceive = new BillReceive(billReceiveDto);
        billReceive.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        billReceive.setCreatedBy(loggedUser.getId());
        billReceive.setBillDate(dateUtil.getStringTODateWk(billReceiveDto.getBillDate()));
        billReceive.setBillInwardDate(dateUtil.getStringTODateWk(billReceiveDto.getBillInwardDate()));
        billReceive.setForwardDate(dateUtil.getStringTODateWk(billReceiveDto.getForwardDate()));
        billReceiveRepository.save(billReceive);
        return new ResponseEntity<Status>(new Status("200", "billReceive saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateReceive(BillReceiveDto billReceiveDto, User loggedUser) {
        Optional<BillReceive> optional = billReceiveRepository.findById(billReceiveDto.getId());
        if (optional.isPresent()) {
            BillReceive billReceive = optional.get();
            billReceive.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            billReceive.setUpdatedBy(loggedUser.getId());
            billReceive.setReceivedDate(dateUtil.getISTDate());
            billReceive.setReceivedPerson(loggedUser);
            Branch branch = new Branch();
            branch.setId(loggedUser.getBranch_id());
            billReceive.setReceivedBranch(branch);
            billReceive.setStatus("Received");
            billReceiveRepository.save(billReceive);
            return new ResponseEntity<Status>(new Status("200", "billReceive updated successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "billReceive not updated successfully!!!"), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> listAllBillReceive(Pageable pageable) throws UsernameNotFoundException {
        Page<BillReceive> billReceives = billReceiveRepository.findAll(pageable);
        return new ResponseEntity<List<BillReceiveDto>>(billReceives.toList().stream().map(billReceive -> {
            BillReceiveDto billReceiveDto = new BillReceiveDto(billReceive);
            billReceiveDto.setBillDate(dateUtil.getDateTOStringWk(billReceive.getBillDate()));
            billReceiveDto.setBillInwardDate(dateUtil.getDateTOStringWk(billReceive.getBillInwardDate()));
            if (billReceive.getForwardDate() != null) {
                billReceiveDto.setForwardDate(dateUtil.getDateTOStringWk(billReceive.getForwardDate()));
            }
            if (billReceive.getReceivedDate() != null) {
                billReceiveDto.setReceivedDate(dateUtil.getDateTOStringWk(billReceive.getReceivedDate()));
            }
            if (billReceive.getExchangeDate() != null) {
                billReceiveDto.setExchangeDate(dateUtil.getDateTOStringWk(billReceive.getExchangeDate()));
            }

            return billReceiveDto;
        }).collect(Collectors.toList()), HttpStatus.OK);
    }

    public ResponseEntity<?> filterBillReceiveList(Long firmId, String firmName, String billNumber, Long fromBranchId, Long toBranchId, String status, String dateFrom, String dateTo, Pageable pageable) throws UsernameNotFoundException {

        HashMap<String, Date> dateHashMap = dateDynamic(dateFrom, dateTo);

        Page<BillReceive> billReceives = billReceiveRepository.filterBillReceiveList(firmId, firmName, billNumber, fromBranchId, toBranchId, status, dateHashMap.get("dateFrom"), dateHashMap.get("dateTo"), pageable);

        List<BillReceiveDto> list = billReceives.getContent().stream().map(billReceive -> {
            BillReceiveDto billReceiveDto = new BillReceiveDto(billReceive);
            billReceiveDto.setBillDate(dateUtil.getDateTOStringWk(billReceive.getBillDate()));
            billReceiveDto.setBillInwardDate(dateUtil.getDateTOStringWk(billReceive.getBillInwardDate()));
            if (billReceive.getForwardDate() != null) {
                billReceiveDto.setForwardDate(dateUtil.getDateTOStringWk(billReceive.getForwardDate()));
            }
            if (billReceive.getReceivedDate() != null) {
                billReceiveDto.setReceivedDate(dateUtil.getDateTOStringWk(billReceive.getReceivedDate()));
            }
            if (billReceive.getExchangeDate() != null) {
                billReceiveDto.setExchangeDate(dateUtil.getDateTOStringWk(billReceive.getExchangeDate()));
            }
            // updateBillReceiveDto(billReceiveDto);
            return billReceiveDto;
        }).collect(Collectors.toList());
        PageDataDto<BillReceiveDto> pageData = new PageDataDto<>(list, billReceives);
        return new ResponseEntity<PageDataDto<BillReceiveDto>>(pageData, HttpStatus.OK);
    }

    public ResponseEntity<?> getBillReceiveDetails(String id) throws UsernameNotFoundException {
        Optional<BillReceive> optionalBillReceive = billReceiveRepository.findById(Long.parseLong(id));
        BillReceiveDto billReceiveDto = new BillReceiveDto(optionalBillReceive.get());
        billReceiveDto.setBillDate(dateUtil.getDateTOStringWk(optionalBillReceive.get().getBillDate()));
        billReceiveDto.setBillInwardDate(dateUtil.getDateTOStringWk(optionalBillReceive.get().getBillInwardDate()));
        if (optionalBillReceive.get().getForwardDate() != null) {
            billReceiveDto.setForwardDate(dateUtil.getDateTOStringWk(optionalBillReceive.get().getForwardDate()));
        }
        if (optionalBillReceive.get().getReceivedDate() != null) {
            billReceiveDto.setReceivedDate(dateUtil.getDateTOStringWk(optionalBillReceive.get().getReceivedDate()));
        } else {
            billReceiveDto.setReceivedDate(dateUtil.getDateTOStringWk(dateUtil.getISTDate()));
        }
        updateBillReceiveDto(billReceiveDto);
        return new ResponseEntity<BillReceiveDto>(billReceiveDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteBillReceive(String id) throws UsernameNotFoundException {
        try {
            Optional<BillReceive> billReceiveOptional = billReceiveRepository.findById(Long.parseLong(id));
            if (billReceiveOptional.isPresent()) {
                BillReceive billReceive = billReceiveOptional.get();
                billReceiveRepository.delete(billReceive);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    // bill Exchange

    public ResponseEntity<?> saveExchange(BillExchangeDto billExchangeDto, User loggedUser) {
        BillExchange billExchange = new BillExchange(billExchangeDto);
        billExchange.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        billExchange.setCreatedBy(loggedUser.getId());
        billExchange.setBillDate(dateUtil.getStringTODateWk(billExchangeDto.getBillDate()));
        billExchange.setBillInwardDate(dateUtil.getStringTODateWk(billExchangeDto.getBillInwardDate()));
        billExchange.setHandOverDate(dateUtil.getStringTODateWk(billExchangeDto.getHandOverDate()));
        billExchange.setStatus("Pending");
        billExchangeRepository.save(billExchange);
        return new ResponseEntity<Status>(new Status("200", "bill exchange saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> verifyExchange(BillVerifyDto billverifyDto, User loggedUser) {
        Optional<BillExchange> billverifychange = billExchangeRepository.findById(billverifyDto.getExchangeId());
        Optional<User> verifyuserDetails = userRepository.findByUsername(billverifyDto.getUserName());
        if (billverifychange.isPresent()) {
            if (verifyuserDetails.isPresent()) {
                billverifychange.get().setVerificationStatus("Verified");
                billverifychange.get().setVerificationStatus("Verified");
                billverifychange.get().setExchangePerson(verifyuserDetails.get());
                billverifychange.get().setVerificationDate(dateUtil.getISTDate());
                billExchangeRepository.save(billverifychange.get());
            }
        }
        return new ResponseEntity<Status>(new Status("200", "bill verification successfull!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateExchange(List<BillExchangeDto> billExchangeDtoList, User loggedUser) {
        if (billExchangeDtoList != null && !billExchangeDtoList.isEmpty()) {
            for (BillExchangeDto billExchangeDto : billExchangeDtoList) {
                Optional<BillExchange> optionalBillExchange = billExchangeRepository.findById(billExchangeDto.getId());
                if (optionalBillExchange.isPresent()) {
                    BillExchange billExchange = optionalBillExchange.get();
                    billExchange.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    billExchange.setUpdatedBy(loggedUser.getId());
                    billExchange.setBillNumber(billExchangeDto.getBillNumber());
                    if (billExchangeDto.getHandOverToBranch() != null) {
                        Branch branch = new Branch();
                        branch.setId(billExchangeDto.getHandOverToBranch());
                        billExchange.setHandOverToBranch(branch);
                    }
                    if (billExchangeDto.getExchangeInitiativePerson() != null) {
                        User user = new User();
                        user.setId(billExchangeDto.getExchangeInitiativePerson());
                        billExchange.setExchangeInitiativePerson(user);
                    }
                    if (billExchangeDto.getExchangePerson() != null) {
                        User user = new User();
                        user.setId(billExchangeDto.getExchangePerson());
                        billExchange.setExchangePerson(user);
                    }
                    if (billExchangeDto.getHandOverToDepartment() != null) {
                        DepartmentDetails details = new DepartmentDetails();
                        details.setId(billExchangeDto.getHandOverToDepartment());
                        billExchange.setHandOverDepartment(details);
                    }
                    billExchange.setHandOverType(billExchangeDto.getHandOverType());
                    billExchange.setBillDate(dateUtil.getStringTODateWk(billExchangeDto.getBillDate()));
                    billExchange.setBillInwardDate(dateUtil.getStringTODateWk(billExchangeDto.getBillInwardDate()));
                    billExchange.setHandOverDate(dateUtil.getStringTODateWk(billExchangeDto.getHandOverDate()));
                    billExchange.setStatus("Exchanged");
                    BillExchange rs = billExchangeRepository.save(billExchange);
                    // exchangeUpdateReceived(result.getId(), billExchangeDto, loggedUser);

                    billUtil.invokeUpdateDpEntry(rs, billExchange.getBillInwardId());

                }
            }
            return new ResponseEntity<Status>(new Status("200", "bill exchange updated successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "No data FOUND"), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> listAllBillExchange(Pageable pageable) throws UsernameNotFoundException {
        Page<BillExchange> billExchanges = billExchangeRepository.findAll(pageable);
        List list = billExchanges.toList().stream().map(user -> {
            BillExchangeDto billExchangeDto = new BillExchangeDto(user);
            billExchangeDto.setBillDate(dateUtil.getDateTOStringWk(user.getBillDate()));
            billExchangeDto.setBillInwardDate(dateUtil.getDateTOStringWk(user.getBillInwardDate()));
            if (user.getHandOverDate() != null) {
                billExchangeDto.setHandOverDate(dateUtil.getDateTOStringWk(user.getHandOverDate()));
            }
            return billExchangeDto;
        }).collect(Collectors.toList());
        PageDataDto<BillExchangeDto> pageData = new PageDataDto<>(list, billExchanges);
        return new ResponseEntity<PageDataDto<BillExchangeDto>>(pageData, HttpStatus.OK);
    }

    public ResponseEntity<?> filterBillExchangeList(Long firmId, String firmName, String billNumber, Long fromBranchId, Long toBranchId, String toBranchName, String status, String dateFrom, String dateTo, Long deliveryBranchId, String deliveryBranchName, Pageable pageable) throws UsernameNotFoundException {

        HashMap<String, Date> dateHashMap = dateDynamic(dateFrom, dateTo);

        Page<BillExchange> billExchanges = billExchangeRepository.filterBillExchangeList(firmId, firmName, billNumber, toBranchId, toBranchName, status, dateHashMap.get("dateFrom"), dateHashMap.get("dateTo"), deliveryBranchId, deliveryBranchName, pageable);
        List<BillExchangeDto> list = billExchanges.toList().stream().map(user -> {
            BillExchangeDto billExchangeDto = new BillExchangeDto(user);
            billExchangeDto.setBillDate(dateUtil.getDateTOStringWk(user.getBillDate()));
            billExchangeDto.setBillInwardDate(dateUtil.getDateTOStringWk(user.getBillInwardDate()));
            if (user.getBillAmount() != null) {
                billExchangeDto.setBillAmountStringValue(new BigDecimal(user.getBillAmount()).toPlainString());
            }
            if (user.getHandOverDate() != null) {
                billExchangeDto.setHandOverDate(dateUtil.getDateTOStringWk(user.getHandOverDate()));
            }
            return billExchangeDto;
        }).collect(Collectors.toList());
        PageDataDto<BillExchangeDto> pageData = new PageDataDto<>(list, billExchanges);
        return new ResponseEntity<PageDataDto<BillExchangeDto>>(pageData, HttpStatus.OK);
    }

    public ResponseEntity<?> getBillExchangeDetails(String id) throws UsernameNotFoundException {
        Optional<BillExchange> optionalBillExchange = billExchangeRepository.findById(Long.parseLong(id));
        BillExchangeDto billExchangeDto = new BillExchangeDto(optionalBillExchange.get());
        billExchangeDto.setBillDate(dateUtil.getDateTOStringWk(optionalBillExchange.get().getBillDate()));
        if (optionalBillExchange.get().getBillAmount() != null) {
            billExchangeDto.setBillAmountStringValue(new BigDecimal(optionalBillExchange.get().getBillAmount()).toPlainString());
        }
        billExchangeDto.setBillInwardDate(dateUtil.getDateTOStringWk(optionalBillExchange.get().getBillInwardDate()));
        if (optionalBillExchange.get().getHandOverDate() != null) {
            billExchangeDto.setHandOverDate(dateUtil.getDateTOStringWk(optionalBillExchange.get().getHandOverDate()));
        }
        return new ResponseEntity<BillExchangeDto>(billExchangeDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteBillExchange(String id, String type) throws UsernameNotFoundException {
        try {
            Optional<BillExchange> billExchangeOptional = billExchangeRepository.findById(Long.parseLong(id));
            if (billExchangeOptional.isPresent()) {
                BillExchange billExchange = billExchangeOptional.get();
                if (type != null && type.equalsIgnoreCase("rework")) {


                    Optional<DirectPurchase> optionalDirectPurchase = directPurchaseRepository.findBy(billExchange.getBillNumber(), billExchange.getBillInwardId());
                    if (optionalDirectPurchase.isPresent()) {

                        if (optionalDirectPurchase.get().getStock_status().equalsIgnoreCase("Pending")) {

                            deleteDirectPurchaseDetails(optionalDirectPurchase.get().getId());

                            billExchange.setStatus("Pending");

                            billExchangeRepository.save(billExchange);

                            return new ResponseEntity<Status>(new Status("200", "Bill Exchange Rework SuccessFully"), HttpStatus.OK);

                        } else {
                            return new ResponseEntity<Status>(new Status("400", "Stock Inward Status Updated Already!!!!"), HttpStatus.OK);
                        }
                    } else {

                        billExchange.setStatus("Pending");

                        billExchangeRepository.save(billExchange);

                        return new ResponseEntity<Status>(new Status("200", "Bill Exchange Rework SuccessFully"), HttpStatus.OK);
                    }

                } else {
                    billExchangeRepository.delete(billExchange);
                    return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
                }
            } else {
                return new ResponseEntity<Status>(new Status("400", "Deleted SuccessFully"), HttpStatus.NOT_FOUND);
            }

        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    // direct Purchase

    public ResponseEntity<?> saveDirectPurchase(DirectPurchaseDto directPurchaseDto, User loggedUser) {
        Long billRefId = null;
        DirectPurchase directPurchase = new DirectPurchase(directPurchaseDto);
        directPurchase.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        directPurchase.setCreatedBy(loggedUser.getId());
        directPurchase.setStorageDate(dateUtil.getStringTODateWk(directPurchaseDto.getStorageDate()));
        DirectPurchase result = directPurchaseRepository.save(directPurchase);
        System.out.println(result.getId());
        if (directPurchaseDto.getStoredProducts() != null && !directPurchaseDto.getStoredProducts().isEmpty()) {

            for (DpEntryStoredProductDto storedProductDto : directPurchaseDto.getStoredProducts()) {
                Optional<DpEntryStoredProduct> checkStoredProduct = storedProductRepository.findBy(storedProductDto.getId(), result.getId(), storedProductDto.getBatchNo(), storedProductDto.getBatchId());
                DpEntryStoredProduct storedProduct = null;
                DpEntryStoredProduct productResult = null;
                if (checkStoredProduct.isPresent()) {
                    storedProduct = checkStoredProduct.get();

                    storedProduct.setPo_number(storedProductDto.getPoNumber());
                    Color color = new Color();
                    color.setId(storedProductDto.getColorId());
                    storedProduct.setColor(color);
                    storedProduct.setPo_id(storedProductDto.getPoId());
                    storedProduct.setBill_ref_id(storedProductDto.getBillRefId());
                    storedProduct.setBatchId(storedProductDto.getBatchId());
                    storedProduct.setLot(storedProductDto.getLot());
                    storedProduct.setBatchNo(storedProductDto.getBatchNo());
                    storedProduct.setItemId(storedProductDto.getItemId());
                    storedProduct.setItemType(storedProductDto.getItemType());
                    storedProduct.setItemName(storedProductDto.getItemName());
                    storedProduct.setPo_id(storedProductDto.getPoId());
                    storedProduct.setEntryId(result.getId());
                    productResult = storedProductRepository.save(storedProduct);
                } else {
                    storedProduct = new DpEntryStoredProduct(storedProductDto);
                    storedProduct.setEntryId(result.getId());
                    productResult = storedProductRepository.save(storedProduct);
                }
                if (storedProductDto.getBinDetails() != null && !storedProductDto.getBinDetails().isEmpty()) {
                    for (DpEntryStoredBinDetailsDto binDetails : storedProductDto.getBinDetails()) {

                        Optional<DpEntryStoredBinDetails> checkStoredBinDetails = storedBinDetailsRepository.findByProductBatch(productResult.getId(), binDetails.getBinId());
                        DpEntryStoredBinDetails storedBinDetails = null;
                        DpEntryStoredBinDetails resultBinDetails = null;
                        if (checkStoredBinDetails.isPresent()) {
                            System.out.println("########");
                            System.out.println("already Stored Bin ");
                            System.out.println("########");
                            storedBinDetails = checkStoredBinDetails.get();
                            storedBinDetails.setBinId(binDetails.getBinId());
                            storedBinDetails.setEntryProductId(productResult.getId());
                            storedBinDetails.setAllottedQuantity(binDetails.getAllottedQuantity());
                            resultBinDetails = storedBinDetailsRepository.save(storedBinDetails);
                        } else {
                            System.out.println("########");
                            System.out.println("Newly Stored Bin ");
                            System.out.println("########");
                            storedBinDetails = new DpEntryStoredBinDetails(binDetails);
                            storedBinDetails.setEntryProductId(productResult.getId());
                            resultBinDetails = storedBinDetailsRepository.save(storedBinDetails);
                        }

                        // binStockUtil.updateBinStock(result, productResult, resultBinDetails,
                        // loggedUser);

                    }
                }
            }

            result.setStock_status("Stock_Inwarded");
            directPurchaseRepository.save(result);

        }

        return new ResponseEntity<Status>(new Status("200", "Stock Inward successful!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateDirectPurchase(DirectPurchaseDto directPurchaseDto, User loggedUser) {
        Date currentDate = dateUtil.getISTDate();
        DirectPurchase directPurchase = new DirectPurchase(directPurchaseDto);
        directPurchase.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        directPurchase.setUpdatedBy(loggedUser.getId());
        directPurchase.setStorageDate(currentDate);
        DirectPurchase result = directPurchaseRepository.save(directPurchase);
        deleteAllStoredProduct(result);
        if (directPurchaseDto.getStoredProducts() != null && !directPurchaseDto.getStoredProducts().isEmpty()) {

            for (DpEntryStoredProductDto storedProductDto : directPurchaseDto.getStoredProducts()) {

                DpEntryStoredProduct storedProduct = new DpEntryStoredProduct(storedProductDto);
                storedProduct.setEntryId(result.getId());
                DpEntryStoredProduct productResult = storedProductRepository.save(storedProduct);

                if(storedProduct.getItemType()!=null && storedProduct.getItemType() == 30){
                    // update raw material last purchase rate
                    if(storedProductDto.getConsumptionDetails()  !=null && !storedProductDto.getConsumptionDetails().isEmpty()){
                        for(ConsumptionDetailDto consumptionDetailsDto : storedProductDto.getConsumptionDetails()){
                            Optional<ConsumptionDetails> consumptionDetails = consumptionDetailsRepository.findById(storedProduct.getId());
                            if(consumptionDetails.isPresent()){
                                ConsumptionDetails consumption = consumptionDetails.get();
                                consumption.setDpEntryStoredProductId(storedProduct.getId());
                                consumption.setConsumptionName(consumptionDetailsDto.getConsumptionName());
                                consumption.setConsumptionCost(consumptionDetailsDto.getConsumptionCost());
                                consumption.setConsumptionQty(consumptionDetailsDto.getConsumptionQty());
                                consumption.setRemarks(consumptionDetailsDto.getRemarks());
                                consumption.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                                consumption.setConsumptionDatefrom(consumptionDetailsDto.getConsumptionDatefrom());
                                consumption.setConsumptionDateto(consumptionDetailsDto.getConsumptionDateto());
                                consumption.setConsumedBy(loggedUser.getId());
                                consumptionDetailsRepository.save(consumption);
                            } else {
                                ConsumptionDetails consumption =new ConsumptionDetails(consumptionDetailsDto);
                                consumption.setDpEntryStoredProductId(storedProduct.getId());
                                consumption.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                                consumptionDetailsRepository.save(consumption);
                            }
                        }
                    }
                }

                if (storedProductDto.getBinDetails() != null && !storedProductDto.getBinDetails().isEmpty()) {
                    for (DpEntryStoredBinDetailsDto binDetails : storedProductDto.getBinDetails()) {
                        Optional<DpEntryStoredBinDetails> checkStoredBinDetails = storedBinDetailsRepository.findByProductBatch(productResult.getId(), binDetails.getBinId());
                        DpEntryStoredBinDetails storedBinDetails = null;
                        DpEntryStoredBinDetails resultBinDetails = null;
                        if (checkStoredBinDetails.isPresent()) {
                            storedBinDetails = checkStoredBinDetails.get();
                            storedBinDetails.setBinId(binDetails.getBinId());
                            storedBinDetails.setEntryProductId(productResult.getId());
                            storedBinDetails.setAllottedQuantity(binDetails.getAllottedQuantity());
                            resultBinDetails = storedBinDetailsRepository.save(storedBinDetails);
                        } else {
                            storedBinDetails = new DpEntryStoredBinDetails(binDetails);
                            storedBinDetails.setEntryProductId(productResult.getId());
                            resultBinDetails = storedBinDetailsRepository.save(storedBinDetails);
                        }
                        // when manager approve the bill that time to save binstock
                        // binStockUtil.updateBinStock(result, productResult, resultBinDetails,
                        // loggedUser);
                    }
                }


                // when manager approve the bill that time to save binstock
                // binStockUtil.updateBinStockAuditLog(productResult, loggedUser);
            }

            result.setStock_status("Stock_Inwarded");
            directPurchaseRepository.save(result);
        }
        return new ResponseEntity<Status>(new Status("200", "directPurchase updated successfully!!!"), HttpStatus.OK);
    }

    public void deleteAllStoredProduct(DirectPurchase result) {

        List<DpEntryStoredProduct> storedProductList = storedProductRepository.findBy(result.getId());
        if (storedProductList != null && !storedProductList.isEmpty()) {
            storedProductRepository.deleteAll(storedProductList);
        }
    }

    public ResponseEntity<?> listAllDirectPurchase(String productCategory, String billno, Long orgId, Long branchId, Long deptId, Long vendorId, String vendorName, String dateForm, String dateTo, String status, Pageable pageable) throws UsernameNotFoundException {
        System.out.println("BillNo == " + billno);
        Date startDateLocalDate = null, endDateLocalDate = null;
        if (dateForm != null) {
            startDateLocalDate = dateUtil.getStringTODateWk(dateForm);
        }
        if (dateTo != null) {
            endDateLocalDate = dateUtil.getStringTODateWk(dateTo);
        }
        if (productCategory != null) {
            if (productCategory.equalsIgnoreCase("RawMaterial")) {
                deptId = null;
            }
        }

        Page<DirectPurchaseList> list = directPurchaseRepository.getListDirectPurchase(productCategory, billno, vendorId, vendorName, status, orgId, branchId, deptId, startDateLocalDate, endDateLocalDate, pageable);
        if (!list.toList().isEmpty()) {
            List<DirectPurchaseListDto> result = list.toList().stream().map(user -> new DirectPurchaseListDto(user)).collect(Collectors.toList());
            PageDataDto<DirectPurchaseListDto> pageData = new PageDataDto<>(result, list);
            return new ResponseEntity<PageDataDto<DirectPurchaseListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<DirectPurchaseListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getDirectPurchaseDetails(String id) throws UsernameNotFoundException {

        DirectPurchaseDetails optionalDirectPurchase = directPurchaseRepository.getDirectPurchaseDetailsById(Long.parseLong(id));

        DirectPurchaseDto directPurchaseDto = new DirectPurchaseDto(optionalDirectPurchase);

        Long billInwardId = directPurchaseDto.getBillRefId();

        List<EntryProductList> entryProductLists = storedProductRepository.findAllByEntryId(directPurchaseDto.getId());

        if (entryProductLists != null && !entryProductLists.isEmpty()) {
            List<DpEntryStoredProductDto> dtoList = entryProductLists.stream().map(data -> {
                DpEntryStoredProductDto dto = new DpEntryStoredProductDto(data);
                Double price = null;
                if (dto.getUnitRate() != null) {
                    String priceValue = decfor.format(Double.valueOf(dto.getUnitRate()));
                    price = Double.valueOf(priceValue);
                }
                Optional<PurchaseOrderItems> purchaseOrderItems = purchaseOrderItemsRepository.findBy(dto.getPoId(), dto.getItemId(), price);
                if (purchaseOrderItems.isPresent()) {
                    dto.setPoItemQty(purchaseOrderItems.get().getFINAL_ORDER_QTY());
                    dto.setPackingAndForwardingCharges(purchaseOrderItems.get().getPackingAndForwardingCharges());
                } else {
                    Optional<PurchaseOrderItems> purchaseOrderItems1 = purchaseOrderItemsRepository.findBy(dto.getPoId(), dto.getItemId());
                    if (purchaseOrderItems1.isPresent()) {
                        dto.setPoItemQty(purchaseOrderItems1.get().getFINAL_ORDER_QTY());
                        dto.setPackingAndForwardingCharges(purchaseOrderItems1.get().getPackingAndForwardingCharges());
                    }
                }
                double poqty = billInwardItemReceivedRepository.sumOfPoQty(dto.getPoId(), dto.getItemId());

                double finalQty = dto.getPoItemQty() - poqty;
                if (finalQty > 0) {
                    dto.setPendingQty(finalQty);
                } else {
                    dto.setPendingQty(0.0);
                }
                Optional<QualityInspectionScore> qualityInspection = qualityInspectionScoreRepo.findByFilter(dto.getItemId(), dto.getBillRefId(), dto.getPoId(), dto.getPoNumber());
                if (qualityInspection.isPresent()) {
                    dto.setQualityInspection(new QualityInspectionDTO(qualityInspection.get()));
                    dto.setScore(qualityInspection.get().getQcScore());
                }

                List<DpEntryStoredBinDetails> binDetails = storedBinDetailsRepository.findByProductId(dto.getId());
                if (binDetails != null && !binDetails.isEmpty()) {
                    dto.setBinDetails(binDetails != null ? binDetails.stream()
                            .map(field -> new DpEntryStoredBinDetailsDto(field)).collect(Collectors.toList()) : null);
                }

                return dto;
            }).collect(Collectors.toList());
            directPurchaseDto.setStoredProducts(dtoList);
        }
        Optional<BillForward> optionalBillForward = billForwardRepository.findByBillInwardId(billInwardId);
        if (optionalBillForward.isPresent()) {
            if (optionalBillForward.get().getStatus() != null && optionalBillForward.get().getStatus().equalsIgnoreCase("Pending") && optionalBillForward.get().getStatus().equalsIgnoreCase("Verify")) {
                directPurchaseDto.setBillVerified("Success");
            } else if (optionalBillForward.get().getStatus() != null && optionalBillForward.get().getStatus().equalsIgnoreCase("Pending")) {
                directPurchaseDto.setBillVerified("Success");
            }
        }
        return new ResponseEntity<DirectPurchaseDto>(directPurchaseDto, HttpStatus.OK);
    }

    public ResponseEntity<?> updateLorryWeight(String id, String lorryWeight) throws UsernameNotFoundException {
        Optional<DirectPurchase> optionalDirectPurchase = directPurchaseRepository.findById(Long.parseLong(id));
        if (optionalDirectPurchase.isPresent()) {

            Optional<BillInward> optional = billInwardRepository.findById(optionalDirectPurchase.get().getBillRefId());
            if (optional.isPresent()) {
                BillInward billInward = optional.get();
                billInward.setLorry_without_Weight(lorryWeight);
                billInwardRepository.save(billInward);
            }

            return new ResponseEntity<Status>(new Status("200", "Updated Successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> getDirectPurchaseListbyId(Long id, User loggedUser) throws UsernameNotFoundException {

        DirectPurchaseList optionalDirectPurchase = directPurchaseRepository.getDirectPurchaseListbyId(id);

        if (optionalDirectPurchase != null) {

            DirectPurchaseListDto directPurchaseDto = new DirectPurchaseListDto(optionalDirectPurchase);
            List<BillInwardLorryDetails> lorryDetails = billInwardLorryDetailsRepository.findByList(directPurchaseDto.getBillInwardId());
            directPurchaseDto.setLorryDetails(lorryDetails != null ? lorryDetails.stream().map(field -> new BillInwardLorryDto(field)).collect(Collectors.toList()) : null);
            if (!directPurchaseDto.getProductCategory().equalsIgnoreCase("RawMaterial")) {
                List<BillInwardItemReceived> list = billInwardItemReceivedRepository.listBillInwardProducts(directPurchaseDto.getBillInwardId());
                if (list != null && list.size() == 1) {
                    directPurchaseDto.setCgst(list.get(0).getCgst());
                    directPurchaseDto.setIgst(list.get(0).getIgst());
                    directPurchaseDto.setSgst(list.get(0).getSgst());
                }
            }
            return new ResponseEntity<DirectPurchaseListDto>(directPurchaseDto, HttpStatus.OK);

        } else {
            return new ResponseEntity<DirectPurchaseListDto>(new DirectPurchaseListDto(), HttpStatus.OK);

        }
    }

    public ResponseEntity<?> deleteDirectPurchase(String id) throws UsernameNotFoundException {
        try {
            Optional<DirectPurchase> directPurchaseOptional = directPurchaseRepository.findById(Long.parseLong(id));
            if (directPurchaseOptional.isPresent()) {
                DirectPurchase directPurchase = directPurchaseOptional.get();
                List<DpEntryStoredProduct> dpEntryStoredProducts = storedProductRepository.findBy(directPurchase.getId());
                if (!dpEntryStoredProducts.isEmpty()) {
                    for (DpEntryStoredProduct dpEntryStoredProduct : dpEntryStoredProducts) {
                        List<DpEntryStoredBinDetails> binDetails = storedBinDetailsRepository.findByProductId(dpEntryStoredProduct.getId());
                        if (!binDetails.isEmpty()) {
                            storedBinDetailsRepository.deleteAll(binDetails);
                        }
                        storedProductRepository.delete(dpEntryStoredProduct);
                    }
                }
                directPurchaseRepository.delete(directPurchase);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public void updateBillReceiveDto(BillReceiveDto receiveDto) {

        Optional<BillInward> result = billInwardRepository.findByBillNoAndDate(receiveDto.getBillNumber(), dateUtil.getStringTODateWk(receiveDto.getBillDate()));
        if (result.isPresent()) {
            receiveDto.setBillCopy(result.get().getBillCopy());
        }
    }

    public ResponseEntity<?> listAllBillReferenceNumberAndVendor(String referenceNumber, String vendorName, Long vendorId, Long deliveryBranchId) throws UsernameNotFoundException {

        List<BillInward> billInwardList = billInwardRepository.findBy(referenceNumber, vendorName, vendorId, deliveryBranchId);

        return new ResponseEntity<List<ProductDistruptancesBillInwardDto>>(billInwardList.stream().map(user -> {
            ProductDistruptancesBillInwardDto billInwardDto = new ProductDistruptancesBillInwardDto(user);
            billInwardDto.setReceivedDate(dateUtil.getDateTOStringWk(user.getReceivedDate()));
            billInwardDto.setBillDate(dateUtil.getDateTOStringWk(user.getBillDate()));
            billInwardDto.setLorryDate(dateUtil.getDateTOStringWk(user.getLorryDate()));
            List<DpEntryStoredProduct> products = storedProductRepository.findByBillRefId(billInwardDto.getId());
            if (!products.isEmpty()) {
                List<DpEntryStoredProductDto> productDtoList = new ArrayList<>();
                for (DpEntryStoredProduct storedProduct : products) {
                    DpEntryStoredProductDto dto = new DpEntryStoredProductDto(storedProduct);
                    List<DpEntryStoredBinDetailsDto> binDetailList = new ArrayList<>();
                    for (DpEntryStoredBinDetails bin : storedProduct.getBinDetails()) {
                        DpEntryStoredBinDetailsDto binDto = new DpEntryStoredBinDetailsDto(bin);
                        Object qty = binStockLogRepository.getConceptionStock(dto.getItemId(), dto.getBatchNo(), binDto.getBinId());
                        binDto.setConceptionStock(qty.toString());
                        binDetailList.add(binDto);
                    }
                    dto.setBinDetails(binDetailList);
                    productDtoList.add(dto);
                }
                billInwardDto.setStoredProductDtos(productDtoList);
            }
            return billInwardDto;
        }).collect(Collectors.toList()), HttpStatus.OK);

    }

    private HashMap<String, Date> dateDynamic(String dateFrom, String dateTo) {
        Date dateFromLocalDate = null, dateToLocalDate = null;
        if ((dateFrom != null && !dateFrom.isEmpty()) && (dateTo != null && !dateTo.isEmpty())) {
            dateFromLocalDate = dateUtil.getLocalDateTimeToDate(dateUtil.getDateToLocalDateTime(dateUtil.getStringTODateWk(dateFrom)));
            dateToLocalDate = dateUtil.getLocalDateTimeToDate(dateUtil.getDateToLocalDateTime(dateUtil.getStringTODateWk(dateTo)));
        } else if ((dateFrom == null || dateFrom.isEmpty()) && ((dateTo != null && !dateTo.isEmpty()))) {
            dateToLocalDate = dateUtil.getLocalDateTimeToDate(dateUtil.getDateToLocalDateTime(dateUtil.getStringTODateWk(dateTo)));
        } else if ((dateFrom != null && !dateFrom.isEmpty()) && (dateTo == null || dateTo.isEmpty())) {
            dateFromLocalDate = dateUtil.getLocalDateTimeToDate(dateUtil.getDateToLocalDateTime(dateUtil.getStringTODateWk(dateFrom)));
        }
        HashMap<String, Date> dateHashMap = new HashMap<>();
        dateHashMap.put("dateFrom", dateFromLocalDate);
        dateHashMap.put("dateTo", dateToLocalDate);
        return dateHashMap;
    }

    public ResponseEntity<?> listAllBillReferenceNumber(String referenceNumber, String batchNo) throws UsernameNotFoundException {
        List<BillInwardDto> billInwardDtoList = null;
        if (referenceNumber != null && batchNo != null) {
            List<BillInward> billInwardList = billInwardRepository.findByNumber("%" + referenceNumber + "%");
            billInwardDtoList = billInwardList.stream().map(user -> {
                BillInwardDto billInward = new BillInwardDto(user);
                List<BillInwardItemReceived> billInwardProducts = billInwardItemReceivedRepository.listBillInwardProductsByBatchNo(batchNo, user.getId());
                if (!billInwardProducts.isEmpty()) {
                    billInward.setItemReceives(billInwardProducts.stream().map(product -> {
                        BillInwardItemReceivedDto billInwardItemReceivedDto = new BillInwardItemReceivedDto(product);
                        return billInwardItemReceivedDto;
                    }).collect(Collectors.toList()));
                } else {
                    billInward.setItemReceives(new ArrayList<>());
                }
                return billInward;
            }).collect(Collectors.toList());
        }
        return new ResponseEntity<List<BillInwardDto>>(billInwardDtoList, HttpStatus.OK);
    }

    public ResponseEntity<?> filterBillInwardLorryArrivalList(String billNo, Long vendorId, Long inwardBranchId, Long employeeId, Long inwardId, String startDate, String endDate, Pageable pageable) throws UsernameNotFoundException {
        Date startDateLocalDate = null, endDateLocalDate = null;
        if (startDate != null && endDate != null) {
            startDateLocalDate = dateUtil.getStringTODateDDMMYYYY(startDate);
            endDateLocalDate = dateUtil.getStringTODateDDMMYYYY(endDate);
        } else {
            startDateLocalDate = dateUtil.getStartDate();
            endDateLocalDate = dateUtil.getEndDate();
        }
        // List<LorryArrival> lorryArrivals =
        // billInwardRepository.filterBillInwardLorryArrivalList(billNo, vendorId,
        // inwardBranchId, employeeId, inwardId, /* startDateLocalDate,
        // endDateLocalDate, */pageable);
        List<LorryArrival> lorryArrivals = new ArrayList<>();
        if (!lorryArrivals.isEmpty()) {
            List<LorryArrivalDto> lorryArrivalDtos = lorryArrivals.stream().map(user -> {
                LorryArrivalDto userDto = new LorryArrivalDto(user);
                return userDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<LorryArrivalDto>>(lorryArrivalDtos, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<LorryArrivalDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> filterInwardInspectionBatchList(Long productId, String date, Pageable pageable) throws UsernameNotFoundException {

        Date convDate = null;

        if (date != null) {
            convDate = dateUtil.getStringTODateWk(date);
        }

        List<InwardInspectionBatchList> inwardInspectionBatchLists = billInwardRepository.filterInwardInspectionBatchLists(productId, convDate, pageable);
        if (!inwardInspectionBatchLists.isEmpty()) {
            List<InwardInspectionBatchListDto> inwardInspectionBatchListDtos = inwardInspectionBatchLists.stream().map(user -> {
                InwardInspectionBatchListDto userDto = new InwardInspectionBatchListDto(user);
                return userDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<InwardInspectionBatchListDto>>(inwardInspectionBatchListDtos, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<InwardInspectionBatchListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> approvebill(Long stockId, Long billInwardId, String eTransfer, String creditOrDebit, User loggedUser) {

        logger.info("approve bill : " + stockId + " billInwardId : " + billInwardId + " eTransfer : " + eTransfer + " creditOrDebit :");

        Optional<BillForward> billForward = billForwardRepository.findByBillInwardId(billInwardId);
        if (billForward.isPresent()) {
            return new ResponseEntity<Status>(new Status("200", "bill approval already exists this bill!!"), HttpStatus.OK);
        } else {
            logger.info("approve bill : " + stockId + " billInwardId : " + billInwardId + " eTransfer : " + eTransfer + " creditOrDebit :");
            Optional<BillInward> billinward = billInwardRepository.findById(billInwardId);
            List<BillInwardItemReceived> billInwardProducts = billInwardItemReceivedRepository.listBillInwardProducts(billinward.get().getId());
            List<BillInwardItemReceivedDto> billInwardItemReceivedDtos = billInwardProducts.stream().map((val) -> new BillInwardItemReceivedDto(val)).collect(Collectors.toList());

            if (billinward.get().getProductCategory().equalsIgnoreCase("RawMaterial")) {

                if (billinward.get().getLorry_without_Weight() != null && billinward.get().getLorry_without_Weight() != "") {
                    if (stockId != null) {
                        Optional<DirectPurchase> optional = directPurchaseRepository.findById(stockId);
                        if (optional.isPresent()) {
                            DirectPurchase dp = optional.get();
                            logger.info("approve bill : " + stockId + " billInwardId : " + billInwardId + " eTransfer : " + eTransfer + " creditOrDebit :");
                            String stockStatus = dp.getStock_status();
                            if (!"Stock_Inwarded".equalsIgnoreCase(dp.getStock_status())) {
                                return new ResponseEntity<>(
                                        new Status("400", "Bill already approved or wrong type."),
                                        HttpStatus.BAD_REQUEST
                                );
                            }
                            try {
                                logger.info("approve bill : " + stockId + " billInwardId : " + billInwardId + " eTransfer : " + eTransfer + " creditOrDebit :");
                                dp.setApproved_by(loggedUser.getId());
                                dp.setApproved__date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                                dp.setStock_status("Completed");
                                dp = directPurchaseRepository.save(dp);
                                saveOrUpdateBinStock(dp, loggedUser);
                                billUtil.updateBillApproveToForward(billinward.get(), billInwardItemReceivedDtos, loggedUser);
                                logger.info("approve bill : " + stockId + " billInwardId : " + billInwardId + " eTransfer : " + eTransfer + " creditOrDebit :");
                            } catch (Exception e) {
                                e.printStackTrace();
                                logger.info("error in stock save!!", e);
                                dp.setStock_status(stockStatus);
                                dp = directPurchaseRepository.save(dp);
                                deleteBinStock(dp, loggedUser);
                                return new ResponseEntity<Status>(new Status("400", "error in stock save!!"), HttpStatus.INTERNAL_SERVER_ERROR);
                            }

                        }
                    }
                } else {
                    return new ResponseEntity<Status>(new Status("200", "please update lorry Without Weight!!"), HttpStatus.OK);
                }
            } else {
                if (stockId != null) {
                    Optional<DirectPurchase> optional = directPurchaseRepository.findById(stockId);
                    if (optional.isPresent()) {
                        DirectPurchase dp = optional.get();
                        if(dp.getStock_status().equalsIgnoreCase("Quality Checked")) {
                        dp.setVerified_by(loggedUser.getId());
                        dp.setVerified_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                        dp.setStock_status("Verified");
                        dp.setCredit_or_debit(creditOrDebit);
                        dp.setETransfer(eTransfer);
                        dp = directPurchaseRepository.save(dp);
                        updateAccountLedger(dp, loggedUser);
                        if (eTransfer != null) {
                            updateETransfer(dp, loggedUser);
                        }
                        }else {
                        	return new ResponseEntity<>(new Status("400", "bill  approval is already Verified!"), HttpStatus.BAD_REQUEST);
                        }
                    }
                }
            }
            return new ResponseEntity<Status>(new Status("200", "bill approval SuccessFully!!"), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> billReject(Long stockId, Long billInwardId, User loggedUser) {

        Optional<BillForward> billForward = billForwardRepository.findByBillInwardId(billInwardId);
        if (billForward.isPresent()) {
            return new ResponseEntity<Status>(new Status("202", "bill approval already exists Not Rejected for this bill !!"), HttpStatus.OK);
        } else {
            Optional<BillInward> billinward = billInwardRepository.findById(billInwardId);
            BillInward billInward = billinward.get();
            if (billInward.getProductCategory().equalsIgnoreCase("RawMaterial")) {
                if (stockId != null) {
                    Optional<DirectPurchase> directPurchaseOptional = directPurchaseRepository.findById(stockId);
                    if (directPurchaseOptional.isPresent()) {
                        DirectPurchase directPurchase = directPurchaseOptional.get();
                        List<DpEntryStoredProduct> dpEntryStoredProducts = storedProductRepository.findBy(directPurchase.getId());
                        if (!dpEntryStoredProducts.isEmpty()) {
                            for (DpEntryStoredProduct dpEntryStoredProduct : dpEntryStoredProducts) {
                                List<DpEntryStoredBinDetails> binDetails = storedBinDetailsRepository.findByProductId(dpEntryStoredProduct.getId());
                                if (!binDetails.isEmpty()) {
                                    storedBinDetailsRepository.deleteAll(binDetails);
                                }
                                storedProductRepository.delete(dpEntryStoredProduct);
                            }
                        }
                        directPurchaseRepository.delete(directPurchase);
                    }
                }
                createBillInWard(billInward);
                return new ResponseEntity<Status>(new Status("200", "Ho bill Rejected SuccessFully!!"), HttpStatus.OK);
            } else {
                Optional<DirectPurchase> directPurchaseOptional = directPurchaseRepository.findById(stockId);
                if (directPurchaseOptional.isPresent()) {

                    DirectPurchase directPurchase = directPurchaseOptional.get();
                    directPurchase.setStock_status("Pending");
                    directPurchase.setUpdatedBy(null);
                    directPurchase.setVerified_by(null);
                    directPurchase.setApproved_by(null);
                    directPurchase.setAuthorised_by(null);
                    directPurchase.setRejected_by(loggedUser.getId());
                    directPurchase.setRejected_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    DirectPurchase rs = directPurchaseRepository.save(directPurchase);
                    rejectInspection(rs);
                    deleteAccountLedger(rs);
                }

                return new ResponseEntity<Status>(new Status("200", "ft bill Rejected SuccessFully!!"), HttpStatus.OK);
            }
        }
    }

    public ResponseEntity<?> billApproveOrAuthorise(String type, Long stockId, Long billInwardId, User loggedUser) {

        Optional<BillForward> billForward = billForwardRepository.findByBillInwardId(billInwardId);
        if (billForward.isPresent()) {
            return new ResponseEntity<Status>(new Status("202", "bill approval already exists Not Rejected for this bill !!"), HttpStatus.OK);
        } else {
            Optional<BillInward> billinward = billInwardRepository.findById(billInwardId);
            BillInward billInward = billinward.get();

            List<BillInwardItemReceived> billInwardProducts = billInwardItemReceivedRepository.listBillInwardProducts(billinward.get().getId());

            List<BillInwardItemReceivedDto> billInwardItemReceivedDtos = billInwardProducts.stream().map((val) -> new BillInwardItemReceivedDto(val)).collect(Collectors.toList());

            if (!billInward.getProductCategory().equalsIgnoreCase("RawMaterial")) {
                if (type.equalsIgnoreCase("Approve")) {
                    Optional<DirectPurchase> directPurchaseOptional = directPurchaseRepository.findById(stockId);
                    if (directPurchaseOptional.isPresent()) {
                        DirectPurchase directPurchase = directPurchaseOptional.get();
                        if(directPurchase.getStock_status().equalsIgnoreCase("Verified")) {
                        directPurchase.setStock_status("Approved");
                        directPurchase.setApproved_by(loggedUser.getId());
                        directPurchase.setApproved__date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                        DirectPurchase dp = directPurchaseRepository.save(directPurchase);
                        }
                        else {
                        	return new ResponseEntity<Status>(new Status("200", "Ft Bill Alreay Approved!!"), HttpStatus.OK);
                        }
                        return new ResponseEntity<Status>(new Status("200", "Ft Bill Approved SuccessFully!!"), HttpStatus.OK);
                    }

                } else if (type.equalsIgnoreCase("Authorise")) {

                    Optional<DirectPurchase> directPurchaseOptional = directPurchaseRepository.findById(stockId);

                    if (directPurchaseOptional.isPresent()) {
                        DirectPurchase directPurchase = directPurchaseOptional.get();
                        String stockStatus = directPurchase.getStock_status();
                       
                        try {
                            directPurchase.setStock_status("Authorised");
                            directPurchase.setAuthorised_by(loggedUser.getId());
                            directPurchase.setAuthorised_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                            DirectPurchase dp = directPurchaseRepository.save(directPurchase);
                            saveOrUpdateBinStock(dp, loggedUser);
                            billUtil.updateBillApproveToForward(billinward.get(), billInwardItemReceivedDtos, loggedUser);
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.info("error in stock save!!", e);
                            directPurchase.setStock_status(stockStatus);
                            directPurchase.setAuthorised_by(null);
                            directPurchase.setAuthorised_date(null);
                            directPurchase = directPurchaseRepository.save(directPurchase);
                            deleteBinStock(directPurchase, loggedUser);
                            return new ResponseEntity<Status>(new Status("400", "error in stock save!!"), HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                    }

                    return new ResponseEntity<Status>(new Status("200", "Ft Bill Authorise SuccessFully!!"), HttpStatus.OK);
                }

            } else {
                return new ResponseEntity<Status>(new Status("204", "This is RawMaterial bill Could not Approve OR Authorise !!"), HttpStatus.OK);
            }

        }

        return new ResponseEntity<Status>(new Status("204", "ft bill Rejected SuccessFully!!"), HttpStatus.NOT_MODIFIED);
    }

    public void saveOrUpdateBinStock(DirectPurchase result, User loggedUser) {

        List<DpEntryStoredProduct> storedProductList = storedProductRepository.findBy(result.getId());
        if (storedProductList != null && !storedProductList.isEmpty()) {
            for (DpEntryStoredProduct productResult : storedProductList) {
                if (productResult.getItemType() != null && (productResult.getItemType() == 30L || productResult.getItemType() == 31L)) {
                    logger.info("saveOrUpdateBinStock Skipped for item type : " + productResult.getItemType());
                    continue;
                } else {
                    logger.info("saveOrUpdateBinStock productResult id : " + productResult.getId());
                    if (productResult.getBinDetails() != null && !productResult.getBinDetails().isEmpty()) {
                        for (DpEntryStoredBinDetails resultBinDetails : productResult.getBinDetails()) {
                            binStockUtil.updateBinStock(result, productResult, resultBinDetails, loggedUser);
                        }
                    }
                    // binStockUtil.updateBinStockAuditLog(result, productResult, loggedUser);
                }
            }
        }
    }


    public void deleteBinStock(DirectPurchase result, User loggedUser) {

        List<DpEntryStoredProduct> storedProductList = storedProductRepository.findBy(result.getId());
        if (storedProductList != null && !storedProductList.isEmpty()) {
            for (DpEntryStoredProduct productResult : storedProductList) {
                if (productResult.getBinDetails() != null && !productResult.getBinDetails().isEmpty()) {
                    for (DpEntryStoredBinDetails resultBinDetails : productResult.getBinDetails()) {
                        binStockUtil.deleteBinStock(result, productResult, resultBinDetails, loggedUser);
                    }
                }
            }
        }
    }

    public ResponseEntity<?> billVerify(Long stockId, Long billInwardId, User loggedUser) {

        Optional<BillForward> billForward = billForwardRepository.findByBillInwardId(billInwardId);
        if (billForward.isPresent()) {
            BillForward billForward1 = billForward.get();
            billForward1.setBill_verified_by(loggedUser.getId());
            billForward1.setBill_verified_date(dateUtil.getISTDate());
            billForward1.setStatus("Verify");
            billForwardRepository.save(billForward1);
            if (stockId != null) {
                Optional<DirectPurchase> optional = directPurchaseRepository.findById(stockId);
                if (optional.isPresent()) {
                    DirectPurchase dp = optional.get();
                    dp.setStock_status("Completed");
                    directPurchaseRepository.save(dp);
                }
            }
            return new ResponseEntity<Status>(new Status("200", "bill verifyed successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "please bill approve!!"), HttpStatus.OK);
        }
    }

    public void updateRestService(Long forwardId, User loggedUser) {

        Optional<BillForward> billForward = billForwardRepository.findById(forwardId);
        if (billForward.isPresent()) {
            Optional<BillInward> billInwardOptional = billInwardRepository.findById(billForward.get().getBill_ref_id());
            if (billInwardOptional.isPresent()) {

                HoForwardDto hoForwardDto = new HoForwardDto(billInwardOptional.get());
                String billAmt = decfor.format(billInwardOptional.get().getBillAmount());
                System.out.println("billAmout  == " + billAmt);
                hoForwardDto.setBillAmt(billAmt);
                Optional<User> user = null;
                if (billInwardOptional.get().getUpdatedBy() != null) {
                    user = userRepository.findById(billInwardOptional.get().getUpdatedBy());
                    hoForwardDto.setBillInwardBy(user.get().getEmployeename());
                }
                hoForwardDto.setFrmId(loggedUser.getOrg_id());
                hoForwardDto.setPoRecItem(billInwardOptional.get().getItemReceived() != null ? billInwardOptional.get().getItemReceived().stream().map(field -> {
                    HoBillReceiveItem hoBillReceiveItem = new HoBillReceiveItem(field);
                    Optional<PurchaseOrder> purchaseOrder = purchaseOrderRepository.findById(hoBillReceiveItem.getHoPoId());
                    if (purchaseOrder.isPresent()) {
                        hoBillReceiveItem.setHoPoId(purchaseOrder.get().getHo_po_id());
                    }
                    return hoBillReceiveItem;
                }).collect(Collectors.toList()) : null);

                Optional<ProductExcessStorage> productExcessStorage = excessStorageRepository.findBy(billInwardOptional.get().getVendor().getId(), billInwardOptional.get().getBillNo(), billInwardOptional.get().getBillDate(), billInwardOptional.get().getReceivedDate());
                if (productExcessStorage.isPresent()) {
                    double qty = excessShortageProductsRepository.findByBagQty(productExcessStorage.get().getId());
                    hoForwardDto.setWeightShortage(qty);
                    hoForwardDto.setShexType(productExcessStorage.get().getExcessShortageProducts().get(0).getType());
                }

                RestResponse response = restService.saveHoForward(hoForwardDto);

                String responseData = getHoData(response);
                System.out.println("responseData = " + responseData);

                updateLog("BillTransfer", responseData, hoForwardDto);

                if (response.getGenericAlertResponse() != null) {
                    RestResponseDto restResponse = response.getGenericAlertResponse();
                    if (restResponse.getErrorCode() != null && restResponse.getErrorCode().equalsIgnoreCase("0")) {
                        if (restResponse.getErrorMessage() != null && !restResponse.getErrorMessage().equalsIgnoreCase("Success")) {
                            BillForward billForward1 = billForward.get();
                            billForward1.setStatus("Pending");
                            billForwardRepository.save(billForward1);
                        }
                    } else {
                        BillForward billForward1 = billForward.get();
                        billForward1.setStatus("Pending");
                        billForwardRepository.save(billForward1);
                    }
                } else {
                    BillForward billForward1 = billForward.get();
                    billForward1.setStatus("Pending");
                    billForwardRepository.save(billForward1);
                }
            }
        }
    }

    public void updateLog(String type, String response, HoForwardDto jsonData) {
        String status = response;
        Long typeId = jsonData.getFactBillId();
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

    public String getHoData(HoForwardDto hoForwardDto) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> map = objectMapper.convertValue(hoForwardDto, new TypeReference<Map<String, Object>>() {
            });
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getHoData(RestResponse hoForwardDto) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> map = objectMapper.convertValue(hoForwardDto, new TypeReference<Map<String, Object>>() {
            });
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResponseEntity<?> findByJaggeryBillList(Long vendorId, String billDate) throws UsernameNotFoundException {

        Date date = null;
        if (billDate != null && billDate != null) {
            date = dateUtil.getStringTODateWk(billDate);
        }

        List<BillInward> billInwardList = billInwardRepository.findBy(vendorId, date);
        if (!billInwardList.isEmpty()) {
            List<BillInwardDto> result = billInwardList.stream().map(user -> {
                BillInwardDto billInwardDto = new BillInwardDto(user);
                if (user.getReceivedDate() != null) {
                    billInwardDto.setReceivedDate(dateUtil.getDateTOStringWk(user.getReceivedDate()));
                }
                billInwardDto.setBillDate(dateUtil.getDateTOStringWk(user.getBillDate()));
                if (user.getReceivedDate() != null) {
                    billInwardDto.setLorryDate(dateUtil.getDateTOStringWk(user.getLorryDate()));
                }
                if (user.getBillAmount() != null) {
                    billInwardDto.setBillAmountStringValue(new BigDecimal(user.getBillAmount()).toPlainString());
                }
                return billInwardDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<BillInwardDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BillInwardDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public void createBillInWard(BillInward removeBillInward) {

        Optional<HOConfigRESTLog> configRESTLogOptional = restLogRepository.findBy(removeBillInward.getHoBillId());
        if (configRESTLogOptional.isPresent()) {
            HOConfigRESTLog hoConfigRESTLog = configRESTLogOptional.get();
            ObjectMapper objectMapper = new ObjectMapper();

            try {

                GRNDto billInwardDto = objectMapper.readValue(hoConfigRESTLog.getJson_data(), GRNDto.class);

                BillInward billInward = new BillInward(billInwardDto);

                billInward.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                String billAmt = decfor.format(billInwardDto.getBillAmount());
                System.out.println("billAmout  == " + billAmt);

                billInward.setBillAmount(Double.valueOf(billAmt));

                billInward.setStatus("Pending");

                billInward.setHoBillId(billInwardDto.getHoBillId());

                billInward.setRaw_ho_grn(getHoData(billInwardDto));

                BillInward result = billInwardRepository.save(billInward);

                System.out.println("billAmount  == " + result.getBillAmount());

                if (billInwardDto.getItemReceives() != null && !billInwardDto.getItemReceives().isEmpty()) {

                    for (GRNProducts billInwardProductDto : billInwardDto.getItemReceives()) {

                        BillInwardItemReceived billInwardProduct = new BillInwardItemReceived(billInwardProductDto);

                        Double lot = Double.valueOf(billInwardProduct.getLot());

                        Double packageQty = Double.valueOf(billInwardProduct.getPackageQuantity());

                        Double covQty = packageQty / lot;
                        System.out.println(covQty);
                        billInwardProduct.setConv_package_quantity(covQty);

                        String convertValue = decfor.format(covQty);

                        List<String> values = new ArrayList<>(Arrays.asList(convertValue.split("\\.")));
                        String bagValue = values.get(0);

                        Double kgValues = Double.valueOf("0." + values.get(1));
                        Double covKGQty = lot * kgValues;
                        String kgValue = decfor1.format(covKGQty);
                        int kg = Integer.parseInt(kgValue);
                        String finalCovQty = null;
                        if (9 >= kg) {
                            finalCovQty = bagValue + ".0" + kgValue;
                        } else {
                            finalCovQty = bagValue + "." + kgValue;
                        }

                        billInwardProduct.setConv_package_quantity_value(finalCovQty);

                        Optional<PurchaseOrder> purchaseOrder = purchaseOrderRepository.getPurchaseOrderByHoPOId(billInwardProductDto.getPoId());
                        if (purchaseOrder.isPresent()) {
                            billInwardProduct.setPo_id(purchaseOrder.get().getId());
                            billInwardProduct.setPo_number(purchaseOrder.get().getPoNumber());
                            billInwardProduct.setHoItemRecId(billInwardProductDto.getHoItemRecId());
                            billInwardProduct.setItem_type(Long.valueOf(28));
                        }

                        billInwardProduct.setInwardId(result.getId());
                        billInwardItemReceivedRepository.save(billInwardProduct);

                    }
                }

                if (billInwardDto.getLorryDetails() != null && !billInwardDto.getLorryDetails().isEmpty()) {
                    for (BillInwardLorryDto billDetailsDto : billInwardDto.getLorryDetails()) {
                        BillInwardLorryDetails billInwardBillDetails = new BillInwardLorryDetails(billDetailsDto);
                        billInwardBillDetails.setInwardId(result.getId());
                        billInwardLorryDetailsRepository.save(billInwardBillDetails);
                    }
                } else {
                    if (billInwardDto.getPayableFreightAmount() != null) {
                        BillInwardLorryDetails billInwardBillDetails = new BillInwardLorryDetails();
                        billInwardBillDetails.setInwardId(result.getId());
                        billInwardBillDetails.setPayableFreightAmount(billInwardDto.getPayableFreightAmount());
                        billInwardLorryDetailsRepository.save(billInwardBillDetails);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            List<BillInwardItemReceived> billInwardItemReceivedList = billInwardItemReceivedRepository.findBy(removeBillInward.getId());
            if (!billInwardItemReceivedList.isEmpty()) {
                billInwardItemReceivedRepository.deleteAll(billInwardItemReceivedList);
            }
            List<BillInwardLorryDetails> billInwardLorryDetailsList = billInwardLorryDetailsRepository.findByList(removeBillInward.getId());
            if (!billInwardLorryDetailsList.isEmpty()) {
                billInwardLorryDetailsRepository.deleteAll(billInwardLorryDetailsList);
            }

            billInwardRepository.delete(removeBillInward);
        }
    }

    public String getHoData(GRNDto grnDto) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> map = objectMapper.convertValue(grnDto, new TypeReference<Map<String, Object>>() {
            });
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public RestResponseDto getResponseData(String response) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> map = objectMapper.convertValue(response, new TypeReference<Map<String, Object>>() {
            });
            RestResponseDto restResponse = objectMapper.readValue((JsonParser) map.get("GenericAlertResponse"), RestResponseDto.class);
            return restResponse;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResponseEntity<?> getResponseData1(Long forwardId, User loggedUser) {
        Optional<BillForward> billForward = billForwardRepository.findById(forwardId);
        if (billForward.isPresent()) {
            Optional<BillInward> billInwardOptional = billInwardRepository.findById(billForward.get().getBill_ref_id());
            if (billInwardOptional.isPresent()) {

                HoForwardDto hoForwardDto = new HoForwardDto(billInwardOptional.get());
                String billAmt = decfor.format(billInwardOptional.get().getBillAmount());
                System.out.println("billAmout  == " + billAmt);
                hoForwardDto.setBillAmt(billAmt);
                Optional<User> user = null;
                if (billInwardOptional.get().getUpdatedBy() != null) {
                    user = userRepository.findById(billInwardOptional.get().getUpdatedBy());
                    hoForwardDto.setBillInwardBy(user.get().getEmployeename());
                }
                hoForwardDto.setFrmId(loggedUser.getOrg_id());
                hoForwardDto.setPoRecItem(billInwardOptional.get().getItemReceived() != null ? billInwardOptional.get().getItemReceived().stream().map(field -> {
                    HoBillReceiveItem hoBillReceiveItem = new HoBillReceiveItem(field);
                    Optional<PurchaseOrder> purchaseOrder = purchaseOrderRepository.findById(hoBillReceiveItem.getHoPoId());
                    if (purchaseOrder.isPresent()) {
                        hoBillReceiveItem.setHoPoId(purchaseOrder.get().getHo_po_id());
                    }
                    return hoBillReceiveItem;
                }).collect(Collectors.toList()) : null);

                RestResponse response = restService.saveHoForward(hoForwardDto);
                if (response.getGenericAlertResponse() != null) {
                    RestResponseDto restResponse = response.getGenericAlertResponse();
                    if (restResponse.getErrorCode() != null && restResponse.getErrorCode().equalsIgnoreCase("0")) {
                        if (restResponse.getErrorMessage() != null && restResponse.getErrorMessage().equalsIgnoreCase("Success")) {
                            System.out.println("res" + restResponse.getErrorMessage());
                        } else {
                            System.out.println(restResponse.getErrorMessage());
                        }
                        System.out.println("res" + restResponse.getErrorMessage());
                    } else {
                        System.out.println("result" + restResponse.getErrorCode());
                    }
                }
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);

    }

    public ResponseEntity<?> checkLorrySerialNo(String itemType, String lorryNo, String inwardDate, String lorrySerialNo) throws UsernameNotFoundException {

        List<Long> itemIds = new ArrayList<>();
        if (itemType != null) {

            if (itemType.equalsIgnoreCase("S") || itemType.contains("S")) {
                itemIds = productMasterRepository.findBYSesameProducts();
            } else if (itemType.equalsIgnoreCase("G") || itemType.contains("G")) {
                itemIds = productMasterRepository.findBYGroundnutProducts();
            } else if (itemType.equalsIgnoreCase("J") || itemType.contains("J")) {
                itemIds = productMasterRepository.findBYJaggerProducts();
            }

            System.out.println("itemIds" + itemIds);
        }

        Date conInwardDate = null;
        if (inwardDate == null) {
            conInwardDate = dateUtil.getDateValue(dateUtil.getISTDate());
            System.out.println("dateUtil.getISTDate() " + dateUtil.getISTDate());
        } else {
            conInwardDate = dateUtil.getStringTODateWk(inwardDate);
            System.out.println("conInwardDate " + conInwardDate);
        }

        List<BillInward> billInwardList = billInwardRepository.checkByLorrySerialNo(conInwardDate, itemIds, lorrySerialNo, lorryNo);
        if (billInwardList != null && !billInwardList.isEmpty()) {
            List<LorrySerialNoDto> billInwardDto = billInwardList.stream().map(data -> new LorrySerialNoDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<LorrySerialNoDto>>(billInwardDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<LorrySerialNoDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> amendmentBill(Long id, String inwardDate, String lorrySerialNo) throws UsernameNotFoundException {

        Date conInwardDate = dateUtil.getStringTODateWk(inwardDate);

        Optional<BillInward> optionalBillInward = billInwardRepository.findById(id);
        Long batchId = 0L;
        String oldBatch = null;
        String newBatch = null;
        if (optionalBillInward.isPresent()) {

            BillInward billInward = optionalBillInward.get();
            List<BillInwardItemReceived> itemReceives = billInwardItemReceivedRepository.findBy(billInward.getId());

            Optional<BatchMaster> optionalBatchMaster = batchMasterRepository.findByBatch(itemReceives.get(0).getBatchId(), itemReceives.get(0).getBatchNo());
            if (optionalBatchMaster.isPresent()) {

                BatchMaster batchMaster = optionalBatchMaster.get();
                oldBatch = batchMaster.getBatchno();
                List<String> batchSplits = new ArrayList<>(Arrays.asList(oldBatch.split("#")));
                String Date = batchSplits.get(0);
                if (lorrySerialNo != null) {
                    newBatch = inwardDate + "-" + lorrySerialNo + "#" + batchSplits.get(1) + "#" + batchSplits.get(2) + "#" + batchSplits.get(3);
                } else {
                    if (!billInward.getProductCategory().equalsIgnoreCase("RawMaterial")) {
                        newBatch = inwardDate + "#" + batchSplits.get(1) + "#" + batchSplits.get(2) + "#" + batchSplits.get(3);
                    } else {
                        newBatch = inwardDate + "-" + billInward.getLorrySerialNo() + "#" + batchSplits.get(1) + "#" + batchSplits.get(2) + "#" + batchSplits.get(3);
                    }
                }
                batchMaster.setBatchno(newBatch);

                BatchMaster batchMa = batchMasterRepository.save(batchMaster);
                batchId = batchMa.getId();
                for (BillInwardItemReceived itemReceive : itemReceives) {
                    itemReceive.setBatchId(batchMa.getId());
                    itemReceive.setBatchNo(batchMa.getBatchno());
                    billInwardItemReceivedRepository.save(itemReceive);
                }
            }

            if (lorrySerialNo != null) {
                billInward.setLorrySerialNo(lorrySerialNo);
            }
            if (inwardDate != null) {
                billInward.setReceivedDate(conInwardDate);
            }
            BillInward rs = billInwardRepository.save(billInward);

            Optional<DirectPurchase> optionalDp = directPurchaseRepository.findBy(rs.getId());

            if (optionalDp.isPresent()) {
                DirectPurchase dp = optionalDp.get();
                dp.setInward_date(rs.getReceivedDate());
                directPurchaseRepository.save(dp);

                List<DpEntryStoredProduct> storedProductList = storedProductRepository.findBy(dp.getId());

                for (DpEntryStoredProduct storedProduct : storedProductList) {
                    storedProduct.setBatchId(batchId);
                    storedProduct.setBatchNo(newBatch);
                    storedProductRepository.save(storedProduct);
                }
            }

            Optional<BillForward> optionalBillForward = billForwardRepository.findByBillInwardId(rs.getId());

            if (optionalBillForward.isPresent()) {
                BillForward dp = optionalBillForward.get();
                dp.setBillInwardDate(rs.getReceivedDate());
                billForwardRepository.save(dp);

                List<BillForwardProduct> forwardProducts = billForwardProductRepository.findBy(dp.getId());

                for (BillForwardProduct storedProduct : forwardProducts) {
                    storedProduct.setBatchId(batchId);
                    storedProduct.setBatchNo(newBatch);
                    billForwardProductRepository.save(storedProduct);
                }
            }

            Optional<BillExchange> optionalBillExchange = billExchangeRepository.findBy(rs.getId());

            if (optionalBillExchange.isPresent()) {
                BillExchange dp = optionalBillExchange.get();
                dp.setBillInwardDate(rs.getReceivedDate());
                billExchangeRepository.save(dp);

                List<BillExchangeProduct> forwardProducts = billExchangeProductRepository.findBy(dp.getId());

                for (BillExchangeProduct storedProduct : forwardProducts) {
                    storedProduct.setBatchId(batchId);
                    storedProduct.setBatchNo(newBatch);
                    billExchangeProductRepository.save(storedProduct);
                }
            }

            return new ResponseEntity<Status>(new Status("200", "Bill Amendment successfully!!!"), HttpStatus.OK);
        }
        return new ResponseEntity<Status>(new Status("400", "Bill Amendment Not Successfully!!!"), HttpStatus.OK);

    }

    public ResponseEntity<?> updateManualBillInward(BillInwardDto billInwardDto, User loggedUser) {

        BillInward billInward = new BillInward(billInwardDto);
        billInward.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        billInward.setUpdatedBy(loggedUser.getId());
        billInward.setCreatedBy(loggedUser.getId());
        billInward.setReceivedDate(dateUtil.getStringTODateWk(billInwardDto.getReceivedDate()));
        billInward.setBillDate(dateUtil.getStringTODateWk(billInwardDto.getBillDate()));
        billInward.setLorryDate(dateUtil.getStringTODateWk(billInwardDto.getLorryDate()));

        String billAmt = decfor.format(billInwardDto.getBillAmount());
        System.out.println("billAmout  == " + billAmt);
        billInward.setBillAmount(Double.valueOf(billAmt));
        BillInward result = billInwardRepository.save(billInward);

        List<BillInwardItemReceivedDto> billInwardItemReceivedDtos = new ArrayList<BillInwardItemReceivedDto>();
        if (!billInwardDto.getItemReceives().isEmpty()) {
            billInwardItemReceivedDtos = billInwardDto.getItemReceives();
            for (BillInwardItemReceivedDto billInwardProductDto : billInwardDto.getItemReceives()) {
                Date currentDate = dateUtil.getISTDate();
                String inwardDate = dateUtil.getDateTOStringWk(currentDate);

                BatchMaster batchMaster = checkManualBatch(billInwardDto, billInwardProductDto, billInwardDto.getBillNo(), billInwardDto.getVendorId(), inwardDate, loggedUser);
                if (batchMaster != null) {
                    billInwardProductDto.setBatchNo(batchMaster.getBatchno());
                    billInwardProductDto.setBatchId(batchMaster.getId());
                }

                BillInwardItemReceived billInwardProduct = new BillInwardItemReceived(billInwardProductDto);
                billInwardProduct.setInwardId(result.getId());
                billInwardItemReceivedRepository.save(billInwardProduct);
            }
        }

        if (billInwardDto.getLorryDetails() != null && !billInwardDto.getLorryDetails().isEmpty()) {
            for (BillInwardLorryDto billDetailsDto : billInwardDto.getLorryDetails()) {
                BillInwardLorryDetails billInwardBillDetails = null;
                Optional<BillInwardLorryDetails> billInwardLorryDetails = billInwardLorryDetailsRepository.findBy(result.getId());
                if (billInwardLorryDetails.isPresent()) {
                    billInwardBillDetails = new BillInwardLorryDetails(billDetailsDto);
                    billInwardBillDetails.setId(billInwardLorryDetails.get().getId());
                } else {
                    billInwardBillDetails = new BillInwardLorryDetails(billDetailsDto);
                }
                billInwardBillDetails.setInwardId(result.getId());
                billInwardLorryDetailsRepository.save(billInwardBillDetails);
            }
        }

        if (result.getProductCategory().equalsIgnoreCase("RawMaterial")) {

            Optional<DirectPurchase> optional = directPurchaseRepository.findBy(result.getBillNo(), result.getId());
            if (optional.isPresent()) {
                if (result.getLorry_without_Weight() != null && optional.get().getStock_status().equalsIgnoreCase("")) {
                    billUtil.updateBillForward(result, billInwardItemReceivedDtos);
                }
            } else {
                billUtil.updateDpEntry(result, billInwardDto, billInwardItemReceivedDtos);
            }
        }
        return new ResponseEntity<Status>(new Status("200", "billInward updated successfully!!!"), HttpStatus.OK);
    }

    public BatchMaster checkManualBatch(BillInwardDto billInwardDto, BillInwardItemReceivedDto billInwardItemReceivedDto, String billNo, Long vendorId, String inwardDate, User loggedUser) {

        String batchNo = null;

        batchNo = billInwardItemReceivedDto.getBatchNo();

        System.out.println("check batch = " + batchNo);

        Optional<BatchMaster> optionalBatchMaster = batchMasterRepository.findByBatchno(batchNo);
        if (!optionalBatchMaster.isPresent()) {
            BatchMaster batchMaster = new BatchMaster();
            batchMaster.setBatchno(batchNo);
            batchMaster.setDept(loggedUser.getDepartment().getId());
            batchMaster.setBranch(loggedUser.getBranch_id());
            batchMaster.setProductId(billInwardItemReceivedDto.getItemId());
            batchMaster.setCreatedby(loggedUser.getId());
            batchMaster.setBtype("INWARD");
            BatchMaster result = batchMasterRepository.save(batchMaster);
            return result;
        } else {
            return optionalBatchMaster.get();
        }
    }

    public ResponseEntity<?> updateStockStatus(Long id, String stockStatus, User user) {
        Optional<DirectPurchase> optionalModel = directPurchaseRepository.findById(id);
        if (optionalModel.isPresent() && stockStatus.equalsIgnoreCase("Quality Checked")) {
            optionalModel.get().setStock_status("Quality Checked");
            directPurchaseRepository.save(optionalModel.get());
            return new ResponseEntity<Status>(new Status("200", "Stock Status Updated SuccessFully!!!"), HttpStatus.OK);
        }
        return ResponseEntity.badRequest().body("Entity not found");
    }

    public void updateAccountLedger(DirectPurchase directPurchase, User user) {

        ViewAccountsLedger ledger = new ViewAccountsLedger();
        if (directPurchase.getCredit_or_debit() != null) {
            ledger.setTrnType(1L); // 1L
            ledger.setDescription("Purchase for - " + directPurchase.getReferenceNo());
        } else if (directPurchase.getETransfer() != null) {
            ledger.setTrnType(1L); // 1L
            ledger.setDescription("Purchase for - " + directPurchase.getReferenceNo());
        }
        ledger.setCompany(directPurchase.getFirm_org_id());
        ledger.setBranch(directPurchase.getBranch().getId());
        ledger.setPartyId(directPurchase.getVendor_id());
        Optional<BillInward> billInward = billInwardRepository.findById(directPurchase.getBillRefId());
        if (billInward.isPresent()) {
            ledger.setTrnno(billInward.get().getBillNo());
            ledger.setTrnAmount(billInward.get().getBillAmount());
        }
        ledger.setTrnDate(dateUtil.getISTDate());
        ledger.setRefDate(directPurchase.getBill_date());
        viewAccountsLedgerRepository.save(ledger);
    }


    public void updateETransfer(DirectPurchase directPurchase, User user) {

        ViewAccountsLedger ledger = new ViewAccountsLedger();
        ledger.setTrnType(2L); // 2L
        ledger.setDescription("Payment for - " + directPurchase.getReferenceNo());
        ledger.setCompany(directPurchase.getFirm_org_id());
        ledger.setBranch(directPurchase.getBranch().getId());
        ledger.setPartyId(directPurchase.getVendor_id());
        Optional<BillInward> billInward = billInwardRepository.findById(directPurchase.getBillRefId());
        if (billInward.isPresent()) {
            ledger.setTrnno(billInward.get().getBillNo());
            ledger.setTrnAmount(billInward.get().getBillAmount());
        }
        ledger.setTrnDate(dateUtil.getISTDate());
        ledger.setRefDate(directPurchase.getBill_date());
        viewAccountsLedgerRepository.save(ledger);
    }

    public void deleteAccountLedger(DirectPurchase directPurchase) {
        Optional<BillInward> billInward = billInwardRepository.findById(directPurchase.getBillRefId());
        if (billInward.isPresent()) {
            List<ViewAccountsLedger> ledger = viewAccountsLedgerRepository.findByTrnno(billInward.get().getBillNo());
            if (ledger != null) {
                viewAccountsLedgerRepository.deleteAll(ledger);
            }
        }
    }

    public void openPurchaseOrder(BillInwardItemReceived billInwardItemReceived) {
        Optional<PurchaseOrderItems> optional = purchaseOrderItemsRepository.findBy(billInwardItemReceived.getPo_id(), billInwardItemReceived.getItem_id());
        if (optional.isPresent()) {
            PurchaseOrderItems po = optional.get();
            po.setPo_inward_status("Open");
            purchaseOrderItemsRepository.save(po);
        }
    }

    public ResponseEntity<?> updateConfigHoBillInward(String id) throws UsernameNotFoundException {
        try {
            Optional<BillInward> billInwardOptional = billInwardRepository.findById(Long.parseLong(id));
            if (billInwardOptional.isPresent()) {
                createBillInWard(billInwardOptional.get());
            }
            return new ResponseEntity<Status>(new Status("200", "Updated ConfigHoBillInward SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }


    public void deleteDirectPurchaseDetails(Long id) throws UsernameNotFoundException {

        Optional<DirectPurchase> directPurchaseOptional = directPurchaseRepository.findById(id);
        if (directPurchaseOptional.isPresent()) {
            DirectPurchase directPurchase = directPurchaseOptional.get();
            List<DpEntryStoredProduct> dpEntryStoredProducts = storedProductRepository.findBy(directPurchase.getId());
            if (!dpEntryStoredProducts.isEmpty()) {
                for (DpEntryStoredProduct dpEntryStoredProduct : dpEntryStoredProducts) {
                    List<DpEntryStoredBinDetails> binDetails = storedBinDetailsRepository.findByProductId(dpEntryStoredProduct.getId());
                    if (!binDetails.isEmpty()) {
                        storedBinDetailsRepository.deleteAll(binDetails);
                    }
                    storedProductRepository.delete(dpEntryStoredProduct);
                }
            }
            directPurchaseRepository.delete(directPurchase);
        } else {
            System.out.println("Nodata");
        }
    }


    public void rejectInspection(DirectPurchase directPurchase) {

        List<DpEntryStoredProduct> dpEntryStoredProducts = storedProductRepository.findBy(directPurchase.getId());

        if (!dpEntryStoredProducts.isEmpty()) {

            for (DpEntryStoredProduct dpEntryStoredProduct : dpEntryStoredProducts) {

                List<QualityInspectionScore> qualityInspectionScores = qualityInspectionScoreRepo.findList(dpEntryStoredProduct.getItemId(), dpEntryStoredProduct.getBill_ref_id(), dpEntryStoredProduct.getPo_id(), dpEntryStoredProduct.getPo_number());

                if (!qualityInspectionScores.isEmpty()) {
                    qualityInspectionScoreRepo.deleteAll(qualityInspectionScores);
                }

                List<SupplierEvaluationReport> supplierEvaluationReportList = supplierEvaluationReportRepository.findByList(dpEntryStoredProduct.getPo_id());

                if (!supplierEvaluationReportList.isEmpty()) {
                    supplierEvaluationReportRepository.deleteAll(supplierEvaluationReportList);
                }
            }

        }
    }


    public List<BillForwardDto> getForwardList(List<Long> forwardIds) {

        Long forwardNo = null;

        Optional<TRNIndex> trnIndex = trnIndexRepo.findByPrefix("HO_Forward");
        if (trnIndex.isPresent()) {
            TRNIndex trn = trnIndex.get();
            forwardNo = trn.getForwardno();
            Long fstrnlast = forwardNo + 1;
            trn.setForwardno(fstrnlast);
            trnIndexRepo.saveAndFlush(trn);
        }
        billForwardRepository.updateTrnNo(forwardNo, forwardIds);

        System.out.println(forwardNo);

        List<BillForward> billForwards = billForwardRepository.forwardList(forwardIds);
        List<BillForwardDto> billForwardDtoList = new ArrayList<>();
        if (!billForwards.isEmpty()) {
            for (BillForward billForward : billForwards) {
                BillForwardDto forwardDto = new BillForwardDto(billForward);
                forwardDto.setTrnNo(forwardNo);
                billForwardDtoList.add(forwardDto);
            }
        }
        return billForwardDtoList;

    }


    public ResponseEntity<?> updateConfigHoBillInWardItems(String id) throws UsernameNotFoundException {
        try {
            Optional<BillInward> billInwardOptional = billInwardRepository.findById(Long.parseLong(id));
            if (billInwardOptional.isPresent()) {
                updateBillInWard(billInwardOptional.get());
            }
            return new ResponseEntity<Status>(new Status("200", "Updated ConfigHoBillInward SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public void updateBillInWard(BillInward inward) {


        Optional<HOConfigRESTLog> configRESTLogOptional = restLogRepository.findBy(inward.getHoBillId());
        if (configRESTLogOptional.isPresent()) {

            HOConfigRESTLog hoConfigRESTLog = configRESTLogOptional.get();

            ObjectMapper objectMapper = new ObjectMapper();

            try {

                GRNDto billInwardDto = objectMapper.readValue(hoConfigRESTLog.getJson_data(), GRNDto.class);

                BillInward billInward = inward;

                if (billInwardDto.getItemReceives() != null && !billInwardDto.getItemReceives().isEmpty()) {

                    for (GRNProducts billInwardProductDto : billInwardDto.getItemReceives()) {

                        BillInwardItemReceived billInwardProduct = new BillInwardItemReceived(billInwardProductDto);

                        Double lot = Double.valueOf(billInwardProduct.getLot());

                        Double packageQty = Double.valueOf(billInwardProduct.getPackageQuantity());

                        Double covQty = packageQty / lot;
                        System.out.println(covQty);
                        billInwardProduct.setConv_package_quantity(covQty);

                        String convertValue = decfor.format(covQty);

                        List<String> values = new ArrayList<>(Arrays.asList(convertValue.split("\\.")));
                        String bagValue = values.get(0);

                        Double kgValues = Double.valueOf("0." + values.get(1));
                        Double covKGQty = lot * kgValues;
                        String kgValue = decfor1.format(covKGQty);
                        int kg = Integer.parseInt(kgValue);
                        String finalCovQty = null;
                        if (9 >= kg) {
                            finalCovQty = bagValue + ".0" + kgValue;
                        } else {
                            finalCovQty = bagValue + "." + kgValue;
                        }

                        billInwardProduct.setConv_package_quantity_value(finalCovQty);

                        Optional<PurchaseOrder> purchaseOrder = purchaseOrderRepository.getPurchaseOrderByHoPOId(billInwardProductDto.getPoId());
                        if (purchaseOrder.isPresent()) {
                            billInwardProduct.setPo_id(purchaseOrder.get().getId());
                            billInwardProduct.setPo_number(purchaseOrder.get().getPoNumber());
                            billInwardProduct.setHoItemRecId(billInwardProductDto.getHoItemRecId());
                            billInwardProduct.setItem_type(Long.valueOf(28));
                        }

                        billInwardProduct.setInwardId(billInward.getId());
                        billInwardItemReceivedRepository.save(billInwardProduct);

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }


    public ResponseEntity<?> updateConfigHoBillForward(String id) throws UsernameNotFoundException {
        try {
            Optional<BillInward> billInwardOptional = billInwardRepository.findById(Long.parseLong(id));
            if (billInwardOptional.isPresent()) {
                updateBillForward(billInwardOptional.get());
            }
            return new ResponseEntity<Status>(new Status("200", "Updated ConfigHoBillInward SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }


    public void updateBillForward(BillInward inward) {

        BillInwardDto billInward = new BillInwardDto(inward);

        if (billInward.getItemReceives() != null && !billInward.getItemReceives().isEmpty()) {

            Optional<BillForward> optional = billForwardRepository.findByBillInwardId(inward.getId());
            if (optional.isPresent()) {
                BillForward billForward = optional.get();
                BillForwardDto billForwardDto = new BillForwardDto(billForward);
                if (billForwardDto.getBillDetails().isEmpty()) {
                    for (BillInwardItemReceivedDto billInwardProduct : billInward.getItemReceives()) {
                        BillForwardProduct billForwardProduct = new BillForwardProduct(billInwardProduct);
                        billForwardProduct.setForwardId(billForward.getId());
                        billForwardProductRepository.save(billForwardProduct);

                    }
                }
            }
        }
    }


    public void updateConfigPO() {

        List<PurchaseOrderItems> purchaseOrderItems = purchaseOrderItemsRepository.findAll();

        if (purchaseOrderItems != null && !purchaseOrderItems.isEmpty()) {

            for (PurchaseOrderItems items : purchaseOrderItems) {

                Optional<BillInwardItemReceived> billInwardItemReceived = billInwardItemReceivedRepository.findBy(items.getPurchaseOrderId(), items.getITEM_ID());
                if (!billInwardItemReceived.isPresent()) {
                    items.setPo_inward_status("Open");
                    purchaseOrderItemsRepository.save(items);
                } else {
                    items.setPo_inward_status("Closed");
                    purchaseOrderItemsRepository.save(items);
                }

            }
        }
    }


    public void updateBillConfig(Long billinwardId) {

        Optional<BillInward> billInwardget = billInwardRepository.findById(billinwardId);

        if (billInwardget.isPresent()) {

            BillInward billInward = billInwardget.get();

            BillInwardDto inwardDto = new BillInwardDto(billInwardget.get());

            List<BillInwardItemReceivedDto> billInwardItemReceivedDto = billInward.getItemReceived().stream().map(val -> new BillInwardItemReceivedDto(val)).collect(Collectors.toList());

            billUtil.updateDpEntry(billInward, inwardDto, billInwardItemReceivedDto);

        }
    }

    public void updateDPConfig(Long dpId) {

        Optional<DirectPurchase> directPurchaseOptional = directPurchaseRepository.findById(dpId);

        if (directPurchaseOptional.isPresent()) {

            Optional<BillInward> billInwardget = billInwardRepository.findById(directPurchaseOptional.get().getBillRefId());

            if (billInwardget.isPresent()) {

                Optional<User> userOptional = userRepository.findById(directPurchaseOptional.get().getApproved_by());

                BillInward billInward = billInwardget.get();

                BillInwardDto inwardDto = new BillInwardDto(billInwardget.get());

                List<BillInwardItemReceivedDto> billInwardItemReceivedDto = billInward.getItemReceived().stream().map(val -> new BillInwardItemReceivedDto(val)).collect(Collectors.toList());

                billUtil.updateBillApproveToForward(billInward, billInwardItemReceivedDto, userOptional.get());

            }

        }
    }


    public void updateBillInwardToBillChange(Long inwardId) {


        Optional<BillInward> billInwardget = billInwardRepository.findById(inwardId);

        if (billInwardget.isPresent()) {

            BillInward billInward = billInwardget.get();

            BillInwardDto inwardDto = new BillInwardDto(billInwardget.get());

            List<BillInwardItemReceivedDto> billInwardItemReceivedDto = billInward.getItemReceived().stream().map(val -> new BillInwardItemReceivedDto(val)).collect(Collectors.toList());

            billUtil.updateBillExchange(billInward, billInwardItemReceivedDto);

        }

    }


    public void closePurchaseOrder() {

        List<BillInwardItemReceived> list = billInwardItemReceivedRepository.findAll();

        if (!list.isEmpty()) {

            for (BillInwardItemReceived itemReceived : list) {

                System.out.println("poid = " + itemReceived.getPo_id());
                System.out.println("itemId = " + itemReceived.getItem_id());
                Optional<PurchaseOrderItems> optional = purchaseOrderItemsRepository.findBy(itemReceived.getPo_id(), itemReceived.getItem_id());
                Double receivedQty = billInwardItemReceivedRepository.sumOfPoQty(itemReceived.getPo_id(), itemReceived.getItem_id());
                System.out.println("receivedQty = " + receivedQty);
                Double poQty = orderItemsRepository.sumOfPoQty(itemReceived.getPo_id(), itemReceived.getItem_id());
                System.out.println("poQty = " + poQty);
                System.out.println("checkQuantity = " + poQty + "  -  " + receivedQty);
                if (optional.isPresent()) {
                    PurchaseOrderItems po = optional.get();
                    System.out.println("po itemId = " + po.getId());
                    if (Double.compare(receivedQty, poQty) == 0) {
                        po.setPo_inward_status("Closed");
                    } else {
                        po.setPo_inward_status("Open");
                    }
                    purchaseOrderItemsRepository.save(po);
                }

            }
        }
    }


    public void updateDpProduct() {

        List<DirectPurchase> directPurchaseList = directPurchaseRepository.findAllIssues();

        for (DirectPurchase result : directPurchaseList) {

            List<BillInwardItemReceived> billInwardProducts = billInwardItemReceivedRepository.findBy(result.getBillRefId());

            if (!billInwardProducts.isEmpty()) {

                for (BillInwardItemReceived billInwardProduct : billInwardProducts) {

                    DpEntryStoredProduct storedProduct = new DpEntryStoredProduct();

                    storedProduct.setEntryId(result.getId());
                    storedProduct.setBatchNo(billInwardProduct.getBatchNo());
                    storedProduct.setBatchId(billInwardProduct.getBatchId());
                    storedProduct.setTotalQuantity(billInwardProduct.getReceivedQuantity().toString());
                    storedProduct.setItemId(billInwardProduct.getItem_id());
                    storedProduct.setItemName(billInwardProduct.getItem_Name());
                    storedProduct.setItemType(billInwardProduct.getItem_type());
                    storedProduct.setPackageType(billInwardProduct.getPackageType());
                    storedProduct.setUnitCost(billInwardProduct.getUnitCost());
                    storedProduct.setUnitRate(billInwardProduct.getUnitRate());
                    storedProduct.setLot(billInwardProduct.getLot());
                    storedProduct.setPo_id(billInwardProduct.getPo_id());
                    storedProduct.setPo_number(billInwardProduct.getPo_number());
                    storedProduct.setBill_ref_id(result.getBillRefId());
                    Color color = new Color();
                    color.setId(23L);
                    storedProduct.setColor(color);
                    storedProductRepository.save(storedProduct);
                }
            } else {
                System.out.println(" noitem Bill Ref Id inward id == " + result.getBillRefId());
            }
        }
    }


    public ResponseEntity<?> listAllBillNumberAndVendor(String billNo, Long vendorId, Long deliveryBranchId, String type) throws UsernameNotFoundException {
        List<ProductDistruptancesBillInwardList> billInwardList = null;
        if (type != null && type.equalsIgnoreCase("ExcessOrShortage")) {

            if (billNo != null) {
                billInwardList = billInwardRepository.excessShortageBillInwardListWithOutLimit(billNo, vendorId, deliveryBranchId);
            } else {
                billInwardList = billInwardRepository.excessShortageBillInwardListWithLimit(billNo, vendorId, deliveryBranchId);
            }
        } else {
            if (billNo != null) {
                billInwardList = billInwardRepository.purchaseReturnBillInwardListWithOutLimit(billNo, vendorId, deliveryBranchId);
            } else {
                billInwardList = billInwardRepository.purchaseReturnBillInwardListWithLimit(billNo, vendorId, deliveryBranchId);
            }
        }

        List<ProductDistruptancesBillInwardDto> result = billInwardList.stream().map(ProductDistruptancesBillInwardDto::new).collect(Collectors.toList());
        return new ResponseEntity<List<ProductDistruptancesBillInwardDto>>(result, HttpStatus.OK);

    }


    public ResponseEntity<?> excessOrReturnBillDetails(Long id) throws UsernameNotFoundException {


        Optional<BillInward> billInwardOptional = billInwardRepository.findById(id);

        if (billInwardOptional.isPresent()) {

            ProductDistruptancesBillInwardDto result = new ProductDistruptancesBillInwardDto(billInwardOptional.get());

            List<DpEntryStoredProduct> products = storedProductRepository.findByBillRefId(result.getId());
            if (!products.isEmpty()) {
                List<DpEntryStoredProductDto> productDtoList = new ArrayList<>();
                for (DpEntryStoredProduct storedProduct : products) {
                    DpEntryStoredProductDto dto = new DpEntryStoredProductDto(storedProduct);
                    List<DpEntryStoredBinDetailsDto> binDetailList = new ArrayList<>();
                    System.out.println("binn details = " + storedProduct.getBinDetails());
                    for (DpEntryStoredBinDetails bin : storedProduct.getBinDetails()) {
                        DpEntryStoredBinDetailsDto binDto = new DpEntryStoredBinDetailsDto(bin);
                        Object qty = binStockLogRepository.getConceptionStock(dto.getItemId(), dto.getBatchNo(), binDto.getBinId());
                        binDto.setConceptionStock(qty.toString());
                        binDetailList.add(binDto);
                    }
                    dto.setBinDetails(binDetailList);
                    productDtoList.add(dto);
                }
                result.setStoredProductDtos(productDtoList);
            }
            return new ResponseEntity<ProductDistruptancesBillInwardDto>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<ProductDistruptancesBillInwardDto>(HttpStatus.NOT_FOUND);
        }

    }

    public ResponseEntity<?> lorryArrivalData(String lorryNo, String inwardDate, String inwardBranchName, Long deliverBranchId, Long inwarBranchId, Long month, Long year, String billDate, String billNo, String ewayBillNo, String place, String vendorName, String rate, String lot,
                                              String quantity, String amount, String freightPaid, String deliverBranchName, String updatedDate, String productName, String verificationStatus, Pageable pageable) {
        Date dateInward = null, dateBill = null;
        Date dateUpdated = null;

        if (billDate != null || inwardDate != null || updatedDate != null) {
            dateInward = dateUtil.getStringTODateWk(inwardDate);
            dateBill = dateUtil.getStringTODateWk(billDate);
            dateUpdated = dateUtil.getStringTODateWk(updatedDate);
            System.out.println("datebill" + dateBill + "and inwarddate" + dateInward);
        }

        Page<LorryArrivalDataList> filterLorryArrivalData = billInwardRepository.filterLorryArrivalData(lorryNo, inwarBranchId, month, year, deliverBranchId, dateInward, inwardBranchName, dateBill, billNo, place, vendorName, rate, lot, ewayBillNo, quantity, amount, freightPaid, deliverBranchName, dateUpdated, productName, pageable);

        List<LorryArrivalDataDto> dtoList = filterLorryArrivalData.stream()
                .map(lorry -> {
                    LorryArrivalDataDto lorryArrival = new LorryArrivalDataDto(lorry);
                    if (lorry.getStatus().equalsIgnoreCase("Pending")) {
                        lorryArrival.setVerificationStatus("Bill_Inwarded");
                        lorryArrival.setUpdatedOn(lorry.getInwardCreatedDate());
                    } else {
                        if (lorry.getStatus().equalsIgnoreCase("Stock_Inwarded")) {
                            lorryArrival.setVerificationStatus(lorry.getStatus());
                            lorryArrival.setUpdatedOn(lorry.getStockInwardDate());
                        } else {
                            lorryArrival.setVerificationStatus(lorry.getStatus());
                            lorryArrival.setUpdatedOn(lorry.getApprovedDate());
                        }
                    }
                    if (lorry.getAudiStatus() == null) {
                        lorryArrival.setQcReportStatus("Not Started");
                    } else {
                        lorryArrival.setQcReportStatus(lorry.getAudiStatus());
                    }
                    return lorryArrival;
                })
                .collect(Collectors.toList());
        PageDataDto<LorryArrivalDataDto> pageData = new PageDataDto<>(dtoList, filterLorryArrivalData);
        return new ResponseEntity<>(pageData, HttpStatus.OK);
    }

}