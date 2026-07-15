package com.aria.service;


import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.*;

import org.hibernate.internal.build.AllowSysOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QcInProcessService {


    @Autowired
    QcInProcessRepository inProcessRepo;


    @Autowired
    AttributeFieldsProcessTypeOptionRepository processTypeRepository;

    @Autowired
    KpiRepository kpiRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    JournalEntryRepository journalEntryRepository;

    @Autowired
    CommonDao commonDao;

    @Autowired
    JournalRepository journalRepository;
    @Autowired
    BinStockRepository binStockRepository;
    @Autowired
    ProductionEntryRepository productionEntryRepository;
    @Autowired
    QcConsumptionSetupRepository qcConsumptionSetupRepository;
    @Autowired
    QcConsumptionMapRepository qcConsumptionMapRepository;
    @Autowired
    BinStockUtil binStockUtil;
    @Autowired
    CExpenseService cExpenseService;
    @Autowired
    ProductMasterRepository productMasterRepository;
    
    @Autowired
    ProreportdbRepository proReportDbRepo;
    
    @Autowired
    AttributeFieldsProcessTypeOptionRepository processOptionRepository;
    
    @Autowired
    InwardSkipLogRepository inwardSkipLogRepository;


    public ResponseEntity<?> inProcessBatchList(User loggedUser, String batchNo, Long productId, Long deptId, Long branch, String shiftDate, Long shiftId) {


        System.out.println("branch ==== " + loggedUser.getBranch_id() + "  Dept ===== " + loggedUser.getDepartment().getId());

        List<InProcessBatchList> batchList = journalEntryRepository.getInProcessBatchList(branch, deptId, batchNo, productId, dateUtil.getStringTODateWk(shiftDate), shiftId);
        if (!batchList.isEmpty()) {
            List<InProcessBatchListDto> result = batchList.stream().map(stepData -> {
                InProcessBatchListDto inProcessBatch = new InProcessBatchListDto(stepData);
                updatesInProcessBatchDetails(inProcessBatch);
                return inProcessBatch;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<InProcessBatchListDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<InProcessBatchListDto>>(new ArrayList<>(), HttpStatus.OK);
        }

    }


    public void updatesInProcessBatchDetails(InProcessBatchListDto inProcessBatchListDto) {
        List<InProcessBatchWithDetailListDto> machineList = new ArrayList<>();

        List<InProcessBatchWithDetailList> batchList = journalEntryRepository.getInProcessBatchWithDetailList(inProcessBatchListDto.getBatchId(), inProcessBatchListDto.getBatchNo());
        if (!batchList.isEmpty()) {
            machineList = batchList.stream().map(stepData -> {
                InProcessBatchWithDetailListDto inProcessBatch = new InProcessBatchWithDetailListDto(stepData);
                inProcessBatch.setMachineType("P");
                return inProcessBatch;
            }).collect(Collectors.toList());
        }

        machineList.addAll(getAuxlairyList(inProcessBatchListDto.getBatchId(), inProcessBatchListDto.getBatchNo()));

        inProcessBatchListDto.setInProcessBatchWithDetailList(machineList);

        List<InProcessBatchWithDetailList> binList = journalEntryRepository.getInProcessBatchWithBinDetailList(inProcessBatchListDto.getBatchId(), inProcessBatchListDto.getBatchNo());
        if (!binList.isEmpty()) {
            List<InProcessBatchWithDetailListDto> result = binList.stream().map(stepData -> {
                InProcessBatchWithDetailListDto inProcessBatch = new InProcessBatchWithDetailListDto(stepData);
                return inProcessBatch;
            }).collect(Collectors.toList());
            inProcessBatchListDto.setInProcessBatchWithBinList(result);
        } else {
            inProcessBatchListDto.setInProcessBatchWithBinList(new ArrayList<>());
        }
    }


    public List<InProcessBatchWithDetailListDto> getAuxlairyList(Long batchId, String batchNo) {
        List<InProcessBatchWithDetailListDto> machineList = new ArrayList<>();
        List<InProcessBatchWithDetailList> batchList = journalEntryRepository.getInProcessBatchWithAuxilaryList(batchId, batchNo);
        if (!batchList.isEmpty()) {
            machineList = batchList.stream().map(stepData -> {
                InProcessBatchWithDetailListDto inProcessBatch = new InProcessBatchWithDetailListDto(stepData);
                inProcessBatch.setMachineType("A");
                return inProcessBatch;
            }).collect(Collectors.toList());
        }
        return machineList;
    }


    @Transactional
    public ResponseEntity<?> saveQcInProcessData(List<QcInProcessDto> reqDtos, User user) {

        String bathNo = null;

        try {

            List<QcInProcess> qcInProcessesList = new ArrayList<>();
            for (QcInProcessDto reqDto : reqDtos) {
                bathNo = reqDto.getBatchno();
                QcInProcess model = new QcInProcess(reqDto);
                model.setStatus("InProgress");
                model.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                model.setCreatedBy(user.getId());
                model.setEntry(dateUtil.getISTDate());
                /*System.out.println("QcInProcess StepName  ==  " + reqDto.getStepName());
                System.out.println("QcInProcess Atid  ==  " + reqDto.getAtid());
                System.out.println("QcInProcess Resval ==  " + reqDto.getResval());*/
                qcInProcessesList.add(model);
            }
            inProcessRepo.saveAll(qcInProcessesList);
            //System.out.println("bathNo  ==  " + bathNo);
            return new ResponseEntity<Status>(new Status("200", "QcInProcess save successfully!!!"), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Status>(new Status("400", "QcInProcess details value not created"), HttpStatus.OK);
    }


    public void updatesDetails(QCKpiListDto step) {

        List<AttributesList> attributesLists = inProcessRepo.getAttributesAndValues(step.getTaskGroupMapId(), step.getKpiNameId(), step.getProductMasterId(), step.getBatchNo());
        if (!attributesLists.isEmpty()) {
            List<AttributesListDto> result = attributesLists.stream().map(kpi -> {
                AttributesListDto fieldDto = new AttributesListDto(kpi);
                if (fieldDto.getAttributesResValue() == null) {
                    List<AttributeFieldsProcessTypeOptionBased> optionBaseds = processTypeRepository.findBy(fieldDto.getId());
                    if (!optionBaseds.isEmpty()) {
                        List<AttributeFieldsProcessTypeOptionBasedDto> te = optionBaseds.stream().map(optionData -> new AttributeFieldsProcessTypeOptionBasedDto(optionData)).collect(Collectors.toList());
                        fieldDto.setOptions(te);
                    }
                }
                return fieldDto;
            }).collect(Collectors.toList());
            step.setAttributeFieldsProcessType(result);
        }
    }


    public ResponseEntity<?> getAttributesQcInProcessData(Long kpiNameId, String batchNo, Long shiftId, String shiftDate) {

        Date shiftConDate = dateUtil.getStringTODateWk(shiftDate);

        List<KpiList> stepList = kpiRepository.getKpiNameStepsDetails(kpiNameId);
        List<QCKpiListDto> result = new ArrayList<>();
        if (!stepList.isEmpty()) {

            for (KpiList stepData : stepList) {
                QCKpiListDto step = new QCKpiListDto(stepData);
                Boolean checkStepStatus = checkStepStatus(step, batchNo, shiftId, shiftConDate);
                System.out.println(checkStepStatus);
                if (checkStepStatus) {
                    step.setStepStatus("C");
                } else {
                    updatesDetails(step);
                }
                result.add(step);
            }
        }
        return new ResponseEntity<List<QCKpiListDto>>(result, HttpStatus.OK);

    }


    public Boolean checkStepStatus(QCKpiListDto step, String batchNo, Long shiftId, Date shiftDate) {


        Optional<QcInProcess> qcInProcess = inProcessRepo.findQcInProcess(step.getTaskGroupMapId(), batchNo, step.getKpiNameId(), shiftId, shiftDate);
        System.out.println(" Alagu :   === qcInProcess : " + qcInProcess);
        if (qcInProcess.isPresent()) {
            return true;
        } else
            return false;
    }


    public ResponseEntity<?> getAllInProcessDetails(String batchNo, Long kpiNameId, String inwardDate, String status, Pageable pageable) {
        Date date = null;
        if (Objects.nonNull(inwardDate)) {
            date = dateUtil.getStringTODateWk(inwardDate);
        }
        Page<QcInProcessList> res = inProcessRepo.getInProcessList(kpiNameId, batchNo, status, date, pageable);
        if (!res.isEmpty()) {
            List<QcInProcessListDTO> list = res.toList().stream().map(data -> new QcInProcessListDTO(data)).collect(Collectors.toList());
            PageDataDto<QcInProcessListDTO> pageData = new PageDataDto<>(list, pageable, res);
            return new ResponseEntity<PageDataDto<QcInProcessListDTO>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<QcInProcessListDTO>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getAttributesQcInProcessDetailsData(Long kpiNameId, String batchNo, Long shiftId, String shiftDate) {

        Date shiftDateCon;
        if (shiftDate != null) {
            shiftDateCon = dateUtil.getStringTODateWk(shiftDate);
        } else {
            shiftDateCon = null;
        }

        QcInProcessDataList qcInProcessData = inProcessRepo.getQcInProcessDataList(kpiNameId, batchNo, shiftId, shiftDateCon);

        if (qcInProcessData != null) {

            QcInProcessDataDTO qcInProcessDataDTO = new QcInProcessDataDTO(qcInProcessData);

            List<KpiList> stepList = kpiRepository.getKpiNameStepsDetails(kpiNameId);

            List<QcProcessKpiListDto> result = new ArrayList<>();

            if (!stepList.isEmpty()) {

                result = stepList.stream().map(stepData -> {
                    QcProcessKpiListDto step = new QcProcessKpiListDto(stepData);
                    String entryType="Qc_Inprocess";
                    Optional<InwardSkipLog> inwardSkipLog = inwardSkipLogRepository.findBy(stepData.getKpiNameId(),
                    		stepData.getTaskGroupMapId(), batchNo, entryType);
                    if (inwardSkipLog.isPresent()) {
                        step.setSkipflag(inwardSkipLog.get().getSkipflag());
                    }
                    updatesMachineDetails(step, batchNo, shiftId, shiftDateCon);
                    return step;
                }).collect(Collectors.toList());
                qcInProcessDataDTO.setQcInProcessStepList(result);
            }
            return new ResponseEntity<QcInProcessDataDTO>(qcInProcessDataDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<QcInProcessDataDTO>(HttpStatus.NOT_FOUND);
        }

    }


    public void updatesMachineDetails(QcProcessKpiListDto step, String batchNo, Long shiftId, Date shiftDate) {

    	
        List<QcInProcessDataList> qcInProcessDataList = inProcessRepo.getQcInProcessDataDetailList(step.getKpiNameId(), step.getTaskGroupMapId(), batchNo, shiftId, shiftDate);

        if (!qcInProcessDataList.isEmpty()) {        
            List<QcInProcessDataDetailsDTO> qcInProcessDataDetailsDTOList = qcInProcessDataList.stream().map(qcInProcessDataDetails -> {
                QcInProcessDataDetailsDTO details = new QcInProcessDataDetailsDTO(qcInProcessDataDetails);
                updatesDetails1(step, details);
                return details;
            }).collect(Collectors.toList());
            step.setQcInProcessMachineLists(qcInProcessDataDetailsDTOList);
        }else {           
            QcInProcessDataDetailsDTO empty = new QcInProcessDataDetailsDTO();
            empty.setKpiId(step.getKpiNameId());
            empty.setBatchNo(batchNo);
            empty.setShiftId(shiftId);
            empty.setShiftDate(shiftDate != null ? dateUtil.getDateTOStringWk(shiftDate) : null);
            empty.setMachineId(null);
            empty.setBinId(null);
            empty.setBinName(null);
            empty.setPackingBatchNo(null);
            empty.setBatchProductNo(null);           
            updatesDetails1(step, empty);
            step.setQcInProcessMachineLists(Arrays.asList(empty));          
        }
    }


    public void updatesDetails1(QcProcessKpiListDto step, QcInProcessDataDetailsDTO qcInProcessDataDetailsDTO) {

        System.out.println(" Alagu :   === batchNo : " + qcInProcessDataDetailsDTO.getBatchNo());
        System.out.println(" Alagu :   === tgid : " + step.getTaskGroupMapId() + " === kpiId : " + step.getKpiNameId() + " === pmId : " + step.getProductMasterId() + " === machineId : " + qcInProcessDataDetailsDTO.getMachineId() + " === shiftId : " + qcInProcessDataDetailsDTO.getShiftId() + " === shiftDate : " + qcInProcessDataDetailsDTO.getShiftDate() + " === binname : " + qcInProcessDataDetailsDTO.getBinName() + " === binId : " + qcInProcessDataDetailsDTO.getBinId());
        List<AttributesList>attributesLists=null;
        if(qcInProcessDataDetailsDTO.getBatchProductNo()==null) {
        	Long productId=step.getProductMasterId();
        attributesLists= inProcessRepo.getAttributesAndValues(step.getTaskGroupMapId(), step.getKpiNameId(), productId, qcInProcessDataDetailsDTO.getBatchNo(), qcInProcessDataDetailsDTO.getMachineId(), qcInProcessDataDetailsDTO.getShiftId(), dateUtil.getStringTODateWk(qcInProcessDataDetailsDTO.getShiftDate()), qcInProcessDataDetailsDTO.getBinName(), qcInProcessDataDetailsDTO.getBinId(),qcInProcessDataDetailsDTO.getPackingBatchNo());
        }else {
        	Long productId=null;
        	 attributesLists = inProcessRepo.getAttributesAndValues(step.getTaskGroupMapId(), step.getKpiNameId(), productId, qcInProcessDataDetailsDTO.getBatchNo(), qcInProcessDataDetailsDTO.getMachineId(), qcInProcessDataDetailsDTO.getShiftId(), dateUtil.getStringTODateWk(qcInProcessDataDetailsDTO.getShiftDate()), qcInProcessDataDetailsDTO.getBinName(), qcInProcessDataDetailsDTO.getBinId(),qcInProcessDataDetailsDTO.getPackingBatchNo());
        }
        if (!attributesLists.isEmpty()) {
            List<AttributesListDto> reslt = attributesLists.stream().map(kpi -> {
                AttributesListDto fieldDto = new AttributesListDto(kpi);
                System.out.println(" Alagu :   === attName : " + fieldDto.getName() + " === inputType : " + fieldDto.getInputType() + " === batchNo : " + qcInProcessDataDetailsDTO.getBatchNo());
                if (fieldDto.getAttributesResValue() == null) {
                    List<AttributeFieldsProcessTypeOptionBased> optionBaseds = processTypeRepository.findBy(fieldDto.getId());
                    if (!optionBaseds.isEmpty()) {
                        List<AttributeFieldsProcessTypeOptionBasedDto> te = optionBaseds.stream().map(optionData -> new AttributeFieldsProcessTypeOptionBasedDto(optionData)).collect(Collectors.toList());
                        fieldDto.setOptions(te);
                    }
                }
                return fieldDto;
            }).collect(Collectors.toList());
            qcInProcessDataDetailsDTO.setAttributeFieldsProcessType(reslt);
        }
    }


    public ResponseEntity<?> completeQcInProcess(Long kpiNameId, String batchNo, Long shiftId, String shiftDate) {

        Date shiftDateCon = null;
        if (shiftDate != null) {
            shiftDateCon = dateUtil.getStringTODateWk(shiftDate);
        }
        try {
            inProcessRepo.updateStatus(kpiNameId, batchNo, shiftId, shiftDateCon);
            return new ResponseEntity<Status>(new Status("200", "StatusUpdated successfully!!"), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<Status>(new Status("400", "Status Not Updated successfully!!"), HttpStatus.OK);
        }

    }


    public ResponseEntity<?> qcInProcessReport(List<QcInProcessEntryRequest> requests) {

        List<QcInProcessEntryResponse> responseList = new ArrayList<>();

        for (QcInProcessEntryRequest request : requests) {

            QcInProcessEntryResponse qcInProcessEntryResponse = null;
  
            QcInProcessEntry qcInProcessEntry = inProcessRepo.findQcInProcessEntryResponse(request.getBatchNo(), request.getKpiId(), request.getShiftId(), dateUtil.getStringTODateWk(request.getShiftDate()));

            if (qcInProcessEntry != null) {
                qcInProcessEntryResponse = new QcInProcessEntryResponse(qcInProcessEntry);
                qcInProcessEntryResponse.setBatchNo(request.getBatchNo());
            }else {
            	qcInProcessEntryResponse = new QcInProcessEntryResponse();
            }




            Optional<QcInProcessEntry> journalEntry = journalEntryRepository.getJournalEntry(request.getBatchNo());
            if(journalEntry.isPresent()){

                qcInProcessEntryResponse.setMainMachineId(journalEntry.get().getMachineId());
                qcInProcessEntryResponse.setMainMachineName(journalEntry.get().getMachineName());
            }
            
            List<QcInProcessEntryAttributes> attributesList = inProcessRepo.findQcInProcessEntryAttributesList(request.getKpiId());

            List<QcInProcessEntryAttributesDTO> qcInProcessEntryAttributes = attributesList.stream().map(att -> new QcInProcessEntryAttributesDTO(att)).collect(Collectors.toList());
            qcInProcessEntryResponse.setQcInProcessEntryAttributesList(qcInProcessEntryAttributes);

            List<QcInProcessEntry> machineList = inProcessRepo.findQcInProcessEntryMachineList(request.getBatchNo(), request.getKpiId(), request.getShiftId(), dateUtil.getStringTODateWk(request.getShiftDate()));

            List<QcInProcessEntryDetails> qcInProcessEntryDetailsList = machineList.stream().map(data -> {

                QcInProcessEntryDetails qcInProcessEntryDetails = new QcInProcessEntryDetails(data);

                List<QcInProcessEntryAttributesDTO> qcInProcessEntryAttList = attributesList.stream().map(att -> {
                	
                    QcInProcessEntryAttributesDTO attributesDTO = new QcInProcessEntryAttributesDTO(att) ;

                    Optional<QcInProcess> qcInProcess = inProcessRepo.findQcInProcess(att.getTgid(), request.getBatchNo() , request.getKpiId(), request.getShiftId(), dateUtil.getStringTODateWk(request.getShiftDate()) , qcInProcessEntryDetails.machineId , att.getAttributeId(),qcInProcessEntryDetails.getPackingBatchNo(),qcInProcessEntryDetails.getBinName()) ;

                    if(qcInProcess.isPresent()){
                        attributesDTO.setAttributeResValue(qcInProcess.get().getResval());
                        attributesDTO.setUnit(qcInProcess.get().getDunit());
                    }

                    return attributesDTO;
                }).collect(Collectors.toList());

                qcInProcessEntryDetails.setQcInProcessEntryAttributes(qcInProcessEntryAttList);

                return qcInProcessEntryDetails;
            }).collect(Collectors.toList());

            qcInProcessEntryResponse.setQcInProcessEntryDetails(qcInProcessEntryDetailsList);

            responseList.add(qcInProcessEntryResponse);
        }
        return new ResponseEntity(responseList, HttpStatus.OK);

    }
    public ResponseEntity<?> getBinOrTankNumber(Long productId, User user) {
		Long branchId = user.getBranch_id();
		Long dept = user.getDepartment().getId();
		 List<Long> list = binStockRepository.findBinNoList(branchId, dept, productId);
		 if (!list.isEmpty()) {
	            return new ResponseEntity<List<Long>>(list, HttpStatus.OK);
	        } else {
	            return new ResponseEntity<List<Long>>(new ArrayList<>(), HttpStatus.OK);
	        }

	}


	public ResponseEntity<?> getBatchNoByBinNo(Long productId, Long binNo, User user) {
	
		List<BinStock> findBatchNoByBinNo = binStockRepository.findBatchNoByBinNo(productId, binNo);
		if(!findBatchNoByBinNo.isEmpty()) {
		 List<BinStockDto> dtoList = findBatchNoByBinNo.stream()
	                .map(BinStockDto::new)
	                .collect(Collectors.toList());
		 return new ResponseEntity<>(dtoList, HttpStatus.OK);
	          	
		}else {
	            return new ResponseEntity<Status>(new Status("400", "batch number not available for this bin!!"), HttpStatus.OK);
	}      
}


	public List<partcategoryListDto> getPackingBatchNoByBinNo(String batchNo) {
		  List<partcategoryList> pList = productionEntryRepository.findPackingPatchByColId(44l, 304l);
		  List<partcategoryListDto> resultList = new ArrayList<>();
		  if(!pList.isEmpty()) {
		    for (partcategoryList pDto : pList) {
		        if (pDto.getTrnNo().equalsIgnoreCase(batchNo)) {
		            resultList.add(new partcategoryListDto(pDto));
		        }
		        }		  
	}
		  return resultList;
		
	}

@Transactional
	public ResponseEntity<?> saveConsumpton(QcConsumptionSetupDto qcConsumptionSetupDto , User user) {
		
		QcConsumptionSetup setup = new QcConsumptionSetup(qcConsumptionSetupDto);
       
        setup.setDept(user.getDepartment().getId());
        setup.setBranch(user.getBranch_id());
        setup.setCreatedBy(user.getId());
        setup.setCreatedDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

        QcConsumptionSetup savedSetup = qcConsumptionSetupRepository.save(setup);

        // Save consumables
        List<QcConsumptionMapDto> consumables = qcConsumptionSetupDto.getConsumables();
        if (consumables != null && !consumables.isEmpty()) {
            List<QcConsumptionMap> maps = consumables.stream().map(c -> {
                QcConsumptionMap map = new QcConsumptionMap(c);
                map.setConfigId(savedSetup.getId());               
                return map;
            }).collect(Collectors.toList());

            qcConsumptionMapRepository.saveAll(maps);
        }
        return new ResponseEntity<Status>(new Status("200", "consumption created  successfully!!!"), HttpStatus.OK);
     
    }

public ResponseEntity<?> getAvailbleQuantity(String batchNo, Long binNo, Long productId, Double quantity) {	
	Optional<BinStock> list = binStockRepository.findAvailableQuantity(batchNo,binNo,productId);
	if(list.isPresent()) {	
		if(list.get().getStkQty()>=quantity) {
		return new ResponseEntity<Status>(new Status("200", "quantity allocated successfully!!!"), HttpStatus.OK);
    } else {       
    	 return new ResponseEntity<Status>(new Status("404", quantity + "is greater than available stock !!!"), HttpStatus.NOT_FOUND);
    }	
	}
	return new ResponseEntity<Status>(new Status("404", " stock is not there !!!"), HttpStatus.NOT_FOUND);
}

@Transactional
public ResponseEntity<?> stockConsumedByQuantity( QcInProcessDto dto, User user) {
	Optional<BinStock> list = binStockRepository.findAvailableQuantity(dto.getBatchno(),dto.getBinName(),dto.getBatchProduct());
	if(list.isPresent())
	{
		 BinStock binstock = list.get();
		 Double updatedStkQty = binstock.getStkQty() - dto.getQuantity();
         binstock.setStkQty(updatedStkQty);
         if(binstock.getAvgCost()!=null) {
         binstock.setCost(binstock.getAvgCost() *updatedStkQty);
         }
         binStockRepository.save(binstock);         
         binStockUtil.updateBinLogInprocessQcTransaction( binstock, user , dto.getQuantity());
         cExpenseService.updateCexpenseInprocessQcTransaction(binstock, user , dto);
         return new ResponseEntity<Status>(new Status("200", "stock consumed successfully!!!"), HttpStatus.OK);
	}
	return new ResponseEntity<Status>(new Status("404", "stock is not available!!!"), HttpStatus.NOT_FOUND);
}	

@Transactional
public ResponseEntity<?> stockConsumedByConsumableQuantity(QcInProcessDto dto, User user) {
	List<QcConsumptionMap> product = qcConsumptionMapRepository.findConsumableProduct(dto.getKpiNameId(),dto.getBatchProduct());
	Long dept=user.getDepartment().getId();
	Long branch=user.getBranch_id();
	if(!product.isEmpty())
	{
		 List<QcConsumptionMapDto> maps = product.stream().map(c -> {
             QcConsumptionMapDto map = new QcConsumptionMapDto(c);
             List<BinStock> stock = binStockRepository.findStockByConProduct(map.getConProduct(),dept, branch ,map.getLot()); 
             if(!stock.isEmpty()){
            	 Double availableQuantity=0.0 ;
            	 for(BinStock binStock : stock)
            	 {
            		 availableQuantity +=binStock.getStkQty();
            	 }
            	 if(availableQuantity>=map.getRequiredQty())
            	 {
            		 Double remaining=map.getRequiredQty();
            		 Double consumed=0.0;
            		 for(BinStock binStock : stock)
            		 {
            			 if(remaining==0.0)
            			 {
            				 break;
            			 }
            			 else {
            				 if(remaining<= binStock.getStkQty()) {
            				 Double updatedStkQty = binStock.getStkQty() - remaining;
            				 binStock.setStkQty(updatedStkQty);
            				 if(binStock.getAvgCost() !=null) {
            				 binStock.setCost(binStock.getAvgCost() * updatedStkQty);
            				 }
            				 consumed=remaining;
            				 remaining=0.0;
            				
            				 }
            				 else {
            					 consumed=binStock.getStkQty();
            					 remaining -= binStock.getStkQty();
            					 binStock.setStkQty(0.0); 
            					 if(binStock.getAvgCost() !=null) {
            					 binStock.setCost(binStock.getAvgCost() * 0.0);
            					 }
            				 }
            		         binStockRepository.save(binStock);
            		         cExpenseService.updateCexpenseInprocessQcTransactionForReport(binStock, user , dto,consumed);
            		         binStockUtil.updateBinLogInprocessQcTransactionForReport( binStock, user , consumed);
            			 }
            		 }
            	 }else {
            		 throw new RuntimeException("Insufficient stock for product "+ map.getConProduct() + " defined for this kpi "+dto.getKpiNameId()+". Please check stock availability.");
            	 }            	 
             }else{            
            	 throw new RuntimeException("Insufficient stock for product"+ map.getConProduct() + " defined for this kpi "+dto.getKpiNameId()+". Please check stock availability.");
             }
             return map;
         }).collect(Collectors.toList());
		 return new ResponseEntity<Status>(new Status("200", "Qc Entry successfully Completed!!!"), HttpStatus.OK);
	
	}else
	{
		 return new ResponseEntity<Status>(new Status("404",  "Cofiguration Does Not Exists !!!"), HttpStatus.NOT_FOUND);
    }	
}


public ResponseEntity<?> getConsumable(Long kpiId, Long productId) {
	List<QcConsumptionMap> product = qcConsumptionMapRepository.findConsumableProduct(kpiId,productId);
	if(!product.isEmpty()) {
	List<QcConsumptionMapDto> dtoList = product.stream()
	.map(c->{
			QcConsumptionMapDto map = new QcConsumptionMapDto(c);
			Optional<ProductMaster> op = productMasterRepository.findById(map.getConProduct());
			if(op.isPresent())
			{
				map.setConProductName(op.get().getProductName());
			}
            return map;	
	})
    .collect(Collectors.toList());
	 return new ResponseEntity<>(dtoList, HttpStatus.OK);   	
        }else {
        return new ResponseEntity<Status>(new Status("400", "consumable products are not available for this kpi and product!!!"), HttpStatus.NOT_FOUND);
      }      
      }


public ResponseEntity<?> updateConsumable(QcConsumptionSetupDto qcConsumptionSetupDto, User user) {

//	QcConsumptionSetup setup = new QcConsumptionSetup(qcConsumptionSetupDto);
//	setup.setId(qcConsumptionSetupDto.getId());
//	 setup.setDept(user.getDepartment().getId());
//     setup.setBranch(user.getBranch_id()); 
//     QcConsumptionSetup savedSetup = qcConsumptionSetupRepository.save(setup);
     if(!qcConsumptionSetupDto.getConsumables().isEmpty())
     {
    	 for(QcConsumptionMapDto conProduct :qcConsumptionSetupDto.getConsumables()) {
    		 if(conProduct.getId()!=0) {
			Optional<QcConsumptionMap> conDetail = qcConsumptionMapRepository.findById(conProduct.getId());
			if(conDetail.isPresent())
			{
				QcConsumptionMap config= conDetail.get();
				config.setConProduct(conProduct.getConProduct());
				config.setRequiredQty(conProduct.getRequiredQty());
				config.setConfigId(conProduct.getConfigId());
				config.setLot(conProduct.getLot());
				qcConsumptionMapRepository.save(config);
			}
    		 }else {
				QcConsumptionMap config = new  QcConsumptionMap(conProduct);
				Optional<QcConsumptionSetup> findByKpiName = qcConsumptionSetupRepository.findByKpiName(qcConsumptionSetupDto.getKpiId(),qcConsumptionSetupDto.getProductId());
				if(findByKpiName.isPresent()) {
				config.setConfigId(findByKpiName.get().getId());
				}
				qcConsumptionMapRepository.save(config);
			}
			
		}
     }  
	return new ResponseEntity<Status>(new Status("200", "consumable  updated  successfully!!!"), HttpStatus.OK);


}


public ResponseEntity<?> deleteConsumable(List<Long> id, User user) {
	qcConsumptionMapRepository.deleteAllById(id);
	return new ResponseEntity<Status>(new Status("200", "consumable  deleted  successfully!!!"), HttpStatus.OK);

}


public ResponseEntity<?> getAllAttributeQc(Long kpiNameId, String batchNo, Long shiftId, String shiftDate) {
    try { 
	Date shiftDateCon = null;
     if (shiftDate != null) {
         shiftDateCon = dateUtil.getStringTODateWk(shiftDate);
     }
     List<AttributesList> optList = proReportDbRepo.getAllAttributeListForQc(kpiNameId,batchNo,shiftId,shiftDateCon);
     List<AttributesListDto> result = optList.stream().map(data -> {
         AttributesListDto attributesDto = new AttributesListDto(data);
         if (attributesDto.getAttributesResValue() == null) {
             List<AttributeFieldsProcessTypeOptionBased> optionBaseds = processOptionRepository.findBy(data.getId());
             if (!optionBaseds.isEmpty()) {
                 List<AttributeFieldsProcessTypeOptionBasedDto> te = optionBaseds.stream().map(data1 -> new AttributeFieldsProcessTypeOptionBasedDto(data1)).collect(Collectors.toList());
                 attributesDto.setOptions(te);
             }
         }
         return attributesDto;
     }).collect(Collectors.toList());

     return new ResponseEntity<List<AttributesListDto>>(result, HttpStatus.OK);
    } catch (Exception e) {
        e.printStackTrace();
    }
    return new ResponseEntity<List<AttributesListDto>>(new ArrayList<>(), HttpStatus.OK);
}

	}
	
