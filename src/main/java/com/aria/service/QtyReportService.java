package com.aria.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aria.dao.BinStockLogRepository;
import com.aria.dao.BinstockAuditLogRepository;
import com.aria.dao.BranchRepository;
import com.aria.dao.OrganisationRepository;
import com.aria.dao.QtyMainRepository;
import com.aria.dao.QtyRefidRepository;
import com.aria.dao.QtySjidRepository;
import com.aria.dao.QtySub1Repository;
import com.aria.dao.QtySub2Repository;
import com.aria.dto.BranchAddress;
import com.aria.dto.BranchAddressList;
import com.aria.dto.Company;
import com.aria.dto.CompanyList;
import com.aria.dto.LotQtyDto;
import com.aria.dto.QtyMainDto;
import com.aria.dto.QtyPfList;
import com.aria.dto.QtyProductJournalDto;
import com.aria.dto.QtyStamentResponseDto;
import com.aria.dto.StockFlowType;
import com.aria.dto.StockQtyWithLot;
import com.aria.dto.Sub2ProductDto;
import com.aria.dto.SubProductDto;
import com.aria.model.QtyMain;
import com.aria.model.QtySub1;
import com.aria.model.QtySub2;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.utils.DateUtil;


@Service
public class QtyReportService {
	@Autowired
	QtyMainRepository qtyMainRepository;
	@Autowired
	QtySub1Repository qtySub1Repository;
	@Autowired
	QtySub2Repository qtySub2Repository;
	@Autowired
	QtySjidRepository qtySjidRepository;
	@Autowired
	QtyRefidRepository qtyRefidRepository;
	
	@Autowired
	DateUtil dateUtil;
	
	@Autowired
	BranchRepository branchRepository;

	    @Autowired
	    OrganisationRepository organisationRepository;
	    
	    @Autowired
	    BinstockAuditLogRepository binstockAuditLogRepository;
	    
