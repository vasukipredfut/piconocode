package com.aria.utils;

import com.aria.config.AuthEntryPointJwt;
import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
public class BinStockUtil {

    private static final Logger logger = LoggerFactory.getLogger(BinStockUtil.class);

    private static final DecimalFormat decfor = new DecimalFormat("0.0000");

    @Autowired
    public BinStockRepository binStockRepository;

    @Autowired
    public BinStatusRepository binStatusRepository;

    @Autowired
    public BillInwardItemReceivedRepository billInwardItemReceivedRepository;

    @Autowired
    public GoGroupRepository goGroupRepository;

    @Autowired
    public BillInwardRepository billInwardRepository;

    @Autowired
    public BinStockLogRepository binStockLogRepository;

    @Autowired
    public PurchaseOrderRepository purchaseOrderRepository;


    @Autowired
    public BinstockAuditLogRepository binstockAuditLogRepository;

    @Autowired
    public ProductExcessStorageRepository productExcessStorageRepository;

    @Autowired
    public PurchaseReturnRepository purchaseReturnRepository;

    @Autowired
    public CampRepository campRepository;

    @Autowired
    public ColorRepository colorRepository;

    @Autowired
    public DateUtil dateUtil;

    @Autowired
    public DayEndUtils dayEndUtils;


    @Autowired
    public BinLockRepository binLockRepository;

    public void updateBinStock(DirectPurchase directPurchase, DpEntryStoredProduct storedProduct,
                               DpEntryStoredBinDetails binDetails, User loggedUser) {
        logger.info("Updating bin stock for product: {}, batch: {}, bin: {}, lot: {}", storedProduct.getItemId(), storedProduct.getBatchNo(), binDetails.getBinNo(), storedProduct.getLot());
        Optional<GoGroup> goGroupOptional = goGroupRepository.findByBinNo(binDetails.getBinId());
        logger.info("GoGroup found for binNo: {}", binDetails.getBinNo());
        if (goGroupOptional.isPresent()) {

            logger.info("GoGroup found for binNo: {}", binDetails.getBinNo());
            binDetails.setBinId(Long.valueOf(goGroupOptional.get().getGoName()));
            binDetails.setBinNo(goGroupOptional.get().getGCode());

            Long godownId = goGroupOptional.get().getGid();
            logger.info("Godown ID: {}", godownId);
            logger.info("Product ID: {}, Batch: {}, Bin: {}", storedProduct.getItemId(), storedProduct.getBatchNo(), binDetails.getBinNo());
            if (storedProduct.getItemId() != null && storedProduct.getBatchNo() != null && binDetails.getBinNo() != null) {

                Optional<BinStock> optionalBinStock = null;
                logger.info("Checking bin stock for product: {}, batch: {}, bin: {}, lot: {} ", storedProduct.getItemId(), storedProduct.getBatchNo(), binDetails.getBinNo(), storedProduct.getLot());
                if (storedProduct.getColor() != null) {
                    if (storedProduct.getColor() != null && storedProduct.getColor().getName() != null) {
                        optionalBinStock = binStockRepository.findByProductBatchAndBin(storedProduct.getItemId(), storedProduct.getBatchNo(), binDetails.getBinNo(), binDetails.getBinId(), Double.valueOf(storedProduct.getLot()), storedProduct.getColor().getName());
                    } else {
                        logger.info("Color is null, so checking bin stock for product: {}, batch: {}, bin: {}, lot: {}", storedProduct.getItemId(), storedProduct.getBatchNo(), binDetails.getBinNo(), storedProduct.getLot());
                        optionalBinStock = binStockRepository.findByProductBatchAndBin(storedProduct.getItemId(), storedProduct.getBatchNo(), binDetails.getBinNo(), binDetails.getBinId(), Double.valueOf(storedProduct.getLot()));

                    }
                } else {
                    logger.info("first check for color is null, then checking without color");
                    logger.info("Color is null, so checking bin stock for product: {}, batch: {}, bin: {}, lot: {}", storedProduct.getItemId(), storedProduct.getBatchNo(), binDetails.getBinNo(), storedProduct.getLot());
                    optionalBinStock = binStockRepository.findByProductBatchAndBin(storedProduct.getItemId(), storedProduct.getBatchNo(), binDetails.getBinNo(), binDetails.getBinId(), Double.valueOf(storedProduct.getLot()));
                }
                Double cost = Double.valueOf(0);
                Double avgCost = Double.valueOf(0);
                Double qty = Double.valueOf(0);
                Double lot = null;
                Date inwardDate = null;
                String status = null;

                Optional<BillInward> billInward = billInwardRepository.findById(storedProduct.getBill_ref_id());
                if (billInward.isPresent()) {

                    logger.info("Bill Inward found for bill_ref_id: {}", storedProduct.getBill_ref_id());

                    inwardDate = billInward.get().getReceivedDate();
                    logger.info("Inward date: {}", inwardDate);

                    if (billInward.get().getProductCategory().equalsIgnoreCase("RawMaterial")) {
                        logger.info("Bill Inward is Raw Material");
                        status = "Y";
                        if (billInward.get().getPerBagCost() != null) {
                            System.out.println("billInwardProd.getUnitPrice() =  " + billInward.get().getPerBagCost());
                            avgCost = Double.valueOf(billInward.get().getPerBagCost());
                            if (storedProduct.getLot() != null) {
                                lot = Double.valueOf(storedProduct.getLot());
                                if (lot > 1) {
                                    qty = binDetails.getAllottedQuantity();
                                    Double avg = qty * avgCost;
                                    cost = avg;
                                } else {
                                    String lotValue = billInward.get().getItemReceived().get(0).getLot();
                                    Double lotVal = Double.valueOf(lotValue);
                                    if (lot == lotVal) {
                                        qty = binDetails.getAllottedQuantity();
                                        Double avg = qty * avgCost;
                                        cost = avg;
                                    } else {
                                        qty = binDetails.getAllottedQuantity();
                                        Double avgCos = avgCost / lotVal;
                                        Double avg = qty * avgCos;
                                        cost = avg;
                                    }
                                }
                            }
                        }
                    }
                }

                if (optionalBinStock.isPresent()) {
                    logger.info("BinStock found for product: {}, batch: {}, bin: {}, lot: {}", storedProduct.getItemId(), storedProduct.getBatchNo(), binDetails.getBinNo());
                    BinStock binStock = optionalBinStock.get();
                    binStock.setBatchId(storedProduct.getBatchId());
                    binStock.setBatchNo(storedProduct.getBatchNo());
                    binStock.setRefId(storedProduct.getItemId());
                    binStock.setBinId(binDetails.getBinNo());
                    binStock.setBinName(binDetails.getBinId());
                    binStock.setStkQty(binDetails.getAllottedQuantity());
                    binStock.setBranch(directPurchase.getBranch().getId());
                    binStock.setDept(binDetails.getDept());
                    binStock.setAvgCost(avgCost);
                    binStock.setAvgCost(avgCost);
                    binStock.setCost(cost);
                    binStock.setGid(godownId);
                    if (storedProduct.getLot() != null) {
                        binStock.setLot(Double.valueOf(storedProduct.getLot()));
                    }
                    logger.info("Lot: {} ", binStock.getLot());
                    if (directPurchase.getCamp() != null) {
                        Optional<Camp> camp = campRepository.findById(directPurchase.getCamp().getId());
                        if (camp.isPresent()) {
                            logger.info("Camp found for campId: {}, cameName: {} ", directPurchase.getCamp().getId(), directPurchase.getCamp().getCamp());
                            binStock.setLocation(camp.get().getState());
                            binStock.setCampName(camp.get().getCamp());
                            binStock.setCategory(camp.get().getState());
                        }
                    } else {
                        logger.info("No camp found");
                    }

                    if (storedProduct.getColor() != null) {
                        Optional<Color> color = colorRepository.findById(storedProduct.getColor().getId());
                        if (color.isPresent()) {
                            logger.info("Color found for colorId: {}, colorName: {} ", storedProduct.getColor().getId(), color.get().getName());
                            binStock.setSeedcolor(color.get().getName());
                        }
                    } else {
                        logger.info("No color found for ");
                    }
                    if (inwardDate != null) {
                        binStock.setInwarDate(inwardDate);
                    }
                    if (status != null) {
                        binStock.setLockStatus(status);
                        binStock.setLockType("WR");
                        binStock.setCategory("WaitingForResult");
                    }
                    binStock.setCid(directPurchase.getFirm_org_id());
                    binStock.setAdate(dateUtil.getISTDate());
                    binStockRepository.save(binStock);
                } else {
                    logger.info("BinStock Create New  itemId : {}, batch: {}, bin: {}, lot: {}", storedProduct.getItemId(), storedProduct.getBatchNo(), binDetails.getBinNo(), storedProduct.getLot());
                    BinStock binStock = new BinStock();
                    binStock.setBatchId(storedProduct.getBatchId());
                    binStock.setBatchNo(storedProduct.getBatchNo());
                    binStock.setRefId(storedProduct.getItemId());
                    binStock.setBinId(binDetails.getBinNo());
                    binStock.setBinName(binDetails.getBinId());
                    binStock.setStkQty(binDetails.getAllottedQuantity());
                    binStock.setBranch(directPurchase.getBranch().getId());
                    binStock.setDept(binDetails.getDept());
                    binStock.setAvgCost(avgCost);
                    binStock.setCost(cost);
                    if (directPurchase.getCamp() != null) {
                        Optional<Camp> camp = campRepository.findById(directPurchase.getCamp().getId());
                        if (camp.isPresent()) {
                            logger.info("Camp found for campId: {}, cameName: {} ", directPurchase.getCamp().getId(), directPurchase.getCamp().getCamp());
                            binStock.setLocation(camp.get().getState());
                            binStock.setCampName(camp.get().getCamp());
                            binStock.setCategory(camp.get().getState());
                        }
                    } else {
                        logger.info("No camp found");
                    }
                    if (storedProduct.getColor() != null) {
                        Optional<Color> color = colorRepository.findById(storedProduct.getColor().getId());
                        if (color.isPresent()) {
                            System.out.println(color.get().getName());
                            binStock.setSeedcolor(color.get().getName());
                        }
                    } else {
                        logger.info("No color found   ");
                    }

                    if (storedProduct.getLot() != null) {
                        binStock.setLot(Double.valueOf(storedProduct.getLot()));
                    }
                    binStock.setGid(godownId);
                    if (inwardDate != null) {
                        binStock.setInwarDate(inwardDate);
                    }
                    if (status != null) {
                        binStock.setLockStatus(status);
                        binStock.setLockType("WR");
                        binStock.setCategory("WaitingForResult");
                    }
                    binStock.setCid(directPurchase.getFirm_org_id());
                    binStock.setAdate(dateUtil.getISTDate());
                    BinStock result = binStockRepository.save(binStock);
                    logger.info("New BinStock created for itemId: {}, batch: {}, bin: {}, lot: {}", storedProduct.getItemId(), storedProduct.getBatchNo(), binDetails.getBinNo(), storedProduct.getLot());
                    updateBinLog(storedProduct.getBill_ref_id(), result, loggedUser);
                    try {
                        logger.info("Updating Bin Stock Audit Log for Stock Inward: itemId: {}, batch: {}, bin: {}, lot: {}", storedProduct.getItemId(), storedProduct.getBatchNo(), binDetails.getBinNo(), storedProduct.getLot());
                        updateBinStockAuditLogForStockInward(directPurchase, storedProduct, result, loggedUser);
                    } catch (Exception e) {
                        logger.info("Error in updating Bin Stock Audit Log for Stock Inward: " + e.getMessage());
                        e.printStackTrace();
                    }

                }
            }
        } else {
            logger.info("No BinNo binNo: {} ", binDetails.getBinNo());
        }
    }


