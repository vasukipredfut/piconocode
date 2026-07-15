package com.aria.service;

import com.aria.dao.*;
import com.aria.dto.MaterialRequestDto;
import com.aria.dto.MaterialRequestList;
import com.aria.dto.PageDataDto;
import com.aria.dto.RequestItemDto;
import com.aria.model.*;
import com.aria.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import jakarta.persistence.Tuple;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MaterialRequestService {

    @Autowired
    MaterialRequestRepository materialRequestRepository;

    @Autowired
    RequestItemRepository requestItemRepository;

    @Autowired
    ProductMasterRepository productMasterRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    MaterialIssueRepository materialIssueRepository;

    @Autowired
    IssueItemsRepository issueItemsRepository;

    @Autowired
    SalesProductFilmOrderRepository salesProductFilmOrderRepository;

    public ResponseEntity<?> saveMaterialRequest(MaterialRequestDto materialRequestDto, User loggedUser) {

        MaterialRequest materialRequest = new MaterialRequest(materialRequestDto);
        materialRequest.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        materialRequest.setRequestBy(loggedUser);
        materialRequest.setCreatedBy(loggedUser.getId());
        materialRequest.setRequestDate(dateUtil.getISTDate());
        /* Branch branch = new Branch();
        branch.setId(loggedUser.getBranch_id());
        materialRequest.setBranch(branch);
         Organisation organisation = new Organisation();
        organisation.setId(loggedUser.getOrg_id());
        materialRequest.setOrganisation(organisation);
        materialRequest.setDepartment(loggedUser.getDepartment()); */
        materialRequest.setStatus("Request Sent");
        MaterialRequest result = materialRequestRepository.save(materialRequest);
        for (RequestItemDto requestItemDto : materialRequestDto.getRequestItems()) {
            Optional<ProductMaster> product = productMasterRepository.findById(requestItemDto.getItemId());
            RequestItem requestItem = new RequestItem(requestItemDto);
            if (product.isPresent()) {
                requestItem.setItemName(product.get().getProductName());
            }
            requestItem.setRequestId(result.getId());
            requestItem.setCreated_date(dateUtil.getISTDate());
            requestItem.setCreatedBy(loggedUser.getId());
            requestItemRepository.save(requestItem);
        }
        updateMaterialIssue(result.getId());
        return new ResponseEntity<Status>(new Status("200", "Material request sent successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateMaterialRequest(MaterialRequestDto materialRequestDto, User loggedUser) {
        MaterialRequest materialRequest = new MaterialRequest(materialRequestDto);
        materialRequest.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        materialRequest.setUpdatedBy(loggedUser.getId());
        materialRequest.setRequestDate(dateUtil.getISTDate());
        /* Branch branch = new Branch();
        branch.setId(loggedUser.getBranch_id());
        materialRequest.setBranch(branch);
        Organisation organisation = new Organisation();
        organisation.setId(loggedUser.getOrg_id());
        materialRequest.setOrganisation(organisation);
        materialRequest.setDepartment(loggedUser.getDepartment()); */
        materialRequest.setStatus("Request Sent");
        MaterialRequest result = materialRequestRepository.save(materialRequest);
        for (RequestItemDto requestItemDto : materialRequestDto.getRequestItems()) {
            RequestItem requestItem = new RequestItem(requestItemDto);
            requestItem.setRequestId(result.getId());
            requestItem.setCreated_date(dateUtil.getISTDate());
            requestItem.setCreatedBy(loggedUser.getId());
            requestItemRepository.save(requestItem);
        }
        return new ResponseEntity<Status>(new Status("200", "material request  updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllMaterialRequest(Long firmId, Long branchId, Long deptId, String requestId, String description, String branchName, String deptName,
                                                    String requestDate, String status, Pageable pageable) throws UsernameNotFoundException {
        Date date = null;
        if (requestDate != null) {
            date = dateUtil.getStringTODateWk(requestDate);
        }
        Long id = null;
        if (requestId != null && requestId.contains("-")) {
            List<String> splitId = new ArrayList<>(Arrays.asList(requestId.split("-")));
            if (splitId.size() >= 2) {
                id = Long.valueOf(splitId.get(1).trim());
            }
//            id = Long.valueOf(splitId.get(1).trim());
        }
        Page<MaterialRequestList> list = materialRequestRepository.materialRequestList(firmId , branchId, deptId, id, description, branchName, deptName, date, status, pageable);
        if (!list.toList().isEmpty()) {
            List<MaterialRequestDto> result = list.toList().stream().map(data -> new MaterialRequestDto(data)).collect(Collectors.toList());
            PageDataDto<MaterialRequestDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<MaterialRequestDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<MaterialRequestDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getMaterialRequest(String id) throws UsernameNotFoundException {
        Optional<MaterialRequest> optionalMaterialFlowRequest = materialRequestRepository.findById(Long.parseLong(id));
        Optional<Branch> branch = branchRepository.findById(optionalMaterialFlowRequest.get().getRequestBy().getBranch_id());
        MaterialRequestDto materialRequestDto = new MaterialRequestDto(optionalMaterialFlowRequest.get(), branch.get());
        if (materialRequestDto.getRequestName().equalsIgnoreCase("Film Order")) {
            List<RequestItem> itemList = requestItemRepository.findAllByRequestId(materialRequestDto.getId());
            List<RequestItemDto> itemDtos = itemList.stream().map(data -> {
                RequestItemDto itemDto = new RequestItemDto(data);
                Optional<SalesProductFilmOrder> salesProductFilmOrder = salesProductFilmOrderRepository.findByMrnNo(materialRequestDto.getId());
                if (salesProductFilmOrder.isPresent()) {
                    itemDto.setOrderQtyKg(salesProductFilmOrder.get().getOrderQtyKg());
                }
                return itemDto;
            }).collect(Collectors.toList());
            materialRequestDto.setRequestItems(itemDtos);
        }
        return new ResponseEntity<MaterialRequestDto>(materialRequestDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteMaterial(String id) throws UsernameNotFoundException {
        try {
            Optional<MaterialRequest> optionalMaterialFlowRequest = materialRequestRepository
                    .findById(Long.parseLong(id));
            if (optionalMaterialFlowRequest.isPresent()) {
                MaterialRequest materialRequest = optionalMaterialFlowRequest.get();
                materialRequestRepository.delete(materialRequest);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }


    public void updateMaterialIssue(Long materialRequestId) {

        Optional<MaterialRequest> optionalMaterialFlowRequest = materialRequestRepository.findById(materialRequestId);

        if (optionalMaterialFlowRequest.isPresent()) {

            MaterialRequest materialRequest = optionalMaterialFlowRequest.get();

            List<Tuple> deptWiseIds = requestItemRepository.findByRequestId(materialRequest.getId());

            if (!deptWiseIds.isEmpty()) {

                for (Tuple tuple : deptWiseIds) {

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
                    Branch issueBranch = new Branch();
                    issueBranch.setId(Long.valueOf(tuple.get("branchId").toString()));
                    materialIssue.setIssueBranch(issueBranch);
                    DepartmentDetails issueDepartment = new DepartmentDetails();
                    issueDepartment.setId(Long.valueOf(tuple.get("deptId").toString()));
                    materialIssue.setIssueDepartment(issueDepartment);
                    MaterialIssue result = materialIssueRepository.save(materialIssue);
                    /*List<RequestItem> requestItems = requestItemRepository.findBy(materialRequest.getId(), Long.valueOf(tuple.get("branchId").toString()), Long.valueOf(tuple.get("deptId").toString()));
                    if (!requestItems.isEmpty()) {
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
                    }*/
                }
            }
        }
    }

}
