package com.aria.service;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.GoDownReportProduct;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GoDownSummaryService {


    @Autowired
    GoDownReportProductRepository goDownReportProductRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    GoDownProductMapRepository goDownProductMapRepository;

    @Autowired
    CommonReportMenuTitleRepository commonReportMenuTitleRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    GoDownReportProductMenuTitleMappingRepository goDownReportProductMenuTitleMappingRepository;


    @Autowired
    BinStockLogRepository stBinRepository;


    @Autowired
    BinstockAuditLogRepository binstockAuditLogRepository;


    public ResponseEntity<List<GoDownReportProduct>> goDownSummaryProductList(String name) {
        return new ResponseEntity<>(goDownReportProductRepository.goDownSummaryProductList(name), HttpStatus.OK);
    }


//    public ResponseEntity<?> goDownSummary(User loggedUser, Long branch, Long itemId, Long godownId, String fromDate, String toDate) {
//
//        Date startDate;
//        Date endDate;
//
//        if (fromDate != null) {
//            startDate = dateUtil.getStringTODateWk(fromDate);
//        } else {
//            startDate = null;
//        }
//
//        if (toDate != null) {
//            endDate = dateUtil.getStringTODateWk(toDate);
//        } else {
//            endDate = null;
//        }
//
//        int month = dateUtil.getMonth(startDate);
//        int year = dateUtil.getYear(startDate);
//        
//        int toMonth = dateUtil.getMonth(endDate);
//        int toYear = dateUtil.getYear(endDate);        
//        
//
//
//        if (itemId == 0) {
//            itemId = null;
//        }
//        List<GoDownSummaryList> goDownSummaryLists = goDownProductMapRepository.goDown(branch, Long.valueOf(year), Long.valueOf(month), itemId, godownId ,Long.valueOf(toYear), Long.valueOf(toMonth));
//
//        if(!goDownSummaryLists.isEmpty()) {
//        	GoDownSummaryResponse finalResultList = new GoDownSummaryResponse();
//        	 List<GoDownSummaryResult> godownList = new ArrayList<>();
//        	 updateHeaderDetails(finalResultList, loggedUser.getOrg_id(), branch);
//        	for(GoDownSummaryList goDownSummaryList : goDownSummaryLists) {
//        GoDownSummaryResult result = new GoDownSummaryResult(goDownSummaryList);
////        updateHeaderDetails(result, loggedUser.getOrg_id(), branch);
//
//        List<GoDownSummaryList> goDownSummaryProductLists = goDownProductMapRepository.goDownSummaryProductList(branch, Long.valueOf(year), Long.valueOf(month), itemId, result.getGodownId(),Long.valueOf(toYear), Long.valueOf(toMonth));
//
//        List<GoDownSummaryProductDTO> productDTOList = goDownSummaryProductLists.stream().map(data -> {
//
//                    GoDownSummaryProductDTO productDTO = new GoDownSummaryProductDTO();
//                    productDTO.setItemName(data.getItemName());
//                    productDTO.setLots(stBinRepository.findByLotList(data.getItemId(), startDate, endDate));
//                    updateSummaryMenuWithStockQty(data, productDTO, branch, startDate, endDate);
//                    return productDTO;
//                }
//
//        ).collect(Collectors.toList());
//
//        result.setProductDTOList(productDTOList);
//
//
//        godownList.add(result);
//        	}
//        	finalResultList.setGoDownDetails(godownList);
//        	 return new ResponseEntity<>(finalResultList, HttpStatus.OK);
//        }
//        return new ResponseEntity<>(new Status("400", "Record Not Exists"), HttpStatus.NOT_FOUND);
//    }
    
    
    public ResponseEntity<?> goDownSummary(User loggedUser,
            Long branch,
            Long itemId,
            Long godownId,
            String fromDate,
            String toDate) {

    	Date startDate = fromDate != null ? dateUtil.getStringTODateWk(fromDate) : null;
    	Date endDate = toDate != null ? dateUtil.getStringTODateWk(toDate) : null;

    	if (itemId != null && itemId == 0) {
    		itemId = null;
    	}

    	List<GoDownSummaryList> dataList =
    			stBinRepository.findGodownProductFromStbin(branch, itemId, godownId, startDate, endDate);

    	if (dataList == null || dataList.isEmpty()) {
    		return new ResponseEntity<>(new Status("400", "Record Not Exists"), HttpStatus.NOT_FOUND);
    	}

    	  GoDownSummaryResponse finalResponse = new GoDownSummaryResponse();

    	    updateHeaderDetails(finalResponse, loggedUser.getOrg_id(), branch);

    	    Map<Long, List<GoDownSummaryList>> godownGroup =
    	            dataList.stream().collect(Collectors.groupingBy(GoDownSummaryList::getGodownId));

    	    List<GoDownSummaryResult> godownResults = new ArrayList<>();

    	    for (Map.Entry<Long, List<GoDownSummaryList>> godownEntry : godownGroup.entrySet()) {

    	        List<GoDownSummaryList> godownData = godownEntry.getValue();

    	        GoDownSummaryResult godownResult = new GoDownSummaryResult();
    	        godownResult.setGodownId(godownEntry.getKey());
    	        godownResult.setGodownName(godownData.get(0).getGodownName());

    	        Map<Long, List<GoDownSummaryList>> itemGroup =
    	                godownData.stream().collect(Collectors.groupingBy(GoDownSummaryList::getItemId));

    	        List<GoDownSummaryProductDTO> productDTOList = new ArrayList<>();

    	        for (Map.Entry<Long, List<GoDownSummaryList>> itemEntry : itemGroup.entrySet()) {

    	            List<GoDownSummaryList> itemData = itemEntry.getValue();
    	            GoDownSummaryList first = itemData.get(0);

    	            GoDownSummaryProductDTO productDTO = new GoDownSummaryProductDTO();
    	            productDTO.setItemName(first.getItemName());

    	            Set<Double> lotSet = itemData.stream()
    	                    .map(GoDownSummaryList::getLot)
    	                    .collect(Collectors.toSet());

    	            productDTO.setLots(new ArrayList<>(lotSet));

    	            updateSummaryMenuWithStockQty(first, productDTO, branch, startDate, endDate);

    	            productDTOList.add(productDTO);
    	        }

    	        godownResult.setProductDTOList(productDTOList);
    	        godownResults.add(godownResult);
    	    }

    	    finalResponse.setGoDownDetails(godownResults);

    	    return new ResponseEntity<>(finalResponse, HttpStatus.OK);
    	}


    public void updateHeaderDetails(GoDownSummaryResponse result, Long firmId, Long branch) {

        CompanyList companyList = organisationRepository.findByFirm(firmId);
        if (companyList != null) {
            result.setFirmDetails(new Company(companyList));
        }
        BranchAddressList branchAddressList1 = branchRepository.getBranchId(branch);
        if (branchAddressList1 != null) {
            result.setPlantDetails(new BranchAddress(branchAddressList1));
        }
    }

    public void updateSummaryMenuWithStockQty(GoDownSummaryList data, GoDownSummaryProductDTO productDTO, Long branch, Date startDate, Date endDate) {

        List<String> menuList = goDownReportProductMenuTitleMappingRepository.menulist(data.getItemId());

        List<GoDownSummaryProductMenuDTO> productMenuTitles = menuList.stream().map(menu -> {
            GoDownSummaryProductMenuDTO productMenu = new GoDownSummaryProductMenuDTO();
            productMenu.setMenuName(menu);
            List<StockQtyWithLot> stockQtyWithLots = null;
            if (menu.equalsIgnoreCase("Opening Balance")) {
            	
                stockQtyWithLots = binstockAuditLogRepository.findBYRefIdWithLot(branch, "OPENING", data.getItemId(), startDate, endDate,data.getGodownId());
            } else if (menu.equalsIgnoreCase("Received from Cleaning")) {            	
            	stockQtyWithLots =stBinRepository.findByRefidAfterCleaningData(branch,"PRODUCTION" , "R", data.getItemId(), startDate, endDate,4L,data.getGodownId());
            } else if (menu.equalsIgnoreCase("Lorry Receipts")) {            	
                stockQtyWithLots =stBinRepository.findByRefidAfterCleaningData(branch,"Stock Inward" , "R", data.getItemId(), startDate, endDate,null,data.getGodownId());     		
            } else if (menu.equalsIgnoreCase("Lorry Excess")) {            	
            	 stockQtyWithLots = stBinRepository.findByRefidAfterCleaningData(branch,"Excess" , "R", data.getItemId(), startDate, endDate,null,data.getGodownId());    			            			
            } else if (menu.equalsIgnoreCase("Split Return")) {
            	stockQtyWithLots = binstockAuditLogRepository.findBYRefIdWithLot(branch, "SPLIT", data.getItemId(), startDate, endDate,data.getGodownId());
            	
            } else if (menu.equalsIgnoreCase("Total Receipts")) {            	
            	
            } else if (menu.equalsIgnoreCase("Issued To Cleaning")) {            	
            	stockQtyWithLots =stBinRepository.findByRefidAfterCleaningData(branch,"PRODUCTION" , "I", data.getItemId(), startDate, endDate,4L, data.getGodownId());
            } else if (menu.equalsIgnoreCase("Lorry Shortage")) {
            	 stockQtyWithLots =stBinRepository.findByRefidAfterCleaningData(branch,"Shortage" , "I", data.getItemId(), startDate, endDate,null,data.getGodownId());
            			
            } else if (menu.equalsIgnoreCase("Despatch")) {            	
            	stockQtyWithLots =stBinRepository.findByRefidAfterCleaningData(branch,"DISPATCH" , "I", data.getItemId(), startDate, endDate,null,data.getGodownId());
            } else if (menu.equalsIgnoreCase("Total Issues")) {            	
            	
            } else if (menu.equalsIgnoreCase("Closing Balance")) {
            	
                stockQtyWithLots = binstockAuditLogRepository.findBYRefIdWithLot(branch, "CLOSING", data.getItemId(), startDate, endDate,data.getGodownId());
            } else if (menu.equalsIgnoreCase("Inter Plant Issues To Plant 2")) {
            	stockQtyWithLots =stBinRepository.interPlantTOPlant2(branch,"MATERIAL_TRANSACTION" , "I", data.getItemId(), startDate, endDate ,null,data.getGodownId());
           

            } else if (menu.equalsIgnoreCase("Issued To Production")) {
            	
            	stockQtyWithLots =stBinRepository.findByRefidAfterCleaningData(branch,"PRODUCTION" , "I", data.getItemId(), startDate, endDate,8L,data.getGodownId());
            	
            	
            } else if (menu.equalsIgnoreCase("Issued To Packing")) {
            	
            	stockQtyWithLots =stBinRepository.findByRefidAfterCleaningData(branch,"PRODUCTION" , "I", data.getItemId(), startDate, endDate ,10L,data.getGodownId());
            	

            } else if (menu.equalsIgnoreCase("Received From Other Plants")) {
            	
            	stockQtyWithLots = binstockAuditLogRepository.findBYRefIdWithLottran(branch, "MATERIAL_TRANSACTION", data.getItemId(), startDate, endDate,data.getGodownId(),"R");
            	
            } else if (menu.equalsIgnoreCase("Issued To Other Plants")) {
            	
            	stockQtyWithLots = binstockAuditLogRepository.findBYRefIdWithLottran(branch, "MATERIAL_TRANSACTION", data.getItemId(), startDate, endDate,data.getGodownId(),"I");
            }

            if (stockQtyWithLots != null && !stockQtyWithLots.isEmpty()) {

            	List<LotQtyDto> lotWithQtyList = stockQtyWithLots.stream()
            		    .map(item -> {
            		        LotQtyDto dto = new LotQtyDto(item);            		       
            		        return dto;
            		    })
            		    .collect(Collectors.toList());

            		productMenu.setLotWithQty(lotWithQtyList);

            } else {
                productMenu.setLotWithQty(null);
            }
            return productMenu;
        }).collect(Collectors.toList());

        productDTO.setProductMenuTitles(productMenuTitles);

    }

}