    public void deleteBinStock(DirectPurchase directPurchase, DpEntryStoredProduct storedProduct,
                               DpEntryStoredBinDetails binDetails, User loggedUser) {

        logger.info("Delete BinStock for itemId: {}, batch: {}, bin: {}, lot: {}", storedProduct.getItemId(), storedProduct.getBatchNo(), binDetails.getBinNo(), storedProduct.getLot());
        List<BinStock> binStockList = binStockRepository.findByBinStockDelete(directPurchase.getBranch().getId(),
                binDetails.getDept(), storedProduct.getItemId(), storedProduct.getBatchId(), storedProduct.getBatchNo());
        if (!binStockList.isEmpty()) {
            for (BinStock binStock : binStockList) {
                logger.info("Delete BinStock for itemId: {}, batch: {}, bin: {}, lot: {}", storedProduct.getItemId(), storedProduct.getBatchNo(), binDetails.getBinNo(), storedProduct.getLot());
                binStockRepository.delete(binStock);
            }
        }

        logger.info("Delete BinStockLog for itemId: {}, batch: {}, bin: {}, lot: {}", storedProduct.getItemId(), storedProduct.getBatchNo(), binDetails.getBinNo(), storedProduct.getLot());
        List<BinStockLog> stbinList = binStockLogRepository.getDeleteLogList(storedProduct.getBatchId(), storedProduct.getBatchNo(), "Stock Inward");
        if (!binStockList.isEmpty()) {
            for (BinStockLog binStock : stbinList) {
                logger.info("Delete BinStockLog for itemId: {}, batch: {}, bin: {}, lot: {}", storedProduct.getItemId(), storedProduct.getBatchNo(), binDetails.getBinNo(), storedProduct.getLot());
                binStockLogRepository.delete(binStock);
            }
        }

        logger.info("Delete BinStockAuditLog for itemId: {}, batch: {}, bin: {}, lot: {}", storedProduct.getItemId(), storedProduct.getBatchNo(), binDetails.getBinNo(), storedProduct.getLot());
        List<BinstockAuditLog> auditLogList = binstockAuditLogRepository.getDeleteLogList(storedProduct.getBatchId(), storedProduct.getBatchNo(), "Stock Inward");
        if (!binStockList.isEmpty()) {
            for (BinStockLog binStock : stbinList) {
                logger.info("Delete BinStockAuditLog for itemId: {}, batch: {}, bin: {}, lot: {}", storedProduct.getItemId(), storedProduct.getBatchNo(), binDetails.getBinNo(), storedProduct.getLot());
                binStockLogRepository.delete(binStock);
            }
        }
    }


    public void updateAriaGradeSeedStockOROil(JournalEntry journalEntry, User loggedUser, HashMap<String, Double> stringDoubleHashMap, String cate) {

        System.out.println("trn 3 == " + journalEntry.getTrnno());

        BinStock binStock = new BinStock();
        binStock.setBatchId(journalEntry.getBatchid());
        binStock.setBatchNo(journalEntry.getBatchno());
        binStock.setRefId(journalEntry.getProid());
        binStock.setBinId(journalEntry.getBinid());
        binStock.setBinName(Long.valueOf(journalEntry.getBinname()));
        binStock.setStkQty(journalEntry.getStkqty());
        binStock.setBranch(loggedUser.getBranch_id());
        binStock.setDept(loggedUser.getDepartment().getId());
        binStock.setAvgCost(journalEntry.getAvgcost());
        binStock.setCost(journalEntry.getCost());
        binStock.setLot(journalEntry.getLot());
        binStock.setLTrnNo(journalEntry.getTrnno());
        Optional<GoGroup> goGroupOptional = goGroupRepository.findByBinId(journalEntry.getBinid().trim());
        if (goGroupOptional.isPresent()) {
            binStock.setGid(goGroupOptional.get().getGid());
        }
        binStock.setCid(loggedUser.getOrg_id());
        binStock.setAdate(dateUtil.getISTDate());

        if (!journalEntry.getBatchno().contains("Prdn")) {

            Optional<BinStock> checkBinStock = binStockRepository.findByBatchInwardDateNotNull(journalEntry.getProid(), journalEntry.getBatchno());
            if (checkBinStock.isPresent()) {
                binStock.setLocation(checkBinStock.get().getLocation());
                binStock.setCampName(checkBinStock.get().getCampName());
                binStock.setInwarDate(checkBinStock.get().getInwarDate());
                binStock.setLockStatus(checkBinStock.get().getLockStatus());
                binStock.setLockType(checkBinStock.get().getLockType());
                binStock.setCategory(checkBinStock.get().getCategory());
                binStock.setSeedMoisture(checkBinStock.get().getSeedMoisture());
                binStock.setSeedcolor(checkBinStock.get().getSeedcolor());
                binStock.setOilFFA(checkBinStock.get().getOilFFA());
            } else {
                System.out.println("no binstock update oilffa ");
            }

        }

        binStockRepository.save(binStock);
        updateJournalEntryBinStockAuditLog(journalEntry, loggedUser);
    }

    public void updateBinStockByFFA(String batchNo, String oilffa, Long productId, User loggedUser) {
        if (batchNo != null && oilffa != null && productId != null) {
            List<BinStock> binStockList = binStockRepository.findByProductBatchNo(batchNo, productId);
            for (BinStock binStock : binStockList) {
                if (binStock != null) {
                    System.out.println("oilffa == " + oilffa);
                    Double oilFFACheck = Double.valueOf(oilffa);
                    System.out.println("oilFFACheck == " + oilFFACheck);
                    if (oilFFACheck >= 1.5 && oilFFACheck <= 2) {
                        System.out.println("Oil_FFA_1.5_to_2.0 == " + oilFFACheck);
                        binStock.setCategory("Oil_FFA_1.5_to_2.0");
                    } else if (oilFFACheck > 2) {
                        System.out.println("Oil_FFA_2.0 == " + oilFFACheck);
                        binStock.setCategory("Oil_FFA_2.0");
                    } else {
                        if (binStock.getLocation() != null) {
                            System.out.println(binStock.getLocation() + " == " + oilFFACheck);
                            binStock.setCategory(binStock.getLocation());
                        } else {
                            binStock.setCategory("");
                        }
                    }
                    System.out.println("only == " + oilFFACheck);

                    binStock.setOilFFA(oilFFACheck);
                    binStockRepository.save(binStock);
                }
            }
        }
    }

    public void updateBinStockBySeedMoisture(String batchNo, String seedMoisture, Long productId) {
        if (batchNo != null && seedMoisture != null && productId != null) {
            List<BinStock> binStockList = binStockRepository.findByProductBatchNo(batchNo, productId);
            for (BinStock binStock : binStockList) {
                if (binStock != null) {
                    System.out.println("seedMoisture == " + seedMoisture);
                    binStock.setSeedMoisture(seedMoisture);
                    binStockRepository.save(binStock);
                }
            }
        }
    }

    public void updateBinLog(Long inward, BinStock binStock, User loggedUser) {
        Date currentDate = dateUtil.getISTDate();
        String trnNo = null;
        Long billId = null;
        Date billDate = null;
        if (inward != null) {
            Optional<BillInward> billInward = billInwardRepository.findById(inward);
            if (billInward.isPresent()) {
                trnNo = "BILLS-" + billInward.get().getId();
                billId = billInward.get().getId();
                billDate = billInward.get().getBillDate();
            } else {
                trnNo = "BILLS";
            }
        } else {
            trnNo = "BILLS";
        }
        BinStockLog binStockLog = new BinStockLog(binStock);
        binStockLog.setEntryType("Stock Inward");
        binStockLog.setAby(loggedUser.getId());
        binStockLog.setTrntype("R");
        binStockLog.setAdate(currentDate);
        binStockLog.setTrnDate(currentDate);
        binStockLog.setTrnno(trnNo);
        binStockLog.setBillid(billId);
        binStockLog.setBdt(billDate);
        binStockLog.setUpdatedon(currentDate);
        binStockLog.setAccounting_date(dayEndUtils.getAccountingDate(binStock.getBranch(), currentDate));
        binStockLogRepository.save(binStockLog);
    }

    public void updateBinStockExcess(ProductExcessStorage excessStorage, ExcessShortageProducts storedProduct,
                                     ExcessStoredBinDetails excessStoredBin, User loggedUser) {

        Optional<GoGroup> goGroupOptional = goGroupRepository.findByBinNo(Long.valueOf(excessStoredBin.getBinNo()));
        if (goGroupOptional.isPresent()) {

            Optional<BinStock> binStock = binStockRepository.findByProductBatchAndBin(excessStoredBin.getItemId(),
                    excessStoredBin.getBatchNo(), goGroupOptional.get().getGCode(), goGroupOptional.get().getGoName(), Double.parseDouble(storedProduct.getLot()));
            if (binStock.isPresent()) {
                BinStock binStock1 = binStock.get();
                Double stkQty = binStock1.getStkQty() + Double.valueOf(excessStoredBin.getExcessOrShortage());
                binStock1.setStkQty(Double.valueOf(decfor.format(stkQty)));
                BinStock result = binStockRepository.save(binStock1);
                updateBinLogExcessOrShortage(excessStorage.getBillTransactionno(), result, loggedUser, "Excess", "R", Double.valueOf(excessStoredBin.getExcessOrShortage()));
            }

        }
    }

