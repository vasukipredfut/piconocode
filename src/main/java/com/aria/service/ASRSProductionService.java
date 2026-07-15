package com.aria.service;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.sqlserver.dao.ERPStoreRequestRepository;
import com.aria.sqlserver.model.ERPStoreRequest;
import com.aria.utils.BinStockUtil;
import com.aria.utils.DateUtil;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ASRSProductionService {

    public static final Logger logger = org.slf4j.LoggerFactory.getLogger(ASRSProductionService.class);

    public static final String TYPE_MOVING_TO_ASRS = "MovingtoASRS";
    public static final String TYPE_DIRECT_TO_ASRS = "DirectToASRS";


    @Autowired
    UserRepository userRepository;

    @Autowired
    ASRSProductionDetailsRepository asrsProductionDetailsRepository;

    @Autowired
    ASRSPalletDetailsRepository asrsPalletDetailsRepository;

    @Autowired
    ProductionEntryRepository productionEntryRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    WcShiftsRepository wcShiftsRepository;

    @Autowired
    JournalRepository journalRepository;

    @Autowired
    PAttributesMappingRepository pAttributesMappingRepository;

    @Autowired
    JournalAttributesMappingRepo journalAttributesMappingRepo;

    @Autowired
    ERPStoreRequestRepository erpStoreRequestRepository;

    @Autowired
    ProductMasterRepository product;

    @Autowired
    BinStockRepository binStockRepository;

    @Autowired
    GogroupPalletNumberMapRepository gogroupPalletNumberMapRepository;

    @Autowired
    GoGroupRepository goGroupRepository;

    @Autowired
    BinStockUtil binStockUtil;

    @Autowired
    FgmDataRepository fgmDataRepository;


    public ResponseEntity<?> getASRSProductionEntryFilterList(Long branchId, Long deptId, Long journalId, String journalName, String productName, String status, String trnNo, String shiftDate, Pageable pageable) throws UsernameNotFoundException {

        Date conShiftDate = null;
        if (shiftDate != null && shiftDate != null) {
            conShiftDate = dateUtil.getStringTODateWk(shiftDate);
        }

        Page<ASRSProductionEntryList> list = productionEntryRepository.getASRSProductionEntryFilterList(branchId, deptId, journalId, journalName, productName, trnNo, status, conShiftDate, pageable);
        List<partcategoryList> pList = productionEntryRepository.findByColId(44l, 304l);
        if (!list.toList().isEmpty()) {
            List<ASRSProductionEntryDTO> result = list.toList().stream().map(data -> {
                ASRSProductionEntryDTO asrs = new ASRSProductionEntryDTO(data);
                if (asrs.getAsrsId() != null) {
                    asrs.setAllocatedQty(asrsPalletDetailsRepository.allocatedQty(asrs.getAsrsId()));
                    asrs.setAsrsQty(asrsPalletDetailsRepository.asQty(asrs.getAsrsId()));
                    asrs.setDeductionQty(asrsPalletDetailsRepository.deductionQty(asrs.getAsrsId()));
                }
                for (partcategoryList pDto : pList) {
                    partcategoryListDto plDto = new partcategoryListDto(pDto);
                    if (pDto.getTrnNo().equalsIgnoreCase(asrs.getTrnNo())) {
                        asrs.setBatchNo(pDto.getDataResValues());
                    }
                    String productType = asrs.getAsrsProductType();
                    if (productType != null && "Export Order".equalsIgnoreCase(productType)) {
                        if (pDto.getTrnNo().equalsIgnoreCase(asrs.getTrnNo())) {
                            asrs.setPartCategory(pDto.getDataResValues());
                        }
                    }
                }

                return asrs;
            }).collect(Collectors.toList());
            PageDataDto<ASRSProductionEntryDTO> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<ASRSProductionEntryDTO>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<ASRSProductionEntryDTO>>(new PageDataDto(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> saveASRSProduction(ASRSProductionDTO asrsProductionDTO, User loggedUser) {
        ASRSProductionDetails journal = new ASRSProductionDetails(asrsProductionDTO);
        ASRSProductionDetails result = asrsProductionDetailsRepository.save(journal);
        if (asrsProductionDTO.getPalletDetails() != null && !asrsProductionDTO.getPalletDetails().isEmpty()) {
            for (ASRSPalletDetailsDTO palletDetailsDTO : asrsProductionDTO.getPalletDetails()) {
                ASRSPalletDetails palletDetails = new ASRSPalletDetails(palletDetailsDTO);
                palletDetails.setAsrsProductionId(result.getId());
                ASRSPalletDetails res = asrsPalletDetailsRepository.save(palletDetails);
                if (palletDetails.getPalletType() != null && palletDetails.getPalletType().equalsIgnoreCase("ASRS")) {
                    logger.info("Pallet Type requires ERP Store Request update: " + palletDetailsDTO.getType());
                    updateErpStoreRequestStatus(result, res, palletDetailsDTO);
                    logger.info("ERP Store Request update completed for Pallet Number: " + res.getPalletNumber());
                }
            }
        }
        return new ResponseEntity<Status>(new Status("200", "ASRS Production created SuccessFully"), HttpStatus.OK);
    }


    public ResponseEntity<?> updateASRSProduction(ASRSProductionDTO asrsProductionDTO, User loggedUser) {
        ASRSProductionDetails journal = new ASRSProductionDetails(asrsProductionDTO);
        ASRSProductionDetails result = asrsProductionDetailsRepository.save(journal);
        if (asrsProductionDTO.getPalletDetails() != null && !asrsProductionDTO.getPalletDetails().isEmpty()) {
            for (ASRSPalletDetailsDTO palletDetailsDTO : asrsProductionDTO.getPalletDetails()) {
                ASRSPalletDetails palletDetails = new ASRSPalletDetails(palletDetailsDTO);
                palletDetails.setAsrsProductionId(result.getId());
                ASRSPalletDetails res = asrsPalletDetailsRepository.save(palletDetails);
                if (palletDetailsDTO.getType() != null && palletDetailsDTO.getType().equalsIgnoreCase("MovingtoASRS")) {
                    updateBinStockForMovingToASRS(res, result, loggedUser);
                }
                logger.info("Saved ASRSPalletDetails with ID: " + res.getId() + " for ASRS Production ID: " + result.getId());
                if (palletDetailsDTO.getType() != null && (palletDetailsDTO.getType().equalsIgnoreCase("MovingtoASRS") || palletDetailsDTO.getType().equalsIgnoreCase("DirectToASRS"))) {
                    logger.info("Pallet Type requires ERP Store Request update: " + palletDetailsDTO.getType());
                    updateErpStoreRequestStatus(result, res, palletDetailsDTO);
                    logger.info("ERP Store Request update completed for Pallet Number: " + res.getPalletNumber());
                }
            }
        }

        return new ResponseEntity<Status>(new Status("200", "ASRS Production created SuccessFully"), HttpStatus.OK);
    }


    public ResponseEntity<?> getASRSProduction(Long asrsId, User loggedUser) {
        Optional<ASRSProductionDetails> productionDetails = asrsProductionDetailsRepository.findById(asrsId);
        if (productionDetails.isPresent()) {
            ASRSProductionDTO asrsProduction = new ASRSProductionDTO(productionDetails.get());
            
            ProductionEntry productionEntry = productionEntryRepository.findPackingPatchByTrnNo(asrsProduction.getTrnNo());
            if(productionEntry != null) {
                asrsProduction.setBatchNo(productionEntry.getDataResValues());
            }
           
            
            Optional<Wcshift> wcShift = wcShiftsRepository.findByTrnNo(asrsProduction.getTrnNo());
            if (wcShift.isPresent()) {
                if (wcShift.get().getEStatus().equalsIgnoreCase("Y")) {
                    asrsProduction.setProductionStatus("Completed");
                } else {
                    asrsProduction.setProductionStatus("Inprogress");
                }
            }


            List<ASRSPalletDetails> asrsPalletDetailsList = asrsPalletDetailsRepository.findBy(asrsId);
            if (asrsPalletDetailsList != null && !asrsPalletDetailsList.isEmpty()) {
                List<ASRSPalletDetailsDTO> palletDetails = asrsPalletDetailsList.stream().map(ASRSPalletDetailsDTO::new).collect(Collectors.toList());
                asrsProduction.setPalletDetails(palletDetails);
            }
            if (asrsProduction.getAmendmentCreatedBy() != null) {
                Optional<User> userOptional = userRepository.findById(asrsProduction.getAmendmentCreatedBy());
                if (userOptional.isPresent()) {
                    asrsProduction.setAmendmentCreatedByName(userOptional.get().getEmployeename());
                }
            }

            return new ResponseEntity<ASRSProductionDTO>(asrsProduction, HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("404", "Not Found"), HttpStatus.NOT_FOUND);
        }
    }

    // 26-08-2025 created by dhivya (ASRSAmendment save api ), discussed by alaguraja sir
    public ResponseEntity<?> saveAmendmentASRSProduction(ASRSProductionDTO amendmentRequest, User loggedUser) {

        Optional<ASRSProductionDetails> productionDetails = asrsProductionDetailsRepository.findById(amendmentRequest.getId());
        ASRSProductionDetails asrsProductionDetails = null;
        if (productionDetails.isPresent()) {
            asrsProductionDetails = productionDetails.get();
        } else {
            asrsProductionDetails = new ASRSProductionDetails(amendmentRequest);
        }
        asrsProductionDetails.setDominoCount(amendmentRequest.getDominoCount());
        asrsProductionDetails.setAmendmentCount(amendmentRequest.getAmendmentCount());
        asrsProductionDetails.setPresentDominoCount(amendmentRequest.getDominoCount() - amendmentRequest.getAmendmentCount());
        asrsProductionDetails.setReason(amendmentRequest.getReason());
        asrsProductionDetails.setAmendmentCreatedBy(loggedUser.getId());
        asrsProductionDetails.setAmendmentCreatedTime(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        asrsProductionDetails.setStatus("Amendmented");
        ASRSProductionDetails result = asrsProductionDetailsRepository.save(asrsProductionDetails);
        updatedominicount(result);
        return new ResponseEntity<Status>(new Status("200", "ASRS Production created SuccessFully"), HttpStatus.OK);
    }

    public void updatedominicount(ASRSProductionDetails asrsProductionDetails) {
        Optional<Wcshift> wcshift = wcShiftsRepository.findByTrnNo(asrsProductionDetails.getTrnNo());
        if (wcshift.isPresent()) {
            Wcshift wcshift1 = wcshift.get();
            wcshift1.setDominoCount(asrsProductionDetails.getPresentDominoCount());
            wcShiftsRepository.save(wcshift1);
        }
    }


    public void updateErpStoreRequestStatus(ASRSProductionDetails asrsProductionDetails, ASRSPalletDetails asrsPalletDetails, ASRSPalletDetailsDTO palletDetailsDTO) {

        logger.info("Updating ERP Store Request Status for ASRS Production ID: " + asrsProductionDetails.getId() + " and Pallet Number: " + asrsPalletDetails.getPalletNumber());
        logger.info("Pallet Type: " + asrsPalletDetails.getPalletType());
        logger.info("Final ASRS Qty: " + asrsPalletDetails.getFinalASRSQty());
        logger.info("Product ID: " + asrsProductionDetails.getProductId());
        logger.info("Product Name: " + asrsProductionDetails.getProductName());
        logger.info("Batch No: " + asrsProductionDetails.getBatchNo());
        logger.info("Journal ID: " + asrsProductionDetails.getJournalId());
        logger.info("Shift Date: " + asrsProductionDetails.getShiftDate());
        logger.info("Shift No: " + asrsProductionDetails.getShiftId());
        logger.info("Quantity: " + asrsPalletDetails.getFinalASRSQty());
        try {
            ERPStoreRequest erpStoreRequest = new ERPStoreRequest();
            erpStoreRequest.setPalletType(asrsPalletDetails.getPalletType());
            erpStoreRequest.setPalletID(asrsPalletDetails.getPalletNumber());
            erpStoreRequest.setProductId(asrsProductionDetails.getProductId().toString());

           /* FgmData fgmData = fgmDataRepository.findByRefid(asrsProductionDetails.getProductId()).orElse(null);
            if (fgmData != null ) {
                if(fgmData.getName() != null && fgmData.getName().equalsIgnoreCase("Export")) {
                   erpStoreRequest.setPalletType("FG");
                }
            }else{
                erpStoreRequest.setPalletType("FG");
            }*/
            erpStoreRequest.setProductName(asrsProductionDetails.getProductName());
            erpStoreRequest.setBatchNo(asrsProductionDetails.getBatchNo());
            erpStoreRequest.setExportOrderNo(asrsProductionDetails.getBatchNo());
            erpStoreRequest.setJournalID(asrsProductionDetails.getJournalId());
            erpStoreRequest.setShiftDate(asrsProductionDetails.getShiftDate());

            // Generate ShiftNo: 1ST SHIFT = T-{date}1, 2ND SHIFT = T-{date}2, 3RD SHIFT = T-{date}3
            Integer shiftNumber = getShiftNumber(asrsProductionDetails.getShiftId());
            String shiftNo = generateShiftNo(asrsProductionDetails.getShiftDate(), shiftNumber);
            logger.info("shiftNo == " + shiftNo);
            if (shiftNo != null) {
                erpStoreRequest.setShiftNo(shiftNo);
            } else {
                // Fallback to shift ID if generation fails
                erpStoreRequest.setShiftNo(asrsProductionDetails.getShiftId().toString());
            }
            erpStoreRequest.setQuantity(Double.parseDouble(asrsPalletDetails.getFinalASRSQty()));
            erpStoreRequest.setRequestStatus("G");
            erpStoreRequest.setRequestDateTime(dateUtil.getIndianDateWithTime());
            erpStoreRequest.setCompletedDateTime(new Date());
            erpStoreRequest.setPartCategory(asrsPalletDetails.getPartCategory());

            ProductMaster productMaster = product.findById(asrsProductionDetails.getProductId()).orElse(null);
            logger.info("Fetched Product Master for Product ID: " + asrsProductionDetails.getProductId());
            if (productMaster != null && productMaster.getGweight() != null) {
                try {
                    erpStoreRequest.setGrossWeight(Double.parseDouble(productMaster.getGweight()));
                } catch (NumberFormatException nfe) {
                    logger.error("Error parsing Gross Weight: " + productMaster.getGweight(), nfe);
                }
            }
            if (productMaster != null && productMaster.getAsrsProductType() != null) {
                if (productMaster.getAsrsProductType().equalsIgnoreCase("Export Order")) {
                    erpStoreRequest.setPalletType("Export Order");
                } else if (productMaster.getAsrsProductType().equalsIgnoreCase("Finished Goods")) {
                    erpStoreRequest.setPalletType("FG");
                }
            }

            if (productMaster != null && productMaster.getProductWeight() != null) {
                erpStoreRequest.setProductWeight(productMaster.getProductWeight());
            }

            if (productMaster != null && productMaster.getUnit() != null) {
                erpStoreRequest.setUOM(productMaster.getUnit());
            }

            logger.info("Fetching production values for ERP Store Request update.");
            Map<Long, String> productionValues = getProductionValues(asrsProductionDetails, asrsPalletDetails);
            logger.info("Fetched production values for ERP Store Request update: " + productionValues);
            if (productionValues != null && !productionValues.isEmpty()) {
                if (productionValues.containsKey(43L) || productionValues.containsKey(1289L)) {
                    String bestBefore = productionValues.get(43L).isEmpty() ? productionValues.get(1289L) : productionValues.get(43L);
                    if (bestBefore != null) {
                        logger.info("Setting Best Before for ERP Store Request: " + bestBefore);
                        String formattedDate = dateUtil.convertToYYYYMMDD(bestBefore);

                        if (formattedDate != null) {
                            erpStoreRequest.setBestbefore(formattedDate);
                        }

                    }
                }
                if (productionValues.containsKey(41L)) {
                    String mrpValue = productionValues.get(41L);
                    if (mrpValue != null && !mrpValue.isEmpty()) {
                        try {
                            double mrp = Double.parseDouble(mrpValue);
                            logger.info("Setting MRP for ERP Store Request: " + mrp);
                            erpStoreRequest.setMRP(mrp);
                        } catch (NumberFormatException nfe) {
                            logger.error("Error parsing MRP value: " + mrpValue, nfe);
                        }
                    }
                }
                if (productionValues.containsKey(304L) || productionValues.containsKey(44L)) {
                    String batchNoValue = productionValues.get(44L).isEmpty() ? productionValues.get(304L) : productionValues.get(44L);
                    if (batchNoValue != null && !batchNoValue.isEmpty()) {
                        logger.info("Setting Batch No for ERP Store Request: " + batchNoValue);
                        erpStoreRequest.setBatchNo(batchNoValue);
                    }
                }
            }else {
                logger.warn("No production values found for ERP Store Request update.");
            }
            logger.info("Saving updated ERP Store Request for Pallet Number: " + asrsPalletDetails.getPalletNumber());
            erpStoreRequestRepository.save(erpStoreRequest);
        } catch (Exception e) {
            logger.error("Error in updating ERP Store Request Status: ", e);
        }

    }


    public Map<Long, String> getProductionValues(ASRSProductionDetails asrsProductionDetails, ASRSPalletDetails asrsPalletDetails) {

        Long journalId = asrsProductionDetails.getJournalId();
        String trnNo = asrsProductionDetails.getTrnNo();
        Long shiftId = asrsProductionDetails.getShiftId();
        Date shiftDate = asrsProductionDetails.getShiftDate();
        String batchNo = asrsProductionDetails.getBatchNo();

        logger.info("Fetching production values for Journal ID: " + journalId + " and Transaction No: " + trnNo);
        Map<Long, String> prodValues = new LinkedHashMap<>();

        List<ProductionEntry> productionEntry = productionEntryRepository.findProductionEntryRowsForMap(journalId, trnNo);

        if (productionEntry.isEmpty()) {
            logger.info("No production entry found for Journal ID: " + journalId + " and Transaction No: " + trnNo);
            return null;
        } else {
            logger.info("Production entry found for Journal ID: " + journalId + " and Transaction No: " + trnNo);
            for (ProductionEntry pe : productionEntry) {
                logger.info("Production Entry ID: " + pe.getId() + ", coltable Name: " + pe.getColTable());
                prodValues.put(pe.getColid(), pe.getDataResValues());
            }
        }

        return prodValues;
    }


    public void updateBinStockForMovingToASRS(ASRSPalletDetails asrsPalletDetails, ASRSProductionDetails asrsProductionDetails, User loggedUser) {
        try {
            logger.info("Updating Bin Stock for Moving to ASRS for Pallet Number: " + asrsPalletDetails.getPalletNumber());
            // Implement bin stock update logic here
            // This is a placeholder for the actual implementation
            // You would typically call another service or repository method to perform the update

            List<BinStock> binStockList = binStockRepository.findBy(asrsProductionDetails.getTrnNo(), asrsProductionDetails.getProductId(), asrsPalletDetails.getPalletNumber());
            if (binStockList == null || binStockList.isEmpty()) {
                logger.warn("No Bin Stock records found for Pallet Number: " + asrsPalletDetails.getPalletNumber());
                return;
            } else {

                Double remaining = Double.valueOf(0);

                for (BinStock binStock : binStockList) {

                    Double currentQty = binStock.getStkQty();
                    logger.info("Current Bin Stock Quantity for Pallet Number " + asrsPalletDetails.getPalletNumber() + ": " + currentQty);
                    Double newQty = 0.0;
                    if (remaining == 0.0) {
                        newQty = Double.parseDouble(asrsPalletDetails.getFinalASRSQty());
                        logger.info("New Quantity to be set for Pallet Number " + asrsPalletDetails.getPalletNumber() + ": " + newQty);
                    } else {
                        newQty = remaining;
                        logger.info("New Quantity to be set from remaining for Pallet Number " + asrsPalletDetails.getPalletNumber() + ": " + newQty);
                    }
                    Boolean newQtyExceeds = currentQty > newQty;

                    if (newQtyExceeds && remaining == 0.0) {

                        logger.warn("Current Bin Stock Quantity " + currentQty + " exceeds the new quantity " + newQty + " for Pallet Number: " + asrsPalletDetails.getPalletNumber());


                        BinStock updatedBinStock = new BinStock(binStock);
                        updatedBinStock.setStkQty(Double.parseDouble(asrsPalletDetails.getFinalASRSQty()));
                        GogroupPalletNumberMap gogroupPalletNumberMap = gogroupPalletNumberMapRepository.findByPalletNumberAndPalletType(asrsPalletDetails.getPalletNumber(), asrsPalletDetails.getPalletType()).orElse(null);
                        if (gogroupPalletNumberMap != null) {
                            updatedBinStock.setBinName(gogroupPalletNumberMap.getBinId());
                            GoGroup goGroup = goGroupRepository.findByBinNo(gogroupPalletNumberMap.getBinId()).orElse(null);
                            if (goGroup != null) {
                                updatedBinStock.setGid(goGroup.getGid());
                                updatedBinStock.setBinType(goGroup.getBinType());
                                updatedBinStock.setBinId(goGroup.getGCode());
                            }
                        }
                        updatedBinStock.setAvgCost(binStock.getAvgCost());
                        Double cost = binStock.getAvgCost() * Double.parseDouble(asrsPalletDetails.getFinalASRSQty());
                        updatedBinStock.setCost(cost);
                        updatedBinStock.setLTrnNo(binStock.getLTrnNo());
                        updatedBinStock.setRefId(binStock.getRefId());
                        updatedBinStock.setLot(binStock.getLot());
                        updatedBinStock.setAdate(new Date());
                        updatedBinStock = binStockRepository.save(updatedBinStock);
                        logger.info("Bin Stock found and updated for Pallet Number: " + asrsPalletDetails.getPalletNumber());


                        Double updatedQty = updatedBinStock.getStkQty() - newQty;
                        logger.info("Updated Bin Stock Quantity after deduction for Pallet Number " + asrsPalletDetails.getPalletNumber() + ": " + updatedQty);
                        binStock.setStkQty(updatedQty);
                        double updatedCost = binStock.getAvgCost() * updatedQty;
                        binStock.setCost(updatedCost);
                        binStockRepository.save(binStock);
                        logger.info("Bin Stock record updated successfully for Pallet Number: " + asrsPalletDetails.getPalletNumber());

                        binStockUtil.updateBinLogAsrsMovement(binStock, loggedUser, "I", newQty);
                        logger.info("Bin Log updated successfully for Pallet Number: " + asrsPalletDetails.getPalletNumber());
                        binStockUtil.updateBinLogAsrsMovement(updatedBinStock, loggedUser, "R", newQty);
                        logger.info("Bin Log updated successfully for Pallet Number: " + asrsPalletDetails.getPalletNumber());

                        break;

                    } else {

                        Boolean qtyExceeds = newQty > currentQty;

                        if (remaining > 0) {

                            remaining = currentQty - newQty;
                            logger.info("Remaining Quantity to be processed for Pallet Number " + asrsPalletDetails.getPalletNumber() + ": " + remaining);

                            logger.warn("Current Bin Stock Quantity " + currentQty + " exceeds the new quantity " + newQty + " for Pallet Number: " + asrsPalletDetails.getPalletNumber());

                            BinStock updatedBinStock = new BinStock(binStock);
                            updatedBinStock.setStkQty(0.0);
                            updatedBinStock.setCost(0.0);
                            updatedBinStock.setAvgCost(binStock.getAvgCost());
                            updatedBinStock.setLTrnNo(binStock.getLTrnNo());
                            updatedBinStock.setRefId(binStock.getRefId());
                            updatedBinStock.setLot(binStock.getLot());
                            updatedBinStock.setAdate(new Date());
                            updatedBinStock = binStockRepository.save(updatedBinStock);
                            logger.info("Bin Stock found and set to zero for Pallet Number: " + asrsPalletDetails.getPalletNumber());

                            binStockUtil.updateBinLogAsrsMovement(binStock, loggedUser, "I", currentQty);
                            logger.info("Bin Log updated successfully for Pallet Number: " + asrsPalletDetails.getPalletNumber());

                            binStockUtil.updateBinLogAsrsMovement(updatedBinStock, loggedUser, "R", currentQty);
                            logger.info("Bin Log updated successfully for Pallet Number: " + asrsPalletDetails.getPalletNumber());

                            if (remaining >= 0) {
                                break;
                            }

                        } else {
                            remaining = newQty - currentQty;
                            logger.info("Remaining Quantity to be processed for Pallet Number " + asrsPalletDetails.getPalletNumber() + ": " + remaining);

                            BinStock updatedBinStock = new BinStock(binStock);
                            updatedBinStock.setStkQty(0.0);
                            updatedBinStock.setCost(0.0);
                            updatedBinStock.setAvgCost(binStock.getAvgCost());
                            updatedBinStock.setLTrnNo(binStock.getLTrnNo());
                            updatedBinStock.setRefId(binStock.getRefId());
                            updatedBinStock.setLot(binStock.getLot());
                            updatedBinStock.setAdate(new Date());
                            updatedBinStock = binStockRepository.save(updatedBinStock);
                            logger.info("Bin Stock found and set to zero for Pallet Number: " + asrsPalletDetails.getPalletNumber());

                            binStockUtil.updateBinLogAsrsMovement(binStock, loggedUser, "I", currentQty);
                            logger.info("Bin Log updated successfully for Pallet Number: " + asrsPalletDetails.getPalletNumber());

                            binStockUtil.updateBinLogAsrsMovement(updatedBinStock, loggedUser, "R", currentQty);
                            logger.info("Bin Log updated successfully for Pallet Number: " + asrsPalletDetails.getPalletNumber());

                            if (remaining <= 0) {
                                break;
                            }
                        }
                    }
                }
            }
            logger.info("Bin Stock updated successfully for Pallet Number: " + asrsPalletDetails.getPalletNumber());
        } catch (Exception e) {
            logger.error("Error in updating Bin Stock for Moving to ASRS: ", e);
        }
    }

    /**
     * Generate ShiftNo by adding shift number to date
     * Format:
     * 1st SHIFT: T-(date + 1)
     * 2nd SHIFT: T-(date + 2)
     * 3rd SHIFT: T-(date + 3)
     */
    public String generateShiftNo(Date shiftDate, Integer shiftNumber) {

        if (shiftDate == null || shiftNumber == null) {
            logger.warn("ShiftDate or ShiftNumber is null.");
            return null;
        }

        if (shiftNumber < 1 || shiftNumber > 3) {
            logger.warn("Invalid ShiftNumber: {}", shiftNumber);
            return null;
        }

        LocalDate lt = shiftDate.toInstant().atZone(ZoneId.of("Asia/Kolkata")).toLocalDate();
        int dateNumber = lt.getDayOfMonth();

        System.out.println("day == " + dateNumber);

        int finalDay = dateNumber - 1;

        int finalShiftValue = (finalDay * 3) + shiftNumber;


        // Add shift number
        // int finalShiftValue = dateNumber + shiftNumber;

        String shiftNo = "T-" + finalShiftValue;

        logger.info("Generated ShiftNo: {} for date: {} and shift: {}",
                shiftNo, shiftDate, shiftNumber);

        return shiftNo;
    }

    /**
     * Get shift number from pshift ID
     * 1st SHIFT = 1
     * 2nd SHIFT = 2
     * 3rd SHIFT = 3
     *
     * @param pShiftId the pshift ID
     * @return the shift number (1, 2, or 3)
     */
    public Integer getShiftNumber(Long pShiftId) {
        try {
            if (pShiftId == null) {
                logger.warn("PShiftId is null. Cannot get shift number.");
                return null;
            }

            // Map pShiftId to shift number
            // This assumes pShiftId 1 = 1st shift, 2 = 2nd shift, 3 = 3rd shift
            // Adjust mapping based on your actual pshift IDs
            Integer shiftNumber = Math.toIntExact(pShiftId);


            if (shiftNumber < 1 || shiftNumber > 3) {
                logger.warn("Invalid shift number: " + shiftNumber + " for pShiftId: " + pShiftId);
                return null;
            }

            logger.info("Retrieved ShiftNumber: " + shiftNumber + " for PShiftId: " + pShiftId);
            return shiftNumber;
        } catch (Exception e) {
            logger.error("Error in getting shift number from pShiftId: ", e);
            return null;
        }
    }

}

