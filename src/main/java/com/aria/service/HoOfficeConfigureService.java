package com.aria.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class HoOfficeConfigureService {

    private static final DecimalFormat decfor = new DecimalFormat("0.00");
    private static final DecimalFormat decfor1 = new DecimalFormat("0");
    @Autowired
    BillInwardRepository billInwardRepository;

    @Autowired
    BillInwardItemReceivedRepository billInwardItemReceivedRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    BatchMasterRepository batchMasterRepository;

    @Autowired
    BillInwardLorryDetailsRepository billInwardLorryDetailsRepository;

    @Autowired
    VendorRepository vendorRepository;

    @Autowired
    ProductMasterRepository productMasterRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    PurchaseOrderItemsRepository purchaseOrderItemsRepository;

    @Autowired
    HOConfigRESTLogRepository restLogRepository;


    @Transactional
    public ResponseEntity<?> grnBillInward(GRNDto billInwardDto, HttpServletRequest request) {

        if (billInwardDto.getHoBillId() != null && billInwardDto.getHoBillId() != 0) {
            Optional<BillInward> orderOptional = billInwardRepository.findByBillNoAndVendorIdANDHoBillId(billInwardDto.getHoBillId(), billInwardDto.getBillNo(), billInwardDto.getVendorId());
            if (orderOptional.isPresent()) {
                HOStatus hoStatus = new HOStatus("400", "Ho BillId & vendorId & Bill No is already exits!!", "failure", "BillInward Not Created", request.getServletPath());
                updateLog("GRN", hoStatus, billInwardDto);
                return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);
            }

            Optional<BillInward> billInwardOptional = billInwardRepository.findByHoBillId(billInwardDto.getHoBillId());
            if (billInwardOptional.isPresent()) {
                HOStatus hoStatus = new HOStatus("400", "Ho BillId is already exits!!", "failure", "BillInward Not Created", request.getServletPath());
                updateLog("GRN", hoStatus, billInwardDto);
                return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);
            }

        } else {
            HOStatus hoStatus = new HOStatus("400", "Ho BillId is not valid!!", "failure", "BillInward Not Created", request.getServletPath());
            updateLog("GRN", hoStatus, billInwardDto);
            return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);
        }

        Optional<Vendor> optionalVendor = null;
        if (billInwardDto.getVendorId() != null && billInwardDto.getVendorId() != 0) {
            optionalVendor = vendorRepository.findById(billInwardDto.getVendorId());
            if (!optionalVendor.isPresent()) {
                HOStatus hoStatus = new HOStatus("400", "vendor id is not valid!!", "failure", "BillInward Not Created", request.getServletPath());
                updateLog("GRN", hoStatus, billInwardDto);
                return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);
            }
        } else {
            HOStatus hoStatus = new HOStatus("400", "vendor id is not valid!!", "failure", "BillInward Not Created", request.getServletPath());
            updateLog("GRN", hoStatus, billInwardDto);
            return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);
        }

        if ((billInwardDto.getVendorId() != null && billInwardDto.getVendorId() != 0) && (billInwardDto.getBillNo() != null && billInwardDto.getBillNo() != "") && (billInwardDto.getBillDate() != null && billInwardDto.getBillDate() != "") ) {
            Optional<BillInward> billInwardOptional = billInwardRepository.findByBillNoAndVendorIdBillDate(billInwardDto.getBillNo(), billInwardDto.getVendorId(), dateUtil.getStringTODateWk(billInwardDto.getBillDate()) );
            if (billInwardOptional.isPresent()) {
                HOStatus hoStatus = new HOStatus("400", "same vendorId & Bill No & Bill Date is already exits!!", "failure", "BillInward Not Created", request.getServletPath());
                updateLog("GRN", hoStatus, billInwardDto);
                return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);
            }
        }

        if (!billInwardDto.getItemReceives().isEmpty()) {
            for (GRNProducts billInwardProductDto : billInwardDto.getItemReceives()) {
                if (billInwardProductDto.getItemId() != null && billInwardProductDto.getItemId() != 0) {
                    Optional<ProductMaster> optional = productMasterRepository.findById(billInwardProductDto.getItemId());
                    if (!optional.isPresent()) {
                        HOStatus hoStatus = new HOStatus("400", "Item id is not valid!!", "failure", "BillInward Not Created", request.getServletPath());
                        updateLog("GRN", hoStatus, billInwardDto);
                        return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);
                    } else {
                        billInwardProductDto.setItemId(optional.get().getId());
                        billInwardProductDto.setItemName(optional.get().getProductName());
                    }
                    Optional<PurchaseOrder> purchaseOrder = purchaseOrderRepository.getPurchaseOrderByHoPOId(billInwardProductDto.getPoId());
                    if (!purchaseOrder.isPresent()) {
                        HOStatus hoStatus = new HOStatus("400", "Ho Po id is not valid!!", "failure", "BillInward Not Created", request.getServletPath());
                        updateLog("GRN", hoStatus, billInwardDto);
                        return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);
                    }
                } else {
                    HOStatus hoStatus = new HOStatus("400", "Item id is not valid!!", "failure", "BillInward Not Created", request.getServletPath());
                    updateLog("GRN", hoStatus, billInwardDto);
                    return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);
                }
            }
        }
        // Subash is discussed at factory
       /* if (billInwardDto.getDeliverBranchName() != null && billInwardDto.getDeliverBranchName() != "") {
            Optional<Branch> branch = branchRepository.findBy(billInwardDto.getDeliverBranchName());
            if (branch.isPresent()) {
                billInwardDto.setDeliverBranchId(branch.get().getId());
            } else {
                HOStatus hoStatus = new HOStatus("400", "Delivery Branch Name is not valid!!", "failure", "BillInward Not Created", request.getServletPath());
                updateLog("GRN", hoStatus, billInwardDto);
                return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);
            }
        }else {
            HOStatus hoStatus = new HOStatus("400", "Delivery Branch Name is not valid!!", "failure", "BillInward Not Created", request.getServletPath());
            updateLog("GRN", hoStatus, billInwardDto);
            return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);
        }*/

        if (billInwardDto.getFirmOrgName() != null && billInwardDto.getFirmOrgName() != "") {
            Optional<Organisation> organisation = organisationRepository.findBy(billInwardDto.getFirmOrgName());
            if (organisation.isPresent()) {
                billInwardDto.setFirmOrgId(organisation.get().getId());
            } else {
                HOStatus hoStatus = new HOStatus("400", "Firm Name is not valid!!", "failure", "BillInward Not Created", request.getServletPath());
                updateLog("GRN", hoStatus, billInwardDto);
                return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);
            }
        }

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
        HOStatus hoStatus = new HOStatus("200", "", "successfully created!!", "BillInward created Successfully!!!", request.getServletPath(), result.getId());
        updateLog("GRN", hoStatus, billInwardDto);
        return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);

    }


    public BatchMaster checkBatch(BillInwardDto billInward, BillInwardItemReceivedDto billInwardItemReceivedDto, String billNo, Long vendorId, String inwardDate) {

        String batchNo = inwardDate + "-" + billInward.getLorrySerialNo() + "#" + billNo + "#" + vendorId + "#" + billInwardItemReceivedDto.getItemId();

        System.out.println("check batch = " + batchNo);

        Optional<BatchMaster> optionalBatchMaster = batchMasterRepository.findByBatchno(batchNo);
        if (!optionalBatchMaster.isPresent()) {
            BatchMaster batchMaster = new BatchMaster();
            batchMaster.setBatchno(batchNo);
            //batchMaster.setDept(loggedUser.getDepartment().getId());
            //batchMaster.setBranch(loggedUser.getBranch_id());
            batchMaster.setProductId(billInwardItemReceivedDto.getItemId());
            //batchMaster.setCreatedby(loggedUser.getId());
            batchMaster.setBtype("INWARD");
            BatchMaster result = batchMasterRepository.save(batchMaster);
            return result;
        } else {
            return optionalBatchMaster.get();
        }
    }


    public String getHoData(GRNDto billInwardDto) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> map = objectMapper.convertValue(billInwardDto, new TypeReference<Map<String, Object>>() {
            });
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getHoStatusData(HOStatus hoStatus) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> map = objectMapper.convertValue(hoStatus, new TypeReference<Map<String, Object>>() {
            });
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public String getHoData(HoPurchaseOrderDto billInwardDto) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> map = objectMapper.convertValue(billInwardDto, new TypeReference<Map<String, Object>>() {
            });
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void updateLog(String type, HOStatus response, GRNDto jsonData) {
        String status = response.getStatus();
        Long typeId = jsonData.getHoBillId();
        String responseData = getHoStatusData(response);
        String jsonData1 = getHoData(jsonData);
        updateRestLog(type, typeId, status, responseData, jsonData1);
    }


    public void updateRestLog(String type, Long typeId, String status, String response, String jsonData) {

        HOConfigRESTLog hoConfigRESTLog = new HOConfigRESTLog();
        hoConfigRESTLog.setType(type);
        hoConfigRESTLog.setFrom_office("HO");
        hoConfigRESTLog.setTypeId(typeId);
        hoConfigRESTLog.setStatus(status);
        hoConfigRESTLog.setResponse(response);
        hoConfigRESTLog.setJson_data(jsonData);
        hoConfigRESTLog.setCreated_time(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        restLogRepository.save(hoConfigRESTLog);
    }


    public ResponseEntity<?> saveHoPurchaseOrder(HttpServletRequest request, HoPurchaseOrderDto purchaseOrderDto) {

        if (purchaseOrderDto.getId() != null && purchaseOrderDto.getId() != 0) {
            Optional<PurchaseOrder> orderOptional = purchaseOrderRepository.getPurchaseOrderByHoPOId(purchaseOrderDto.getId());
            if (orderOptional.isPresent()) {
                HOStatus hoStatus = new HOStatus("400", "Ho id is already exits!!", "failure", "Purchase Order Not created", request.getServletPath());
                updateLog("PO", hoStatus, purchaseOrderDto);
                return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<HOStatus>(new HOStatus("400", "Ho id is not valid!!", "failure", "Purchase Order Not created", request.getServletPath()), HttpStatus.OK);
        }

        Optional<Vendor> optionalVendor = null;
        if (purchaseOrderDto.getEarms_party_id() != null && purchaseOrderDto.getEarms_party_id() != 0) {
            optionalVendor = vendorRepository.findById(purchaseOrderDto.getEarms_party_id());
            if (!optionalVendor.isPresent()) {
                HOStatus hoStatus = new HOStatus("400", "earms supplier id is not valid!!", "failure", "Purchase Order Not created", request.getServletPath());
                updateLog("PO", hoStatus, purchaseOrderDto);
                return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);
            }
        } else {
            HOStatus hoStatus = new HOStatus("400", "earms supplier id is not valid!!", "failure", "Purchase Order Not created", request.getServletPath());
            updateLog("PO", hoStatus, purchaseOrderDto);
            return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);
        }

        for (PurchaseOrderItemsDto purchaseOrderItemsDto : purchaseOrderDto.getPoOrderItems()) {
            PurchaseOrderItems purchaseOrderItems = new PurchaseOrderItems(purchaseOrderItemsDto);
            if (purchaseOrderItemsDto.getEarms_item_id() != null && purchaseOrderItemsDto.getEarms_item_id() != 0) {
                purchaseOrderItems.setITEM_ID(purchaseOrderItemsDto.getEarms_item_id());
            } else {
                HOStatus hoStatus = new HOStatus("400", "earms item id is not valid!!", "failure", "Purchase Order Not created", request.getServletPath());
                updateLog("PO", hoStatus, purchaseOrderDto);
                return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);
            }
            Optional<ProductMaster> optional = productMasterRepository.findById(purchaseOrderItemsDto.getEarms_item_id());
            if (!optional.isPresent()) {
                HOStatus hoStatus = new HOStatus("400", "earms item id is not valid!!", "failure", "Purchase Order Not created", request.getServletPath());
                updateLog("PO", hoStatus, purchaseOrderDto);
                return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);
            }
        }


        PurchaseOrder purchaseOrder = new PurchaseOrder(purchaseOrderDto);
        purchaseOrder.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        purchaseOrder.setCompanyBranchId(10L);

        if (purchaseOrderDto.getEarms_party_id() != null && purchaseOrderDto.getEarms_party_id() != 0) {
            optionalVendor = vendorRepository.findById(purchaseOrderDto.getEarms_party_id());
        } else {
            HOStatus hoStatus = new HOStatus("400", "earms supplier id is not valid!!", "failure", "Purchase Order Not created", request.getServletPath());
            updateLog("PO", hoStatus, purchaseOrderDto);
            return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);
        }
        if (optionalVendor.isPresent()) {
            purchaseOrder.setVendor(optionalVendor.get());
            purchaseOrder.setSupplierName(optionalVendor.get().getName());
            purchaseOrder.setSupplierId(optionalVendor.get().getId());
        } else {
            HOStatus hoStatus = new HOStatus("400", "earms supplier id is not valid!!", "failure", "Purchase Order Not created", request.getServletPath());
            updateLog("PO", hoStatus, purchaseOrderDto);
            return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);
        }

        purchaseOrder.setTypeFlag("HO");
        purchaseOrder.setRaw_ho_Po(getHoData(purchaseOrderDto));
        PurchaseOrder result = purchaseOrderRepository.save(purchaseOrder);

        if (!purchaseOrderDto.getPoOrderItems().isEmpty() && purchaseOrderDto.getPoOrderItems() != null) {
            for (PurchaseOrderItemsDto purchaseOrderItemsDto : purchaseOrderDto.getPoOrderItems()) {
                PurchaseOrderItems purchaseOrderItems = new PurchaseOrderItems(purchaseOrderItemsDto);

                if (purchaseOrderItemsDto.getEarms_item_id() != null && purchaseOrderItemsDto.getEarms_item_id() != 0) {
                    purchaseOrderItems.setITEM_ID(purchaseOrderItemsDto.getEarms_item_id());
                } else {
                    HOStatus hoStatus = new HOStatus("400", "earms item id is not valid!!", "failure", "Purchase Order Not created", request.getServletPath());
                    updateLog("PO", hoStatus, purchaseOrderDto);
                    return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);
                }

                Optional<ProductMaster> optional = productMasterRepository.findBy(purchaseOrderItemsDto.getEarms_item_id());
                if (optional.isPresent()) {
                    purchaseOrderItems.setITEM_ID(optional.get().getId());
                    purchaseOrderItems.setITEM_NAME(optional.get().getProductName());
                    purchaseOrderItems.setProductMaster(optional.get());
                } else {
                    HOStatus hoStatus = new HOStatus("400", "earms item id is not valid!!", "failure", "Purchase Order Not created", request.getServletPath());
                    updateLog("PO", hoStatus, purchaseOrderDto);
                    return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);
                }

                purchaseOrderItems.setPurchaseOrderId(result.getId());
                purchaseOrderItemsRepository.save(purchaseOrderItems);
            }
        }

        HOStatus hoStatus = new HOStatus("200", "", "successfully created!!", "Purchase order created", request.getServletPath());
        updateLog("PO", hoStatus, purchaseOrderDto);
        return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);
    }


    public void updateLog(String type, HOStatus response, HoPurchaseOrderDto jsonData) {
        String status = response.getStatus();
        Long typeId = jsonData.getId();
        String responseData = getHoStatusData(response);
        String jsonData1 = getHoData(jsonData);
        updateRestLog(type, typeId, status, responseData, jsonData1);
    }


}
