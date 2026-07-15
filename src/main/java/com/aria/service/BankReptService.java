package com.aria.service;



import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.internal.build.AllowSysOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.aria.dao.BankReptRepository;
import com.aria.dao.BinStockLogRepository;
import com.aria.dao.BinstockAuditLogRepository;
import com.aria.dao.BranchRepository;
import com.aria.dao.OrganisationRepository;
import com.aria.dto.BankReptDto;
import com.aria.dto.BankSummaryProductDTO;
import com.aria.dto.BranchAddress;
import com.aria.dto.BranchAddressList;
import com.aria.dto.Company;
import com.aria.dto.CompanyList;
import com.aria.dto.GoDownDetailsForBankDto;
import com.aria.dto.GoDownDetailsForBankList;
import com.aria.dto.GoDownSummaryList;
import com.aria.dto.LotWiseGodownDto;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.utils.DateUtil;

@Service
public class BankReptService {
	
	@Autowired
	BankReptRepository bankReptRepository;
	
	@Autowired
	BranchRepository branchRepository;

	@Autowired
	OrganisationRepository organisationRepository;
	
	@Autowired
	BinStockLogRepository stBinRepository;
	
	@Autowired
    BinstockAuditLogRepository binstockAuditLogRepository;

	@Autowired
	DateUtil dateUtil;

	
	
	public void updateHeaderDetails(BankReptDto result, Long firmId, Long branch ,Long year , Long month) {

        CompanyList companyList = organisationRepository.findByFirm(firmId);
        if (companyList != null) {
            result.setFirmDetails(new Company(companyList));
        }
        BranchAddressList branchAddressList1 = branchRepository.getBranchId(branch);
        if (branchAddressList1 != null) {
            result.setPlantDetails(new BranchAddress(branchAddressList1));
        }
        result.setMonth(month);
        result.setYear(year);
    }

	
	
	
	public ResponseEntity<?> bankReport(
	        User loggedUser,
	        Long branch,
	        Long firmId,
	        Long year,
	        Long month,
	        String fromDate,
	        String toDate) {
		
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

	    Long branchId = (branch != null && branch == 0L) ? null : branch;

	    boolean isMonthFilter = (year != null && month != null);
	    boolean isDateFilter = (startDate != null && endDate != null);

	    if (!isMonthFilter && !isDateFilter) {
	        return new ResponseEntity<>(
	                new Status("400", "Either Year & Month OR FromDate & ToDate required"),
	                HttpStatus.BAD_REQUEST);
	    }

	    List<GoDownSummaryList> productList = bankReptRepository.findByProducts();

	    if (productList.isEmpty()) {
	        return new ResponseEntity<>(
	                new Status("404", "Record Not Exists"),
	                HttpStatus.NOT_FOUND);
	    }

	    BankReptDto finalResult = new BankReptDto();
	    updateHeaderDetails(finalResult, firmId, branchId, year, month);

	    List<BankSummaryProductDTO> productDTOList = new ArrayList<>();

	    for (GoDownSummaryList product : productList) {

	        BankSummaryProductDTO productDTO = new BankSummaryProductDTO();
	        productDTO.setItemName(product.getItemName());

	        List<LotWiseGodownDto> lotWiseList = new ArrayList<>();

	        // 🔥 CASE 1: ALL BRANCHES
	        if (branchId == null) {

	            List<Object[]> branchLotList =
	                    isMonthFilter ?
	                            stBinRepository.findBranchLotList(product.getItemId(), year, month)
	                            :
	                            stBinRepository.findBranchLotListByDate(product.getItemId(), startDate, endDate);

	            for (Object[] row : branchLotList) {

	                Double lot = ((Number) row[0]).doubleValue();
	                Long br = ((Number) row[1]).longValue();
	                String brName = (String) row[2];

	                List<GoDownDetailsForBankList> processDetails =
	                        getTransactions(br, firmId, product.getItemId(),
	                                year, month, startDate, endDate, lot, isMonthFilter);

	                List<GoDownDetailsForBankDto> godownDtos =
	                        buildGodownDtos(processDetails, br, firmId,
	                                product.getItemId(), year, month,
	                                startDate, endDate, lot, isMonthFilter);

	                LotWiseGodownDto lotDto = new LotWiseGodownDto();
	                lotDto.setLot(lot);
	                lotDto.setGodowns(godownDtos);
	                lotDto.setBranch(br);
	                lotDto.setBrName(brName);

	                lotWiseList.add(lotDto);
	            }

	        }
	        // 🔥 CASE 2: SINGLE BRANCH
	        else {

	            List<Double> lots =
	                    isMonthFilter ?
	                            stBinRepository.findByListLot(product.getItemId(), year, month)
	                            :
	                            stBinRepository.findByListLotByDate(product.getItemId(), startDate, endDate);

	            for (Double lot : lots) {

	                List<GoDownDetailsForBankList> processDetails =
	                        getTransactions(branchId, firmId, product.getItemId(),
	                                year, month, startDate, endDate, lot, isMonthFilter);

	                List<GoDownDetailsForBankDto> godownDtos =
	                        buildGodownDtos(processDetails, branchId, firmId,
	                                product.getItemId(), year, month,
	                                startDate, endDate, lot, isMonthFilter);

	                LotWiseGodownDto lotDto = new LotWiseGodownDto();
	                lotDto.setLot(lot);
	                lotDto.setGodowns(godownDtos);
	                lotDto.setBranch(branchId);

	                lotWiseList.add(lotDto);
	            }
	        }

	        productDTO.setLots(lotWiseList);
	        productDTOList.add(productDTO);
	    }

	    finalResult.setProductDTOList(productDTOList);

	    return new ResponseEntity<>(finalResult, HttpStatus.OK);
	}
	
	
	