    public void updateBinStockShortage(ProductExcessStorage excessStorage, ExcessShortageProducts storedProduct,
                                       ExcessStoredBinDetails excessStoredBin, User loggedUser) {

        Optional<GoGroup> goGroupOptional = goGroupRepository.findByBinNo(Long.valueOf(excessStoredBin.getBinNo()));
        if (goGroupOptional.isPresent()) {

            Optional<BinStock> binStock = binStockRepository.findByProductBatchAndBin(excessStoredBin.getItemId(),
                    excessStoredBin.getBatchNo(), goGroupOptional.get().getGCode(), goGroupOptional.get().getGoName(), Double.parseDouble(storedProduct.getLot()));
            if (binStock.isPresent()) {
                BinStock binStock1 = binStock.get();
                Double stkQty = binStock1.getStkQty() - Double.valueOf(excessStoredBin.getExcessOrShortage());
                binStock1.setStkQty(Double.valueOf(decfor.format(stkQty)));
                BinStock result = binStockRepository.save(binStock1);
                updateBinLogExcessOrShortage(excessStorage.getBillTransactionno(), result, loggedUser, "Shortage", "I", Double.valueOf(excessStoredBin.getExcessOrShortage()));
            }
        }
    }

    public void updateBinLogExcessOrShortage(String trnNo, BinStock binStock, User loggedUser, String type, String trnType, Double qty) {
        Date currentDate = dateUtil.getISTDate();
        System.out.println(" stbin updated ");
        BinStockLog binStockLog = new BinStockLog(binStock);
        binStockLog.setQty(qty);
        binStockLog.setAby(loggedUser.getId());
        binStockLog.setTrntype(trnType);
        binStockLog.setEntryType(type);
        binStockLog.setUpdatedon(currentDate);
        binStockLog.setTrnno(trnNo);
        binStockLog.setAdate(currentDate);
        binStockLog.setTrnDate(currentDate);
        binStockLog.setAccounting_date(dayEndUtils.getAccountingDate(binStock.getBranch(), currentDate));
        binStockLogRepository.save(binStockLog);
    }

    public void updateBinLogPurchaseReturn(String trnNo, BinStock binStock, User loggedUser, String type, String trnType) {
        Date currentDate = dateUtil.getISTDate();
        BinStockLog binStockLog = new BinStockLog(binStock);
        binStockLog.setAby(loggedUser.getId());
        binStockLog.setTrntype(trnType);
        binStockLog.setEntryType(type);
        binStockLog.setUpdatedon(currentDate);
        binStockLog.setTrnno(trnNo);
        binStockLog.setAdate(currentDate);
        binStockLog.setTrnDate(currentDate);
        binStockLog.setAccounting_date(dayEndUtils.getAccountingDate(binStock.getBranch(), currentDate));
        binStockLogRepository.save(binStockLog);
    }

    public void updateBinStockPurchaseReturn(PurchaseReturn purchaseReturn, PurchaseReturnProducts storedProduct,
                                             PurchaseReturnStoredBinDetails storedBinDetails, User loggedUser) {

        Optional<GoGroup> goGroupOptional = goGroupRepository.findByBinNo(Long.valueOf(storedBinDetails.getBinNo()));
        if (goGroupOptional.isPresent()) {

            Optional<BinStock> binStock = binStockRepository.findByProductBatchAndBin(storedBinDetails.getItemId(),
                    storedBinDetails.getBatchNo(), goGroupOptional.get().getGCode(), goGroupOptional.get().getGoName(), Double.parseDouble(storedProduct.getLot()));
            if (binStock.isPresent()) {
                BinStock binStock1 = binStock.get();
                Double stkQty = binStock1.getStkQty() - Double.valueOf(storedBinDetails.getExcessOrShortage());
                binStock1.setStkQty(Double.valueOf(decfor.format(stkQty)));
                BinStock result = binStockRepository.save(binStock1);
                ;
                updateBinLogPurchaseReturn(purchaseReturn.getBillTransactionNo(), result, loggedUser, "PurchaseReturn", "I");
            } else {
                logger.error("BinStock error Not found : {}", storedBinDetails.getItemId() + " , " + storedBinDetails.getBatchNo() + " , " + storedBinDetails.getBinNo() + " , " + storedProduct.getLot());
            }
        }
    }


    public void updateBinStockAuditLogForStockInward(DirectPurchase directPurchase,
                                                     DpEntryStoredProduct storedProduct, BinStock binStock,
                                                     User loggedUser) {

        String trnNo = null;
        String entryType = "Stock Inward";
        String narration = null;
        if (storedProduct != null) {
            Optional<BillInward> billInward = billInwardRepository.findById(storedProduct.getBill_ref_id());
            if (billInward.isPresent()) {

                BillInwardDto inwardDto = new BillInwardDto(billInward.get());
                List<BillInwardItemReceives> billInwardItemReceives = billInwardItemReceivedRepository
                        .findBillInwardProducts(inwardDto.getId());
                if (!billInwardItemReceives.isEmpty()) {
                    for (BillInwardItemReceives itemReceives : billInwardItemReceives) {

                        if (itemReceives.getPoId() != null) {
                            Optional<PurchaseOrder> purchaseOrder = purchaseOrderRepository.findById(itemReceives.getPoId());
                            if (purchaseOrder.isPresent()) {
                                if (purchaseOrder.get().getProposalId() != null) {
                                    trnNo = "BILLS-NEW-" + billInward.get().getId();
                                } else {
                                    trnNo = "BILLS-" + billInward.get().getId();
                                }
                                narration = "By Purchases from " + purchaseOrder.get().getVendor().getName()
                                        + "(Bill No :" + billInward.get().getBillNo() + " (Bill Dt :"
                                        + new DateUtil().getDateTOStringWk(billInward.get().getBillDate()) + "))";
                            }
                        }


                        Optional<BinstockAuditLog> binstockAuditLog = binstockAuditLogRepository.findBy(
                                itemReceives.getItemId(), billInward.get().getId(), loggedUser.getBranch_id(),
                                entryType);
                        if (!binstockAuditLog.isPresent()) {
                            BinstockAuditLog binStockLog = new BinstockAuditLog();
                            binStockLog.setItem_id(itemReceives.getItemId());
                            binStockLog.setEntry_type("Stock Inward");
                            if (itemReceives.getQty() != null) {
                                binStockLog.setQty(Double.valueOf(itemReceives.getQty()));
                            } else {
                                binStockLog.setQty(Double.valueOf(0));
                            }
                            binStockLog.setNaration(narration);
                            if (itemReceives.getLot() != null) {
                                binStockLog.setLot(Double.valueOf(itemReceives.getLot()));
                            }
                            binStockLog.setBill_id(billInward.get().getId());
                            binStockLog.setTrn_type("R");
                            binStockLog.setTrnno(trnNo);

                            Date currentDate = dateUtil.getISTDate();
                            binStockLog.setAccouting_date(dayEndUtils.getAccountingDate(directPurchase.getBranch().getId(), currentDate));
                            binStockLog.setUpdate_on(currentDate);
                            binStockLog.setBranch_id(directPurchase.getBranch().getId());
                            binStockLog.setFirm_id(directPurchase.getFirm_org_id());
                            binStockLog.setBinNo(binStock.getBinName());
                            binStockLog.setGodownId(binStock.getGid());

                            Double openingStock = binStockRepository.getCurrentOpeningSock(directPurchase.getBranch().getId(),
                                    itemReceives.getItemId());
                            if (openingStock != null) {
                                Double balance = openingStock + Double.valueOf(itemReceives.getQty());
                                binStockLog.setBalance(balance);
                            } else {
                                binStockLog.setBalance(openingStock);
                            }

                            binstockAuditLogRepository.save(binStockLog);

                        } else {
                            System.out.println("Already update Audit Log");
                        }
                    }
                }
            }
        } else {
            System.out.println("Bill inward not present");
        }
    }

    public void updateBinStockAuditLog(DirectPurchase directPurchase, DpEntryStoredProduct storedProduct, User loggedUser) {

        String trnNo = null;
        String entryType = "Stock Inward";
        String narration = null;
        if (storedProduct != null) {
            Optional<BillInward> billInward = billInwardRepository.findById(storedProduct.getBill_ref_id());
            if (billInward.isPresent()) {

                BillInwardDto inwardDto = new BillInwardDto(billInward.get());
                List<BillInwardItemReceives> billInwardItemReceives = billInwardItemReceivedRepository
                        .findBillInwardProducts(inwardDto.getId());
                if (!billInwardItemReceives.isEmpty()) {
                    for (BillInwardItemReceives itemReceives : billInwardItemReceives) {

                        if (itemReceives.getPoId() != null) {
                            Optional<PurchaseOrder> purchaseOrder = purchaseOrderRepository
                                    .findById(itemReceives.getPoId());
                            if (purchaseOrder.isPresent()) {
                                if (purchaseOrder.get().getProposalId() != null) {
                                    trnNo = "BILLS-NEW-" + billInward.get().getId();
                                } else {
                                    trnNo = "BILLS-" + billInward.get().getId();
                                }
                                narration = "By Purchases from " + purchaseOrder.get().getVendor().getName()
                                        + "(Bill No :" + billInward.get().getBillNo() + " (Bill Dt :"
                                        + new DateUtil().getDateTOStringWk(billInward.get().getBillDate()) + "))";
                            }
                        }


                        Optional<BinstockAuditLog> binstockAuditLog = binstockAuditLogRepository.findBy(
                                itemReceives.getItemId(), billInward.get().getId(), loggedUser.getBranch_id(),
                                entryType);
                        if (!binstockAuditLog.isPresent()) {
                            BinstockAuditLog binStockLog = new BinstockAuditLog();
                            binStockLog.setItem_id(itemReceives.getItemId());
                            binStockLog.setEntry_type("Stock Inward");
                            if (itemReceives.getQty() != null) {
                                binStockLog.setQty(Double.valueOf(itemReceives.getQty()));
                            } else {
                                binStockLog.setQty(Double.valueOf(0));
                            }
                            binStockLog.setNaration(narration);
                            if (itemReceives.getLot() != null) {
                                binStockLog.setLot(Double.valueOf(itemReceives.getLot()));
                            }
                            binStockLog.setBill_id(billInward.get().getId());
                            binStockLog.setTrn_type("R");
                            binStockLog.setTrnno(trnNo);

                            Date currentDate = dateUtil.getISTDate();
                            binStockLog.setAccouting_date(dayEndUtils.getAccountingDate(directPurchase.getBranch().getId(), currentDate));
                            binStockLog.setUpdate_on(currentDate);
                            binStockLog.setBranch_id(directPurchase.getBranch().getId());
                            binStockLog.setFirm_id(directPurchase.getFirm_org_id());
                            
                            Double openingStock = binStockRepository.getCurrentOpeningSock(directPurchase.getBranch().getId(),
                                    itemReceives.getItemId());
                            if (openingStock != null) {
                                Double balance = openingStock + Double.valueOf(itemReceives.getQty());
                                binStockLog.setBalance(balance);
                            } else {
                                binStockLog.setBalance(openingStock);
                            }

                            binstockAuditLogRepository.save(binStockLog);

                        } else {
                            System.out.println("Already update Audit Log");
                        }
                    }
                }
            }
        } else {
            System.out.println("Bill inward not present");
        }
    }

