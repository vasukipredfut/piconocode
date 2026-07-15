package com.aria.controller;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;

import com.aria.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.aria.dao.UserRepository;
import com.aria.model.User;
import com.aria.service.ProposalService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ProposalController {

    @Autowired
    ProposalService proposalService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/saveproposal")
    public ResponseEntity<?> saveproposal(@RequestBody ProposalDto proposalDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return proposalService.createProposal(proposalDto, user.get());
    }

    @PostMapping("/saveProposalVendor")
    public ResponseEntity<?> saveProposalVendor(@RequestBody List<ProposalVendorSpecDto> proposalDto,
                                                Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return proposalService.saveProposalVendor(proposalDto, user.get());
    }

    @GetMapping("/listproposal")
    public ResponseEntity<?> listproposal(@RequestParam(value = "status", required = false) String status,
                                          @RequestParam(value = "firmId", required = false) Long firmId,
                                          @RequestParam(value = "branch", required = false) Long branch,
                                          @RequestParam(value = "dept", required = false) Long dept,
                                          @RequestParam(value = "proposalId", required = false) Long proposalId,
                                          @RequestParam(value = "specId", required = false) Long specId,
                                          @RequestParam(value = "category", required = false) Long category,
                                          @RequestParam(value = "specType", required = false) Long specType, Pageable pageable,
                                          Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return proposalService.ListProposal(status, proposalId , specId, user.get(), pageable, category, specType, firmId, dept, branch);
    }


    @GetMapping("/validateActiveProductByVendor")
    public ResponseEntity<?> validateActiveProductByVendor(@RequestParam("specType") Long specType,
                                                           @RequestParam("specId") Long specId,
                                                           @RequestParam("vendorId") Long vendorId,
                                                           @RequestParam(value = "firmId",required = false) Long firmId,
                                                           Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return proposalService.ValidateSpecWithVendor(specType, specId, vendorId, firmId);
    }


    @PostMapping("/validateActiveProduct")
    public ResponseEntity<?> validateActiveProduct(@RequestBody List<ValidateSpecDto> validateSpecDto,
                                                   Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return proposalService.ValidateSpec(validateSpecDto, user.get());
    }

    @GetMapping("/getproposal")
    public ResponseEntity<?> getproposal(@RequestParam(value = "id", required = false) Long id, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return proposalService.getProposal(id, user.get());
    }

    @GetMapping("/getSpecificationByProdIdAndType")
    public ResponseEntity<?> getproposalSpecByProdId(@RequestParam(value = "prodId", required = false) Long id,
                                                     @RequestParam(value = "specType", required = false) Long specType, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return new ResponseEntity<SpecificationDto>(proposalService.getProposalByProdAndSpecType(id, specType), HttpStatus.OK);
    }

    @GetMapping("/getproposal/vendorlist")
    public ResponseEntity<?> getproposalvendorlist(@RequestParam(value = "id", required = false) Long id,
                                                   Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return proposalService.getProposal(id, user.get());
    }

    @PostMapping("/proposal/approval")
    public ResponseEntity<?> approvalproposal(@RequestBody ProposalDto proposalDtos, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return proposalService.proposalApproval(proposalDtos, user.get());

    }

    @PostMapping("/changeProposalStatus")
    public ResponseEntity<?> changeProposalStatus(@RequestParam(value = "status", required = true) String status,
                                                  @RequestParam(value = "proposalId", required = true) Long proposalId, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user != null) {
            if (status.equalsIgnoreCase("Reject")) {
                return proposalService.proposalReject(proposalId, user.get());
            } else {
                return proposalService.changeProposalStatus(proposalId, user, status);
            }
        } else {
            return ResponseEntity.badRequest().body("User not found");
        }
    }

    @PostMapping("/proposal/quotation/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("proposalId") Long proposalId,
                                        // @RequestParam("vendorid") Long vendorid,
                                        @RequestParam("file") MultipartFile file, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        // vendorid
        return proposalService.UploadFile(file, proposalId, user.get());

    }

    @GetMapping("/proposal/quotation/download")
    public ResponseEntity<?> getProposalDownload(@RequestParam("proposalId") Long proposalId,
                                                 // @RequestParam("vendorid") Long vendorid,
                                                 Authentication authentication) throws FileNotFoundException {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        // vendorid,
        return proposalService.downloadFile(proposalId, user.get());
    }


    @PutMapping("/proposal/closeProposal")
    public ResponseEntity<?> closeProposal(@RequestParam("proposalId") Long proposalId,
                                           @RequestParam(value = "proposalSpecId", required = false) Long proposalSpecId,
                                           @RequestParam(value = "proposalSpecIdVendorId", required = false) Long proposalSpecIdVendorId,
                                           @RequestParam("type") String type,
                                           @RequestParam("active") boolean active,
                                           @RequestParam(value = "status", required = false) String status,
                                           Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return proposalService.closeProposal(proposalId, proposalSpecId, proposalSpecIdVendorId, type, active, status);
    }



    @PutMapping("/proposal/closeVendorSpec")
    public ResponseEntity<?> closeVendorSpec(@RequestBody List<CloseVendorSpec> closeVendorSpec,
                                           Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return proposalService.closeVendorSpec(closeVendorSpec, user.get());
    }


    @PutMapping("/updateProposalVendorBySpec")
    public ResponseEntity<?> updateProposalVendorBySpec(@RequestBody List<VendorSpecDto> proposalDto,
                                                        Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return proposalService.updateProposalVendorBySpec(proposalDto, user.get());
    }



    @DeleteMapping("/deleteProposalSpec")
    public ResponseEntity<?> deleteProposalSpec(  @RequestParam("proposalId") Long proposalId,
                                                  @RequestParam("proposalSpecId") Long proposalSpecId,
                                                        Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return proposalService.deleteProposalSpec(proposalId , proposalSpecId);
    }

}