	    @Autowired
	    BinStockLogRepository stBinRepository;

	  
	public ResponseEntity<?> getAllQtyProduct() {
		List<QtyMain> QtyName = qtyMainRepository.findAll();
    	if(!QtyName.isEmpty()) {
        List<QtyMainDto> voucherFormatDtos = QtyName.stream().map(val -> new QtyMainDto(val)).collect(Collectors.toList());
        return new ResponseEntity<List<QtyMainDto>>(voucherFormatDtos, HttpStatus.OK);
    	}
    	else {
    		  return new ResponseEntity<List<QtyMainDto>>(new ArrayList<>(), HttpStatus.OK);
    	}
		
	}

	
	public ResponseEntity<?> qtyStatement( Long branch, Long itemId, String fromDate, String toDate, User loggedUser) {
		
		Date startDate;
        Date endDate;

        if (fromDate != null) {
            startDate = dateUtil.getStringTODateWk(fromDate);
        } else {
            startDate = null;
        }

        if (toDate != null) {
            endDate = dateUtil.getStringTODateWk(toDate);
        } else {
            endDate = null;
        }
        
        
		List<QtySub1> sub1List = qtySub1Repository.findByParent(itemId);
		if(!sub1List.isEmpty()) {
			QtyStamentResponseDto finalResult = new QtyStamentResponseDto();
			 updateHeaderDetails(finalResult, loggedUser.getOrg_id(), branch);
			List<SubProductDto> subProduct =  new ArrayList<>();
		 List<SubProductDto> collect = sub1List.stream().map(data->{
			SubProductDto result = new SubProductDto();
		result.setSub1ProuctId(data.getId());
		result.setSub1ProductName(data.getName());
		updateSummaryMenu(result, itemId);
		return result;
		}).collect(Collectors.toList());
		
		finalResult.setSub1ProdutList(collect);

	    // ✅ Return the final DTO
	    return ResponseEntity.ok(finalResult);
	} return new ResponseEntity<>(new Status("400", "Record Not Exists"), HttpStatus.NOT_FOUND);
}
	private void updateSummaryMenu(SubProductDto result, Long itemId) {
	    List<QtySub2> menuList =
	        qtySub2Repository.findByParentAndGparent(result.getSub1ProuctId(), itemId);

	    List<Sub2ProductDto> list = menuList.stream()
	        .map(menu -> {
	            Sub2ProductDto dto = new Sub2ProductDto();
	            dto.setProductId(menu.getId());
	            dto.setProductName(menu.getName());
	            return dto;
	        }).collect(Collectors.toList());

	    result.setSub2ProductList(list);
	}
	
	
	public List<Sub2ProductDto> getQtyDetail(Long branch, Long itemId, Long sub1Id, String fromDate, String toDate, User user) {

		
		Date startDate;
        Date endDate;

        if (fromDate != null) {
            startDate = dateUtil.getStringTODateWk(fromDate);
        } else {
            startDate = null;
        }

        if (toDate != null) {
            endDate = dateUtil.getStringTODateWk(toDate);
        } else {
            endDate = null;
        }
        List<QtySub2> sub2List =
                qtySub2Repository.findByParentAndGparent(sub1Id, itemId);

        List<Sub2ProductDto> result = new ArrayList<>();

        for (QtySub2 sub2 : sub2List) {

            StockFlowType flowType = resolveFlowType(sub2.getName());
            if (flowType == StockFlowType.IGNORE) {
                continue;
            }

            QtyProductJournalDto productId =
                    resolveProductIds(sub2.getId(), sub1Id, itemId);          
            
           if((productId.getJournalId() == null || productId.getJournalId().isEmpty())
        		   &&
        		    (productId.getProductId() == null || productId.getProductId().isEmpty())) {
            	continue;
            }
           boolean isPf=productId.isPfKey();
           List<Long> sjId = productId.getJournalId();
           List<Long> refId = productId.getProductId();
           
           boolean hasSj = sjId != null && !sjId.isEmpty();
           boolean hasRef = refId != null && !refId.isEmpty();
           List<StockQtyWithLot> stock;
           if(!isPf) {
           if(hasSj) {
            stock =
                    fetchStockQtySjid(flowType, branch, sjId,refId,
                            startDate, endDate, user.getOrg_id());
           }else {
        	   stock =
        			   fetchStockQtyref(flowType, branch,refId,
                               startDate, endDate, user.getOrg_id());
           }
         
            Sub2ProductDto dto = new Sub2ProductDto();
            dto.setProductId(sub2.getId());
            dto.setProductName(sub2.getName());

            if (stock != null && !stock.isEmpty()) {

            	List<LotQtyDto> lotQty =
            	        stock.stream()
            	             .map(LotQtyDto::new)  
            	             .collect(Collectors.toList());

            	dto.setLotWithQty(lotQty);
            }

            result.add(dto);
           }
           else {
        	   if(hasSj) {
                   stock =
                           fetchStockQtySjidPf(flowType, branch, sjId,refId,
                                   startDate, endDate, user.getOrg_id());
                  }else {
               	   stock =
               			   fetchStockQtyrefPf(flowType, branch,refId,
                                      startDate, endDate, user.getOrg_id());
                  }
                
                   Sub2ProductDto dto = new Sub2ProductDto();
                   dto.setProductId(sub2.getId());
                   dto.setProductName(sub2.getName());

                   if (stock != null && !stock.isEmpty()) {

                   	List<LotQtyDto> lotQty =
                   			stock.stream()
                            .map(s -> {
                            	LotQtyDto lDto = new LotQtyDto(s);
                            	Double qty = lDto.getValue();
                                if (qty != null) {
                                	lDto.setValue((double) Math.round(qty)); 
                                } else {
                                	lDto.setValue(0.0);
                                }
                                return lDto;
                            })
                            .collect(Collectors.toList());

                   	dto.setLotWithQty(lotQty);
                   }

                   result.add(dto);
           }
        }

        return result;
	}
	