    public void updateBinStockAuditLog(ProductExcessStorage storedProduct, PurchaseReturn purchaseReturn, String type,
                                       User loggedUser) {

        if (type.equalsIgnoreCase("ShortageOrExcess")) {
            List<ExcessOrShortageOrReturn> excessOrShortageOrReturns = productExcessStorageRepository
                    .findBy(storedProduct.getId(), storedProduct.getVendor().getId(), storedProduct.getBillNo());
            for (ExcessOrShortageOrReturn excessOrShortageOrReturn : excessOrShortageOrReturns) {
                Optional<BillInward> optional = billInwardRepository.findByBillNoAndVendorIdBillDate(
                        excessOrShortageOrReturn.getBillNo(), storedProduct.getVendor().getId(), excessOrShortageOrReturn.getBillDate());
                if (optional.isPresent()) {
                    updateAuditLog(optional.get(), excessOrShortageOrReturn, storedProduct.getVendor().getName(),
                            "ShortageOrExcess", new DateUtil().getDateTOStringWk(storedProduct.getBillDate()),
                            loggedUser);
                }
            }

        } else if (type.equalsIgnoreCase("PurchaseReturn")) {
            List<ExcessOrShortageOrReturn> excessOrShortageOrReturns = purchaseReturnRepository
                    .findBy(purchaseReturn.getVendor().getId(), purchaseReturn.getBillNo());
            for (ExcessOrShortageOrReturn excessOrShortageOrReturn : excessOrShortageOrReturns) {
                Optional<BillInward> optional = billInwardRepository.findByBillNoAndVendorIdBillDate(purchaseReturn.getBillNo(),
                        purchaseReturn.getVendor().getId(), purchaseReturn.getBillDate());
                if (optional.isPresent()) {
                    updateAuditLog(optional.get(), excessOrShortageOrReturn, purchaseReturn.getVendor().getName(),
                            "PurchaseReturn", new DateUtil().getDateTOStringWk(purchaseReturn.getBillDate()),
                            loggedUser);
                }
            }
        }

    }

    public void updateAuditLog(BillInward billInward, ExcessOrShortageOrReturn storedProduct, String vendorName,
                               String type, String billDate, User loggedUser) {

        String trnNo = null;
        String narration = null;
        String entryType = null;
        String trnType = null;
        if (vendorName == null) {
            vendorName = billInward.getVendor().getName();
        }
        if (storedProduct != null) {

            if (vendorName != null) {

                if (type.equalsIgnoreCase("ShortageOrExcess")) {
                    if (storedProduct.getType().equalsIgnoreCase("Excess")) {
                        trnNo = "Excess-" + storedProduct.getBillNo();
                        entryType = "Excess";
                        trnType = "R";
                    } else {
                        trnNo = "Shortage-" + storedProduct.getBillNo();
                        entryType = "Shortage";
                        trnType = "I";
                    }
                    narration = "By " + entryType + " from " + vendorName + "(Bill No :" + storedProduct.getBillNo()
                            + " (Bill Dt :" + billDate + "))";
                } else if (type.equalsIgnoreCase("PurchaseReturn")) {
                    trnNo = "PurchaseReturn-" + storedProduct.getBillNo();
                    entryType = "PurchaseReturn";
                    trnType = "I";
                    narration = "By " + entryType + " from " + vendorName + "(Bill No :" + storedProduct.getBillNo()
                            + " (Bill Dt :" + billDate + "))";
                } else {
                    throw new RuntimeException();
                }
            }
            Date currentDate = dateUtil.getISTDate();
            Optional<BinstockAuditLog> binstockAuditLog = binstockAuditLogRepository.findBy(storedProduct.getItemId(),
                    billInward.getId(), loggedUser.getBranch_id(), entryType);
            if (!binstockAuditLog.isPresent()) {
                BinstockAuditLog binStockLog = new BinstockAuditLog();
                binStockLog.setItem_id(storedProduct.getItemId());
                binStockLog.setEntry_type(entryType);
                if (storedProduct.getQty() != null) {
                    binStockLog.setQty(Double.valueOf(storedProduct.getQty()));
                } else {
                    binStockLog.setQty(Double.valueOf(0));
                }
                binStockLog.setNaration(narration);
                binStockLog.setLot(Double.valueOf(storedProduct.getLot()));
                binStockLog.setBill_id(billInward.getId());
                binStockLog.setTrn_type(trnType);
                binStockLog.setTrnno(trnNo);


                binStockLog.setAccouting_date(dayEndUtils.getAccountingDate(loggedUser.getBranch_id(), currentDate));
                binStockLog.setUpdate_on(currentDate);
                binStockLog.setBranch_id(loggedUser.getBranch_id());
                binStockLog.setFirm_id(loggedUser.getOrg_id());

                Double openingStock = binStockRepository.getCurrentOpeningSock(loggedUser.getBranch_id(),
                        storedProduct.getItemId());
                if (openingStock != null) {

                    if (type.equalsIgnoreCase("ShortageOrExcess")) {
                        if (storedProduct.getType().equalsIgnoreCase("Excess")) {
                            Double balance = openingStock + Double.valueOf(storedProduct.getQty());
                            binStockLog.setBalance(balance);
                        } else {
                            Double balance = openingStock - Double.valueOf(storedProduct.getQty());
                            binStockLog.setBalance(balance);
                        }
                    } else if (type.equalsIgnoreCase("PurchaseReturn")) {
                        Double balance = openingStock - Double.valueOf(storedProduct.getQty());
                        binStockLog.setBalance(balance);
                    }

                } else {
                    binStockLog.setBalance(openingStock);
                }
                binStockLog.setFirm_id(loggedUser.getOrg_id());

                binStockLog.setUpdate_on(currentDate);
                binstockAuditLogRepository.save(binStockLog);
            } else {
                System.out.println("Already update Audit Log");
            }
        }
    }

    public void updateBinStockLockRelease(String batchNo, Long productId) {
        if (batchNo != null && productId != null) {
            List<BinStock> binStockList = binStockRepository.findByProductBatchNo(batchNo, productId);
            for (BinStock binStock : binStockList) {
                if (binStock != null) {
                    if (binStock.getLockType() != null && binStock.getLockType().equalsIgnoreCase("WR")) {

                        System.out.println("category : " + binStock.getCategory());

                        if (binStock.getCategory().equalsIgnoreCase("Oil_FFA_2.0")) {
                            binStock.setCategory("Oil_FFA_2.0");
                        } else if (binStock.getCategory().equalsIgnoreCase("Oil_FFA_1.5_to_2.0")) {
                            binStock.setCategory("Oil_FFA_1.5_to_2.0");
                        } else if (binStock.getLocation() != null) {
                            binStock.setCategory(binStock.getLocation());
                        }

                        Optional<BinStatus> binLockOptional = binStatusRepository.findBy(binStock.getBinName(), true);
                        if (binLockOptional.isPresent()) {
                            binStock.setLockStatus("Y");
                            binStock.setLockType(binLockOptional.get().getLocktype());
                        } else {
                            binStock.setLockStatus("N");
                            binStock.setLockType("");
                        }

                        System.out.println("category : " + binStock.getCategory());
                        binStockRepository.save(binStock);
                    }
                }
            }
        }
    }

    public void updateJournalEntryStockAuditLog(JournalEntry journalEntry, Double stkQty, BinStock binstock,
                                                User loggedUser) {
        System.out.println("JournalEntry  == Audit log");
        System.out.println("JournalEntry  == " + journalEntry.getProid());
        System.out.println("JournalEntry  == ");
        String entryType = "PRODN";
        String narration = null;
        Date currentDate = dateUtil.getISTDate();
        if (journalEntry.getTrnno() != null) {

            narration = "By PRODN Issue from " + journalEntry.getBinid() + "(Bin No :" + journalEntry.getBinname()
                    + " (Bill inward Dt :" + new DateUtil().getDateTOStringWk(binstock.getInwarDate()) + "))";

            //Optional<BinstockAuditLog> binstockAuditLog = binstockAuditLogRepository.findBy(journalEntry.getProid(), journalEntry.getBatchno(), loggedUser.getBranch_id(), entryType);
            //if (!binstockAuditLog.isPresent()) {
            BinstockAuditLog binStockLog = new BinstockAuditLog();
            binStockLog.setItem_id(journalEntry.getProid());
            if (stkQty != null) {
                binStockLog.setQty(stkQty);
            } else {
                binStockLog.setQty(Double.valueOf(0));
            }
            binStockLog.setLot(journalEntry.getLot());
            binStockLog.setNaration(narration);
            binStockLog.setBatch_id(journalEntry.getBatchid());
            binStockLog.setBatchno(journalEntry.getBatchno());
            binStockLog.setEntry_type(entryType);
            binStockLog.setTrn_type("I");
            binStockLog.setTrnno(journalEntry.getTrnno());
            binStockLog.setAccouting_date(journalEntry.getAccounting_date());
            binStockLog.setUpdate_on(currentDate);
            binStockLog.setBranch_id(loggedUser.getBranch_id());
            binStockLog.setFirm_id(loggedUser.getOrg_id());
            Double openingStock = binStockRepository.getCurrentOpeningSock(loggedUser.getBranch_id(), journalEntry.getProid());
            binStockLog.setBalance(openingStock);
            binStockLog.setFirm_id(loggedUser.getOrg_id());
            binStockLog.setUpdate_on(currentDate);
            binstockAuditLogRepository.save(binStockLog);
           /* } else {
                System.out.println("Already update Audit Log");
            }*/

        } else {
            System.out.println("TRN No Missing");
        }

    }


