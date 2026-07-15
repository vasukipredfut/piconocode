package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.MaterialReturnListDTO;
import com.aria.dto.OtherBranchIssueDto;
import com.aria.model.User;
import com.aria.service.MaterialFlowGDCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class MaterialFlowGDCController {

    @Autowired
    MaterialFlowGDCService materialFlowGDCService;


    @Autowired
    UserRepository userRepository;

    @GetMapping("/gdcList")
    public ResponseEntity<?> gdcList(@RequestParam(value = "gdcNo", required = false) String gdcNo,
                                     @RequestParam(value = "issueBranchId", required = false) Long issueBranchId,
                                     @RequestParam(value = "firmId", required = false) Long firmId,
                                     @RequestParam(value = "dateFrom", required = false) String dateFrom,
                                     @RequestParam(value = "dateTo", required = false) String dateTo,
                                     Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return materialFlowGDCService.filterListAll(gdcNo, issueBranchId, firmId , dateFrom, dateTo, pageable);
        } else {
            return new ResponseEntity<List<MaterialReturnListDTO>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/gdcView")
    public ResponseEntity<?> gdcView(@RequestParam(value = "gdcNo", required = false) String gdcNo,
                                     @RequestParam(value = "issueBranchId", required = false) Long issueBranchId,
                                     @RequestParam(value = "reqBranchId", required = false) Long reqBranchId,
                                     Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return materialFlowGDCService.viewGdc(gdcNo, issueBranchId, reqBranchId);
        } else {
            return new ResponseEntity<List<MaterialReturnListDTO>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


}