	private StockFlowType resolveFlowType(String name) {
		
	
	    if (equalsAny(name,
	            "OPENING STOCK", "OPENING BALANCE",
	            "UNCLEANED SEED OPENING STOCK", "OIL OPENING STOCK")) {
	        return StockFlowType.OPENING;
	    }

	    if (equalsAny(name,
	            "PURCHASE", "BRANCH PURCHASE", "LORRY RECEIPTS")) {
	        return StockFlowType.PURCHASE;
	    }

	    if ("LORRY EXCESS".equalsIgnoreCase(name))
	        return StockFlowType.EXCESS;

	    if ("LORRY SHORTAGE".equalsIgnoreCase(name))
	        return StockFlowType.SHORTAGE;

	    if (equalsAny(name,
	            "DAMAGE", "ISSUED TO SPLIT",
	            "TAKEN FOR REJECTION  SEEDS PACKED")) {
	        return StockFlowType.SCRAP;
	    }
	    
	    if ("RECEIPT FROM SPLIT".equalsIgnoreCase(name))
	        return StockFlowType.STOCK;
	    
	    if (equalsAny(name,
	            "RECEIVED FROM DRYING",
	            "RECEIVED FROM YARD DRYING ",
	            "RECEIVED FROM DRYING PROCESS",
	            "RECEIVED FROM YARD DRYING ",
	            "RECEIVED FROM YARD DRIYING",
	            "WASTAGE",
	            "CRUSHING WASTAGE",
	            "GROUNDNUT WASTAGE",
	            "CRUSHING WASTE",
	            "RECEIVED FROM SAND CLEANING",
	            "RECEIVED FROM SAMBANDHI CLEANING",
	            "RECEIVED FROM SORTING",
	            "RECEIVED FROM SAMBANDHI SORTING",
	            "RECEIVED FROM SORTING SEED",
	            "RECEIVED FROM CLEANING",
	            "RECEIVED FROM CLEANING PROCESS",
	            "RECEIVED FROM CLEANED SEED",
	            "RECEIVED FROM BREAKING PROCESS","RECEIVED FROM PROCESS",  "RECEIVED FROM PRODUCTION", "RECEIVED FROM CRUSHING", "RECEIVED OIL FROM RGS CT CRUSHING", "RECEIVED CAKE FROM RGS CT CRUSHING", "NADAPPU GN OIL CAKE", "RECEIVED OIL CAKE",
	            "OIL CAKE RECEIVED FROM CRUSHING", "RECEIVED FROM OIL PRODUCTION IN KG"
	    )) {
	        return StockFlowType.PRODUCTION_IN;
	    }
	    
	    if (equalsAny(name,
	            "ISSUE TO SAND CLEANING",
	            "ISSUE TO SAMBANDHI SEED CLEANING",
	            "ISSUE TO CLEANING",
	            "ISSUED TO CLEANING",
	            "ISSUE TO YARD DRYING ",
	            "ISSUED TO YARD DRIYING",
	            "ISSUED TO SORTING",
	            "ISSUE TO SORTING",
	            "ISSUED TO SAMBANDHI SORTING",
	            "DRAYAGE",
	            "DRYAGE",
	            "CAKE DRAYAGE",
	            "GROUNDNUT POD DRYAGE",
	            "SESAME OIL CAKE DRAYAGE",
	            "ISSUED TO DRYING",
	            "GROUNDNUT DRYAGE",
	            "ISSUE TO CRUSHING ",
	            "SEND TO CRUSHING",
	            "ISSUED TO PRODUCTION AS OIL",
	            "ISSUED TO PRODUCTION AS SEED",
	            "ISSUE TO PRODUCTION(Cake)",
	            "ISSUE TO PRODUCTION(Oil)",
	            "ISSUE TO PRODUCTION",
	            "ISSUED TO PRODUCTION",
	            "SEED TAKEN FOR CRUSHING",
	            "ISSUED TO CHIKKI PROCESS",
	            "ISSUE TO CHICKKI PROCESS",
	            "ADD JAGGERY",
	            "ISSUED JAGGERY TO CRUSHING",
	            "ISSUE TO OIL CAKE RECRUSHING",
	            "ISSUED TO BREAKING",
	            "ISSUED TO BRIEQUETTING"
	    )) {
	        return StockFlowType.PRODUCTION_OUT;
	    }
	    
	    if (equalsAny(name,
	            "SALES", "DESPATCH", "ISSUED TO SALES", "2NDS OIL SALES")) {
	        return StockFlowType.SALES;
	    }

	    if ("SALES RETURN".equalsIgnoreCase(name))
	        return StockFlowType.SALES_RETURN;

	    if (equalsAny(name,
	            "PURCHASE RETURN", "BRANCH RETURN",
	            "PARTY RETURN", "ISSUE TO VENDOR")) {
	        return StockFlowType.PURCHASE_RETURN;
	    }

	    if (equalsAny(name,
	            "CLOSING STOCK", "CLOSING BALANCE")) {
	        return StockFlowType.CLOSING;
	    }
	    
	    if (equalsAny(name,
	            "RECEIVED FROM OTHER PLANT", "RECEIVED FROM OTHER PLANTS")) {
	        return StockFlowType.INTER_RECEIPT;
	    }
	    
	    if (equalsAny(name,
	            "TOTAL ISSUE", "TOTAL ISSUES")) {
	        return StockFlowType.TOTAL_ISSUE;
	    }
	    
	    if ("TOTAL RECEIPTS".equalsIgnoreCase(name))
	        return StockFlowType.TOTAL_RECEIPT;
	    
	    if ("SUB TOTAL".equalsIgnoreCase(name))
	        return StockFlowType.SUB_TOTAL;
	    
	    if (equalsAny(name,
	    		"ISSUE TO PACKING","ISSUED TO PACKING", "ISSUE TO REPACKING", "ISSUED FOR PACKING" )) {
	        return StockFlowType.ISSUE_PACKING;
	    }
	    
	    if (equalsAny(name,
	            "ISSUED TO OTHER PLANTS", "ISSUED TO OTHER PLANT", "ISSUE TO OTHER PLANTS")) {
	        
	        return StockFlowType.INTER_ISSUE;
	    }
	    
	    if (equalsAny(name, "RECEIVED FROM PACKED", "RECEIVED FROM PACKING PROCESS", "50 KG CAKE BAG", "CAKE PACKED", "2NDS CAKE PACKED", "RECEIVED FROM PACKING" )) {
	        return StockFlowType.RECEIPT_PACKING;
	    }
	     return StockFlowType.IGNORE;
	}
	
	
	
	
	private boolean equalsAny(String source, String... values) {
		String src = normalize(source);  
	    for (String v : values) {
	        if (v.equalsIgnoreCase(src)) {
	            return true;
	        }
	    }
	    return false;
	}
	