    public void updateJournalEntryBinStockAuditLog(JournalEntry journalEntry, User loggedUser) {
        System.out.println("JournalEntry  == Audit log");
        System.out.println("JournalEntry  == " + journalEntry.getProid());
        System.out.println("JournalEntry  == ");
        String trnNo = null;
        String entryType = "PRODN";
        String narration = null;
        Date currentDate = dateUtil.getISTDate();
        if (journalEntry.getTrnno() != null) {

            narration = "By PRODN RECEIPT from " + journalEntry.getBinid() + "(Bin No :" + journalEntry.getBinname()
                    + " (production inward Dt :" + new DateUtil().getDateTOStringWk(currentDate) + "))";

            Optional<BinstockAuditLog> binstockAuditLog = binstockAuditLogRepository.findBy(journalEntry.getProid(),
                    journalEntry.getBatchno(), loggedUser.getBranch_id(), entryType);
            if (!binstockAuditLog.isPresent()) {
                BinstockAuditLog binStockLog = new BinstockAuditLog();
                binStockLog.setItem_id(journalEntry.getProid());
                binStockLog.setQty(journalEntry.getStkqty());
                binStockLog.setNaration(narration);
                binStockLog.setLot(journalEntry.getLot());
                binStockLog.setBatch_id(journalEntry.getBatchid());
                binStockLog.setBatchno(journalEntry.getBatchno());
                binStockLog.setEntry_type(entryType);
                binStockLog.setTrn_type("R");
                binStockLog.setTrnno(journalEntry.getTrnno());
                binStockLog.setAccouting_date(journalEntry.getAccounting_date());
                binStockLog.setUpdate_on(currentDate);
                binStockLog.setBranch_id(loggedUser.getBranch_id());
                binStockLog.setFirm_id(loggedUser.getOrg_id());
                Double openingStock = binStockRepository.getCurrentOpeningSock(loggedUser.getBranch_id(),
                        journalEntry.getProid());
                binStockLog.setBalance(openingStock);
                binStockLog.setFirm_id(loggedUser.getOrg_id());
                binStockLog.setUpdate_on(currentDate);
                binstockAuditLogRepository.save(binStockLog);
            } else {
                System.out.println("Already update Audit Log");
            }

        } else {
            System.out.println("TRN No Missing");
        }

    }


    public void updateReduceBinLogProduction(JournalEntry journalEntry, BinStock binStock, Double qty, User loggedUser, String type, String trnType) {
        Date currentDate = dateUtil.getISTDate();
        System.out.println(" stbin updated Production batch");
        BinStockLog binStockLog = new BinStockLog();
        binStockLog.setRefid(binStock.getRefId());
        binStockLog.setTrnno(journalEntry.getTrnno());
        binStockLog.setGname(journalEntry.getBinname());
        binStockLog.setGcode(journalEntry.getBinid());
        binStockLog.setGid(binStock.getGid());
        binStockLog.setAvgcost(binStock.getAvgCost());
        if (binStock.getAvgCost() != null) {
            Double cost = qty * binStock.getAvgCost();
            binStockLog.setCost(cost);
        }
        binStockLog.setBatchid(binStock.getBatchId());
        binStockLog.setBatchno(binStock.getBatchNo());
        binStockLog.setCid(binStock.getCid());
        binStockLog.setSjid(journalEntry.getSjid());
        binStockLog.setQty(qty);
        binStockLog.setLot(binStock.getLot());
        binStockLog.setAby(loggedUser.getId());
        binStockLog.setTrntype(trnType);
        binStockLog.setEntryType(type);
        binStockLog.setUpdatedon(currentDate);
        binStockLog.setAdate(currentDate);
        binStockLog.setTrnDate(currentDate);
        binStockLog.setAccounting_date(dayEndUtils.getAccountingDate(loggedUser.getBranch_id(), currentDate));
        binStockLog.setBranch(loggedUser.getBranch_id());
        binStockLog.setFmid(loggedUser.getBranch_id());
        binStockLog.setFmtable("branch");
        binStockLog.setToid(loggedUser.getBranch_id());
        binStockLog.setTotable("branch");
        binStockLog.setDept(loggedUser.getDepartment().getId());
        binStockLogRepository.save(binStockLog);
    }


    public void updateBinLogProduction(JournalEntry journalEntry, User loggedUser, String type, String trnType) {
        Date currentDate = dateUtil.getISTDate();
        System.out.println(" stbin updated");
        BinStockLog binStockLog = new BinStockLog();
        binStockLog.setRefid(journalEntry.getProid());
        binStockLog.setTrnno(journalEntry.getTrnno());
        binStockLog.setGname(journalEntry.getBinname());
        binStockLog.setGcode(journalEntry.getBinid());
        if(journalEntry.getBinname() != null) {
            Optional<GoGroup> groupOptional = goGroupRepository.findByBinNo(Long.valueOf(journalEntry.getBinname()));
            if (groupOptional.isPresent()) {
            	binStockLog.setGid(groupOptional.get().getGid()); 
            }}     
        binStockLog.setAvgcost(journalEntry.getAvgcost());
        binStockLog.setCost(journalEntry.getCost());
        binStockLog.setBatchid(journalEntry.getBatchid());
        binStockLog.setBatchno(journalEntry.getBatchno());
        binStockLog.setCid(loggedUser.getOrg_id());
        binStockLog.setSjid(journalEntry.getSjid());
        binStockLog.setQty(journalEntry.getStkqty());
        binStockLog.setLot(journalEntry.getLot());
        binStockLog.setAby(loggedUser.getId());
        binStockLog.setTrntype(trnType);
        binStockLog.setEntryType(type);
        binStockLog.setUpdatedon(currentDate);
        binStockLog.setAdate(currentDate);
        binStockLog.setTrnDate(currentDate);
        binStockLog.setAccounting_date(dayEndUtils.getAccountingDate(loggedUser.getBranch_id(), currentDate));
        binStockLog.setDept(loggedUser.getDepartment().getId());
        binStockLog.setBranch(loggedUser.getBranch_id());
        binStockLog.setFmid(loggedUser.getBranch_id());
        binStockLog.setFmtable("branch");
        binStockLog.setToid(loggedUser.getBranch_id());
        binStockLog.setTotable("branch");
        binStockLogRepository.save(binStockLog);
    }

    public void updateBinLogMaterialTransaction(MaterialIssue issue, BinStock binStock, User loggedUser, String trnType) {
        Date currentDate = dateUtil.getISTDate();
        System.out.println(" stbin updated");
        BinStockLog binStockLog = new BinStockLog(binStock);
        binStockLog.setAby(loggedUser.getId());
        binStockLog.setRefid(binStock.getRefId());
        binStockLog.setGcode(binStock.getBinId());
        binStockLog.setGname(binStock.getBinName());
        binStockLog.setGid(binStock.getGid());
        binStockLog.setCid(issue.getOrganisation().getId());
        binStockLog.setDept(issue.getIssueDepartment().getId());
        binStockLog.setQty(binStock.getStkQty());
        binStockLog.setTrntype(trnType);
        binStockLog.setEntryType("MATERIAL_TRANSACTION");
        binStockLog.setUpdatedon(currentDate);
        binStockLog.setTrnno(binStock.getLTrnNo());
        binStockLog.setBatchid(binStock.getBatchId());
        binStockLog.setBatchno(binStock.getBatchNo());
        binStockLog.setLot(binStock.getLot());
        binStockLog.setCost(binStock.getCost());
        binStockLog.setAvgcost(binStock.getAvgCost());
        binStockLog.setBranch(binStock.getBranch());
        binStockLog.setFmid(issue.getIssueBranch().getId());
        binStockLog.setFmtable("branch");
        binStockLog.setToid(issue.getRequestBranch().getId());
        binStockLog.setTotable("branch");
        binStockLog.setAdate(currentDate);
        binStockLog.setTrnDate(currentDate);
        binStockLog.setAccounting_date(dayEndUtils.getAccountingDate(binStock.getBranch(), currentDate));
        binStockLogRepository.save(binStockLog);
        if (trnType.equalsIgnoreCase("I")) {
            updateMaterialTransActionONIssueBinStockAuditLog(binStock, loggedUser, "MaterialIssue", "I");
        } else {
            updateMaterialTransONReceiveActionBinStockAuditLog(binStock, loggedUser, "MaterialReceive", "R");
        }

    }


    public void updateBinLogDispatch(BinStock binStock, User loggedUser, String trnType) {
        Date currentDate = dateUtil.getISTDate();
        System.out.println(" stbin updated");
        BinStockLog binStockLog = new BinStockLog(binStock);
        binStockLog.setAby(loggedUser.getId());
        binStockLog.setTrntype(trnType);
        binStockLog.setEntryType("DISPATCH");
        binStockLog.setUpdatedon(currentDate);
        binStockLog.setTrnno(binStock.getLTrnNo());
        binStockLog.setAdate(currentDate);
        binStockLog.setTrnDate(currentDate);
        binStockLog.setAccounting_date(dayEndUtils.getAccountingDate(binStock.getBranch(), currentDate));
        binStockLogRepository.save(binStockLog);
    }

    public void updateMaterialTransActionONIssueBinStockAuditLog(BinStock binStock, User loggedUser, String entryType,
                                                                 String type) {

        String narration = null;
        Date currentDate = dateUtil.getISTDate();
        if (binStock.getLTrnNo() != null) {

            narration = "By MRN ISSUE from " + binStock.getBinId() + "(Bin No :" + binStock.getBinName()
                    + " (Material issue Dt :" + new DateUtil().getDateTOStringWk(currentDate) + "))";

            Optional<BinstockAuditLog> binstockAuditLog = binstockAuditLogRepository.findBy(binStock.getRefId(),
                    binStock.getBatchNo(), loggedUser.getBranch_id(), entryType, binStock.getLTrnNo());
            if (!binstockAuditLog.isPresent()) {
                BinstockAuditLog binStockLog = new BinstockAuditLog();
                binStockLog.setItem_id(binStock.getRefId());
                binStockLog.setQty(binStock.getStkQty());
                binStockLog.setNaration(narration);
                binStockLog.setLot(binStock.getLot());
                binStockLog.setBatch_id(binStock.getBatchId());
                binStockLog.setBatchno(binStock.getBatchNo());
                binStockLog.setEntry_type(entryType);
                binStockLog.setTrn_type(type);
                binStockLog.setTrnno(binStock.getLTrnNo());

                binStockLog.setAccouting_date(dayEndUtils.getAccountingDate(loggedUser.getBranch_id(), currentDate));
                binStockLog.setUpdate_on(currentDate);
                binStockLog.setBranch_id(loggedUser.getBranch_id());
                binStockLog.setFirm_id(loggedUser.getOrg_id());
                binStockLog.setUpdate_on(currentDate);
                binStockLog.setBinNo(binStock.getBinName());
                binStockLog.setGodownId(binStock.getGid());
                binstockAuditLogRepository.save(binStockLog);
            } else {
                System.out.println("Already update Audit Log");
            }

        } else {
            System.out.println("TRN No Missing");
        }

    }


