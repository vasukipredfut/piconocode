package com.aria.service;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.BinStockUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.utils.DateUtil;


@Service
public class PurchaseOrderService {

    private static final Logger logger = LoggerFactory.getLogger(BinStockUtil.class);

    private static final DecimalFormat decfor = new DecimalFormat("0.00");

    @Autowired
    PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    ProposalVendorSpecRepository proposalVendorSpecRepo;

    @Autowired
    PurchaseOrderItemsRepository purchaseOrderItemsRepository;

    @Autowired
    PurchasePaymentRepository purchasePaymentRepository;

    @Autowired
    VendorRepository vendorRepository;

    @Autowired
    ProductMasterRepository productMasterRepository;

    @Autowired
    GstTaxCategoryRepository gstTaxCategoryRepository;

    @Autowired
    ProposalRepository proposalRepository;

    @Autowired
    ProposalVendorSpecRepository proposalVendorSpecRepository;

    @Autowired
    ProposalSpecRepository proposalSpecRepository;

    @Autowired
    SpecificationRepository specificationRepository;

    @Autowired
    LotRepository lotRepository;

    @Autowired
    AdvancePaymentForPurchaseOrderRepo advancePaymentForPurchaseOrderRepo;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    BillInwardItemReceivedRepository billInwardItemReceivedRepository;

    @Autowired
    UnitsRepository unitsRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    ViewAccountsLedgerRepository viewAccountsLedgerRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    OurCompanyAddressRepository ourCompanyAddressRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    TRNIndexRepo trnIndexRepo;

