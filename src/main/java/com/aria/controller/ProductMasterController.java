package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.HoProductDto;
import com.aria.dto.ProductMasterDto;
import com.aria.model.User;
import com.aria.service.ProductMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ProductMasterController {

    @Autowired
    ProductMasterService productMasterService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/saveProductMaster")
    public ResponseEntity<?> savePage(@RequestBody ProductMasterDto productMasterDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return productMasterService.saveProductMaster(productMasterDto, user.get());
    }

    @PutMapping("/updateProductMaster")
    public ResponseEntity<?> updatePage(@RequestBody ProductMasterDto productMasterDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return productMasterService.updateProductMaster(productMasterDto, user.get());
    }

    @GetMapping("/getProductMasterList")
    public ResponseEntity<?> getProductMasterList(
            @RequestParam(value = "productName", required = false) String productName,
            @RequestParam(value = "productType", required = false) String productType, Pageable pageable,
            Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return productMasterService.listAllProductMaster(productName, productType, pageable);
        } else {
            return new ResponseEntity<List<ProductMasterDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/ListProductsMaster")
    public ResponseEntity<?> ListProducts(Authentication authentication) {

        return productMasterService.getProductMasterList();
    }

    @GetMapping("/getProductMasterDetails/{id}")
    public ResponseEntity<?> getProductMaster(@PathVariable String id, Authentication authentication) {

        return productMasterService.getProductMaster(id);
    }

    @DeleteMapping("/deleteProductMaster/{id}")
    public ResponseEntity<?> deleteProductMaster(@PathVariable String id, Authentication authentication) {

        return productMasterService.deleteProductMaster(id);
    }

    @GetMapping("/getDropdownList")
    public ResponseEntity<?> getUnitList(@RequestParam("type") String type,
                                         @RequestParam(value = "typeString", required = false) String typeString, Authentication authentication) {

        return productMasterService.getDropdownList(type, typeString);
    }

    @GetMapping("/getProductBlending")
    public ResponseEntity<?> getProductforBlending(Authentication authentication) {

        return productMasterService.getProductforBlending();
    }

    @GetMapping("/product/getAllSearchedProductd")
    public ResponseEntity<?> getAllSearchedProductd(@RequestParam("searchProduct") String searchProduct,
                                                    Authentication authentication) {

        return productMasterService.getAllSearchedProductd(searchProduct);
    }

    @GetMapping("/product/getProductByBranchAndDepartment")
    public ResponseEntity<?> getProductByBranchAndDepartment(
            @RequestParam(value = "searchProduct", required = false) String searchProduct,
            @RequestParam(value = "branchId", required = false) Long branchId,
            @RequestParam(value = "departmentId", required = false) Long departmentId, Authentication authentication) {
        return productMasterService.getProductByBranchAndDepartment(searchProduct, branchId, departmentId);
    }

    @PostMapping("/auth/saveProductMaster")
    public ResponseEntity<?> saveProductMaster(@RequestBody HoProductDto productMasterDto, HttpServletRequest request) {
        return productMasterService.saveProductMaster(productMasterDto, request);
    }

    @GetMapping("/allProductMasterList")
    public ResponseEntity<?> allProductMasterList(@RequestParam(value = "pcategory", required = false) String pcategory,
                                                  @RequestParam(value = "productName", required = false) String productName, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return productMasterService.listAllProductMasterlist(productName, pcategory);
        } else {
            return new ResponseEntity<List<ProductMasterDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getProductMasterListByCategory")
    public ResponseEntity<?> getProductMasterListByCategory(
            @RequestParam(value = "productName", required = false) String productName,
            @RequestParam(value = "productType", required = false) String productType,
            @RequestParam(value = "categoryId", required = false) Long categoryId, Pageable pageable,
            Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return productMasterService.listAllProductMasterByCategory(productName, productType, categoryId, pageable);
        } else {
            return new ResponseEntity<List<ProductMasterDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getProductWithLotList")
    public ResponseEntity<?> getProductWithLotList(
            @RequestParam(value = "productName", required = false) String productName, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return productMasterService.getProductWithLotList(productName);
        } else {
            return new ResponseEntity<List<ProductMasterDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getSplitProductList")
    public ResponseEntity<?> getSplitProductList(
            @RequestParam(value = "productName", required = false) String productName, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return productMasterService.findBYSplit(productName);
        } else {
            return new ResponseEntity<List<ProductMasterDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/restrictLabelOrFlimProductMasterList")
    public ResponseEntity<?> restrictLabelOrFlimProductMasterList(@RequestParam(value = "productName", required = false) String productName,
                                                       @RequestParam(value = "productType", required = false) String productType, Pageable pageable,
                                                       Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return productMasterService.restrictLabelOrFlimProductMasterList(productName, productType, pageable);
        } else {
            return new ResponseEntity<List<ProductMasterDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

}
