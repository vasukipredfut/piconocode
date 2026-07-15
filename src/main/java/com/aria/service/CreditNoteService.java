package com.aria.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aria.dao.BillInwardRepository;
import com.aria.dao.CreditNoteBillRepository;
import com.aria.dao.CreditNoteCategoryRepository;
import com.aria.dao.CreditNoteItemsRepository;
import com.aria.dao.ProductMasterRepository;
import com.aria.dao.ViewAccountsLedgerRepository;
import com.aria.dto.CreditNoteBillDto;
import com.aria.dto.CreditNoteBillItemList;
import com.aria.dto.CreditNoteBillItemListDto;
import com.aria.dto.CreditNoteBillList;
import com.aria.dto.CreditNoteBillListDto;
import com.aria.dto.CreditNoteCategoryDto;
import com.aria.dto.CreditNoteDto;
import com.aria.dto.CreditNoteItemsDto;
import com.aria.dto.CreditNoteItemsListDTO;
import com.aria.dto.CreditNoteList;
import com.aria.dto.PageDataDto;
import com.aria.model.BillInward;
import com.aria.model.CreditNoteBill;
import com.aria.model.CreditNoteCategory;
import com.aria.model.CreditNoteItems;
import com.aria.model.ProductMaster;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.model.ViewAccountsLedger;
import com.aria.utils.DateUtil;

@Service
public class CreditNoteService {

    @Autowired
    public CreditNoteBillRepository creditNoteBillRepository;

    @Autowired
    public DateUtil dateUtil;

    @Autowired
    public BillInwardRepository billInwardRepository;

    @Autowired
    public CreditNoteCategoryRepository creditNoteCategoryRepository;

    @Autowired
    public CreditNoteItemsRepository creditNoteItemsRepository;

    @Autowired
    ViewAccountsLedgerRepository viewAccountsLedgerRepository;

    @Autowired
    ProductMasterRepository productMasterRepository;

