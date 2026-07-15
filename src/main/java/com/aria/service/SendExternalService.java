package com.aria.service;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.DateUtil;
import com.aria.utils.GDCUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SendExternalService {

    @Autowired
    SendExternalRepository sendExternalRepository;

    @Autowired
    SendExternalItemsRepository sendExternalItemsRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DepartmentDetailsRepository detailsRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    GDCUtil gdcUtil;

    @Autowired
    VendorRepository vendorRepository;

    @Autowired
    MaterialReturnRepository materialReturnRepository;

    @Autowired
    MaterialReturnItemsRepository materialReturnItemsRepository;

    public ResponseEntity getSendExternal(String name, String toReach, String fromDate, String toDate,
                                          String issueByName, String fromBranch, Long fromBranchId, Long firmId , Pageable pageable) {
        Date from = null;
        Date to = null;
        if (fromDate != null)
            from = dateUtil.getStringTODateWk(fromDate);
        if (toDate != null)
            to = dateUtil.getStringTODateWk(toDate);
        Page<SendExternalEntityList> list = sendExternalRepository.getAll(from, to, toReach, name, issueByName, fromBranch, fromBranchId, firmId , pageable);

        if (!list.toList().isEmpty()) {
            List<SendExternalEntityListDTO> result = list.toList().stream().map(data -> {
                SendExternalEntityListDTO sendExternalEntityListDTO = new SendExternalEntityListDTO(data);
                return sendExternalEntityListDTO;
            }).collect(Collectors.toList());
            PageDataDto pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto>(new PageDataDto(), HttpStatus.OK);
        }
    }


    public ResponseEntity saveExternal(SendExternalDto sendExternalEntity, User user) {
        SendExternalEntity sendExternalDto = null;
        boolean materialReturn = false;
        if (sendExternalEntity.getMaterialFlowSendExternalId() == null || sendExternalEntity.getMaterialFlowSendExternalId() == 0) {
            sendExternalDto = new SendExternalEntity();
            sendExternalDto.setCreatedBy(user.getId());
            sendExternalDto.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        } else {
            Optional<SendExternalEntity> optional = sendExternalRepository.findById(sendExternalEntity.getMaterialFlowSendExternalId());
            if (optional.isPresent()) {
                sendExternalDto = optional.get();
                sendExternalItemsRepository.deleteBySendExternalRefId(sendExternalDto.getMaterialFlowSendExternalId());
                sendExternalDto.setUpdatedBy(user.getId());
                sendExternalDto.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            } else {
                sendExternalDto = new SendExternalEntity();
                sendExternalDto.setCreatedBy(user.getId());
                sendExternalDto.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            }
        }
        sendExternalDto.setMaterialFlowSendExternalId(sendExternalEntity.getMaterialFlowSendExternalId());
        sendExternalDto.setFirmId(sendExternalEntity.getFirmId());
        sendExternalDto.setTransactionNumber(sendExternalEntity.getTransactionNumber());
        sendExternalDto.setReferenceDate(dateUtil.getStringTODateWk(sendExternalEntity.getReferenceDate()));
        sendExternalDto.setVehicle(sendExternalEntity.getVehicle());
        sendExternalDto.setVehicleNumber(sendExternalEntity.getVehicleNumber());
        sendExternalDto.setActiveFlag(sendExternalEntity.getActiveFlag());
        sendExternalDto.setIssueMode(sendExternalEntity.getIssueMode());
        sendExternalDto.setIssueById(sendExternalEntity.getIssueById());
        sendExternalDto.setMaterialReturn(sendExternalEntity.getMaterialReturn());
        sendExternalDto.setToReach(sendExternalEntity.getToReach());
        sendExternalDto.setToVendorRefId(sendExternalEntity.getToVendorRefId());
        sendExternalDto.setToDepartmentId(sendExternalEntity.getToDepartmentId());
        sendExternalDto.setToBranchId(sendExternalEntity.getToBranchId());
        sendExternalDto.setCreatedDepartmentId(sendExternalEntity.getCreatedDepartmentId());
        sendExternalDto.setCreatedBranchId(sendExternalEntity.getCreatedBranchId());
        sendExternalDto.setStatus(sendExternalEntity.getStatus());
        sendExternalDto.setEwayBill(sendExternalEntity.getEwayBill());
        if (sendExternalDto.getGDCNO() == null || sendExternalDto.getGDCNO().equalsIgnoreCase("")) {
            String gdcNo = gdcUtil.getGDCNo(user.getBranch_id(), user.getOrg_id());
            sendExternalDto.setGDCNO(gdcNo);
            sendExternalDto.setGdc_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

        }
        sendExternalDto = sendExternalRepository.save(sendExternalDto);
        List<SendExternalItemsEntity> list = new ArrayList<>();
        SendExternalItemsEntity entity = null;
        for (SendExternalItemDto dto : sendExternalEntity.getSendExternalItems()) {
            entity = new SendExternalItemsEntity();
            entity.setSendExternalRefId(sendExternalDto.getMaterialFlowSendExternalId());
            entity.setSendExternalItemName(dto.getSendExternalItemName());
            entity.setSendExternalItemQuantity(dto.getSendExternalItemQuantity());
            entity.setSendExternalItemCost(dto.getSendExternalItemCost());
            entity.setSendExternalItemUnit(dto.getSendExternalItemUnit());
            entity.setSendExternalItemLot(dto.getSendExternalItemLot());
            entity.setSendExternalItemHsncode(dto.getSendExternalItemHsncode());
            entity.setPackingQuantity(dto.getPackingQuantity());
            entity.setPackingType(dto.getPackingType());
            if (dto.getMaterialReturn() != null && dto.getMaterialReturn().equalsIgnoreCase("YES")) {
                materialReturn = true;
            } else {
                MaterialReturnItems materialReturnItems = materialReturnItemsRepository.findBy(sendExternalEntity.getMaterialFlowSendExternalId(), entity.getSendExternalItemId(), entity.getSendExternalItemName(), entity.getSendExternalItemQuantity().toString());
                if (materialReturnItems != null) {
                    return new ResponseEntity<Object>(new Status("400", "Already Material ReturnUpdated this product!!!"), HttpStatus.OK);
                }
            }
            entity.setMaterialReturn(dto.getMaterialReturn());

            list.add(entity);
        }
        sendExternalItemsRepository.saveAll(list);
        if (materialReturn) {
            updateMaterialReturn(sendExternalDto, user);
        } else {
            deleteMaterialReturn(sendExternalDto, user);
        }

        return new ResponseEntity<Object>(new Status("200", "Saved Successfully..."), HttpStatus.OK);
    }

    private SendExternalDto entityToDto(SendExternalEntity sendExternalEntity) {
        SendExternalDto sendExternalDto = new SendExternalDto();
        sendExternalDto.setFirmId(sendExternalEntity.getFirmId());
        sendExternalDto.setMaterialFlowSendExternalId(sendExternalEntity.getMaterialFlowSendExternalId());
        sendExternalDto.setTransactionNumber(sendExternalEntity.getTransactionNumber());
        sendExternalDto.setReferenceDate(dateUtil.getDateTOStringWk(sendExternalEntity.getReferenceDate()));
        sendExternalDto.setVehicle(sendExternalEntity.getVehicle());
        sendExternalDto.setVehicleNumber(sendExternalEntity.getVehicleNumber());
        sendExternalDto.setActiveFlag(sendExternalEntity.getActiveFlag());
        sendExternalDto.setIssueMode(sendExternalEntity.getIssueMode());
        sendExternalDto.setIssueById(sendExternalEntity.getIssueById());
        sendExternalDto.setMaterialReturn(sendExternalEntity.getMaterialReturn());
        sendExternalDto.setToReach(sendExternalEntity.getToReach());
        sendExternalDto.setToVendorRefId(sendExternalEntity.getToVendorRefId());
        sendExternalDto.setToDepartmentId(sendExternalEntity.getToDepartmentId());
        sendExternalDto.setToBranchId(sendExternalEntity.getToBranchId());
        sendExternalDto.setCreatedDepartmentId(sendExternalEntity.getCreatedDepartmentId());
        sendExternalDto.setCreatedBranchId(sendExternalEntity.getCreatedBranchId());
        sendExternalDto.setStatus(sendExternalEntity.getStatus());
        sendExternalDto.setEwayBill(sendExternalEntity.getEwayBill());
        sendExternalDto.setGDCNO(sendExternalEntity.getGDCNO());
        if (sendExternalEntity.getGdc_date() != null) {
            sendExternalDto.setGdcDate(dateUtil.localDateTimeToString(sendExternalEntity.getGdc_date()));
            sendExternalDto.setGdcDateTime(dateUtil.localDateTimeToString(sendExternalEntity.getGdc_date()));
        }
        Optional<User> optional = userRepository.findById(sendExternalEntity.getIssueById());
        if (optional.isPresent()) {
            sendExternalDto.setIssueByName(optional.get().getEmployeename());
        }
        if (sendExternalEntity.getToDepartmentId() != null && sendExternalEntity.getToDepartmentId() != 0) {
            Optional<DepartmentDetails> departmentDetailsOptionalTo = detailsRepository.findById(sendExternalDto.getToDepartmentId());
            if (departmentDetailsOptionalTo.isPresent()) {
                sendExternalDto.setToDepartmentName(departmentDetailsOptionalTo.get().getName());
            }
        }
        if (sendExternalEntity.getCreatedDepartmentId() != null && sendExternalEntity.getCreatedDepartmentId() != 0) {
            Optional<DepartmentDetails> departmentDetailsOptionalCreate = detailsRepository.findById(sendExternalDto.getCreatedDepartmentId());
            if (departmentDetailsOptionalCreate.isPresent()) {
                sendExternalDto.setCreatedDepartmentName(departmentDetailsOptionalCreate.get().getName());
            }
        }

        if (sendExternalEntity.getToVendorRefId() != null && sendExternalEntity.getToVendorRefId() != 0) {
            PartyList partyList = vendorRepository.findByVendor(sendExternalEntity.getToVendorRefId());
            if (partyList != null)
                sendExternalDto.setVendorDetails(new PartyDetails(partyList));
        }

        sendExternalDto.setCreatedBy(sendExternalEntity.getCreatedBy());
        Optional<User> optionalCreate = userRepository.findById(sendExternalEntity.getCreatedBy());
        if (optionalCreate.isPresent()) {
            sendExternalDto.setCreatedByName(optionalCreate.get().getEmployeename());

            CompanyList companyList = organisationRepository.findByFirm(optionalCreate.get().getOrg_id());
            if (companyList != null) {
                sendExternalDto.setFirmDetails(new Company(companyList));
            }

            if (sendExternalDto.getToBranchId() != null && sendExternalDto.getToBranchId() > 0) {
                BranchAddressList branchAddressList = branchRepository.getBranchByIdANDOrgId(sendExternalDto.getToBranchId(), optionalCreate.get().getOrg_id());
                if (branchAddressList != null) {
                    sendExternalDto.setToBranchDetails(new BranchAddress(branchAddressList));
                } else {
                    BranchAddressList branchAddressList1 = branchRepository.getBranchId(sendExternalDto.getToBranchId());
                    if (branchAddressList1 != null) {
                        sendExternalDto.setToBranchDetails(new BranchAddress(branchAddressList1));
                    }
                }
            }

            if (sendExternalDto.getCreatedBranchId() != null && sendExternalDto.getCreatedBranchId() > 0) {
                BranchAddressList branchAddressList = branchRepository.getBranchByIdANDOrgId(sendExternalDto.getCreatedBranchId(), optionalCreate.get().getOrg_id());
                if (branchAddressList != null) {
                    sendExternalDto.setFromBranchDetails(new BranchAddress(branchAddressList));
                } else {
                    BranchAddressList branchAddressList1 = branchRepository.getBranchId(sendExternalDto.getCreatedBranchId());
                    sendExternalDto.setFromBranchDetails(new BranchAddress(branchAddressList1));
                }
            }
        }

        List<SendExternalItemDto> list = new ArrayList<>();
        List<SendExternalItemsEntity> items = sendExternalItemsRepository.findBySendExternalRefId(sendExternalEntity.getMaterialFlowSendExternalId());
        for (SendExternalItemsEntity dto : items) {
            SendExternalItemDto entity = new SendExternalItemDto();
            entity.setSendExternalRefId(sendExternalDto.getMaterialFlowSendExternalId());
            entity.setSendExternalItemName(dto.getSendExternalItemName());
            entity.setSendExternalItemQuantity(dto.getSendExternalItemQuantity());
            entity.setSendExternalItemCost(dto.getSendExternalItemCost());
            entity.setSendExternalItemUnit(dto.getSendExternalItemUnit());
            entity.setSendExternalItemLot(dto.getSendExternalItemLot());
            entity.setSendExternalItemHsncode(dto.getSendExternalItemHsncode());
            entity.setPackingQuantity(dto.getPackingQuantity());
            entity.setMaterialReturn(dto.getMaterialReturn());
            entity.setPackingType(dto.getPackingType());
            entity.setMaterialFlowSendExternalItemsId(dto.getMaterialFlowSendExternalItemsId());
            list.add(entity);
        }
        sendExternalDto.setSendExternalItems(list);
        return sendExternalDto;

    }

    public ResponseEntity deleteSendExternal(Long id) {
        sendExternalItemsRepository.deleteBySendExternalRefId(id);
        sendExternalRepository.deleteById(id);
        return new ResponseEntity<Object>(new Status("200", "Deleted Successfully..."), HttpStatus.OK);
    }

    public ResponseEntity getSendExternal(Long id) {
        Optional<SendExternalEntity> optional = sendExternalRepository.findById(id);
        if (optional.isPresent()) {
            return new ResponseEntity<SendExternalDto>(entityToDto(optional.get()), HttpStatus.OK);
        }
        return new ResponseEntity<Object>(new Status("404", "Record not found"), HttpStatus.NOT_FOUND);
    }


    public void updateMaterialReturn(SendExternalEntity sendExternalEntity, User loggedUser) {

        Boolean update = false;

        MaterialReturn materialReturn = null;

        Optional<MaterialReturn> materialReturnOptional = materialReturnRepository.findBySendExternalId(sendExternalEntity.getMaterialFlowSendExternalId());

        if (materialReturnOptional.isPresent()) {
            materialReturn = new MaterialReturn(materialReturnOptional.get(), sendExternalEntity);
            update = true;
        } else {
            materialReturn = new MaterialReturn(sendExternalEntity);
        }

        materialReturn.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        materialReturn.setCreatedBy(loggedUser.getId());
        materialReturn.setType("SendExternal");
        //materialReturn.setStatus("Pending");
        if(sendExternalEntity.getFirmId() != null) {
            Organisation organisation = new Organisation();
            organisation.setId(sendExternalEntity.getFirmId());
            materialReturn.setOrganisation(organisation);
        }else {
            Organisation organisation = new Organisation();
            organisation.setId(loggedUser.getOrg_id());
            materialReturn.setOrganisation(organisation);
        }
        if (materialReturn.getFromDeptId() != null) {
            Optional<DepartmentDetails> departmentDetails = detailsRepository.findById(materialReturn.getFromDeptId());
            if (departmentDetails.isPresent())
                materialReturn.setFromDeptName(departmentDetails.get().getName());
        }
        if (materialReturn.getToDeptId() != null) {
            Optional<DepartmentDetails> departmentDetails = detailsRepository.findById(materialReturn.getToDeptId());
            if (departmentDetails.isPresent())
                materialReturn.setToDeptName(departmentDetails.get().getName());
        }

        if (materialReturn.getToReach().equalsIgnoreCase("Vendor")) {
            Optional<Vendor> vendorOptional = vendorRepository.findById(materialReturn.getToReachId());
            if (vendorOptional.isPresent()) {
                materialReturn.setToReachName(vendorOptional.get().getName());
            }
        } else if (materialReturn.getToReach().equalsIgnoreCase("Branch")) {
            Optional<Branch> optionalBranch = branchRepository.findById(materialReturn.getToReachId());
            if (optionalBranch.isPresent()) {
                materialReturn.setToReachName(optionalBranch.get().getBranchName());
            }
        } else if (materialReturn.getToReach().equalsIgnoreCase("Dep")) {
            Optional<DepartmentDetails> departmentDetails = detailsRepository.findById(materialReturn.getToReachId());
            if (departmentDetails.isPresent()) {
                materialReturn.setToReachName(departmentDetails.get().getName());
            }
        }

        MaterialReturn result = materialReturnRepository.save(materialReturn);
        if (update) {
            List<MaterialReturnItems> materialReturnItems = materialReturnOptional.get().getReturnItems();
            if(!materialReturnItems.isEmpty()) {
                materialReturnItemsRepository.deleteAll(materialReturnItems);
            }
        }

        List<SendExternalItemsEntity> otherBranchIssueItems = sendExternalItemsRepository.findBy(sendExternalEntity.getMaterialFlowSendExternalId());
        for (SendExternalItemsEntity itemsDto : otherBranchIssueItems) {
            if (update) {
                Optional<MaterialReturnItems> materialReturnItems = materialReturnItemsRepository.findBy(result.getId(), itemsDto.getSendExternalItemId(), itemsDto.getSendExternalItemName());
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


    public void deleteMaterialReturn(SendExternalEntity sendExternalEntity, User loggedUser) {

        Optional<MaterialReturn> materialReturnOptional = materialReturnRepository.findBySendExternalId(sendExternalEntity.getMaterialFlowSendExternalId());

        if (materialReturnOptional.isPresent()) {
            MaterialReturn materialReturn = materialReturnOptional.get();

            List<MaterialReturnItems> materialReturnItems = materialReturn.getReturnItems();
            if (!materialReturnItems.isEmpty()) {
                materialReturnItemsRepository.deleteAll(materialReturnItems);
            }
            materialReturnRepository.delete(materialReturn);

        }

    }
}
