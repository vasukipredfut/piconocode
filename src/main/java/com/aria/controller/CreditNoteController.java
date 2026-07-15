package com.aria.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dao.UserRepository;
import com.aria.dto.CreditNoteBillDto;
import com.aria.dto.CreditNoteItemsDto;
import com.aria.model.User;
import com.aria.service.CreditNoteService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class CreditNoteController {

    @Autowired
    public CreditNoteService creditNoteService;

    @Autowired
    public UserRepository userRepository;

    /* Save Credit Note */
    @PostMapping("/saveCreditNote")
    public ResponseEntity<?> saveCreditNote(@RequestBody CreditNoteBillDto creditNoteBillDto,
                                            Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null)
            return creditNoteService.saveCreditNote(creditNoteBillDto, user.get());
        else
            return ResponseEntity.badRequest().body("User not found");
    }

    @PutMapping("/updateCreditNote")
    public ResponseEntity<?> updateCreditNote(@RequestBody CreditNoteBillDto creditNoteBillDto,
                                              Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null)
            return creditNoteService.updateCreditNote(creditNoteBillDto, user.get());
        else
            return ResponseEntity.badRequest().body("User not found");
    }

    @GetMapping("/getCreditNoteList")
    public ResponseEntity<?> getCreditNoteList(@RequestParam(value = "billNumber", required = false) String billNumber,
                                               @RequestParam(value = "type", required = false) String type,
                                               @RequestParam(value = "status", required = false) String status,
                                               @RequestParam(value = "vendorName", required = false) String vendorName,
                                               @RequestParam(value = "branch", required = false) Long branch,
                                               @RequestParam(value = "firmId", required = false) Long firmId, Pageable pageable,
                                               Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null)
            return creditNoteService.getCreditNoteList(billNumber, vendorName, type, status, branch , firmId, pageable);
        else
            return ResponseEntity.badRequest().body("User not found");

    }

    @GetMapping("/selectCreditNoteBillList")
    public ResponseEntity<?> selectCreditNoteItemList(@RequestParam(value = "vendorId", required = false) Long vendorId,
                                                      @RequestParam(value = "billNumber", required = false) String billNumber,
                                                      @RequestParam(value = "inwardDate", required = false) String inwardDate,
                                                      @RequestParam(value = "billDate", required = false) String billDate,
                                                      @RequestParam(value = "branch", required = false) Long branch,
                                                      @RequestParam(value = "firmId", required = false) Long firmId,
													  Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null)
            return creditNoteService.selectCreditNoteBillList(billNumber, vendorId, inwardDate, billDate, branch , firmId);
        else
            return ResponseEntity.badRequest().body("User not found");

    }

    @GetMapping("/selectCreditNoteBillItemList")
    public ResponseEntity<?> selectCreditNoteItemList(
            @RequestParam(value = "billNumber", required = false) String billNumber,
            @RequestParam(value = "vendorId", required = false) Long vendorId,
            @RequestParam(value = "inwardDate", required = false) String inwardDate, Pageable pageable,
            Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null)
            return creditNoteService.selectCreditNoteBillItemList(billNumber, vendorId, inwardDate);
        else
            return ResponseEntity.badRequest().body("User not found");

    }

    @PostMapping("/saveCreditNoteItems")
    public ResponseEntity<?> saveCreditNoteItems(@RequestBody List<CreditNoteItemsDto> creditNoteItemsDto,
                                                 Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null)
            return creditNoteService.saveCreditNoteItem(creditNoteItemsDto, user.get());
        else
            return ResponseEntity.badRequest().body("User not found");

    }

    @GetMapping("/getCreditNote/{id}")
    public ResponseEntity<?> getCreditNote(@PathVariable("id") Long id, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null)
            return creditNoteService.getCreditNote(id, user.get());
        else
            return ResponseEntity.badRequest().body("User not found");

    }

    @GetMapping("/getCreditNoteItem")
    public ResponseEntity<?> getCreditNoteItem(@RequestParam("noteId") Long noteId,
                                               @RequestParam("billNo") String billNo, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null)
            return creditNoteService.getCreditNoteItem(noteId, billNo, user.get());
        else
            return ResponseEntity.badRequest().body("User not found");

    }

    @GetMapping("/getCreditNoteBillItem")
    public ResponseEntity<?> getCreditNoteItem(@RequestParam("creditNoteId") Long creditNoteId,
                                               Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null)
            return creditNoteService.getCreditNoteBillItem(creditNoteId, user.get());
        else
            return ResponseEntity.badRequest().body("User not found");

    }

    @PostMapping("/changeCreditNoteStatus")
    public ResponseEntity<?> changeCreditNoteStatus(@RequestParam(value = "status", required = true) String status,
                                                    @RequestParam(value = "creditNoteId", required = true) Long creditNoteId, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user != null) {
            if (status.equalsIgnoreCase("Rejected")) {
                return creditNoteService.creditNoteReject(creditNoteId);
            } else {
                return creditNoteService.changeCreditStatus(creditNoteId, user, status);
            }
        } else {
            return ResponseEntity.badRequest().body("User not found");
        }
    }

    /* CreditNote Category List */
    @GetMapping("/getCreditNoteCategoryList")
    public ResponseEntity<?> getCreditNoteCategoryList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null)
            return creditNoteService.selectCreditNoteCategoryList();
        else
            return ResponseEntity.badRequest().body("User not found");

    }

}
