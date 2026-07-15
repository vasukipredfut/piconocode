package com.aria.service;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.BinStockUtil;
import com.aria.utils.DateUtil;
import com.aria.utils.GDCUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductDistruptanciesService {

    @Autowired
    ProductExcessStorageRepository productExcessStorageRepository;

    @Autowired
    ExcessShortageProductsRepository excessShortageProductsRepository;

    @Autowired
    PurchaseReturnRepository purchaseReturnRepository;


    @Autowired
    PurchaseReturnProductsRepository purchaseReturnProductsRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BinStockLogRepository binStockLogRepository;

    @Autowired
    BinStockRepository binStockRepository;

    @Autowired
    DateUtil dateUtil;
    @Autowired
    BranchRepository branchRepository;

    @Autowired
    BinStockUtil binStockUtil;

    @Autowired
    ExcessStoredBinDetailsRepository excessStoredBinDetailsRepository;

    @Autowired
    PurchaseReturnStoredBinDetailsRepository purchaseReturnStoredBinDetailsRepository;

    @Autowired
    TRNIndexRepo trnIndexRepo;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    VendorRepository vendorRepository;


    @Autowired
    BillInwardItemReceivedRepository billInwardItemReceivedRepository;

    @Autowired
    GDCUtil gdcUtil;

    // Excess/shortage

    public ResponseEntity<?> saveProductExcessStorage(ProductExcessStorageDto productExcessStorageDto, User loggedUser) {

        ProductExcessStorage productExcessStorage = new ProductExcessStorage(productExcessStorageDto);

        productExcessStorage.setBillTransactionno(getTrnNo("SHTGEXCS"));

        productExcessStorage.setBillInwardDate(dateUtil.getStringTODateWk(productExcessStorageDto.getBillInwardDate()));
        productExcessStorage.setBillDate(dateUtil.getStringTODateWk(productExcessStorageDto.getBillDate()));
        productExcessStorage.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        productExcessStorage.setCreatedBy(loggedUser.getId());
        productExcessStorage.setBranchId(loggedUser.getBranch_id());
        ProductExcessStorage result = productExcessStorageRepository.save(productExcessStorage);
        for (ExcessShortageProductsDto excessShortageProductsDto : productExcessStorageDto
                .getExcessShortageProducts()) {
            ExcessShortageProducts excessShortageProducts = new ExcessShortageProducts(excessShortageProductsDto);
            excessShortageProducts.setExcessStorageId(result.getId());
            ExcessShortageProducts excessShortageProducts1 = excessShortageProductsRepository
                    .save(excessShortageProducts);

            if (!excessShortageProductsDto.getBinDetails().isEmpty()) {
                for (ExcessStoredBinDetailsDto excessStoredBinDetailsDto : excessShortageProductsDto.getBinDetails()) {
                    ExcessStoredBinDetails excessStoredBinDetails = new ExcessStoredBinDetails(excessStoredBinDetailsDto);
                    excessStoredBinDetails.setExcessProductId(excessShortageProducts1.getId());
                    ExcessStoredBinDetails excessStoredBinDetails1 = excessStoredBinDetailsRepository.save(excessStoredBinDetails);
                    /*
                     * After verification only binstock should be updated if
                     * (excessShortageProducts.getType().equalsIgnoreCase("Excess")) {
                     * binStockUtil.updateBinStockExcess(result, excessShortageProducts1,
                     * excessStoredBinDetails1, loggedUser); } else {
                     * binStockUtil.updateBinStockShortage(result, excessShortageProducts1,
                     * excessStoredBinDetails1, loggedUser); }
                     */
                }
            }
        }

        // binStockUtil.updateBinStockAuditLog(result, null, "ShortageOrExcess", loggedUser);

        return new ResponseEntity<Status>(new Status("200", "ProductExcessShortage save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateProductExcessStorage(ProductExcessStorageDto productExcessStorageDto,
                                                        User loggedUser) {
        ProductExcessStorage productExcessStorage = new ProductExcessStorage(productExcessStorageDto);

        if (productExcessStorage.getBillTransactionno() != null) {
            productExcessStorage.setBillTransactionno(getTrnNo("SHTGEXCS"));
        }

        if (productExcessStorageDto.getStatus() == null) {
            productExcessStorage.setStatus("Pending");
            productExcessStorage.setRejectedBy(null);
            productExcessStorage.setRejected_date(null);
        }

        productExcessStorage.setBillInwardDate(dateUtil.getStringTODateWk(productExcessStorageDto.getBillInwardDate()));
        productExcessStorage.setBillDate(dateUtil.getStringTODateWk(productExcessStorageDto.getBillDate()));
        productExcessStorage.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        productExcessStorage.setUpdatedBy(loggedUser.getId());
        ProductExcessStorage result = productExcessStorageRepository.save(productExcessStorage);

        for (ExcessShortageProductsDto excessShortageProductsDto : productExcessStorageDto
                .getExcessShortageProducts()) {
            ExcessShortageProducts excessShortageProducts = new ExcessShortageProducts(excessShortageProductsDto);
            excessShortageProducts.setExcessStorageId(result.getId());
            ExcessShortageProducts excessShortageProducts1 = excessShortageProductsRepository
                    .save(excessShortageProducts);

            if (!excessShortageProductsDto.getBinDetails().isEmpty()) {
                for (ExcessStoredBinDetailsDto excessStoredBinDetailsDto : excessShortageProductsDto.getBinDetails()) {
                    ExcessStoredBinDetails excessStoredBinDetails = new ExcessStoredBinDetails(
                            excessStoredBinDetailsDto);
                    excessStoredBinDetails.setExcessProductId(excessShortageProducts1.getId());
                    ExcessStoredBinDetails excessStoredBinDetails1 = excessStoredBinDetailsRepository
                            .save(excessStoredBinDetails);
                    if ("verify".equalsIgnoreCase(productExcessStorageDto.getStatus())) {
                        if (excessShortageProducts.getType().equalsIgnoreCase("Excess")) {
                            binStockUtil.updateBinStockExcess(result, excessShortageProducts1, excessStoredBinDetails1, loggedUser);
                        } else {
                            binStockUtil.updateBinStockShortage(result, excessShortageProducts1, excessStoredBinDetails1, loggedUser);
                        }
                        binStockUtil.updateBinStockAuditLog(result, null, "ShortageOrExcess", loggedUser);
                        result.setStatus("Verified");
                        result.setVerifiedBy(loggedUser.getId());
                        result.setVerified_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                        result.setRejectedBy(null);
                        result.setRejected_date(null);
                    } else if ("reject".equalsIgnoreCase(productExcessStorageDto.getStatus())) {
                        result.setStatus("Rejected");
                        result.setRejectedBy(loggedUser.getId());
                        result.setRejected_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                        deleteAllProduct(result.getId());
                    }
                }
                productExcessStorageRepository.save(result);
            }
        }
        return new ResponseEntity<Status>(new Status("200", "ProductExcessShortage updated successfully!!!"),
                HttpStatus.OK);
    }

    public ResponseEntity<?> listAllProductExcessStorage(Long branchId, String vendorName, String billNo, String type,
                                                         String productName, String dateFrom, String dateTo, String status, Pageable pageable) throws UsernameNotFoundException {

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

        Page<ProductExcessStorageList> list = productExcessStorageRepository.productExcessStorageFilterList(branchId, vendorName,
                billNo, type, productName, startDate, endDate, status, pageable);
        if (!list.toList().isEmpty()) {
            List<ProductExcessStorageListDto> result = list.toList().stream()
                    .map(data -> new ProductExcessStorageListDto(data)).collect(Collectors.toList());
            PageDataDto<ProductExcessStorageListDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<ProductExcessStorageListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<ProductExcessStorageListDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getProductExcessStorageDetails(String id) throws UsernameNotFoundException {

        Optional<ProductExcessStorage> optionalProductExcessStorage = productExcessStorageRepository.findById(Long.parseLong(id));

        ProductExcessStorageDto productExcessStorageDto = new ProductExcessStorageDto(optionalProductExcessStorage.get());
        productExcessStorageDto.setBillInwardDate(dateUtil.getDateTOStringWk(optionalProductExcessStorage.get().getBillInwardDate()));
        productExcessStorageDto.setBillDate(dateUtil.getDateTOStringWk(optionalProductExcessStorage.get().getBillDate()));

        if (productExcessStorageDto.getCreatedBy() != null) {
            Optional<User> userOptional = userRepository.findById(productExcessStorageDto.getCreatedBy());
            if (userOptional.isPresent()) {
                productExcessStorageDto.setCreatedByName(userOptional.get().getEmployeename());
            }
        }

        if (productExcessStorageDto.getUpdatedBy() != null) {
            Optional<User> userOptional = userRepository.findById(productExcessStorageDto.getUpdatedBy());
            if (userOptional.isPresent()) {
                productExcessStorageDto.setUpdatedByName(userOptional.get().getEmployeename());
            }
        }

        if (productExcessStorageDto.getRejectedBy() != null) {
            Optional<User> userOptional = userRepository.findById(productExcessStorageDto.getRejectedBy());
            if (userOptional.isPresent()) {
                productExcessStorageDto.setRejectedByName(userOptional.get().getEmployeename());
            }
        }
        if (productExcessStorageDto.getVerifiedBy() != null) {
            Optional<User> userOptional = userRepository.findById(productExcessStorageDto.getVerifiedBy());
            if (userOptional.isPresent()) {
                productExcessStorageDto.setVerifiedByName(userOptional.get().getEmployeename());
            }
        }

        return new ResponseEntity<ProductExcessStorageDto>(productExcessStorageDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteProductExcessStorage(String id) throws UsernameNotFoundException {
        try {
            Optional<ProductExcessStorage> optionalProductExcessStorage = productExcessStorageRepository
                    .findById(Long.parseLong(id));
            if (optionalProductExcessStorage.isPresent()) {
                ProductExcessStorage billNo = optionalProductExcessStorage.get();
                productExcessStorageRepository.delete(billNo);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public void updateEmployeeDetails(ProductExcessStorageDto productExcessStorage) {

        Optional<User> user = userRepository.findById(productExcessStorage.getCreatedBy());
        if (user.isPresent()) {
            // productExcessStorage.setCreatedByName(user.get().getEmployeeName());
            // productExcessStorage.setUpdatedByName(user.get().getEmployeeName());
        }
        if (productExcessStorage.getUpdatedBy() != null) {
            Optional<User> user1 = userRepository.findById(productExcessStorage.getUpdatedBy());
            if (user1.isPresent()) {
                // productExcessStorage.setUpdatedByName(user.get().getEmployeeName());
            }
        }
    }

    // Purchase Return

    public ResponseEntity<?> savePurchaseReturn(PurchaseReturnDto purchaseReturnDto, User loggedUser) {

        PurchaseReturn purchaseReturn = new PurchaseReturn(purchaseReturnDto);

        purchaseReturn.setBillTransactionNo(getTrnNo("Purreturn"));

        purchaseReturn.setBillInwardDate(dateUtil.getStringTODateWk(purchaseReturnDto.getBillInwardDate()));

        purchaseReturn.setBillDate(dateUtil.getStringTODateWk(purchaseReturnDto.getBillDate()));

        purchaseReturn.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        purchaseReturn.setCreatedBy(loggedUser.getId());
        purchaseReturn.setBranchId(loggedUser.getBranch_id());
        Organisation organisation = new Organisation();
        organisation.setId(loggedUser.getOrg_id());
        purchaseReturn.setFirmOrg(organisation);
        PurchaseReturn result = purchaseReturnRepository.save(purchaseReturn);

        for (PurchaseReturnProductsDto purchaseReturnProductsDto : purchaseReturnDto.getPurchaseReturnProducts()) {
            PurchaseReturnProducts purchaseReturnProducts = new PurchaseReturnProducts(purchaseReturnProductsDto);
            purchaseReturnProducts.setPurchaseReturnId(result.getId());
            PurchaseReturnProducts purchaseReturnProducts1 = purchaseReturnProductsRepository
                    .save(purchaseReturnProducts);

            if (!purchaseReturnProductsDto.getBinDetails().isEmpty()) {
                for (ExcessStoredBinDetailsDto excessStoredBinDetailsDto : purchaseReturnProductsDto.getBinDetails()) {
                    PurchaseReturnStoredBinDetails purchaseReturnStoredBinDetails = new PurchaseReturnStoredBinDetails(
                            excessStoredBinDetailsDto);
                    purchaseReturnStoredBinDetails.setReturnProductId(purchaseReturnProducts1.getId());
                    PurchaseReturnStoredBinDetails excessStoredBinDetails1 = purchaseReturnStoredBinDetailsRepository
                            .save(purchaseReturnStoredBinDetails);
                    /*
                     * After Verify the binstock should be updated
                     * binStockUtil.updateBinStockPurchaseReturn(result, purchaseReturnProducts1, excessStoredBinDetails1, loggedUser);
                     */
                }
            }
        }
//		binStockUtil.updateBinStockAuditLog(null, result, "PurchaseReturn", loggedUser);
        return new ResponseEntity<Status>(new Status("200", "PurchaseReturn save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updatePurchaseReturn(PurchaseReturnDto purchaseReturnDto, User loggedUser) {

        PurchaseReturn purchaseReturn = new PurchaseReturn(purchaseReturnDto);
        if (purchaseReturn.getBillTransactionNo() != null) {
            purchaseReturn.setBillTransactionNo(getTrnNo("Purreturn"));
        }

        purchaseReturn.setBillInwardDate(dateUtil.getStringTODateWk(purchaseReturnDto.getBillInwardDate()));
        purchaseReturn.setBillDate(dateUtil.getStringTODateWk(purchaseReturnDto.getBillDate()));
        purchaseReturn.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        purchaseReturn.setUpdatedBy(loggedUser.getId());
        purchaseReturn.setStatus(purchaseReturnDto.getStatus());
        if (purchaseReturnDto.getStatus() == null) {
            purchaseReturn.setStatus("Pending");
            purchaseReturn.setRejectedBy(null);
            purchaseReturn.setRejected_date(null);
        }

        PurchaseReturn result = purchaseReturnRepository.save(purchaseReturn);
        for (PurchaseReturnProductsDto purchaseReturnProductsDto : purchaseReturnDto.getPurchaseReturnProducts()) {
            PurchaseReturnProducts purchaseReturnProducts = new PurchaseReturnProducts(purchaseReturnProductsDto);
            purchaseReturnProducts.setPurchaseReturnId(result.getId());
            PurchaseReturnProducts purchaseReturnProducts1 = purchaseReturnProductsRepository
                    .save(purchaseReturnProducts);
            if (!purchaseReturnProductsDto.getBinDetails().isEmpty()) {
                for (ExcessStoredBinDetailsDto excessStoredBinDetailsDto : purchaseReturnProductsDto.getBinDetails()) {
                    PurchaseReturnStoredBinDetails purchaseReturnStoredBinDetails = new PurchaseReturnStoredBinDetails(
                            excessStoredBinDetailsDto);
                    purchaseReturnStoredBinDetails.setReturnProductId(purchaseReturnProducts1.getId());
                    PurchaseReturnStoredBinDetails excessStoredBinDetails1 = purchaseReturnStoredBinDetailsRepository
                            .save(purchaseReturnStoredBinDetails);
                    if ("verify".equalsIgnoreCase(purchaseReturn.getStatus())) {
                        binStockUtil.updateBinStockPurchaseReturn(result, purchaseReturnProducts1, excessStoredBinDetails1, loggedUser);
                        result.setStatus("Verified");
                        result.setUpdatedBy(loggedUser.getId());
                        result.setVerifiedBy(loggedUser.getId());
                        result.setVerified_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                        String gdcNo = gdcUtil.getGDCNo(result.getBranchId(), result.getFirmOrg().getId());
                        result.setGDCNo(gdcNo);
                        result.setGdc_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                        result.setRejectedBy(null);
                        result.setRejected_date(null);
                        binStockUtil.updateBinStockAuditLog(null, result, "PurchaseReturn", loggedUser);
                    } else if ("reject".equalsIgnoreCase(purchaseReturn.getStatus())) {
                        result.setStatus("Rejected");
                        result.setUpdatedBy(loggedUser.getId());
                        result.setRejectedBy(loggedUser.getId());
                        result.setRejected_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                        deleteAllPurchaseReturnProduct(result.getId());
                    }
                }
            }
        }
        purchaseReturnRepository.save(result);
        return new ResponseEntity<Status>(new Status("200", "PurchaseReturn  updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllPurchaseReturn(Long branchId, String vendorName, String billNo, String type, String productName,
                                                   String dateFrom, String dateTo, String status, Pageable pageable) throws UsernameNotFoundException {
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
        Page<ProductExcessStorageList> list = purchaseReturnRepository.purchaseReturnFilterList(branchId, vendorName, billNo,
                type, productName, startDate, endDate, status, pageable);
        if (!list.toList().isEmpty()) {
            List<ProductExcessStorageListDto> result = list.toList().stream()
                    .map(data -> new ProductExcessStorageListDto(data)).collect(Collectors.toList());
            PageDataDto<ProductExcessStorageListDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<ProductExcessStorageListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<ProductExcessStorageListDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getPurchaseReturn(String id) throws UsernameNotFoundException {
        Optional<PurchaseReturn> optionalPurchaseReturn = purchaseReturnRepository.findById(Long.parseLong(id));
        PurchaseReturnDto purchaseReturnDto = new PurchaseReturnDto(optionalPurchaseReturn.get());
        purchaseReturnDto.setBillInwardDate(dateUtil.getDateTOStringWk(optionalPurchaseReturn.get().getBillInwardDate()));
        purchaseReturnDto.setBillDate(dateUtil.getDateTOStringWk(optionalPurchaseReturn.get().getBillDate()));
        updateEmployeeDetails(purchaseReturnDto);
        return new ResponseEntity<PurchaseReturnDto>(purchaseReturnDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deletePurchaseReturn(String id) throws UsernameNotFoundException {
        try {
            Optional<PurchaseReturn> optionalPurchaseReturn = purchaseReturnRepository.findById(Long.parseLong(id));
            if (optionalPurchaseReturn.isPresent()) {
                PurchaseReturn billNo = optionalPurchaseReturn.get();
                purchaseReturnRepository.delete(billNo);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public void updateEmployeeDetails(PurchaseReturnDto productPurchaseDetails) {

        Optional<User> user = userRepository.findById(productPurchaseDetails.getCreatedBy());
        if (user.isPresent()) {
            productPurchaseDetails.setCreatedByName(user.get().getEmployeename());
        }
        if (productPurchaseDetails.getUpdatedBy() != null) {
            Optional<User> user1 = userRepository.findById(productPurchaseDetails.getUpdatedBy());
            if (user1.isPresent()) {
                productPurchaseDetails.setUpdatedByName(user.get().getEmployeename());
            }
        }
        if (productPurchaseDetails.getRejectedBy() != null) {
            Optional<User> userOptional = userRepository.findById(productPurchaseDetails.getRejectedBy());
            if (userOptional.isPresent()) {
                productPurchaseDetails.setRejectedByName(userOptional.get().getEmployeename());
            }
        }
        if (productPurchaseDetails.getVerifiedBy() != null) {
            Optional<User> userOptional = userRepository.findById(productPurchaseDetails.getVerifiedBy());
            if (userOptional.isPresent()) {
                productPurchaseDetails.setVerifiedByName(userOptional.get().getEmployeename());
            }
        }

    }

    public ResponseEntity<?> getPurchaseReceipt(String id, User loggeUser) {
        Long purchaseReturnId = Long.parseLong(id);
        Optional<PurchaseReturn> OptionalpurchaseReturn = purchaseReturnRepository.findById(purchaseReturnId);
        if (OptionalpurchaseReturn.isPresent()) {
            PurchaseReturn purchaseReturn = OptionalpurchaseReturn.get();
            List<PurchaseReturnProducts> purchaseReturnProductsList = purchaseReturnProductsRepository.findByPurchaseReturnId(purchaseReturn.getId());
            System.out.println(purchaseReturn.getId());
            List<PurchaseReturnStoredBinDetails> binDetailsList = new ArrayList<>();
            for (PurchaseReturnProducts product : purchaseReturnProductsList) {
                Long productId = product.getId();
                List<PurchaseReturnStoredBinDetails> binDetails = purchaseReturnStoredBinDetailsRepository.findByReturnProductId(productId);
                binDetailsList.addAll(binDetails);
            }
            PurchaseReturnReceiptDto purchaseReturnReceipt = new PurchaseReturnReceiptDto(purchaseReturn, userRepository,  binStockRepository,  billInwardItemReceivedRepository);
            purchaseReturnReceipt.setBillInwardDate(dateUtil.getDateTOStringWk(purchaseReturn.getBillInwardDate()));
            purchaseReturnReceipt.setBillDate(dateUtil.getDateTOStringWk(purchaseReturn.getBillDate()));

            if (purchaseReturn.getFirmOrg() != null) {

                CompanyList companyList = organisationRepository.findByFirm(purchaseReturn.getFirmOrg().getId());
                if (companyList != null) {
                    purchaseReturnReceipt.setFirmDetails(new Company(companyList));
                }

                if (purchaseReturn.getBranchId() != null) {
                    BranchAddressList branchAddressList = branchRepository.getBranchByIdANDOrgId(purchaseReturn.getBranchId() , purchaseReturn.getFirmOrg().getId());
                    if (branchAddressList != null) {
                        purchaseReturnReceipt.setFromBranch(new BranchAddress(branchAddressList));
                    }else {
                        BranchAddressList branchAddressList1 = branchRepository.getBranchId(purchaseReturn.getBranchId());
                        purchaseReturnReceipt.setFromBranch(new BranchAddress(branchAddressList1));
                    }
                }
            }

            if (purchaseReturn.getVendor() != null) {
                PartyList partyList = vendorRepository.findByVendor(purchaseReturn.getVendor().getId());
                if (partyList != null)
                    purchaseReturnReceipt.setVendorDetails(new PartyDetails(partyList));
            }

            return new ResponseEntity<PurchaseReturnReceiptDto>(purchaseReturnReceipt, HttpStatus.OK);
        }
        return new ResponseEntity<Status>(new Status("304", "Could not found Record"), HttpStatus.NOT_MODIFIED);
    }


    public ResponseEntity<?> checkValidBill(String type, Long vendorId, String billNo) throws UsernameNotFoundException {
        try {
            if (type.equalsIgnoreCase("ExcessStorage")) {
                Optional<ProductExcessStorage> productExcessStorage = productExcessStorageRepository.findBy(vendorId, billNo);
                if (productExcessStorage.isPresent()) {
                    return new ResponseEntity<Status>(new Status("400", "This Bill No Already Exists!!!"), HttpStatus.OK);
                }
            } else if (type.equalsIgnoreCase("PurchaseReturn")) {
                Optional<PurchaseReturn> productExcessStorage = purchaseReturnRepository.findByReturn(vendorId, billNo);
                if (productExcessStorage.isPresent()) {
                    return new ResponseEntity<Status>(new Status("400", "This Bill No Already Exists!!!"), HttpStatus.OK);
                }
            }
            return new ResponseEntity<Status>(new Status("404", "NOT FOUND"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not FOUND Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    private String getTrnNo(String type) {
        String trnNo = null;

        Optional<TRNIndex> trnIndex = trnIndexRepo.findByPrefix(type);

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


    void deleteAllProduct(Long id) {
        List<ExcessShortageProducts> excessShortageProductsList = excessShortageProductsRepository.findBy(id);
        if (!excessShortageProductsList.isEmpty()) {
            excessShortageProductsRepository.deleteAll(excessShortageProductsList);
        }
    }


	void deleteAllPurchaseReturnProduct(Long id) {
		try {
            purchaseReturnProductsRepository.deleteByPurchaseReturnId(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