    public void updateMaterialTransONReceiveActionBinStockAuditLog(BinStock binStock, User loggedUser, String entryType,
                                                                   String type) {

        String narration = null;
        Date currentDate = dateUtil.getISTDate();
        if (binStock.getLTrnNo() != null) {

            narration = "By MRN RECEIVE from " + binStock.getBinId() + "(Bin No :" + binStock.getBinName()
                    + " (Material receive Dt :" + new DateUtil().getDateTOStringWk(currentDate) + "))";

            Optional<BinstockAuditLog> binstockAuditLog = binstockAuditLogRepository.findBy(binStock.getRefId(),
                    binStock.getBatchNo(), loggedUser.getBranch_id(), entryType, binStock.getLTrnNo());
            if (!binstockAuditLog.isPresent()) {
                BinstockAuditLog binStockLog = new BinstockAuditLog();
                binStockLog.setItem_id(binStock.getRefId());
                binStockLog.setQty(binStock.getStkQty());
                binStockLog.setNaration(narration);
                binStockLog.setLot(binStock.getLot());
                binStockLog.setBatch_id(binStock.getBatchId());
                binStockLog.setBatchno(binStock.getBatchNo());
                binStockLog.setEntry_type(entryType);
                binStockLog.setTrn_type(type);
                binStockLog.setTrnno(binStock.getLTrnNo());
                binStockLog.setAccouting_date(dayEndUtils.getAccountingDate(loggedUser.getBranch_id(), currentDate));
                binStockLog.setUpdate_on(currentDate);
                binStockLog.setBranch_id(binStock.getBranch());
                binStockLog.setFirm_id(loggedUser.getOrg_id());
                binStockLog.setUpdate_on(currentDate);
                binStockLog.setBinNo(binStock.getBinName());
                binStockLog.setGodownId(binStock.getGid());
                binstockAuditLogRepository.save(binStockLog);
            } else {
                System.out.println("Already update Audit Log");
            }

        } else {
            System.out.println("TRN No Missing");
        }
    }


    public void updateSplitTrnReduceStock(SplitProductDetails splitProduct, SplitTransaction splitTrn, User loggedUser) {

        Optional<BinStock> optionalBinStock = binStockRepository.findBySplit(splitTrn.getBinname(), splitProduct.getBsk_batch_no(),splitTrn.getBid(),splitTrn.getDid());
        if (optionalBinStock.isPresent()) {

            BinStock binStock = optionalBinStock.get();

            Double stkqty = binStock.getStkQty();
            Double sqty = splitProduct.getSelected_qty();

            Double finalQty = stkqty - sqty;

            binStock.setStkQty(Double.valueOf(decfor.format(finalQty)));

            BinStock rs = binStockRepository.save(binStock);
            updateReduceBinLogSplit(splitProduct, splitTrn, loggedUser);
            updateSplitBinStockAuditLog(rs, splitProduct, splitTrn, loggedUser);

        }
    }

    public BinStock updateSplitTrnStock(SplitProductDetails splitProduct, SplitTransaction splitTrn, User loggedUser) {

        BinStock binStock = new BinStock();
        binStock.setBatchId(splitProduct.getBatch_id());
        binStock.setBatchNo(splitProduct.getBatch_no());
        binStock.setRefId(splitProduct.getProduct_id());
        Optional<GoGroup> goGroupOptional = goGroupRepository.findByBinNo(splitProduct.getBinno());
        if (goGroupOptional.isPresent()) {
            binStock.setBinId(goGroupOptional.get().getGCode());
            binStock.setBinName(splitProduct.getBinno());
            binStock.setGid(goGroupOptional.get().getGid());
        }

        binStock.setStkQty(splitProduct.getQuantity());
        binStock.setBranch(loggedUser.getBranch_id());
        binStock.setDept(loggedUser.getDepartment().getId());
        binStock.setLTrnNo(splitTrn.getTrnno());
        binStock.setBranch(splitTrn.getBid());
        binStock.setDept(splitTrn.getDid());
        binStock.setLot(splitProduct.getLot());
        binStock.setCid(loggedUser.getOrg_id());
        binStock.setAdate(dateUtil.getISTDate());
        BinStock rs = binStockRepository.save(binStock);
        updateBinLogSplit(splitProduct, splitTrn, loggedUser);
        updateSplitBinStockAuditLog(rs, loggedUser);
        return rs;
    }


    public void updateReduceBinLogSplit(SplitProductDetails product, SplitTransaction splitTrn, User loggedUser) {
        Date currentDate = dateUtil.getISTDate();
        System.out.println(" stbin updated");
        BinStockLog binStockLog = new BinStockLog();
        binStockLog.setRefid(splitTrn.getRefid());
        binStockLog.setTrnno(splitTrn.getTrnno());
        binStockLog.setGname(splitTrn.getBinname());
        Optional<GoGroup> goGroupOptional = goGroupRepository.findByBinNo(splitTrn.getBinname());
        if (goGroupOptional.isPresent()) {
            binStockLog.setGcode(goGroupOptional.get().getGCode());
            binStockLog.setGid(goGroupOptional.get().getGid());
        }
        //binStockLog.setAvgcost(journalEntry.getAvgcost());
        //binStockLog.setCost(journalEntry.getCost());
        binStockLog.setBatchid(product.getBsk_batch_id());
        binStockLog.setBatchno(product.getBsk_batch_no());
        binStockLog.setCid(loggedUser.getOrg_id());
        binStockLog.setBranch(loggedUser.getBranch_id());
        binStockLog.setQty(product.getQuantity());
        binStockLog.setLot(product.getLot());
        binStockLog.setAby(loggedUser.getId());
        //binStockLog.setTrntype(trnType);
        binStockLog.setEntryType(product.getSplit_type());
        binStockLog.setUpdatedon(currentDate);
        binStockLog.setAdate(currentDate);
        binStockLog.setTrnDate(currentDate);
        binStockLog.setAccounting_date(dayEndUtils.getAccountingDate(loggedUser.getBranch_id(), currentDate));
        binStockLogRepository.save(binStockLog);
    }

    public void updateBinLogSplit(SplitProductDetails product, SplitTransaction splitTrn, User loggedUser) {
        Date currentDate = dateUtil.getISTDate();
        System.out.println(" stbin updated");
        BinStockLog binStockLog = new BinStockLog();
        binStockLog.setRefid(product.getProduct_id());
        binStockLog.setTrnno(splitTrn.getTrnno());
        binStockLog.setGname(product.getBinno());
        binStockLog.setBranch(product.getBranch());
        binStockLog.setDept(product.getDepartment());
        Optional<GoGroup> goGroupOptional = goGroupRepository.findByBinNo(product.getBinno());
        if (goGroupOptional.isPresent()) {
            binStockLog.setGcode(goGroupOptional.get().getGCode());
            binStockLog.setGid(goGroupOptional.get().getGid());
        }
        //binStockLog.setAvgcost(journalEntry.getAvgcost());
        //binStockLog.setCost(journalEntry.getCost());
        binStockLog.setBatchid(product.getBatch_id());
        binStockLog.setBatchno(product.getBatch_no());
        binStockLog.setCid(loggedUser.getOrg_id());
        binStockLog.setQty(product.getQuantity());
        binStockLog.setLot(product.getLot());
        binStockLog.setAby(loggedUser.getId());
        //binStockLog.setTrntype(trnType);
        binStockLog.setEntryType(product.getSplit_type());
        binStockLog.setUpdatedon(currentDate);
        binStockLog.setAdate(currentDate);
        binStockLog.setTrnDate(currentDate);
        binStockLog.setAccounting_date(dayEndUtils.getAccountingDate(product.getBranch(), currentDate));
        binStockLogRepository.save(binStockLog);
    }


