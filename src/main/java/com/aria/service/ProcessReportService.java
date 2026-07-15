package com.aria.service;


import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProcessReportService {

    @Autowired
    ProcessReportNameRepository reportNameRepository;

    @Autowired
    ProcessReportTitleRepository reportTitleRepository;

    @Autowired
    ProcessReportJournalGroupRepository reportJournalGroupRepository;

    @Autowired
    ProcessReportProductGroupRepository reportProductGroupRepository;

    @Autowired
    ProcessReportGroupRepository reportGroupRepository;

    @Autowired
    ProcessReportProductRepository reportProductRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    BinStockLogRepository stbinLogRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    BinstockAuditLogRepository binstockAuditLogRepository;


    public ResponseEntity<List<ProcessReportName>> getProcessReportNameList(String name) {
        return new ResponseEntity<>(reportNameRepository.findAll(name), HttpStatus.OK);
    }


    public ResponseEntity<ProcessReportResult> processReport(User loggeduser, Long branch, Long reportNameId, String fromDate, String toDate) {

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

        ProcessReportResult result = new ProcessReportResult();


        CompanyList companyList = organisationRepository.findByFirm(loggeduser.getOrg_id());
        if (companyList != null) {
            result.setFirmDetails(new Company(companyList));
        }
        BranchAddressList branchAddressList1 = branchRepository.getBranchId(branch);
        if (branchAddressList1 != null) {
            result.setPlantDetails(new BranchAddress(branchAddressList1));
        }

        List<ProcessReportGroup> processReportGroups = reportGroupRepository.findBy(reportNameId);

        List<ProcessReportGroupDTO> processReportGroupDTOS = processReportGroups.stream().map(reportTable -> {

            ProcessReportGroupDTO processReportGroupDTO = new ProcessReportGroupDTO(reportTable);

            List<ProcessReportProductGroup> processReportProductGroups = reportProductGroupRepository.findBy(processReportGroupDTO.getPrid(), processReportGroupDTO.getId());

            List<ProcessReportProductGroupDTO> productGroupDTOS = processReportProductGroups.stream().map(productGroup -> {
                ProcessReportProductGroupDTO productGroupDTO = new ProcessReportProductGroupDTO(productGroup);
                productGroupDTO.setProductGroupDTOS(getProductWithLotlist( processReportGroupDTO, productGroupDTO, startDate, endDate));
                productGroupDTO.setMenuDTOList(getProcessReportMenuList(branch, processReportGroupDTO, productGroupDTO, startDate, endDate));
                return productGroupDTO;
            }).collect(Collectors.toList());
            processReportGroupDTO.setProductGroupDTOS(productGroupDTOS);

            return processReportGroupDTO;
        }).collect(Collectors.toList());
        result.setTableDataList(processReportGroupDTOS);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    public List<ProcessReportProductDTO> processReportProductGrouplist(ProcessReportGroupDTO processReportGroupDTO, ProcessReportProductGroupDTO productGroupDTO, Date startDate, Date endDate) {
        List<ProcessReportProductDTO> result = null;
        List<ProcessReportProductList> reportProducts = reportProductRepository.findBy(processReportGroupDTO.getPrid(), processReportGroupDTO.getId(), productGroupDTO.getId());
        result = reportProducts.stream().map(data -> {
            ProcessReportProductDTO reportProductDTO = new ProcessReportProductDTO(data);
            reportProductDTO.setLots(stbinLogRepository.findByLotList(reportProductDTO.productId, startDate, endDate));
            return reportProductDTO;
        }).collect(Collectors.toList());
        return result;
    }


    public List<ProcessReportProductDTO> getProductWithLotlist(ProcessReportGroupDTO processReportGroupDTO, ProcessReportProductGroupDTO productGroupDTO, Date startDate, Date endDate) {
        List<ProcessReportProductDTO> result = null;
        List<ProcessReportProductList> reportProducts = reportProductRepository.findBy(processReportGroupDTO.getPrid(), processReportGroupDTO.getId(), productGroupDTO.getId());
        result = reportProducts.stream().map(data -> {
            ProcessReportProductDTO reportProductDTO = new ProcessReportProductDTO(data);
            reportProductDTO.setLots(stbinLogRepository.findByLotList(reportProductDTO.productId, startDate, endDate));
            return reportProductDTO;
        }).collect(Collectors.toList());
        return result;
    }


    public List<ProcessReportMenuDTO> getProcessReportMenuList(Long branch, ProcessReportGroupDTO processReportGroupDTO, ProcessReportProductGroupDTO productGroupDTO, Date startDate, Date endDate) {
        List<ProcessReportMenuDTO> result = null;
        List<ProcessReportTitle> processReportTitles = reportTitleRepository.findBy(processReportGroupDTO.getPrid(), processReportGroupDTO.getId());
        result = processReportTitles.stream().map(data -> {
            ProcessReportMenuDTO reportProductDTO = new ProcessReportMenuDTO(data);
            reportProductDTO.setProductStock(getProductWithStock(branch , processReportGroupDTO, productGroupDTO, reportProductDTO, startDate, endDate));
            return reportProductDTO;
        }).collect(Collectors.toList());
        return result;
    }


    public List<ProcessReportProductDTO> getProductWithStock(Long branch, ProcessReportGroupDTO processReportGroupDTO, ProcessReportProductGroupDTO productGroupDTO, ProcessReportMenuDTO reportMenuDTO, Date startDate, Date endDate) {
        List<ProcessReportProductDTO> result = null;
        List<ProcessReportProductList> reportProducts = reportProductRepository.findBy(processReportGroupDTO.getPrid(), processReportGroupDTO.getId(), productGroupDTO.getId());
        result = reportProducts.stream().map(data -> {
            ProcessReportProductDTO reportProductDTO = new ProcessReportProductDTO(data);
            reportProductDTO.setLots(stbinLogRepository.findByLotList(reportProductDTO.getProductId(), startDate, endDate));
            updateProductWithStockDetails(branch ,  reportProductDTO, processReportGroupDTO, reportMenuDTO, startDate, endDate);
            return reportProductDTO;
        }).collect(Collectors.toList());
        return result;
    }


    public void updateProductWithStockDetails(Long branch, ProcessReportProductDTO reportProductDTO, ProcessReportGroupDTO processReportGroupDTO, ProcessReportMenuDTO reportMenuDTO, Date startDate, Date endDate) {

        System.out.println(" ==============");
        System.out.println(" Branch = "+branch);
        System.out.println(" StartDate = "+startDate);
        System.out.println(" EndDate = "+endDate);
        System.out.println(" DispName = "+reportMenuDTO.getDispName());
        System.out.println(" Title = "+reportMenuDTO.getTitle());
        System.out.println(" itemName = "+reportProductDTO.getProductName());
        System.out.println(" itemId = "+reportProductDTO.getProductId());
        System.out.println(" ==============");
        if (!reportProductDTO.getLots().isEmpty()) {
            if (reportMenuDTO.getDispName().equalsIgnoreCase("PRODUCTION")) {
                List<ProcessReportProductLotWithStockList> stockList = stbinLogRepository.getStock(branch , processReportGroupDTO.getPrid(), processReportGroupDTO.getId(), reportMenuDTO.getId(), reportProductDTO.getProductId(), reportProductDTO.getLots(), reportMenuDTO.getTrnType(), startDate, endDate, "PRODUCTION");
                reportProductDTO.setLotWithStocks(stockList.stream().map(ProcessReportProductLotWithStock::new).collect(Collectors.toList()));
            } else if (reportMenuDTO.getDispName().equalsIgnoreCase("SALES_RETURN")) {
                List<ProcessReportProductLotWithStockList> stockList = stbinLogRepository.getStock(branch, reportProductDTO.getProductId(), reportProductDTO.getLots(), startDate, endDate, "SALES_RETURN");
                reportProductDTO.setLotWithStocks(stockList.stream().map(ProcessReportProductLotWithStock::new).collect(Collectors.toList()));
            } else if (reportMenuDTO.getDispName().equalsIgnoreCase("OPENING")) {
                List<ProcessReportProductLotWithStockList> stockList = binstockAuditLogRepository.findBYOpeningStock( branch , reportProductDTO.getProductId(), reportProductDTO.getLots(),  startDate);
                reportProductDTO.setLotWithStocks(stockList.stream().map(ProcessReportProductLotWithStock::new).collect(Collectors.toList()));
            } else if (reportMenuDTO.getDispName().equalsIgnoreCase("CLOSING")) {
                List<ProcessReportProductLotWithStockList> stockList = binstockAuditLogRepository.findBYClosingStock(branch , reportProductDTO.getProductId(), reportProductDTO.getLots(),  endDate );
                reportProductDTO.setLotWithStocks(stockList.stream().map(ProcessReportProductLotWithStock::new).collect(Collectors.toList()));
            } else if (reportMenuDTO.getDispName().equalsIgnoreCase("EXCESS")) {
                System.out.println("####### Check With Excess ########");
                List<ProcessReportProductLotWithStockList> stockList = stbinLogRepository.getStock(branch, reportProductDTO.getProductId(), reportProductDTO.getLots(), startDate, endDate, "Excess");
                reportProductDTO.setLotWithStocks(stockList.stream().map(ProcessReportProductLotWithStock::new).collect(Collectors.toList()));
            } else if (reportMenuDTO.getDispName().equalsIgnoreCase("SHORTAGE")) {
                List<ProcessReportProductLotWithStockList> stockList = stbinLogRepository.getStock(branch, reportProductDTO.getProductId(), reportProductDTO.getLots(), startDate, endDate, "Shortage");
                reportProductDTO.setLotWithStocks(stockList.stream().map(ProcessReportProductLotWithStock::new).collect(Collectors.toList()));
            }else if (reportMenuDTO.getDispName().equalsIgnoreCase("PURCHASERETURN")) {
                List<ProcessReportProductLotWithStockList> stockList = stbinLogRepository.getStock(branch, reportProductDTO.getProductId(), reportProductDTO.getLots(), startDate, endDate, "PurchaseReturn");
                reportProductDTO.setLotWithStocks(stockList.stream().map(ProcessReportProductLotWithStock::new).collect(Collectors.toList()));
            }else if (reportMenuDTO.getDispName().equalsIgnoreCase("STOCKINWARD")) {
                List<ProcessReportProductLotWithStockList> stockList = stbinLogRepository.getStock(branch, reportProductDTO.getProductId(), reportProductDTO.getLots(), startDate, endDate, "Stock Inward");
                reportProductDTO.setLotWithStocks(stockList.stream().map(ProcessReportProductLotWithStock::new).collect(Collectors.toList()));
            }else if (reportMenuDTO.getDispName().equalsIgnoreCase("MATERIALTRANSACTION")) {
            	if(reportMenuDTO.getTrnType().equalsIgnoreCase("I")) {
                List<ProcessReportProductLotWithStockList> stockList = stbinLogRepository.getStockIssueByPlant(branch, reportProductDTO.getProductId(), reportProductDTO.getLots(), reportMenuDTO.getTrnType(), startDate, endDate, "MATERIAL_TRANSACTION");
                reportProductDTO.setLotWithStocks(stockList.stream().map(ProcessReportProductLotWithStock::new).collect(Collectors.toList()));
            	}else if(reportMenuDTO.getTrnType().equalsIgnoreCase("R")) {
                        List<ProcessReportProductLotWithStockList> stockList = stbinLogRepository.getStockReceiptByPlant(branch, reportProductDTO.getProductId(), reportProductDTO.getLots(), reportMenuDTO.getTrnType(), startDate, endDate, "MATERIAL_TRANSACTION");
                        reportProductDTO.setLotWithStocks(stockList.stream().map(ProcessReportProductLotWithStock::new).collect(Collectors.toList()));
                    	}}else if (reportMenuDTO.getDispName().equalsIgnoreCase("Sales")) {
                List<ProcessReportProductLotWithStockList> stockList = stbinLogRepository.getStock(branch, reportProductDTO.getProductId(), reportProductDTO.getLots(), startDate, endDate, "DISPATCH");
                reportProductDTO.setLotWithStocks(stockList.stream().map(ProcessReportProductLotWithStock::new).collect(Collectors.toList()));
            }else if (reportMenuDTO.getDispName().equalsIgnoreCase("Stock")) {
                List<ProcessReportProductLotWithStockList> stockList = stbinLogRepository.getStock(branch, reportProductDTO.getProductId(), reportProductDTO.getLots(), startDate, endDate, "Stock");
                reportProductDTO.setLotWithStocks(stockList.stream().map(ProcessReportProductLotWithStock::new).collect(Collectors.toList()));
            }else if (reportMenuDTO.getDispName().equalsIgnoreCase("Scrap")) {
                List<ProcessReportProductLotWithStockList> stockList = stbinLogRepository.getStock(branch, reportProductDTO.getProductId(), reportProductDTO.getLots(), startDate, endDate, "Scrap");
                reportProductDTO.setLotWithStocks(stockList.stream().map(ProcessReportProductLotWithStock::new).collect(Collectors.toList()));
            }else if (reportMenuDTO.getDispName().equalsIgnoreCase("MATERIALT_RECEIVE")) {
                List<ProcessReportProductLotWithStockList> stockList = stbinLogRepository.getStockReceiptByPlant(branch, reportProductDTO.getProductId(), reportProductDTO.getLots(), reportMenuDTO.getTrnType(), startDate, endDate, "MATERIAL_TRANSACTION");
                reportProductDTO.setLotWithStocks(stockList.stream().map(ProcessReportProductLotWithStock::new).collect(Collectors.toList()));
            }else if (reportMenuDTO.getDispName().equalsIgnoreCase("MATERIAL_ISSUE")) {
                List<ProcessReportProductLotWithStockList> stockList = stbinLogRepository.getStockIssueByPlant(branch, reportProductDTO.getProductId(), reportProductDTO.getLots(), reportMenuDTO.getTrnType(), startDate, endDate, "MATERIAL_TRANSACTION");
                reportProductDTO.setLotWithStocks(stockList.stream().map(ProcessReportProductLotWithStock::new).collect(Collectors.toList()));
            }
        }


    }
}
