package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.service.FactoryToHoService;
import com.aria.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class FactoryToHoController {

    @Autowired
    FactoryToHoService hoService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RestService restService;


    // INVENTRY


    @GetMapping("/api/auth/currentStockList")
    public ResponseEntity<?> currentStockList( @RequestParam(value = "branchId", required = false) Long branchId,
                                               @RequestParam(value = "productId", required = false) Long productId,
                                               @RequestParam(value = "binNo", required = false) String binNo,
                                               @RequestParam(value = "productCategory", required = false) String productCategory,
                                               @RequestParam(value = "productName", required = false) String productName,
                                               @RequestParam(value = "lot", required = false) Double lot, Pageable pageable ) {

       
            return hoService.currentStockList(branchId, productId, productName, productCategory, binNo, lot, pageable);
      
    }






    /*

    //PurchaseClaimPremium


    @PostMapping("/api/purchaseClaimPremium")
    public ResponseEntity<?> savePurchaseClaimPremium(@RequestBody PurchaseClaimPremiumDto purchaseClaimPremiumDto,
                                                      Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return hoService.savePurchaseClaimPremium(purchaseClaimPremiumDto, user.get());
    }

    @PutMapping("/api/purchaseClaimPremium")
    public ResponseEntity<?> updatePurchaseClaimPremium(@RequestBody PurchaseClaimPremiumDto purchaseClaimPremiumDto,
                                                        Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return hoService.updatePurchaseClaimPremium(purchaseClaimPremiumDto, user.get());
    }

    @GetMapping("/api/getPurchaseClaimPremiumList")
    public ResponseEntity<?> listAllWorkFlow(Authentication authentication, Pageable pageable) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return hoService.listAllPurchaseClaimPremium(pageable);
        } else {
            return new ResponseEntity<List<WorkFlowDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/api/getPurchaseClaimPremiumDetails/{id}")
    public ResponseEntity<?> getPurchaseClaimPremiumDetails(@PathVariable String id, Authentication authentication) {

        return hoService.getPurchaseClaimPremiumDetails(id);
    }

    @DeleteMapping("/api/deletePurchaseClaimPremiumDetails/{id}")
    public ResponseEntity<?> deletePurchaseClaimPremiumDetails(@PathVariable String id, Authentication authentication) {

        return hoService.deletePurchaseClaimPremiumDetails(id);
    }


    //PurchaseClaimPremiumDetail


    @PostMapping("/api/purchaseClaimPremiumDetail")
    public ResponseEntity<?> savePurchaseClaimPremium(@RequestBody PurchaseClaimPremiumDetailDto purchaseClaimPremiumDto,
                                                      Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return hoService.savePurchaseClaimPremiumDetail(purchaseClaimPremiumDto, user.get());
    }

    @PutMapping("/api/purchaseClaimPremiumDetail")
    public ResponseEntity<?> updatePurchaseClaimPremiumDetail(@RequestBody PurchaseClaimPremiumDetailDto purchaseClaimPremiumDto,
                                                              Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return hoService.updatePurchaseClaimPremiumDetail(purchaseClaimPremiumDto, user.get());
    }

    @GetMapping("/api/getPurchaseClaimPremiumDetailList")
    public ResponseEntity<?> listAllPurchaseClaimPremiumDetail(Authentication authentication, Pageable pageable) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return hoService.listAllPurchaseClaimPremiumDetail(pageable);
        } else {
            return new ResponseEntity<List<WorkFlowDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/api/getPurchaseClaimPremiumDetailById/{id}")
    public ResponseEntity<?> getPurchaseClaimPremiumDetailById(@PathVariable String id, Authentication authentication) {

        return hoService.getPurchaseClaimPremiumDetailById(id);
    }

    @DeleteMapping("/api/deletePurchaseClaimPremiumDetailById/{id}")
    public ResponseEntity<?> deletePurchaseClaimPremiumDetailById(@PathVariable String id, Authentication authentication) {

        return hoService.deletePurchaseClaimPremiumDetailById(id);
    }


    //PurchaseClaimPremiumBreakup


    @PostMapping("/api/purchaseClaimPremiumBreakup")
    public ResponseEntity<?> savePurchaseClaimPremiumBreakup(@RequestBody PurchaseClaimPremiumBreakupDto purchaseClaimPremiumDto,
                                                             Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return hoService.savePurchaseClaimPremiumBreakup(purchaseClaimPremiumDto, user.get());
    }

    @PutMapping("/api/purchaseClaimPremiumBreakup")
    public ResponseEntity<?> updatePurchaseClaimPremiumBreakup(@RequestBody PurchaseClaimPremiumBreakupDto purchaseClaimPremiumDto,
                                                               Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return hoService.updatePurchaseClaimPremiumBreakup(purchaseClaimPremiumDto, user.get());
    }

    @GetMapping("/api/getPurchaseClaimPremiumBreakupList")
    public ResponseEntity<?> listAllPurchaseClaimPremiumBreakup(Authentication authentication, Pageable pageable) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return hoService.listAllPurchaseClaimPremiumBreakup(pageable);
        } else {
            return new ResponseEntity<List<WorkFlowDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/api/getPurchaseClaimPremiumBreakupDetailById/{id}")
    public ResponseEntity<?> getPurchaseClaimPremiumBreakup(@PathVariable String id, Authentication authentication) {

        return hoService.getPurchaseClaimPremiumBreakupDetailById(id);
    }

    @DeleteMapping("/api/deletePurchaseClaimPremiumBreakupDetailById/{id}")
    public ResponseEntity<?> deletePurchaseClaimPremiumBreakup(@PathVariable String id, Authentication authentication) {

        return hoService.deletePurchaseClaimPremiumBreakupDetailById(id);
    }


    //PurchaseBrokerage


    @PostMapping("/api/purchaseBrokerage")
    public ResponseEntity<?> savePurchaseBrokerage(@RequestBody PurchaseBrokerageDto purchaseBrokerageDto,
                                                   Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return hoService.savePurchaseBrokerage(purchaseBrokerageDto, user.get());
    }

    @PutMapping("/api/purchaseBrokerage")
    public ResponseEntity<?> updatePurchaseBrokerage(@RequestBody PurchaseBrokerageDto purchaseBrokerageDto,
                                                     Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return hoService.updatePurchaseBrokerage(purchaseBrokerageDto, user.get());
    }

    @GetMapping("/api/purchaseBrokerageList")
    public ResponseEntity<?> listAllPurchaseBrokerage(Authentication authentication, Pageable pageable) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return hoService.listAllPurchaseBrokerage(pageable);
        } else {
            return new ResponseEntity<List<WorkFlowDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/api/purchaseBrokerageById/{id}")
    public ResponseEntity<?> purchaseBrokerageById(@PathVariable String id, Authentication authentication) {

        return hoService.getPurchaseBrokerageById(id);
    }

    @DeleteMapping("/api/deletePurchaseBrokerage/{id}")
    public ResponseEntity<?> deletePurchaseBrokerage(@PathVariable String id, Authentication authentication) {

        return hoService.deletePurchaseBrokerageById(id);
    }


    //PurchaseBrokerageBreakup


    @PostMapping("/api/purchaseBrokerageBreakup")
    public ResponseEntity<?> savePurchaseBrokerage(@RequestBody PurchaseBrokerageBreakupDto purchaseBrokerageDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return hoService.savePurchaseBrokerageBreakup(purchaseBrokerageDto, user.get());
    }

    @PutMapping("/api/purchaseBrokerageBreakup")
    public ResponseEntity<?> updatePurchaseBrokerage(@RequestBody PurchaseBrokerageBreakupDto purchaseBrokerageDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return hoService.updatePurchaseBrokerageBreakup(purchaseBrokerageDto, user.get());
    }

    @GetMapping("/api/purchaseBrokerageBreakupList")
    public ResponseEntity<?> listAllPurchaseBrokerageBreakup(Authentication authentication, Pageable pageable) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return hoService.listAllPurchaseBrokerageBreakup(pageable);
        } else {
            return new ResponseEntity<List<WorkFlowDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/api/purchaseBrokerageBreakup/{id}")
    public ResponseEntity<?> purchaseBrokerageBreakup(@PathVariable String id, Authentication authentication) {

        return hoService.getPurchaseBrokerageBreakupById(id);
    }

    @DeleteMapping("/api/deletePurchaseBrokerageBreakup/{id}")
    public ResponseEntity<?> deletePurchaseBrokerageBreakup(@PathVariable String id, Authentication authentication) {

        return hoService.deletePurchaseBrokerageBreakupById(id);
    }
*/
    /*@PostMapping("/api/auth/checkAPi")
    public ResponseEntity<?> checkAPi() {

        return new ResponseEntity<>(restService.CheckApi(),HttpStatus.OK);
    }*/
}