	private String normalize(String s) {
	    if (s == null) return "";
	    return s
	            .toUpperCase()
	            .trim()
	            .replaceAll("\\s+", " ");
	}
	
	// JOURNAL OR PRODUCT ID METHOD	
	private QtyProductJournalDto resolveProductIds(Long menuId, Long sub1Id, Long itemId) {
		QtyProductJournalDto dto= new QtyProductJournalDto();
		List<Long> ids = qtySjidRepository
		            .findBySjidAndParentAndCreatedBy(menuId, sub1Id, itemId);
		    if ( ids != null && !ids.isEmpty()) {
		    dto.setJournalId(ids);
		    }
		    else {
		    	 dto.setJournalId(null);
		    }
		   List<QtyPfList> pIds = qtyRefidRepository.findByRefIdAndParentAndCreatedBy(menuId, sub1Id, itemId);
		   if ( pIds != null && !pIds.isEmpty()) {
			   List<Long> productIds = pIds.stream()
				        .map(QtyPfList::getRefid).collect(Collectors.toList());

				boolean pfAvailable = pIds.stream()
				        .anyMatch(x -> x.getPf() != null && !x.getPf().trim().isEmpty()); 
				dto.setProductId(productIds);
				dto.setPfKey(pfAvailable);
		   }
		   else {
			   dto.setProductId(null);
			   dto.setPfKey(false);
		   }
		   
		   return dto;
	}
	
