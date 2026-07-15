package com.aria.service;

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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SalesReturnService {


    @Autowired
    SalesReturnRepository salesReturnRepository;

    @Autowired
    SalesReturnProductsRepository salesReturnProductsRepository;

    @Autowired
    TRNIndexRepo trnIndexRepo;

    @Autowired
    SalesReturnBinDetailsRepository salesReturnBinDetailsRepository;

    @Autowired
    DateUtil dateUtil;


    @Autowired
    BinStockUtil binStockUtil;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    BatchMasterRepository batchMasterRepository;

    @Autowired
    UserRepository userRepository;


    // SalesReturn
    public ResponseEntity<?> saveSalesReturn(SalesReturnDto salesReturnDto, User loggedUser) {

        SalesReturn salesReturn = new SalesReturn(salesReturnDto);
        salesReturn.setTrnNo(getTrnNo());
        salesReturn.setStatus("Pending");
        salesReturn.setCreatedBy(loggedUser.getId());
        salesReturn.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        salesReturn.setTrnDate(dateUtil.getISTDate());
        Organisation organisation = new Organisation();
        organisation.setId(loggedUser.getOrg_id());
        salesReturn.setFirmOrg(organisation);

        SalesReturn result = salesReturnRepository.save(salesReturn);
        if (!salesReturnDto.getSalesReturnProducts().isEmpty()) {
            for (SalesReturnProductsDto salesReturnProductsDto : salesReturnDto.getSalesReturnProducts()) {
                SalesReturnProducts salesReturnProducts = new SalesReturnProducts(salesReturnProductsDto);
                salesReturnProducts.setReturnId(result.getId());
                if (result.getReturn_type().equalsIgnoreCase("selfTransfer")) {
                    BatchMaster batchMaster = checkBatchMasterSelfReturn(result, salesReturnDto, salesReturnProducts, loggedUser);
                    if (batchMaster != null) {
                        salesReturnProducts.setBatch_No(batchMaster.getBatchno());
                        salesReturnProducts.setBatch_Id(batchMaster.getId());
                    }
                } else if (result.getReturn_type().equalsIgnoreCase("saleReturn")) {
                    BatchMaster batchMaster = checkBatchMaster(result, salesReturnDto, salesReturnProducts, loggedUser);
                    if (batchMaster != null) {
                        salesReturnProducts.setBatch_No(batchMaster.getBatchno());
                        salesReturnProducts.setBatch_Id(batchMaster.getId());
                    }
                }
                SalesReturnProducts resultProduct = salesReturnProductsRepository.save(salesReturnProducts);
                if (!salesReturnProductsDto.getBinDetails().isEmpty()) {
                    for (SalesReturnBinDetailsDto salesReturnBinDetailsDto : salesReturnProductsDto.getBinDetails()) {
                        SalesReturnBinDetails salesReturnBinDetails = new SalesReturnBinDetails(salesReturnBinDetailsDto);
                        salesReturnBinDetails.setReturnProductId(resultProduct.getId());
                        salesReturnBinDetailsRepository.save(salesReturnBinDetails);
                    }
                }
            }
        }
        return new ResponseEntity<Status>(new Status("200", "SalesReturn saved successfully!!!"), HttpStatus.OK);
    }


    public ResponseEntity<?> updateSalesReturn(SalesReturnDto salesReturnDto, User loggedUser) {
        SalesReturn salesReturn = new SalesReturn(salesReturnDto);
        salesReturn.setUpdatedBy(loggedUser.getId());
        salesReturn.setStatus("Pending");
        salesReturn.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        SalesReturn result = salesReturnRepository.save(salesReturn);
        if (!salesReturnDto.getSalesReturnProducts().isEmpty()) {
            for (SalesReturnProductsDto salesReturnProductsDto : salesReturnDto.getSalesReturnProducts()) {
                SalesReturnProducts salesReturnProducts = new SalesReturnProducts(salesReturnProductsDto);
                salesReturnProducts.setReturnId(result.getId());

                if (result.getReturn_type().equalsIgnoreCase("selfTransfer")) {
                    BatchMaster batchMaster = checkBatchMasterSelfReturn(result, salesReturnDto, salesReturnProducts, loggedUser);
                    if (batchMaster != null) {
                        salesReturnProducts.setBatch_No(batchMaster.getBatchno());
                        salesReturnProducts.setBatch_Id(batchMaster.getId());
                    }

                } else if (result.getReturn_type().equalsIgnoreCase("saleReturn")) {
                    BatchMaster batchMaster = checkBatchMaster(result, salesReturnDto, salesReturnProducts, loggedUser);
                    if (batchMaster != null) {
                        salesReturnProducts.setBatch_No(batchMaster.getBatchno());
                        salesReturnProducts.setBatch_Id(batchMaster.getId());
                    }
                }

                SalesReturnProducts resultProduct = salesReturnProductsRepository.save(salesReturnProducts);
                if (!salesReturnProductsDto.getBinDetails().isEmpty()) {
                    for (SalesReturnBinDetailsDto salesReturnBinDetailsDto : salesReturnProductsDto.getBinDetails()) {
                        SalesReturnBinDetails salesReturnBinDetails = new SalesReturnBinDetails(salesReturnBinDetailsDto);
                        salesReturnBinDetails.setReturnProductId(resultProduct.getId());
                        salesReturnBinDetailsRepository.save(salesReturnBinDetails);
                    }
                }
            }
        }
        return new ResponseEntity<Status>(new Status("200", "SalesReturn updated  successfully!!!"), HttpStatus.OK);
    }


    public BatchMaster checkBatchMasterSelfReturn(SalesReturn salesReturn, SalesReturnDto salesReturnDto, SalesReturnProducts salesReturnProducts, User loggedUser) {

        String trnprefix = null;
        String branch = null;
        String productId = null;

        if (salesReturn.getFirmOrg().getTrnprefix() != null) {
            trnprefix = salesReturn.getFirmOrg().getTrnprefix();
        }


        productId = salesReturnProducts.getProductCode();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(dateUtil.getISTDate());
        Long time = calendar.getTimeInMillis();

        String batchNo = null;
        if (salesReturnProducts.getBatch_No() != null) {
            batchNo = salesReturnProducts.getBatch_No();
        } else {
            batchNo = "selfTransfer#" + productId + "#" + salesReturn.getReferenceNo() + "#" + time;
        }

        Optional<BatchMaster> optionalBatchMaster = batchMasterRepository.findByBatchno(batchNo);
        if (!optionalBatchMaster.isPresent()) {
            BatchMaster batchMaster = new BatchMaster();
            batchMaster.setBatchno(batchNo);
            batchMaster.setDept(loggedUser.getDepartment().getId());
            batchMaster.setBranch(loggedUser.getBranch_id());
            batchMaster.setProductId(Long.valueOf(productId));
            batchMaster.setCreatedby(loggedUser.getId());
            batchMaster.setBtype("SelfTransfer");
            BatchMaster result = batchMasterRepository.save(batchMaster);
            return result;
        } else {
            return optionalBatchMaster.get();
        }
    }

    public BatchMaster checkBatchMaster(SalesReturn salesReturn, SalesReturnDto salesReturnDto, SalesReturnProducts salesReturnProducts, User loggedUser) {
        String trnprefix = null;
        String branch = null;
        String productId = null;

        if (salesReturn.getFirmOrg().getTrnprefix() != null) {
            trnprefix = salesReturn.getFirmOrg().getTrnprefix();
        }

        if (salesReturn.getBranch().toString().equalsIgnoreCase("1")) {
            branch = "P1";
        } else if (salesReturn.getBranch().toString().equalsIgnoreCase("2")) {
            branch = "P2";
        } else if (salesReturn.getBranch().toString().equalsIgnoreCase("3")) {
            branch = "P3";
        } else if (salesReturn.getBranch().toString().equalsIgnoreCase("4")) {
            branch = "P4";
        }

        productId = salesReturnProducts.getProductCode();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(dateUtil.getISTDate());
        Long time = calendar.getTimeInMillis();

        String batchNo = null;
        if (salesReturnProducts.getBatch_No() != null) {
            batchNo = salesReturnProducts.getBatch_No();
        } else {
            batchNo = "Salreturn#" + trnprefix + "#" + branch + "#" + productId + "#" + "SJSUM#" + loggedUser.getId() + "#" + time;
        }

        Optional<BatchMaster> optionalBatchMaster = batchMasterRepository.findByBatchno(batchNo);
        if (!optionalBatchMaster.isPresent()) {
            BatchMaster batchMaster = new BatchMaster();
            batchMaster.setBatchno(batchNo);
            batchMaster.setDept(loggedUser.getDepartment().getId());
            batchMaster.setBranch(loggedUser.getBranch_id());
            batchMaster.setProductId(Long.valueOf(productId));
            batchMaster.setCreatedby(loggedUser.getId());
            batchMaster.setBtype("SalesReturn");
            BatchMaster result = batchMasterRepository.save(batchMaster);
            return result;
        } else {
            return optionalBatchMaster.get();
        }
    }

    public ResponseEntity<?> getSalesReturnList(String type, String refNo, String vendorName, String dateFrom, String dateTo, Pageable pageable) {

        Date startDate = null;
        Date endDate = null;
        if (dateFrom != null && dateTo != null) {
            startDate = dateUtil.getStringTODateWk(dateFrom);
            endDate = dateUtil.getStringTODateWk(dateTo);
        } else if (dateFrom != null) {
            startDate = dateUtil.getStringTODateWk(dateFrom);
        } else if (dateTo != null) {
            endDate = dateUtil.getStringTODateWk(dateTo);
        }

        Page<SalesReturn> list = salesReturnRepository.findBy(type, refNo, vendorName, startDate, endDate, pageable);
        if (!list.isEmpty()) {
            List<SalesReturnDto> result = list.stream().map(data -> new SalesReturnDto(data)).collect(Collectors.toList());
            PageDataDto<SalesReturnDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<SalesReturnDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<SalesReturnDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> getSalesReturnDetails(String id) {
        try {
            Optional<SalesReturn> aReturn = salesReturnRepository.findById(Long.valueOf(id));
            if (aReturn.isPresent()) {
                SalesReturn salesReturn = aReturn.get();
                SalesReturnDto salesReturnDto = new SalesReturnDto(salesReturn);
                if(salesReturnDto.getCreatedBy() != null){
                    Optional<User> user = userRepository.findById(salesReturnDto.getCreatedBy());
                    salesReturnDto.setCreatedByName(user.get().getEmployeename());
                }
                if(salesReturnDto.getUpdatedBy() != null && salesReturnDto.getUpdatedBy() > 0 ){
                    Optional<User> user = userRepository.findById(salesReturnDto.getUpdatedBy());
                    salesReturnDto.setUpdatedByName(user.get().getEmployeename());
                }
                if(salesReturnDto.getVerifiedBy() != null && salesReturnDto.getVerifiedBy() > 0 ){
                    Optional<User> user = userRepository.findById(salesReturnDto.getVerifiedBy());
                    salesReturnDto.setVerifyByName(user.get().getEmployeename());
                }
                if(salesReturnDto.getRejectedBy() != null && salesReturnDto.getRejectedBy() > 0 ){
                    Optional<User> user = userRepository.findById(salesReturnDto.getRejectedBy());
                    salesReturnDto.setRejectedByName(user.get().getEmployeename());
                }
                return new ResponseEntity<SalesReturnDto>(salesReturnDto, HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Status>(new Status("400", "SalesReturn NoT FOUND!!!"), HttpStatus.OK);
    }


    public ResponseEntity<?> deleteSalesReturnDetails(String id) throws UsernameNotFoundException {
        try {
            Optional<SalesReturn> salesReturn = salesReturnRepository.findById(Long.valueOf(id));
            if (salesReturn.isPresent()) {
                SalesReturn salesReturn1 = salesReturn.get();
                salesReturnRepository.delete(salesReturn1);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    private String getTrnNo() {
        String trnNo = null;
        Optional<TRNIndex> trnIndex = trnIndexRepo.findByPrefix("Salreturn");
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


    public ResponseEntity<?> salesRejectOrVerify(Long id, String type, User loggedUser) {
        try {
            Optional<SalesReturn> aReturn = salesReturnRepository.findById(Long.valueOf(id));
            if (aReturn.isPresent()) {
                SalesReturn salesReturn = aReturn.get();
                SalesReturnDto salesReturnDto = new SalesReturnDto(salesReturn);
                salesReturn.setStatus(type);
                if (type.equalsIgnoreCase("Verify")) {
                    salesReturn.setVerifiedBy(loggedUser.getId());
                    salesReturn.setVerified_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                } else if (type.equalsIgnoreCase("Reject")) {
                    salesReturn.setRejectedBy(loggedUser.getId());
                    salesReturn.setRejected_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                }
                SalesReturn result = salesReturnRepository.save(salesReturn);
                if (type.equalsIgnoreCase("Verify")) {

                    binStockUtil.updateSalesReturnStock(salesReturnDto, loggedUser);

                } else if (type.equalsIgnoreCase("Reject")) {

                    deleteAllReturnProduct(result.getId());
                }

                return new ResponseEntity<Status>(new Status("200", "SalesReturn updated successfully!!!"), HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Status>(new Status("400", "SalesReturn NoT FOUND!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> deleteReturnProduct(Long id) {

        try {

            Optional<SalesReturnProducts> aReturn = salesReturnProductsRepository.findById(Long.valueOf(id));

            if (aReturn.isPresent()) {

                salesReturnProductsRepository.delete(aReturn.get());

                return new ResponseEntity<Status>(new Status("200", "SalesReturnProducts deleted successfully!!!"), HttpStatus.OK);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Status>(new Status("400", "SalesReturnProducts NOT FOUND!!!"), HttpStatus.OK);
    }

    public void deleteAllReturnProduct(Long id) {


        List<SalesReturnProducts> salesReturnProductsList = salesReturnProductsRepository.findBy(id);
        if (!salesReturnProductsList.isEmpty()) {
            System.out.println("check log");
            for (SalesReturnProducts products : salesReturnProductsList) {
                System.out.println("check log product");
                salesReturnBinDetailsRepository.deleteAll(products.getId());
            }
            salesReturnProductsRepository.deleteList(id);
        }
    }


}
