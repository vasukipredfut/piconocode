package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.MaterialReturnListDTO;
import com.aria.model.User;
import com.aria.service.MaterialReturnService;
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
public class MaterialReturnController {


    @Autowired
    MaterialReturnService materialReturnService;

    @Autowired
    UserRepository userRepository;


    @GetMapping("/getMaterialReturnList")
    public ResponseEntity<?> getMaterialIssueList(@RequestParam(value = "requestDeptName", required = false) String requestDeptName,
                                                  @RequestParam(value = "requestBranchName", required = false) String requestBranchName,
                                                  @RequestParam(value = "itemName", required = false) String itemName,
                                                  @RequestParam(value = "dateFrom", required = false) String dateFrom,
                                                  @RequestParam(value = "dateTo", required = false) String dateTo,
                                                  @RequestParam(value = "firmId", required = false) Long firmId,
                                                  @RequestParam(value = "issuebranch", required = false) Long issuebranch,
                                                  @RequestParam(value = "issueDept", required = false) Long issueDept,
                                                  Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return materialReturnService.filterListAll(firmId , requestDeptName, requestBranchName, itemName, dateFrom, dateTo, issuebranch , issueDept , pageable);
        } else {
            return new ResponseEntity<List<MaterialReturnListDTO>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @PutMapping("/updateMaterialReturn")
    public ResponseEntity<?> updateMaterialReturn(@RequestParam("id") Long id, @RequestParam("returnId") Long returnId, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return materialReturnService.updateMaterialReturn(id, returnId, user.get());
        } else {
            return new ResponseEntity<List<MaterialReturnListDTO>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/getMaterialReturn")
    public ResponseEntity<?> getMaterialReturn(@RequestParam("id") Long id, @RequestParam("returnId") Long returnId, Authentication authentication) {
        return materialReturnService.getMaterialReturn(id, returnId);
    }


}