    public ResponseEntity<?> savePurchaseOrder(PurchaseOrderDto purchaseOrderDto, User loggedUser) {
        long timestamp = System.currentTimeMillis();
        Random random = new Random(timestamp);
        int randomNumber = random.nextInt(900000) + 100000;

        List<PurchaseOrderItemsDto> purchaseOrderItemsDto_new = new ArrayList<PurchaseOrderItemsDto>();

        if (purchaseOrderDto != null) {
            List<PurchaseOrderItemsDto> purchaseOrderItemsDto = purchaseOrderDto.getPoOrderItems();

            for (PurchaseOrderItemsDto purchaseOrderItemsDto_itr : purchaseOrderItemsDto) {

                if (purchaseOrderItemsDto_itr.getSpec_id() != null && purchaseOrderItemsDto_itr.getSpec_type() == 28) {
                    SpecificationDetails specification = specificationRepository
                            .findbyProductSpecificationId(purchaseOrderItemsDto_itr.getSpec_id());
                    purchaseOrderItemsDto_itr.setItem_id(specification.getRefid());
                    purchaseOrderItemsDto_itr.setProduct_id(specification.getRefid());
                    purchaseOrderItemsDto_itr.setItem_name(specification.getRefname());
                }
                if (purchaseOrderItemsDto_itr.getSpec_id() != null && purchaseOrderItemsDto_itr.getSpec_type() == 29) {
                    SpecificationDetails specification = specificationRepository
                            .findbyMachineSpecificationId(purchaseOrderItemsDto_itr.getSpec_id());
                    purchaseOrderItemsDto_itr.setItem_id(specification.getRefid());
                    purchaseOrderItemsDto_itr.setMachine_id(specification.getRefid());
                    purchaseOrderItemsDto_itr.setItem_name(specification.getRefname());
                }
                if (purchaseOrderItemsDto_itr.getSpec_id() != null && purchaseOrderItemsDto_itr.getSpec_type() == 30) {
                    SpecificationDetails specification = specificationRepository
                            .findbyExpenseSpecificationId(purchaseOrderItemsDto_itr.getSpec_id());
                    purchaseOrderItemsDto_itr.setItem_id(specification.getRefid());
                    purchaseOrderItemsDto_itr.setExpense_id(specification.getRefid());
                    purchaseOrderItemsDto_itr.setItem_name(specification.getRefname());
                }
                if (purchaseOrderItemsDto_itr.getSpec_id() != null && purchaseOrderItemsDto_itr.getSpec_type() == 31) {
                    SpecificationDetails specification = specificationRepository
                            .findbyAMCExpenseSpecificationId(purchaseOrderItemsDto_itr.getSpec_id());
                    purchaseOrderItemsDto_itr.setItem_id(specification.getRefid());
                    purchaseOrderItemsDto_itr.setExpense_id(specification.getRefid());
                    purchaseOrderItemsDto_itr.setItem_name(specification.getRefname());
                }

                purchaseOrderItemsDto_new.add(purchaseOrderItemsDto_itr);
            }

            purchaseOrderDto.setPoOrderItems(purchaseOrderItemsDto_new);
            PurchaseOrder purchaseOrder = new PurchaseOrder(purchaseOrderDto);
            Branch branch = new Branch();
            branch.setId(purchaseOrderDto.getBranch());
            purchaseOrder.setBranch(branch);
            DepartmentDetails dept = new DepartmentDetails();
            dept.setId(purchaseOrderDto.getDept());
            purchaseOrder.setDept(dept);
            purchaseOrder.setTypeFlag("Factory");
            String orgName = "VVV";
            if (purchaseOrderDto.getFirmId() == 1) {
                orgName = "VVV";
            } else {
                orgName = "VRA";
            }
            String ponumber = orgName + "-" + loggedUser.getBranch_id() + "-" + randomNumber;
            purchaseOrder.setPoNumber(ponumber);
            purchaseOrder.setPoDate(dateUtil.getISTDate());
            purchaseOrder.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            purchaseOrder.setCreatedBy(loggedUser);
            purchaseOrder.setCompanyBranchId(loggedUser.getBranch_id());
            purchaseOrder.setStatus("Pending");
            Optional<Vendor> optionalVendor = vendorRepository.findById(purchaseOrderDto.getVendorId());

            if (optionalVendor.isPresent()) {
                purchaseOrder.setVendor(optionalVendor.get());
                purchaseOrder.setSupplierName(optionalVendor.get().getName());
                purchaseOrder.setSupplierId(optionalVendor.get().getId());
                purchaseOrder.setCity(optionalVendor.get().getCity());
                purchaseOrder.setStateName(optionalVendor.get().getState());
                purchaseOrder.setStateId(optionalVendor.get().getStateid());
                purchaseOrder.setCityId(optionalVendor.get().getCityid());
                purchaseOrder.setGstNO(optionalVendor.get().getGstNumber());
            }
            purchaseOrder.setPoDate(dateUtil.getISTDate());
            logger.info("Purchase Order saved successfully with id: " + purchaseOrder.getId());
            PurchaseOrder result = purchaseOrderRepository.save(purchaseOrder);
            updatePurchaseOrderProductsMasterMapping("CREATE", result, purchaseOrderDto);
            updatePurchaseOrderPayment("CREATE", result, purchaseOrderDto, loggedUser);
            updateProposal(result);
            return new ResponseEntity<Status>(new Status("200", "Purchase Order saved successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "Purchase Order Bad Request"), HttpStatus.OK);

        }

    }

    public void updateProposal(PurchaseOrder purchaseOrder) {

        Optional<Proposal> optional = proposalRepository.findById(purchaseOrder.getProposalId());

        if (optional.isPresent()) {

            Proposal po = optional.get();

            logger.info("Reached update proposal logic");
            logger.info("Proposal Category: " + po.getCategory());


            if (po.getCategory() == 39) {

                List<ProposalSpec> proposalspecList = proposalSpecRepository.getProposalspecList(po.getId(), purchaseOrder.getVendor().getId());

                for (ProposalSpec proposalSpec : proposalspecList) {

                    double fixedQty = 0.0;
                    Optional<ProposalVendorSpec> proposalVendorSpec = proposalVendorSpecRepository.getProposalVendorSpecBySpecId(proposalSpec.getProposal_ref_id(), proposalSpec.getId(), purchaseOrder.getVendor().getId());
                    if (proposalVendorSpec.isPresent()) {
                        ProposalVendorSpec rs = proposalVendorSpec.get();
                        if (rs.getNet_approved_qty() != null) {
                            fixedQty = rs.getNet_approved_qty();
                        }
                    }

                    Double poQty = purchaseOrderRepository.findByQty(po.getId(), purchaseOrder.getVendor().getId(), proposalSpec.getSpecid());

                    double finalVal = fixedQty - poQty;
                    if (finalVal > 0) {
                        System.out.println("ProposalVendorSpec status Not Closed for vendor: " + purchaseOrder.getVendor().getId());
                    } else {
                        if (proposalVendorSpec.isPresent()) {
                            ProposalVendorSpec rs = proposalVendorSpec.get();
                            rs.setStatus("Closed");
                            proposalVendorSpecRepository.save(rs);
                            System.out.println("ProposalVendorSpec status set to Closed for vendor: " + purchaseOrder.getVendor().getId());
                        } else {
                            System.out.println("ProposalVendorSpec not found for proposal id: " + po.getId() + " and vendor id: " + purchaseOrder.getVendor().getId());
                        }
                    }
                }

                logger.info("Reached update proposal logic ends");

            } else {
                logger.info("Proposal Category not found in the system");
            }
        } else {
            logger.info("Proposal not found for proposal id: " + purchaseOrder.getProposalId());
        }
    }

    public ResponseEntity<?> updatePurchaseOrder(PurchaseOrderDto purchaseOrderDto, User loggedUser) {

        List<PurchaseOrderItemsDto> purchaseOrderItemsDto_new = new ArrayList<PurchaseOrderItemsDto>();

        if (purchaseOrderDto != null) {

            List<PurchaseOrderItemsDto> purchaseOrderItemsDto = purchaseOrderDto.getPoOrderItems();

            for (PurchaseOrderItemsDto purchaseOrderItemsDto_itr : purchaseOrderItemsDto) {

                if (purchaseOrderItemsDto_itr.getSpec_id() != null && purchaseOrderItemsDto_itr.getSpec_type() == 28) {
                    SpecificationDetails specification = specificationRepository
                            .findbyProductSpecificationId(purchaseOrderItemsDto_itr.getSpec_id());
                    purchaseOrderItemsDto_itr.setItem_id(specification.getRefid());
                    purchaseOrderItemsDto_itr.setProduct_id(specification.getRefid());
                    purchaseOrderItemsDto_itr.setItem_name(specification.getRefname());
                }
                if (purchaseOrderItemsDto_itr.getSpec_id() != null && purchaseOrderItemsDto_itr.getSpec_type() == 29) {
                    SpecificationDetails specification = specificationRepository
                            .findbyMachineSpecificationId(purchaseOrderItemsDto_itr.getSpec_id());
                    purchaseOrderItemsDto_itr.setItem_id(specification.getRefid());
                    purchaseOrderItemsDto_itr.setMachine_id(specification.getRefid());
                    purchaseOrderItemsDto_itr.setItem_name(specification.getRefname());
                }
                if (purchaseOrderItemsDto_itr.getSpec_id() != null && purchaseOrderItemsDto_itr.getSpec_type() == 30) {
                    SpecificationDetails specification = specificationRepository
                            .findbyExpenseSpecificationId(purchaseOrderItemsDto_itr.getSpec_id());
                    purchaseOrderItemsDto_itr.setItem_id(specification.getRefid());
                    purchaseOrderItemsDto_itr.setExpense_id(specification.getRefid());
                    purchaseOrderItemsDto_itr.setItem_name(specification.getRefname());
                }
                if (purchaseOrderItemsDto_itr.getSpec_id() != null && purchaseOrderItemsDto_itr.getSpec_type() == 31) {
                    SpecificationDetails specification = specificationRepository
                            .findbyAMCExpenseSpecificationId(purchaseOrderItemsDto_itr.getSpec_id());
                    purchaseOrderItemsDto_itr.setItem_id(specification.getRefid());
                    purchaseOrderItemsDto_itr.setExpense_id(specification.getRefid());
                    purchaseOrderItemsDto_itr.setItem_name(specification.getRefname());
                }

                purchaseOrderItemsDto_new.add(purchaseOrderItemsDto_itr);
            }
        }
        purchaseOrderDto.setPoOrderItems(purchaseOrderItemsDto_new);
        PurchaseOrder purchaseOrder = null;
        Optional<PurchaseOrder> purchaseOrderOptional = purchaseOrderRepository.findById(purchaseOrderDto.getId());
        if (purchaseOrderOptional.isPresent()) {
            purchaseOrder = gerOrderDetails(purchaseOrderOptional.get(), purchaseOrderDto);
        } else {
            purchaseOrder = new PurchaseOrder(purchaseOrderDto);
        }

        if (purchaseOrder.getPoDate() == null) {
            purchaseOrder.setPoDate(dateUtil.getISTDate());
        }

        purchaseOrder.setCompanyBranchId(loggedUser.getBranch_id());
        purchaseOrder.setUpdatedBy(loggedUser);
        purchaseOrder.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        Optional<Vendor> optionalVendor = vendorRepository.findById(purchaseOrderDto.getVendorId());

        if (optionalVendor.isPresent()) {
            purchaseOrder.setVendor(optionalVendor.get());
            purchaseOrder.setSupplierName(optionalVendor.get().getName());
            purchaseOrder.setSupplierId(optionalVendor.get().getId());
            purchaseOrder.setCity(optionalVendor.get().getCity());
            purchaseOrder.setStateName(optionalVendor.get().getState());
            purchaseOrder.setStateId(optionalVendor.get().getStateid());
            purchaseOrder.setCityId(optionalVendor.get().getCityid());
            purchaseOrder.setGstNO(optionalVendor.get().getGstNumber());

        }
        if (purchaseOrderDto.getStatus() == "Pending") {
            purchaseOrder.setStatus("Pending");
        }

        if (purchaseOrder.getRejectedBy() != null) {
            purchaseOrder.setRejectedBy(null);
            purchaseOrder.setRejectedOn(null);
        }

        if (purchaseOrderDto.isUpdatePurchaseOrder()) {
            purchaseOrder.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            purchaseOrder.setCreatedBy(loggedUser);
        }

        if (purchaseOrderDto.isAmendmentPo()) {
            purchaseOrder.setAmendment_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            purchaseOrder.setAmendmentBy(loggedUser);
        }

        PurchaseOrder result = purchaseOrderRepository.save(purchaseOrder);
        updatePurchaseOrderProductsMasterMapping("UPDATE", result, purchaseOrderDto);
        updatePurchaseOrderPayment("UPDATE", result, purchaseOrderDto, loggedUser);
        return new ResponseEntity<Status>(new Status("200", "Purchase Order updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> purchaseOrderListAll(String poNumber, Long firmId, Long vendorId, String vendorName,
                                                  Long proposalid, Long branch, Long dept, String status, Pageable pageable)
            throws UsernameNotFoundException {

        Page<PurchaseOrder> list = purchaseOrderRepository.getPurchaseOrderFilterByPOAndVendorId(poNumber, vendorId,
                vendorName, proposalid, firmId, dept, branch, status, pageable);

        if (list.toList().size() > 0) {
            List<PoDto> result = list.toList().stream().map(data -> {
                PoDto purchaseOrderDto = new PoDto(data, true);
                if (data.getProposalId() != null) {
                    Optional<Proposal> proposal = proposalRepository.findById(data.getProposalId());
                    if (proposal.isPresent()) {
                        purchaseOrderDto.setProposalId(proposal.get().getId());
                        purchaseOrderDto.setProposalName(proposal.get().getName());
                    }
                }
                return purchaseOrderDto;
            }).collect(Collectors.toList());
            PageDataDto<PoDto> pageData = new PageDataDto<>(result, list);

            return new ResponseEntity<PageDataDto<PoDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<PoDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> purchaseOrderById(String poNumber) throws UsernameNotFoundException {

        Optional<PurchaseOrder> optional = purchaseOrderRepository.findById(Long.valueOf(poNumber));
        if (optional.isPresent()) {
            List<PoItemsDto> itemsDtos = new ArrayList<PoItemsDto>();
            PoDto purchaseOrderDto = new PoDto(optional.get());
            if (purchaseOrderDto.getPoTotalAmount() != null) {
                purchaseOrderDto.setPoTotalAmount_value(decfor.format(purchaseOrderDto.getPoTotalAmount()));
            }
            if (purchaseOrderDto.getFirmId() != null) {
                Optional<Organisation> organisation = organisationRepository.findById(purchaseOrderDto.getFirmId());
                if (organisation != null) {
                    purchaseOrderDto.setDeliveryPlantFirmName(organisation.get().getName());
                    purchaseOrderDto.setDeliveryPlantFirmAddress(organisation.get().getAddress());
                    purchaseOrderDto.setDeliveryPlantFirmCity(organisation.get().getCity());
                    purchaseOrderDto.setDeliveryPlantFirmState(organisation.get().getState());
                    purchaseOrderDto.setDeliveryPlantFirmEmail(organisation.get().getEmail());
                    purchaseOrderDto.setDeliveryPlantFirmPhone(organisation.get().getPhone());
                    purchaseOrderDto.setDeliveryPlantFirmGST(organisation.get().getGST());
                    purchaseOrderDto.setDeliveryPlantFirmAddress(organisation.get().getAddress());
                    Optional<OurCompanyAddress> ourCompanyAddress = ourCompanyAddressRepository.findBy(organisation.get().getId());
                    if (ourCompanyAddress.isPresent()) {
                        System.out.println("check Our branch address");
                        purchaseOrderDto.setDeliveryPlantFirmPinCode(ourCompanyAddress.get().getPincode());
                    }
                }
            }

            if (purchaseOrderDto.getDeliveryplant() != null) {
                Optional<Branch> branch = branchRepository.findById(purchaseOrderDto.getDeliveryplant());
                if (branch.isPresent()) {
                    purchaseOrderDto.setDeliveryplantName(branch.get().getBranchName());
                    purchaseOrderDto.setDeliveryPlantAddress(branch.get().getAddress());
                    purchaseOrderDto.setDeliveryPlantState(branch.get().getState());
                    purchaseOrderDto.setDeliveryPlantCity(branch.get().getCity());
                    purchaseOrderDto.setDeliveryPlantEmail(branch.get().getEmail());
                    purchaseOrderDto.setDeliveryPlantPhone(branch.get().getPhone());
                    purchaseOrderDto.setDeliveryPlantPincode(branch.get().getPincode());
                }
            }


            Long proposalId = null;
            if (purchaseOrderDto.getProposalId() != null) {
                proposalId = purchaseOrderDto.getProposalId();
            }

            Optional<ProposalVendorSpec> proposal = proposalVendorSpecRepository.getProposalVendorSpecById(proposalId, purchaseOrderDto.getVendorId());
            if (proposal.isPresent()) {
                purchaseOrderDto.setQuotationNo(proposal.get().getQuotation_no());
                purchaseOrderDto.setQuotationDate(dateUtil.getDateTOStringWk(proposal.get().getQuotation_date()));
                if (proposal.get().getPacking_frieght() != null) {
                    purchaseOrderDto.setPackingFreightAmount(proposal.get().getPacking_frieght().toString());
                } else {
                    purchaseOrderDto.setPackingFreightAmount("0");
                }
            }


            for (PoItemsDto poItems : purchaseOrderDto.getPurchaseOrderItems()) {

                if (poItems.getItem_id() != null) {

                    Double receivedQty = billInwardItemReceivedRepository.sumOfPoQty(purchaseOrderDto.getId(),
                            poItems.getItem_id());
                    if (receivedQty != null) {
                        System.out.println("receivedQty = " + receivedQty);
                        Double pendingQty = poItems.getFinal_order_qty() - receivedQty;
                        System.out.println("pendingQty = " + pendingQty);
                        poItems.setPendingQty(pendingQty);
                    } else {
                        poItems.setPendingQty(poItems.getFinal_order_qty());
                    }


                    Optional<ProductMaster> product = productMasterRepository.findById(poItems.getItem_id());
                    if (product.isPresent()) {
                        poItems.setHsnCode(product.get().getHsnRefId());
                        List<Lot> lot = lotRepository.findByLot(product.get().getId());
                        poItems.setLots(lot);
                    }


                    Boolean gst = false;
                    if (proposalId != null) {
                        Optional<ProposalVendorSpec> proposalVendorSpec = proposalVendorSpecRepository
                                .getProposalVendorSpecById(proposalId, purchaseOrderDto.getVendorId(), poItems.getSpec_id());
                        if (proposalVendorSpec.isPresent()) {
                           /* poItems.setCgst(proposalVendorSpec.get().getCgst());
                            poItems.setSgst(proposalVendorSpec.get().getSgst());
                            poItems.setIgst(proposalVendorSpec.get().getIgst());*/
                            itemsDtos.add(poItems);
                        } else {
                            gst = true;
                        }
                    } else {
                        gst = true;
                    }

                    if (gst) {
                        if (product.isPresent()) {

                            GstTaxCategory gstTaxCategory = gstTaxCategoryRepository.findByHsnCode(product.get().getHsnRefId(), "P");

                            if (gstTaxCategory != null) {
                                if (purchaseOrderDto.getStateName() != null
                                        && purchaseOrderDto.getStateName().toLowerCase().contains("tamil")) {
                                    poItems.setCgst(gstTaxCategory.getCgst());
                                    poItems.setSgst(gstTaxCategory.getSgst());
                                    poItems.setIgst(0.0);
                                    itemsDtos.add(poItems);
                                } else {
                                    poItems.setIgst(gstTaxCategory.getIgst());
                                    poItems.setCgst(0.0);
                                    poItems.setSgst(0.0);
                                    itemsDtos.add(poItems);
                                }
                            } else {
                                poItems.setCgst(0.0);
                                poItems.setSgst(0.0);
                                poItems.setIgst(0.0);
                                itemsDtos.add(poItems);
                            }
                        }
                    }
                }
            }

            if (itemsDtos.size() > 0) {
                purchaseOrderDto.setPurchaseOrderItems(itemsDtos);
            }

            return new ResponseEntity<PoDto>(purchaseOrderDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<PoDto>(HttpStatus.NOT_FOUND);
        }
    }


    public PurchaseOrder gerOrderDetails(PurchaseOrder order, PurchaseOrderDto purchaseOrderDto) {

        DateUtil dateUtil = new DateUtil();

        order.setPoNumber(purchaseOrderDto.getPoNumber());
        if (purchaseOrderDto.getPo_date() != null && purchaseOrderDto.getPo_date() != "") {
            order.setPoDate(dateUtil.getStringTODateYYYYMMDD(purchaseOrderDto.getPo_date()));
        }
        order.setFirmId(purchaseOrderDto.getFirmId());
        order.setCompanyBranchId(purchaseOrderDto.getCompany_branch_id());
        order.setGstNO(purchaseOrderDto.getGst_no());
        order.setSupplierId(purchaseOrderDto.getVendorId());
        order.setSupplierName(purchaseOrderDto.getVendorName());
        order.setPoTotalAmount(purchaseOrderDto.getPo_total_amount());
        order.setCityId(purchaseOrderDto.getCity_id());
        order.setCity(purchaseOrderDto.getCity());

        order.setStateId(purchaseOrderDto.getState_id());
        order.setStateName(purchaseOrderDto.getState_name());

        order.setPoNotes(purchaseOrderDto.getPo_notes());
        order.setReferenceNo(purchaseOrderDto.getReference_no());
        order.setPoMode(purchaseOrderDto.getPo_mode());
        order.setStatus(purchaseOrderDto.getStatus());

        order.setRemarks(purchaseOrderDto.getRemarks());
        order.setApprovalStatus(purchaseOrderDto.getApproval_status());

        order.setCessType(purchaseOrderDto.getCess_type());
        order.setTypeFlag(purchaseOrderDto.getTypeflag());
        order.setProposalId(purchaseOrderDto.getProposalId());

        if (purchaseOrderDto.getDeliveryduedate() != null && purchaseOrderDto.getDeliveryduedate() != "") {
            order.setDeliveryduedate(dateUtil.getStringTODateYYYYMMDD(purchaseOrderDto.getDeliveryduedate()));
        }
        order.setDeliveryplant(purchaseOrderDto.getDeliveryplant());
        order.setPartnercontribution(purchaseOrderDto.getPartnercontribution());
        order.setProposalId(purchaseOrderDto.getProposalId());
        order.setRoundOff_amount(purchaseOrderDto.getRoundOff_amount());
        order.setRoundOffType(purchaseOrderDto.getRoundOffType());

        if (purchaseOrderDto.isAdvanceType()) {
            order.setAdvanceType(purchaseOrderDto.isAdvanceType());
        } else {
            order.setAdvanceType(false);
        }

        if (purchaseOrderDto.getDept() != null) {
            DepartmentDetails departmentDetails = new DepartmentDetails();
            departmentDetails.setId(purchaseOrderDto.getDept());
            order.setDept(departmentDetails);
        }
        if (purchaseOrderDto.getBranch() != null) {
            Branch branch = new Branch();
            branch.setId(purchaseOrderDto.getBranch());
            order.setBranch(branch);
        }

        return order;
    }


    public ResponseEntity<?> viewPurchaseOrder(String poNumber) throws UsernameNotFoundException {


        Optional<PurchaseOrder> optional = purchaseOrderRepository.findById(Long.valueOf(poNumber));
        if (optional.isPresent()) {

            List<PoItemsDto> itemsDtos = new ArrayList<PoItemsDto>();
            PoDto purchaseOrderDto = new PoDto(optional.get());
            if (purchaseOrderDto.getPoTotalAmount() != null) {
                purchaseOrderDto.setPoTotalAmount_value(decfor.format(purchaseOrderDto.getPoTotalAmount()));
            }
            Optional<Organisation> organisation = organisationRepository.findById(purchaseOrderDto.getFirmId());
            if (organisation != null) {
                purchaseOrderDto.setDeliveryPlantFirmName(organisation.get().getName());
                purchaseOrderDto.setDeliveryPlantFirmAddress(organisation.get().getAddress());
                purchaseOrderDto.setDeliveryPlantFirmCity(organisation.get().getCity());
                purchaseOrderDto.setDeliveryPlantFirmState(organisation.get().getState());
                purchaseOrderDto.setDeliveryPlantFirmEmail(organisation.get().getEmail());
                purchaseOrderDto.setDeliveryPlantFirmPhone(organisation.get().getPhone());
                purchaseOrderDto.setDeliveryPlantFirmGST(organisation.get().getGST());
                purchaseOrderDto.setDeliveryPlantFirmAddress(organisation.get().getAddress());
                Optional<OurCompanyAddress> ourCompanyAddress = ourCompanyAddressRepository.findBy(organisation.get().getId());
                if (ourCompanyAddress.isPresent()) {
                    purchaseOrderDto.setDeliveryPlantFirmPinCode(ourCompanyAddress.get().getPincode());
                }
            }

            if (purchaseOrderDto.getDeliveryplant() != null) {
                Optional<Branch> branch = branchRepository.findById(purchaseOrderDto.getDeliveryplant());
                if (branch.isPresent()) {
                    purchaseOrderDto.setDeliveryplantName(branch.get().getBranchName());
                    purchaseOrderDto.setDeliveryPlantAddress(branch.get().getAddress());
                    purchaseOrderDto.setDeliveryPlantState(branch.get().getState());
                    purchaseOrderDto.setDeliveryPlantCity(branch.get().getCity());
                    purchaseOrderDto.setDeliveryPlantEmail(branch.get().getEmail());
                    purchaseOrderDto.setDeliveryPlantPhone(branch.get().getPhone());
                    purchaseOrderDto.setDeliveryPlantPincode(branch.get().getPincode());
                }
            }

            Long proposalId = null;
            if (purchaseOrderDto.getProposalId() != null) {
                proposalId = purchaseOrderDto.getProposalId();
            }

            Optional<ProposalVendorSpec> proposal = proposalVendorSpecRepository.getProposalVendorSpecById(proposalId, purchaseOrderDto.getVendorId());
            if (proposal.isPresent()) {
                purchaseOrderDto.setQuotationNo(proposal.get().getQuotation_no());
                purchaseOrderDto.setQuotationDate(dateUtil.getDateTOStringWk(proposal.get().getQuotation_date()));
                if (proposal.get().getPacking_frieght() != null) {
                    purchaseOrderDto.setPackingFreightAmount(proposal.get().getPacking_frieght().toString());
                }
            }

            List<PurchaseOrderItems> purchaseOrderItemsList = purchaseOrderItemsRepository.findBy(purchaseOrderDto.getId());

            if (!purchaseOrderItemsList.isEmpty()) {

                for (PurchaseOrderItems poItemsValue : purchaseOrderItemsList) {

                    PoItemsDto poItems = new PoItemsDto(poItemsValue);

                    if (poItems.getItem_id() != null) {

                        Double receivedQty = billInwardItemReceivedRepository.sumOfPoQty(purchaseOrderDto.getId(), poItems.getItem_id());

                        if (receivedQty != null) {
                            System.out.println("receivedQty = " + receivedQty);
                            Double pendingQty = poItems.getFinal_order_qty() - receivedQty;
                            System.out.println("pendingQty = " + pendingQty);
                            poItems.setPendingQty(pendingQty);
                        } else {
                            poItems.setPendingQty(poItems.getFinal_order_qty());
                        }

                        Optional<ProductMaster> product = productMasterRepository.findById(poItems.getItem_id());
                        if (product.isPresent()) {
                            poItems.setHsnCode(product.get().getHsnRefId());
                            List<Lot> lot = lotRepository.findByLot(product.get().getId());
                            poItems.setLots(lot);
                        }


                        Boolean gst = false;

                        if (proposalId != null) {

                            Optional<ProposalVendorSpec> proposalVendorSpec = proposalVendorSpecRepository
                                    .getProposalVendorSpecById(proposalId, purchaseOrderDto.getVendorId(), poItems.getSpec_id());
                            if (proposalVendorSpec.isPresent()) {
                                /*poItems.setCgst(proposalVendorSpec.get().getCgst());
                                poItems.setSgst(proposalVendorSpec.get().getSgst());
                                poItems.setIgst(proposalVendorSpec.get().getIgst());*/
                                itemsDtos.add(poItems);
                            } else {
                                gst = true;
                            }
                        } else {
                            gst = true;
                        }

                        if (gst) {
                            if (product.isPresent()) {

                                GstTaxCategory gstTaxCategory = gstTaxCategoryRepository.findByHsnCode(product.get().getHsnRefId(), "P");

                                if (gstTaxCategory != null) {

                                    if (purchaseOrderDto.getStateName() != null && purchaseOrderDto.getStateName().toLowerCase().contains("tamil")) {
                                        poItems.setCgst(gstTaxCategory.getCgst());
                                        poItems.setSgst(gstTaxCategory.getSgst());
                                        poItems.setIgst(0.0);
                                        itemsDtos.add(poItems);
                                    } else {
                                        poItems.setIgst(gstTaxCategory.getIgst());
                                        poItems.setCgst(0.0);
                                        poItems.setSgst(0.0);
                                        itemsDtos.add(poItems);
                                    }
                                } else {
                                    poItems.setCgst(0.0);
                                    poItems.setSgst(0.0);
                                    poItems.setIgst(0.0);
                                    itemsDtos.add(poItems);
                                }
                            }
                        }
                    }
                }
            }

            if (itemsDtos.size() > 0) {
                purchaseOrderDto.setPurchaseOrderItems(itemsDtos);
            }

            return new ResponseEntity<PoDto>(purchaseOrderDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<PoDto>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> getPurchaseOrderFilterListByVendorId(Long vendorId) throws UsernameNotFoundException {
        List<PurchaseOrder> list = purchaseOrderRepository.getPurchaseOrderFilterListByVendorId(vendorId);
        if (!list.isEmpty()) {
            List<PoDto> result = list.stream().map(data -> {
                PoDto purchaseOrderDto = new PoDto(data);
                // updateProductsMaster(purchaseOrderDto, data);
                return purchaseOrderDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<PoDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<PoDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    private void updatePurchaseOrderProductsMasterMapping(String type, PurchaseOrder purchaseOrder, PurchaseOrderDto purchaseOrderDto) {
        if (!purchaseOrderDto.getPoOrderItems().isEmpty() && purchaseOrderDto.getPoOrderItems() != null)
            for (PurchaseOrderItemsDto purchaseOrderItemsDto : purchaseOrderDto.getPoOrderItems()) {
                PurchaseOrderItems purchaseOrderItems = new PurchaseOrderItems(purchaseOrderItemsDto);
                purchaseOrderItems.setPurchaseOrderId(purchaseOrder.getId());
                purchaseOrderItemsRepository.save(purchaseOrderItems);
                if (type != null && type.equalsIgnoreCase("UPDATE")) {
                    updateProposalVendor(purchaseOrder, purchaseOrderItems);
                }
            }
    }

    private void updatePurchaseOrderPayment(String type, PurchaseOrder purchaseOrder, PurchaseOrderDto purchaseOrderDto, User loggedUser) {

        if (purchaseOrderDto.getDeletePurchasePaymentIds() != null && !purchaseOrderDto.getDeletePurchasePaymentIds().isEmpty()) {
            List<String> paymentIds = Arrays.asList(purchaseOrderDto.getDeletePurchasePaymentIds().split(","));
            for (String paymentId : paymentIds) {
                Optional<PurchasePayment> optional = purchasePaymentRepository.findById(Long.valueOf(paymentId));
                if (optional.isPresent()) {
                    Optional<AdvancePaymentForPurchaseOrder> advancePaymentForPurchaseOrder = advancePaymentForPurchaseOrderRepo.findByPurchaseOrderAdvancePaymentByPaymentId(purchaseOrder.getId(), optional.get().getId());
                    if (advancePaymentForPurchaseOrder.isPresent()) {
                        advancePaymentForPurchaseOrderRepo.delete(advancePaymentForPurchaseOrder.get());
                        purchasePaymentRepository.delete(optional.get());
                    } else {
                        purchasePaymentRepository.delete(optional.get());
                    }
                }
            }
        }


        if (!purchaseOrderDto.getPurchasePaymentOption().isEmpty() && purchaseOrderDto.getPurchasePaymentOption() != null) {

            for (PurchasePaymentDto purchasePaymentDto : purchaseOrderDto.getPurchasePaymentOption()) {

                Optional<PurchasePayment> payment = purchasePaymentRepository.getPurchasePayment(purchaseOrder.getId(), purchasePaymentDto.getPaymentCategoryId());

                if (payment.isPresent()) {
                    PurchasePayment purchasePayment = payment.get();
                    purchasePayment.setPurchaseOrderId(purchaseOrder.getId());
                    if (purchasePaymentDto.getPaymentCategoryId() != null) {
                        purchasePayment.setPaymentCategoryId(purchasePaymentDto.getPaymentCategoryId());
                    }
                    purchasePayment.setPaymentCategoryName(purchasePaymentDto.getPaymentCategoryName());
                    purchasePayment.setPercentage(purchasePaymentDto.getPercentage());
                    purchasePayment.setPayment_by(purchasePaymentDto.getPayment_by());
                    purchasePayment.setPayment_status(purchasePaymentDto.getPayment_status());
                    purchasePayment.setStatus(purchasePaymentDto.getStatus());
                    purchasePayment.setFirmId(purchaseOrderDto.getFirmId());
                    purchasePayment.setBranch(purchaseOrder.getBranch());
                    purchasePayment.setDept(purchaseOrder.getDept());

                   /* if (purchasePaymentDto.getPercentage() != null) {
                        double amount = (purchasePaymentDto.getPercentage() / 100) * purchaseOrder.getPoTotalAmount();
                        purchasePayment.setAmount(amount);
                    }*/
                    purchasePayment.setAmount(purchasePaymentDto.getAmount());

                    if (purchasePayment.getPayment_date() != null) {
                        purchasePayment.setPayment_date(dateUtil.getStringTODateYYYYMMDD(purchasePaymentDto.getPayment_date()));
                    }
                    PurchasePayment result = purchasePaymentRepository.save(purchasePayment);
                    if (type.equalsIgnoreCase("UPDATE") && purchaseOrder.getStatus().equalsIgnoreCase("Authorized")) {
                        updateAdvancePayment(result, purchaseOrder, loggedUser);
                    }
                } else {
                    PurchasePayment purchasePayment = new PurchasePayment(purchasePaymentDto);
                    purchasePayment.setPurchaseOrderId(purchaseOrder.getId());
                    purchasePayment.setFirmId(purchaseOrderDto.getFirmId());
                    purchasePayment.setBranch(purchaseOrder.getBranch());
                    purchasePayment.setDept(purchaseOrder.getDept());
                    PurchasePayment result = purchasePaymentRepository.save(purchasePayment);
                    if (type.equalsIgnoreCase("UPDATE") && purchaseOrder.getStatus().equalsIgnoreCase("Authorized")) {
                        updateAdvancePayment(result, purchaseOrder, loggedUser);
                    }
                }
            }
        }
    }

    /*
     * public void updateAdvancePayment(PurchasePayment purchasePayment,
     * PurchaseOrder purchaseOrder, User user) {
     *
     * Optional<AdvancePaymentForPurchaseOrder> advancePaymentForPurchaseOrder =
     * advancePaymentForPurchaseOrderRepo
     * .findByPurchaseOrderAdvancePaymentByPaymentId(purchaseOrder.getId(),
     * purchasePayment.getId(), purchasePayment.paymentCategoryId); if
     * (advancePaymentForPurchaseOrder.isPresent()) {
     *
     * AdvancePaymentForPurchaseOrder advancePayment =
     * advancePaymentForPurchaseOrder.get();
     *
     * double poAmt = purchaseOrder.getPoTotalAmount();
     * System.out.println("poAmt = " + poAmt); double valuePercentage =
     * purchasePayment.getPercentage(); System.out.println("valuePercentage == " +
     * valuePercentage); double advanceAmt = (valuePercentage / 100) * poAmt;
     * System.out.println("AdvanceAmt = " + advanceAmt);
     *
     * advancePayment.setPendingAmount(purchaseOrder.getPoTotalAmount());
     * advancePayment.setAdvanceAmount(advanceAmt);
     * advancePayment.setPercentage(valuePercentage);
     * advancePayment.setCreatedTimeStamp(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
     * advancePayment.setCreateById(user); Branch branch = new Branch();
     * branch.setId(user.getBranch_id()); advancePayment.setBranch(branch);
     * advancePayment.setDept(user.getDepartment()); if (purchasePayment.getFirmId()
     * != null) { Organisation organisation = new Organisation();
     * organisation.setId(purchasePayment.getFirmId());
     * advancePayment.setFirmId(organisation); } Optional<ProposalVendorSpec>
     * proposalVendor = proposalVendorSpecRepo
     * .getProposalVendorSpecById(purchaseOrder.getProposalId(),
     * purchaseOrder.getVendor().getId());
     * advancePayment.setQuotationNumber(proposalVendor.get().getQuotation_no());
     * advancePayment.setPurchaseOrderRefId(purchasePayment.getPurchaseOrderId());
     * advancePayment.setPo_amount(purchaseOrder.getPoTotalAmount());
     * advancePayment.setVendorRefId(purchaseOrder.getVendor().getId());
     * advancePayment.setPurchasePaymentId(purchasePayment.getId());
     * advancePayment.setPaymentCategoryId(purchasePayment.getPaymentCategoryId());
     * advancePayment.setPaymentCategoryName(purchasePayment.getPaymentCategoryName(
     * )); advancePaymentForPurchaseOrderRepo.save(advancePayment);
     *
     * } else {
     *
     * AdvancePaymentForPurchaseOrder advancePayment = new
     * AdvancePaymentForPurchaseOrder();
     *
     * double poAmt = purchaseOrder.getPoTotalAmount(); double valuePercentage =
     * purchasePayment.getPercentage(); double advanceAmt = (valuePercentage / 100)
     * * poAmt;
     *
     * advancePayment.setPendingAmount(purchaseOrder.getPoTotalAmount());
     * advancePayment.setAdvanceAmount(advanceAmt);
     * advancePayment.setPercentage(purchasePayment.getPercentage());
     * advancePayment.setCreatedTimeStamp(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
     * advancePayment.setCreateById(user); advancePayment.setStatus("Pending"); if
     * (purchasePayment.getFirmId() != null) { Organisation organisation = new
     * Organisation(); organisation.setId(purchasePayment.getFirmId());
     * advancePayment.setFirmId(organisation); } Branch branch = new Branch();
     * branch.setId(user.getBranch_id()); advancePayment.setBranch(branch);
     * advancePayment.setDept(user.getDepartment()); Optional<ProposalVendorSpec>
     * proposalVendor = proposalVendorSpecRepo
     * .getProposalVendorSpecById(purchaseOrder.getProposalId(),
     * purchaseOrder.getVendor().getId());
     * advancePayment.setQuotationNumber(proposalVendor.get().getQuotation_no());
     * advancePayment.setQuotationDate(proposalVendor.get().getQuotation_date());
     * advancePayment.setPurchaseOrderRefId(purchasePayment.getPurchaseOrderId());
     * advancePayment.setPo_amount(purchaseOrder.getPoTotalAmount());
     * advancePayment.setVendorRefId(purchaseOrder.getVendor().getId());
     * advancePayment.setPurchasePaymentId(purchasePayment.getId());
     * advancePayment.setPaymentCategoryId(purchasePayment.getPaymentCategoryId());
     * advancePayment.setPaymentCategoryName(purchasePayment.getPaymentCategoryName(
     * )); advancePaymentForPurchaseOrderRepo.save(advancePayment); } }
     *
     */
    public void deleteAdvancePayment(PurchaseOrder purchaseOrder) {
        List<AdvancePaymentForPurchaseOrder> advancePaymentForPurchaseOrder = advancePaymentForPurchaseOrderRepo
                .findByPurchaseOrderAdvancePayment(purchaseOrder.getId());
        if (!advancePaymentForPurchaseOrder.isEmpty()) {
            advancePaymentForPurchaseOrderRepo.deleteAll(advancePaymentForPurchaseOrder);
        }
    }

    public void updateProductsMaster(PoDto purchaseOrderDto, PurchaseOrder purchaseOrder) {

        List<PurchaseOrderItemsDto> purchaseOrderItemsDtos = new ArrayList<>();
        if (!purchaseOrder.getPurchaseOrderItems().isEmpty()) {
            purchaseOrder.getPurchaseOrderItems().stream().forEach(data -> {
                PurchaseOrderItemsDto purchaseOrderItemsDto = new PurchaseOrderItemsDto(data);
                purchaseOrderItemsDtos.add(purchaseOrderItemsDto);
            });
        }
    }

    public ResponseEntity<?> deletePurchaseOrder(Long poId) throws UsernameNotFoundException {
        Optional<PurchaseOrder> optional = purchaseOrderRepository.findById(poId);
        if (optional.isPresent()) {
            List<PurchasePayment> paymentList = purchasePaymentRepository.getPurchasePayment(poId);
            if (!paymentList.isEmpty()) {
                purchasePaymentRepository.deleteAll(paymentList);
            }
            List<PurchaseOrderItems> purchaseOrderItems = purchaseOrderItemsRepository.findBy(poId);
            if (!purchaseOrderItems.isEmpty()) {
                purchaseOrderItemsRepository.deleteAll(purchaseOrderItems);
            }
            List<AdvancePaymentForPurchaseOrder> advancePaymentForPurchaseOrders = advancePaymentForPurchaseOrderRepo
                    .findByPurchaseOrderAdvancePayment(poId);
            if (!advancePaymentForPurchaseOrders.isEmpty()) {
                advancePaymentForPurchaseOrderRepo.deleteAll(advancePaymentForPurchaseOrders);
            }
            purchaseOrderRepository.delete(optional.get());
            return new ResponseEntity<Status>(new Status("200", "deleted successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "could not deleted"), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> all() {
        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAll();
        for (PurchaseOrder po : purchaseOrders) {
            po.setSupplierId(po.getVendor().getId());
            po.setSupplierName(po.getVendor().getName());
            purchaseOrderRepository.save(po);
        }
        return new ResponseEntity<HOStatus>(
                new HOStatus("200", "", "successfully created!!", "Purchase Order created Successfully!!!", ""),
                HttpStatus.OK);
    }

    public String getHoData(HoPurchaseOrderDto purchaseOrderDto) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> map = objectMapper.convertValue(purchaseOrderDto,
                    new TypeReference<Map<String, Object>>() {
                    });
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResponseEntity<?> getSupplierQty(String poNumber, Long proposalid, Long vendorId, Long plantid,
                                            Pageable pageable) throws UsernameNotFoundException {

        Page<SupplierQty> list = purchaseOrderRepository.getSupplierQty(poNumber, vendorId, proposalid, plantid,
                pageable);
        if (list.toList().size() > 0) {
            List<SupplierQtyDto> result = list.stream().map(data -> {
                SupplierQtyDto purchaseOrderDto = new SupplierQtyDto(data);
                if (purchaseOrderDto.getTypeFlag() != null && purchaseOrderDto.getTypeFlag().contains("Factory")) {
                    Optional<Units> units = unitsRepository.findById(Long.valueOf(purchaseOrderDto.getLot()));
                    if (units.isPresent()) {
                        purchaseOrderDto.setLot(units.get().getName());
                    }

                    Double reQty = billInwardItemReceivedRepository.sumOfPoQty(purchaseOrderDto.getPoId(),
                            purchaseOrderDto.getItemId());

                    System.out.println("reQty == " + reQty);
                    System.out.println("purchaseOrderDto.getOrderQty() == " + purchaseOrderDto.getOrderQty());

                    Double pendingQty = purchaseOrderDto.getOrderQty() - reQty;
                    purchaseOrderDto.setPendingQty(pendingQty);
                }
                return purchaseOrderDto;
            }).collect(Collectors.toList());
            PageDataDto<SupplierQtyDto> pageData = new PageDataDto<>(result, list);
            return new ResponseEntity<PageDataDto<SupplierQtyDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<SupplierQtyDto>>(new PageDataDto<>(), HttpStatus.OK);
        }

    }

    public ResponseEntity<?> getPurchaseOrderFilterListBySisterConcernVendorId(Long vendorId, Long firmId)
            throws UsernameNotFoundException {

        List<POList> list = null;

        Optional<Vendor> vendorOptional = vendorRepository.findById(vendorId);
        if (vendorOptional.isPresent()) {
            if (vendorOptional.get().getSisterConcernGroupId() != null && vendorOptional.get().getSisterConcernGroupId() != 0) {
                list = purchaseOrderRepository.purchaseOrderFilterListBySisterConcernVendorId(firmId, vendorId);
            } else {
                list = purchaseOrderRepository.purchaseOrderFilterListByVendorIdInwardPo(firmId, vendorId);
            }
        }

        if (!list.isEmpty()) {
            List<PoDto> result = list.stream().map(data -> {
                PoDto purchaseOrderDto = new PoDto(data);

                /*List<PurchaseOrderItems> purchaseOrderItems = purchaseOrderItemsRepository.findByInwardStatus(purchaseOrderDto.getId());
                if (!purchaseOrderItems.isEmpty()) {
                    purchaseOrderDto.setPurchaseOrderItems(purchaseOrderItems != null ? purchaseOrderItems
                            .stream().map(field -> new PoItemsDto(field)).collect(Collectors.toList()) : null);
                }*/
                return purchaseOrderDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<PoDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<PoDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    /* PO Status Changes */

    public ResponseEntity<?> poReject(Long poId, User user) {

        Optional<PurchaseOrder> po = purchaseOrderRepository.findById(poId);
        if (po.isPresent()) {
        	if(po.get().getStatus() !="Rejected") {
            po.get().setStatus("Rejected");
            po.get().setRejectedBy(user);
            po.get().setRejectedOn(dateUtil.getISTDate());
        }else {
    		return new ResponseEntity<>(new Status("400", "Purchase  Order is already Rejected!"), HttpStatus.BAD_REQUEST);
    	}
        
            if (po.get().getCreatedBy() != null) {
                po.get().setCreatedBy(null);
                po.get().setCreated_date(null);
            }
            if (po.get().getApprovedBy() != null) {
                po.get().setApprovedBy(null);
                po.get().setApprovedOn(null);
            }
            if (po.get().getVerifiedBy() != null) {
                po.get().setVerifiedBy(null);
                po.get().setVerifiedOn(null);
            }
            purchaseOrderRepository.save(po.get());
            return new ResponseEntity<Object>(new Status("200", "Purchase Order Rejected Sucessfully..."),
                    HttpStatus.OK);
            
        } else
            return ResponseEntity.badRequest().body("PO Id is not exist...");
    }

    public ResponseEntity<?> changePOStatus(Long poId, Optional<User> user, String status) {
        Optional<PurchaseOrder> po = purchaseOrderRepository.findById(poId);
        if (po.isPresent()) {
            if (status.equalsIgnoreCase("approved")) {
            	if(po.get().getStatus().equalsIgnoreCase("Verified")) {
            		logger.info("Purchase Order saved successfully with id: " + po.get().getStatus());
                po.get().setStatus("Approved");
                po.get().setApprovedBy(user.get());
                po.get().setApprovedOn(dateUtil.getISTDate());
            	}else {
            		return new ResponseEntity<>(new Status("400", "Purchase  Order is already Approved!"), HttpStatus.BAD_REQUEST);
            	}  
            }
            if (status.equalsIgnoreCase("verified")) {
            	if(po.get().getStatus().equalsIgnoreCase("Pending")) {
                po.get().setStatus("Verified");
                po.get().setVerifiedBy(user.get());
                po.get().setVerifiedOn(dateUtil.getISTDate());
            	}else {
            		return new ResponseEntity<>(new Status("400", "Purchase  Order is already Verified!"), HttpStatus.BAD_REQUEST);
            	} 
            	
            }
            purchaseOrderRepository.save(po.get());
            return new ResponseEntity<Object>(new Status("200", "PO Status Updated Sucessfully ..."), HttpStatus.OK);
        } else
            return ResponseEntity.badRequest().body("PO Id is not exist...");
    }

    public ResponseEntity<?> POStatusAuthorized(PurchaseOrderDto dto, Optional<User> user) {
        Optional<PurchaseOrder> po = purchaseOrderRepository.findById(dto.getId());

        if (po.isPresent()) {
        		if(po.get().getStatus().equalsIgnoreCase("Approved")) {
            for (PurchasePaymentDto purchasePaymentDto : dto.getPurchasePaymentOption()) {

                Optional<PurchasePayment> payment = purchasePaymentRepository.getPurchasePayment(dto.getId(), purchasePaymentDto.getPaymentCategoryId());

                System.out.println(payment.get().getFirmId());
                if (dto.isAdvanceType()) {
                    saveAdvancePayment(payment.get(), po.get(), user.get());
                }
            }
            po.get().setStatus("Authorized");
            po.get().setAuthorizedBy(user.get());
            po.get().setAuthorizedOn(dateUtil.getISTDate());
            purchaseOrderRepository.save(po.get());
            return new ResponseEntity<Object>(new Status("200", "PO Authorized Sucessfully..."), HttpStatus.OK);
        }else {
    		return new ResponseEntity<>(new Status("400", "Purchase  Order is already Authorized!"), HttpStatus.BAD_REQUEST);
    	} 
        }
        return ResponseEntity.badRequest().body("PO Id is not exist...");
    }

    public void saveAdvancePayment(PurchasePayment purchasePayment, PurchaseOrder purchaseOrder, User user) {

        AdvancePaymentForPurchaseOrder advancePayment = null;
        Optional<AdvancePaymentForPurchaseOrder> advancePaymentForPurchaseOrder = advancePaymentForPurchaseOrderRepo.findByPurchaseOrderAdvancePaymentByPaymentId(purchaseOrder.getId(), purchasePayment.getId(), purchasePayment.getPaymentCategoryId());
        if (advancePaymentForPurchaseOrder.isPresent()) {
            advancePayment = advancePaymentForPurchaseOrder.get();
        } else {
            advancePayment = new AdvancePaymentForPurchaseOrder();
        }

        /*double poAmt = purchaseOrder.getPoTotalAmount();
        double valuePercentage = purchasePayment.getPercentage();
        double advanceAmt = (valuePercentage / 100) * poAmt;*/

        advancePayment.setPendingAmount(purchaseOrder.getPoTotalAmount());
        advancePayment.setAdvanceAmount(purchasePayment.getAmount());
        advancePayment.setPercentage(purchasePayment.getPercentage());
        advancePayment.setCreatedTimeStamp(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        advancePayment.setCreateById(user);
        advancePayment.setCreated_date(dateUtil.getISTDate());
        advancePayment.setStatus("Pending");
        advancePayment.setTrnno(getTrnNo());
        if (purchasePayment.getFirmId() != null) {
            Organisation organisation = new Organisation();
            organisation.setId(purchasePayment.getFirmId());
            advancePayment.setFirmId(organisation);
        }
        advancePayment.setBranch(purchasePayment.getBranch());
        advancePayment.setDept(purchasePayment.getDept());
        Optional<ProposalVendorSpec> proposalVendor = proposalVendorSpecRepo.getProposalVendorSpecById(purchaseOrder.getProposalId(), purchaseOrder.getVendor().getId());
        advancePayment.setQuotationNumber(proposalVendor.get().getQuotation_no());
        advancePayment.setQuotationDate(proposalVendor.get().getQuotation_date());
        advancePayment.setPurchaseOrderRefId(purchasePayment.getPurchaseOrderId());
        advancePayment.setPo_amount(purchaseOrder.getPoTotalAmount());
        advancePayment.setVendorRefId(purchaseOrder.getVendor().getId());
        advancePayment.setVendorAddress(purchaseOrder.getVendor().getAddress());
        advancePayment.setPurchasePaymentId(purchasePayment.getId());
        advancePayment.setPaymentCategoryId(purchasePayment.getPaymentCategoryId());
        advancePayment.setPaymentCategoryName(purchasePayment.getPaymentCategoryName());
        advancePaymentForPurchaseOrderRepo.save(advancePayment);
    }


    public void updateAdvancePayment(PurchasePayment purchasePayment, PurchaseOrder purchaseOrder, User user) {


        Double paidAmount = advancePaymentForPurchaseOrderRepo.sumOfAdvanceAmount(purchaseOrder.getId());


        AdvancePaymentForPurchaseOrder advancePayment = null;
        Optional<AdvancePaymentForPurchaseOrder> advancePaymentForPurchaseOrder = advancePaymentForPurchaseOrderRepo.findByPurchaseOrderAdvancePaymentByPaymentId(purchaseOrder.getId(), purchasePayment.getId());
        if (advancePaymentForPurchaseOrder.isPresent()) {

            advancePayment = advancePaymentForPurchaseOrder.get();

            if (advancePayment.getStatus().equalsIgnoreCase("Pending")) {

               /* double poAmt = purchaseOrder.getPoTotalAmount();
                double valuePercentage = purchasePayment.getPercentage();
                double advanceAmt = (valuePercentage / 100) * poAmt;*/

                double pendingAmount = purchaseOrder.getPoTotalAmount() - paidAmount;

                advancePayment.setPendingAmount(pendingAmount);
                //advancePayment.setAdvanceAmount(advanceAmt);
                advancePayment.setAdvanceAmount(purchasePayment.getAmount());
                advancePayment.setPercentage(purchasePayment.getPercentage());
                advancePayment.setCreatedTimeStamp(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                advancePayment.setCreateById(user);
                advancePayment.setCreated_date(dateUtil.getISTDate());
                advancePayment.setStatus("Pending");
                advancePayment.setTrnno(getTrnNo());
                if (purchasePayment.getFirmId() != null) {
                    Organisation organisation = new Organisation();
                    organisation.setId(purchasePayment.getFirmId());
                    advancePayment.setFirmId(organisation);
                }
                advancePayment.setBranch(purchasePayment.getBranch());
                advancePayment.setDept(purchasePayment.getDept());
                Optional<ProposalVendorSpec> proposalVendor = proposalVendorSpecRepo.getProposalVendorSpecById(purchaseOrder.getProposalId(), purchaseOrder.getVendor().getId());
                advancePayment.setQuotationNumber(proposalVendor.get().getQuotation_no());
                advancePayment.setQuotationDate(proposalVendor.get().getQuotation_date());
                advancePayment.setPurchaseOrderRefId(purchasePayment.getPurchaseOrderId());
                advancePayment.setPo_amount(purchaseOrder.getPoTotalAmount());
                advancePayment.setVendorRefId(purchaseOrder.getVendor().getId());
                advancePayment.setVendorAddress(purchaseOrder.getVendor().getAddress());
                advancePayment.setPurchasePaymentId(purchasePayment.getId());
                advancePayment.setPaymentCategoryId(purchasePayment.getPaymentCategoryId());
                advancePayment.setPaymentCategoryName(purchasePayment.getPaymentCategoryName());
                advancePaymentForPurchaseOrderRepo.saveAndFlush(advancePayment);
            }

        } else {

            advancePayment = new AdvancePaymentForPurchaseOrder();

            double pendingAmount = purchaseOrder.getPoTotalAmount() - paidAmount;

            //double poAmt = purchaseOrder.getPoTotalAmount();
            //double valuePercentage = purchasePayment.getPercentage();
            //double advanceAmt = (valuePercentage / 100) * poAmt;

            advancePayment.setPendingAmount(pendingAmount);
            advancePayment.setAdvanceAmount(purchasePayment.getAmount());
            advancePayment.setPercentage(purchasePayment.getPercentage());
            advancePayment.setCreatedTimeStamp(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            advancePayment.setCreateById(user);
            advancePayment.setCreated_date(dateUtil.getISTDate());
            advancePayment.setStatus("Pending");
            advancePayment.setTrnno(getTrnNo());
            if (purchasePayment.getFirmId() != null) {
                Organisation organisation = new Organisation();
                organisation.setId(purchasePayment.getFirmId());
                advancePayment.setFirmId(organisation);
            }
            advancePayment.setBranch(purchasePayment.getBranch());
            advancePayment.setDept(purchasePayment.getDept());
            Optional<ProposalVendorSpec> proposalVendor = proposalVendorSpecRepo.getProposalVendorSpecById(purchaseOrder.getProposalId(), purchaseOrder.getVendor().getId());
            advancePayment.setQuotationNumber(proposalVendor.get().getQuotation_no());
            advancePayment.setQuotationDate(proposalVendor.get().getQuotation_date());
            advancePayment.setPurchaseOrderRefId(purchasePayment.getPurchaseOrderId());
            advancePayment.setPo_amount(purchaseOrder.getPoTotalAmount());
            advancePayment.setVendorRefId(purchaseOrder.getVendor().getId());
            advancePayment.setVendorAddress(purchaseOrder.getVendor().getAddress());
            advancePayment.setPurchasePaymentId(purchasePayment.getId());
            advancePayment.setPaymentCategoryId(purchasePayment.getPaymentCategoryId());
            advancePayment.setPaymentCategoryName(purchasePayment.getPaymentCategoryName());
            advancePaymentForPurchaseOrderRepo.saveAndFlush(advancePayment);
        }


    }


    private String getTrnNo() {
        String trnNo = null;
        Optional<TRNIndex> trnIndex = trnIndexRepo.findByPrefix("Advance");
        if (trnIndex.isPresent()) {
            TRNIndex trn = trnIndex.get();
            Long trnlast = trn.getLastno();
            trnNo = trn.getPrefix() + "-" + trn.getLastno();
            Long fstrnlast = trnlast + 1;
            trn.setLastno(fstrnlast);
            trnIndexRepo.saveAndFlush(trn);
            return trnNo;
        } else {
            return trnNo;
        }
    }

    public ResponseEntity<?> inactivePurchaseOrderItem(Long poId, String poItemId, boolean active) {
        Optional<PurchaseOrder> po = purchaseOrderRepository.findById(poId);
        if (po.isPresent()) {
            List<String> poItemIdList = null;
            if (poItemId.contains(",")) {
                poItemIdList = new ArrayList<>(Arrays.asList(poItemId.split("\\,")));
                if (!poItemIdList.isEmpty()) {
                    for (String poValue : poItemIdList) {
                        Optional<PurchaseOrderItems> optional = purchaseOrderItemsRepository.findById(poId, Long.valueOf(poValue));
                        if (optional.isPresent()) {
                            PurchaseOrderItems purchaseOrderItems = optional.get();
                            purchaseOrderItems.setActive(active);
                            purchaseOrderItemsRepository.save(purchaseOrderItems);
                        }
                    }
                }
            } else {
                Optional<PurchaseOrderItems> optional = purchaseOrderItemsRepository.findById(poId, Long.valueOf(poItemId));
                if (optional.isPresent()) {
                    PurchaseOrderItems purchaseOrderItems = optional.get();
                    purchaseOrderItems.setActive(active);
                    purchaseOrderItemsRepository.save(purchaseOrderItems);
                }
            }
            if (active) {
                return new ResponseEntity<Object>(new Status("200", " PurchaseOrder Items Activated Successfully!!!!!"), HttpStatus.OK);
            } else {
                return new ResponseEntity<Object>(new Status("200", " PurchaseOrder Items InActivated Successfully!!!!!"), HttpStatus.OK);
            }
        } else
            return ResponseEntity.badRequest().body("PO Id is not exist...");
    }


    public void updateGst() {
        System.out.println("Updating GST...");
        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAll();
        if (!purchaseOrders.isEmpty()) {
            for (PurchaseOrder purchaseOrder : purchaseOrders) {

                List<PurchaseOrderItems> purchaseOrderItems = purchaseOrderItemsRepository.findBy(purchaseOrder.getId());

                if (!purchaseOrderItems.isEmpty()) {
                    for (PurchaseOrderItems purchaseOrderItem : purchaseOrderItems) {
                        System.out.println("Purchase Order Id: " + purchaseOrder.getId());
                        if (purchaseOrder.getProposalId() != null && purchaseOrder.getVendor() != null && purchaseOrderItem.getSpec_id() != null) {
                            Optional<ProposalVendorSpec> proposalVendorSpec = proposalVendorSpecRepository
                                    .getProposalVendorSpecById(purchaseOrder.getProposalId(), purchaseOrder.getVendor().getId(), purchaseOrderItem.getSpec_id());
                            System.out.println("Proposal Id: " + purchaseOrder.getProposalId() + ", Vendor Id: " + purchaseOrder.getVendor().getId() + ", Spec Id: ");
                            if (proposalVendorSpec.isPresent()) {
                                System.out.println("GST Percentage: proposalVendorSpec");
                                purchaseOrderItem.setSgstPercentage(proposalVendorSpec.get().getCgst());
                                purchaseOrderItem.setCgstPercentage(proposalVendorSpec.get().getSgst());
                                purchaseOrderItem.setIgstPercentage(proposalVendorSpec.get().getIgst());
                                purchaseOrderItemsRepository.save(purchaseOrderItem);
                                System.out.println("Purchase Order Item Id: " + purchaseOrderItem.getId() + ", Updated GST Successfully.");
                            }
                        } else {
                            System.out.println("Proposal Id or Vendor or Spec Id is not found for Purchase Order Id: " + purchaseOrder.getId());
                        }
                    }
                } else {
                    System.out.println("No Purchase Order Items Found for Purchase Order Id: " + purchaseOrder.getId());
                }
            }
        } else {
            System.out.println("No Purchase Order Found...");
        }
    }


    public void updateProposalVendor(PurchaseOrder purchaseOrder, PurchaseOrderItems items) {


        List<ProposalSpec> proposalspecList = proposalSpecRepository.getProposalSpecListWithVendor(purchaseOrder.getProposalId(), purchaseOrder.getVendor().getId(), items.getSpec_id(), items.getSpec_type());
        if (!proposalspecList.isEmpty()) {
            for (ProposalSpec proposalSpec : proposalspecList) {

                double fixedQty = 0.0;
                String poValidityRequest = null;
                Optional<ProposalVendorSpec> proposalVendorSpec = proposalVendorSpecRepository.getProposalVendorSpecBySpecId(proposalSpec.getProposal_ref_id(), proposalSpec.getId(), purchaseOrder.getVendor().getId());
                if (proposalVendorSpec.isPresent()) {
                    ProposalVendorSpec rs = proposalVendorSpec.get();
                    if (rs.getNet_approved_qty() != null) {
                        fixedQty = rs.getNet_approved_qty();
                    }
                    if (rs.getPo_validity_request() != null) {
                        poValidityRequest = rs.getPo_validity_request();
                    }
                }

                if (poValidityRequest != null && !poValidityRequest.isEmpty()) {

                    Double poQty = purchaseOrderRepository.findByQty(purchaseOrder.getProposalId(), purchaseOrder.getVendor().getId(), proposalSpec.getSpecid());

                    if (  fixedQty > 0 && (poQty > fixedQty) ) {
                        System.out.println("PO Quantity is less than Fixed Quantity for vendor: " + purchaseOrder.getVendor().getId());
                        if (proposalVendorSpec.isPresent()) {
                            ProposalVendorSpec rs = proposalVendorSpec.get();
                            rs.setStatus("Closed");
                            proposalVendorSpecRepository.save(rs);
                            System.out.println("ProposalVendorSpec status set to Closed for vendor: " + purchaseOrder.getVendor().getId());
                        } else {
                            System.out.println("ProposalVendorSpec not found for proposal id: " + purchaseOrder.getProposalId() + " and vendor id: " + purchaseOrder.getVendor().getId());
                        }
                    }
                }
            }
        }
    }



}
