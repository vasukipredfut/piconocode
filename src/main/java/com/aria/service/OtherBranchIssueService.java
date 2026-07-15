package com.aria.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import com.aria.utils.GDCUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.aria.utils.DateUtil;
import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;

import jakarta.persistence.Tuple;

@Service
public class OtherBranchIssueService {

    @Autowired
    OtherBranchIssueRepository otherBranchIssueRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    GDCUtil gdcUtil;

//    @Autowired
//    IntraBranchReceiptRepository intraBranchReceiptRepository;

    @Autowired
    IntraBranchRecRepository intraBranchRecRepository;

    @Autowired
    IntraBranchReceiptMappingRepository intraBranchReceiptMappingRepository;

    @Autowired
    HupMappingRepository hupMappingRepository;

    @Autowired
    MaterialIssueRepository materialIssueRepository;

    @Autowired
    MaterialRequestRepository materialRequestRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    ProductMasterRepository productMasterRepo;

    @Autowired
    IssueItemsRepository issueItemsRepository;

    @Autowired
    BinStockRepository binStockRepository;

    @Autowired
    MaterialIssueService materialIssueService;

    @Autowired
    MaterialFlowGDCRepository materialFlowGDCRepository;

    @Autowired
    MaterialFlowGDCItemRepository materialFlowGDCItemRepository;

    @Autowired
    EmastDailyRepository emastDailyRepository;

    @Autowired
    OtherBranchIssueItemsRepository otherBranchIssueItemsRepository;

    @Autowired
    MaterialReturnRepository materialReturnRepository;
    @Autowired
    MaterialReturnItemsRepository materialReturnItemsRepository;