	private List<StockQtyWithLot> fetchStockQtySjid(
	        StockFlowType type,
	        Long branch,
	        List<Long> sjId,
	        List<Long> refId, Date startDate,
	        Date endDate,
	        Long firmId) {

	    switch (type) {

	        case OPENING:
	            return binstockAuditLogRepository.findBYRefIdAndSjidWithLot(branch, "OPENING",refId, startDate, endDate, firmId);

	        case PURCHASE:
	        	 return stBinRepository.findByRefidQtyWithLotSjid(branch, "Stock Inward",sjId,refId, startDate, endDate, firmId);

	        case EXCESS:
	        	return stBinRepository.findByRefidQtyWithLotSjid(branch, "Excess",sjId,refId, startDate, endDate, firmId);
	       
	        case SCRAP:
	        	return stBinRepository.findByRefidQtyWithLotSjid(branch, "Scrap",sjId,refId, startDate, endDate, firmId);
	       
	        case STOCK:
	        	return stBinRepository.findByRefidQtyWithLotSjid(branch, "Stock",sjId,refId, startDate, endDate, firmId);
	       
	        case SHORTAGE:
	        	 return stBinRepository.findByRefidQtyWithLotSjid(branch, "Shortage",sjId,refId, startDate, endDate, firmId);
	       
	        case SALES:
	        	return stBinRepository.findBYRecRefWithLotSjid(branch, "DISPATCH", "I",null, sjId,refId, startDate, endDate, firmId);
	        
	        case PRODUCTION_IN:
	        	return stBinRepository.findBYRecRefWithLotSjid(branch, "PRODUCTION", "R",null, sjId,refId, startDate, endDate, firmId);
	        
	        case PRODUCTION_OUT:
	        	return stBinRepository.findBYRecRefWithLotSjid(branch, "PRODUCTION", "I",null, sjId,refId, startDate, endDate, firmId);
	        
	        	
	        case SALES_RETURN:
	        	return stBinRepository.findBYRecRefWithLotSjid(branch, "SALES_RETURN", "I",null, sjId,refId, startDate, endDate, firmId);
	        
	        case PURCHASE_RETURN:
	        	return stBinRepository.findBYRecRefWithLotSjid(branch, "PurchaseReturn", "I",null, sjId,refId, startDate, endDate, firmId);
	        
	        case ISSUE_PACKING:
	        	return stBinRepository.findBYRecRefWithLotSjid(branch, "PRODUCTION", "I",10L, sjId,refId, startDate, endDate, firmId);
	        
	        case RECEIPT_PACKING:
	        	return stBinRepository.findBYRecRefWithLotSjid(branch, "PRODUCTION", "R",null, sjId,refId, startDate, endDate, firmId);
	        
	        case INTER_RECEIPT:
	        	return stBinRepository.findBYInterRefidWithLotSjid(branch, "MATERIAL_TRANSACTION", "R", sjId,refId, startDate, endDate, firmId);
	        
	        case INTER_ISSUE:
	        	return stBinRepository.findBYInterRefidWithLotSjid(branch, "MATERIAL_TRANSACTION", "I", sjId,refId, startDate, endDate, firmId);
	        
	        case TOTAL_ISSUE:
	        	return stBinRepository.findBYTotalRefIdWithLotsSjid(branch,  "I", sjId,refId, startDate, endDate, firmId);
	        
	        case TOTAL_RECEIPT:
		        	return stBinRepository.findBYTotalRefIdWithLotsSjid(branch,  "R", sjId,refId, startDate, endDate, firmId);
		        
	        case CLOSING:
	            return binstockAuditLogRepository.findBYRefIdAndSjidWithLot(branch, "CLOSING",refId, startDate, endDate, firmId);

	        default:
	            return Collections.emptyList();
	    }
	}
	
