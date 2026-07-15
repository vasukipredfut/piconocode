package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.DpEntryProposalParamInspectionScoreDto;
import com.aria.model.DpEntryProposalParamInspectionScore;
import com.aria.model.User;
import com.aria.service.ProposalParamInspectionScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ProposalParamInspectionScoreController {

    @Autowired
    ProposalParamInspectionScoreService proposalParamInspectionScoreService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/paramInspectionScore")
    public ResponseEntity<?> saveParamInspectionScore(@RequestBody List<DpEntryProposalParamInspectionScoreDto> paramInspectionScoreDtos, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return proposalParamInspectionScoreService.saveParamInspectionScore(paramInspectionScoreDtos, user.get());
    }
    
   
    
    @GetMapping("/listparamInspectionScore")
    public ResponseEntity<?> listParamInspectionScore(@RequestParam(value = "billId", required = false) String billId,@RequestParam(value = "itemId", required = false) String itemId, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return proposalParamInspectionScoreService.ListParamInspectionScore(billId, itemId, user.get());
    }
    
    @GetMapping("/getInspectionScore")
    public ResponseEntity<?> getInspectionScore(@RequestParam(value = "billId", required = false) String billId,@RequestParam(value = "itemId", required = false) String itemId, @RequestParam(value = "poId", required = false) String poId,@RequestParam(value = "poNumber", required = false) String poNumber , Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return proposalParamInspectionScoreService.getInspectionScore(poNumber,billId, itemId,poId, user.get());
    }
}