	private List<GoDownDetailsForBankList> getTransactions(
	        Long branchId,
	        Long firmId,
	        Long productId,
	        Long year,
	        Long month,
	        Date fromDate,
	        Date toDate,
	        Double lot,
	        boolean isMonthFilter) {

	    return isMonthFilter
	            ? stBinRepository.findTransactionsByProduct(
	                    branchId, firmId, productId, year, month, lot)
	            : stBinRepository.findTransactionsByProductByDate(
	                    branchId, firmId, productId, fromDate, toDate, lot);
	}
	
	
	private List<GoDownDetailsForBankDto> buildGodownDtos(
	        List<GoDownDetailsForBankList> godownList,
	        Long branchId,
	        Long firmId,
	        Long productId,
	        Long year,
	        Long month,
	        Date fromDate,
	        Date toDate,
	        Double lot,
	        boolean isMonthFilter) {

	    return godownList.stream().map(data -> {

	        GoDownDetailsForBankDto dto = new GoDownDetailsForBankDto();

	        dto.setId(data.getGodownId());
	        dto.setGName(data.getGname());
	        dto.setLot(lot);
	        dto.setBranch(branchId);

	        if (isMonthFilter) {

	            dto.setOpening(binstockAuditLogRepository.findBYRefIdWithQty(
	                    branchId, firmId, "OPENING", productId, year, month, lot, data.getGodownId()));

	            dto.setClosing(binstockAuditLogRepository.findBYRefIdWithQty(
	                    branchId, firmId, "CLOSING", productId, year, month, lot, data.getGodownId()));
	            dto.setInternalPlantIssue(
		                stBinRepository.findBYRefIdWithQtynterPlantIssue(
		                        branchId, firmId, "I", productId, year, month, lot, data.getGodownId()));

		        dto.setInternalPlantReceipt(
		                stBinRepository.findBYRefIdWithQtynterPlantReceipt(
		                        branchId, firmId, "R", productId, year, month, lot, data.getGodownId()));

		        dto.setDelivery(
		                stBinRepository.findBYRefIdWithQtyIssue(
		                        branchId, firmId, productId, year, month, lot, data.getGodownId()));

		        dto.setReceipts(
		                stBinRepository.findBYRefIdWithQtyReciept(
		                        branchId, firmId, "R", productId, year, month, lot, data.getGodownId()));

		        dto.setTotal(null);
	        } else {

	            dto.setOpening(binstockAuditLogRepository.findBYRefIdWithQtyByDate(
	                    branchId, firmId,"OPENING", productId, fromDate,toDate, lot, data.getGodownId()));

	            dto.setClosing(binstockAuditLogRepository.findBYRefIdWithQtyByDate(
	                    branchId, firmId,"CLOSING", productId,fromDate, toDate, lot, data.getGodownId()));
	            dto.setInternalPlantIssue(
		                stBinRepository.findBYRefIdWithQtynterPlantIssueByDate(
		                        branchId, firmId, "I", productId, fromDate, toDate, lot, data.getGodownId()));

		        dto.setInternalPlantReceipt(
		                stBinRepository.findBYRefIdWithQtynterPlantReceiptByDate(
		                        branchId, firmId, "R", productId, fromDate, toDate, lot, data.getGodownId()));

		        dto.setDelivery(
		                stBinRepository.findBYRefIdWithQtyIssueByDate(
		                        branchId, firmId, productId, fromDate, toDate, lot, data.getGodownId()));

		        dto.setReceipts(
		                stBinRepository.findBYRefIdWithQtyRecieptByDate(
		                        branchId, firmId, "R", productId, fromDate, toDate, lot, data.getGodownId()));

		        dto.setTotal(null);
	        }

	       

	        return dto;

	    }).collect(Collectors.toList());
	}
	
	
	
}