	private List<StockQtyWithLot> fetchStockQtyref(
	        StockFlowType type,
	        Long branch,
	       List<Long> refId, Date startDate,
	        Date endDate,
	        Long firmId) {

	    switch (type) {

	        case OPENING:
	            return binstockAuditLogRepository.findBYRefIdAndSjidWithLot(branch, "OPENING",refId, startDate, endDate, firmId);

	        case PURCHASE:
	        	 return stBinRepository.findByRefidQtyWithLot(branch, "Stock Inward",refId, startDate, endDate, firmId);

	        case EXCESS:
	        	return stBinRepository.findByRefidQtyWithLot(branch, "Excess",refId, startDate, endDate, firmId);
	       
	        case SCRAP:
	        	return stBinRepository.findByRefidQtyWithLot(branch, "Scrap",refId, startDate, endDate, firmId);
	       
	        case STOCK:
	        	return stBinRepository.findByRefidQtyWithLot(branch, "Stock",refId, startDate, endDate, firmId);
	       
	        case SHORTAGE:
	        	 return stBinRepository.findByRefidQtyWithLot(branch, "Shortage",refId, startDate, endDate, firmId);
	       
	        case SALES:
	        	return stBinRepository.findBYRecRefWithLot(branch, "DISPATCH", "I",null,refId, startDate, endDate, firmId);
	        
	        case PRODUCTION_IN:
	        	return stBinRepository.findBYRecRefWithLot(branch, "PRODUCTION", "R",null,refId, startDate, endDate, firmId);
	        
	        case PRODUCTION_OUT:
	        	return stBinRepository.findBYRecRefWithLot(branch, "PRODUCTION", "I",null,refId, startDate, endDate, firmId);
	        
	        	
	        case SALES_RETURN:
	        	return stBinRepository.findBYRecRefWithLot(branch, "SALES_RETURN", "I",null,refId, startDate, endDate, firmId);
	        
	        case PURCHASE_RETURN:
	        	return stBinRepository.findBYRecRefWithLot(branch, "PurchaseReturn", "I",null,refId, startDate, endDate, firmId);
	        
	        case ISSUE_PACKING:
	        	return stBinRepository.findBYRecRefWithLot(branch, "PRODUCTION", "I",10L,refId, startDate, endDate, firmId);
	        
	        case RECEIPT_PACKING:
	        	return stBinRepository.findBYRecRefWithLot(branch, "PRODUCTION", "R",null,refId, startDate, endDate, firmId);
	        
	        case INTER_RECEIPT:
	        	return stBinRepository.findBYInterRefidWithLot(branch, "MATERIAL_TRANSACTION", "R", refId, startDate, endDate, firmId);
	        
	        case INTER_ISSUE:
	        	return stBinRepository.findBYInterRefidWithLot(branch, "MATERIAL_TRANSACTION", "I",refId, startDate, endDate, firmId);
	        
	        case TOTAL_ISSUE:
	        	return stBinRepository.findBYTotalRefIdWithLots(branch,  "I",refId, startDate, endDate, firmId);
	        
	        case TOTAL_RECEIPT:
		        	return stBinRepository.findBYTotalRefIdWithLots(branch,  "R",refId, startDate, endDate, firmId);
		        
	        case CLOSING:
	            return binstockAuditLogRepository.findBYRefIdAndSjidWithLot(branch, "CLOSING",refId, startDate, endDate, firmId);

	        default:
	            return Collections.emptyList();
	    }
	}

