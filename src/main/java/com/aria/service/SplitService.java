package com.aria.service;

import com.aria.dto.SplitBinLotWiseStock;
import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.BinStockUtil;
import com.aria.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.stream.Collectors;

@Service
public class SplitService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SplitService.class);

    @Autowired
    SplitMasterRepository splitMasterRepository;

    @Autowired
    SplitNameRepository splitNameRepository;

    @Autowired
    SplitTransactionRepository splitTransactionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    public JournalEntryRepository journalEntryRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    TRNIndexRepo trnIndexRepo;

    @Autowired
    SplitProductDetailsRepository splitProductDetailsRepository;

    @Autowired
    BinStockUtil binStockUtil;

    @Autowired
    BatchMasterRepository batchMasterRepository;

    @Autowired
    JournalProductMappingRepository journalProductMappingRepository;

    @Autowired
    ProductMasterRepository productMasterRepository;

    @Autowired
    BinStockRepository binStockRepository;

    @Autowired
    JournalRepository journalRepository;

    @Autowired
    DepartmentDetailsRepository departmentDetailsRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    ASRSPalletDetailsRepository asrsPalletDetailsRepository;


    public ResponseEntity<?> getSplitJournalBatch(String batchNo, Long binNo, Double lot)
            throws UsernameNotFoundException {
        List<SplitBinLotWiseStock> list = journalEntryRepository.getSplitJournalBatch(batchNo, binNo, lot);
        List<SplitBinLotWiseStockDTO> result = list.stream().map(data -> {
            SplitBinLotWiseStockDTO stockLedgerDto = new SplitBinLotWiseStockDTO(data);
            return stockLedgerDto;
        }).collect(Collectors.toList());
        return new ResponseEntity<List<SplitBinLotWiseStockDTO>>(result, HttpStatus.OK);
    }

    public ResponseEntity<?> getSplitJournalBatchProductList(Long binNo, String batchNo, String splitConfigbased) throws UsernameNotFoundException {

        System.out.println("batch No == " + batchNo);

        System.out.println("binNo No == " + binNo);
        System.out.println("splitConfigBased == " + splitConfigbased);





        /*Optional<JournalEntry> optional = journalEntryRepository.getSplitJournal(batchNo);

        if (optional.isPresent()) {

            System.out.println(" ####  "+binNo);

            List<JournalProductMapping> list = journalProductMappingRepository.findByJournalProductMappingList(optional.get().getSjid());
            if (!list.isEmpty()) {
                List<JournalProductMappingDto> result = list.stream().map(data -> {
                    JournalProductMappingDto stockLedgerDto = new JournalProductMappingDto(data);
                    return stockLedgerDto;
                }).collect(Collectors.toList());
                return new ResponseEntity<List<JournalProductMappingDto>>(result, HttpStatus.OK);
            } else {
                return new ResponseEntity<List<SplitBinLotWiseStockDTO>>(new ArrayList<>(), HttpStatus.OK);
            }

        } else {
*/
        Optional<BinStock> optionalBinStock = binStockRepository.findBy(binNo, batchNo);

        if (optionalBinStock.isPresent()) {

            Optional<Journal> journal = null;
            if (splitConfigbased.equalsIgnoreCase("BOM")) {
                journal = journalRepository.findByeProduct(optionalBinStock.get().getRefId());
            } else {
                journal = journalRepository.findBomProduct(optionalBinStock.get().getRefId());
            }
            if (journal.isPresent()) {
                List<JournalProductMapping> list = journalProductMappingRepository.findByJournalProductMappingList(journal.get().getId());
                if (!list.isEmpty()) {
                    List<JournalProductMappingDto> result = list.stream().map(data -> {
                        JournalProductMappingDto stockLedgerDto = new JournalProductMappingDto(data);
                        return stockLedgerDto;
                    }).collect(Collectors.toList());
                    return new ResponseEntity<List<JournalProductMappingDto>>(result, HttpStatus.OK);
                } else {
                    return new ResponseEntity<List<SplitBinLotWiseStockDTO>>(new ArrayList<>(), HttpStatus.OK);
                }
            } else {
                return new ResponseEntity<List<SplitBinLotWiseStockDTO>>(new ArrayList<>(), HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<List<SplitBinLotWiseStockDTO>>(new ArrayList<>(), HttpStatus.OK);
        }

        //}
    }


    public ResponseEntity<?> saveSplit(SplitDto splitDto, User loggedUser) {

        SplitTransaction splitTransaction = new SplitTransaction(splitDto);
        splitTransaction.setCby(loggedUser.getId());
        splitTransaction.setTrnno(getTrnNo());
        splitTransaction.setStatus("Pending");
        splitTransaction.setTrndate(dateUtil.getISTDate());
        SplitTransaction splitTrn = splitTransactionRepository.save(splitTransaction);

        String batchNo = null;

        if (!splitDto.getSplitProducts().isEmpty()) {

            for (SplitProductDto splitProduct : splitDto.getSplitProducts()) {

                List<String> considerUs = new ArrayList<>(Arrays.asList(splitProduct.getConsiderUs().split("\\,")));

                for (String consider : considerUs) {

                    consider = consider.trim();

                    SplitProductDetails splitProductDetails = new SplitProductDetails(splitProduct, splitTrn);
                    splitProductDetails.setSplit_type(consider);

                    if (consider.equalsIgnoreCase("Scrap")) {

                        splitProductDetails.setBinno(splitProduct.getScrapBin());
                        splitProductDetails.setQuantity(splitProduct.getScrapQty());

                    } else if (consider.equalsIgnoreCase("Stock")) {

                        splitProductDetails.setBinno(splitProduct.getStockBin());
                        splitProductDetails.setQuantity(splitProduct.getStockQty());

                    }

                    BatchMaster batchMaster = checkBatchMaster(splitTrn, splitDto, splitProductDetails, loggedUser);
                    if (batchMaster != null) {
                        splitProductDetails.setBatch_no(batchMaster.getBatchno());
                        splitProductDetails.setBatch_id(batchMaster.getId());
                    }

                    SplitProductDetails result = splitProductDetailsRepository.save(splitProductDetails);

                }
                /*if (splitProduct.getPalletNumber() != null && !splitProduct.getPalletNumber().isEmpty()) {
                    ASRSPalletDetails asrsPalletDetails = asrsPalletDetailsRepository.findByAsrsProductionIdAndPalletNumber(splitProduct.getBsk_batch_no(), splitDto.getBinname(), splitProduct.getPalletNumber());
                    if (asrsPalletDetails != null && asrsPalletDetails.getPalletType().equalsIgnoreCase("Packing")) {
                        asrsPalletDetails.setDeduction(String.valueOf(splitDto.getQty()));
                        asrsPalletDetailsRepository.save(asrsPalletDetails);
                    } else {
                        logger.info("No ASRSPalletDetails found for trnNo: {}, binName: {}, palletNumber: {}", splitProduct.getBsk_batch_no(), splitDto.getBinname(), splitProduct.getPalletNumber());
                    }
                }*/

            }
        }
        return new ResponseEntity<Status>(new Status("200", "Split created successfully"), HttpStatus.OK);
    }


    private String getTrnNo() {
        String trnNo = null;
        Optional<TRNIndex> trnIndex = trnIndexRepo.findByPrefix("Split");
        if (trnIndex.isPresent()) {
            TRNIndex trn = trnIndex.get();
            Long trnlast = trn.getLastno();
            trnNo = trn.getPrefix() + "-" + trn.getLastno();
            Long fstrnlast = trnlast + 1;
            trn.setLastno(fstrnlast);
            TRNIndex rs = trnIndexRepo.saveAndFlush(trn);
            return trnNo;
        } else {
            return trnNo;
        }
    }


    public BatchMaster checkBatchMaster(SplitTransaction salesReturn, SplitDto salesReturnDto, SplitProductDetails salesReturnProducts, User loggedUser) {


        String productId = null;


        productId = salesReturnProducts.getProduct_id().toString();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(dateUtil.getISTDate());
        Long time = calendar.getTimeInMillis();

        String batchNo = null;
        if (salesReturnProducts.getBatch_no() != null) {
            batchNo = salesReturnProducts.getBatch_no();
        } else {
            batchNo = "Split#" + salesReturnProducts.getBsk_batch_id() + "#" + productId + "#" + "Split#" + loggedUser.getId() + "#" + time;
        }

        Optional<BatchMaster> optionalBatchMaster = batchMasterRepository.findByBatchno(batchNo);
        if (!optionalBatchMaster.isPresent()) {
            BatchMaster batchMaster = new BatchMaster();
            batchMaster.setBatchno(batchNo);
            batchMaster.setDept(loggedUser.getDepartment().getId());
            batchMaster.setBranch(loggedUser.getBranch_id());
            batchMaster.setProductId(Long.valueOf(productId));
            batchMaster.setCreatedby(loggedUser.getId());
            batchMaster.setBtype("Split");
            BatchMaster result = batchMasterRepository.save(batchMaster);
            return result;
        } else {
            return optionalBatchMaster.get();
        }
    }


    public ResponseEntity<?> splitList(String productName, String dateFrom, String dateTo, Long branch, Long dept , Pageable pageable) throws UsernameNotFoundException {

        Date startDate = null;
        Date endDate = null;

        if (dateFrom != null) {
            startDate = dateUtil.getStringTODateWk(dateFrom);
        }

        if (dateTo != null) {
            endDate = dateUtil.getStringTODateWk(dateTo);
        }

        Page<SplitList> splitLists = splitTransactionRepository.getAllSplitList(productName, startDate, endDate, branch, dept,  pageable);

        List<SplitListDto> list = splitLists.getContent().stream().map(user -> {
            SplitListDto splitListDto = new SplitListDto(user);
            return splitListDto;
        }).collect(Collectors.toList());
        PageDataDto<SplitListDto> pageData = new PageDataDto<>(list, splitLists);
        return new ResponseEntity<PageDataDto<SplitListDto>>(pageData, HttpStatus.OK);

    }


    public ResponseEntity<?> getSplitDetails(Long id, String trnNo, Long branch, Long dept) throws UsernameNotFoundException {


        Optional<SplitTransaction> splitTransaction = splitTransactionRepository.viewSplitTransaction(id, branch, dept, trnNo);

        if (splitTransaction.isPresent()) {
            SplitTransactionDto result = new SplitTransactionDto(splitTransaction.get());

            Optional<ProductMaster> productMaster = productMasterRepository.findById(result.getRefid());
            if (productMaster.isPresent()) {
                result.setProductName(productMaster.get().getProductName());
            }

            Optional<DepartmentDetails> departmentDetails = departmentDetailsRepository.findById(result.getDept());
            if (departmentDetails.isPresent()) {
                result.setDeptName(departmentDetails.get().getName());
            }

            Optional<Branch> branch1 = branchRepository.findById(result.getBranch());
            if (branch1.isPresent()) {
                result.setBranchName(branch1.get().getBranchName());
            }

            Optional<User> optionalUser = userRepository.findById(result.getCreatedBy());
            if (optionalUser.isPresent()) {
                result.setCreatedByName(optionalUser.get().getEmployeename());
            }

            List<SplitProductDetails> list = splitProductDetailsRepository.viewSplitProductDetails(result.getId());
            result.setProductDetailsDTOList(list.stream().map(user -> {
                SplitProductDetailsDTO splitListDto = new SplitProductDetailsDTO(user);
                return splitListDto;
            }).collect(Collectors.toList()));

            return new ResponseEntity<SplitTransactionDto>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }


    public ResponseEntity<?> approveSplit(Long id, String trnNo, String type, User loggedUser) throws UsernameNotFoundException {

        Map<String,Object> response = new HashMap<>();
        Optional<SplitTransaction> splitTransaction = splitTransactionRepository.getSplitTransaction(id, trnNo);

        if (splitTransaction.isPresent()) {

            SplitTransaction res = splitTransaction.get();
            //res.setStatus(type);
            //SplitTransaction result = splitTransactionRepository.save(res);

            if (type.equalsIgnoreCase("Verify")) {

                List<Map<String,Object>> mapList = new ArrayList<>();

                List<SplitProductBatchList> checkList = splitProductDetailsRepository.findBy(res.getId());
                boolean status = true;

                if (checkList.size() > 0) {

                    for (SplitProductBatchList spd : checkList) {

                        logger.info("Batch No in approveSplit: {}", spd.getBatchNo(), spd.getPalletNo());
                        ASRSPalletDetails asrsPalletDetails = asrsPalletDetailsRepository.findByAsrsProductionIdAndPalletNumber(spd.getBatchNo(), res.getBinname(), spd.getPalletNo());
                        if (asrsPalletDetails != null && asrsPalletDetails.getPalletType().equalsIgnoreCase("Packing")) {
                            logger.info("Pallet Number in approveSplit: {}", spd.getPalletNo());
                        } else {
                            Map<String,Object> data = new HashMap<>();
                            data.put("batchNo",spd.getBatchNo());
                            data.put("palletNo",spd.getPalletNo());
                            mapList.add(data);
                            logger.info("No ASRSPalletDetails found for trnNo: {}, binName: {}, palletNumber: {}", spd.getBatchNo(), res.getBinname(), spd.getPalletNo());
                            status = false;
                        }

                    }
                }


                if (status) {

                    logger.info("No SplitProductDetails found for SplitTransaction ID: {}", res.getId());

                    List<SplitProductDetails> list = splitProductDetailsRepository.viewSplitProductDetails(res.getId());

                    if (!list.isEmpty()) {

                        SplitProductDetails reduceProductDetails = list.get(0);

                        binStockUtil.updateSplitTrnReduceStock(reduceProductDetails, res, loggedUser);

                        for (SplitProductDetails splitProductDetails : list) {
                            BinStock binStock = binStockUtil.updateSplitTrnStock(splitProductDetails, res, loggedUser);
                            if (binStock != null && binStock.getId() != null) {
                                splitProductDetails.setBinstock_id(binStock.getId());
                                splitProductDetailsRepository.save(splitProductDetails);
                            }
                            logger.info("Batch No in approveSplit: {}", splitProductDetails.getBatch_no());

                        }

                        List<SplitProductBatchList> detuctionList = splitProductDetailsRepository.findBy(res.getId());

                        if(detuctionList != null && !detuctionList.isEmpty()) {
                            for (SplitProductBatchList detuction : detuctionList) {
                                logger.info("Pallet Number in approveSplit: {}", detuction.getPalletNo());
                                if (detuction.getPalletNo() != null && !detuction.getPalletNo().isEmpty()) {
                                    ASRSPalletDetails asrsPalletDetails = asrsPalletDetailsRepository.findByAsrsProductionIdAndPalletNumber(detuction.getBatchNo(), res.getBinname(), detuction.getPalletNo());
                                    if (asrsPalletDetails != null && asrsPalletDetails.getPalletType().equalsIgnoreCase("Packing")) {
                                        asrsPalletDetails.setDeduction(String.valueOf(detuction.getQty()));
                                        asrsPalletDetailsRepository.save(asrsPalletDetails);
                                    } else {
                                        logger.info("No ASRSPalletDetails found for trnNo: {}, binName: {}, palletNumber: {}", detuction.getBatchNo(), res.getBinname(), detuction.getPalletNo());
                                    }
                                }
                            }
                        }

                    } else {
                        return new ResponseEntity<Status>(new Status(false, "400", "SplitProductDetails List not found"), HttpStatus.OK);
                    }

                } else {
                    response.put("data", mapList);
                    response.put("message", "Some Pallet Numbers not found in ASRSPalletDetails");
                    response.put("status", "400");
                    response.put("success", false);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }


            } else if (type.equalsIgnoreCase("Reject")) {

                deleteAllProductDetails(res.getId());
            }
            res.setStatus(type);
            SplitTransaction result = splitTransactionRepository.save(res);
            return new ResponseEntity<Status>(new Status(true,"200", "Split updated successfully"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }


    public void deleteAllProductDetails(Long id) {

        List<SplitProductDetails> list = splitProductDetailsRepository.viewSplitProductDetails(id);

        if (!list.isEmpty()) {
            splitProductDetailsRepository.deleteAll(list);
        }

    }

    public ResponseEntity<?> getSplitDetails(Long id, String trnNo) throws UsernameNotFoundException {


        Optional<SplitTransaction> splitTransaction = splitTransactionRepository.getSplitTransaction(id, trnNo);

        if (splitTransaction.isPresent()) {
            SplitTransactionDto result = new SplitTransactionDto(splitTransaction.get());
            Optional<ProductMaster> productMaster = productMasterRepository.findById(result.getRefid());
            result.setProductName(productMaster.get().getProductName());
            List<SplitProductDetails> list = splitProductDetailsRepository.viewSplitProductDetails(result.getId());
            result.setProductDetailsDTOList(list.stream().map(user -> {
                SplitProductDetailsDTO splitListDto = new SplitProductDetailsDTO(user);
                return splitListDto;
            }).collect(Collectors.toList()));

            return new ResponseEntity<SplitTransactionDto>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity<?> updateSplit(SplitDto splitDto, User loggedUser) {
        logger.info("Entering SplitService.updateSplit with SplitDto: {}", splitDto);
        try {
            SplitTransaction splitTransaction = new SplitTransaction(splitDto, true);
            splitTransaction.setCby(loggedUser.getId());
            splitTransaction.setStatus("Pending");
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(dateUtil.getISTDate());
            splitTransaction.setCon(String.valueOf(calendar.getTimeInMillis()));
            SplitTransaction splitTrn = splitTransactionRepository.save(splitTransaction);

            if (!splitDto.getSplitProducts().isEmpty()) {

                for (SplitProductDto splitProduct : splitDto.getSplitProducts()) {

                    List<String> considerUs = new ArrayList<>(Arrays.asList(splitProduct.getConsiderUs().split("\\,")));

                    for (String consider : considerUs) {

                        consider = consider.trim();

                        SplitProductDetails splitProductDetails = new SplitProductDetails(splitProduct, splitTrn);
                        splitProductDetails.setSplit_type(consider);

                        if (consider.equalsIgnoreCase("Scrap")) {
                            splitProductDetails.setBinno(splitProduct.getScrapBin());
                            splitProductDetails.setQuantity(splitProduct.getScrapQty());
                        } else if (consider.equalsIgnoreCase("Stock")) {
                            splitProductDetails.setBinno(splitProduct.getStockBin());
                            splitProductDetails.setQuantity(splitProduct.getStockQty());
                        }

                        BatchMaster batchMaster = checkBatchMaster(splitTrn, splitDto, splitProductDetails, loggedUser);
                        if (batchMaster != null) {
                            splitProductDetails.setBatch_no(batchMaster.getBatchno());
                            splitProductDetails.setBatch_id(batchMaster.getId());
                        }
                        SplitProductDetails result = splitProductDetailsRepository.save(splitProductDetails);


                    }

                    if (splitProduct.getPalletNumber() != null && !splitProduct.getPalletNumber().isEmpty()) {
                        ASRSPalletDetails asrsPalletDetails = asrsPalletDetailsRepository.findByAsrsProductionIdAndPalletNumber(splitProduct.getBsk_batch_no(), splitDto.getBinname(), splitProduct.getPalletNumber());
                        if (asrsPalletDetails != null && asrsPalletDetails.getPalletType().equalsIgnoreCase("Packing")) {
                            asrsPalletDetails.setDeduction(String.valueOf(splitDto.getQty()));
                            asrsPalletDetailsRepository.save(asrsPalletDetails);
                        } else {
                            logger.info("No ASRSPalletDetails found for trnNo: {}, binName: {}, palletNumber: {}", splitProduct.getBsk_batch_no(), splitDto.getBinname(), splitProduct.getPalletNumber());
                        }
                    }
                }
            }


            return new ResponseEntity<Status>(new Status("200", "Split created successfully"), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error in SplitService.updateSplit: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