    public ResponseEntity<?> saveCreditNote(CreditNoteBillDto creditNoteBillDto, User loggedUser) {
        CreditNoteBill creditNoteBill = new CreditNoteBill(creditNoteBillDto);
        creditNoteBill.setStatus("Pending");
        creditNoteBill.setCreatedBy(loggedUser);
        creditNoteBill.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        creditNoteBillRepository.save(creditNoteBill);
        return new ResponseEntity<Status>(new Status("200", "CreditNote created SuccessFully"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateCreditNote(CreditNoteBillDto dto, User loggedUser) {
        Optional<CreditNoteBill> optional = creditNoteBillRepository.findById(dto.getId());
        if (optional != null) {
            optional.get().setCreditAmount(dto.getCreditAmount());
            optional.get().setCreditNoteQuantity(dto.getCreditNoteQuantity());
            optional.get().setRoundOff(dto.getRoundOff());
        }
        creditNoteBillRepository.save(optional.get());
        return new ResponseEntity<Status>(new Status("200", "CreditNote updated SuccessFully"), HttpStatus.OK);
    }

    public ResponseEntity<?> getCreditNote(Long id, User loggedUser) {

        Optional<CreditNoteBill> creditNoteBillOptional = creditNoteBillRepository.findById(id);
        if (creditNoteBillOptional.isPresent()) {
            CreditNoteBillDto creditNoteBillDto = new CreditNoteBillDto(creditNoteBillOptional.get());

            Optional<BillInward> billInward = billInwardRepository.findByBillNoAndInwardDate(creditNoteBillDto.getInwardBillNumber(), creditNoteBillDto.getVendorId(), dateUtil.getStringTODateWk(creditNoteBillDto.getBillInwardDate()) );
            if (billInward.isPresent()) {
                creditNoteBillDto.setBillDate(dateUtil.getDateTOStringWk(billInward.get().getBillDate()));
            }

            return new ResponseEntity<CreditNoteBillDto>(creditNoteBillDto, HttpStatus.OK);
        }
        return new ResponseEntity<CreditNoteBillDto>(HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> getCreditNoteList(String billNumber, String vendorName, String type, String status,
                                               Long branch, Long firmId, Pageable pageable) {

        Page<CreditNoteList> list = creditNoteBillRepository.findBy(billNumber, vendorName, type, status, branch, firmId, pageable);
        if (list.toList().size() > 0) {
            List<CreditNoteDto> result = list.toList().stream().map(data -> {
                CreditNoteDto purchaseOrderDto = new CreditNoteDto(data);
                return purchaseOrderDto;
            }).collect(Collectors.toList());
            PageDataDto<CreditNoteDto> pageData = new PageDataDto<>(result, list);
            return new ResponseEntity<PageDataDto<CreditNoteDto>>(pageData, HttpStatus.OK);
        }
        return new ResponseEntity<List<CreditNoteDto>>(new ArrayList<>(), HttpStatus.OK);

    }

    public ResponseEntity<?> creditNoteReject(Long creditNoteId) {
        Optional<CreditNoteBill> optional = creditNoteBillRepository.findById(creditNoteId);
        if (optional.isPresent()) {
            optional.get().setStatus("Pending");
            ViewAccountsLedger ledger = viewAccountsLedgerRepository
                    .findByTrnnoAndTrnType(String.valueOf(optional.get().getId()), 3L);
            if (ledger != null) {
                viewAccountsLedgerRepository.delete(ledger);
            }
            if (optional.get().getApprovedBy() != null) {
                optional.get().setApprovedBy(null);
            }
            if (optional.get().getVerifiedBy() != null) {
                optional.get().setVerifiedBy(null);
            }
            if (optional.get().getAuthorizedBy() != null) {
                optional.get().setAuthorizedBy(null);
            }
            creditNoteBillRepository.save(optional.get());
            return new ResponseEntity<Object>(new Status("200", "Credit Note Rejected Sucessfully..."), HttpStatus.OK);
        }
        return ResponseEntity.badRequest().body("Credit Note Id is not exist...");
    }

    public ResponseEntity<?> changeCreditStatus(Long creditNoteId, Optional<User> user, String status) {
        Optional<CreditNoteBill> optional = creditNoteBillRepository.findById(creditNoteId);
        if (optional.isPresent()) {
            if (status.equalsIgnoreCase("verified")) {
                optional.get().setStatus("Verified");
                optional.get().setVerifiedBy(user.get());
                optional.get().setVerified_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                updateAccountLedger(optional.get(), user.get());
            }
            if (status.equalsIgnoreCase("approved")) {
                optional.get().setStatus("Approved");
                optional.get().setApprovedBy(user.get());
                optional.get().setApproved_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            }
            if (status.equalsIgnoreCase("authorized")) {
                optional.get().setStatus("Authorized");
                optional.get().setAuthorizedBy(user.get());
                optional.get().setAuthorized_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            }
            creditNoteBillRepository.save(optional.get());
            return new ResponseEntity<Object>(new Status("200", "Credit note status updated Successfully..."), HttpStatus.OK);
        }
        return ResponseEntity.badRequest().body("Credit Note Id is not exist...");
    }

    public void updateAccountLedger(CreditNoteBill model, User user) {
        ViewAccountsLedger ledger = new ViewAccountsLedger();
        ledger.setPartyId(model.getVendorId());
        ledger.setCompany(user.getOrg_id());
        ledger.setBranch(user.getBranch_id());
        ledger.setTrnno(model.getCreditNoteNumber());
        ledger.setTrnType(3L);
        ledger.setDescription("Credit Note for " + model.getInwardBillNumber());
        ledger.setTrnAmount(model.getCreditAmount());
        ledger.setTrnDate(dateUtil.getISTDate());
        viewAccountsLedgerRepository.save(ledger);
    }

    /* Credit Note Item */

    public ResponseEntity<?> saveCreditNoteItem(List<CreditNoteItemsDto> creditNoteItemsDtoList, User loggedUser) {
        if (!creditNoteItemsDtoList.isEmpty()) {
            for (CreditNoteItemsDto creditNoteItemsDto : creditNoteItemsDtoList) {
                CreditNoteItems creditNoteItems = new CreditNoteItems(creditNoteItemsDto);
                creditNoteItemsRepository.save(creditNoteItems);
                // updateNoteBill(creditNoteItemsDto.getCreditNoteId());
            }
            return new ResponseEntity<Status>(new Status("200", "CreditNoteItems created SuccessFully"), HttpStatus.OK);
        }
        return new ResponseEntity<Status>(new Status("200", "CreditNoteItems not created!!!"), HttpStatus.OK);
    }

    public void updateNoteBill(Long noteId) {

        Optional<CreditNoteBill> creditNoteBillOptional = creditNoteBillRepository.findById(noteId);
        if (creditNoteBillOptional.isPresent()) {
            CreditNoteBill cre = creditNoteBillOptional.get();
            cre.setStatus("Completed");
            creditNoteBillRepository.save(cre);
        }
    }

    public ResponseEntity<?> selectCreditNoteBillList(String billNumber, Long vendorId, String inwardDate,
                                                      String billDate, Long branch, Long firmId) {

        Date inwardDateVal = null;
        if (inwardDate != null) {
            inwardDateVal = dateUtil.getFilterDate(inwardDate);
        }

        Date billDateVal = null;
        if (billDate != null) {
            billDateVal = dateUtil.getFilterDate(billDate);
        }

        List<CreditNoteBillList> list = billInwardRepository.findByCreditNoteBill(vendorId, billNumber, inwardDateVal, billDateVal, branch, firmId);

        if (!list.isEmpty()) {
            List<CreditNoteBillListDto> result = list.stream().map(data -> {
                CreditNoteBillListDto purchaseOrderDto = new CreditNoteBillListDto(data);
                return purchaseOrderDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<CreditNoteBillListDto>>(result, HttpStatus.OK);
        }
        return new ResponseEntity<List<CreditNoteBillListDto>>(new ArrayList<>(), HttpStatus.OK);
    }

    public ResponseEntity<?> getCreditNoteItem(Long noteId, String billNo, User loggedUser) {
        Optional<CreditNoteItems> creditNoteBillOptional = creditNoteItemsRepository.findByBillNoAndNoteId(billNo, noteId);

        if (creditNoteBillOptional.isPresent()) {
            CreditNoteItemsDto creditNoteBillDto = new CreditNoteItemsDto(creditNoteBillOptional.get());
            Optional<ProductMaster> product = productMasterRepository.findById(creditNoteBillOptional.get().getRefId());
            if (product.isPresent()) {
                creditNoteBillDto.setItemName(product.get().getProductName());
            }
            return new ResponseEntity<CreditNoteItemsDto>(creditNoteBillDto, HttpStatus.OK);
        }
        return new ResponseEntity<CreditNoteItemsDto>(HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> getCreditNoteBillItem(Long creditNoteId, User user) {
        List<CreditNoteItems> list = creditNoteItemsRepository.findByCreditNoteId(creditNoteId);
        if (!list.isEmpty()) {

            Optional<CreditNoteBill> creditNoteBill = creditNoteBillRepository.findById(creditNoteId);
            if (creditNoteBill.isPresent()) {
                CreditNoteBillDto creditNoteBillDto = new CreditNoteBillDto(creditNoteBill.get());
                List<CreditNoteItemsListDTO> result = new ArrayList<>();
                for (CreditNoteItems creditNoteItems : list) {
                    CreditNoteItemsListDTO creditItem = new CreditNoteItemsListDTO(creditNoteItems, creditNoteBillDto);
                    Optional<ProductMaster> product = productMasterRepository.findById(creditNoteItems.getRefId());
                    if (product.isPresent()) {
                        creditItem.setItemName(product.get().getProductName());
                    }
                    result.add(creditItem);
                }
                return new ResponseEntity<List<CreditNoteItemsListDTO>>(result, HttpStatus.OK);
            }
        }
        return new ResponseEntity<List<CreditNoteItemsListDTO>>(new ArrayList<>(), HttpStatus.OK);
    }

    public ResponseEntity<?> selectCreditNoteBillItemList(String billNumber, Long vendorId, String inwardDate) {

        Date date = null;
        System.out.println(inwardDate);
        if (inwardDate != null) {
            date = dateUtil.getFilterDate(inwardDate);
        }
        List<CreditNoteBillItemList> list = billInwardRepository.findByCreditNoteBillItems(vendorId, billNumber, date);
        if (!list.isEmpty()) {
            List<CreditNoteBillItemListDto> result = list.stream().map(data -> {
                CreditNoteBillItemListDto purchaseOrderDto = new CreditNoteBillItemListDto(data);
                return purchaseOrderDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<CreditNoteBillItemListDto>>(result, HttpStatus.OK);
        }
        return new ResponseEntity<List<CreditNoteBillItemListDto>>(new ArrayList<>(), HttpStatus.OK);
    }

    /* CreditNote Category List */

    public ResponseEntity<?> selectCreditNoteCategoryList() {
        List<CreditNoteCategory> list = creditNoteCategoryRepository.findAll();
        if (list.size() > 0) {
            List<CreditNoteCategoryDto> result = list.stream().map(data -> {
                CreditNoteCategoryDto purchaseOrderDto = new CreditNoteCategoryDto(data);
                return purchaseOrderDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<CreditNoteCategoryDto>>(result, HttpStatus.OK);
        }
        return new ResponseEntity<List<CreditNoteCategoryDto>>(new ArrayList<>(), HttpStatus.OK);
    }

}