    public void updateSalesReturnStock(SalesReturnDto salesReturn, User loggedUser) {

        String type = salesReturn.getReturnType();


        if (!salesReturn.getSalesReturnProducts().isEmpty()) {
            for (SalesReturnProductsDto salesReturnProducts : salesReturn.getSalesReturnProducts()) {

                if (!salesReturnProducts.getBinDetails().isEmpty()) {
                    for (SalesReturnBinDetailsDto salesReturnBinDetails : salesReturnProducts.getBinDetails()) {

                        Optional<BinStock> binStockOptional = binStockRepository.findByProductBatchAndBin(salesReturnProducts.getProductId(), salesReturnProducts.getBatchNo(), salesReturnProducts.getBatchId(), salesReturnBinDetails.getBinNo(), salesReturnProducts.getLot());
                        BinStock binStock = null;
                        if (binStockOptional.isPresent()) {
                            binStock = new BinStock();
                            binStock.setBatchId(salesReturnProducts.getBatchId());
                            binStock.setBatchNo(salesReturnProducts.getBatchNo());
                            binStock.setRefId(salesReturnProducts.getProductId());
                            binStock.setBinName(salesReturnBinDetails.getBinNo());
                            binStock.setStkQty(Double.valueOf(salesReturnBinDetails.getAllocatedQty()));
                            binStock.setBranch(salesReturn.getBranch());
                            binStock.setDept(salesReturn.getDept());
                            binStock.setLot(salesReturnProducts.getLot());

                            Optional<GoGroup> goGroupOptional = goGroupRepository.findByBinNo(salesReturnBinDetails.getBinNo());
                            if (goGroupOptional.isPresent()) {
                                binStock.setGid(goGroupOptional.get().getGid());
                                binStock.setBinId(goGroupOptional.get().getGCode());
                            }

                            binStock.setCid(salesReturn.getFirmId());
                            binStock.setAdate(dateUtil.getISTDate());
                            binStock.setLTrnNo(salesReturn.getTrnNo());
                            binStock.setAvgCost(binStockOptional.get().getAvgCost());
                            Double cost = binStockOptional.get().getAvgCost() * binStock.getStkQty();
                            binStock.setCost(cost);

                        } else {

                            Optional<BinStock> binStockOptional1 = binStockRepository.findByProductBatchAndBin(salesReturnProducts.getProductId(), salesReturnProducts.getBatchNo(), salesReturnProducts.getLot());
                            binStock = new BinStock();
                            binStock.setBatchId(salesReturnProducts.getBatchId());
                            binStock.setBatchNo(salesReturnProducts.getBatchNo());
                            binStock.setRefId(salesReturnProducts.getProductId());
                            binStock.setBinName(salesReturnBinDetails.getBinNo());
                            binStock.setStkQty(Double.valueOf(salesReturnBinDetails.getAllocatedQty()));
                            binStock.setBranch(salesReturn.getBranch());
                            binStock.setDept(salesReturn.getDept());
                            binStock.setLot(salesReturnProducts.getLot());
                            Optional<GoGroup> goGroupOptional = goGroupRepository.findByBinNo(salesReturnBinDetails.getBinNo());
                            if (goGroupOptional.isPresent()) {
                                binStock.setGid(goGroupOptional.get().getGid());
                                binStock.setBinId(goGroupOptional.get().getGCode());
                            }
                            binStock.setCid(salesReturn.getFirmId());
                            binStock.setAdate(dateUtil.getISTDate());
                            binStock.setLTrnNo(salesReturn.getTrnNo());

                            if (binStockOptional1.isPresent()) {
                                binStock.setAvgCost(binStockOptional1.get().getAvgCost());
                                Double cost = binStockOptional1.get().getAvgCost() * binStock.getStkQty();
                                binStock.setCost(cost);
                            }

                        }

                        BinStock rsBinStock = binStockRepository.save(binStock);
                        updateSalesReturnBinStockAuditLog(rsBinStock, loggedUser, type);
                        updateBinLogSalesReturn(rsBinStock, loggedUser, type);
                    }
                }
            }
        }

    }


    public void updateBinLogSalesReturn(BinStock binStock, User loggedUser, String type) {
        Date currentDate = dateUtil.getISTDate();
        System.out.println(" stbin updated");
        BinStockLog binStockLog = new BinStockLog(binStock);
        binStockLog.setAby(loggedUser.getId());
        binStockLog.setTrntype("SalesReturn");

        if (type.equalsIgnoreCase("saleReturn")) {
            binStockLog.setEntryType("SALES_RETURN");
        } else {
            binStockLog.setEntryType("SELF_TRANSFER");
        }

        binStockLog.setUpdatedon(currentDate);
        binStockLog.setTrnno(binStock.getLTrnNo());
        binStockLog.setAdate(currentDate);
        binStockLog.setTrnDate(currentDate);
        binStockLog.setAccounting_date(dayEndUtils.getAccountingDate(binStock.getBranch(), currentDate));
        binStockLogRepository.save(binStockLog);
    }


    public void updateSalesReturnBinStockAuditLog(BinStock binStock, User loggedUser, String type) {

        System.out.println(" SalesReturn  == Audit log");
        System.out.println(" SalesReturn  == " + binStock.getRefId());
        System.out.println(" SalesReturn  == ");
        String entryType = null;
        String ntype = null;
        if (type.equalsIgnoreCase("saleReturn")) {
            entryType = "SALESRETURN";
            ntype = "Sales Return";
        } else {
            entryType = "SELFTRANSFER";
            ntype = "Self Transfer";
        }
        String narration = null;
        Date currentDate = dateUtil.getISTDate();

        if (binStock.getLTrnNo() != null) {

            narration = "By " + ntype + "RECEIPT from " + binStock.getBinId() + "(Bin No :" + binStock.getBinName()
                    + " (production inward Dt :" + new DateUtil().getDateTOStringWk(currentDate) + "))";

            Optional<BinstockAuditLog> binstockAuditLog = binstockAuditLogRepository.findBy(binStock.getRefId(), binStock.getBatchNo(), binStock.getBranch(), entryType);

            if (!binstockAuditLog.isPresent()) {
                BinstockAuditLog binStockLog = new BinstockAuditLog();
                binStockLog.setItem_id(binStock.getRefId());
                binStockLog.setQty(binStock.getStkQty());
                binStockLog.setNaration(narration);
                binStockLog.setLot(binStock.getLot());
                binStockLog.setBatch_id(binStock.getBatchId());
                binStockLog.setBatchno(binStock.getBatchNo());
                binStockLog.setEntry_type(entryType);
                binStockLog.setTrn_type("R");
                binStockLog.setTrnno(binStock.getLTrnNo());
                binStockLog.setAccouting_date(dayEndUtils.getAccountingDate(binStock.getBranch(), currentDate));
                binStockLog.setUpdate_on(currentDate);
                binStockLog.setBranch_id(binStock.getBranch());
                binStockLog.setFirm_id(binStock.getCid());
                Double openingStock = binStockRepository.getCurrentOpeningSock(binStock.getBranch(), binStock.getRefId());
                binStockLog.setBalance(openingStock);
                binStockLog.setUpdate_on(currentDate);
                binstockAuditLogRepository.save(binStockLog);
            } else {
                System.out.println("Already update Audit Log");
            }

        } else {
            System.out.println("TRN No Missing");
        }

    }


    public void updateSplitBinStockAuditLog(BinStock binStock, User loggedUser) {

        System.out.println(" Split  == Audit log");
        System.out.println(" Split  == " + binStock.getRefId());
        System.out.println(" Split  == ");
        String entryType = "SPLIT";
        String narration = null;
        Date currentDate = dateUtil.getISTDate();

        if (binStock.getLTrnNo() != null) {

            narration = "By Split RECEIPT from " + binStock.getBinId() + "(Bin No :" + binStock.getBinName()
                    + " (production inward Dt :" + new DateUtil().getDateTOStringWk(currentDate) + "))";

            Optional<BinstockAuditLog> binstockAuditLog = binstockAuditLogRepository.findBy(binStock.getRefId(), binStock.getBatchNo(), binStock.getBranch(), entryType);

            if (!binstockAuditLog.isPresent()) {
                BinstockAuditLog binStockLog = new BinstockAuditLog();
                binStockLog.setItem_id(binStock.getRefId());
                binStockLog.setQty(binStock.getStkQty());
                binStockLog.setNaration(narration);
                binStockLog.setLot(binStock.getLot());
                binStockLog.setBatch_id(binStock.getBatchId());
                binStockLog.setBatchno(binStock.getBatchNo());
                binStockLog.setEntry_type(entryType);
                binStockLog.setTrn_type("R");
                binStockLog.setTrnno(binStock.getLTrnNo());
                binStockLog.setAccouting_date(dayEndUtils.getAccountingDate(binStock.getBranch(), currentDate));
                binStockLog.setUpdate_on(currentDate);
                binStockLog.setBranch_id(binStock.getBranch());
                binStockLog.setFirm_id(binStock.getCid());
                Double openingStock = binStockRepository.getCurrentOpeningSock(binStock.getBranch(), binStock.getRefId());
                binStockLog.setBalance(openingStock);
                binStockLog.setUpdate_on(currentDate);
                binStockLog.setBinNo(binStock.getBinName());
                binStockLog.setGodownId(binStock.getGid());
                binstockAuditLogRepository.save(binStockLog);
            } else {
                System.out.println("Already update Audit Log");
            }

        } else {
            System.out.println("TRN No Missing");
        }

    }


    public void updateSplitBinStockAuditLog(BinStock binStock, SplitProductDetails productDetails, SplitTransaction splitTransaction, User loggedUser) {

        System.out.println(" Split  == Audit log");
        System.out.println(" Split  == " + binStock.getRefId());
        System.out.println(" Split  == ");
        String entryType = "SPLIT";
        String narration = null;
        Date currentDate = dateUtil.getISTDate();

        if (binStock.getLTrnNo() != null) {

            narration = "By Split Issue from " + binStock.getBinId() + "(Bin No :" + binStock.getBinName()
                    + " (Issue Dt :" + new DateUtil().getDateTOStringWk(currentDate) + "))";

            BinstockAuditLog binStockLog = new BinstockAuditLog();
            binStockLog.setItem_id(binStock.getRefId());
            binStockLog.setQty(productDetails.getSelected_qty());
            binStockLog.setNaration(narration);
            binStockLog.setLot(binStock.getLot());
            binStockLog.setBatch_id(binStock.getBatchId());
            binStockLog.setBatchno(binStock.getBatchNo());
            binStockLog.setEntry_type(entryType);
            binStockLog.setTrn_type("I");
            binStockLog.setTrnno(splitTransaction.getTrnno());
            binStockLog.setAccouting_date(dayEndUtils.getAccountingDate(splitTransaction.getBid(), currentDate));
            binStockLog.setUpdate_on(currentDate);
            binStockLog.setBranch_id(splitTransaction.getBid());
            binStockLog.setFirm_id(binStock.getCid());
            Double openingStock = binStockRepository.getCurrentOpeningSock(splitTransaction.getBid(), binStock.getRefId());
            binStockLog.setBalance(openingStock);
            binStockLog.setUpdate_on(currentDate);
            binStockLog.setBinNo(binStock.getBinName());
            binStockLog.setGodownId(binStock.getGid());
            binstockAuditLogRepository.save(binStockLog);

        } else {
            System.out.println("TRN No Missing");
        }

    }


