package com.aria.service;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MaterialFlowGDCService {

    @Autowired
    MaterialFlowGDCRepository materialFlowGDCRepository;

    @Autowired
    MaterialFlowGDCItemRepository materialFlowGDCItemRepository;

    @Autowired
    OtherBranchIssueRepository otherBranchIssueRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    MaterialRequestRepository materialRequestRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    BranchRepository branchRepository;


    @Autowired
    ProductMasterRepository productMasterRepository;

    @Autowired
    BinStockRepository binStockRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EmastDailyRepository emastDailyRepository;


    public ResponseEntity<?> filterListAll(String gdcNo, Long issueBranchId, Long firmId, String fromDate, String toDate, Pageable pageable) throws UsernameNotFoundException {

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

        Page<GDCList> list = materialFlowGDCRepository.gdcList(gdcNo, issueBranchId, firmId,
                                                                     startDate, endDate, pageable);
        if (!list.toList().isEmpty()) {
            List<GDCDTO> result = list.toList().stream().map(data -> new GDCDTO(data))
                    .collect(Collectors.toList());
            PageDataDto<GDCDTO> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<GDCDTO>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<GDCDTO>>(new PageDataDto(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> viewGdc(String gdcNo, Long issueBranchId, Long reqBranchId) throws UsernameNotFoundException {


        GDCDetailsList gdcDetails = materialFlowGDCRepository.gdcDetails(gdcNo, issueBranchId, reqBranchId);
        if (gdcDetails != null) {

            GDCDetails details = new GDCDetails(gdcDetails);

            Optional<MaterialRequest> materialRequest = materialRequestRepository.findById(details.getRequestId());

            if (materialRequest.isPresent()) {

                CompanyList companyList = organisationRepository.findByFirm(materialRequest.get().getOrganisation().getId());
                if (companyList != null)
                    details.setCompanyDetails(new Company(companyList));

                BranchAddressList issueBranch = branchRepository.getBranchByIdANDOrgId(details.getIssueBranchId(), materialRequest.get().getOrganisation().getId());
                if (issueBranch != null)
                    details.setIssueBranchDetails(new BranchAddress(issueBranch));


                BranchAddressList reqBranch = branchRepository.getBranchByIdANDOrgId(details.getRequestBranchId(), materialRequest.get().getOrganisation().getId());
                if (reqBranch != null)
                    details.setFromBranchDetails(new BranchAddress(reqBranch));
            }

            if (details.getIssueMode().equalsIgnoreCase("c")) {
                Optional<EmastDaily> user = emastDailyRepository.findById(details.getDeliveryUserId());
                if (user.isPresent()) {
                    details.setDeliveryByStaffName(user.get().getName());
                }
            } else {
                Optional<User> user = userRepository.findById(details.getDeliveryUserId());
                if (user.isPresent()) {
                    details.setDeliveryByStaffName(user.get().getEmployeename());
                }
            }
            Optional<User> user = userRepository.findById(details.getSenderId());
            if (user.isPresent()) {
                details.setSenderName(user.get().getEmployeename());
            }

            Optional<User> updateUser = userRepository.findById(details.getUpdatedBy());
            if (updateUser.isPresent()) {
                details.setUpdatedByName(updateUser.get().getEmployeename());
            }

            List<GDCItemsList> gdcItemsLists = materialFlowGDCRepository.gdcItemDetails(gdcNo, issueBranchId, reqBranchId);
            if (!gdcItemsLists.isEmpty()) {
                List<GDCItems> gdcItems = gdcItemsLists.stream().map(data -> {
                    GDCItems gdcItem = new GDCItems(data);
                    Optional<ProductMaster> optional = productMasterRepository.findById(gdcItem.getItemId());
                    if (optional.isPresent()) {
                        ProductMaster productMaster = optional.get();
                        gdcItem.setHsnCode(productMaster.getHsnRefId());
                        if (gdcItem.getLot() != null) {
                            if (gdcItem.getLot().equalsIgnoreCase("1")) {
                                gdcItem.setUnit(productMaster.getUnit());
                            } else {
                                gdcItem.setUnit(productMaster.getAltunit());
                            }
                        }
                    }
                    Double amount = 0.0;

                    System.out.println("gdcItem.getItemId() == " + gdcItem.getItemId() + " gdcItem.getBatch() == " + gdcItem.getBatch() + " gdcItem.getBinName() " + gdcItem.getBinName() + " gdcItem.getLot()" + gdcItem.getLot());

                    BinStock binStock = binStockRepository.findByProductBatchAndBinId(gdcItem.getItemId(), gdcItem.getBatch(), Long.valueOf(gdcItem.getBinName()), Double.valueOf(gdcItem.getLot()));
                    if (binStock != null) {
                        if (binStock.getAvgCost() != null && binStock.getAvgCost() > 0) {
                            amount = (binStock.getAvgCost() * Long.valueOf(gdcItem.getAllocatedQuantity()));
                        } else {
                            if (binStock.getCost() != null && binStock.getCost() > 0) {
                                Double amount1 = (binStock.getCost() / binStock.getStkQty());
                                amount = amount1 * Long.valueOf(gdcItem.getAllocatedQuantity());
                            }

                        }
                    }
                    gdcItem.setAmount(amount);

                    return gdcItem;
                }).collect(Collectors.toList());

                details.setGdcItemsList(gdcItems);
            }

            return new ResponseEntity<GDCDetails>(details, HttpStatus.OK);
        } else {
            return new ResponseEntity<GDCDetails>(HttpStatus.NOT_FOUND);
        }
    }
}