    public ResponseEntity<?> updateOtherBranchIssue(OtherBranchIssueDto otherBranchIssueDto, User loggedUser) {

        Optional<OtherBranchIssue> optional = otherBranchIssueRepository.findById(otherBranchIssueDto.getId());
        if (optional.isPresent()) {
            OtherBranchIssue branchReceipt = optional.get();
            branchReceipt.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            branchReceipt.setUpdatedBy(loggedUser.getId());
            branchReceipt.setStatus(otherBranchIssueDto.getStatus());
            branchReceipt.setDelivery_user_id(otherBranchIssueDto.getDeliveryByStaffId());
            branchReceipt.setDispatchOn(dateUtil.getStringTODateF1(otherBranchIssueDto.getDispatchOn()));
            branchReceipt.setTransportMode(otherBranchIssueDto.getTransportMode());
            branchReceipt.setTransportVehicleNo(otherBranchIssueDto.getTransportVehicleNo());
            branchReceipt.setTransportVehicleType(otherBranchIssueDto.getTransportVehicleType());
            branchReceipt.setIssueMode(otherBranchIssueDto.getIssueMode());
            //branchReceipt.setMaterialReturn(otherBranchIssueDto.getMaterialReturn());
            branchReceipt.setGenerateEWayBill(otherBranchIssueDto.getGenerateEWayBill());
            /*ObjectMapper objectMapper = new ObjectMapper();
            try {
                branchReceipt.setOtherBranchIssueValue(objectMapper.writeValueAsString(otherBranchIssueDto.getOtherBranchIssueValue()));
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            OtherBranchIssue result = otherBranchIssueRepository.save(branchReceipt);

            for (OtherBranchIssueItemsDTO issueItemsDTO : otherBranchIssueDto.getOtherBranchIssueValue()) {
                OtherBranchIssueItems items = new OtherBranchIssueItems(issueItemsDTO);
                items.setIssueItemId(result.getId());
                otherBranchIssueItemsRepository.save(items);
            }

            List<Long> ids = new ArrayList<>();
            ids.add(result.getId());
            updateIntraBranchReceipt(ids);
            return new ResponseEntity<Status>(new Status("200", "other branch issue updated successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("404", "not updated!!!"), HttpStatus.NOT_FOUND);
        }

    }


    public ResponseEntity<?> ListupdateOtherBranchIssue(List<OtherBranchIssueDto> otherBranchIssueList,
                                                        User loggedUser) {

        boolean partial = false;
        List<Long> ids = new ArrayList<>();
        List<Long> returnIds = new ArrayList<>();
        String gdcNo = gdcUtil.getGDCNo(loggedUser.getBranch_id(), loggedUser.getOrg_id());
        System.out.println("GDC-NO ==== " + gdcNo);
        for (OtherBranchIssueDto otherBranchIssueDto : otherBranchIssueList) {

            Optional<OtherBranchIssue> optional = otherBranchIssueRepository.findById(otherBranchIssueDto.getId());
            if (optional.isPresent()) {
                OtherBranchIssue branchReceipt = optional.get();
                if ("Completed".equalsIgnoreCase(branchReceipt.getStatus())) {
                    return new ResponseEntity<>(
                        new Status("400", "Already Completed! Cannot process again."),
                        HttpStatus.BAD_REQUEST
                    );
                }
                branchReceipt.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                branchReceipt.setUpdatedBy(loggedUser.getId());
                branchReceipt.setStatus(otherBranchIssueDto.getStatus());
                branchReceipt.setDelivery_user_id(otherBranchIssueDto.getDeliveryByStaffId());
                branchReceipt.setDispatchOn(dateUtil.getFilterDate(otherBranchIssueDto.getDispatchOn()));
                branchReceipt.setTransportMode(otherBranchIssueDto.getTransportMode());
                branchReceipt.setTransportVehicleNo(otherBranchIssueDto.getTransportVehicleNo());
                branchReceipt.setTransportVehicleType(otherBranchIssueDto.getTransportVehicleType());
                branchReceipt.setIssueMode(otherBranchIssueDto.getIssueMode());
                //branchReceipt.setMaterialReturn(otherBranchIssueDto.getMaterialReturn());
                branchReceipt.setGenerateEWayBill(otherBranchIssueDto.getGenerateEWayBill());
                branchReceipt.setGDCNo(gdcNo);
                OtherBranchIssue result = otherBranchIssueRepository.save(branchReceipt);
                /* ObjectMapper objectMapper = new ObjectMapper();
                try {
                    branchReceipt.setOtherBranchIssueValue(objectMapper.writeValueAsString(otherBranchIssueDto.getOtherBranchIssueValue()));
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                for (OtherBranchIssueItemsDTO issueItemsDTO : otherBranchIssueDto.getOtherBranchIssueValue()) {
                    if (issueItemsDTO.getMaterialReturn() != null && issueItemsDTO.getMaterialReturn().equalsIgnoreCase("YES")) {
                        returnIds.add(result.getId());
                    }
                    OtherBranchIssueItems items = new OtherBranchIssueItems(issueItemsDTO);
                    items.setIssueItemId(result.getId());
                    OtherBranchIssueItems rs = otherBranchIssueItemsRepository.save(items);
                }
                updateGDC(result);
                ids.add(result.getId());
            } else {
                partial = true;
            }
        }
        updateIntraBranchReceipt(ids);
        if (returnIds.size() > 0) {
            updateMaterialReturn(returnIds, loggedUser);
        }
        if (partial) {
            return new ResponseEntity<Status>(new Status("202", "other branch issue Paritally Updated!!!"),
                    HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<Status>(new Status("200", "other branch issue updated successfully!!!"),
                    HttpStatus.OK);
        }

    }

    public ResponseEntity<?> listAll(User loggedUser, Pageable pageable) throws UsernameNotFoundException {
        Page<OtherBranchIssue> list = otherBranchIssueRepository.findAll(pageable);
        if (!list.toList().isEmpty()) {
            List<OtherBranchIssueDto> result = list.toList().stream().map(data -> {
                OtherBranchIssueDto materialWithInBranchReceiptDto = new OtherBranchIssueDto(data);
                if (data.getDispatchOn() != null) {
                    materialWithInBranchReceiptDto.setDispatchOn(dateUtil.getStringTODateF1(data.getDispatchOn()));
                }
                return materialWithInBranchReceiptDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<OtherBranchIssueDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<OtherBranchIssueDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> filterListAll(Long firmId, Long userBranchId, String mrnNo, String requestBranchName, String issueDeptName,
                                           String fromDate, String toDate, String status, Pageable pageable) throws UsernameNotFoundException {

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

        Page<OtherBranchIssueList> list = otherBranchIssueRepository.otherBranchIssueList(firmId, userBranchId, mrnNo, requestBranchName, issueDeptName, startDate, endDate, status, pageable);
        if (!list.toList().isEmpty()) {
            List<OtherBranchIssueListDto> result = list.toList().stream().map(data -> new OtherBranchIssueListDto(data)).collect(Collectors.toList());
            PageDataDto<OtherBranchIssueListDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<OtherBranchIssueListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<OtherBranchIssueListDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }


    public ResponseEntity<OtherBranchIssueDto> getOtherBranchIssue(String id) throws UsernameNotFoundException {
        Optional<OtherBranchIssue> optional = otherBranchIssueRepository.findById(Long.parseLong(id));
        OtherBranchIssueDto otherBranchIssueDto = new OtherBranchIssueDto(optional.get());
        if (optional.get().getDispatchOn() != null) {
            otherBranchIssueDto.setDispatchOn(dateUtil.getStringTODateF1(optional.get().getDispatchOn()));
            if (otherBranchIssueDto.getIssueMode().equalsIgnoreCase("c")) {
                Optional<EmastDaily> user = emastDailyRepository.findById(otherBranchIssueDto.getDeliveryByStaffId());
                if (user.isPresent()) {
                    otherBranchIssueDto.setDeliveryByStaffId(user.get().getId());
                    otherBranchIssueDto.setDeliveryByStaff(user.get().getName());
                }
            } else {
                Optional<User> user = userRepository.findById(otherBranchIssueDto.getDeliveryByStaffId());
                if (user.isPresent()) {
                    otherBranchIssueDto.setDeliveryByStaffId(user.get().getId());
                    otherBranchIssueDto.setDeliveryByStaff(user.get().getEmployeename());
                }
            }

            Optional<MaterialIssue> request = materialIssueRepository.findById(otherBranchIssueDto.getIssueId());
            if (request.isPresent()) {
                otherBranchIssueDto.setIssuedBy(request.get().getIssueBy().getEmployeename());
                Optional<MaterialRequest> optionalMaterialRequest = materialRequestRepository.findById(optional.get().getRequestId());
                Optional<User> updatedByUser = userRepository.findById(optional.get().getUpdatedBy());
                updatedByUser.ifPresent(user -> otherBranchIssueDto.setUpdatedByUser(user.getEmployeename()));
                if (optionalMaterialRequest.isPresent())
                    otherBranchIssueDto.setOtherBranchIssueBriefValue(materialIssueService
                            .getIssueItemBrief(optionalMaterialRequest.get(), request.get().getId()));

            }
        }

        return new ResponseEntity<OtherBranchIssueDto>(otherBranchIssueDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteOtherBranchIssue(String id) throws UsernameNotFoundException {
        try {
            Optional<OtherBranchIssue> optional = otherBranchIssueRepository
                    .findById(Long.parseLong(id));
            if (optional.isPresent()) {
                OtherBranchIssue otherBranchIssue = optional.get();
                otherBranchIssueRepository.delete(otherBranchIssue);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public ResponseEntity<?> verifyAndAcceptAndRejectOtherBranchIssue(Long id, String status) throws UsernameNotFoundException {
        try {
            Optional<OtherBranchIssue> optional = otherBranchIssueRepository.findById(id);
            if (optional.isPresent()) {
                OtherBranchIssue otherBranchIssue = optional.get();
                otherBranchIssue.setStatus(status);
                otherBranchIssueRepository.save(otherBranchIssue);
                if (status.equalsIgnoreCase("REJECTED")) {
                    Optional<MaterialIssue> optionalMaterialIssue = materialIssueRepository.findById(otherBranchIssue.getIssueId());
                    if (optionalMaterialIssue.isPresent()) {
                        MaterialIssue materialIssue = optionalMaterialIssue.get();
                        materialIssue.setStatus("REJECTED");
                        materialIssueRepository.save(materialIssue);
                        Optional<MaterialRequest> materialRequestOptional = materialRequestRepository.findById(materialIssue.getRequestId());
                        materialRequestOptional.ifPresent(materialRequest -> {
                            materialRequest.setStatus("REJECTED");
                            materialRequestRepository.save(materialRequest);
                        });
                    }
                }
            }
            return new ResponseEntity<Status>(new Status("200", status + " SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not found Record"), HttpStatus.NOT_MODIFIED);
        }
    }


    public void updateIntraBranchReceipt(List<Long> ids) {


        Optional<OtherBranchIssue> optional = otherBranchIssueRepository.findById(ids.get(0));

        if (optional.isPresent()) {

            OtherBranchIssue otherBranchIssue = optional.get();

            IntraBranchReceiptEntity intraBranchReceipt = new IntraBranchReceiptEntity();
            intraBranchReceipt.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            intraBranchReceipt.setCreatedBy(otherBranchIssue.getUpdatedBy());
            intraBranchReceipt.setIssueBranch(otherBranchIssue.getIssueBranch());
            intraBranchReceipt.setRequestBranchId(otherBranchIssue.getRequestBranch().getId());
            if (otherBranchIssue.getOrganisation() != null) {
                intraBranchReceipt.setRequestFirmId(otherBranchIssue.getOrganisation().getId());
            }
            intraBranchReceipt.setRequestDeptId(otherBranchIssue.getRequestDepartment().getId());
            intraBranchReceipt.setDocketNo(intraBranchReceipt.getHupId());
            intraBranchReceipt.setDispatchOn(otherBranchIssue.getDispatchOn());
            intraBranchReceipt.setDispatchBy(otherBranchIssue.getUpdatedBy());
            intraBranchReceipt.setDeliveryByStaff(otherBranchIssue.getDelivery_user_id());
            intraBranchReceipt.setTrnNo(otherBranchIssue.getTrnNo());
            intraBranchReceipt.setStatus("Pending");
            intraBranchReceipt = intraBranchRecRepository.save(intraBranchReceipt);
            for (Long id : ids) {
                IntraBranchReceiptMapping intraBranchReceiptMapping = new IntraBranchReceiptMapping();
                intraBranchReceiptMapping.setIntraBranchReceiptId(intraBranchReceipt.getId());
                intraBranchReceiptMapping.setOtherBranchIssueId(id);
                intraBranchReceiptMappingRepository.save(intraBranchReceiptMapping);
            }
        }
    }


    /*
     * private HupMapping findHupId(Long branchId) { HupMapping hupMapping = new
     * HupMapping(); hupMapping.setBranchId(branchId);
     * hupMapping.setDate(dateUtil.getISTDate()); return
     * hupMappingRepository.save(hupMapping); }
     *
     * private void updateHup() { List<HupMapping> hupMappings =
     * hupMappingRepository.findAll(); if (!hupMappings.isEmpty()) {
     * hupMappingRepository.deleteAll(hupMappings); } }
     */

    public ResponseEntity<?> getReceiptData(Long otherBranchIssueId) throws UsernameNotFoundException {

        Optional<OtherBranchIssue> optional = otherBranchIssueRepository.findById(otherBranchIssueId);

        if (optional.isPresent()) {

            OtherBranchIssue otherBranchIssue = optional.get();
            Optional<MaterialRequest> optionalMaterialRequest = materialRequestRepository
                    .findById(otherBranchIssue.getRequestId());
            Optional<Branch> branch = branchRepository.findById(optionalMaterialRequest.get().getRequestBy().getBranch_id());

            MaterialReciptDto materialRequestDto = new MaterialReciptDto(otherBranchIssue, optionalMaterialRequest.get(), branch.get(), branchRepository, productMasterRepo, issueItemsRepository, binStockRepository, otherBranchIssue.getIssueId());
            if (otherBranchIssue.getGDCNo() != null) {
                materialRequestDto.setGdcNo(otherBranchIssue.getGDCNo());
            }

            materialRequestDto.setVehicleNumber(otherBranchIssue.getTransportVehicleNo());

            return new ResponseEntity<MaterialReciptDto>(materialRequestDto, HttpStatus.OK);
        }
        return new ResponseEntity<Status>(new Status("304", "Could not found Record"), HttpStatus.NOT_MODIFIED);

    }

    public void updateGDC(OtherBranchIssue otherBranchIssue) {

        OtherBranchIssueDto otherBranchIssueDto = new OtherBranchIssueDto(otherBranchIssue);

        MaterialGDC materialGDC = new MaterialGDC(otherBranchIssue);
        materialGDC.setGdc_Date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        materialGDC.setActive_Flag(0);
        MaterialGDC rs = materialFlowGDCRepository.save(materialGDC);

        if (!otherBranchIssueDto.getOtherBranchIssueValue().isEmpty()) {
            for (OtherBranchIssueItemsDTO items : otherBranchIssueDto.getOtherBranchIssueValue()) {
                MaterialFlowGDCItem materialFlowGDCItem = new MaterialFlowGDCItem(items);
                materialFlowGDCItem.setMaterial_Flow_Gdc_Number(rs.getGdc_Number());
                materialFlowGDCItemRepository.save(materialFlowGDCItem);
            }
        }

    }


    public ResponseEntity<?> amendmentGDC(OtherBranchIssueDto otherBranchIssueDto) throws UsernameNotFoundException {

        List<OtherBranchIssue> list = otherBranchIssueRepository.findBy(otherBranchIssueDto.getGdcNo(), otherBranchIssueDto.getIssueFromBranchId(), otherBranchIssueDto.getRequestFromBranchId());
        if (!list.isEmpty()) {
            for (OtherBranchIssue otherBranchIssue : list) {
                otherBranchIssue.setTransportVehicleNo(otherBranchIssueDto.getTransportVehicleNo());
                otherBranchIssue.setTransportVehicleType(otherBranchIssueDto.getTransportVehicleType());
                otherBranchIssue.setTransportMode(otherBranchIssueDto.getTransportMode());
                otherBranchIssueRepository.save(otherBranchIssue);
            }
            return new ResponseEntity<Status>(new Status("200", "Updated successfully!!!"), HttpStatus.OK);
        }
        return new ResponseEntity<Status>(new Status("304", "Could not found Record"), HttpStatus.NOT_MODIFIED);

    }


    public void updateMaterialReturn(List<Long> returnIds, User loggedUser) {

        List<OtherBranchIssue> otherBranchIssues = otherBranchIssueRepository.findBy(returnIds);
        if (!otherBranchIssues.isEmpty()) {
            for (OtherBranchIssue otherBranchIssue : otherBranchIssues) {

                Optional<MaterialIssue> optionalMaterialIssue = materialIssueRepository.findById(otherBranchIssue.getIssueId());
                if (optionalMaterialIssue.isPresent()) {

                    MaterialReturn materialReturn = new MaterialReturn(optionalMaterialIssue.get());
                    materialReturn.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    materialReturn.setCreatedBy(loggedUser.getId());
                    materialReturn.setDispatchDate(otherBranchIssue.getDispatchOn());
                    materialReturn.setType("OtherBranchIssue");
                    //materialReturn.setStatus("Pending");
                    MaterialReturn result = materialReturnRepository.save(materialReturn);
                    List<Tuple> otherBranchIssueItems = otherBranchIssueItemsRepository.findBy(otherBranchIssue.getId());
                    for (Tuple itemsDto : otherBranchIssueItems) {
                        MaterialReturnItems materialReturnItems = new MaterialReturnItems(itemsDto);
                        materialReturnItems.setReturnId(result.getId());
                        materialReturnItemsRepository.save(materialReturnItems);
                    }
                }
            }
        }

    }


    public ResponseEntity<?> rejectOtherBranchIssue(Long id, String status) throws UsernameNotFoundException {
        try {

            Optional<OtherBranchIssue> optional = otherBranchIssueRepository.findById(id);
            if (optional.isPresent()) {

                OtherBranchIssue otherBranchIssue = optional.get();
                otherBranchIssue.setStatus(status);
                otherBranchIssueRepository.save(otherBranchIssue);
                if (status.equalsIgnoreCase("REJECTED")) {
                    Optional<MaterialIssue> optionalMaterialIssue = materialIssueRepository.findById(otherBranchIssue.getIssueId());
                    if (optionalMaterialIssue.isPresent()) {
                        MaterialIssue materialIssue = optionalMaterialIssue.get();

                        Long count = materialIssueRepository.getCount(materialIssue.getRequestId());

                        if (count > 1) {
                            String statusValue = materialIssue.getStatus();
                            if (statusValue.equalsIgnoreCase("Pending_Quantity")) {
                                materialIssue.setStatus("Pending");
                                materialIssue.setTrnNo(null);
                            } else if (statusValue.equalsIgnoreCase("Issue")) {
                                materialIssue.setStatus("REJECTED");
                            }
                            materialIssueRepository.save(materialIssue);
                            rejectBinUpdates(materialIssue);
                        } else {

                            materialIssue.setStatus("Pending");
                            materialIssue.setTrnNo(null);
                            materialIssueRepository.save(materialIssue);
                            rejectBinUpdates(materialIssue);
                        }

                    }
                }
            }
            return new ResponseEntity<Status>(new Status("200", status + " SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<Status>(new Status("304", "Could not found Record"), HttpStatus.NOT_MODIFIED);
        }
    }


    public void rejectBinUpdates(MaterialIssue materialIssue) {

        List<IssueItems> items = issueItemsRepository.findAllByIssueItemIdANDActive(materialIssue.getId());

        for (IssueItems issueItems : items) {

            if (issueItems.getReceiveBinStockId() != null) {

                Optional<BinStock> optional = binStockRepository.findById(issueItems.getReceiveBinStockId());
                if (optional.isPresent()) {
                    Double revertQty = optional.get().getStkQty();

                    if (issueItems.getBinStockId() != null) {
                        Optional<BinStock> mainBin = binStockRepository.findById(issueItems.getBinStockId());
                        BinStock binStock = mainBin.get();
                        Double qty = binStock.getStkQty() + revertQty;
                        binStock.setStkQty(qty);
                        binStockRepository.save(binStock);
                        binStockRepository.delete(optional.get());
                    }
                }
            }
        }
        issueItemsRepository.updateInActive(materialIssue.getId());

    }

}