	private void updateHeaderDetails(QtyStamentResponseDto finalResult, Long firmId, Long branch) {
		 CompanyList companyList = organisationRepository.findByFirm(firmId);
	        if (companyList != null) {
	        	finalResult.setFirmDetails(new Company(companyList));
	        }
	        BranchAddressList branchAddressList1 = branchRepository.getBranchId(branch);
	        if (branchAddressList1 != null) {
	        	finalResult.setPlantDetails(new BranchAddress(branchAddressList1));
	        }
	}

	private List<StockQtyWithLot> fetchStockQtySjidPf(
	        StockFlowType type,
	        Long branch,
	        List<Long> sjId,
	        List<Long> refId, Date startDate,
	        Date endDate,
	        Long firmId) {

	    switch (type) {

	        case OPENING:
	            return binstockAuditLogRepository.findBYRefIdAndSjidWithLotPf(branch, "OPENING",refId, startDate, endDate, firmId);

	        case PURCHASE:
	        	 return stBinRepository.findByRefidQtyWithLotSjidPf(branch, "Stock Inward",sjId,refId, startDate, endDate, firmId);

	        case EXCESS:
	        	return stBinRepository.findByRefidQtyWithLotSjidPf(branch, "Excess",sjId,refId, startDate, endDate, firmId);
	       
	        case SCRAP:
	        	return stBinRepository.findByRefidQtyWithLotSjidPf(branch, "Scrap",sjId,refId, startDate, endDate, firmId);
	       
	        case STOCK:
	        	return stBinRepository.findByRefidQtyWithLotSjidPf(branch, "Stock",sjId,refId, startDate, endDate, firmId);
	       
	        case SHORTAGE:
	        	 return stBinRepository.findByRefidQtyWithLotSjidPf(branch, "Shortage",sjId,refId, startDate, endDate, firmId);
	       
	        case SALES:
	        	return stBinRepository.findBYRecRefWithLotSjidPf(branch, "DISPATCH", "I",null, sjId,refId, startDate, endDate, firmId);
	        
	        case PRODUCTION_IN:
	        	return stBinRepository.findBYRecRefWithLotSjidPf(branch, "PRODUCTION", "R",null, sjId,refId, startDate, endDate, firmId);
	        
	        case PRODUCTION_OUT:
	        	return stBinRepository.findBYRecRefWithLotSjidPf(branch, "PRODUCTION", "I",null, sjId,refId, startDate, endDate, firmId);
	        
	        	
	        case SALES_RETURN:
	        	return stBinRepository.findBYRecRefWithLotSjidPf(branch, "SALES_RETURN", "I",null, sjId,refId, startDate, endDate, firmId);
	        
	        case PURCHASE_RETURN:
	        	return stBinRepository.findBYRecRefWithLotSjidPf(branch, "PurchaseReturn", "I",null, sjId,refId, startDate, endDate, firmId);
	        
	        case ISSUE_PACKING:
	        	return stBinRepository.findBYRecRefWithLotSjidPf(branch, "PRODUCTION", "I",10L, sjId,refId, startDate, endDate, firmId);
	        
	        case RECEIPT_PACKING:
	        	return stBinRepository.findBYRecRefWithLotSjidPf(branch, "PRODUCTION", "R",null, sjId,refId, startDate, endDate, firmId);
	        
	        case INTER_RECEIPT:
	        	return stBinRepository.findBYInterRefidWithLotSjidPf(branch, "MATERIAL_TRANSACTION", "R", sjId,refId, startDate, endDate, firmId);
	        
	        case INTER_ISSUE:
	        	return stBinRepository.findBYInterRefidWithLotSjidPf(branch, "MATERIAL_TRANSACTION", "I", sjId,refId, startDate, endDate, firmId);
	        
	        case TOTAL_ISSUE:
	        	return stBinRepository.findBYTotalRefIdWithLotsSjidPf(branch,  "I", sjId,refId, startDate, endDate, firmId);
	        
	        case TOTAL_RECEIPT:
		        	return stBinRepository.findBYTotalRefIdWithLotsSjidPf(branch,  "R", sjId,refId, startDate, endDate, firmId);
		        
	        case CLOSING:
	            return binstockAuditLogRepository.findBYRefIdAndSjidWithLotPf(branch, "CLOSING",refId, startDate, endDate, firmId);

	        default:
	            return Collections.emptyList();
	    }
	}
	private List<StockQtyWithLot> fetchStockQtyrefPf(
	        StockFlowType type,
	        Long branch,
	       List<Long> refId, Date startDate,
	        Date endDate,
	        Long firmId) {

	    switch (type) {

	        case OPENING:
	            return binstockAuditLogRepository.findBYRefIdAndSjidWithLotPf(branch, "OPENING",refId, startDate, endDate, firmId);

	        case PURCHASE:
	        	 return stBinRepository.findByRefidQtyWithLotPf(branch, "Stock Inward",refId, startDate, endDate, firmId);

	        case EXCESS:
	        	return stBinRepository.findByRefidQtyWithLotPf(branch, "Excess",refId, startDate, endDate, firmId);
	       
	        case SCRAP:
	        	return stBinRepository.findByRefidQtyWithLotPf(branch, "Scrap",refId, startDate, endDate, firmId);
	       
	        case STOCK:
	        	return stBinRepository.findByRefidQtyWithLotPf(branch, "Stock",refId, startDate, endDate, firmId);
	       
	        case SHORTAGE:
	        	 return stBinRepository.findByRefidQtyWithLotPf(branch, "Shortage",refId, startDate, endDate, firmId);
	       
	        case SALES:
	        	return stBinRepository.findBYRecRefWithLotPf(branch, "DISPATCH", "I",null,refId, startDate, endDate, firmId);
	        
	        case PRODUCTION_IN:
	        	return stBinRepository.findBYRecRefWithLotPf(branch, "PRODUCTION", "R",null,refId, startDate, endDate, firmId);
	        
	        case PRODUCTION_OUT:
	        	return stBinRepository.findBYRecRefWithLotPf(branch, "PRODUCTION", "I",null,refId, startDate, endDate, firmId);
	        
	        	
	        case SALES_RETURN:
	        	return stBinRepository.findBYRecRefWithLotPf(branch, "SALES_RETURN", "I",null,refId, startDate, endDate, firmId);
	        
	        case PURCHASE_RETURN:
	        	return stBinRepository.findBYRecRefWithLotPf(branch, "PurchaseReturn", "I",null,refId, startDate, endDate, firmId);
	        
	        case ISSUE_PACKING:
	        	return stBinRepository.findBYRecRefWithLotPf(branch, "PRODUCTION", "I",10L,refId, startDate, endDate, firmId);
	        
	        case RECEIPT_PACKING:
	        	return stBinRepository.findBYRecRefWithLotPf(branch, "PRODUCTION", "R",null,refId, startDate, endDate, firmId);
	        
	        case INTER_RECEIPT:
	        	return stBinRepository.findBYInterRefidWithLotPf(branch, "MATERIAL_TRANSACTION", "R", refId, startDate, endDate, firmId);
	        
	        case INTER_ISSUE:
	        	return stBinRepository.findBYInterRefidWithLotPf(branch, "MATERIAL_TRANSACTION", "I",refId, startDate, endDate, firmId);
	        
	        case TOTAL_ISSUE:
	        	return stBinRepository.findBYTotalRefIdWithLotsPf(branch,  "I",refId, startDate, endDate, firmId);
	        
	        case TOTAL_RECEIPT:
		        	return stBinRepository.findBYTotalRefIdWithLotsPf(branch,  "R",refId, startDate, endDate, firmId);
		        
	        case CLOSING:
	            return binstockAuditLogRepository.findBYRefIdAndSjidWithLotPf(branch, "CLOSING",refId, startDate, endDate, firmId);

	        default:
	            return Collections.emptyList();
	    }
	}
}