    public void revertProduction(ProductionEntryFormDto entryFormDto) {

        List<BinStockLog> byeList = binStockLogRepository.getLogList(entryFormDto.getJournalId(), null, "R", entryFormDto.getTrnNo());
        if (!byeList.isEmpty()) {
            for (BinStockLog binStockLog : byeList) {
                List<BinStock> binStockList = binStockRepository.revertProductionReceiveBatch(binStockLog.getRefid(), binStockLog.getBatchid(), binStockLog.getBatchno(), binStockLog.getTrnno());
                if (!binStockList.isEmpty()) {
                    binStockRepository.deleteAll(binStockList);
                }
            }
        }

        List<BinStockLog> bomList = binStockLogRepository.getLogList(entryFormDto.getJournalId(), null, "I", entryFormDto.getTrnNo());

        if (!bomList.isEmpty()) {
            for (BinStockLog binStockLog : bomList) {
                List<BinStock> binStockList = binStockRepository.revertProductionIssueBatch(binStockLog.getRefid(), binStockLog.getBatchid(), binStockLog.getBatchno(), binStockLog.getLot(), binStockLog.getGcode(), binStockLog.getGname());
                if (!binStockList.isEmpty()) {

                    BinStock binStock = binStockList.get(0);
                    Double avgCost = 0.0;
                    Double cost = 0.0;
                    Double qty = 0.0;
                    if (binStockLog.getAvgcost() != null && binStockLog.getAvgcost() > 0) {
                        System.out.println("stbin avg cost = " + binStockLog.getAvgcost());
                        System.out.println("binstock avg cost = " + binStock.getAvgCost());
                        avgCost = (binStock.getAvgCost() + binStockLog.getAvgcost()) / 2;
                    }

                    System.out.println(" avgCost = " + avgCost);
                    if (binStock.getStkQty() > 0 && binStock.getCost() > 0) {
                        cost = binStock.getCost() + binStockLog.getQty();
                        qty = binStock.getStkQty() + binStockLog.getQty();
                    } else {
                        if (binStock.getStkQty() > 0) {
                            qty = binStock.getStkQty() + binStockLog.getQty();
                        } else {
                            qty = binStockLog.getQty();
                        }

                        if (binStock.getCost() > 0) {
                            cost = binStock.getCost() + binStockLog.getQty();
                        } else {
                            cost = avgCost * binStockLog.getQty();
                        }

                    }
                    binStock.setAvgCost(avgCost);
                    binStock.setCost(cost);
                    binStock.setStkQty(Double.valueOf(decfor.format(qty)));

                    Optional<BinLock> binLockOptional = binLockRepository.findBy(binStock.getBinName().toString());
                    if (binLockOptional.isPresent()) {
                        binStock.setLockStatus("Y");
                        binStock.setLockType(binLockOptional.get().getLockType());
                    }

                    binStockRepository.save(binStock);

                }
            }
        }

        List<BinstockAuditLog> binstockAuditLogList = binstockAuditLogRepository.findAllByTrnNo(entryFormDto.getTrnNo());
        if (!binstockAuditLogList.isEmpty()) {
            binstockAuditLogRepository.deleteAll(binstockAuditLogList);
        }

        List<BinStockLog> binStockLogList = binStockLogRepository.getProductionRevertList(entryFormDto.getTrnNo());
        if (!binStockLogList.isEmpty()) {
            binStockLogRepository.deleteAll(binStockLogList);
        }

    }


    public void updateBinStockLogExternal(BinStock binStock, User loggedUser, String type) {
        Date currentDate = dateUtil.getISTDate();
        System.out.println(" stbin updated");
        BinStockLog binStockLog = new BinStockLog(binStock);
        binStockLog.setAby(loggedUser.getId());
        binStockLog.setTrntype(type);
        binStockLog.setEntryType("EXTERNAL_GODOWN");
        binStockLog.setUpdatedon(currentDate);
        binStockLog.setTrnno(binStock.getLTrnNo());
        binStockLog.setAdate(currentDate);
        binStockLog.setTrnDate(currentDate);
        binStockLog.setAccounting_date(dayEndUtils.getAccountingDate(binStock.getBranch(), currentDate));
        binStockLogRepository.save(binStockLog);
    }
    public void updateBinLogInprocessQcTransaction(BinStock binStock, User loggedUser, Double quantity) {
		
		  Date currentDate = dateUtil.getISTDate();
	        System.out.println(" stbin updated");
	        BinStockLog binStockLog = new BinStockLog(binStock);
	        binStockLog.setAby(loggedUser.getId());
	        binStockLog.setRefid(binStock.getRefId());
	        binStockLog.setGcode(binStock.getBinId());
	        binStockLog.setGname(binStock.getBinName());
	        binStockLog.setGid(binStock.getGid());
	        binStockLog.setCid(loggedUser.getOrg_id());
	        binStockLog.setDept(loggedUser.getDepartment().getId());
	        binStockLog.setQty(quantity);
	        binStockLog.setTrntype("I");
	        binStockLog.setEntryType("QC_INPROCESS");
	        binStockLog.setUpdatedon(currentDate);
	        binStockLog.setTrnno(binStock.getLTrnNo());
	        binStockLog.setBatchid(binStock.getBatchId());
	        binStockLog.setBatchno(binStock.getBatchNo());
	        binStockLog.setLot(binStock.getLot());
	        if(binStock.getAvgCost()!=null) {
	        binStockLog.setCost(binStock.getAvgCost() *quantity );
	        }
	        binStockLog.setAvgcost(binStock.getAvgCost());
	        binStockLog.setBranch(binStock.getBranch());
	        binStockLog.setFmid(loggedUser.getBranch_id());
	        binStockLog.setFmtable("branch");
	        binStockLog.setToid(loggedUser.getBranch_id());
	        binStockLog.setTotable("branch");
	        binStockLog.setAdate(currentDate);
	        binStockLog.setTrnDate(currentDate);
	        binStockLog.setAccounting_date(dayEndUtils.getAccountingDate(binStock.getBranch(), currentDate));
	        binStockLogRepository.save(binStockLog);
	}
    public void updateBinLogInprocessQcTransactionForReport(BinStock binStock, User loggedUser, Double consumed) {
		
		 Date currentDate = dateUtil.getISTDate();
	        System.out.println(" stbin updated");
	        BinStockLog binStockLog = new BinStockLog(binStock);
	        binStockLog.setAby(loggedUser.getId());
	        binStockLog.setRefid(binStock.getRefId());
	        binStockLog.setGcode(binStock.getBinId());
	        binStockLog.setGname(binStock.getBinName());
	        binStockLog.setGid(binStock.getGid());
	        binStockLog.setCid(loggedUser.getOrg_id());
	        binStockLog.setDept(loggedUser.getDepartment().getId());
	        binStockLog.setQty(consumed);
	        binStockLog.setTrntype("I");
	        binStockLog.setEntryType("QC_INPROCESS");
	        binStockLog.setUpdatedon(currentDate);
	        binStockLog.setTrnno(binStock.getLTrnNo());
	        binStockLog.setBatchid(binStock.getBatchId());
	        binStockLog.setBatchno(binStock.getBatchNo());
	        binStockLog.setLot(binStock.getLot());
	        if(binStock.getAvgCost() !=null) {
	        binStockLog.setCost(binStock.getAvgCost() *consumed);
	        }
	        binStockLog.setAvgcost(binStock.getAvgCost());
	        binStockLog.setBranch(binStock.getBranch());
	        binStockLog.setFmid(loggedUser.getBranch_id());
	        binStockLog.setFmtable("branch");
	        binStockLog.setToid(loggedUser.getBranch_id());
	        binStockLog.setTotable("branch");
	        binStockLog.setAdate(currentDate);
	        binStockLog.setTrnDate(currentDate);
	        binStockLog.setAccounting_date(dayEndUtils.getAccountingDate(binStock.getBranch(), currentDate));
	        binStockLogRepository.save(binStockLog);
	}



    public void updateBinLogAsrsMovement(BinStock binStock, User loggedUser, String trnType, Double quantity) {
        Date currentDate = dateUtil.getISTDate();
        System.out.println(" stbin updated");
        BinStockLog binStockLog = new BinStockLog(binStock);
        binStockLog.setAby(loggedUser.getId());
        binStockLog.setRefid(binStock.getRefId());
        binStockLog.setGcode(binStock.getBinId());
        binStockLog.setGname(binStock.getBinName());
        binStockLog.setGid(binStock.getGid());
        binStockLog.setCid(binStock.getCid());
        binStockLog.setDept(binStock.getDept());
        binStockLog.setQty(quantity);
        binStockLog.setTrntype(trnType);
        binStockLog.setEntryType("ASRS_MOVEMENT");
        binStockLog.setUpdatedon(currentDate);
        binStockLog.setTrnno(binStock.getLTrnNo());
        binStockLog.setBatchid(binStock.getBatchId());
        binStockLog.setBatchno(binStock.getBatchNo());
        binStockLog.setLot(binStock.getLot());
        binStockLog.setCost(binStock.getCost());
        binStockLog.setAvgcost(binStock.getAvgCost());
        binStockLog.setBranch(binStock.getBranch());
        binStockLog.setAdate(currentDate);
        binStockLog.setTrnDate(currentDate);
        binStockLog.setAccounting_date(dayEndUtils.getAccountingDate(binStock.getBranch(), currentDate));
        binStockLogRepository.save(binStockLog);


    }
    
    public void updateBinLogForBillCreation(BinStock binStock, User loggedUser, Double quantity) {
		
		  Date currentDate = dateUtil.getISTDate();
	        BinStockLog binStockLog = new BinStockLog(binStock);
	        binStockLog.setAby(loggedUser.getId());
	        binStockLog.setRefid(binStock.getRefId());
	        binStockLog.setGcode(binStock.getBinId());
	        binStockLog.setGname(binStock.getBinName());
	        binStockLog.setGid(binStock.getGid());
	        binStockLog.setCid(loggedUser.getOrg_id());
	        binStockLog.setDept(loggedUser.getDepartment().getId());
	        binStockLog.setQty(quantity);
	        binStockLog.setTrntype("I");
	        binStockLog.setEntryType("Deduction");
	        binStockLog.setUpdatedon(currentDate);
	        binStockLog.setTrnno(binStock.getLTrnNo());
	        binStockLog.setBatchid(binStock.getBatchId());
	        binStockLog.setBatchno(binStock.getBatchNo());
	        binStockLog.setLot(binStock.getLot());
	        if(binStock.getAvgCost()!=null) {
	        binStockLog.setCost(binStock.getAvgCost() *quantity );
	        }
	        binStockLog.setAvgcost(binStock.getAvgCost());
	        binStockLog.setBranch(binStock.getBranch());
	        binStockLog.setFmid(loggedUser.getBranch_id());
	        binStockLog.setFmtable("branch");
	        binStockLog.setToid(loggedUser.getBranch_id());
	        binStockLog.setTotable("branch");
	        binStockLog.setAdate(currentDate);
	        binStockLog.setTrnDate(currentDate);
	        binStockLog.setAccounting_date(dayEndUtils.getAccountingDate(binStock.getBranch(), currentDate));
	        binStockLogRepository.save(binStockLog);
	}

}
