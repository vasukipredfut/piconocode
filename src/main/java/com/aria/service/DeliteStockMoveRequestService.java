package com.aria.service;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.DateUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DeliteStockMoveRequestService {

    @Autowired
    private DeliteStockMoveRequestRepository deliteStockMoveRequestRepository;
    @Autowired
    private MaterialRequestRepository materialRequestRepository;
    @Autowired
    private ProductMasterRepository productMasterRepository;
    @Autowired
    private RequestItemRepository requestItemRepository;
    @Autowired
    private MaterialRequestService materialRequestService;
    @Autowired
    private DeliteStockMoveRequestBinRepository deliteStockMoveRequestBinRepository;

    @Autowired
    private DateUtil dateUtil;

    public ResponseEntity<?> saveDeliteStockMoveRequest(DeliteStockMoveRequestDto deliteStockMoveRequestDto, User loggedUser) {
        DeliteStockMoveRequest deliteStockMoveRequest = new DeliteStockMoveRequest(deliteStockMoveRequestDto);
        deliteStockMoveRequest.setRequestDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        deliteStockMoveRequest.setUpdatedBy(loggedUser.getId());
        DeliteStockMoveRequest res = deliteStockMoveRequestRepository.save(deliteStockMoveRequest);
        if (!deliteStockMoveRequestDto.getDeliteStockMoveRequestBins().isEmpty()) {
            for (DeliteStockMoveRequestBinDto binDto : deliteStockMoveRequestDto.getDeliteStockMoveRequestBins()) {
                DeliteStockMoveRequestBin deliteStockMoveRequestBin = new DeliteStockMoveRequestBin(binDto);
                deliteStockMoveRequestBin.setDelitestockMoverequestId(res.getId());
                deliteStockMoveRequestBinRepository.save(deliteStockMoveRequestBin);
            }
        }
        if (res.getActivationStatus().equalsIgnoreCase("Activated")) {
            saveMaterialFlowRequest(res, loggedUser);
        }
        return new ResponseEntity<Status>(new Status("200", "DeliteStockMoveRequest save successfully!!!"), HttpStatus.OK);
    }

    public void saveMaterialFlowRequest(DeliteStockMoveRequest deliteStockMoveRequest, User loggedUser) {
        MaterialRequest materialRequest = createMaterialRequest(deliteStockMoveRequest, loggedUser);
        deliteStockMoveRequest.setMaterialRequestId(materialRequest.getId());
        deliteStockMoveRequestRepository.save(deliteStockMoveRequest);
        createRequestItem(materialRequest, deliteStockMoveRequest);
        materialRequestService.updateMaterialIssue(materialRequest.getId());
    }

    private MaterialRequest createMaterialRequest(DeliteStockMoveRequest res, User loggedUser) {

        System.out.println("FirmId == "+res.getFirmId());

        MaterialRequestDto materialRequestDto = new MaterialRequestDto();
        materialRequestDto.setRequestFromFirmId(res.getFirmId());
        materialRequestDto.setRequestName("Delite Bag Transfer");
        materialRequestDto.setRequestDescription("Delite Bag Transfer");
        materialRequestDto.setRequestById(res.getActivatedBy());
        materialRequestDto.setRequestFromBranchId(res.getToBranchId());
        materialRequestDto.setRequestFromDepartmentId(4L);
        materialRequestDto.setCreatedBy(res.getActivatedBy());
        materialRequestDto.setStatus("Request Sent");
        MaterialRequest materialRequest = new MaterialRequest(materialRequestDto);
        materialRequest.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        materialRequest.setRequestDate(Date.from(res.getActivatedOn().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        return materialRequestRepository.save(materialRequest);
    }

    private void createRequestItem(MaterialRequest materialRequest, DeliteStockMoveRequest deliteStockMoveRequest) {

        List<DeliteBinStockQuantity> deliteBinStocks = deliteStockMoveRequestBinRepository.getDeliteBinStockListGroupByBranchDeptItemLot(deliteStockMoveRequest.getId());
        if (!deliteBinStocks.isEmpty()) {

            for (DeliteBinStockQuantity deliteBinStockQuantity : deliteBinStocks) {
                RequestItemDto requestItemDto = new RequestItemDto();
                requestItemDto.setRequestId(materialRequest.getId());
                requestItemDto.setSelectLot(String.valueOf(deliteBinStockQuantity.getLot()));
                requestItemDto.setIssueDepartmentId(4L);
                requestItemDto.setIssueBranchId(deliteStockMoveRequest.getFromBranchId());
                requestItemDto.setStockQuantity(String.valueOf(deliteBinStockQuantity.getQuantity()));
                requestItemDto.setStockRequired(String.valueOf(deliteBinStockQuantity.getQuantity()));
                requestItemDto.setActive(materialRequest.getActive());
                requestItemDto.setCreatedBy(materialRequest.getCreatedBy());
                requestItemDto.setCreated_date(materialRequest.getRequestDate());
                requestItemDto.setUpdatedBy(materialRequest.getUpdatedBy());
                requestItemDto.setUpdated_date(materialRequest.getRequestDate());
                RequestItem requestItem = new RequestItem(requestItemDto);
                Optional<ProductMaster> product = productMasterRepository.findById(deliteBinStockQuantity.getItemId());
                product.ifPresent(productMaster -> {
                    requestItem.setItemId(productMaster.getId());
                    requestItem.setItemName(productMaster.getProductName());
                });
                requestItemRepository.save(requestItem);
            }
        }
    }


    public ResponseEntity<?> fetchAllDeliteStockMoveRequest(Long fromBranchId, Long toBranchId, String status, Pageable pageable) {
        Page<DeliteStockMoveRequest> deliteStockMoveRequests = deliteStockMoveRequestRepository
                .findAllByBranchAndStatusPagination(fromBranchId, toBranchId, status, pageable);
        List<DeliteStockMoveRequestDto> deliteStockMoveRequestDtos = deliteStockMoveRequests
                .stream().map(DeliteStockMoveRequestDto::new).collect(Collectors.toList());
        PageDataDto<DeliteStockMoveRequestDto> pageData = new PageDataDto<>(deliteStockMoveRequestDtos, pageable, deliteStockMoveRequests);
        return new ResponseEntity<>(pageData, HttpStatus.OK);
    }

    public ResponseEntity<?> fetchDeliteStockMoveRequestById(Long id) {

        Optional<DeliteStockMoveRequest> deliteStockMoveRequestOpt = deliteStockMoveRequestRepository.findById(id);

        if (deliteStockMoveRequestOpt.isPresent()) {
            DeliteStockMoveRequest deliteStockMoveRequest = deliteStockMoveRequestOpt.get();
            DeliteStockMoveRequestDto deliteStockMoveRequestDto = new DeliteStockMoveRequestDto(deliteStockMoveRequest);
            return new ResponseEntity<>(deliteStockMoveRequestDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new Status("404", "No Records Found"), HttpStatus.NOT_FOUND);
        }
    }


    public void updateDeliteScheduler() {
        System.out.println("###################");

        Date date = dateUtil.getISTDate();
        System.out.println(" IST DATE == " + date);

        System.out.println("###################");
        System.out.println("###################");

        List<DeliteStockMoveRequest> deliteStockMoveRequests = deliteStockMoveRequestRepository.findAllByToDate(date);
        if (!deliteStockMoveRequests.isEmpty() && deliteStockMoveRequests != null) {
            for (DeliteStockMoveRequest deliteStockMove : deliteStockMoveRequests) {
                deliteStockMove.setActivationStatus("Closed");
                deliteStockMoveRequestRepository.save(deliteStockMove);
            }
        }
        System.out.println("###################");
    }
}
